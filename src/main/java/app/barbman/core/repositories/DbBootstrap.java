package app.barbman.core.repositories;

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

/**
 * Initializes and manages the SQLite database.
 * Ensures the database file and required tables exist, and provides
 * methods for connecting to the database.
 * Also includes functionality for creating database backups.
 */
public class DbBootstrap {
    private static final Logger logger = LogManager.getLogger(DbBootstrap.class);

    public static final String DB_FOLDER = "data";  // Folder name
    public static final String DB_NAME = "barbman.db";  // Database file name

    public static void init() {
        logger.info("[DB] Ensuring database exists...");

        // Ensure the database folder exists
        File folder = new File(DB_FOLDER);
        if (!folder.exists()) {
            logger.warn("[DB] Database folder not found. Creating folder: " + DB_FOLDER);
            if (folder.mkdirs()) {
                logger.info("[DB] Database folder created successfully.");
            } else {
                logger.error("[DB] Failed to create database folder: " + DB_FOLDER);
            }
        }

        // Ensure the database file exists
        File dbFile = new File(DB_FOLDER + "/" + DB_NAME);
        if (dbFile.exists()) logger.info("[DB] Database found: {}", dbFile.getAbsolutePath());
        else {
            logger.warn("[DB] Database file not found. Creating new database: " + DB_NAME);
            createTables();
        }
    }

    /**
     * Connect to the SQLite database.
     *
     * @return Database connection
     * @throws SQLException if a database access error occurs
     */
    public static Connection connect() throws SQLException {
        String url = "jdbc:sqlite:" + DB_FOLDER + "/" + DB_NAME;
        logger.debug("[DB] Connecting to database. URL: {}", url);
        Connection conn = DriverManager.getConnection(url);
        logger.debug("[DB] Database connected and ready to use.");
        return conn;
    }

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
                            name TEXT NOT NULL,
                            role TEXT NOT NULL,
                            pin TEXT NOT NULL UNIQUE CHECK(length(pin) = 4 AND pin GLOB '[0-9][0-9][0-9][0-9]'),
                            payment_type INTEGER NOT NULL DEFAULT 0,
                            param_1 REAL,
                            param_2 REAL
                        );
                    """);

            // SERVICE DEFINITIONS
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS service_definitions (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT NOT NULL UNIQUE,
                            base_price REAL NOT NULL
                        );
                    """);

            // PERFORMED SERVICES
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS performed_services (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            user_id INTEGER NOT NULL,
                            service_type_id INTEGER NOT NULL,
                            price REAL NOT NULL,
                            date TEXT NOT NULL CHECK (date = date(date)),
                            payment_method_id INTEGER NOT NULL,
                            notes TEXT,
                            FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id),
                            FOREIGN KEY (user_id) REFERENCES users(id),
                            FOREIGN KEY (service_type_id) REFERENCES service_definitions(id)
                        );
                    """);

            // PRODUCTS
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS products (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT NOT NULL UNIQUE,
                            cost_price REAL NOT NULL,      -- cost from supplier
                            unit_price REAL NOT NULL,      -- selling price
                            stock INTEGER NOT NULL DEFAULT 0,
                            category TEXT,                 -- optional, can be NULL
                            brand TEXT,                    -- optional, can be NULL
                            notes TEXT NOT NULL                -- optional
                        );
                    """);

            // SALES
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS sales (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            date TEXT NOT NULL CHECK (date = date(date)),
                            total REAL NOT NULL,
                            payment_method_id INTEGER NOT NULL,
                            FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id)
                        );
                    """);

            // SALE ITEMS
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS sale_items (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            sale_id INTEGER NOT NULL,
                            product_id INTEGER NOT NULL,
                            quantity INTEGER NOT NULL CHECK (quantity > 0),
                            unit_price REAL NOT NULL,
                            FOREIGN KEY (sale_id) REFERENCES sales(id),
                            FOREIGN KEY (product_id) REFERENCES products(id)
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
                                    'supply', 'service', 'purchase', 'other', 'salary', 'advance'

                                    -- 'supply'    -> supplies and products (e.g., restocking inventory)
                                    -- 'service'   -> cleaning, rent, electricity, etc.
                                    -- 'purchase'  -> furniture, tools, decoration
                                    -- 'other'     -> irregular expenses, delivery, miscellaneous
                                    -- 'salary'    -> employees' wages
                                    -- 'advance'   -> advances before the weekly close
                                )
                            ),
                            payment_method_id INTEGER NOT NULL,
                            FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id)
                        );
                    """);

            // SALARIES
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS salaries (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            user_id INTEGER NOT NULL,
                            week_start TEXT NOT NULL,
                            week_end TEXT NOT NULL,
                            total_production REAL NOT NULL,
                            amount_paid REAL NOT NULL,
                            pay_type_snapshot INTEGER NOT NULL,
                            pay_date TEXT,
                            payment_method_id INTEGER NOT NULL,
                            FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id),
                            FOREIGN KEY (user_id) REFERENCES users(id)
                        );
                    """);
            // PAYMENT METHODS
            stmt.execute("""
                         CREATE TABLE IF NOT EXISTS payment_methods (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT NOT NULL UNIQUE   -- ej: cash, transfer, qr, card
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
            String backupDir = "backups";
            File folder = new File(backupDir);
            if (!folder.exists() && !folder.mkdirs()) {
                logger.warn("[DB] Can't create backup directory: {}", backupDir);
                return;
            }

            // Name format: database_backup_YYYY-MM-DD_HH-MM-SS.db
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String backupName = "database_backup_" + timestamp + ".db";

            Path source = Paths.get(DB_FOLDER, DB_NAME);
            Path target = Paths.get(backupDir, backupName);

            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            logger.info("[DB] Backup created: {}", target.toString());

            // Only keep the 7 most recent backups
            File[] backups = folder.listFiles((dir, name) -> name.endsWith(".db"));
            if (backups != null && backups.length > 7) {
                Arrays.stream(backups)
                        .sorted(Comparator.comparingLong(File::lastModified).reversed())
                        .skip(7) // keep the 7 most recent
                        .forEach(file -> {
                            if (file.delete()) {
                                logger.info("[DB] Removed old backup: {}", file.getName());
                            } else {
                                logger.warn("[DB] Can't remove old backup: {}", file.getName());
                            }
                        });
            }

        } catch (IOException e) {
            logger.error("[DB] Error creating database backup: {}", e.getMessage());
        }
    }

}
