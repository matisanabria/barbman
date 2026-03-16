-- V3: Cashbox redesign - manual periods, reconciliation, opening_id on movements

-- 1. Add 'closed' column to cashbox_openings (0 = open, 1 = closed)
ALTER TABLE cashbox_openings ADD COLUMN closed INTEGER NOT NULL DEFAULT 0;

-- 2. Make period_start_date nullable for new openings (new code won't use it)
--    SQLite doesn't support ALTER COLUMN, so we leave the existing constraint.
--    New code sets period_start_date to opened_at date for compatibility.

-- 3. Add opening_id to cashbox_movements
ALTER TABLE cashbox_movements ADD COLUMN opening_id INTEGER REFERENCES cashbox_openings(id);

-- 4. Backfill opening_id on existing movements by matching occurred_at to nearest opening
UPDATE cashbox_movements
SET opening_id = (
    SELECT o.id
    FROM cashbox_openings o
    WHERE o.period_start_date <= date(cashbox_movements.occurred_at)
    ORDER BY o.period_start_date DESC
    LIMIT 1
);

-- 5. Fix null payment_method_id on OPENING movements
--    Cash opening movements get payment_method_id = 0
--    We identify them by description pattern
UPDATE cashbox_movements
SET payment_method_id = 0
WHERE movement_type = 'OPENING'
  AND payment_method_id IS NULL
  AND description LIKE '%cash%';

UPDATE cashbox_movements
SET payment_method_id = 1
WHERE movement_type = 'OPENING'
  AND payment_method_id IS NULL
  AND description LIKE '%bank%';

-- For any remaining null OPENING movements, default to cash
UPDATE cashbox_movements
SET payment_method_id = 0
WHERE movement_type = 'OPENING'
  AND payment_method_id IS NULL;

-- 6. Recreate cashbox_closures with new schema
--    SQLite doesn't support DROP COLUMN, so we recreate the table
CREATE TABLE cashbox_closures_new (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    opening_id        INTEGER NOT NULL,
    closed_at         TEXT    NOT NULL DEFAULT (datetime('now')),
    closed_by_user_id INTEGER NOT NULL,
    expected_cash     REAL    NOT NULL DEFAULT 0,
    expected_bank     REAL    NOT NULL DEFAULT 0,
    actual_cash       REAL    NOT NULL DEFAULT 0,
    actual_bank       REAL    NOT NULL DEFAULT 0,
    cash_discrepancy  REAL    NOT NULL DEFAULT 0,
    bank_discrepancy  REAL    NOT NULL DEFAULT 0,
    notes             TEXT,
    FOREIGN KEY (opening_id) REFERENCES cashbox_openings(id),
    FOREIGN KEY (closed_by_user_id) REFERENCES users(id)
);

-- 7. Migrate existing closure data
INSERT INTO cashbox_closures_new (opening_id, closed_at, closed_by_user_id, expected_cash, expected_bank, actual_cash, actual_bank, cash_discrepancy, bank_discrepancy, notes)
SELECT
    o.id,
    c.closed_at,
    c.closed_by_user_id,
    c.expected_cash,
    c.expected_bank,
    c.expected_cash,   -- actual = expected for legacy rows (no reconciliation data)
    c.expected_bank,
    0,                 -- no discrepancy for legacy
    0,
    c.notes
FROM cashbox_closures c
JOIN cashbox_openings o ON o.period_start_date = c.period_start_date;

-- 8. Mark migrated openings as closed
UPDATE cashbox_openings
SET closed = 1
WHERE id IN (
    SELECT o.id
    FROM cashbox_openings o
    JOIN cashbox_closures c ON c.period_start_date = o.period_start_date
);

-- 9. Replace old table
DROP TABLE cashbox_closures;
ALTER TABLE cashbox_closures_new RENAME TO cashbox_closures;
