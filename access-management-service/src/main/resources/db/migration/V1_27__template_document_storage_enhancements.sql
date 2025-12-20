-- =====================================================
-- V1_27: Template Document Storage Enhancements
-- Add document_storage_path for direct S3 path storage
-- Increase column sizes to prevent length issues
-- =====================================================

-- =====================================================
-- 1. TEMPLATES TABLE - Document Storage Path Columns
-- =====================================================

-- Add document storage path for direct S3 access
ALTER TABLE templates ADD COLUMN IF NOT EXISTS document_storage_path VARCHAR(500);
ALTER TABLE templates ADD COLUMN IF NOT EXISTS document_storage_bucket VARCHAR(100);
ALTER TABLE templates ADD COLUMN IF NOT EXISTS document_content_type VARCHAR(100);

-- Increase column sizes for future-proofing
ALTER TABLE templates ALTER COLUMN dms_document_id TYPE VARCHAR(100);
ALTER TABLE templates ALTER COLUMN document_url TYPE VARCHAR(1000);
ALTER TABLE templates ALTER COLUMN document_original_name TYPE VARCHAR(500);

-- Add comments
COMMENT ON COLUMN templates.document_storage_path IS 'S3 storage path: templates/{channel}/{template_id}/{filename}';
COMMENT ON COLUMN templates.document_storage_bucket IS 'S3 bucket name for document storage';
COMMENT ON COLUMN templates.document_content_type IS 'MIME type of the document';

-- Create index for storage path lookup
CREATE INDEX IF NOT EXISTS idx_templates_storage_path ON templates(document_storage_path) WHERE document_storage_path IS NOT NULL;

-- =====================================================
-- 2. DMS DOCUMENTS TABLE - Increase Column Sizes
-- =====================================================

ALTER TABLE documents ALTER COLUMN document_id TYPE VARCHAR(150);
ALTER TABLE documents ALTER COLUMN file_url TYPE VARCHAR(1000);
ALTER TABLE documents ALTER COLUMN storage_path TYPE VARCHAR(1000);
ALTER TABLE documents ALTER COLUMN document_name TYPE VARCHAR(500);
ALTER TABLE documents ALTER COLUMN file_name TYPE VARCHAR(500);

-- =====================================================
-- 3. COMMUNICATION HISTORY - Increase Column Sizes
-- =====================================================

ALTER TABLE communication_history ALTER COLUMN dms_document_id TYPE VARCHAR(100);
ALTER TABLE communication_history ALTER COLUMN original_document_url TYPE VARCHAR(1000);
ALTER TABLE communication_history ALTER COLUMN processed_document_url TYPE VARCHAR(1000);
ALTER TABLE communication_history ALTER COLUMN document_original_name TYPE VARCHAR(500);

-- =====================================================
-- 4. NOTICES TABLE - Increase Column Sizes
-- =====================================================

ALTER TABLE notices ALTER COLUMN dms_document_id TYPE VARCHAR(100);
ALTER TABLE notices ALTER COLUMN pdf_url TYPE VARCHAR(1000);
ALTER TABLE notices ALTER COLUMN original_document_url TYPE VARCHAR(1000);
ALTER TABLE notices ALTER COLUMN processed_document_url TYPE VARCHAR(1000);
ALTER TABLE notices ALTER COLUMN document_original_name TYPE VARCHAR(500);

-- =====================================================
-- 5. RECEIPTS & SETTLEMENT LETTERS - Increase Column Sizes
-- =====================================================

ALTER TABLE receipts ALTER COLUMN pdf_url TYPE VARCHAR(1000);
ALTER TABLE settlement_letters ALTER COLUMN pdf_url TYPE VARCHAR(1000);

-- =====================================================
-- 6. DMS DOCUMENT EXPORT JOBS - Increase Column Sizes
-- =====================================================

ALTER TABLE document_export_jobs ALTER COLUMN export_file_url TYPE VARCHAR(1000);
