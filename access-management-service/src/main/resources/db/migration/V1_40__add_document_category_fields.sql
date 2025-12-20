-- Add document categorization fields to documents table
-- Allows filtering by category (TEMPLATE, GENERATED, USER_UPLOAD) and channel

-- Add document_category column
ALTER TABLE documents ADD COLUMN IF NOT EXISTS document_category VARCHAR(20) DEFAULT 'USER_UPLOAD';

-- Add channel column
ALTER TABLE documents ADD COLUMN IF NOT EXISTS channel VARCHAR(20);

-- Add case_id column (for GENERATED documents)
ALTER TABLE documents ADD COLUMN IF NOT EXISTS case_id BIGINT;

-- Add source_template_id column (for GENERATED documents - links to template)
ALTER TABLE documents ADD COLUMN IF NOT EXISTS source_template_id BIGINT;

-- Create indexes for efficient filtering
CREATE INDEX IF NOT EXISTS idx_documents_category ON documents(document_category);
CREATE INDEX IF NOT EXISTS idx_documents_channel ON documents(channel);
CREATE INDEX IF NOT EXISTS idx_documents_category_channel ON documents(document_category, channel);
CREATE INDEX IF NOT EXISTS idx_documents_case_id ON documents(case_id);

-- Update existing documents to USER_UPLOAD category
UPDATE documents SET document_category = 'USER_UPLOAD' WHERE document_category IS NULL;
