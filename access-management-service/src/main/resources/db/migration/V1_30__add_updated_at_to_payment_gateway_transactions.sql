-- =============================================
-- Migration: Add updated_at column to payment_gateway_transactions
-- =============================================
-- The table already has a trigger (update_payment_gateway_transactions_updated_at)
-- created in V1_2__create_triggers.sql that expects this column.
-- This migration adds the missing column.
-- =============================================

-- Add updated_at column to payment_gateway_transactions table
ALTER TABLE payment_gateway_transactions
ADD COLUMN updated_at TIMESTAMP;

-- Set initial value for existing records (use created_at as default)
UPDATE payment_gateway_transactions
SET updated_at = created_at
WHERE updated_at IS NULL;
