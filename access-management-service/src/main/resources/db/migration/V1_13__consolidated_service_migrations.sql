-- =====================================================
-- V1_13: Consolidated Service Migrations
-- Migrations from: template-management, notice-management, strategy-engine
-- =====================================================

-- =====================================================
-- 1. TEMPLATE MANAGEMENT - Document Columns
-- =====================================================

-- Add DMS document reference column
ALTER TABLE templates ADD COLUMN IF NOT EXISTS dms_document_id VARCHAR(50);

-- Add document metadata columns
ALTER TABLE templates ADD COLUMN IF NOT EXISTS document_url VARCHAR(500);
ALTER TABLE templates ADD COLUMN IF NOT EXISTS document_original_name VARCHAR(255);
ALTER TABLE templates ADD COLUMN IF NOT EXISTS document_type VARCHAR(50);
ALTER TABLE templates ADD COLUMN IF NOT EXISTS document_size_bytes BIGINT;
ALTER TABLE templates ADD COLUMN IF NOT EXISTS has_document_variables BOOLEAN DEFAULT FALSE;

-- Add comments for documentation
COMMENT ON COLUMN templates.dms_document_id IS 'Reference to document stored in DMS service (OVH S3)';
COMMENT ON COLUMN templates.document_url IS 'Public URL to access the document from DMS';
COMMENT ON COLUMN templates.document_original_name IS 'Original filename of the uploaded document';
COMMENT ON COLUMN templates.document_type IS 'Document type: PDF, DOC, DOCX';
COMMENT ON COLUMN templates.document_size_bytes IS 'Size of the document in bytes';
COMMENT ON COLUMN templates.has_document_variables IS 'Flag indicating if document contains placeholders for variable replacement';

-- Create indexes for templates
CREATE INDEX IF NOT EXISTS idx_templates_dms_document ON templates(dms_document_id) WHERE dms_document_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_templates_has_document ON templates(document_url) WHERE document_url IS NOT NULL;

-- =====================================================
-- 2. NOTICE MANAGEMENT - Document Reference Columns
-- =====================================================

-- Add DMS document reference columns to notices
ALTER TABLE notices ADD COLUMN IF NOT EXISTS dms_document_id VARCHAR(50);
ALTER TABLE notices ADD COLUMN IF NOT EXISTS original_document_url VARCHAR(500);
ALTER TABLE notices ADD COLUMN IF NOT EXISTS processed_document_url VARCHAR(500);
ALTER TABLE notices ADD COLUMN IF NOT EXISTS document_type VARCHAR(20);
ALTER TABLE notices ADD COLUMN IF NOT EXISTS document_original_name VARCHAR(255);

-- Add comments
COMMENT ON COLUMN notices.dms_document_id IS 'Reference to document stored in DMS service (OVH S3)';
COMMENT ON COLUMN notices.original_document_url IS 'Original template document URL from DMS';
COMMENT ON COLUMN notices.processed_document_url IS 'Processed document URL with replaced placeholders';
COMMENT ON COLUMN notices.document_type IS 'Document type: PDF, DOC, DOCX';
COMMENT ON COLUMN notices.document_original_name IS 'Original filename of the document';

-- Create index for DMS document lookup
CREATE INDEX IF NOT EXISTS idx_notices_dms_document ON notices(dms_document_id) WHERE dms_document_id IS NOT NULL;

-- =====================================================
-- 3. STRATEGY ENGINE - Communication History Table
-- =====================================================

CREATE TABLE IF NOT EXISTS communication_history (
    id BIGSERIAL PRIMARY KEY,
    communication_id VARCHAR(50) UNIQUE NOT NULL,
    case_id BIGINT NOT NULL,
    execution_id BIGINT,
    strategy_id BIGINT,
    action_id BIGINT,
    channel VARCHAR(20) NOT NULL,
    template_id BIGINT,
    template_code VARCHAR(50),

    -- Recipient Information
    recipient_mobile VARCHAR(20),
    recipient_email VARCHAR(255),
    recipient_name VARCHAR(255),
    recipient_address TEXT,

    -- Content
    subject VARCHAR(500),
    content TEXT,

    -- Document Attachment (DMS Reference)
    has_document BOOLEAN DEFAULT FALSE,
    dms_document_id VARCHAR(50),
    original_document_url VARCHAR(500),
    processed_document_url VARCHAR(500),
    document_type VARCHAR(20),
    document_original_name VARCHAR(255),

    -- Status Tracking
    status VARCHAR(30) DEFAULT 'PENDING',
    provider_message_id VARCHAR(100),
    provider_response TEXT,
    failure_reason VARCHAR(500),

    -- Notice-specific fields
    notice_id BIGINT,
    notice_number VARCHAR(50),

    -- Timestamps
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    failed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT
);

-- Create indexes for communication_history
CREATE INDEX IF NOT EXISTS idx_comm_history_case ON communication_history(case_id);
CREATE INDEX IF NOT EXISTS idx_comm_history_channel ON communication_history(channel);
CREATE INDEX IF NOT EXISTS idx_comm_history_execution ON communication_history(execution_id);
CREATE INDEX IF NOT EXISTS idx_comm_history_template ON communication_history(template_id);
CREATE INDEX IF NOT EXISTS idx_comm_history_status ON communication_history(status);
CREATE INDEX IF NOT EXISTS idx_comm_history_created ON communication_history(created_at);
CREATE INDEX IF NOT EXISTS idx_comm_history_dms_doc ON communication_history(dms_document_id) WHERE dms_document_id IS NOT NULL;

-- Add comments
COMMENT ON TABLE communication_history IS 'Tracks all communications sent via strategy execution';
COMMENT ON COLUMN communication_history.channel IS 'Communication channel: SMS, WHATSAPP, EMAIL, NOTICE, IVR, VOICE_CALL';
COMMENT ON COLUMN communication_history.dms_document_id IS 'Reference to document in DMS service (OVH S3)';
COMMENT ON COLUMN communication_history.processed_document_url IS 'URL of document with placeholders replaced';
