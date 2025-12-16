-- =====================================================
-- V1_28: Simplify Documents Table
-- Remove unused columns from documents table
-- Keep only essential fields for simple document storage
-- =====================================================

-- =====================================================
-- 1. DROP COLUMNS THAT ARE NO LONGER NEEDED
-- =====================================================

-- Make entity_type and entity_id nullable (no longer required)
ALTER TABLE documents ALTER COLUMN entity_type DROP NOT NULL;
ALTER TABLE documents ALTER COLUMN entity_id DROP NOT NULL;

-- Drop unused columns
ALTER TABLE documents DROP COLUMN IF EXISTS document_subtype;
ALTER TABLE documents DROP COLUMN IF EXISTS category_id;
ALTER TABLE documents DROP COLUMN IF EXISTS description;
ALTER TABLE documents DROP COLUMN IF EXISTS file_hash;
ALTER TABLE documents DROP COLUMN IF EXISTS storage_provider;
ALTER TABLE documents DROP COLUMN IF EXISTS metadata;
ALTER TABLE documents DROP COLUMN IF EXISTS tags;
ALTER TABLE documents DROP COLUMN IF EXISTS is_archived;
ALTER TABLE documents DROP COLUMN IF EXISTS archived_at;
ALTER TABLE documents DROP COLUMN IF EXISTS archived_by;
ALTER TABLE documents DROP COLUMN IF EXISTS version_number;
ALTER TABLE documents DROP COLUMN IF EXISTS parent_document_id;
ALTER TABLE documents DROP COLUMN IF EXISTS retention_days;
ALTER TABLE documents DROP COLUMN IF EXISTS expires_at;
ALTER TABLE documents DROP COLUMN IF EXISTS created_by;
ALTER TABLE documents DROP COLUMN IF EXISTS updated_by;

-- =====================================================
-- 2. DROP UNUSED RELATED TABLES
-- =====================================================

DROP TABLE IF EXISTS document_categories CASCADE;
DROP TABLE IF EXISTS document_export_jobs CASCADE;

-- =====================================================
-- 3. UPDATE TABLE COMMENT
-- =====================================================

COMMENT ON TABLE documents IS 'Simple document storage - stores file metadata and S3 location';
COMMENT ON COLUMN documents.document_id IS 'DMS-generated unique ID: DOC-YYYYMMDD-XXXXXXXX';
COMMENT ON COLUMN documents.document_name IS 'Custom name or original filename';
COMMENT ON COLUMN documents.file_url IS 'Full S3 URL';
COMMENT ON COLUMN documents.document_status IS 'ACTIVE or DELETED';
