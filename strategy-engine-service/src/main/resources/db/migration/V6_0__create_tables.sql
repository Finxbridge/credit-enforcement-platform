-- ===================================================
-- STRATEGY ENGINE SERVICE - TABLE CREATION
-- Domain: Strategies, Rules, Actions, Executions
-- ===================================================

CREATE TABLE strategies (
    id BIGSERIAL PRIMARY KEY,
    strategy_code VARCHAR(50) UNIQUE NOT NULL,
    strategy_name VARCHAR(255) NOT NULL,
    strategy_type VARCHAR(20) NOT NULL,
    description TEXT,
    trigger_frequency VARCHAR(20),
    trigger_time TIME NULL,
    trigger_days VARCHAR(50) NULL,
    is_active BOOLEAN DEFAULT TRUE,
    priority INTEGER DEFAULT 0,
    effective_from DATE,
    effective_to DATE,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'DRAFT',
    last_run_at TIMESTAMP NULL,
    success_count INTEGER DEFAULT 0,
    failure_count INTEGER DEFAULT 0,
    schedule_expression VARCHAR(100),
    event_type VARCHAR(50),
    updated_by BIGINT
);

CREATE TABLE strategy_rules (
    id BIGSERIAL PRIMARY KEY,
    strategy_id BIGINT NOT NULL,
    rule_name VARCHAR(255) NOT NULL,
    rule_order INTEGER DEFAULT 0,
    conditions JSONB NOT NULL,
    logical_operator VARCHAR(5) DEFAULT 'AND',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    field_name VARCHAR(100),
    operator VARCHAR(50),
    field_value TEXT
);

CREATE TABLE strategy_actions (
    id BIGSERIAL PRIMARY KEY,
    strategy_id BIGINT NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    action_order INTEGER DEFAULT 0,
    action_config JSONB NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    template_id BIGINT,
    channel VARCHAR(50),
    priority INTEGER DEFAULT 0
);

CREATE TABLE strategy_executions (
    id BIGSERIAL PRIMARY KEY,
    strategy_id BIGINT NOT NULL,
    execution_type VARCHAR(20),
    total_records_evaluated INTEGER DEFAULT 0,
    records_matched INTEGER DEFAULT 0,
    records_processed INTEGER DEFAULT 0,
    records_failed INTEGER DEFAULT 0,
    execution_status VARCHAR(20),
    error_message TEXT,
    execution_log JSONB,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    executed_by BIGINT,
    execution_id VARCHAR(100) UNIQUE,
    strategy_name VARCHAR(255),
    estimated_cases_affected INTEGER,
    successful_actions INTEGER DEFAULT 0,
    failed_actions INTEGER DEFAULT 0,
    execution_metadata JSONB,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE strategy_execution_details (
    id BIGSERIAL PRIMARY KEY,
    execution_id VARCHAR(100) NOT NULL,
    case_id BIGINT,
    action_type VARCHAR(50),
    action_status VARCHAR(20),
    error_message TEXT,
    action_metadata JSONB,
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE IF NOT EXISTS scheduled_jobs (
    id BIGSERIAL PRIMARY KEY,
    service_name VARCHAR(100) NOT NULL,           -- e.g., 'strategy-engine-service', 'case-sourcing-service'
    job_name VARCHAR(255) NOT NULL,               -- e.g., 'Collection Reminder Strategy', 'Daily Case Import'
    job_type VARCHAR(50) NOT NULL,                -- e.g., 'STRATEGY_EXECUTION', 'BATCH_IMPORT', 'AUTO_ALLOCATION'
    job_reference_id BIGINT NULL,                 -- Reference to entity (e.g., strategy_id, batch_template_id)
    job_reference_type VARCHAR(50) NULL,          -- Type of reference (e.g., 'STRATEGY', 'BATCH_TEMPLATE')
    is_enabled BOOLEAN DEFAULT FALSE,             -- Enable/Disable scheduler
    schedule_type VARCHAR(20) NOT NULL,           -- 'DAILY', 'WEEKLY', 'MONTHLY', 'CRON', 'EVENT_BASED'
    schedule_time TIME NULL,                      -- Time to run (for DAILY/WEEKLY)
    schedule_days VARCHAR(100) NULL,              -- Comma-separated days (e.g., 'MONDAY,WEDNESDAY,FRIDAY')
    cron_expression VARCHAR(100) NULL,            -- Custom cron expression (for CRON type)
    timezone VARCHAR(50) DEFAULT 'Asia/Kolkata', -- Timezone for scheduling
    next_run_at TIMESTAMP NULL,                   -- Calculated next execution time
    last_run_at TIMESTAMP NULL,                   -- Last execution timestamp
    last_run_status VARCHAR(20) NULL,             -- 'SUCCESS', 'FAILED', 'RUNNING', 'SKIPPED'
    last_run_message TEXT NULL,                   -- Last run result message or error
    run_count INTEGER DEFAULT 0,                  -- Total successful runs
    failure_count INTEGER DEFAULT 0,              -- Total failed runs
    avg_execution_time_ms BIGINT NULL,            -- Average execution time in milliseconds
    job_config JSONB NULL,                        -- Additional job-specific configuration
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);


COMMENT ON TABLE strategies IS 'Strategy definitions and configurations';
COMMENT ON TABLE strategy_rules IS 'Rules and conditions for strategy evaluation';
COMMENT ON TABLE strategy_actions IS 'Actions to be executed when strategy rules match';
COMMENT ON TABLE strategy_executions IS 'Strategy execution history and statistics';
COMMENT ON TABLE strategy_execution_details IS 'Detailed execution logs for individual cases';
