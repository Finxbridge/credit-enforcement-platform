-- ===================================================
-- Allocation & Reallocation Service - Tables
-- Version: 3.0
-- Description: New tables and extensions for allocation service
-- ===================================================

-- ===================================================
-- 1. EXTEND EXISTING allocations TABLE
-- Add new columns to support allocation service features
-- ===================================================
ALTER TABLE allocations ADD COLUMN IF NOT EXISTS external_case_id VARCHAR(100);
ALTER TABLE allocations ADD COLUMN IF NOT EXISTS secondary_agent_id BIGINT;
ALTER TABLE allocations ADD COLUMN IF NOT EXISTS allocation_rule_id BIGINT;
ALTER TABLE allocations ADD COLUMN IF NOT EXISTS batch_id VARCHAR(100);
ALTER TABLE allocations ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE allocations ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

COMMENT ON COLUMN allocations.external_case_id IS 'External reference ID for the case';
COMMENT ON COLUMN allocations.secondary_agent_id IS 'Secondary agent for dual allocation';
COMMENT ON COLUMN allocations.allocation_rule_id IS 'Reference to allocation rule used';
COMMENT ON COLUMN allocations.batch_id IS 'Batch ID from bulk upload';

-- ===================================================
-- 2. EXTEND EXISTING allocation_history TABLE
-- Add new columns to support enhanced history tracking
-- ===================================================
ALTER TABLE allocation_history ADD COLUMN IF NOT EXISTS external_case_id VARCHAR(100);
ALTER TABLE allocation_history ADD COLUMN IF NOT EXISTS allocated_to_username VARCHAR(100);
ALTER TABLE allocation_history ADD COLUMN IF NOT EXISTS batch_id VARCHAR(100);
ALTER TABLE allocation_history ADD COLUMN IF NOT EXISTS created_at TIMESTAMP;
UPDATE allocation_history SET created_at = changed_at WHERE created_at IS NULL;
ALTER TABLE allocation_history ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;

COMMENT ON COLUMN allocation_history.external_case_id IS 'External reference ID for the case';
COMMENT ON COLUMN allocation_history.allocated_to_username IS 'Username of the allocated user';
COMMENT ON COLUMN allocation_history.batch_id IS 'Batch ID from bulk operation';

-- ===================================================
-- 3. EXTEND EXISTING batch_errors TABLE
-- Add new columns to support allocation error tracking
-- ===================================================
ALTER TABLE batch_errors ADD COLUMN IF NOT EXISTS error_id VARCHAR(100) UNIQUE;
ALTER TABLE batch_errors ADD COLUMN IF NOT EXISTS case_id BIGINT;
ALTER TABLE batch_errors ADD COLUMN IF NOT EXISTS module VARCHAR(50) DEFAULT 'ALLOCATION';

-- Generate error_id for existing records
UPDATE batch_errors SET error_id = 'err_' || id WHERE error_id IS NULL;

COMMENT ON COLUMN batch_errors.error_id IS 'Unique identifier for the error';
COMMENT ON COLUMN batch_errors.case_id IS 'Case ID related to the error';
COMMENT ON COLUMN batch_errors.module IS 'Module that generated the error (ALLOCATION, REALLOCATION, etc.)';

-- ===================================================
-- 4. NEW TABLE: allocation_batches
-- Track bulk allocation upload batches
-- ===================================================
CREATE TABLE IF NOT EXISTS allocation_batches (
    id BIGSERIAL PRIMARY KEY,
    batch_id VARCHAR(100) UNIQUE NOT NULL,
    total_cases INTEGER DEFAULT 0,
    successful_allocations INTEGER DEFAULT 0,
    failed_allocations INTEGER DEFAULT 0,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PROCESSING', 'COMPLETED', 'FAILED', 'PARTIALLY_COMPLETED')),
    uploaded_by BIGINT,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    file_name VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE allocation_batches IS 'Tracks bulk allocation CSV upload batches';
COMMENT ON COLUMN allocation_batches.batch_id IS 'Unique identifier for the batch (e.g., ALLOC_BATCH_20250115_001)';
COMMENT ON COLUMN allocation_batches.status IS 'Batch processing status';

-- ===================================================
-- 5. NEW TABLE: allocation_rules
-- Store allocation rules and configurations
-- ===================================================
CREATE TABLE IF NOT EXISTS allocation_rules (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    criteria JSONB NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'DRAFT')),
    priority INTEGER DEFAULT 0,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE allocation_rules IS 'Stores allocation rules and configurations for automatic case allocation';
COMMENT ON COLUMN allocation_rules.criteria IS 'Rule criteria stored as JSON (e.g., capacity, geography, bucket)';
COMMENT ON COLUMN allocation_rules.priority IS 'Rule execution priority (higher number = higher priority)';

-- ===================================================
-- 6. NEW TABLE: contact_update_batches
-- Track bulk contact update batches
-- ===================================================
CREATE TABLE IF NOT EXISTS contact_update_batches (
    id BIGSERIAL PRIMARY KEY,
    batch_id VARCHAR(100) UNIQUE NOT NULL,
    total_records INTEGER DEFAULT 0,
    successful_updates INTEGER DEFAULT 0,
    failed_updates INTEGER DEFAULT 0,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PROCESSING', 'COMPLETED', 'FAILED', 'PARTIALLY_COMPLETED')),
    uploaded_by BIGINT,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    file_name VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE contact_update_batches IS 'Tracks bulk contact information update batches';
COMMENT ON COLUMN contact_update_batches.batch_id IS 'Unique identifier for the batch (e.g., CONTACT_BATCH_20250115_001)';

-- ===================================================
-- 7. NEW TABLE: reallocation_jobs
-- Track reallocation jobs (by-agent and by-filter)
-- ===================================================
CREATE TABLE IF NOT EXISTS reallocation_jobs (
    id BIGSERIAL PRIMARY KEY,
    job_id VARCHAR(100) UNIQUE NOT NULL,
    job_type VARCHAR(50) NOT NULL CHECK (job_type IN ('BY_AGENT', 'BY_FILTER', 'BULK_UPLOAD')),
    from_user_id BIGINT,
    to_user_id BIGINT,
    filter_criteria JSONB,
    reason VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PROCESSING' CHECK (status IN ('PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED')),
    total_cases INTEGER DEFAULT 0,
    processed_cases INTEGER DEFAULT 0,
    successful_cases INTEGER DEFAULT 0,
    failed_cases INTEGER DEFAULT 0,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE reallocation_jobs IS 'Tracks reallocation jobs executed by agent or filter criteria';
COMMENT ON COLUMN reallocation_jobs.job_type IS 'Type of reallocation (BY_AGENT, BY_FILTER, BULK_UPLOAD)';
COMMENT ON COLUMN reallocation_jobs.filter_criteria IS 'Filter criteria for BY_FILTER jobs stored as JSON';
