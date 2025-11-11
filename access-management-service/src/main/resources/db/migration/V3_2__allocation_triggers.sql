-- ===================================================
-- Allocation & Reallocation Service - Triggers
-- Version: 3.2
-- Description: Triggers for allocation service tables
-- ===================================================

-- ===================================================
-- TRIGGERS FOR UPDATED_AT COLUMNS
-- Note: The function update_updated_at_column() already exists from V1_2__triggers.sql
-- ===================================================

-- Trigger for existing allocations table (extended with updated_at column)
CREATE TRIGGER update_allocations_updated_at
BEFORE UPDATE ON allocations
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Trigger for new allocation_batches table
CREATE TRIGGER update_allocation_batches_updated_at
BEFORE UPDATE ON allocation_batches
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Trigger for new allocation_rules table
CREATE TRIGGER update_allocation_rules_updated_at
BEFORE UPDATE ON allocation_rules
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Trigger for new contact_update_batches table
CREATE TRIGGER update_contact_update_batches_updated_at
BEFORE UPDATE ON contact_update_batches
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Trigger for new reallocation_jobs table
CREATE TRIGGER update_reallocation_jobs_updated_at
BEFORE UPDATE ON reallocation_jobs
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ===================================================
-- COMMENTS FOR DBA REFERENCE
-- ===================================================
COMMENT ON TRIGGER update_allocations_updated_at ON allocations IS 'Auto-updates updated_at timestamp on allocation record changes';
COMMENT ON TRIGGER update_allocation_batches_updated_at ON allocation_batches IS 'Auto-updates updated_at timestamp on batch record changes';
COMMENT ON TRIGGER update_allocation_rules_updated_at ON allocation_rules IS 'Auto-updates updated_at timestamp on rule changes';
COMMENT ON TRIGGER update_contact_update_batches_updated_at ON contact_update_batches IS 'Auto-updates updated_at timestamp on contact batch changes';
COMMENT ON TRIGGER update_reallocation_jobs_updated_at ON reallocation_jobs IS 'Auto-updates updated_at timestamp on reallocation job changes';
