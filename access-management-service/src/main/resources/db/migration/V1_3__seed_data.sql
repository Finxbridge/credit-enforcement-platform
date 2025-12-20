-- ===================================================
-- CREDIT ENFORCEMENT PLATFORM - SEED DATA
-- Initial Data for System Configuration
-- ===================================================
-- This file contains all INSERT statements for seed data
-- Consolidated from: V2__seed_data.sql
-- ===================================================

-- ===============================================
-- INSERT: user_groups
-- Purpose: Sample user groups for organizing users
-- ===============================================
INSERT INTO user_groups (group_name, group_code, group_type, parent_group_id, description, is_active) VALUES
('Head Office', 'REGION_HO', 'REGION', NULL, 'Central administrative region', TRUE),
('Collections Team', 'TEAM_COLL', 'TEAM', 1, 'Internal collections management team', TRUE),
('Agency Partners', 'TEAM_AGENCY', 'TEAM', 1, 'External collections agency partners', TRUE);

-- ===============================================
-- INSERT: role_groups (Minimal)
-- ===============================================
INSERT INTO role_groups (group_name, group_code, description, display_order, is_active) VALUES
('System Administration', 'GRP_SYS_ADMIN', 'System-level admin roles', 1, TRUE),
('Collections Management', 'GRP_COLL_MGMT', 'Roles related to collections management', 2, TRUE),
('Collections Agency', 'GRP_COLL_AGENCY', 'Roles related to collections agencies', 3, TRUE);

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
INSERT INTO users (username, password_hash, email, first_name, last_name, status, is_first_login, assigned_geographies) VALUES
('naveen', '$2a$10$KAqljhNBfn0kawY8/eVDI.y9.9sUMZsYdFy3jDPrjhjQNZ1TpqJTW', 'naveen@finxbridge.com', 'Naveen', 'Manyam', 'ACTIVE', FALSE, '["BANGALORE"]'::jsonb),
('admin', '$2a$10$KAqljhNBfn0kawY8/eVDI.y9.9sUMZsYdFy3jDPrjhjQNZ1TpqJTW', 'shivani@finxbridge.com', 'Admin', 'User', 'ACTIVE', FALSE, '["MUMBAI", "DELHI", "BANGALORE"]'::jsonb),
('vaishnavi', '$2a$10$KAqljhNBfn0kawY8/eVDI.y9.9sUMZsYdFy3jDPrjhjQNZ1TpqJTW', 'vaishnavi.g@finxbridge.com', 'Vaishnavi', 'Gandla', 'ACTIVE', FALSE, '["HYDERABAD"]'::jsonb);

-- ===============================================
-- INSERT: permissions (Retain only essential ones)
-- ===============================================
INSERT INTO permissions (permission_name, permission_code, resource, action, description) VALUES
-- User Management
('View Users', 'USER_READ', 'USER', 'READ', 'View user details'),
('Create Users', 'USER_CREATE', 'USER', 'CREATE', 'Create new users'),
('Update Users', 'USER_UPDATE', 'USER', 'UPDATE', 'Update user information'),
('Delete Users', 'USER_DELETE', 'USER', 'DELETE', 'Delete users'),

-- Role Management
('View Roles', 'ROLE_READ', 'ROLE', 'READ', 'View role details'),
('Create Roles', 'ROLE_CREATE', 'ROLE', 'CREATE', 'Create new roles'),
('Update Roles', 'ROLE_UPDATE', 'ROLE', 'UPDATE', 'Update role information'),
('Delete Roles', 'ROLE_DELETE', 'ROLE', 'DELETE', 'Delete roles'),

-- Permission Management
('View Permissions', 'PERMISSION_READ', 'PERMISSION', 'READ', 'View permission details'),
('Create Permissions', 'PERMISSION_CREATE', 'PERMISSION', 'CREATE', 'Create new permissions'),
('Update Permissions', 'PERMISSION_UPDATE', 'PERMISSION', 'UPDATE', 'Update permission information'),
('Delete Permissions', 'PERMISSION_DELETE', 'PERMISSION', 'DELETE', 'Delete permissions');

-- ===============================================
-- INSERT: role_permissions (minimal mappings)
-- ===============================================
INSERT INTO role_permissions (role_id, permission_id) VALUES
-- Super Admin: all permissions
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11), (1, 12),

-- Admin: all permissions
(2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 7), (2, 8), (2, 9), (2, 10), (2, 11), (2, 12),

(3, 1), (3, 2), (3, 3), (3, 4), (3, 5), (3, 6), (3, 7), (3, 8), (3, 9), (3, 10), (3, 11), (3, 12);

-- ===============================================
-- INSERT: user_roles
-- Purpose: Assign roles to sample users
-- ===============================================
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), -- superadmin -> Super Admin
(2, 2),
(3, 1); -- admin -> Admin

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
('JWT_ACCESS_TOKEN_EXPIRATION_MINUTES', '60', 'INTEGER', 'JWT Access Token expiration in minutes', TRUE), -- Added
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
-- SMS Provider - All endpoints stored in config_json
INSERT INTO third_party_integration_master (integration_name, integration_type, api_endpoint, api_key_encrypted, config_json, is_active) VALUES
('MSG91_SMS', 'SMS_PROVIDER', 'https://control.msg91.com/api/v5/flow',
    '469650An6LVQBk68d37c1dP1',
 '{"route": "TRANSACTIONAL", "dlt_registered": true, "daily_limit": 100000, "cost_per_sms": 0.15, "unicode_support": true, "sms_type": "NORMAL", "sender_id": "SWSTIS", "add_template_url": "https://control.msg91.com/api/v5/sms/addTemplate", "get_template_versions_url": "https://control.msg91.com/api/v5/sms/getTemplateVersions", "add_template_version_url": "https://control.msg91.com/api/v5/sms/addTemplateVersion", "mark_active_url": "https://control.msg91.com/api/v5/sms/markActive", "logs_url": "https://control.msg91.com/api/v5/report/logs/p/sms", "analytics_url": "https://control.msg91.com/api/v5/report/analytics/p/sms"}', TRUE),

-- WhatsApp Provider - All endpoints stored in config_json (namespace and integrated_number required)
('MSG91_WHATSAPP', 'WHATSAPP_PROVIDER', 'https://api.msg91.com/api/v5/whatsapp/whatsapp-outbound-message/bulk/',
    '469650An6LVQBk68d37c1dP1',
 '{"verified": true, "template_support": true, "media_support": true, "daily_limit": 100000, "cost_per_message": 0.40, "namespace": "34f55069_1932_4d78_818a_ec1cdfda76c4", "integrated_number": "918143170546", "create_template_url": "https://api.msg91.com/api/v5/whatsapp/client-panel-template/", "single_message_url": "https://api.msg91.com/api/v5/whatsapp/whatsapp-outbound-message/"}', TRUE),

-- Email Provider - All endpoints stored in config_json
('MSG91_EMAIL',
    'EMAIL_PROVIDER',
    'https://control.msg91.com/api/v5/email/send',
    '469650An6LVQBk68d37c1dP1',
    '{"daily_limit": 100000, "template_support": true, "analytics": true, "cost_per_email": 0.02, "region": "INDIA", "create_template_url": "https://control.msg91.com/api/v5/email/templates"}', TRUE),

-- OTP Provider - Full endpoint for OTP API
('MSG91_OTP',
    'OTP_PROVIDER',
    'https://control.msg91.com/api/v5/otp',
    '469650An6LVQBk68d37c1dP1',
    '{"template_id": "68ee3e406f2cc106e130bf47", "sender_id": "FINXCO", "daily_limit": 100000, "cost_per_otp": 0.15, "resend_url": "https://control.msg91.com/api/v5/otp/retry", "verify_url": "https://control.msg91.com/api/v5/otp/verify"}', TRUE),

-- Voice Provider - All endpoints stored in config_json
('MSG91_VOICE',
    'VOICE_PROVIDER',
    'https://api.msg91.com/api/v5/voice/flow/',
    '469650An6LVQBk68d37c1dP1',
    '{"click_to_call_url": "https://api.msg91.com/api/v5/voice/call/ctc", "voice_sms_url": "https://api.msg91.com/api/v5/voice/call/", "logs_url": "https://api.msg91.com/api/v5/voice/call-logs/"}', TRUE),

-- FinxBridge Payment Gateway Provider
-- Endpoints: /api/v1/paymentLink/getPaymentLink, /api/v1/service/status, /api/v1/service/refund
('FINXBRIDGE_PAYMENT_LINK',
    'PAYMENT_PROVIDER',
    'http://localhost:9000',
    NULL,
    '{"provider": "PHONEPE", "merchant_id": "FINXBRIDGE_MERCHANT", "store_id": "STORE001", "terminal_id": "TERM001", "service_type": "PAYMENT_LINK", "daily_limit": 10000, "cost_per_transaction": 2.00, "supported_methods": ["UPI", "CARD", "NETBANKING", "WALLET"], "currency": "INR"}', TRUE),

-- OVH Cloud Object Storage (S3-compatible) for Document Management
-- Reference: https://help.ovhcloud.com/csm/en-public-cloud-storage-s3-location
-- Location: London (UK) - endpoint: s3.uk.io.cloud.ovh.net
-- Bucket: wrathful-de-gennes
('OVH_S3_STORAGE',
    'STORAGE_PROVIDER',
    'https://s3.uk.io.cloud.ovh.net',
    '563d28e30a154fa3b964b67ff7e54b37',
    '{"provider": "OVH", "region": "uk", "secret_key": "514dddebf3b54bfa84b2474c270922f5", "bucket_templates": "wrathful-de-gennes", "bucket_documents": "wrathful-de-gennes", "bucket_processed": "wrathful-de-gennes", "signature_version": "s3v4", "path_style_access": true, "max_file_size_mb": 50, "allowed_extensions": ["pdf", "doc", "docx", "png", "jpg", "jpeg", "gif"], "public_url_prefix": "https://s3.uk.io.cloud.ovh.net/wrathful-de-gennes"}', TRUE);

-- ===============================================
-- INSERT: cache_config
-- Purpose: Default admin credentials for cache management
-- ===============================================
-- Username: admin
-- Password: Admin@123 (hashed with BCrypt)
INSERT INTO cache_config (username, password, is_active) VALUES
('admin', '$2a$10$7jKyJJ8qBIyYz9Azrj6EJ.2T/h5/apwWQhL4/4CRORtlvIIQtuZi.', TRUE);


-- =============================================
-- STRATEGY ENGINE SERVICE SEED DATA
-- =============================================

-- Default Filter Fields (using actual columns: field_code, field_key, field_type, display_name)
INSERT INTO filter_fields (field_code, field_key, field_type, display_name, description, is_active, sort_order) VALUES
-- Numeric Fields
('DPD', 'dpd', 'NUMERIC', 'Days Past Due (DPD)', 'Number of days past due', true, 1),
('POS', 'principalOutstanding', 'NUMERIC', 'Principal Outstanding', 'Principal amount outstanding', true, 2),
('EMI', 'emiAmount', 'NUMERIC', 'EMI Amount', 'Monthly EMI amount', true, 3),
('TOTAL_OS', 'totalOutstanding', 'NUMERIC', 'Total Outstanding', 'Total outstanding amount', true, 4),
('LOAN_AMT', 'loanAmount', 'NUMERIC', 'Loan Amount', 'Original loan amount', true, 5),
-- Text/Dropdown Fields
('BUCKET', 'bucket', 'TEXT', 'Bucket', 'Delinquency bucket', true, 6),
('PRODUCT', 'productType', 'TEXT', 'Product Type', 'Type of loan product', true, 7),
('STATE', 'state', 'TEXT', 'State', 'Customer state', true, 8),
('CITY', 'city', 'TEXT', 'City', 'Customer city', true, 9),
('STATUS', 'status', 'TEXT', 'Case Status', 'Current case status', true, 10),
-- Date Fields
('DUE_DATE', 'nextDueDate', 'DATE', 'Due Date', 'Next payment due date', true, 11),
('LAST_PAY_DATE', 'lastPaymentDate', 'DATE', 'Last Payment Date', 'Date of last payment', true, 12)
ON CONFLICT (field_code) DO NOTHING;

-- Default Filter Field Options for Dropdowns
INSERT INTO filter_field_options (filter_field_id, option_value, option_label, sort_order, is_active)
SELECT ff.id, 'CURRENT', 'Current (0 DPD)', 1, true FROM filter_fields ff WHERE ff.field_code = 'BUCKET'
UNION ALL SELECT ff.id, 'X', 'Bucket X (1-30 DPD)', 2, true FROM filter_fields ff WHERE ff.field_code = 'BUCKET'
UNION ALL SELECT ff.id, '1', 'Bucket 1 (31-60 DPD)', 3, true FROM filter_fields ff WHERE ff.field_code = 'BUCKET'
UNION ALL SELECT ff.id, '2', 'Bucket 2 (61-90 DPD)', 4, true FROM filter_fields ff WHERE ff.field_code = 'BUCKET'
UNION ALL SELECT ff.id, '3', 'Bucket 3 (91-120 DPD)', 5, true FROM filter_fields ff WHERE ff.field_code = 'BUCKET'
UNION ALL SELECT ff.id, '4', 'Bucket 4 (121-150 DPD)', 6, true FROM filter_fields ff WHERE ff.field_code = 'BUCKET'
UNION ALL SELECT ff.id, '5', 'Bucket 5 (151-180 DPD)', 7, true FROM filter_fields ff WHERE ff.field_code = 'BUCKET'
UNION ALL SELECT ff.id, 'NPA', 'NPA (180+ DPD)', 8, true FROM filter_fields ff WHERE ff.field_code = 'BUCKET'
ON CONFLICT ON CONSTRAINT unique_field_option DO NOTHING;

INSERT INTO filter_field_options (filter_field_id, option_value, option_label, sort_order, is_active)
SELECT ff.id, 'ACTIVE', 'Active', 1, true FROM filter_fields ff WHERE ff.field_code = 'STATUS'
UNION ALL SELECT ff.id, 'CLOSED', 'Closed', 2, true FROM filter_fields ff WHERE ff.field_code = 'STATUS'
UNION ALL SELECT ff.id, 'ON_HOLD', 'On Hold', 3, true FROM filter_fields ff WHERE ff.field_code = 'STATUS'
UNION ALL SELECT ff.id, 'SETTLED', 'Settled', 4, true FROM filter_fields ff WHERE ff.field_code = 'STATUS'
ON CONFLICT ON CONSTRAINT unique_field_option DO NOTHING;

-- =============================================
-- TEMPLATE MANAGEMENT SERVICE SEED DATA
-- =============================================

-- Default Variable Definitions (using actual columns: variable_key, display_name, entity_path, data_type)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active) VALUES
-- Customer Variables
('customer_name', 'Customer Name', 'customer.name', 'STRING', 'Full name of the customer', 'CUSTOMER', 'John Doe', true),
('customer_first_name', 'Customer First Name', 'customer.firstName', 'STRING', 'First name of the customer', 'CUSTOMER', 'John', true),
('customer_email', 'Customer Email', 'customer.email', 'STRING', 'Email address of the customer', 'CUSTOMER', 'john@example.com', true),
('customer_phone', 'Customer Phone', 'customer.phoneNumber', 'STRING', 'Phone number of the customer', 'CUSTOMER', '9876543210', true),
-- Loan Variables
('loan_account_number', 'Loan Account Number', 'loanDetails.loanAccountNumber', 'STRING', 'Loan account number', 'LOAN', 'LN123456', true),
('outstanding_amount', 'Outstanding Amount', 'loanDetails.totalOutstanding', 'DECIMAL', 'Total outstanding amount', 'LOAN', '50000.00', true),
('emi_amount', 'EMI Amount', 'loanDetails.emiAmount', 'DECIMAL', 'Monthly EMI amount', 'LOAN', '5000.00', true),
('due_date', 'Due Date', 'loanDetails.nextDueDate', 'DATE', 'Next payment due date', 'LOAN', '2024-01-15', true),
('dpd', 'Days Past Due', 'loanDetails.dpd', 'INTEGER', 'Number of days past due', 'LOAN', '30', true),
('principal_outstanding', 'Principal Outstanding', 'loanDetails.principalOutstanding', 'DECIMAL', 'Principal amount outstanding', 'LOAN', '45000.00', true),
-- Company Variables
('company_name', 'Company Name', 'static.companyName', 'STRING', 'Name of the company', 'COMPANY', 'FinXBridge', true),
('company_phone', 'Company Phone', 'static.companyPhone', 'STRING', 'Company contact number', 'COMPANY', '+91-XXXXXXXXXX', true),
('company_email', 'Company Email', 'static.companyEmail', 'STRING', 'Company email address', 'COMPANY', 'support@finxbridge.com', true),
-- Dynamic Variables
('payment_link', 'Payment Link', 'generated.paymentLink', 'STRING', 'Payment link URL', 'DYNAMIC', 'https://pay.example.com/xyz', true),
('current_date', 'Current Date', 'computed.currentDate', 'DATE', 'Current date', 'DYNAMIC', '2024-01-01', true),
('agent_name', 'Agent Name', 'user.firstName', 'STRING', 'Assigned agent name', 'AGENT', 'Agent Name', true)
ON CONFLICT (variable_key) DO NOTHING;

-- =====================================================
-- END OF SEED DATA
-- =====================================================
