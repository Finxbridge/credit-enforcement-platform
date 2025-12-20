-- =====================================================
-- CREDIT ENFORCEMENT PLATFORM - NOTICE DOCUMENTS TABLE
-- Migration V1_11: Notice Document Repository
-- =====================================================

-- =====================================================
-- NOTICE DOCUMENTS TABLE
-- Stores all notice documents with metadata
-- =====================================================

CREATE TABLE IF NOT EXISTS notice_documents (
    id BIGSERIAL PRIMARY KEY,
    notice_number VARCHAR(100) UNIQUE NOT NULL,
    notice_type VARCHAR(50) NOT NULL, -- DEMAND_NOTICE, LEGAL_NOTICE, RECALL_NOTICE, etc.
    notice_status VARCHAR(30) DEFAULT 'DRAFT', -- DRAFT, PENDING_APPROVAL, APPROVED, GENERATED, DISPATCHED, DELIVERED, RETURNED, FAILED, CANCELLED, EXPIRED

    -- Case/Loan Reference
    case_id BIGINT,
    loan_account_number VARCHAR(50),
    customer_id BIGINT,
    customer_name VARCHAR(255),

    -- Financial Details
    principal_amount DECIMAL(15,2),
    total_dues DECIMAL(15,2),
    dpd INTEGER,
    bucket VARCHAR(20),

    -- Template Reference
    template_id BIGINT,
    template_name VARCHAR(200),

    -- Document Storage
    document_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500),
    file_name VARCHAR(255),
    file_type VARCHAR(50),
    file_size_bytes BIGINT,
    storage_provider VARCHAR(50),
    storage_path VARCHAR(500),
    storage_bucket VARCHAR(100),

    -- Address Information
    delivery_address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    pincode VARCHAR(10),
    region VARCHAR(50),

    -- Product Information
    product_type VARCHAR(50),
    product_name VARCHAR(100),

    -- Generation Info
    generated_at TIMESTAMP,
    generated_by BIGINT,
    generated_by_name VARCHAR(100),

    -- Approval Info
    approved_at TIMESTAMP,
    approved_by BIGINT,
    approved_by_name VARCHAR(100),

    -- Dispatch Info
    dispatched_at TIMESTAMP,
    dispatch_vendor_id BIGINT,
    dispatch_vendor_name VARCHAR(100),
    tracking_number VARCHAR(100),

    -- Delivery Info
    delivered_at TIMESTAMP,
    delivery_proof_url VARCHAR(500),
    delivery_status VARCHAR(30),

    -- Response
    response_due_date DATE,
    response_received_at TIMESTAMP,
    response_notes TEXT,

    -- Metadata
    metadata TEXT,
    tags TEXT,

    -- Versioning
    version_number INTEGER DEFAULT 1,
    parent_notice_id BIGINT,

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

-- =====================================================
-- INDEXES
-- =====================================================

-- Primary lookups
CREATE INDEX IF NOT EXISTS idx_notice_docs_number ON notice_documents(notice_number);
CREATE INDEX IF NOT EXISTS idx_notice_docs_type ON notice_documents(notice_type);
CREATE INDEX IF NOT EXISTS idx_notice_docs_status ON notice_documents(notice_status);

-- Case/Loan lookups
CREATE INDEX IF NOT EXISTS idx_notice_docs_case ON notice_documents(case_id);
CREATE INDEX IF NOT EXISTS idx_notice_docs_loan ON notice_documents(loan_account_number);
CREATE INDEX IF NOT EXISTS idx_notice_docs_customer ON notice_documents(customer_id);

-- Financial filters
CREATE INDEX IF NOT EXISTS idx_notice_docs_dpd ON notice_documents(dpd);
CREATE INDEX IF NOT EXISTS idx_notice_docs_dues ON notice_documents(total_dues);
CREATE INDEX IF NOT EXISTS idx_notice_docs_bucket ON notice_documents(bucket);

-- Location filters
CREATE INDEX IF NOT EXISTS idx_notice_docs_region ON notice_documents(region);
CREATE INDEX IF NOT EXISTS idx_notice_docs_state ON notice_documents(state);
CREATE INDEX IF NOT EXISTS idx_notice_docs_pincode ON notice_documents(pincode);

-- Product filters
CREATE INDEX IF NOT EXISTS idx_notice_docs_product ON notice_documents(product_type);

-- Date filters
CREATE INDEX IF NOT EXISTS idx_notice_docs_generated ON notice_documents(generated_at);
CREATE INDEX IF NOT EXISTS idx_notice_docs_dispatched ON notice_documents(dispatched_at);
CREATE INDEX IF NOT EXISTS idx_notice_docs_delivered ON notice_documents(delivered_at);

-- Vendor filter
CREATE INDEX IF NOT EXISTS idx_notice_docs_vendor ON notice_documents(dispatch_vendor_id);

-- Response tracking
CREATE INDEX IF NOT EXISTS idx_notice_docs_response_due ON notice_documents(response_due_date) WHERE response_received_at IS NULL;

-- Versioning
CREATE INDEX IF NOT EXISTS idx_notice_docs_parent ON notice_documents(parent_notice_id);

-- Composite indexes for common queries
CREATE INDEX IF NOT EXISTS idx_notice_docs_type_status ON notice_documents(notice_type, notice_status);
CREATE INDEX IF NOT EXISTS idx_notice_docs_region_status ON notice_documents(region, notice_status);
CREATE INDEX IF NOT EXISTS idx_notice_docs_dpd_status ON notice_documents(dpd, notice_status);

-- =====================================================
-- DMS AUDIT LOGS TABLE
-- =====================================================

CREATE TABLE IF NOT EXISTS dms_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    service_name VARCHAR(50) NOT NULL DEFAULT 'dms-service',
    event_type VARCHAR(50) NOT NULL,
    event_category VARCHAR(30),
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(50),
    entity_name VARCHAR(255),
    action VARCHAR(30) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    change_summary TEXT,
    actor_id BIGINT,
    actor_name VARCHAR(100),
    actor_type VARCHAR(30),
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    trace_id VARCHAR(50),
    severity VARCHAR(20),
    event_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Audit log indexes
CREATE INDEX IF NOT EXISTS idx_dms_audit_entity ON dms_audit_logs(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_dms_audit_actor ON dms_audit_logs(actor_id);
CREATE INDEX IF NOT EXISTS idx_dms_audit_timestamp ON dms_audit_logs(event_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_dms_audit_event ON dms_audit_logs(event_type);

-- =====================================================
-- TABLE COMMENTS
-- =====================================================

COMMENT ON TABLE notice_documents IS 'Repository of all notice documents with metadata and tracking';
COMMENT ON COLUMN notice_documents.notice_type IS 'Type: DEMAND_NOTICE, LEGAL_NOTICE, RECALL_NOTICE, SETTLEMENT_OFFER, etc.';
COMMENT ON COLUMN notice_documents.notice_status IS 'Status: DRAFT, PENDING_APPROVAL, APPROVED, GENERATED, DISPATCHED, DELIVERED, RETURNED, FAILED, CANCELLED, EXPIRED';
COMMENT ON TABLE dms_audit_logs IS 'Audit trail for DMS service operations';
