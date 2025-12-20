-- V1_16: Update OVH S3 Storage configuration for London (UK) region
-- Bucket: wrathful-de-gennes
-- Endpoint: s3.uk.io.cloud.ovh.net (OVHcloud S3 unified endpoint format)
-- Reference: https://help.ovhcloud.com/csm/en-public-cloud-storage-s3-location

-- Update existing OVH_S3_STORAGE configuration
UPDATE third_party_integration_master
SET
    api_endpoint = 'https://s3.uk.io.cloud.ovh.net',
    api_key_encrypted = '563d28e30a154fa3b964b67ff7e54b37',
    config_json = '{"provider": "OVH", "region": "uk", "secret_key": "514dddebf3b54bfa84b2474c270922f5", "bucket_templates": "wrathful-de-gennes", "bucket_documents": "wrathful-de-gennes", "bucket_processed": "wrathful-de-gennes", "signature_version": "s3v4", "path_style_access": true, "max_file_size_mb": 50, "allowed_extensions": ["pdf", "doc", "docx", "png", "jpg", "jpeg", "gif"], "public_url_prefix": "https://s3.uk.io.cloud.ovh.net/wrathful-de-gennes"}',
    updated_at = CURRENT_TIMESTAMP
WHERE integration_name = 'OVH_S3_STORAGE';

-- Insert if not exists (for fresh installations)
INSERT INTO third_party_integration_master (integration_name, integration_type, api_endpoint, api_key_encrypted, config_json, is_active)
SELECT
    'OVH_S3_STORAGE',
    'STORAGE_PROVIDER',
    'https://s3.uk.io.cloud.ovh.net',
    '563d28e30a154fa3b964b67ff7e54b37',
    '{"provider": "OVH", "region": "uk", "secret_key": "514dddebf3b54bfa84b2474c270922f5", "bucket_templates": "wrathful-de-gennes", "bucket_documents": "wrathful-de-gennes", "bucket_processed": "wrathful-de-gennes", "signature_version": "s3v4", "path_style_access": true, "max_file_size_mb": 50, "allowed_extensions": ["pdf", "doc", "docx", "png", "jpg", "jpeg", "gif"], "public_url_prefix": "https://s3.uk.io.cloud.ovh.net/wrathful-de-gennes"}',
    TRUE
WHERE NOT EXISTS (SELECT 1 FROM third_party_integration_master WHERE integration_name = 'OVH_S3_STORAGE');
