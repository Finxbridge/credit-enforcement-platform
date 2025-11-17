
-- ===================================================
-- PLATFORM-WIDE SCHEDULER TABLE (COMMON FOR ALL SERVICES)
-- ===================================================
-- This table manages scheduled jobs across all microservices
-- Can be used for: Strategy Execution, Batch Imports, Auto-Allocation,
-- Report Generation, Data Cleanup, etc.
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

-- Indexes for efficient querying
CREATE INDEX idx_scheduled_jobs_enabled ON scheduled_jobs(is_enabled, next_run_at) WHERE is_enabled = TRUE;
CREATE INDEX idx_scheduled_jobs_service ON scheduled_jobs(service_name, job_type);
CREATE INDEX idx_scheduled_jobs_reference ON scheduled_jobs(job_reference_type, job_reference_id);
CREATE INDEX idx_scheduled_jobs_next_run ON scheduled_jobs(next_run_at) WHERE is_enabled = TRUE;