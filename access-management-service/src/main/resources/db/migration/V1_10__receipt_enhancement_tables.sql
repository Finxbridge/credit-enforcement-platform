-- =====================================================
-- CREDIT ENFORCEMENT PLATFORM - RECEIPT ENHANCEMENT TABLES
-- Migration V1_10: Receipt History and Status Tracking
-- =====================================================

-- =====================================================
-- ALTER RECEIPTS TABLE
-- Add new columns for enhanced receipt management
-- =====================================================

-- Add status column
ALTER TABLE receipts ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'GENERATED';

-- Add customer contact fields
ALTER TABLE receipts ADD COLUMN IF NOT EXISTS customer_email VARCHAR(255);
ALTER TABLE receipts ADD COLUMN IF NOT EXISTS customer_phone VARCHAR(20);

-- Add template reference
ALTER TABLE receipts ADD COLUMN IF NOT EXISTS template_id BIGINT;

-- Add PDF storage path
ALTER TABLE receipts ADD COLUMN IF NOT EXISTS pdf_storage_path VARCHAR(500);

-- Add generation details
ALTER TABLE receipts ADD COLUMN IF NOT EXISTS generated_by_name VARCHAR(100);

-- Add verification fields
ALTER TABLE receipts ADD COLUMN IF NOT EXISTS verified_at TIMESTAMP;
ALTER TABLE receipts ADD COLUMN IF NOT EXISTS verified_by BIGINT;
ALTER TABLE receipts ADD COLUMN IF NOT EXISTS verified_by_name VARCHAR(100);

-- Add email count
ALTER TABLE receipts ADD COLUMN IF NOT EXISTS email_count INTEGER DEFAULT 0;

-- Add SMS fields
ALTER TABLE receipts ADD COLUMN IF NOT EXISTS sms_sent_at TIMESTAMP;
ALTER TABLE receipts ADD COLUMN IF NOT EXISTS sms_sent_to VARCHAR(20);

-- Add cancellation fields
ALTER TABLE receipts ADD COLUMN IF NOT EXISTS cancelled_at TIMESTAMP;
ALTER TABLE receipts ADD COLUMN IF NOT EXISTS cancelled_by BIGINT;
ALTER TABLE receipts ADD COLUMN IF NOT EXISTS cancelled_by_name VARCHAR(100);
ALTER TABLE receipts ADD COLUMN IF NOT EXISTS cancellation_reason TEXT;

-- Add void fields
ALTER TABLE receipts ADD COLUMN IF NOT EXISTS voided_at TIMESTAMP;
ALTER TABLE receipts ADD COLUMN IF NOT EXISTS voided_by BIGINT;
ALTER TABLE receipts ADD COLUMN IF NOT EXISTS voided_by_name VARCHAR(100);
ALTER TABLE receipts ADD COLUMN IF NOT EXISTS void_reason TEXT;

-- Add remarks
ALTER TABLE receipts ADD COLUMN IF NOT EXISTS remarks TEXT;

-- Add updated_at
ALTER TABLE receipts ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

-- Update existing records with default status
UPDATE receipts SET status = 'GENERATED' WHERE status IS NULL;

-- =====================================================
-- RECEIPT HISTORY TABLE
-- Audit trail for receipt actions
-- =====================================================

CREATE TABLE IF NOT EXISTS receipt_history (
    id BIGSERIAL PRIMARY KEY,
    receipt_id BIGINT NOT NULL,
    receipt_number VARCHAR(50),
    action VARCHAR(30) NOT NULL, -- GENERATED, DOWNLOADED, EMAILED, SMS_SENT, VERIFIED, CANCELLED, VOIDED, REGENERATED
    from_status VARCHAR(20),
    to_status VARCHAR(20),
    actor_id BIGINT,
    actor_name VARCHAR(100),
    remarks TEXT,
    metadata JSONB,
    action_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- INDEXES
-- =====================================================

-- Receipt status indexes
CREATE INDEX IF NOT EXISTS idx_receipts_status ON receipts(status);
CREATE INDEX IF NOT EXISTS idx_receipts_generated_at ON receipts(generated_at);
CREATE INDEX IF NOT EXISTS idx_receipts_payment_mode ON receipts(payment_mode);
CREATE INDEX IF NOT EXISTS idx_receipts_loan_account ON receipts(loan_account_number);

-- Receipt history indexes
CREATE INDEX IF NOT EXISTS idx_receipt_history_receipt_id ON receipt_history(receipt_id);
CREATE INDEX IF NOT EXISTS idx_receipt_history_action ON receipt_history(action);
CREATE INDEX IF NOT EXISTS idx_receipt_history_actor ON receipt_history(actor_id);
CREATE INDEX IF NOT EXISTS idx_receipt_history_timestamp ON receipt_history(action_timestamp DESC);

-- =====================================================
-- TABLE COMMENTS
-- =====================================================

COMMENT ON COLUMN receipts.status IS 'Status: PENDING, GENERATED, VERIFIED, SENT, DOWNLOADED, CANCELLED, VOID';
COMMENT ON TABLE receipt_history IS 'Audit trail for all receipt actions';
COMMENT ON COLUMN receipt_history.action IS 'Action: GENERATED, DOWNLOADED, EMAILED, SMS_SENT, VERIFIED, CANCELLED, VOIDED, REGENERATED';
