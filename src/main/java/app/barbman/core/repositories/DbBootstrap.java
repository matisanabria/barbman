package app.barbman.core.repositories;

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
            // Defines types of legacy offered with base prices and availability status
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS service_definition (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            displayName TEXT NOT NULL UNIQUE,
                            base_price REAL NOT NULL,
                            available INTEGER NOT NULL DEFAULT 1 CHECK (available IN (0,1))
                        );
                    """);

            // SERVICE HEADER
            // Header for services performed by users on specific dates
            // We use user_id as a snapshot of who performed the serviceheader, and
            // date to track when it was done. Total is the sum of all serviceheader items.
            // Those columns are kept for calculating production per user/date.
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS service_header (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            user_id INTEGER NOT NULL,
                            sale_id INTEGER NOT NULL,                   -- links to sales table
                            date TEXT NOT NULL CHECK (date = date(date)),
                            subtotal REAL NOT NULL DEFAULT 0,
                            FOREIGN KEY (user_id) REFERENCES users(id),
                            FOREIGN KEY (sale_id) REFERENCES sales(id)
                        );
                    """);
            // SERVICE ITEM
            // Links legacy to specific serviceheader definitions (items) with individual pricing
            // This allows for multiple items per serviceheader record
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS service_item (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            service_header_id INTEGER NOT NULL,
                            service_definition_id INTEGER NOT NULL,
                            quantity INTEGER NOT NULL DEFAULT 1 CHECK (quantity > 0),
                            unit_price REAL NOT NULL,               -- snapshot of the unitPrice at the time of serviceHeader
                            item_total REAL NOT NULL DEFAULT 0
                            FOREIGN KEY (service_header_id) REFERENCES service_header(id),
                            FOREIGN KEY (service_definition_id) REFERENCES service_definition(id)
                        );
                    """);

            // PRODUCTS
            // Inventory of products with pricing, stock, and optional categorization
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS products (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            displayName TEXT NOT NULL UNIQUE,
                            cost_price REAL NOT NULL,      -- cost from supplier
                            unit_price REAL NOT NULL,      -- selling unitPrice
                            stock INTEGER NOT NULL DEFAULT 0,
                            category TEXT,                 -- optional, can be NULL
                            brand TEXT,                    -- optional, can be NULL
                            image_path TEXT,               -- image file path of the product
                            notes TEXT NOT NULL                -- optional
                        );
                    """);

            // PRODUCT HEADER
            // Header for products sold
            // Links to sales table for unified sales tracking
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS product_sales (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            sale_id INTEGER NOT NULL,                   -- links to sales table
                            subtotal REAL NOT NULL,
                            FOREIGN KEY (sale_id) REFERENCES sales(id)
                        );
                    """);

            // PRODUCT SALE ITEMS
            // Links individual products to product sales with quantity and pricing details
            // Allows multiple products per salecart record
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
            // A header table for all sales transactions (services + products)
            // Links to users, clients, payment methods, and records the total amount
            stmt.execute(
                    """
                        CREATE TABLE IF NOT EXISTS sales (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            user_id INTEGER NOT NULL,
                            client_id INTEGER,                  -- can be NULL
                            payment_method_id INTEGER NOT NULL,
                            date TEXT NOT NULL CHECK (date = date(date)),
                            total REAL NOT NULL,
                            FOREIGN KEY (user_id) REFERENCES users(id),
                            FOREIGN KEY (client_id) REFERENCES clients(id),
                            FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id)
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
            // Records salary payments of users. Each salary entry links to an expense record for traceability.
            // Includes production totals, payment amounts, payment methods, and pay date.
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
                            expense_id INTEGER, -- links to expenses table for traceability
                            FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id),
                            FOREIGN KEY (user_id) REFERENCES users(id),
                            FOREIGN KEY (expense_id) REFERENCES expenses(id)
                        );
                    """);


            // ADVANCES
            // Keeps a detailed record of all cash advances given to users (barbers)
            // Each advance is linked to an expense entry for unified accounting
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
            // Defines available payment methods for transactions
            stmt.execute("""
                         CREATE TABLE IF NOT EXISTS payment_methods (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            displayName TEXT NOT NULL UNIQUE   -- ej: cash, transfer, qr, card
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
            // CLIENTS
            // Stores client information for appointments and marketing purposes
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS clients (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        displayName TEXT NOT NULL,          -- requerido
                        document TEXT,               -- RUC (opcional)
                        phone TEXT NOT NULL,         -- requerido
                        email TEXT,                  -- opcional
                        notes TEXT,
                        active INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0,1))
                    );
                    """);

            // BUDGETS
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS budgets (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        
                        -- Inicio del período presupuestario (ej: 2025-01-01)
                        start_date TEXT NOT NULL,
                        
                        -- Fin del período presupuestario (ej: 2025-01-31)
                        end_date TEXT NOT NULL,
                        
                        -- Estado del presupuesto
                        -- draft: en edición
                        -- active: en uso
                        -- closed: cerrado (solo lectura)
                        status TEXT NOT NULL CHECK (status IN ('draft', 'active', 'closed')),
                        
                        -- Fecha de creación del presupuesto
                        created_at TEXT NOT NULL DEFAULT (datetime('now'))
                    );
                    """);

            // BUDGET ITEMS
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS budget_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        
                        -- Presupuesto al que pertenece este item
                        budget_id INTEGER NOT NULL,
                        
                        -- Nombre del gasto esperado (ej: Luz, Insumos, Alquiler)
                        displayName TEXT NOT NULL,
                        
                        -- Monto estimado para este gasto
                        estimated_amount INTEGER NOT NULL CHECK (estimated_amount >= 0),
                        
                        -- Tipo de gasto
                        -- fixed: gasto fijo
                        -- variable: gasto variable
                        type TEXT NOT NULL CHECK (type IN ('fixed', 'variable')),
                        
                        -- Categoría lógica del gasto (ej: utilities, supplies, rent)
                        category TEXT NOT NULL,
                        
                        FOREIGN KEY (budget_id) REFERENCES budgets(id)
                            ON DELETE CASCADE
                    );
                    """);

            // BUDGET EXPENSE MATCHES
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS budget_expense_matches (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        
                        -- Item del presupuesto afectado
                        budget_item_id INTEGER NOT NULL,
                        
                        -- Egreso real aplicado
                        expense_id INTEGER NOT NULL,
                        
                        -- Monto del egreso que se aplica a este item
                        -- Permite cuotas y pagos parciales
                        amount_applied INTEGER NOT NULL CHECK (amount_applied > 0),
                        
                        FOREIGN KEY (budget_item_id) REFERENCES budget_items(id)
                            ON DELETE CASCADE,
                            
                        FOREIGN KEY (expense_id) REFERENCES expenses(id)
                            ON DELETE CASCADE,
                            
                        -- Evita duplicar el mismo expense aplicado dos veces al mismo item
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

}
