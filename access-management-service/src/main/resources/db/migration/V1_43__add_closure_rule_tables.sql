-- Migration: Add Closure Rules tables for cycle-closure functionality
-- Tables: closure_rules, closure_rule_executions

-- =============================================
-- Table: closure_rules
-- Stores cycle-closure rule configurations
-- =============================================
CREATE TABLE IF NOT EXISTS closure_rules (
    id BIGSERIAL PRIMARY KEY,
    rule_code VARCHAR(50) NOT NULL UNIQUE,
    rule_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    rule_type VARCHAR(50) NOT NULL, -- ZERO_OUTSTANDING, SETTLEMENT_COMPLETE, WRITE_OFF, MANUAL, INACTIVITY_BASED
    cron_expression VARCHAR(100), -- Cron expression for scheduled execution
    is_scheduled BOOLEAN DEFAULT FALSE,
    closure_reason VARCHAR(255),
    criteria TEXT, -- JSON criteria for rule evaluation
    min_zero_outstanding_days INTEGER DEFAULT 0, -- Minimum days with zero outstanding
    min_inactivity_days INTEGER DEFAULT 0, -- Minimum days of inactivity
    include_buckets VARCHAR(255), -- Comma-separated bucket codes to include
    exclude_statuses VARCHAR(255), -- Comma-separated case statuses to exclude
    is_active BOOLEAN DEFAULT TRUE,
    priority INTEGER DEFAULT 0, -- Rule execution priority (higher = first)
    last_executed_at TIMESTAMP,
    last_execution_count INTEGER DEFAULT 0,
    total_cases_closed BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

-- Indexes for closure_rules
CREATE INDEX IF NOT EXISTS idx_closure_rules_rule_code ON closure_rules(rule_code);
CREATE INDEX IF NOT EXISTS idx_closure_rules_rule_type ON closure_rules(rule_type);
CREATE INDEX IF NOT EXISTS idx_closure_rules_is_active ON closure_rules(is_active);
CREATE INDEX IF NOT EXISTS idx_closure_rules_is_scheduled ON closure_rules(is_scheduled);
CREATE INDEX IF NOT EXISTS idx_closure_rules_priority ON closure_rules(priority);

-- =============================================
-- Table: closure_rule_executions
-- Stores execution history of closure rules
-- =============================================
CREATE TABLE IF NOT EXISTS closure_rule_executions (
    id BIGSERIAL PRIMARY KEY,
    execution_id VARCHAR(50) NOT NULL UNIQUE, -- Unique execution identifier (UUID)
    rule_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL, -- PENDING, RUNNING, COMPLETED, FAILED, CANCELLED
    is_simulation BOOLEAN DEFAULT FALSE, -- True if dry-run/simulation
    total_eligible INTEGER DEFAULT 0, -- Total cases eligible for closure
    total_processed INTEGER DEFAULT 0, -- Total cases processed
    total_success INTEGER DEFAULT 0, -- Successfully closed cases
    total_failed INTEGER DEFAULT 0, -- Failed to close cases
    total_skipped INTEGER DEFAULT 0, -- Skipped cases
    error_message TEXT, -- Error message if execution failed
    execution_log TEXT, -- Detailed execution log
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    duration_ms BIGINT, -- Execution duration in milliseconds
    triggered_by VARCHAR(50), -- MANUAL, SCHEDULED, API
    executed_by BIGINT, -- User ID who triggered execution
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_closure_rule_executions_rule FOREIGN KEY (rule_id) REFERENCES closure_rules(id) ON DELETE CASCADE
);

-- Indexes for closure_rule_executions
CREATE INDEX IF NOT EXISTS idx_closure_rule_executions_execution_id ON closure_rule_executions(execution_id);
CREATE INDEX IF NOT EXISTS idx_closure_rule_executions_rule_id ON closure_rule_executions(rule_id);
CREATE INDEX IF NOT EXISTS idx_closure_rule_executions_status ON closure_rule_executions(status);
CREATE INDEX IF NOT EXISTS idx_closure_rule_executions_is_simulation ON closure_rule_executions(is_simulation);
CREATE INDEX IF NOT EXISTS idx_closure_rule_executions_started_at ON closure_rule_executions(started_at);
CREATE INDEX IF NOT EXISTS idx_closure_rule_executions_triggered_by ON closure_rule_executions(triggered_by);

-- =============================================
-- Insert default closure rules
-- =============================================
INSERT INTO closure_rules (rule_code, rule_name, description, rule_type, is_scheduled, cron_expression, closure_reason, min_zero_outstanding_days, is_active, priority, created_by)
VALUES
('ZERO_OS_30_DAYS', 'Zero Outstanding 30 Days', 'Close cases with zero outstanding for 30+ days', 'ZERO_OUTSTANDING', TRUE, '0 0 2 * * ?', 'Zero outstanding balance for 30 days', 30, TRUE, 100, 1),
('SETTLEMENT_COMPLETE', 'Settlement Complete', 'Close cases with completed settlements', 'SETTLEMENT_COMPLETE', TRUE, '0 0 3 * * ?', 'Settlement completed', 0, TRUE, 90, 1),
('WRITE_OFF_APPROVED', 'Write-Off Approved', 'Close cases approved for write-off', 'WRITE_OFF', FALSE, NULL, 'Approved for write-off', 0, TRUE, 80, 1),
('INACTIVITY_90_DAYS', 'Inactivity 90 Days', 'Close cases with no activity for 90+ days', 'INACTIVITY_BASED', TRUE, '0 0 4 * * ?', 'No activity for 90 days', 0, FALSE, 70, 1)
ON CONFLICT (rule_code) DO NOTHING;
