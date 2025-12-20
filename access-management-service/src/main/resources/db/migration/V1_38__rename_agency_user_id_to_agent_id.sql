-- V1_38: Rename agency_user_id to agent_id in agency_case_allocations table
-- The entity uses agent_id for consistency with the second-level allocation flow

-- Rename column from agency_user_id to agent_id
ALTER TABLE agency_case_allocations
    RENAME COLUMN agency_user_id TO agent_id;

-- Add index for agent_id if not exists
CREATE INDEX IF NOT EXISTS idx_agency_case_allocations_agent_id
    ON agency_case_allocations(agent_id);

-- Update any existing indexes that reference the old column name
DROP INDEX IF EXISTS idx_agency_case_allocations_agency_user_id;
