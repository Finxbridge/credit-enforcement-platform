-- ===================================================
-- V1_36: Add media_upload_url to MSG91_WHATSAPP config
-- ===================================================
-- This migration adds the media_upload_url to the MSG91_WHATSAPP
-- integration config for uploading documents to get header_handle
-- for WhatsApp template creation with document attachments.
--
-- API: POST https://api.msg91.com/api/v5/whatsapp/sample-media-upload/
-- Form fields: whatsapp_number, media (file)
-- ===================================================

UPDATE third_party_integration_master
SET config_json = jsonb_set(
    config_json::jsonb,
    '{media_upload_url}',
    '"https://api.msg91.com/api/v5/whatsapp/sample-media-upload/"'
)
WHERE integration_name = 'MSG91_WHATSAPP';
