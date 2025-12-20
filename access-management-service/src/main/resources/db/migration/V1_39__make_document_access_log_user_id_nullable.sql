-- Make user_id nullable in document_access_logs table
-- This allows anonymous or system downloads without a user context

ALTER TABLE document_access_logs ALTER COLUMN user_id DROP NOT NULL;
