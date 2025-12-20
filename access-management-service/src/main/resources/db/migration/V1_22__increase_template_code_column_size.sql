-- Increase template_code column size from 50 to 100 characters
-- This is needed because the auto-generated template codes include:
-- channel name + template name + timestamp, which can exceed 50 chars

ALTER TABLE templates ALTER COLUMN template_code TYPE VARCHAR(100);

-- Add comment for documentation
COMMENT ON COLUMN templates.template_code IS 'Unique template code (auto-generated): CHANNEL_TEMPLATENAME_TIMESTAMP, max 100 chars';
