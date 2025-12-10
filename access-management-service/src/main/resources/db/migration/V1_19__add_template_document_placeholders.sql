-- V1_19: Add document_placeholders column to templates table
-- This column stores JSON array of placeholders extracted from attached documents

ALTER TABLE templates ADD COLUMN IF NOT EXISTS document_placeholders TEXT;

COMMENT ON COLUMN templates.document_placeholders IS 'JSON array of placeholders extracted from document template';
