package app.barbman.core.repositories;

import app.barbman.core.repositories.migrations.RemoveCascadeMigration;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Ole32;
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
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import com.sun.jna.platform.win32.KnownFolders;
import com.sun.jna.platform.win32.Shell32;

/**
 * Initializes and manages the SQLite database.
 * Ensures the database file and required tables exist, and provides
 * methods for connecting to the database.
 * Also includes functionality for creating database backups.
 */
public class DbBootstrap {
    private static final Logger logger = LogManager.getLogger(DbBootstrap.class);
    private static final String DB_NAME = "database.db";
    private static final File DOCUMENTS_FOLDER = getRealDocumentsFolder();

    private static File appFolder;
    private static File dataFolder;
    private static File dbFile;

    public static void init() {
        logger.info("[DB] Ensuring database exists...");

        logger.info("[DB] Windows Documents resolved at: {}", DOCUMENTS_FOLDER.getAbsolutePath());

        // Create app folder in documents if it doesn't exist
        appFolder = new File(DOCUMENTS_FOLDER, "Barbman Data");
        if (!appFolder.exists() && !appFolder.mkdirs()) {
            throw new IllegalStateException(
                    "Failed to create application folder: " + appFolder.getAbsolutePath()
            );
        }

        // Create database folder path
        dataFolder = new File(appFolder, "data");
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new IllegalStateException(
                    "Failed to create data folder: " + dataFolder.getAbsolutePath()
            );
        }

        // Create or verify database file
        dbFile = new File(dataFolder, DB_NAME);
        if (dbFile.exists()) {
            logger.info("[DB] Database found at {}", dbFile.getAbsolutePath());
        } else {
            logger.warn("[DB] Database file missing. Creating new database...");
            createTables();  // This already throws on failure
            logger.info("[DB] New database created successfully.");
        }

        // Initialize payment methods (always runs, only inserts if empty)
        initializePaymentMethods();

        // Migrate users table to add avatar_path column if needed
        migrateUsersAvatarPath();

        // Initialize avatars folder
        initializeAvatarsFolder();
        runCascadeRemovalMigration();
    }

    /**
     * Connect to the SQLite database.
     *
     * @return Database connection
     * @throws SQLException if a database access error occurs
     */
    public static Connection connect() throws SQLException {
        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        logger.debug("[DB] Connecting to database. URL: {}", url);
        Connection conn = DriverManager.getConnection(url);
        logger.debug("[DB] Database connected and ready to use.");
        return conn;
    }

    /**
     * Creates the necessary tables in the database
     */
    /**
     * Creates the necessary tables in the database
     */
    private static void createTables() {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            logger.info("[DB] Creating database tables...");

            // USERS
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        displayName TEXT NOT NULL,
                        role TEXT NOT NULL,
                        pin TEXT NOT NULL UNIQUE CHECK(length(pin) = 4 AND pin GLOB '[0-9][0-9][0-9][0-9]'),
                        payment_type INTEGER NOT NULL DEFAULT 0,
                        pay_frequency TEXT NOT NULL DEFAULT 'WEEKLY'
                                    CHECK (pay_frequency IN ('DAILY', 'WEEKLY', 'BIWEEKLY', 'MONTHLY')),
                        param_1 REAL,
                        param_2 REAL
                    );
                """);

            // SERVICE DEFINITION
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS service_definition (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        displayName TEXT NOT NULL UNIQUE,
                        base_price REAL NOT NULL,
                        available INTEGER NOT NULL DEFAULT 1 CHECK (available IN (0,1))
                    );
                """);

            // SERVICE HEADER - SIN CASCADE
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS service_header (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        sale_id INTEGER NOT NULL,
                        date TEXT NOT NULL CHECK (date = date(date)),
                        subtotal REAL NOT NULL DEFAULT 0,
                        FOREIGN KEY (user_id) REFERENCES users(id),
                        FOREIGN KEY (sale_id) REFERENCES sales(id)
                    );
                """);

            // SERVICE ITEM - SIN CASCADE
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS service_item (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        service_header_id INTEGER NOT NULL,
                        service_definition_id INTEGER NOT NULL,
                        quantity INTEGER NOT NULL DEFAULT 1 CHECK (quantity > 0),
                        unit_price REAL NOT NULL,
                        item_total REAL NOT NULL DEFAULT 0,
                        FOREIGN KEY (service_header_id) REFERENCES service_header(id),
                        FOREIGN KEY (service_definition_id) REFERENCES service_definition(id)
                    );
                """);

            // PRODUCTS
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS products (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        displayName TEXT NOT NULL UNIQUE,
                        cost_price REAL NOT NULL,
                        unit_price REAL NOT NULL,
                        stock INTEGER NOT NULL DEFAULT 0,
                        category TEXT,
                        brand TEXT,
                        image_path TEXT,
                        notes TEXT NOT NULL
                    );
                """);

            // PRODUCT SALES - SIN CASCADE
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS product_sales (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        sale_id INTEGER NOT NULL,
                        subtotal REAL NOT NULL,
                        FOREIGN KEY (sale_id) REFERENCES sales(id)
                    );
                """);

            // PRODUCT SALE ITEMS - SIN CASCADE
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS product_sale_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        product_header_id INTEGER NOT NULL,
                        product_id INTEGER NOT NULL,
                        quantity INTEGER NOT NULL CHECK (quantity > 0),
                        unit_price REAL NOT NULL,
                        item_total REAL NOT NULL,
                        FOREIGN KEY (product_header_id) REFERENCES product_sales(id),
                        FOREIGN KEY (product_id) REFERENCES products(id)
                    );
                """);

            // SALES
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS sales (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        client_id INTEGER,
                        payment_method_id INTEGER NOT NULL,
                        date TEXT NOT NULL CHECK (date = date(date)),
                        total REAL NOT NULL,
                        FOREIGN KEY (user_id) REFERENCES users(id),
                        FOREIGN KEY (client_id) REFERENCES clients(id),
                        FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id)
                    );
                """);

            // EXPENSES
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS expenses (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        description TEXT NOT NULL,
                        amount REAL NOT NULL CHECK (amount > 0),
                        date TEXT NOT NULL CHECK (date = date(date)),
                        type TEXT NOT NULL CHECK (
                            type IN (
                                'supply', 'service', 'purchase', 'tax', 'other', 'salary', 'advance'
                            )
                        ),
                        payment_method_id INTEGER NOT NULL,
                        FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id)
                    );
                """);

            // SALARIES - SIN CASCADE
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS salaries (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        start_date TEXT NOT NULL,
                        end_date TEXT NOT NULL,
                        total_production REAL NOT NULL,
                        amount_paid REAL NOT NULL,
                        pay_type_snapshot INTEGER NOT NULL,
                        pay_date TEXT,
                        payment_method_id INTEGER NOT NULL,
                        expense_id INTEGER,
                        FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id),
                        FOREIGN KEY (user_id) REFERENCES users(id),
                        FOREIGN KEY (expense_id) REFERENCES expenses(id)
                    );
                """);

            // ADVANCES - SIN CASCADE
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS advances (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        amount REAL NOT NULL CHECK (amount > 0),
                        date TEXT NOT NULL CHECK (date = date(date)),
                        payment_method_id INTEGER NOT NULL,
                        expense_id INTEGER NOT NULL,
                        description TEXT NOT NULL,
                        FOREIGN KEY (user_id) REFERENCES users(id),
                        FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id),
                        FOREIGN KEY (expense_id) REFERENCES expenses(id)
                    );
                """);

            // PAYMENT METHODS
            stmt.execute("""
                     CREATE TABLE IF NOT EXISTS payment_methods (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        displayName TEXT NOT NULL UNIQUE
                     );
                 """);

            // CASHBOX OPENINGS
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS cashbox_openings (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    period_start_date TEXT NOT NULL,
                    opened_at TEXT NOT NULL DEFAULT (datetime('now')),
                    opened_by_user_id INTEGER NOT NULL,
                    cash_amount REAL NOT NULL CHECK (cash_amount >= 0),
                    bank_amount REAL NOT NULL CHECK (bank_amount >= 0),
                    notes TEXT,
                    UNIQUE (period_start_date),
                    FOREIGN KEY (opened_by_user_id) REFERENCES users(id)
                );
                """);

            // CASHBOX CLOSURES
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS cashbox_closures (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    period_start_date TEXT NOT NULL,
                    period_end_date TEXT NOT NULL,
                    closed_at TEXT NOT NULL DEFAULT (datetime('now')),
                    closed_by_user_id INTEGER NOT NULL,
                    expected_cash REAL NOT NULL,
                    expected_bank REAL NOT NULL,
                    expected_total REAL NOT NULL,
                    notes TEXT,
                    UNIQUE (period_start_date),
                    FOREIGN KEY (closed_by_user_id) REFERENCES users(id)
                );
                """);

            // CASHBOX MOVEMENTS
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS cashbox_movements (
                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                      movement_type TEXT NOT NULL,
                      direction TEXT NOT NULL,
                      amount REAL NOT NULL CHECK (amount > 0),
                      payment_method_id INTEGER,
                      reference_type TEXT,
                      reference_id INTEGER,
                      description TEXT,
                      user_id INTEGER,
                      occurred_at TEXT NOT NULL,
                      created_at TEXT NOT NULL DEFAULT (datetime('now')),
                      FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id),
                      FOREIGN KEY (user_id) REFERENCES users(id)
                  );
                """);

            // CLIENTS
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS clients (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    displayName TEXT NOT NULL,
                    document TEXT,
                    phone TEXT,
                    email TEXT,
                    notes TEXT,
                    active INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0,1))
                );
                """);

            // BUDGETS
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS budgets (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    start_date TEXT NOT NULL,
                    end_date TEXT NOT NULL,
                    status TEXT NOT NULL CHECK (status IN ('draft', 'active', 'closed')),
                    created_at TEXT NOT NULL DEFAULT (datetime('now'))
                );
                """);

            // BUDGET ITEMS
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS budget_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    budget_id INTEGER NOT NULL,
                    displayName TEXT NOT NULL,
                    estimated_amount INTEGER NOT NULL CHECK (estimated_amount >= 0),
                    type TEXT NOT NULL CHECK (type IN ('fixed', 'variable')),
                    category TEXT NOT NULL,
                    FOREIGN KEY (budget_id) REFERENCES budgets(id)
                );
                """);

            // BUDGET EXPENSE MATCHES - SIN CASCADE
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS budget_expense_matches (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    budget_item_id INTEGER NOT NULL,
                    expense_id INTEGER NOT NULL,
                    amount_applied INTEGER NOT NULL CHECK (amount_applied > 0),
                    FOREIGN KEY (budget_item_id) REFERENCES budget_items(id),
                    FOREIGN KEY (expense_id) REFERENCES expenses(id),
                    UNIQUE (budget_item_id, expense_id)
                );
                """);

            logger.info("[DB] Database tables created successfully.");
        } catch (SQLException e) {
            logger.error("[DB] Error creating database tables: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a backup of the database file.
     * The backup files are stored in the "backups" directory
     * with a timestamped filename. Only the 7 most recent backups are kept.
     */
    public static void backupDatabase() {
        try {
            // Create backups directory if it doesn't exist
            File backupFolder = new File(appFolder, "backups");
            if (!backupFolder.exists() && !backupFolder.mkdirs()) {
                logger.warn("[DB] Can't create backup directory: {}", backupFolder.getAbsolutePath());
                return;
            }

            // Name format: database_backup_YYYY-MM-DD_HH-MM-SS.db
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String backupName = "database_backup_" + timestamp + ".db";

            Path source = dbFile.toPath();
            Path target = backupFolder.toPath().resolve(backupName);

            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            logger.info("[DB] Backup created: {}", target.toString());

            // Only keep the 7 most recent backups
            File[] backups = backupFolder.listFiles((dir, displayName) -> displayName.endsWith(".db"));
            if (backups != null && backups.length > 7) {
                Arrays.stream(backups)
                        .sorted(Comparator.comparingLong(File::lastModified).reversed())
                        .skip(7)
                        .forEach(file -> {
                            if (file.delete())
                                logger.info("[DB] Removed old backup: {}", file.getName());
                        });
            }

        } catch (IOException e) {
            logger.error("[DB] Error creating database backup: {}", e.getMessage());
        }
    }

    public static File getRealDocumentsFolder() {
        try {
            PointerByReference pbr = new PointerByReference();

            WinNT.HRESULT hr = Shell32.INSTANCE.SHGetKnownFolderPath(
                    KnownFolders.FOLDERID_Documents,
                    0,
                    null,
                    pbr
            );

            if (!WinNT.S_OK.equals(hr)) {
                throw new IllegalStateException(
                        "Failed to resolve Windows 'Documents' folder. HRESULT=" + hr
                );
            }

            Pointer ptr = pbr.getValue();
            if (ptr == null) {
                throw new IllegalStateException(
                        "Native pointer for 'Documents' folder is null."
                );
            }

            String path = ptr.getWideString(0);

            // Free native memory
            Ole32.INSTANCE.CoTaskMemFree(ptr);

            return new File(path);

        } catch (Exception e) {
            throw new RuntimeException("Critical error while resolving Documents folder.", e);
        }
    }
    public static File getAppFolder() {
        return appFolder;
    }

    /**
     * Gets the avatars folder path
     * @return File pointing to the avatars directory
     */
    public static File getAvatarsFolder() {
        return new File(appFolder, "avatars");
    }
    public static void initializePaymentMethods() {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {

            var rs = stmt.executeQuery("SELECT COUNT(*) FROM payment_methods");
            int count = rs.next() ? rs.getInt(1) : 0;

            if (count > 0) {
                logger.debug("[DB] Payment methods already initialized (count={})", count);
                return;
            }

            logger.info("[DB] Initializing default payment methods...");

            String insertSql = "INSERT INTO payment_methods (id, displayName) VALUES (?, ?)";
            var ps = conn.prepareStatement(insertSql);

            // Guardamos en inglés (keys)
            String[] methods = {"cash", "transfer", "card", "qr"};

            for (int i = 0; i < methods.length; i++) {
                ps.setInt(1, i);
                ps.setString(2, methods[i]);
                ps.addBatch();
            }

            ps.executeBatch();
            logger.info("[DB] Payment methods initialized: {}", String.join(", ", methods));

        } catch (SQLException e) {
            logger.error("[DB] Error initializing payment methods: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize payment methods", e);
        }
    }

    /**
     * Adds avatar_path column to users table if it doesn't exist
     */
    private static void migrateUsersAvatarPath() {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {

            // Check if column exists
            var rs = stmt.executeQuery("PRAGMA table_info(users)");
            boolean hasAvatarPath = false;

            while (rs.next()) {
                String colName = rs.getString("name");
                if ("avatar_path".equals(colName)) {
                    hasAvatarPath = true;
                    break;
                }
            }

            if (!hasAvatarPath) {
                logger.info("[DB] Adding avatar_path column to users table...");
                stmt.execute("ALTER TABLE users ADD COLUMN avatar_path TEXT DEFAULT 'default.png'");
                logger.info("[DB] avatar_path column added successfully.");
            } else {
                logger.debug("[DB] avatar_path column already exists.");
            }

        } catch (SQLException e) {
            logger.error("[DB] Error migrating avatar_path column: {}", e.getMessage());
            throw new RuntimeException("Failed to add avatar_path column", e);
        }
    }
    /**
     * Creates the avatars folder and copies the default avatar if needed
     */
    private static void initializeAvatarsFolder() {
        try {
            // Create avatars folder
            File avatarsFolder = new File(appFolder, "avatars");
            if (!avatarsFolder.exists()) {
                if (avatarsFolder.mkdirs()) {
                    logger.info("[DB] Avatars folder created at: {}", avatarsFolder.getAbsolutePath());
                } else {
                    logger.warn("[DB] Failed to create avatars folder");
                    return;
                }
            } else {
                logger.debug("[DB] Avatars folder already exists at: {}", avatarsFolder.getAbsolutePath());
            }

            // Copy default avatar from resources if it doesn't exist
            File defaultAvatarFile = new File(avatarsFolder, "default.png");
            if (!defaultAvatarFile.exists()) {
                try (var in = DbBootstrap.class.getResourceAsStream("/app/barbman/core/assets/avatars/default.png")) {
                    if (in != null) {
                        Files.copy(in, defaultAvatarFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        logger.info("[DB] Default avatar copied to: {}", defaultAvatarFile.getAbsolutePath());
                    } else {
                        logger.warn("[DB] Default avatar resource not found in JAR");
                    }
                }
            }

        } catch (IOException e) {
            logger.error("[DB] Error initializing avatars folder: {}", e.getMessage());
        }
    }
    private static void runCascadeRemovalMigration() {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {

            // Check if migrations already ran by looking for a marker
            var rs = stmt.executeQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='migration_log'"
            );

            boolean migrationTableExists = rs.next();
            rs.close();

            if (!migrationTableExists) {
                // Create migrations log table
                stmt.execute("""
                CREATE TABLE migration_log (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    migration_name TEXT NOT NULL UNIQUE,
                    applied_at TEXT NOT NULL DEFAULT (datetime('now'))
                );
            """);
            }

            // Check if this specific migrations already ran
            rs = stmt.executeQuery(
                    "SELECT 1 FROM migration_log WHERE migration_name = 'remove_cascade_v1'"
            );

            boolean alreadyRan = rs.next();
            rs.close();

            if (alreadyRan) {
                logger.debug("[DB] CASCADE removal migrations already applied, skipping");
                return;
            }

            // Run the migrations
            logger.info("[DB] Running CASCADE removal migrations...");
            RemoveCascadeMigration.migrate(conn);

            // Mark as completed
            var ps = conn.prepareStatement(
                    "INSERT INTO migration_log (migration_name) VALUES (?)"
            );
            ps.setString(1, "remove_cascade_v1");
            ps.executeUpdate();
            ps.close();

            logger.info("[DB] CASCADE removal migrations completed successfully");

        } catch (Exception e) {
            logger.error("[DB] Error running CASCADE removal migrations: {}", e.getMessage());
            throw new RuntimeException("Failed to run migrations", e);
        }
    }
}
