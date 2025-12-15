-- =====================================================
-- CREDIT ENFORCEMENT PLATFORM - COLLECTIONS SERVICE SCHEMA UPDATES
-- Migration V1_24: Add missing columns for collections-service entities
-- =====================================================

-- =====================================================
-- REPAYMENTS TABLE
-- Add loan_account_number column
-- =====================================================

ALTER TABLE repayments
ADD COLUMN IF NOT EXISTS loan_account_number VARCHAR(50);

-- Add index for faster lookups by loan account number
CREATE INDEX IF NOT EXISTS idx_repayments_loan_account_number ON repayments(loan_account_number);

-- =====================================================
-- COMMENTS
-- =====================================================

COMMENT ON COLUMN repayments.loan_account_number IS 'Loan account number associated with the repayment';
