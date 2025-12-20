-- =====================================================
-- V1_12: Export Enhancement Tables
-- DMS Service - Enhanced Export Job Management
-- =====================================================

-- =====================================================
-- 1. ALTER DOCUMENT_EXPORT_JOBS TABLE (Add missing columns if not exist)
-- =====================================================

-- Add download tracking columns
ALTER TABLE document_export_jobs
ADD COLUMN IF NOT EXISTS download_count INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS last_downloaded_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS last_downloaded_by BIGINT;

-- Add retry tracking
ALTER TABLE document_export_jobs
ADD COLUMN IF NOT EXISTS retry_count INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS max_retries INTEGER DEFAULT 3,
ADD COLUMN IF NOT EXISTS original_job_id BIGINT;

-- Add notification columns
ALTER TABLE document_export_jobs
ADD COLUMN IF NOT EXISTS notify_email VARCHAR(255),
ADD COLUMN IF NOT EXISTS notify_on_complete BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS notification_sent BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS notification_sent_at TIMESTAMP;

-- Add storage path column
ALTER TABLE document_export_jobs
ADD COLUMN IF NOT EXISTS storage_path VARCHAR(500);

-- =====================================================
-- 2. CREATE EXPORT_DOWNLOAD_HISTORY TABLE
-- =====================================================

CREATE TABLE IF NOT EXISTS export_download_history (
    id BIGSERIAL PRIMARY KEY,
    export_job_id BIGINT NOT NULL,
    job_id VARCHAR(100) NOT NULL,
    downloaded_by BIGINT NOT NULL,
    downloaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    download_method VARCHAR(50), -- DIRECT, URL, API
    file_size_bytes BIGINT,
    download_duration_ms BIGINT,
    success BOOLEAN DEFAULT TRUE,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 3. CREATE EXPORT_ERROR_LOG TABLE
-- =====================================================

CREATE TABLE IF NOT EXISTS export_error_log (
    id BIGSERIAL PRIMARY KEY,
    export_job_id BIGINT NOT NULL,
    job_id VARCHAR(100) NOT NULL,
    document_id BIGINT,
    document_name VARCHAR(255),
    error_code VARCHAR(50),
    error_message TEXT,
    error_details TEXT,
    stack_trace TEXT,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    retry_attempt INTEGER DEFAULT 0,
    resolved BOOLEAN DEFAULT FALSE,
    resolved_at TIMESTAMP,
    resolved_by BIGINT
);

-- =====================================================
-- 4. CREATE EXPORT_STATISTICS TABLE (Daily aggregations)
-- =====================================================

CREATE TABLE IF NOT EXISTS export_statistics (
    id BIGSERIAL PRIMARY KEY,
    stat_date DATE NOT NULL,
    user_id BIGINT, -- NULL for overall stats

    -- Job counts
    total_jobs INTEGER DEFAULT 0,
    completed_jobs INTEGER DEFAULT 0,
    failed_jobs INTEGER DEFAULT 0,
    cancelled_jobs INTEGER DEFAULT 0,

    -- Document counts
    total_documents INTEGER DEFAULT 0,
    exported_documents INTEGER DEFAULT 0,
    failed_documents INTEGER DEFAULT 0,

    -- Size stats
    total_export_size_bytes BIGINT DEFAULT 0,
    avg_export_size_bytes BIGINT DEFAULT 0,

    -- Time stats
    total_processing_time_seconds BIGINT DEFAULT 0,
    avg_processing_time_seconds BIGINT DEFAULT 0,

    -- Download stats
    total_downloads INTEGER DEFAULT 0,
    unique_downloads INTEGER DEFAULT 0,

    -- By format
    zip_exports INTEGER DEFAULT 0,
    pdf_merged_exports INTEGER DEFAULT 0,
    original_exports INTEGER DEFAULT 0,

    -- By type
    single_exports INTEGER DEFAULT 0,
    bulk_exports INTEGER DEFAULT 0,
    filtered_exports INTEGER DEFAULT 0,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_export_stats_date_user UNIQUE (stat_date, user_id)
);

-- =====================================================
-- 5. INDEXES FOR EXPORT_DOWNLOAD_HISTORY
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_export_download_job_id
ON export_download_history(export_job_id);

CREATE INDEX IF NOT EXISTS idx_export_download_job_id_str
ON export_download_history(job_id);

CREATE INDEX IF NOT EXISTS idx_export_download_user
ON export_download_history(downloaded_by);

CREATE INDEX IF NOT EXISTS idx_export_download_date
ON export_download_history(downloaded_at);

CREATE INDEX IF NOT EXISTS idx_export_download_date_user
ON export_download_history(downloaded_at, downloaded_by);

-- =====================================================
-- 6. INDEXES FOR EXPORT_ERROR_LOG
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_export_error_job_id
ON export_error_log(export_job_id);

CREATE INDEX IF NOT EXISTS idx_export_error_job_id_str
ON export_error_log(job_id);

CREATE INDEX IF NOT EXISTS idx_export_error_document
ON export_error_log(document_id);

CREATE INDEX IF NOT EXISTS idx_export_error_occurred
ON export_error_log(occurred_at);

CREATE INDEX IF NOT EXISTS idx_export_error_resolved
ON export_error_log(resolved);

CREATE INDEX IF NOT EXISTS idx_export_error_code
ON export_error_log(error_code);

-- =====================================================
-- 7. INDEXES FOR EXPORT_STATISTICS
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_export_stats_date
ON export_statistics(stat_date);

CREATE INDEX IF NOT EXISTS idx_export_stats_user
ON export_statistics(user_id);

CREATE INDEX IF NOT EXISTS idx_export_stats_date_range
ON export_statistics(stat_date, user_id);

-- =====================================================
-- 8. ADDITIONAL INDEXES FOR DOCUMENT_EXPORT_JOBS
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_export_job_status_created
ON document_export_jobs(job_status, created_at);

CREATE INDEX IF NOT EXISTS idx_export_job_user_status
ON document_export_jobs(created_by, job_status);

CREATE INDEX IF NOT EXISTS idx_export_job_completed_date
ON document_export_jobs(completed_at);

CREATE INDEX IF NOT EXISTS idx_export_job_format
ON document_export_jobs(export_format);

CREATE INDEX IF NOT EXISTS idx_export_job_type
ON document_export_jobs(export_type);

CREATE INDEX IF NOT EXISTS idx_export_job_expires
ON document_export_jobs(expires_at);

CREATE INDEX IF NOT EXISTS idx_export_job_original
ON document_export_jobs(original_job_id);

-- =====================================================
-- 9. FOREIGN KEY CONSTRAINTS
-- =====================================================

ALTER TABLE export_download_history
DROP CONSTRAINT IF EXISTS fk_download_export_job;

ALTER TABLE export_download_history
ADD CONSTRAINT fk_download_export_job
FOREIGN KEY (export_job_id) REFERENCES document_export_jobs(id)
ON DELETE CASCADE;

ALTER TABLE export_error_log
DROP CONSTRAINT IF EXISTS fk_error_export_job;

ALTER TABLE export_error_log
ADD CONSTRAINT fk_error_export_job
FOREIGN KEY (export_job_id) REFERENCES document_export_jobs(id)
ON DELETE CASCADE;

-- Self-referential FK for retry tracking
ALTER TABLE document_export_jobs
DROP CONSTRAINT IF EXISTS fk_original_export_job;

ALTER TABLE document_export_jobs
ADD CONSTRAINT fk_original_export_job
FOREIGN KEY (original_job_id) REFERENCES document_export_jobs(id)
ON DELETE SET NULL;

-- =====================================================
-- 10. COMMENTS
-- =====================================================

COMMENT ON TABLE export_download_history IS 'Tracks all download events for export jobs';
COMMENT ON TABLE export_error_log IS 'Detailed error logging for export failures';
COMMENT ON TABLE export_statistics IS 'Daily aggregated statistics for export operations';

COMMENT ON COLUMN document_export_jobs.download_count IS 'Number of times this export has been downloaded';
COMMENT ON COLUMN document_export_jobs.retry_count IS 'Number of retry attempts for failed jobs';
COMMENT ON COLUMN document_export_jobs.original_job_id IS 'Reference to original job if this is a retry';
COMMENT ON COLUMN document_export_jobs.notify_email IS 'Email address for completion notification';
COMMENT ON COLUMN document_export_jobs.storage_path IS 'Physical storage path of the export file';

COMMENT ON COLUMN export_download_history.download_method IS 'Method used: DIRECT, URL, or API';
COMMENT ON COLUMN export_download_history.download_duration_ms IS 'Time taken to download in milliseconds';

COMMENT ON COLUMN export_error_log.error_code IS 'Application-specific error code';
COMMENT ON COLUMN export_error_log.retry_attempt IS 'Which retry attempt this error occurred on';

COMMENT ON COLUMN export_statistics.stat_date IS 'Date for which statistics are aggregated';
COMMENT ON COLUMN export_statistics.user_id IS 'User ID for user-specific stats, NULL for global stats';
