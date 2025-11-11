-- Strategy Engine Service Enhancements
-- Version 5.0 - Add missing columns and fields for Strategy Engine Service APIs

-- ============================================
-- ALTER strategies table
-- ============================================
ALTER TABLE strategies
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'DRAFT',
    ADD COLUMN IF NOT EXISTS last_run_at TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS success_count INTEGER DEFAULT 0,
    ADD COLUMN IF NOT EXISTS failure_count INTEGER DEFAULT 0,
    ADD COLUMN IF NOT EXISTS schedule_expression VARCHAR(100),
    ADD COLUMN IF NOT EXISTS event_type VARCHAR(50),
    ADD COLUMN IF NOT EXISTS updated_by BIGINT;

-- Update existing NULL values
UPDATE strategies SET status = 'ACTIVE' WHERE status IS NULL AND is_active = TRUE;
UPDATE strategies SET status = 'INACTIVE' WHERE status IS NULL AND is_active = FALSE;

-- Add comments
COMMENT ON COLUMN strategies.status IS 'Strategy status: ACTIVE, INACTIVE, DRAFT';
COMMENT ON COLUMN strategies.schedule_expression IS 'Cron expression for SCHEDULED trigger type (e.g., 0 0 9 * * ?)';
COMMENT ON COLUMN strategies.event_type IS 'Event type for EVENT_BASED triggers (e.g., PTP_BROKEN)';
COMMENT ON COLUMN strategies.last_run_at IS 'Timestamp of last execution';
COMMENT ON COLUMN strategies.success_count IS 'Total successful actions across all executions';
COMMENT ON COLUMN strategies.failure_count IS 'Total failed actions across all executions';

-- ============================================
-- ALTER strategy_actions table
-- ============================================
ALTER TABLE strategy_actions
    ADD COLUMN IF NOT EXISTS template_id BIGINT,
    ADD COLUMN IF NOT EXISTS channel VARCHAR(50),
    ADD COLUMN IF NOT EXISTS priority INTEGER DEFAULT 0;

COMMENT ON COLUMN strategy_actions.template_id IS 'Reference to communication template';
COMMENT ON COLUMN strategy_actions.channel IS 'Communication channel: SMS, EMAIL, WHATSAPP, etc.';
COMMENT ON COLUMN strategy_actions.priority IS 'Action execution priority (lower number = higher priority)';

-- ============================================
-- ALTER strategy_executions table
-- ============================================
ALTER TABLE strategy_executions
    ADD COLUMN IF NOT EXISTS execution_id VARCHAR(100) UNIQUE,
    ADD COLUMN IF NOT EXISTS strategy_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS estimated_cases_affected INTEGER,
    ADD COLUMN IF NOT EXISTS successful_actions INTEGER DEFAULT 0,
    ADD COLUMN IF NOT EXISTS failed_actions INTEGER DEFAULT 0,
    ADD COLUMN IF NOT EXISTS execution_metadata JSONB,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Generate execution_id for existing records
UPDATE strategy_executions
SET execution_id = 'exec_' || id || '_' || EXTRACT(EPOCH FROM started_at)::bigint
WHERE execution_id IS NULL;

COMMENT ON COLUMN strategy_executions.execution_id IS 'Unique execution identifier for API tracking';
COMMENT ON COLUMN strategy_executions.strategy_name IS 'Strategy name snapshot at execution time';
COMMENT ON COLUMN strategy_executions.estimated_cases_affected IS 'Estimated cases affected (from simulation)';
COMMENT ON COLUMN strategy_executions.successful_actions IS 'Count of successful actions';
COMMENT ON COLUMN strategy_executions.failed_actions IS 'Count of failed actions';
COMMENT ON COLUMN strategy_executions.execution_metadata IS 'Additional execution metadata (JSONB)';

-- ============================================
-- ALTER strategy_rules table
-- ============================================
ALTER TABLE strategy_rules
    ADD COLUMN IF NOT EXISTS field_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS operator VARCHAR(50),
    ADD COLUMN IF NOT EXISTS field_value TEXT;

COMMENT ON COLUMN strategy_rules.field_name IS 'Field name for rule evaluation (e.g., dpd, bucket)';
COMMENT ON COLUMN strategy_rules.operator IS 'Rule operator: EQUALS, GREATER_THAN, IN, etc.';
COMMENT ON COLUMN strategy_rules.field_value IS 'Field value for comparison';
COMMENT ON COLUMN strategy_rules.conditions IS 'Legacy JSONB conditions (kept for backward compatibility)';

-- ============================================
-- CREATE execution_details table
-- ============================================
CREATE TABLE IF NOT EXISTS strategy_execution_details (
    id BIGSERIAL PRIMARY KEY,
    execution_id VARCHAR(100) NOT NULL,
    case_id BIGINT,
    action_type VARCHAR(50),
    action_status VARCHAR(20),
    error_message TEXT,
    action_metadata JSONB,
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_execution_details_execution_id FOREIGN KEY (execution_id)
        REFERENCES strategy_executions(execution_id) ON DELETE CASCADE
);

CREATE INDEX idx_execution_details_execution_id ON strategy_execution_details(execution_id);
CREATE INDEX idx_execution_details_case_id ON strategy_execution_details(case_id);
CREATE INDEX idx_execution_details_action_status ON strategy_execution_details(action_status);

COMMENT ON TABLE strategy_execution_details IS 'Detailed execution logs for individual cases and actions';
COMMENT ON COLUMN strategy_execution_details.execution_id IS 'Reference to strategy_executions.execution_id';
COMMENT ON COLUMN strategy_execution_details.case_id IS 'Case ID that was processed';
COMMENT ON COLUMN strategy_execution_details.action_type IS 'Type of action executed';
COMMENT ON COLUMN strategy_execution_details.action_status IS 'SUCCESS or FAILED';
