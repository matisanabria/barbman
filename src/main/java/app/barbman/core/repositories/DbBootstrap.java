package app.barbman.core.repositories;

import app.barbman.core.infrastructure.FlywayMigrator;
import app.barbman.core.infrastructure.HibernateUtil;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.KnownFolders;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.PointerByReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Resolves the application's data directory (Windows Documents via JNA),
 * sets up the folder structure, runs Flyway migrations, and initializes Hibernate.
 *
 * Table creation and schema migrations are handled by Flyway (db/migration/*.sql).
 * This class is only responsible for paths, directories, and startup wiring.
 */
public class DbBootstrap {

    private static final Logger logger = LogManager.getLogger(DbBootstrap.class);
    private static final String DB_NAME = "database.db";
    private static final File DOCUMENTS_FOLDER = resolveDocumentsFolder();

    private static File appFolder;
    private static File dataFolder;
    private static File dbFile;

    public static void init() {
        logger.info("[DB] Initializing application data directory...");
        logger.info("[DB] Windows Documents resolved at: {}", DOCUMENTS_FOLDER.getAbsolutePath());

        appFolder = new File(DOCUMENTS_FOLDER, "Barbman Data");
        ensureDir(appFolder);

        dataFolder = new File(appFolder, "data");
        ensureDir(dataFolder);

        dbFile = new File(dataFolder, DB_NAME);
        logger.info("[DB] Database path: {}", dbFile.getAbsolutePath());

        initializeAvatarsFolder();

        FlywayMigrator.migrate(dbFile.getAbsolutePath());
        HibernateUtil.init(dbFile.getAbsolutePath());
    }

    // ============================================================
    // ====================== JDBC (legacy) =======================
    // ============================================================

    /**
     * Returns a raw JDBC connection. Used by repositories not yet migrated to Hibernate.
     * Prefer {@link HibernateUtil#createEntityManager()} for new code.
     */
    public static Connection connect() throws SQLException {
        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        logger.debug("[DB] Connecting via JDBC. URL: {}", url);
        return DriverManager.getConnection(url);
    }

    // ============================================================
    // ======================= BACKUP =============================
    // ============================================================

    /**
     * Creates a timestamped backup of the database. Keeps only the 7 most recent backups.
     */
    public static void backupDatabase() {
        try {
            File backupFolder = new File(appFolder, "backups");
            ensureDir(backupFolder);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            Path target = backupFolder.toPath().resolve("database_backup_" + timestamp + ".db");

            Files.copy(dbFile.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            logger.info("[DB] Backup created: {}", target);

            File[] backups = backupFolder.listFiles((dir, name) -> name.endsWith(".db"));
            if (backups != null && backups.length > 7) {
                Arrays.stream(backups)
                        .sorted(Comparator.comparingLong(File::lastModified).reversed())
                        .skip(7)
                        .forEach(f -> {
                            if (f.delete()) logger.info("[DB] Removed old backup: {}", f.getName());
                        });
            }
        } catch (IOException e) {
            logger.error("[DB] Error creating database backup: {}", e.getMessage());
        }
    }

    // ============================================================
    // ======================= ACCESSORS ==========================
    // ============================================================

    public static File getAppFolder() { return appFolder; }
    public static File getAvatarsFolder() { return new File(appFolder, "avatars"); }
    public static String getDbPath() { return dbFile.getAbsolutePath(); }

    // ============================================================
    // ======================= INTERNALS ==========================
    // ============================================================

    private static void ensureDir(File dir) {
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Failed to create directory: " + dir.getAbsolutePath());
        }
    }

    private static void initializeAvatarsFolder() {
        try {
            File avatarsFolder = new File(appFolder, "avatars");
            if (!avatarsFolder.exists() && !avatarsFolder.mkdirs()) {
                logger.warn("[DB] Failed to create avatars folder");
                return;
            }

            File defaultAvatar = new File(avatarsFolder, "default.png");
            if (!defaultAvatar.exists()) {
                try (var in = DbBootstrap.class.getResourceAsStream("/app/barbman/core/assets/avatars/default.png")) {
                    if (in != null) {
                        Files.copy(in, defaultAvatar.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        logger.info("[DB] Default avatar copied to: {}", defaultAvatar.getAbsolutePath());
                    }
                }
            }
        } catch (IOException e) {
            logger.error("[DB] Error initializing avatars folder: {}", e.getMessage());
        }
    }

    private static File resolveDocumentsFolder() {
        try {
            PointerByReference pbr = new PointerByReference();
            WinNT.HRESULT hr = Shell32.INSTANCE.SHGetKnownFolderPath(
                    KnownFolders.FOLDERID_Documents, 0, null, pbr);

            if (!WinNT.S_OK.equals(hr)) {
                throw new IllegalStateException("Failed to resolve Windows 'Documents' folder. HRESULT=" + hr);
            }

            Pointer ptr = pbr.getValue();
            if (ptr == null) throw new IllegalStateException("Native pointer for 'Documents' folder is null.");

            String path = ptr.getWideString(0);
            Ole32.INSTANCE.CoTaskMemFree(ptr);
            return new File(path);

        } catch (Exception e) {
            throw new RuntimeException("Critical error while resolving Documents folder.", e);
        }
    }
}
