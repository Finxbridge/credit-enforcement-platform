-- =============================================
-- V1_18: Increase trigger_days column size in strategies table
-- Purpose: Allow storing multiple days like ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"]
-- =============================================

-- Increase trigger_days column from VARCHAR(50) to VARCHAR(255)
ALTER TABLE strategies ALTER COLUMN trigger_days TYPE VARCHAR(255);
