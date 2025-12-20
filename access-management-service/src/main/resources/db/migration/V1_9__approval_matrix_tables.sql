-- =====================================================
-- CREDIT ENFORCEMENT PLATFORM - APPROVAL MATRIX TABLES
-- Migration V1_9: Maker-Checker Approval Workflow
-- =====================================================

-- =====================================================
-- APPROVAL MATRIX
-- Configuration for multi-level approval rules
-- =====================================================

CREATE TABLE IF NOT EXISTS approval_matrix (
    id BIGSERIAL PRIMARY KEY,
    matrix_code VARCHAR(50) UNIQUE NOT NULL,
    matrix_name VARCHAR(200) NOT NULL,
    approval_type VARCHAR(30) NOT NULL, -- WAIVER, SETTLEMENT, WRITE_OFF, REVERSAL, REFUND, PTP_OVERRIDE, SPECIAL_DISCOUNT, EXCEPTION_HANDLING, CASE_CLOSURE
    description TEXT,
    -- Amount-based Thresholds
    min_amount DECIMAL(15,2),
    max_amount DECIMAL(15,2),
    -- Percentage-based Thresholds (for waivers)
    min_percentage DECIMAL(5,2),
    max_percentage DECIMAL(5,2),
    -- Approval Hierarchy
    approval_level INTEGER DEFAULT 1,
    approver_role_id BIGINT,
    approver_role_name VARCHAR(100),
    approver_user_id BIGINT,
    approver_user_name VARCHAR(100),
    -- Escalation Settings
    escalation_hours INTEGER, -- auto-escalate after these hours
    escalation_level INTEGER,
    escalation_role_id BIGINT,
    -- Auto-approval Settings
    auto_approve_enabled BOOLEAN DEFAULT FALSE,
    auto_approve_below_amount DECIMAL(15,2),
    -- Additional Criteria (JSONB for flexibility)
    criteria JSONB,
    -- Status
    is_active BOOLEAN DEFAULT TRUE,
    priority_order INTEGER DEFAULT 0,
    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

-- =====================================================
-- APPROVAL REQUESTS
-- Individual approval requests submitted by users
-- =====================================================

CREATE TABLE IF NOT EXISTS approval_requests (
    id BIGSERIAL PRIMARY KEY,
    request_number VARCHAR(50) UNIQUE NOT NULL,
    approval_type VARCHAR(30) NOT NULL,
    approval_status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED, ESCALATED, AUTO_APPROVED, EXPIRED
    -- Entity Reference
    entity_type VARCHAR(50), -- OTS, WAIVER, SETTLEMENT, etc.
    entity_id BIGINT,
    entity_reference VARCHAR(100),
    -- Case Reference
    case_id BIGINT,
    loan_account_number VARCHAR(50),
    customer_name VARCHAR(255),
    -- Request Details
    requested_amount DECIMAL(15,2),
    requested_percentage DECIMAL(5,2),
    request_reason TEXT,
    request_details JSONB,
    -- Requester
    requested_by BIGINT,
    requested_by_name VARCHAR(100),
    requested_at TIMESTAMP,
    -- Current Approval Level
    current_level INTEGER DEFAULT 1,
    max_levels INTEGER DEFAULT 1,
    -- Current Approver
    current_approver_role_id BIGINT,
    current_approver_user_id BIGINT,
    -- Approval Details
    approved_by BIGINT,
    approved_by_name VARCHAR(100),
    approved_at TIMESTAMP,
    approved_amount DECIMAL(15,2),
    approval_remarks TEXT,
    -- Rejection Details
    rejected_by BIGINT,
    rejected_by_name VARCHAR(100),
    rejected_at TIMESTAMP,
    rejection_reason TEXT,
    -- Escalation Details
    escalated_at TIMESTAMP,
    escalation_reason VARCHAR(255),
    -- Expiry
    expires_at TIMESTAMP,
    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- APPROVAL HISTORY
-- Audit trail of all actions on approval requests
-- =====================================================

CREATE TABLE IF NOT EXISTS approval_history (
    id BIGSERIAL PRIMARY KEY,
    approval_request_id BIGINT NOT NULL REFERENCES approval_requests(id),
    action VARCHAR(30) NOT NULL, -- SUBMITTED, APPROVED, REJECTED, ESCALATED, RETURNED, EXPIRED
    action_level INTEGER,
    from_status VARCHAR(20),
    to_status VARCHAR(20),
    -- Actor Information
    actor_id BIGINT,
    actor_name VARCHAR(100),
    actor_role VARCHAR(100),
    -- Action Details
    remarks TEXT,
    approved_amount DECIMAL(15,2),
    metadata JSONB,
    -- Timestamp
    action_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- APPROVAL DELEGATION
-- Delegate approval authority to another user
-- =====================================================

CREATE TABLE IF NOT EXISTS approval_delegations (
    id BIGSERIAL PRIMARY KEY,
    delegator_id BIGINT NOT NULL,
    delegator_name VARCHAR(100),
    delegate_id BIGINT NOT NULL,
    delegate_name VARCHAR(100),
    -- Delegation Scope
    approval_type VARCHAR(30), -- NULL means all types
    max_amount DECIMAL(15,2), -- NULL means no limit
    -- Validity
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason VARCHAR(255),
    -- Status
    is_active BOOLEAN DEFAULT TRUE,
    revoked_at TIMESTAMP,
    revoked_by BIGINT,
    revoke_reason VARCHAR(255),
    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT
);

-- =====================================================
-- INDEXES
-- =====================================================

-- Approval Matrix indexes
CREATE INDEX IF NOT EXISTS idx_approval_matrix_type ON approval_matrix(approval_type);
CREATE INDEX IF NOT EXISTS idx_approval_matrix_active ON approval_matrix(is_active);
CREATE INDEX IF NOT EXISTS idx_approval_matrix_level ON approval_matrix(approval_type, approval_level);
CREATE INDEX IF NOT EXISTS idx_approval_matrix_priority ON approval_matrix(priority_order DESC);

-- Approval Request indexes
CREATE INDEX IF NOT EXISTS idx_approval_requests_status ON approval_requests(approval_status);
CREATE INDEX IF NOT EXISTS idx_approval_requests_type ON approval_requests(approval_type);
CREATE INDEX IF NOT EXISTS idx_approval_requests_case ON approval_requests(case_id);
CREATE INDEX IF NOT EXISTS idx_approval_requests_entity ON approval_requests(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_approval_requests_requester ON approval_requests(requested_by);
CREATE INDEX IF NOT EXISTS idx_approval_requests_approver_role ON approval_requests(current_approver_role_id) WHERE approval_status = 'PENDING';
CREATE INDEX IF NOT EXISTS idx_approval_requests_approver_user ON approval_requests(current_approver_user_id) WHERE approval_status = 'PENDING';
CREATE INDEX IF NOT EXISTS idx_approval_requests_expires ON approval_requests(expires_at) WHERE approval_status = 'PENDING';

-- Approval History indexes
CREATE INDEX IF NOT EXISTS idx_approval_history_request ON approval_history(approval_request_id);
CREATE INDEX IF NOT EXISTS idx_approval_history_actor ON approval_history(actor_id);
CREATE INDEX IF NOT EXISTS idx_approval_history_timestamp ON approval_history(action_timestamp DESC);

-- Approval Delegation indexes
CREATE INDEX IF NOT EXISTS idx_approval_delegations_delegator ON approval_delegations(delegator_id);
CREATE INDEX IF NOT EXISTS idx_approval_delegations_delegate ON approval_delegations(delegate_id);
CREATE INDEX IF NOT EXISTS idx_approval_delegations_active ON approval_delegations(is_active, start_date, end_date);

-- =====================================================
-- TABLE COMMENTS
-- =====================================================

COMMENT ON TABLE approval_matrix IS 'Configuration rules for multi-level approval workflow';
COMMENT ON TABLE approval_requests IS 'Individual approval requests from users';
COMMENT ON TABLE approval_history IS 'Complete audit trail of approval actions';
COMMENT ON TABLE approval_delegations IS 'Temporary delegation of approval authority';

COMMENT ON COLUMN approval_matrix.approval_type IS 'Type: WAIVER, SETTLEMENT, WRITE_OFF, REVERSAL, REFUND, PTP_OVERRIDE, SPECIAL_DISCOUNT, EXCEPTION_HANDLING, CASE_CLOSURE';
COMMENT ON COLUMN approval_requests.approval_status IS 'Status: PENDING, APPROVED, REJECTED, ESCALATED, AUTO_APPROVED, EXPIRED';
COMMENT ON COLUMN approval_history.action IS 'Action: SUBMITTED, APPROVED, REJECTED, ESCALATED, RETURNED, EXPIRED';
