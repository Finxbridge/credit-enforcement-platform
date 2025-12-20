-- =====================================================
-- CREDIT ENFORCEMENT PLATFORM - FIX PAYMENT INTEGRATION PROVIDER
-- Migration V1_26: Update provider value from PHONEPE to FINXBRIDGEPROVIDER
-- =====================================================

-- Update FINXBRIDGE_DQR configuration
UPDATE third_party_integration_master
SET config_json = jsonb_set(config_json, '{provider}', '"FINXBRIDGEPROVIDER"'),
    updated_at = CURRENT_TIMESTAMP
WHERE integration_name = 'FINXBRIDGE_DQR';

-- Update FINXBRIDGE_COLLECT_CALL configuration
UPDATE third_party_integration_master
SET config_json = jsonb_set(config_json, '{provider}', '"FINXBRIDGEPROVIDER"'),
    updated_at = CURRENT_TIMESTAMP
WHERE integration_name = 'FINXBRIDGE_COLLECT_CALL';

-- Update FINXBRIDGE_PAYMENT_LINK configuration
UPDATE third_party_integration_master
SET config_json = jsonb_set(config_json, '{provider}', '"FINXBRIDGEPROVIDER"'),
    updated_at = CURRENT_TIMESTAMP
WHERE integration_name = 'FINXBRIDGE_PAYMENT_LINK';
