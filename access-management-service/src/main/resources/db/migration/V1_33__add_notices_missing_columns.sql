-- =====================================================
-- V1_33: Add missing columns to notices table
-- =====================================================
-- Required by my-workflow-service Notice entity

-- Add status column (entity expects 'status' but table has 'notice_status')
ALTER TABLE notices ADD COLUMN IF NOT EXISTS status VARCHAR(30);

-- Copy existing notice_status values to status column
UPDATE notices SET status = notice_status WHERE status IS NULL AND notice_status IS NOT NULL;

-- Add sent_at column
ALTER TABLE notices ADD COLUMN IF NOT EXISTS sent_at TIMESTAMP;

-- Add indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_notices_status ON notices(status);
CREATE INDEX IF NOT EXISTS idx_notices_sent_at ON notices(sent_at);
