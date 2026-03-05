-- V1: Initial schema
-- All tables are created without ON DELETE CASCADE.
-- Foreign key enforcement is left to the application layer.

CREATE TABLE IF NOT EXISTS users (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    displayName   TEXT    NOT NULL,
    role          TEXT    NOT NULL,
    pin           TEXT    NOT NULL UNIQUE CHECK(length(pin) = 4 AND pin GLOB '[0-9][0-9][0-9][0-9]'),
    payment_type  INTEGER NOT NULL DEFAULT 0,
    pay_frequency TEXT    NOT NULL DEFAULT 'WEEKLY'
                          CHECK (pay_frequency IN ('DAILY', 'WEEKLY', 'BIWEEKLY', 'MONTHLY')),
    param_1       REAL,
    param_2       REAL,
    avatar_path   TEXT    DEFAULT 'default.png'
);

CREATE TABLE IF NOT EXISTS clients (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    displayName TEXT    NOT NULL,
    document    TEXT,
    phone       TEXT,
    email       TEXT,
    notes       TEXT,
    active      INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0, 1))
);

CREATE TABLE IF NOT EXISTS payment_methods (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    displayName TEXT    NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS service_definition (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    displayName TEXT    NOT NULL UNIQUE,
    base_price  REAL    NOT NULL,
    available   INTEGER NOT NULL DEFAULT 1 CHECK (available IN (0, 1))
);

CREATE TABLE IF NOT EXISTS sales (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id           INTEGER NOT NULL,
    client_id         INTEGER,
    payment_method_id INTEGER NOT NULL,
    date              TEXT    NOT NULL CHECK (date = date(date)),
    total             REAL    NOT NULL,
    FOREIGN KEY (user_id)           REFERENCES users(id),
    FOREIGN KEY (client_id)         REFERENCES clients(id),
    FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id)
);

CREATE TABLE IF NOT EXISTS service_header (
    id      INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    sale_id INTEGER NOT NULL,
    date    TEXT    NOT NULL CHECK (date = date(date)),
    subtotal REAL   NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (sale_id) REFERENCES sales(id)
);

CREATE TABLE IF NOT EXISTS service_item (
    id                    INTEGER PRIMARY KEY AUTOINCREMENT,
    service_header_id     INTEGER NOT NULL,
    service_definition_id INTEGER NOT NULL,
    quantity              INTEGER NOT NULL DEFAULT 1 CHECK (quantity > 0),
    unit_price            REAL    NOT NULL,
    item_total            REAL    NOT NULL DEFAULT 0,
    FOREIGN KEY (service_header_id)     REFERENCES service_header(id),
    FOREIGN KEY (service_definition_id) REFERENCES service_definition(id)
);

CREATE TABLE IF NOT EXISTS products (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    displayName TEXT    NOT NULL UNIQUE,
    cost_price  REAL    NOT NULL,
    unit_price  REAL    NOT NULL,
    stock       INTEGER NOT NULL DEFAULT 0,
    category    TEXT,
    brand       TEXT,
    image_path  TEXT,
    notes       TEXT    NOT NULL
);

CREATE TABLE IF NOT EXISTS product_sales (
    id       INTEGER PRIMARY KEY AUTOINCREMENT,
    sale_id  INTEGER NOT NULL,
    subtotal REAL    NOT NULL,
    FOREIGN KEY (sale_id) REFERENCES sales(id)
);

CREATE TABLE IF NOT EXISTS product_sale_items (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    product_header_id INTEGER NOT NULL,
    product_id        INTEGER NOT NULL,
    quantity          INTEGER NOT NULL CHECK (quantity > 0),
    unit_price        REAL    NOT NULL,
    item_total        REAL    NOT NULL,
    FOREIGN KEY (product_header_id) REFERENCES product_sales(id),
    FOREIGN KEY (product_id)        REFERENCES products(id)
);

CREATE TABLE IF NOT EXISTS expenses (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    description       TEXT    NOT NULL,
    amount            REAL    NOT NULL CHECK (amount > 0),
    date              TEXT    NOT NULL CHECK (date = date(date)),
    type              TEXT    NOT NULL CHECK (
        type IN ('supply', 'service', 'purchase', 'tax', 'other', 'salary', 'advance')
    ),
    payment_method_id INTEGER NOT NULL,
    FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id)
);

CREATE TABLE IF NOT EXISTS salaries (
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id             INTEGER NOT NULL,
    start_date          TEXT    NOT NULL,
    end_date            TEXT    NOT NULL,
    total_production    REAL    NOT NULL,
    amount_paid         REAL    NOT NULL,
    pay_type_snapshot   INTEGER NOT NULL,
    pay_date            TEXT,
    payment_method_id   INTEGER NOT NULL,
    expense_id          INTEGER,
    FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id),
    FOREIGN KEY (user_id)           REFERENCES users(id),
    FOREIGN KEY (expense_id)        REFERENCES expenses(id)
);

CREATE TABLE IF NOT EXISTS advances (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id           INTEGER NOT NULL,
    amount            REAL    NOT NULL CHECK (amount > 0),
    date              TEXT    NOT NULL CHECK (date = date(date)),
    payment_method_id INTEGER NOT NULL,
    expense_id        INTEGER NOT NULL,
    description       TEXT    NOT NULL,
    FOREIGN KEY (user_id)           REFERENCES users(id),
    FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id),
    FOREIGN KEY (expense_id)        REFERENCES expenses(id)
);

CREATE TABLE IF NOT EXISTS cashbox_openings (
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    period_start_date   TEXT    NOT NULL,
    opened_at           TEXT    NOT NULL DEFAULT (datetime('now')),
    opened_by_user_id   INTEGER NOT NULL,
    cash_amount         REAL    NOT NULL CHECK (cash_amount >= 0),
    bank_amount         REAL    NOT NULL CHECK (bank_amount >= 0),
    notes               TEXT,
    UNIQUE (period_start_date),
    FOREIGN KEY (opened_by_user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS cashbox_closures (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    period_start_date TEXT    NOT NULL,
    period_end_date   TEXT    NOT NULL,
    closed_at         TEXT    NOT NULL DEFAULT (datetime('now')),
    closed_by_user_id INTEGER NOT NULL,
    expected_cash     REAL    NOT NULL,
    expected_bank     REAL    NOT NULL,
    expected_total    REAL    NOT NULL,
    notes             TEXT,
    UNIQUE (period_start_date),
    FOREIGN KEY (closed_by_user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS cashbox_movements (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    movement_type     TEXT    NOT NULL,
    direction         TEXT    NOT NULL,
    amount            REAL    NOT NULL CHECK (amount > 0),
    payment_method_id INTEGER,
    reference_type    TEXT,
    reference_id      INTEGER,
    description       TEXT,
    user_id           INTEGER,
    occurred_at       TEXT    NOT NULL,
    created_at        TEXT    NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id),
    FOREIGN KEY (user_id)           REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS budgets (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    start_date TEXT    NOT NULL,
    end_date   TEXT    NOT NULL,
    status     TEXT    NOT NULL CHECK (status IN ('draft', 'active', 'closed')),
    created_at TEXT    NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS budget_items (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    budget_id        INTEGER NOT NULL,
    displayName      TEXT    NOT NULL,
    estimated_amount INTEGER NOT NULL CHECK (estimated_amount >= 0),
    type             TEXT    NOT NULL CHECK (type IN ('fixed', 'variable')),
    category         TEXT    NOT NULL,
    FOREIGN KEY (budget_id) REFERENCES budgets(id)
);

CREATE TABLE IF NOT EXISTS budget_expense_matches (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    budget_item_id INTEGER NOT NULL,
    expense_id     INTEGER NOT NULL,
    amount_applied INTEGER NOT NULL CHECK (amount_applied > 0),
    FOREIGN KEY (budget_item_id) REFERENCES budget_items(id),
    FOREIGN KEY (expense_id)     REFERENCES expenses(id),
    UNIQUE (budget_item_id, expense_id)
);
