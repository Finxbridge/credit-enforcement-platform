-- =============================================
-- CONSOLIDATED TRIGGERS MIGRATION
-- All trigger functions and triggers for all services
-- =============================================

-- =============================================
-- TRIGGER FUNCTION: Auto-update updated_at timestamp
-- =============================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- =============================================
-- ACCESS MANAGEMENT SERVICE TRIGGERS
-- =============================================

-- Users table
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- User groups table
CREATE TRIGGER update_user_groups_updated_at
    BEFORE UPDATE ON user_groups
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Role groups table
CREATE TRIGGER update_role_groups_updated_at
    BEFORE UPDATE ON role_groups
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Roles table
CREATE TRIGGER update_roles_updated_at
    BEFORE UPDATE ON roles
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- System config table
CREATE TRIGGER update_system_config_updated_at
    BEFORE UPDATE ON system_config
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Cache config table
CREATE TRIGGER update_cache_config_updated_at
    BEFORE UPDATE ON cache_config
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Third-party integration master table
CREATE TRIGGER update_third_party_integration_master_updated_at
    BEFORE UPDATE ON third_party_integration_master
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- MASTER DATA SERVICE TRIGGERS
-- =============================================

-- Master data table
CREATE TRIGGER update_master_data_updated_at
    BEFORE UPDATE ON master_data
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Master cities table
CREATE TRIGGER update_master_cities_updated_at
    BEFORE UPDATE ON master_cities
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Master states table
CREATE TRIGGER update_master_states_updated_at
    BEFORE UPDATE ON master_states
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Master pincodes table
CREATE TRIGGER update_master_pincodes_updated_at
    BEFORE UPDATE ON master_pincodes
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- CASE SOURCING SERVICE TRIGGERS
-- =============================================

-- Customers table
CREATE TRIGGER update_customers_updated_at
    BEFORE UPDATE ON customers
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Loan details table
CREATE TRIGGER update_loan_details_updated_at
    BEFORE UPDATE ON loan_details
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Cases table
CREATE TRIGGER update_cases_updated_at
    BEFORE UPDATE ON cases
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Case batches table
CREATE TRIGGER update_case_batches_updated_at
    BEFORE UPDATE ON case_batches
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- NOTE: Triggers for case_notes, case_activities, telecalling_logs, telecalling_history,
-- receipts, payment_transactions, repayments, ots_settlements tables
-- moved to UNUSED_FUTURE_SERVICES_SCRIPTS.sql (tables not yet implemented)

-- PTP commitments table
CREATE TRIGGER update_ptp_commitments_updated_at
    BEFORE UPDATE ON ptp_commitments
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- ALLOCATION SERVICE TRIGGERS
-- =============================================

-- Allocations table
CREATE TRIGGER update_allocations_updated_at
    BEFORE UPDATE ON allocations
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Allocation history table
CREATE TRIGGER update_allocation_history_updated_at
    BEFORE UPDATE ON allocation_history
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Allocation batches table
CREATE TRIGGER update_allocation_batches_updated_at
    BEFORE UPDATE ON allocation_batches
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Allocation rules table
CREATE TRIGGER update_allocation_rules_updated_at
    BEFORE UPDATE ON allocation_rules
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- NOTE: Trigger for reallocation_jobs table moved to UNUSED_FUTURE_SERVICES_SCRIPTS.sql

-- Contact update batches table
CREATE TRIGGER update_contact_update_batches_updated_at
    BEFORE UPDATE ON contact_update_batches
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- COMMUNICATION SERVICE TRIGGERS
-- =============================================

-- NOTE: Trigger for communication_providers table moved to UNUSED_FUTURE_SERVICES_SCRIPTS.sql

-- SMS messages table
CREATE TRIGGER update_sms_messages_updated_at
    BEFORE UPDATE ON sms_messages
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Email messages table
CREATE TRIGGER update_email_messages_updated_at
    BEFORE UPDATE ON email_messages
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- WhatsApp messages table
CREATE TRIGGER update_whatsapp_messages_updated_at
    BEFORE UPDATE ON whatsapp_messages
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Voice call logs table
CREATE TRIGGER update_voice_call_logs_updated_at
    BEFORE UPDATE ON voice_call_logs
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Dialer call logs table
CREATE TRIGGER update_dialer_call_logs_updated_at
    BEFORE UPDATE ON dialer_call_logs
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- NOTE: Trigger for communication_webhooks table moved to UNUSED_FUTURE_SERVICES_SCRIPTS.sql

-- Payment gateway transactions table
CREATE TRIGGER update_payment_gateway_transactions_updated_at
    BEFORE UPDATE ON payment_gateway_transactions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- OTP requests table
CREATE TRIGGER update_otp_requests_updated_at
    BEFORE UPDATE ON otp_requests
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- STRATEGY ENGINE SERVICE TRIGGERS
-- =============================================

-- Strategies table
CREATE TRIGGER update_strategies_updated_at
    BEFORE UPDATE ON strategies
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Strategy rules table
CREATE TRIGGER update_strategy_rules_updated_at
    BEFORE UPDATE ON strategy_rules
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Strategy actions table
CREATE TRIGGER update_strategy_actions_updated_at
    BEFORE UPDATE ON strategy_actions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Strategy executions table
CREATE TRIGGER update_strategy_executions_updated_at
    BEFORE UPDATE ON strategy_executions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- NOTE: Trigger for strategy_execution_details table moved to UNUSED_FUTURE_SERVICES_SCRIPTS.sql

-- Scheduled jobs table
CREATE TRIGGER update_scheduled_jobs_updated_at
    BEFORE UPDATE ON scheduled_jobs
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Filter fields table
CREATE TRIGGER update_filter_fields_updated_at
    BEFORE UPDATE ON filter_fields
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Filter field options table
CREATE TRIGGER update_filter_field_options_updated_at
    BEFORE UPDATE ON filter_field_options
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- TEMPLATE MANAGEMENT SERVICE TRIGGERS
-- =============================================

-- Templates table
CREATE TRIGGER update_templates_updated_at
    BEFORE UPDATE ON templates
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Template content table
CREATE TRIGGER update_template_content_updated_at
    BEFORE UPDATE ON template_content
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- NOTE: Triggers for campaign_templates, campaigns, campaign_executions tables
-- moved to UNUSED_FUTURE_SERVICES_SCRIPTS.sql (tables not yet implemented)

-- Variable definitions table
CREATE TRIGGER update_variable_definitions_updated_at
    BEFORE UPDATE ON variable_definitions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
