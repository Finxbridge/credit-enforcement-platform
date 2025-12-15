-- =====================================================
-- CREDIT ENFORCEMENT PLATFORM - PAYMENT INTEGRATION SEED DATA
-- Migration V1_25: Update payment integration configurations
-- Dynamic QR, Payment Link, and Collect Call integrations
-- =====================================================

-- =====================================================
-- UPDATE EXISTING FINXBRIDGE INTEGRATIONS
-- =====================================================

-- Update FINXBRIDGE_DQR configuration
UPDATE third_party_integration_master
SET config_json = '{
    "provider": "PHONEPE",
    "merchant_id": "FINXBRIDGEUAT",
    "provider_id": "FINXBRIDGEPROVIDER",
    "store_id": "teststore1",
    "terminal_id": "testterminal1",
    "service_type": "DYNAMIC_QR",
    "qr_expiry_minutes": 15,
    "currency": "INR"
}'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE integration_name = 'FINXBRIDGE_DQR';

-- Update FINXBRIDGE_COLLECT_CALL configuration
UPDATE third_party_integration_master
SET config_json = '{
    "provider": "PHONEPE",
    "merchant_id": "FINXBRIDGEUAT",
    "provider_id": "FINXBRIDGEPROVIDER",
    "store_id": "teststore1",
    "terminal_id": "testterminal1",
    "service_type": "COLLECT_CALL",
    "collect_expiry_minutes": 5,
    "currency": "INR"
}'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE integration_name = 'FINXBRIDGE_COLLECT_CALL';

-- =====================================================
-- INSERT PAYMENT LINK INTEGRATION (if not exists)
-- =====================================================

INSERT INTO third_party_integration_master (integration_name, integration_type, api_endpoint, api_key_encrypted, config_json, is_active)
SELECT
    'FINXBRIDGE_PAYMENT_LINK',
    'PAYMENT_PROVIDER',
    'http://localhost:9000',
    NULL,
    '{
        "provider": "PHONEPE",
        "merchant_id": "FINXBRIDGEUAT",
        "provider_id": "FINXBRIDGEPROVIDER",
        "store_id": "teststore1",
        "terminal_id": "testterminal1",
        "service_type": "PAYMENT_LINK",
        "link_expiry_minutes": 1440,
        "currency": "INR"
    }'::jsonb,
    TRUE
WHERE NOT EXISTS (SELECT 1 FROM third_party_integration_master WHERE integration_name = 'FINXBRIDGE_PAYMENT_LINK');

-- =====================================================
-- INSERT IF NOT EXISTS (for fresh databases)
-- =====================================================

-- Insert FINXBRIDGE_DQR if not exists
INSERT INTO third_party_integration_master (integration_name, integration_type, api_endpoint, api_key_encrypted, config_json, is_active)
SELECT
    'FINXBRIDGE_DQR',
    'PAYMENT_PROVIDER',
    'http://localhost:9000',
    NULL,
    '{
        "provider": "PHONEPE",
        "merchant_id": "FINXBRIDGEUAT",
        "provider_id": "FINXBRIDGEPROVIDER",
        "store_id": "teststore1",
        "terminal_id": "testterminal1",
        "service_type": "DYNAMIC_QR",
        "qr_expiry_minutes": 15,
        "currency": "INR"
    }'::jsonb,
    TRUE
WHERE NOT EXISTS (SELECT 1 FROM third_party_integration_master WHERE integration_name = 'FINXBRIDGE_DQR');

-- Insert FINXBRIDGE_COLLECT_CALL if not exists
INSERT INTO third_party_integration_master (integration_name, integration_type, api_endpoint, api_key_encrypted, config_json, is_active)
SELECT
    'FINXBRIDGE_COLLECT_CALL',
    'PAYMENT_PROVIDER',
    'http://localhost:9000',
    NULL,
    '{
        "provider": "PHONEPE",
        "merchant_id": "FINXBRIDGEUAT",
        "provider_id": "FINXBRIDGEPROVIDER",
        "store_id": "teststore1",
        "terminal_id": "testterminal1",
        "service_type": "COLLECT_CALL",
        "collect_expiry_minutes": 5,
        "currency": "INR"
    }'::jsonb,
    TRUE
WHERE NOT EXISTS (SELECT 1 FROM third_party_integration_master WHERE integration_name = 'FINXBRIDGE_COLLECT_CALL');

-- =====================================================
-- COMMENTS
-- =====================================================

COMMENT ON TABLE third_party_integration_master IS 'Configuration for third-party payment integrations including Dynamic QR, Payment Link, and Collect Call';
