CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Function to auto-update strategy statistics
CREATE OR REPLACE FUNCTION update_strategy_stats()
RETURNS TRIGGER AS $$
BEGIN
    -- Update strategy statistics when execution completes
    IF NEW.execution_status = 'COMPLETED' AND (OLD.execution_status IS NULL OR OLD.execution_status != 'COMPLETED') THEN
        UPDATE strategies
        SET
            last_run_at = NEW.completed_at,
            success_count = success_count + COALESCE(NEW.successful_actions, 0),
            failure_count = failure_count + COALESCE(NEW.failed_actions, 0),
            updated_at = CURRENT_TIMESTAMP
        WHERE id = NEW.strategy_id;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Function to set strategy_name in executions
CREATE OR REPLACE FUNCTION set_execution_strategy_name()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.strategy_name IS NULL THEN
        SELECT strategy_name INTO NEW.strategy_name
        FROM strategies
        WHERE id = NEW.strategy_id;
    END IF;

    IF NEW.execution_id IS NULL THEN
        NEW.execution_id := 'exec_' || NEW.id || '_' || EXTRACT(EPOCH FROM NEW.started_at)::bigint;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ===================================================
-- TRIGGERS FOR UPDATED_AT COLUMNS
-- ===================================================

-- Access Management Domain
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_user_groups_updated_at BEFORE UPDATE ON user_groups FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_role_groups_updated_at BEFORE UPDATE ON role_groups FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_roles_updated_at BEFORE UPDATE ON roles FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Master Data Domain
CREATE TRIGGER update_master_data_updated_at BEFORE UPDATE ON master_data FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_system_config_updated_at BEFORE UPDATE ON system_config FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_third_party_integration_master_updated_at BEFORE UPDATE ON third_party_integration_master FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_cache_config_updated_at BEFORE UPDATE ON cache_config FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Communication Domain
CREATE TRIGGER update_communication_providers_updated_at BEFORE UPDATE ON communication_providers FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Customer & Loan Domain
CREATE TRIGGER update_customers_updated_at BEFORE UPDATE ON customers FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_loan_details_updated_at BEFORE UPDATE ON loan_details FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Case Domain
CREATE TRIGGER update_cases_updated_at BEFORE UPDATE ON cases FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_case_batches_updated_at BEFORE UPDATE ON case_batches FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Allocation & Reallocation Domain
CREATE TRIGGER update_allocations_updated_at BEFORE UPDATE ON allocations FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_allocation_batches_updated_at BEFORE UPDATE ON allocation_batches FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_allocation_rules_updated_at BEFORE UPDATE ON allocation_rules FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_contact_update_batches_updated_at BEFORE UPDATE ON contact_update_batches FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_reallocation_jobs_updated_at BEFORE UPDATE ON reallocation_jobs FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Strategy Engine Domain
CREATE TRIGGER update_strategies_updated_at BEFORE UPDATE ON strategies FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_strategy_rules_updated_at BEFORE UPDATE ON strategy_rules FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_strategy_actions_updated_at BEFORE UPDATE ON strategy_actions FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Campaign Domain
CREATE TRIGGER update_campaign_templates_updated_at BEFORE UPDATE ON campaign_templates FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_campaigns_updated_at BEFORE UPDATE ON campaigns FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Agency Domain
CREATE TRIGGER update_agencies_updated_at BEFORE UPDATE ON agencies FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Payment & Receipt Domain
CREATE TRIGGER update_repayments_updated_at BEFORE UPDATE ON repayments FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_ots_settlements_updated_at BEFORE UPDATE ON ots_settlements FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Notice Domain
CREATE TRIGGER update_notice_vendors_updated_at BEFORE UPDATE ON notice_vendors FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_notice_dispatch_details_updated_at BEFORE UPDATE ON notice_dispatch_details FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_notices_updated_at BEFORE UPDATE ON notices FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_notice_batch_items_updated_at BEFORE UPDATE ON notice_batch_items FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ===================================================
-- STRATEGY ENGINE SPECIFIC TRIGGERS
-- ===================================================

-- Trigger to update strategy statistics when execution completes
DROP TRIGGER IF EXISTS trigger_update_strategy_stats ON strategy_executions;
CREATE TRIGGER trigger_update_strategy_stats
    AFTER UPDATE ON strategy_executions
    FOR EACH ROW
    WHEN (NEW.execution_status = 'COMPLETED')
    EXECUTE FUNCTION update_strategy_stats();

-- Trigger to set execution strategy name and ID
DROP TRIGGER IF EXISTS trigger_set_execution_strategy_name ON strategy_executions;
CREATE TRIGGER trigger_set_execution_strategy_name
    BEFORE INSERT ON strategy_executions
    FOR EACH ROW
    EXECUTE FUNCTION set_execution_strategy_name();

-- ===================================================
-- TRIGGER COMMENTS FOR DBA REFERENCE
-- ===================================================

COMMENT ON FUNCTION update_updated_at_column() IS 'Auto-updates updated_at timestamp on record changes';
COMMENT ON FUNCTION update_strategy_stats() IS 'Auto-updates strategy statistics when execution completes';
COMMENT ON FUNCTION set_execution_strategy_name() IS 'Auto-populates strategy_name and execution_id';

-- Allocation Domain Comments
COMMENT ON TRIGGER update_allocations_updated_at ON allocations IS 'Auto-updates updated_at timestamp on allocation record changes';
COMMENT ON TRIGGER update_allocation_batches_updated_at ON allocation_batches IS 'Auto-updates updated_at timestamp on batch record changes';
COMMENT ON TRIGGER update_allocation_rules_updated_at ON allocation_rules IS 'Auto-updates updated_at timestamp on rule changes';
COMMENT ON TRIGGER update_contact_update_batches_updated_at ON contact_update_batches IS 'Auto-updates updated_at timestamp on contact batch changes';
COMMENT ON TRIGGER update_reallocation_jobs_updated_at ON reallocation_jobs IS 'Auto-updates updated_at timestamp on reallocation job changes';
