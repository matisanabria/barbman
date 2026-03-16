-- V2: Seed default payment methods
-- Keys stored in English; display names are resolved via i18n bundles in the UI.
INSERT OR IGNORE INTO payment_methods (id, displayName) VALUES (0, 'cash');
INSERT OR IGNORE INTO payment_methods (id, displayName) VALUES (1, 'transfer');
INSERT OR IGNORE INTO payment_methods (id, displayName) VALUES (2, 'card');
INSERT OR IGNORE INTO payment_methods (id, displayName) VALUES (3, 'qr');
