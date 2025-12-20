-- =====================================================
-- CREDIT ENFORCEMENT PLATFORM - ADD PAYMENT API ENDPOINT PATHS
-- Migration V1_41: Add full API endpoint paths to payment integration configs
-- All API paths are now database-configurable (no hardcoded defaults)
-- =====================================================

-- =====================================================
-- UPDATE FINXBRIDGE_DQR CONFIGURATION
-- =====================================================
UPDATE third_party_integration_master
SET config_json = '{
    "provider": "FINXBRIDGEPROVIDER",
    "merchant_id": "FINXBRIDGEUAT",
    "provider_id": "FINXBRIDGEPROVIDER",
    "store_id": "teststore1",
    "terminal_id": "testterminal1",
    "service_type": "DYNAMIC_QR",
    "qr_expiry_minutes": 15,
    "currency": "INR",
    "init_endpoint": "/api/v1/dqr/init",
    "status_endpoint": "/api/v1/service/status",
    "cancel_endpoint": "/api/v1/service/cancel",
    "refund_endpoint": "/api/v1/service/refund"
}'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE integration_name = 'FINXBRIDGE_DQR';

-- =====================================================
-- UPDATE FINXBRIDGE_COLLECT_CALL CONFIGURATION
-- =====================================================
UPDATE third_party_integration_master
SET config_json = '{
    "provider": "FINXBRIDGEPROVIDER",
    "merchant_id": "FINXBRIDGEUAT",
    "provider_id": "FINXBRIDGEPROVIDER",
    "store_id": "teststore1",
    "terminal_id": "testterminal1",
    "service_type": "COLLECT_CALL",
    "collect_expiry_minutes": 5,
    "currency": "INR",
    "instrument_type": "MOBILE",
    "init_endpoint": "/api/v1/collect/collect-call",
    "status_endpoint": "/api/v1/service/status",
    "cancel_endpoint": "/api/v1/service/cancel",
    "refund_endpoint": "/api/v1/service/refund"
}'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE integration_name = 'FINXBRIDGE_COLLECT_CALL';

-- =====================================================
-- UPDATE FINXBRIDGE_PAYMENT_LINK CONFIGURATION
-- =====================================================
UPDATE third_party_integration_master
SET config_json = '{
    "provider": "FINXBRIDGEPROVIDER",
    "merchant_id": "FINXBRIDGEUAT",
    "provider_id": "FINXBRIDGEPROVIDER",
    "store_id": "teststore1",
    "terminal_id": "testterminal1",
    "service_type": "PAYMENT_LINK",
    "link_expiry_minutes": 1440,
    "currency": "INR",
    "init_endpoint": "/api/v1/paymentLink/getPaymentLink",
    "status_endpoint": "/api/v1/service/status",
    "cancel_endpoint": "/api/v1/service/cancel",
    "refund_endpoint": "/api/v1/service/refund",
    "default_message": "Payment request from FinX Collections"
}'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE integration_name = 'FINXBRIDGE_PAYMENT_LINK';

-- =====================================================
-- COMMENTS
-- =====================================================
COMMENT ON TABLE third_party_integration_master IS 'Configuration for third-party payment integrations with full API endpoint paths. Required config fields: api_endpoint (base URL), init_endpoint, status_endpoint, cancel_endpoint, refund_endpoint';
