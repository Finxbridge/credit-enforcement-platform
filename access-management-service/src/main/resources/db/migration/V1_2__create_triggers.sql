-- ACCESS MANAGEMENT SERVICE - TRIGGERS

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_groups_updated_at
    BEFORE UPDATE ON user_groups
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_role_groups_updated_at
    BEFORE UPDATE ON role_groups
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_roles_updated_at
    BEFORE UPDATE ON roles
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_cache_config_updated_at
    BEFORE UPDATE ON cache_config
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_master_data_updated_at
    BEFORE UPDATE ON master_data
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_system_config_updated_at
    BEFORE UPDATE ON system_config
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_third_party_integration_master_updated_at
    BEFORE UPDATE ON third_party_integration_master
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ===================================================
-- TRIGGERS FOR UPDATED_AT COLUMNS
-- ===================================================

-- Customer Domain
CREATE TRIGGER update_customers_updated_at
    BEFORE UPDATE ON customers
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_loan_details_updated_at
    BEFORE UPDATE ON loan_details
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Case Domain
CREATE TRIGGER update_cases_updated_at
    BEFORE UPDATE ON cases
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_case_batches_updated_at
    BEFORE UPDATE ON case_batches
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_case_notes_updated_at
    BEFORE UPDATE ON case_notes
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Payment Domain
CREATE TRIGGER update_repayments_updated_at
    BEFORE UPDATE ON repayments
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_ots_settlements_updated_at
    BEFORE UPDATE ON ots_settlements
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ===================================================
-- TRIGGER COMMENTS
-- ===================================================

COMMENT ON FUNCTION update_updated_at_column() IS 'Auto-updates updated_at timestamp on record changes';
COMMENT ON TRIGGER update_customers_updated_at ON customers IS 'Auto-updates updated_at on customer record changes';
COMMENT ON TRIGGER update_cases_updated_at ON cases IS 'Auto-updates updated_at on case record changes';
COMMENT ON TRIGGER update_repayments_updated_at ON repayments IS 'Auto-updates updated_at on repayment record changes';

CREATE TRIGGER update_allocations_updated_at
    BEFORE UPDATE ON allocations
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_allocation_batches_updated_at
    BEFORE UPDATE ON allocation_batches
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_allocation_rules_updated_at
    BEFORE UPDATE ON allocation_rules
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_contact_update_batches_updated_at
    BEFORE UPDATE ON contact_update_batches
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_reallocation_jobs_updated_at
    BEFORE UPDATE ON reallocation_jobs
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ===================================================
-- TRIGGER COMMENTS
-- ===================================================

COMMENT ON FUNCTION update_updated_at_column() IS 'Auto-updates updated_at timestamp on record changes';
COMMENT ON TRIGGER update_allocations_updated_at ON allocations IS 'Auto-updates updated_at on allocation record changes';
COMMENT ON TRIGGER update_allocation_batches_updated_at ON allocation_batches IS 'Auto-updates updated_at on batch record changes';
COMMENT ON TRIGGER update_allocation_rules_updated_at ON allocation_rules IS 'Auto-updates updated_at on rule changes';
COMMENT ON TRIGGER update_contact_update_batches_updated_at ON contact_update_batches IS 'Auto-updates updated_at on contact batch changes';
COMMENT ON TRIGGER update_reallocation_jobs_updated_at ON reallocation_jobs IS 'Auto-updates updated_at on reallocation job changes';

CREATE TRIGGER update_communication_providers_updated_at
    BEFORE UPDATE ON communication_providers
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
