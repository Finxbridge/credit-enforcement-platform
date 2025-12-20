-- ===================================================
-- V1_37: Add agency_id to users table and AGENT role
-- Purpose:
-- 1. Add agency_id field to users (mandatory for AGENT role)
-- 2. Add AGENT role for external agency agents
-- 3. Add case_events table for end-to-end event tracking
-- ===================================================

-- =============================================
-- 1. Add agency_id column to users table
-- =============================================
ALTER TABLE users
ADD COLUMN IF NOT EXISTS agency_id BIGINT REFERENCES agencies(id);

-- Add index for agency_id
CREATE INDEX IF NOT EXISTS idx_users_agency_id ON users(agency_id);

-- =============================================
-- 2. Add AGENT role for external agency agents
-- =============================================
-- Add Agency Agents role group if not exists
INSERT INTO role_groups (group_name, group_code, description, display_order, is_active)
VALUES ('Agency Agents', 'GRP_AGENCY_AGENTS', 'Roles for external agency agents', 4, TRUE)
ON CONFLICT (group_code) DO NOTHING;

-- Add AGENT role (for external agency agents - requires agency_id)
INSERT INTO roles (role_name, role_code, role_group_id, description, is_active)
VALUES ('Agent', 'AGENT',
    (SELECT id FROM role_groups WHERE group_code = 'GRP_AGENCY_AGENTS'),
    'External agency agent - requires agency assignment', TRUE)
ON CONFLICT (role_code) DO NOTHING;

-- Add SUPERVISOR role for team supervisors
INSERT INTO roles (role_name, role_code, role_group_id, description, is_active)
VALUES ('Supervisor', 'SUPERVISOR',
    (SELECT id FROM role_groups WHERE group_code = 'GRP_COLL_MGMT'),
    'Team supervisor - can manage team cases and approvals', TRUE)
ON CONFLICT (role_code) DO NOTHING;

-- Add TELECALLER role for call center agents
INSERT INTO roles (role_name, role_code, role_group_id, description, is_active)
VALUES ('Telecaller', 'TELECALLER',
    (SELECT id FROM role_groups WHERE group_code = 'GRP_COLL_AGENCY'),
    'Telecaller for outbound calling', TRUE)
ON CONFLICT (role_code) DO NOTHING;

-- =============================================
-- 3. Add case_events table for end-to-end tracking
-- =============================================
CREATE TABLE IF NOT EXISTS case_events (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(100) UNIQUE NOT NULL,
    case_id BIGINT NOT NULL,
    loan_account_number VARCHAR(50),

    -- Event Classification
    event_type VARCHAR(50) NOT NULL,
    event_subtype VARCHAR(50),
    event_category VARCHAR(30) NOT NULL, -- ALLOCATION, COMMUNICATION, COLLECTION, WORKFLOW, SYSTEM

    -- Event Details
    event_title VARCHAR(255),
    event_description TEXT,
    event_data JSONB,

    -- Actor Information
    actor_id BIGINT,
    actor_name VARCHAR(100),
    actor_type VARCHAR(30), -- USER, SYSTEM, STRATEGY, AGENCY

    -- Source Service
    source_service VARCHAR(50), -- case-sourcing, allocation, communication, workflow, etc.

    -- Related Entities
    related_entity_type VARCHAR(50),
    related_entity_id BIGINT,

    -- Communication specific
    communication_channel VARCHAR(30), -- SMS, EMAIL, WHATSAPP, CALL
    communication_status VARCHAR(30),
    communication_id BIGINT,

    -- Allocation specific
    from_agent_id BIGINT,
    to_agent_id BIGINT,

    -- PTP specific
    ptp_amount DECIMAL(15,2),
    ptp_date DATE,
    ptp_status VARCHAR(30),

    -- Payment specific
    payment_amount DECIMAL(15,2),
    payment_mode VARCHAR(30),
    receipt_number VARCHAR(50),

    -- Status Change
    old_status VARCHAR(50),
    new_status VARCHAR(50),

    -- Timestamps
    event_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Metadata
    metadata JSONB,
    ip_address VARCHAR(50),
    user_agent VARCHAR(255)
);

-- Indexes for case_events
CREATE INDEX IF NOT EXISTS idx_case_events_case_id ON case_events(case_id);
CREATE INDEX IF NOT EXISTS idx_case_events_loan_account ON case_events(loan_account_number);
CREATE INDEX IF NOT EXISTS idx_case_events_type ON case_events(event_type);
CREATE INDEX IF NOT EXISTS idx_case_events_category ON case_events(event_category);
CREATE INDEX IF NOT EXISTS idx_case_events_actor ON case_events(actor_id);
CREATE INDEX IF NOT EXISTS idx_case_events_timestamp ON case_events(event_timestamp);
CREATE INDEX IF NOT EXISTS idx_case_events_source ON case_events(source_service);

-- =============================================
-- 4. Create view for role-based allocation eligibility
-- =============================================
CREATE OR REPLACE VIEW allocation_eligible_users AS
SELECT u.id, u.username, u.first_name, u.last_name, u.email, u.mobile_number,
       u.state, u.city, u.max_case_capacity, u.current_case_count, u.status,
       u.agency_id,
       array_agg(r.role_code) as role_codes
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE u.status = 'ACTIVE'
  AND r.role_code IN ('COLL_AGENT', 'TELECALLER', 'SUPERVISOR') -- First-level allocation roles
  AND r.role_code NOT IN ('AGENT', 'ADMIN', 'SUPER_ADMIN', 'COLL_MANAGER') -- Exclude these
  AND u.agency_id IS NULL -- Internal users only (not agency agents)
GROUP BY u.id, u.username, u.first_name, u.last_name, u.email, u.mobile_number,
         u.state, u.city, u.max_case_capacity, u.current_case_count, u.status, u.agency_id;

-- =============================================
-- 5. Create view for agency agents
-- =============================================
CREATE OR REPLACE VIEW agency_agents_view AS
SELECT u.id, u.username, u.first_name, u.last_name, u.email, u.mobile_number,
       u.state, u.city, u.max_case_capacity, u.current_case_count, u.status,
       u.agency_id, a.agency_name, a.agency_code
FROM users u
JOIN agencies a ON u.agency_id = a.id
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE u.status = 'ACTIVE'
  AND r.role_code = 'AGENT'
  AND u.agency_id IS NOT NULL;

-- =============================================
-- Comments
-- =============================================
COMMENT ON COLUMN users.agency_id IS 'Required when user has AGENT role. References agencies table.';
COMMENT ON TABLE case_events IS 'Centralized event tracking for all case-related activities across services';
COMMENT ON VIEW allocation_eligible_users IS 'Users eligible for first-level case allocation (internal collectors)';
COMMENT ON VIEW agency_agents_view IS 'External agency agents with their agency details';
