-- ===================================================
-- Allocation & Reallocation Service - Indexes
-- Version: 3.1
-- Description: Indexes for allocation service tables
-- ===================================================

-- ===================================================
-- INDEXES FOR EXTENDED allocations TABLE
-- ===================================================
CREATE INDEX IF NOT EXISTS idx_allocations_external_case_id ON allocations(external_case_id);
CREATE INDEX IF NOT EXISTS idx_allocations_secondary_agent ON allocations(secondary_agent_id);
CREATE INDEX IF NOT EXISTS idx_allocations_allocation_rule ON allocations(allocation_rule_id);
CREATE INDEX IF NOT EXISTS idx_allocations_batch_id ON allocations(batch_id);
CREATE INDEX IF NOT EXISTS idx_allocations_created_at ON allocations(created_at);
CREATE INDEX IF NOT EXISTS idx_allocations_updated_at ON allocations(updated_at);

-- Composite indexes for common queries
CREATE INDEX IF NOT EXISTS idx_allocations_status_batch ON allocations(allocation_status, batch_id);
CREATE INDEX IF NOT EXISTS idx_allocations_allocated_to_status ON allocations(allocated_to_id, allocation_status);

-- ===================================================
-- INDEXES FOR EXTENDED allocation_history TABLE
-- ===================================================
CREATE INDEX IF NOT EXISTS idx_allocation_history_external_case_id ON allocation_history(external_case_id);
CREATE INDEX IF NOT EXISTS idx_allocation_history_batch_id ON allocation_history(batch_id);
CREATE INDEX IF NOT EXISTS idx_allocation_history_username ON allocation_history(allocated_to_username);

-- Composite indexes for history queries
CREATE INDEX IF NOT EXISTS idx_allocation_history_case_changed_at ON allocation_history(case_id, changed_at DESC);
CREATE INDEX IF NOT EXISTS idx_allocation_history_new_owner_changed_at ON allocation_history(new_owner_id, changed_at DESC);

-- ===================================================
-- INDEXES FOR EXTENDED batch_errors TABLE
-- ===================================================
CREATE INDEX IF NOT EXISTS idx_batch_errors_error_id ON batch_errors(error_id);
CREATE INDEX IF NOT EXISTS idx_batch_errors_case_id ON batch_errors(case_id);
CREATE INDEX IF NOT EXISTS idx_batch_errors_module ON batch_errors(module);
CREATE INDEX IF NOT EXISTS idx_batch_errors_module_batch ON batch_errors(module, batch_id);

-- ===================================================
-- INDEXES FOR allocation_batches TABLE
-- ===================================================
CREATE INDEX IF NOT EXISTS idx_allocation_batches_batch_id ON allocation_batches(batch_id);
CREATE INDEX IF NOT EXISTS idx_allocation_batches_status ON allocation_batches(status);
CREATE INDEX IF NOT EXISTS idx_allocation_batches_uploaded_at ON allocation_batches(uploaded_at DESC);
CREATE INDEX IF NOT EXISTS idx_allocation_batches_uploaded_by ON allocation_batches(uploaded_by);
CREATE INDEX IF NOT EXISTS idx_allocation_batches_status_uploaded_at ON allocation_batches(status, uploaded_at DESC);

-- ===================================================
-- INDEXES FOR allocation_rules TABLE
-- ===================================================
CREATE INDEX IF NOT EXISTS idx_allocation_rules_status ON allocation_rules(status);
CREATE INDEX IF NOT EXISTS idx_allocation_rules_priority ON allocation_rules(priority DESC);
CREATE INDEX IF NOT EXISTS idx_allocation_rules_status_priority ON allocation_rules(status, priority DESC);
CREATE INDEX IF NOT EXISTS idx_allocation_rules_created_by ON allocation_rules(created_by);

-- GIN index for JSONB criteria column
CREATE INDEX IF NOT EXISTS idx_allocation_rules_criteria_gin ON allocation_rules USING GIN (criteria);

-- ===================================================
-- INDEXES FOR contact_update_batches TABLE
-- ===================================================
CREATE INDEX IF NOT EXISTS idx_contact_update_batches_batch_id ON contact_update_batches(batch_id);
CREATE INDEX IF NOT EXISTS idx_contact_update_batches_status ON contact_update_batches(status);
CREATE INDEX IF NOT EXISTS idx_contact_update_batches_uploaded_at ON contact_update_batches(uploaded_at DESC);
CREATE INDEX IF NOT EXISTS idx_contact_update_batches_uploaded_by ON contact_update_batches(uploaded_by);

-- ===================================================
-- INDEXES FOR reallocation_jobs TABLE
-- ===================================================
CREATE INDEX IF NOT EXISTS idx_reallocation_jobs_job_id ON reallocation_jobs(job_id);
CREATE INDEX IF NOT EXISTS idx_reallocation_jobs_status ON reallocation_jobs(status);
CREATE INDEX IF NOT EXISTS idx_reallocation_jobs_job_type ON reallocation_jobs(job_type);
CREATE INDEX IF NOT EXISTS idx_reallocation_jobs_from_user ON reallocation_jobs(from_user_id);
CREATE INDEX IF NOT EXISTS idx_reallocation_jobs_to_user ON reallocation_jobs(to_user_id);
CREATE INDEX IF NOT EXISTS idx_reallocation_jobs_started_at ON reallocation_jobs(started_at DESC);
CREATE INDEX IF NOT EXISTS idx_reallocation_jobs_created_by ON reallocation_jobs(created_by);

-- GIN index for JSONB filter_criteria column
CREATE INDEX IF NOT EXISTS idx_reallocation_jobs_filter_criteria_gin ON reallocation_jobs USING GIN (filter_criteria);

-- Composite indexes for common job queries
CREATE INDEX IF NOT EXISTS idx_reallocation_jobs_status_type ON reallocation_jobs(status, job_type);
CREATE INDEX IF NOT EXISTS idx_reallocation_jobs_user_status ON reallocation_jobs(to_user_id, status);

-- ===================================================
-- PERFORMANCE ANALYSIS
-- Add comments for DBA reference
-- ===================================================
COMMENT ON INDEX idx_allocations_status_batch IS 'Composite index for batch status queries';
COMMENT ON INDEX idx_allocation_rules_criteria_gin IS 'GIN index for JSONB criteria field - enables efficient JSON queries';
COMMENT ON INDEX idx_reallocation_jobs_filter_criteria_gin IS 'GIN index for JSONB filter_criteria field';
