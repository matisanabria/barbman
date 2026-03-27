package app.barbman.core.infrastructure;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/**
 * Handles database schema migrations using Flyway.
 *
 * Migration scripts live in {@code src/main/resources/db/migration/}.
 * They are extracted to a temp directory at runtime to bypass JPMS classloader
 * restrictions (named modules encapsulate resources from external modules like Flyway).
 *
 * To add a new migration: create the .sql file and add its name to MIGRATION_FILES.
 */
public class FlywayMigrator {

    private static final Logger logger = LogManager.getLogger(FlywayMigrator.class);

    /** Ordered list of migration scripts — add new migrations here. */
    private static final String[] MIGRATION_FILES = {
            "V1__initial_schema.sql",
            "V2__seed_payment_methods.sql",
            "V3__cashbox_redesign.sql",
            "V4__seed_default_admin.sql"
    };

    private FlywayMigrator() {}

    public static void migrate(String dbPath) {
        logger.info("[FLYWAY] Starting migrations. DB: {}", dbPath);

        Path tempDir = null;
        try {
            tempDir = extractMigrations();

            Flyway flyway = Flyway.configure()
                    .dataSource("jdbc:sqlite:" + dbPath, "", "")
                    .locations("filesystem:" + tempDir.toAbsolutePath())
                    .baselineOnMigrate(true)
                    .baselineVersion("0")
                    .load();

            flyway.repair();
            MigrateResult result = flyway.migrate();
            logger.info("[FLYWAY] Migrations complete. Applied: {}", result.migrationsExecuted);

        } catch (IOException e) {
            throw new RuntimeException("[FLYWAY] Failed to extract migration scripts", e);
        } finally {
            if (tempDir != null) deleteDirectory(tempDir);
        }
    }

    /** Copies SQL files from the module's resources to a temp directory. */
    private static Path extractMigrations() throws IOException {
        Path tempDir = Files.createTempDirectory("barbman_migrations_");
        tempDir.toFile().deleteOnExit();

        for (String file : MIGRATION_FILES) {
            try (InputStream is = FlywayMigrator.class.getResourceAsStream("/db/migration/" + file)) {
                if (is == null) throw new IOException("Migration script not found: " + file);
                Path dest = tempDir.resolve(file);
                Files.copy(is, dest);
                dest.toFile().deleteOnExit();
            }
        }
        return tempDir;
    }

    private static void deleteDirectory(Path dir) {
        try {
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException ignored) {}
    }
}
