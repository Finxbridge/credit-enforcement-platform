-- V1_15: Add original_row_data column to batch_errors table
-- This column stores the original CSV row data as JSON for error export
-- Allows exporting failed records with all original uploaded data

-- Add column to batch_errors table if it doesn't exist
ALTER TABLE batch_errors ADD COLUMN IF NOT EXISTS original_row_data TEXT;

-- Add comment for documentation
COMMENT ON COLUMN batch_errors.original_row_data IS 'Original CSV row data stored as JSON for error export with all fields';
