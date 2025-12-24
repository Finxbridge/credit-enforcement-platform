-- Add reminder template and channel columns to ptp_commitments table
-- These columns support the PTP reminder functionality with template selection

-- Add reminder_template_id column
ALTER TABLE ptp_commitments ADD COLUMN IF NOT EXISTS reminder_template_id BIGINT;

-- Add reminder_template_code column
ALTER TABLE ptp_commitments ADD COLUMN IF NOT EXISTS reminder_template_code VARCHAR(100);

-- Add reminder_channel column (WHATSAPP, SMS, EMAIL)
ALTER TABLE ptp_commitments ADD COLUMN IF NOT EXISTS reminder_channel VARCHAR(20);

-- Add comments
COMMENT ON COLUMN ptp_commitments.reminder_template_id IS 'Template ID from Template Management Service for sending reminders';
COMMENT ON COLUMN ptp_commitments.reminder_template_code IS 'Template code/name for reference';
COMMENT ON COLUMN ptp_commitments.reminder_channel IS 'Communication channel for reminder (WHATSAPP, SMS, EMAIL)';
