-- V4: Seed default admin user (PIN: 0000)
INSERT OR IGNORE INTO users (displayName, role, pin, payment_type, pay_frequency, param_1, param_2, avatar_path)
VALUES ('Admin', 'admin', '0000', 0, 'MONTHLY', 0, 0, 'default.png');
