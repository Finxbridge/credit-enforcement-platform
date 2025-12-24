-- Add agency-related columns to allocation_history table
-- These columns are used to track agency allocations and agent assignments

-- Add agency_id column
ALTER TABLE allocation_history
ADD COLUMN IF NOT EXISTS agency_id BIGINT NULL;

-- Add agency_code column
ALTER TABLE allocation_history
ADD COLUMN IF NOT EXISTS agency_code VARCHAR(50) NULL;

-- Add agency_name column
ALTER TABLE allocation_history
ADD COLUMN IF NOT EXISTS agency_name VARCHAR(255) NULL;

-- Update action_type column to accommodate new values
-- New actions: AGENCY_ALLOCATED, AGENCY_DEALLOCATED, AGENT_ASSIGNED, AGENT_REASSIGNED, AGENT_UNASSIGNED
ALTER TABLE allocation_history
ALTER COLUMN action_type TYPE VARCHAR(50);

-- Create index for agency-related queries
CREATE INDEX IF NOT EXISTS idx_allocation_history_agency_id ON allocation_history(agency_id);
CREATE INDEX IF NOT EXISTS idx_allocation_history_action_type ON allocation_history(action_type);

-- Add comment
COMMENT ON COLUMN allocation_history.agency_id IS 'ID of the agency for agency-level allocations';
COMMENT ON COLUMN allocation_history.agency_code IS 'Agency code for display purposes';
COMMENT ON COLUMN allocation_history.agency_name IS 'Agency name for display purposes';
