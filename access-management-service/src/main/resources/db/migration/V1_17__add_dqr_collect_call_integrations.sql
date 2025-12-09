-- V1_17: Add Dynamic QR (DQR) and Collect Call integration configurations
-- These providers share the same FinxBridge API endpoint but use different endpoints

-- Insert FINXBRIDGE_DQR if not exists
INSERT INTO third_party_integration_master (integration_name, integration_type, api_endpoint, api_key_encrypted, config_json, is_active)
SELECT
    'FINXBRIDGE_DQR',
    'PAYMENT_PROVIDER',
    'http://localhost:9000',
    NULL,
    '{"provider": "PHONEPE", "merchant_id": "FINXBRIDGE_MERCHANT", "store_id": "STORE001", "service_type": "DYNAMIC_QR", "qr_expiry_minutes": 15, "currency": "INR"}',
    TRUE
WHERE NOT EXISTS (SELECT 1 FROM third_party_integration_master WHERE integration_name = 'FINXBRIDGE_DQR');

-- Insert FINXBRIDGE_COLLECT_CALL if not exists
INSERT INTO third_party_integration_master (integration_name, integration_type, api_endpoint, api_key_encrypted, config_json, is_active)
SELECT
    'FINXBRIDGE_COLLECT_CALL',
    'PAYMENT_PROVIDER',
    'http://localhost:9000',
    NULL,
    '{"provider": "PHONEPE", "merchant_id": "FINXBRIDGE_MERCHANT", "store_id": "STORE001", "service_type": "COLLECT_CALL", "collect_expiry_minutes": 5, "currency": "INR"}',
    TRUE
WHERE NOT EXISTS (SELECT 1 FROM third_party_integration_master WHERE integration_name = 'FINXBRIDGE_COLLECT_CALL');
