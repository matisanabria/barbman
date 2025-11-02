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
    private static final String DB_NAME = "database.db";
    private static final File DOCUMENTS_FOLDER = new File(System.getProperty("user.home"), "Documents");

    private static File appFolder;
    private static File dataFolder;
    private static File dbFile;

    public static void init() {
        logger.info("[DB] Ensuring database exists...");

        // Create app folder in documents if it doesn't exist
        appFolder = new File(DOCUMENTS_FOLDER, "Barbman Data");
        if (!appFolder.exists() && appFolder.mkdirs())
            logger.info("[DB] Created app folder at {}", appFolder.getAbsolutePath());

        // Create database folder path
        dataFolder = new File(appFolder, "data");
        if (!dataFolder.exists() && dataFolder.mkdirs())
            logger.info("[DB] Created data folder at {}", dataFolder.getAbsolutePath());

        // Create or verify database file
        dbFile = new File(dataFolder, DB_NAME);
        if (dbFile.exists()) {
            logger.info("[DB] Database found: {}", dbFile.getAbsolutePath());
        } else {
            logger.warn("[DB] Database file not found. Creating new database...");
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
        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
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
            // Contains user information including a 4-digit PIN with uniqueness constraint
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
            // Defines types of services offered with base prices and availability status
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS service_definitions (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT NOT NULL UNIQUE,
                            base_price REAL NOT NULL,
                            available INTEGER NOT NULL DEFAULT 1 CHECK (available IN (0,1))
                        );
                    """);

            // SERVICES
            // Records individual service transactions linked to users (employees) and payment methods
            // Items are stored in a separate table (service_items)
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS services (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            user_id INTEGER NOT NULL,
                            date TEXT NOT NULL CHECK (date = date(date)),
                            payment_method_id INTEGER NOT NULL,
                            total REAL NOT NULL DEFAULT 0,
                            notes TEXT,
                            FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id),
                            FOREIGN KEY (user_id) REFERENCES users(id)
                        );
                    """);
            // SERVICE ITEMS
            // Links services to specific service definitions (items) with individual pricing
            // This allows for multiple items per service record
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS service_items (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            service_id INTEGER NOT NULL,
                            service_type_id INTEGER NOT NULL,
                            price REAL NOT NULL CHECK (price > 0),
                            FOREIGN KEY (service_id) REFERENCES services(id),
                            FOREIGN KEY (service_type_id) REFERENCES service_definitions(id)
                        );
                    """);

            // PRODUCTS
            // Inventory of products with pricing, stock, and optional categorization
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
            // Records individual sales transactions linked to payment methods
            // Items sold are stored in a separate table (sale_items)
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
            // Links sales to specific products sold with quantity and unit pricing
            // This allows for multiple products per sale record
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
            // Records various types of expenses with descriptions, amounts, dates, and payment methods
            // Expense types are constrained to a predefined set of categories
            // Categories salary and advance are used for employee payments and only registered by admins
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS expenses (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            description TEXT NOT NULL,
                            amount REAL NOT NULL CHECK (amount > 0),
                            date TEXT NOT NULL CHECK (date = date(date)),
                            type TEXT NOT NULL CHECK (
                                type IN (
                                    'supply', 'service', 'purchase', 'tax', 'other', 'salary', 'advance'

                                    -- 'supply'    -> supplies and products (e.g., restocking inventory)
                                    -- 'service'   -> cleaning, rent, electricity, etc.
                                    -- 'purchase'  -> furniture, tools, decoration
                                    -- 'tax'       -> taxes
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
            // Records weekly salary payments to users with production totals and payment details
            // TODO: Add monthly salary support
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
            // Defines available payment methods for transactions
            stmt.execute("""
                         CREATE TABLE IF NOT EXISTS payment_methods (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT NOT NULL UNIQUE   -- ej: cash, transfer, qr, card
                         );
                     """);
            // CASHBOX
            // Records daily cashbox openings and closings with unique date constraint
            // Only one cashbox record per day is allowed
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS cashbox (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        date TEXT NOT NULL UNIQUE,
                        opening_balance REAL NOT NULL DEFAULT 0,
                        closing_balance REAL NOT NULL DEFAULT 0
                    );
                    """);
            // CASHBOX MOVEMENTS
            // Records individual cashbox movements (income/expense) linked to a specific day's cashbox
            // and the user who registered the movement. It's used like a cash register log.
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS cashbox_movements (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        cashbox_id INTEGER NOT NULL,       -- referencia al día correspondiente
                        type TEXT NOT NULL,                -- 'INCOME' o 'EXPENSE'
                        amount REAL NOT NULL,
                        description TEXT,
                        user_id INTEGER,                   -- quién registró el movimiento
                        created_at TEXT NOT NULL DEFAULT (datetime('now')),
                        FOREIGN KEY (cashbox_id) REFERENCES cashbox(id),
                        FOREIGN KEY (user_id) REFERENCES users(id)
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
            File[] backups = backupFolder.listFiles((dir, name) -> name.endsWith(".db"));
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

}
