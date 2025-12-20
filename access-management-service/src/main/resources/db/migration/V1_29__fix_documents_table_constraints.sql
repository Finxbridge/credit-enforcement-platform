-- =====================================================
-- V1_29: Fix Documents Table Constraints
-- Make document_type nullable since we no longer use it
-- =====================================================

-- Make document_type nullable (was missed in V1_28)
ALTER TABLE documents ALTER COLUMN document_type DROP NOT NULL;
