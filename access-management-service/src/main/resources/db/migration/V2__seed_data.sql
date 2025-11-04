-- SAMPLE DATA INSERTS FOR CONFIGURATION TABLES
-- =====================================================

-- ===============================================
-- INSERT: user_groups
-- Purpose: Sample user groups for organizing users
-- ===============================================
INSERT INTO user_groups (group_name, group_code, group_type, parent_group_id, description, is_active) VALUES
('Head Office', 'REGION_HO', 'REGION', NULL, 'Central administrative region', TRUE),
('Collections Department', 'DEPT_COLLECTIONS', 'DEPARTMENT', 1, 'Collections operations department', TRUE),
('Collections Team A', 'TEAM_COLL_A', 'TEAM', 2, 'Primary collections team', TRUE);

-- ===============================================
-- INSERT: role_groups (Minimal)
-- ===============================================
INSERT INTO role_groups (group_name, group_code, description, display_order, is_active) VALUES
('System Administration', 'GRP_SYS_ADMIN', 'System-level admin roles', 1, TRUE),
('Collections Management', 'GRP_COLL_MGMT', 'Collections management roles', 2, TRUE),
('Collections Operations', 'GRP_COLL_OPS', 'Collections operations roles', 3, TRUE);

-- ===============================================
-- INSERT: roles (Only the needed ones)
-- ===============================================
INSERT INTO roles (role_name, role_code, role_group_id, description, is_active) VALUES
('Super Admin', 'SUPER_ADMIN', 1, 'Full system access with all privileges', TRUE),
('Admin', 'ADMIN', 1, 'Administrative access to manage operations', TRUE),
('Collections Manager', 'COLL_MANAGER', 2, 'Manage collection teams and operations', TRUE),
('Collections Agent', 'COLL_AGENT', 3, 'Handle collection cases and calls', TRUE);

-- ===============================================
-- INSERT: users (Only 4 required users)
-- ===============================================
INSERT INTO users (username, password_hash, email, first_name, last_name, status, is_first_login) VALUES
('superadmin', '$2a$10$KAqljhNBfn0kawY8/eVDI.y9.9sUMZsYdFy3jDPrjhjQNZ1TpqJTW', 'superadmin@example.com', 'Super', 'Admin', 'ACTIVE', TRUE),
('admin', '$2a$10$KAqljhNBfn0kawY8/eVDI.y9.9sUMZsYdFy3jDPrjhjQNZ1TpqJTW', 'naveen@finxbridge.com', 'Admin', 'User', 'ACTIVE', TRUE),
('collmanager', '$2a$10$KAqljhNBfn0kawY8/eVDI.y9.9sUMZsYdFy3jDPrjhjQNZ1TpqJTW', 'naveenfinxbridge@gmail.com', 'Collections', 'Manager', 'ACTIVE', TRUE),
('agent1', '$2a$10$KAqljhNBfn0kawY8/eVDI.y9.9sUMZsYdFy3jDPrjhjQNZ1TpqJTW', 'agent1@example.com', 'Collections', 'Agent1', 'ACTIVE', TRUE);

-- ===============================================
-- INSERT: permissions (Retain only essential ones)
-- ===============================================
INSERT INTO permissions (permission_name, permission_code, resource, action, description) VALUES
-- User Management
('View Users', 'USER_READ', 'USER', 'READ', 'View user details'),
('Create Users', 'USER_CREATE', 'USER', 'CREATE', 'Create new users'),
('Update Users', 'USER_UPDATE', 'USER', 'UPDATE', 'Update user information'),

-- Case Management
('View Cases', 'CASE_READ', 'CASE', 'READ', 'View case details'),
('Update Cases', 'CASE_UPDATE', 'CASE', 'UPDATE', 'Update case information'),
('Allocate Cases', 'CASE_ALLOCATE', 'CASE', 'ALLOCATE', 'Allocate cases to agents'),

-- Payment
('View Payments', 'PAYMENT_READ', 'PAYMENT', 'READ', 'View payment details'),
('Record Payments', 'PAYMENT_CREATE', 'PAYMENT', 'CREATE', 'Record new payments');

-- ===============================================
-- INSERT: role_permissions (minimal mappings)
-- ===============================================
INSERT INTO role_permissions (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), -- Super Admin: all user permissions
(2, 1), (2, 3),         -- Admin: view & update users
(3, 4), (3, 5), (3, 6), -- Coll Manager: manage cases
(4, 4), (4, 8);         -- Agent: view cases, record payments

-- ===============================================
-- INSERT: user_roles
-- Purpose: Assign roles to sample users
-- ===============================================
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), -- superadmin -> Super Admin
(2, 2), -- admin -> Admin
(3, 3), -- collmanager -> Collections Manager
(4, 4); -- agent1 -> Collections Agent

-- ===============================================
-- INSERT: system_config
-- ===============================================
INSERT INTO system_config (config_key, config_value, data_type, description, is_active) VALUES
('SESSION_SINGLE_ENFORCEMENT', 'true', 'BOOLEAN', 'Enforce single active session per user', true);

INSERT INTO system_config (config_key, config_value, data_type, description, is_active) VALUES
('ENCRYPTION_SECRET_KEY', 'FinxBridge2025!!FinxBridge2025!!', 'STRING', 'Secret key for AES encryption/decryption in EncryptionUtil.', TRUE);

-- MSG91 Configuration for OTP Emails
INSERT INTO system_config (config_key, config_value, data_type, description, is_active) VALUES
('MSG91_OTP_TEMPLATE_ID', 'global_otp', 'STRING', 'Template ID from MSG91 for sending OTP emails.', true);

INSERT INTO system_config (config_key, config_value, data_type, description, is_active) VALUES
('MSG91_FROM_NAME', 'Swastisree Solutions', 'STRING', 'The From name to be used in OTP emails sent via MSG91.', true),
('SESSION_INACTIVITY_TIMEOUT_MINUTES', '30', 'INTEGER', 'Maximum session inactivity timeout in minutes', TRUE), -- Corrected key

-- Account Lockout & Security Configuration
('SECURITY_MAX_FAILED_LOGIN_ATTEMPTS', '5', 'INTEGER', 'Maximum failed login attempts before lockout', TRUE),
('SECURITY_ACCOUNT_LOCKOUT_DURATION_MINUTES', '30', 'INTEGER', 'Account lockout duration in minutes', TRUE),
('JWT_ACCESS_TOKEN_EXPIRATION_MINUTES', '15', 'INTEGER', 'JWT Access Token expiration in minutes', TRUE), -- Added
('JWT_REFRESH_TOKEN_EXPIRATION_DAYS', '7', 'INTEGER', 'JWT Refresh Token expiration in days', TRUE), -- Added
('JWT_RESET_TOKEN_EXPIRATION_MINUTES', '10', 'INTEGER', 'JWT Reset Token expiration in minutes', TRUE), -- Added

('PASSWORD_MIN_LENGTH', '8', 'INTEGER', 'Minimum password length', TRUE),
('PASSWORD_REQUIRE_UPPERCASE', 'true', 'BOOLEAN', 'Password must contain uppercase letter', TRUE),
('PASSWORD_REQUIRE_LOWERCASE', 'true', 'BOOLEAN', 'Password must contain lowercase letter', TRUE),
('PASSWORD_REQUIRE_NUMBER', 'true', 'BOOLEAN', 'Password must contain number', TRUE),
('PASSWORD_REQUIRE_SPECIAL_CHAR', 'true', 'BOOLEAN', 'Password must contain special character', TRUE),
('PASSWORD_EXPIRY_DAYS', '90', 'INTEGER', 'Password expiry period in days', TRUE),
('PASSWORD_HISTORY_COUNT', '5', 'INTEGER', 'Number of previous passwords to prevent reuse', TRUE),

-- OTP Configuration
('OTP_LENGTH', '6', 'INTEGER', 'OTP code length (number of digits)', TRUE),
('OTP_EXPIRY_MINUTES', '5', 'INTEGER', 'OTP expiry time in minutes', TRUE),
('OTP_EXPIRY_SECONDS', '300', 'INTEGER', 'OTP expiry time in seconds (for MSG91 API)', TRUE),
('OTP_MAX_ATTEMPTS', '3', 'INTEGER', 'Maximum OTP verification attempts', TRUE),
('OTP_RESEND_COOLDOWN_SECONDS', '60', 'INTEGER', 'Cooldown period before resending OTP in seconds', TRUE),

-- Two-Factor Authentication
('AUTH_ENABLE_TWO_FACTOR_AUTH', 'false', 'BOOLEAN', 'Enable two-factor authentication', TRUE),
('AUTH_2FA_ISSUER_NAME', 'Finxbridge Collections', 'STRING', '2FA issuer name for authenticator apps', TRUE),

-- Rate Limiting (Future Enhancement)
('RATE_LIMIT_LOGIN_PER_MINUTE', '5', 'INTEGER', 'Maximum login attempts per minute per IP', TRUE),
('RATE_LIMIT_OTP_PER_HOUR', '10', 'INTEGER', 'Maximum OTP requests per hour per user', TRUE),

-- Email Configuration for OTP
('EMAIL_FROM_ADDRESS', 'Info@mail.swastisree.com', 'STRING', 'From email address for OTP emails', TRUE),
('EMAIL_FROM_NAME', 'Swastisree Solutions', 'STRING', 'From name for OTP emails', TRUE),

-- Cache Configuration
('CACHE_CONFIG_TTL_SECONDS', '300', 'INTEGER', 'Cache TTL for system configuration in seconds', TRUE),
('CACHE_USER_PERMISSIONS_TTL_SECONDS', '600', 'INTEGER', 'Cache TTL for user permissions in seconds', TRUE),
('CACHE_SESSION_TTL_SECONDS', '900', 'INTEGER', 'Cache TTL for user sessions in seconds', TRUE),

-- WebClient Timeout Configuration (milliseconds)
('WEBCLIENT_CONNECTION_TIMEOUT', '30000', 'INTEGER', 'WebClient connection timeout in milliseconds (default: 30s)', TRUE),
('WEBCLIENT_READ_TIMEOUT', '60', 'INTEGER', 'WebClient read timeout in seconds (default: 60s)', TRUE),
('WEBCLIENT_WRITE_TIMEOUT', '60', 'INTEGER', 'WebClient write timeout in seconds (default: 60s)', TRUE),
('WEBCLIENT_RESPONSE_TIMEOUT', '60', 'INTEGER', 'WebClient response timeout in seconds (default: 60s)', TRUE),

-- WebClient Connection Pool Configuration
('WEBCLIENT_MAX_CONNECTIONS', '500', 'INTEGER', 'Maximum number of connections in pool', TRUE),
('WEBCLIENT_PENDING_ACQUIRE_TIMEOUT', '45000', 'INTEGER', 'Pending connection acquire timeout in milliseconds', TRUE),
('WEBCLIENT_MAX_IDLE_TIME', '20000', 'INTEGER', 'Maximum idle time for connections in milliseconds', TRUE),
('WEBCLIENT_MAX_LIFE_TIME', '60000', 'INTEGER', 'Maximum lifetime for connections in milliseconds', TRUE);

-- ===============================================
-- INSERT: third_party_integration_master
-- ===============================================
-- SMS Providers
INSERT INTO third_party_integration_master (integration_name, integration_type, api_endpoint,api_key_encrypted, config_json, is_active) VALUES
('MSG91_SMS', 'SMS_PROVIDER', 'https://control.msg91.com/api/v5',
    'BNO7wXg7GHI5KWOuJxqUAWFyq7Dm/ec2KJqZ5BSF1Z5GIuUzm3BuJsdjMG8hdHY8',
 '{"route": "TRANSACTIONAL", "dlt_registered": true, "daily_limit": 100000, "cost_per_sms": 0.15, "unicode_support": true}', TRUE),

-- WhatsApp Providers
('MSG91_WHATSAPP', 'WHATSAPP_PROVIDER', 'https://api.msg91.com/api/v5/whatsapp',
    'BNO7wXg7GHI5KWOuJxqUAWFyq7Dm/ec2KJqZ5BSF1Z5GIuUzm3BuJsdjMG8hdHY8',
 '{"verified": true, "template_support": true, "media_support": true, "daily_limit": 100000, "cost_per_message": 0.40}', TRUE),

('MSG91_EMAIL',
    'EMAIL_PROVIDER',
    'https://control.msg91.com/api/v5/email',
    'BNO7wXg7GHI5KWOuJxqUAWFyq7Dm/ec2KJqZ5BSF1Z5GIuUzm3BuJsdjMG8hdHY8',
    '{"daily_limit": 100000, "template_support": true, "analytics": true, "cost_per_email": 0.02, "region": "INDIA"}', TRUE),
    
-- OTP Providers
('MSG91_OTP',
    'OTP_PROVIDER',
    'https://control.msg91.com/api/v5/otp',
    'BNO7wXg7GHI5KWOuJxqUAWFyq7Dm/ec2KJqZ5BSF1Z5GIuUzm3BuJsdjMG8hdHY8',
    '{"template_id": "68ee3e406f2cc106e130bf47", "sender_id": "FINXCO", "daily_limit": 100000, "cost_per_otp": 0.15}', TRUE);   

-- ===============================================
-- INSERT: cache_config
-- Purpose: Default admin credentials for cache management
-- ===============================================
-- Username: admin
-- Password: Admin@123 (hashed with BCrypt)
INSERT INTO cache_config (username, password, is_active) VALUES
('admin', '$2a$10$7jKyJJ8qBIyYz9Azrj6EJ.2T/h5/apwWQhL4/4CRORtlvIIQtuZi.', TRUE);

--
-- =====================================================
-- END OF NORMALIZED SCHEMA
-- =====================================================
