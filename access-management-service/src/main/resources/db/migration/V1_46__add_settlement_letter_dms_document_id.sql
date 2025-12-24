-- =====================================================
-- Add DMS document ID column to settlement_letters table
-- This column stores the reference to the document uploaded to DMS service
-- =====================================================

ALTER TABLE settlement_letters ADD COLUMN IF NOT EXISTS dms_document_id VARCHAR(100);

-- Add index for faster lookups by DMS document ID
CREATE INDEX IF NOT EXISTS idx_settlement_letters_dms_document_id ON settlement_letters(dms_document_id);

COMMENT ON COLUMN settlement_letters.dms_document_id IS 'Reference to the document stored in DMS service';
