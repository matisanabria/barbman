package app.barbman.core.repositories.migrations;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Migration to remove CASCADE from foreign keys.
 * SQLite doesn't support ALTER TABLE DROP CONSTRAINT,
 * so we need to recreate tables.
 */
public class RemoveCascadeMigration {

    private static final Logger logger = LogManager.getLogger(RemoveCascadeMigration.class);

    public static void migrate(Connection conn) throws Exception {
        logger.info("[MIGRATION] Removing CASCADE from foreign keys...");

        Statement stmt = conn.createStatement();

        // Disable foreign keys temporarily
        stmt.execute("PRAGMA foreign_keys = OFF;");

        // Start transaction
        stmt.execute("BEGIN TRANSACTION;");

        try {
            // ===== SERVICE_HEADER =====
            logger.info("[MIGRATION] Recreating service_header table...");

            // Create new table without CASCADE
            stmt.execute("""
                CREATE TABLE service_header_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    sale_id INTEGER NOT NULL,
                    date TEXT NOT NULL CHECK (date = date(date)),
                    subtotal REAL NOT NULL DEFAULT 0,
                    FOREIGN KEY (user_id) REFERENCES users(id),
                    FOREIGN KEY (sale_id) REFERENCES sales(id)
                );
            """);

            // Copy data
            stmt.execute("""
                INSERT INTO service_header_new 
                SELECT * FROM service_header;
            """);

            // Drop old table
            stmt.execute("DROP TABLE service_header;");

            // Rename new table
            stmt.execute("ALTER TABLE service_header_new RENAME TO service_header;");

            // ===== SERVICE_ITEM =====
            logger.info("[MIGRATION] Recreating service_item table...");

            stmt.execute("""
                CREATE TABLE service_item_new (
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

            stmt.execute("""
                INSERT INTO service_item_new 
                SELECT * FROM service_item;
            """);

            stmt.execute("DROP TABLE service_item;");
            stmt.execute("ALTER TABLE service_item_new RENAME TO service_item;");

            // ===== PRODUCT_SALES =====
            logger.info("[MIGRATION] Recreating product_sales table...");

            stmt.execute("""
                CREATE TABLE product_sales_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    sale_id INTEGER NOT NULL,
                    subtotal REAL NOT NULL,
                    FOREIGN KEY (sale_id) REFERENCES sales(id)
                );
            """);

            stmt.execute("""
                INSERT INTO product_sales_new 
                SELECT * FROM product_sales;
            """);

            stmt.execute("DROP TABLE product_sales;");
            stmt.execute("ALTER TABLE product_sales_new RENAME TO product_sales;");

            // ===== PRODUCT_SALE_ITEMS =====
            logger.info("[MIGRATION] Recreating product_sale_items table...");

            stmt.execute("""
                CREATE TABLE product_sale_items_new (
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

            stmt.execute("""
                INSERT INTO product_sale_items_new 
                SELECT * FROM product_sale_items;
            """);

            stmt.execute("DROP TABLE product_sale_items;");
            stmt.execute("ALTER TABLE product_sale_items_new RENAME TO product_sale_items;");

            // ===== SALARIES =====
            logger.info("[MIGRATION] Recreating salaries table...");

            stmt.execute("""
                CREATE TABLE salaries_new (
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

            stmt.execute("""
                INSERT INTO salaries_new 
                SELECT * FROM salaries;
            """);

            stmt.execute("DROP TABLE salaries;");
            stmt.execute("ALTER TABLE salaries_new RENAME TO salaries;");

            // ===== ADVANCES =====
            logger.info("[MIGRATION] Recreating advances table...");

            stmt.execute("""
                CREATE TABLE advances_new (
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

            stmt.execute("""
                INSERT INTO advances_new 
                SELECT * FROM advances;
            """);

            stmt.execute("DROP TABLE advances;");
            stmt.execute("ALTER TABLE advances_new RENAME TO advances;");

            // ===== BUDGET_ITEMS =====
            logger.info("[MIGRATION] Recreating budget_items table...");

            stmt.execute("""
                CREATE TABLE budget_items_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    budget_id INTEGER NOT NULL,
                    displayName TEXT NOT NULL,
                    estimated_amount INTEGER NOT NULL CHECK (estimated_amount >= 0),
                    type TEXT NOT NULL CHECK (type IN ('fixed', 'variable')),
                    category TEXT NOT NULL,
                    FOREIGN KEY (budget_id) REFERENCES budgets(id)
                );
            """);

            stmt.execute("""
                INSERT INTO budget_items_new 
                SELECT * FROM budget_items;
            """);

            stmt.execute("DROP TABLE budget_items;");
            stmt.execute("ALTER TABLE budget_items_new RENAME TO budget_items;");

            // ===== BUDGET_EXPENSE_MATCHES =====
            logger.info("[MIGRATION] Recreating budget_expense_matches table...");

            stmt.execute("""
                CREATE TABLE budget_expense_matches_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    budget_item_id INTEGER NOT NULL,
                    expense_id INTEGER NOT NULL,
                    amount_applied INTEGER NOT NULL CHECK (amount_applied > 0),
                    FOREIGN KEY (budget_item_id) REFERENCES budget_items(id),
                    FOREIGN KEY (expense_id) REFERENCES expenses(id),
                    UNIQUE (budget_item_id, expense_id)
                );
            """);

            stmt.execute("""
                INSERT INTO budget_expense_matches_new 
                SELECT * FROM budget_expense_matches;
            """);

            stmt.execute("DROP TABLE budget_expense_matches;");
            stmt.execute("ALTER TABLE budget_expense_matches_new RENAME TO budget_expense_matches;");

            // ===== CASHBOX_MOVEMENTS =====
            logger.info("[MIGRATION] Recreating cashbox_movements table...");

            stmt.execute("""
                CREATE TABLE cashbox_movements_new (
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

            stmt.execute("""
                INSERT INTO cashbox_movements_new 
                SELECT * FROM cashbox_movements;
            """);

            stmt.execute("DROP TABLE cashbox_movements;");
            stmt.execute("ALTER TABLE cashbox_movements_new RENAME TO cashbox_movements;");

            // Commit transaction
            stmt.execute("COMMIT;");

            logger.info("[MIGRATION] CASCADE removed successfully!");

        } catch (Exception e) {
            // Rollback on error
            stmt.execute("ROLLBACK;");
            logger.error("[MIGRATION] Error during migration, rolled back", e);
            throw e;
        } finally {
            // Re-enable foreign keys
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.close();
        }
    }
}