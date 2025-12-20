-- =====================================================
-- Add state and city columns to users table for geography-based allocation
-- =====================================================

-- Add state column
ALTER TABLE users ADD COLUMN IF NOT EXISTS state VARCHAR(100);

-- Add city column
ALTER TABLE users ADD COLUMN IF NOT EXISTS city VARCHAR(100);

-- Create indexes for case-insensitive geography-based queries
CREATE INDEX IF NOT EXISTS idx_users_state_lower ON users (LOWER(state));
CREATE INDEX IF NOT EXISTS idx_users_city_lower ON users (LOWER(city));
CREATE INDEX IF NOT EXISTS idx_users_state_city_lower ON users (LOWER(state), LOWER(city));

-- Add comments
COMMENT ON COLUMN users.state IS 'State for geography-based allocation (e.g., Telangana, Maharashtra)';
COMMENT ON COLUMN users.city IS 'City for geography-based allocation (e.g., Hyderabad, Mumbai)';

-- =====================================================
-- Seed data: Update existing users with state and city
-- =====================================================

-- Update users with Telangana state and Hyderabad city (for users 1-5)
UPDATE users SET state = 'Telangana', city = 'Hyderabad' WHERE id IN (1) AND state IS NULL;

-- Update users with Maharashtra state and Mumbai city (for users 6-10)
UPDATE users SET state = 'Maharashtra', city = 'Mumbai' WHERE id IN (3) AND state IS NULL;

-- Set default state/city for any remaining users without geography
UPDATE users SET state = 'Telangana', city = 'Hyderabad' WHERE state IS NULL;

-- =====================================================
-- V1_21: Add reallocate_to_agent column to cases table
-- Supports unified CSV workflow: case sourcing -> allocation -> reallocation
-- REALLOCATE TO AGENT is mandatory for reallocation operations
-- =====================================================

-- Add reallocate_to_agent column to cases table
ALTER TABLE cases ADD COLUMN IF NOT EXISTS reallocate_to_agent VARCHAR(100);

-- Comment for the column
COMMENT ON COLUMN cases.reallocate_to_agent IS 'Target agent for reallocation (mandatory for reallocation workflow)';

-- Index for reallocate_to_agent queries
CREATE INDEX IF NOT EXISTS idx_cases_reallocate_to_agent ON cases(reallocate_to_agent) WHERE reallocate_to_agent IS NOT NULL;
