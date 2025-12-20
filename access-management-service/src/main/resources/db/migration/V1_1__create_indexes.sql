-- =====================================================
-- CREDIT ENFORCEMENT PLATFORM - CONSOLIDATED INDEXES
-- All CREATE INDEX statements in one file
-- =====================================================

-- =====================================================
-- ACCESS MANAGEMENT SERVICE INDEXES
-- =====================================================

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_user_group_id ON users(user_group_id);
CREATE INDEX idx_users_team_id ON users(team_id);

CREATE INDEX idx_user_groups_group_code ON user_groups(group_code);
CREATE INDEX idx_user_groups_group_type ON user_groups(group_type);
CREATE INDEX idx_user_groups_parent_group_id ON user_groups(parent_group_id);
CREATE INDEX idx_user_groups_is_active ON user_groups(is_active);

CREATE INDEX idx_role_groups_group_code ON role_groups(group_code);
CREATE INDEX idx_role_groups_display_order ON role_groups(display_order);

CREATE INDEX idx_roles_role_group_id ON roles(role_group_id);
CREATE INDEX idx_roles_is_active ON roles(is_active);

CREATE INDEX idx_permissions_resource ON permissions(resource);
CREATE INDEX idx_permissions_action ON permissions(action);

CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

CREATE INDEX idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission_id ON role_permissions(permission_id);

CREATE INDEX idx_session_user_id ON user_sessions(user_id);
CREATE INDEX idx_session_id ON user_sessions(session_id);
CREATE INDEX idx_session_active ON user_sessions(is_active);
CREATE INDEX idx_session_expires_at ON user_sessions(expires_at);

CREATE INDEX idx_cache_config_username ON cache_config(username);
CREATE INDEX idx_cache_config_is_active ON cache_config(is_active);

CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);

CREATE INDEX idx_otp_request_id ON otp_requests(request_id);
CREATE INDEX idx_otp_mobile ON otp_requests(mobile);
CREATE INDEX idx_otp_status ON otp_requests(status);
CREATE INDEX idx_otp_expires_at ON otp_requests(expires_at);
CREATE INDEX idx_otp_user_id ON otp_requests(user_id);
CREATE INDEX idx_otp_created_at ON otp_requests(created_at);

CREATE INDEX idx_system_config_key ON system_config(config_key);
CREATE INDEX idx_system_config_active ON system_config(is_active);
CREATE INDEX idx_system_config_category ON system_config(config_category);

CREATE INDEX idx_third_party_integration_type ON third_party_integration_master(integration_type);
CREATE INDEX idx_third_party_integration_active ON third_party_integration_master(is_active);

-- =====================================================
-- MASTER DATA SERVICE INDEXES
-- =====================================================

CREATE INDEX idx_master_data_type ON master_data(data_type);
CREATE INDEX idx_master_data_code ON master_data(code);
CREATE INDEX idx_master_data_active ON master_data(is_active);
CREATE INDEX idx_master_data_display_order ON master_data(display_order);

-- =====================================================
-- CASE SOURCING SERVICE INDEXES
-- =====================================================

-- Customer Indexes
CREATE INDEX idx_customers_customer_code ON customers(customer_code);
CREATE INDEX idx_customers_mobile ON customers(mobile_number);
CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_customers_is_active ON customers(is_active);

-- Loan Indexes
CREATE INDEX idx_loan_details_account_number ON loan_details(loan_account_number);
CREATE INDEX idx_loan_details_primary_customer ON loan_details(primary_customer_id);
CREATE INDEX idx_loan_details_dpd ON loan_details(dpd);
CREATE INDEX idx_loan_details_bucket ON loan_details(bucket);
CREATE INDEX idx_loan_details_product_code ON loan_details(product_code);
CREATE INDEX idx_loan_details_bank_code ON loan_details(bank_code);

-- Case Indexes
CREATE INDEX idx_cases_case_number ON cases(case_number);
CREATE INDEX idx_cases_external_case_id ON cases(external_case_id);
CREATE INDEX idx_cases_case_status ON cases(case_status);
CREATE INDEX idx_cases_loan_id ON cases(loan_id);
CREATE INDEX idx_cases_allocated_to_user ON cases(allocated_to_user_id);
CREATE INDEX idx_cases_import_batch_id ON cases(import_batch_id);
CREATE INDEX idx_cases_created_at ON cases(created_at);
CREATE INDEX idx_cases_status_created_at ON cases(case_status, created_at);
CREATE INDEX idx_cases_next_followup_date ON cases(next_followup_date);
CREATE INDEX idx_cases_ptp_date ON cases(ptp_date);
CREATE INDEX idx_cases_geography_code ON cases(geography_code);

CREATE INDEX idx_case_batches_batch_id ON case_batches(batch_id);
CREATE INDEX idx_case_batches_status ON case_batches(status);
CREATE INDEX idx_case_batches_source_type ON case_batches(source_type);
CREATE INDEX idx_case_batches_created_at ON case_batches(created_at);
CREATE INDEX idx_case_batches_uploaded_by ON case_batches(uploaded_by);

CREATE INDEX idx_batch_errors_batch_id ON batch_errors(batch_id);
CREATE INDEX idx_batch_errors_error_type ON batch_errors(error_type);
CREATE INDEX idx_batch_errors_external_case_id ON batch_errors(external_case_id);
CREATE INDEX idx_batch_errors_error_id ON batch_errors(error_id);
CREATE INDEX idx_batch_errors_case_id ON batch_errors(case_id);
CREATE INDEX idx_batch_errors_module ON batch_errors(module);
CREATE INDEX idx_batch_errors_module_batch ON batch_errors(module, batch_id);

-- NOTE: Indexes for case_notes, case_activities, telecalling_logs, telecalling_history,
-- receipts, payment_transactions, repayments, ots_settlements tables
-- moved to UNUSED_FUTURE_SERVICES_SCRIPTS.sql (tables not yet implemented)

-- PTP Indexes
CREATE INDEX idx_ptp_commitments_case_id ON ptp_commitments(case_id);
CREATE INDEX idx_ptp_commitments_user_id ON ptp_commitments(user_id);
CREATE INDEX idx_ptp_commitments_ptp_date ON ptp_commitments(ptp_date);
CREATE INDEX idx_ptp_commitments_ptp_status ON ptp_commitments(ptp_status);
CREATE INDEX idx_ptp_commitments_follow_up_date ON ptp_commitments(follow_up_date);
CREATE INDEX idx_ptp_commitments_created_at ON ptp_commitments(created_at);
CREATE INDEX idx_ptp_commitments_status_date ON ptp_commitments(ptp_status, ptp_date);

-- Case Status Indexes (for lifecycle status: 200=ACTIVE, 400=CLOSED)
CREATE INDEX idx_cases_status ON cases(status);
CREATE INDEX idx_cases_status_case_status ON cases(status, case_status);

-- Case Closure Indexes
CREATE INDEX idx_case_closure_case_id ON case_closure(case_id);
CREATE INDEX idx_case_closure_loan_id ON case_closure(loan_id);
CREATE INDEX idx_case_closure_action ON case_closure(action);
CREATE INDEX idx_case_closure_closed_at ON case_closure(closed_at);
CREATE INDEX idx_case_closure_closed_by ON case_closure(closed_by);
CREATE INDEX idx_case_closure_batch_id ON case_closure(batch_id);
CREATE INDEX idx_case_closure_case_action ON case_closure(case_id, action);

-- =====================================================
-- ALLOCATION REALLOCATION SERVICE INDEXES
-- =====================================================

CREATE INDEX idx_allocations_case_id ON allocations(case_id);
CREATE INDEX idx_allocations_allocated_to ON allocations(allocated_to_id, allocated_to_type);
CREATE INDEX idx_allocations_status ON allocations(allocation_status);
CREATE INDEX idx_allocations_allocated_at ON allocations(allocated_at);
CREATE INDEX idx_allocations_external_case_id ON allocations(external_case_id);
CREATE INDEX idx_allocations_secondary_agent ON allocations(secondary_agent_id);
CREATE INDEX idx_allocations_allocation_rule ON allocations(allocation_rule_id);
CREATE INDEX idx_allocations_batch_id ON allocations(batch_id);
CREATE INDEX idx_allocations_created_at ON allocations(created_at);
CREATE INDEX idx_allocations_updated_at ON allocations(updated_at);
CREATE INDEX idx_allocations_status_batch ON allocations(allocation_status, batch_id);
CREATE INDEX idx_allocations_allocated_to_status ON allocations(allocated_to_id, allocation_status);

CREATE INDEX idx_allocation_history_case_id ON allocation_history(case_id);
CREATE INDEX idx_allocation_history_external_case_id ON allocation_history(external_case_id);
CREATE INDEX idx_allocation_history_batch_id ON allocation_history(batch_id);
CREATE INDEX idx_allocation_history_username ON allocation_history(allocated_to_username);
CREATE INDEX idx_allocation_history_case_changed_at ON allocation_history(case_id, changed_at DESC);
CREATE INDEX idx_allocation_history_new_owner_changed_at ON allocation_history(new_owner_id, changed_at DESC);

CREATE INDEX idx_allocation_batches_batch_id ON allocation_batches(batch_id);
CREATE INDEX idx_allocation_batches_status ON allocation_batches(status);
CREATE INDEX idx_allocation_batches_uploaded_at ON allocation_batches(uploaded_at DESC);
CREATE INDEX idx_allocation_batches_uploaded_by ON allocation_batches(uploaded_by);
CREATE INDEX idx_allocation_batches_status_uploaded_at ON allocation_batches(status, uploaded_at DESC);

CREATE INDEX idx_allocation_rules_status ON allocation_rules(status);
CREATE INDEX idx_allocation_rules_priority ON allocation_rules(priority DESC);
CREATE INDEX idx_allocation_rules_status_priority ON allocation_rules(status, priority DESC);
CREATE INDEX idx_allocation_rules_created_by ON allocation_rules(created_by);
CREATE INDEX idx_allocation_rules_criteria_gin ON allocation_rules USING GIN (criteria);

CREATE INDEX idx_contact_update_batches_batch_id ON contact_update_batches(batch_id);
CREATE INDEX idx_contact_update_batches_status ON contact_update_batches(status);
CREATE INDEX idx_contact_update_batches_uploaded_at ON contact_update_batches(uploaded_at DESC);
CREATE INDEX idx_contact_update_batches_uploaded_by ON contact_update_batches(uploaded_by);

-- NOTE: Indexes for reallocation_jobs table moved to UNUSED_FUTURE_SERVICES_SCRIPTS.sql

-- =====================================================
-- COMMUNICATION SERVICE INDEXES
-- =====================================================

-- NOTE: Indexes for communication_providers table moved to UNUSED_FUTURE_SERVICES_SCRIPTS.sql

-- SMS Message Indexes
CREATE INDEX idx_sms_messages_mobile ON sms_messages(mobile);
CREATE INDEX idx_sms_messages_template_code ON sms_messages(template_code);
CREATE INDEX idx_sms_messages_status ON sms_messages(status);
CREATE INDEX idx_sms_messages_case_id ON sms_messages(case_id);
CREATE INDEX idx_sms_messages_user_id ON sms_messages(user_id);
CREATE INDEX idx_sms_messages_campaign_id ON sms_messages(campaign_id);
CREATE INDEX idx_sms_messages_sent_at ON sms_messages(sent_at);
CREATE INDEX idx_sms_messages_created_at ON sms_messages(created_at);
CREATE INDEX idx_sms_messages_provider_message_id ON sms_messages(provider_message_id);
CREATE INDEX idx_sms_messages_case_status ON sms_messages(case_id, status);

-- Email Message Indexes
CREATE INDEX idx_email_messages_email_to ON email_messages(email_to);
CREATE INDEX idx_email_messages_from_email ON email_messages(from_email);
CREATE INDEX idx_email_messages_template_code ON email_messages(template_code);
CREATE INDEX idx_email_messages_status ON email_messages(status);
CREATE INDEX idx_email_messages_case_id ON email_messages(case_id);
CREATE INDEX idx_email_messages_user_id ON email_messages(user_id);
CREATE INDEX idx_email_messages_campaign_id ON email_messages(campaign_id);
CREATE INDEX idx_email_messages_sent_at ON email_messages(sent_at);
CREATE INDEX idx_email_messages_created_at ON email_messages(created_at);
CREATE INDEX idx_email_messages_provider_message_id ON email_messages(provider_message_id);
CREATE INDEX idx_email_messages_case_status ON email_messages(case_id, status);

-- WhatsApp Message Indexes
CREATE INDEX idx_whatsapp_messages_mobile ON whatsapp_messages(mobile);
CREATE INDEX idx_whatsapp_messages_template_name ON whatsapp_messages(template_name);
CREATE INDEX idx_whatsapp_messages_status ON whatsapp_messages(status);
CREATE INDEX idx_whatsapp_messages_case_id ON whatsapp_messages(case_id);
CREATE INDEX idx_whatsapp_messages_user_id ON whatsapp_messages(user_id);
CREATE INDEX idx_whatsapp_messages_campaign_id ON whatsapp_messages(campaign_id);
CREATE INDEX idx_whatsapp_messages_sent_at ON whatsapp_messages(sent_at);
CREATE INDEX idx_whatsapp_messages_created_at ON whatsapp_messages(created_at);
CREATE INDEX idx_whatsapp_messages_provider_message_id ON whatsapp_messages(provider_message_id);
CREATE INDEX idx_whatsapp_messages_case_status ON whatsapp_messages(case_id, status);

-- Voice Call Log Indexes
CREATE INDEX idx_voice_call_logs_customer_mobile ON voice_call_logs(customer_mobile);
CREATE INDEX idx_voice_call_logs_caller_id ON voice_call_logs(caller_id);
CREATE INDEX idx_voice_call_logs_call_type ON voice_call_logs(call_type);
CREATE INDEX idx_voice_call_logs_call_status ON voice_call_logs(call_status);
CREATE INDEX idx_voice_call_logs_case_id ON voice_call_logs(case_id);
CREATE INDEX idx_voice_call_logs_agent_id ON voice_call_logs(agent_id);
CREATE INDEX idx_voice_call_logs_user_id ON voice_call_logs(user_id);
CREATE INDEX idx_voice_call_logs_initiated_at ON voice_call_logs(initiated_at);
CREATE INDEX idx_voice_call_logs_created_at ON voice_call_logs(created_at);
CREATE INDEX idx_voice_call_logs_provider_call_id ON voice_call_logs(provider_call_id);
CREATE INDEX idx_voice_call_logs_case_status ON voice_call_logs(case_id, call_status);

-- Dialer Call Log Indexes
CREATE INDEX idx_dialer_call_logs_customer_mobile ON dialer_call_logs(customer_mobile);
CREATE INDEX idx_dialer_call_logs_call_type ON dialer_call_logs(call_type);
CREATE INDEX idx_dialer_call_logs_call_status ON dialer_call_logs(call_status);
CREATE INDEX idx_dialer_call_logs_case_id ON dialer_call_logs(case_id);
CREATE INDEX idx_dialer_call_logs_agent_id ON dialer_call_logs(agent_id);
CREATE INDEX idx_dialer_call_logs_initiated_at ON dialer_call_logs(initiated_at);
CREATE INDEX idx_dialer_call_logs_dialer_call_id ON dialer_call_logs(dialer_call_id);

-- NOTE: Indexes for communication_webhooks table moved to UNUSED_FUTURE_SERVICES_SCRIPTS.sql

-- Payment Gateway Transaction Indexes
CREATE INDEX idx_payment_gateway_txn_case_id ON payment_gateway_transactions(case_id);
CREATE INDEX idx_payment_gateway_txn_loan_account ON payment_gateway_transactions(loan_account_number);
CREATE INDEX idx_payment_gateway_txn_status ON payment_gateway_transactions(status);
CREATE INDEX idx_payment_gateway_txn_gateway_name ON payment_gateway_transactions(gateway_name);
CREATE INDEX idx_payment_gateway_txn_gateway_order_id ON payment_gateway_transactions(gateway_order_id);
CREATE INDEX idx_payment_gateway_txn_gateway_payment_id ON payment_gateway_transactions(gateway_payment_id);
CREATE INDEX idx_payment_gateway_txn_customer_mobile ON payment_gateway_transactions(customer_mobile);
CREATE INDEX idx_payment_gateway_txn_created_at ON payment_gateway_transactions(created_at);
CREATE INDEX idx_payment_gateway_txn_case_status ON payment_gateway_transactions(case_id, status);

-- =====================================================
-- STRATEGY ENGINE SERVICE INDEXES
-- =====================================================

CREATE INDEX idx_strategies_status ON strategies(status);
CREATE INDEX idx_strategies_last_run_at ON strategies(last_run_at);
CREATE INDEX idx_strategies_priority_status ON strategies(priority DESC, status) WHERE is_active = TRUE;

CREATE INDEX idx_strategy_rules_strategy_id ON strategy_rules(strategy_id);
CREATE INDEX idx_strategy_rules_rule_order ON strategy_rules(rule_order);

CREATE INDEX idx_strategy_actions_strategy_id ON strategy_actions(strategy_id);
CREATE INDEX idx_strategy_actions_action_order ON strategy_actions(action_order);
CREATE INDEX idx_strategy_actions_template_id ON strategy_actions(template_id);

CREATE INDEX idx_strategy_executions_strategy_id ON strategy_executions(strategy_id);
CREATE INDEX idx_strategy_executions_execution_id ON strategy_executions(execution_id);
CREATE INDEX idx_strategy_executions_status ON strategy_executions(execution_status);
CREATE INDEX idx_strategy_executions_started_at ON strategy_executions(started_at DESC);

-- NOTE: Indexes for strategy_execution_details table moved to UNUSED_FUTURE_SERVICES_SCRIPTS.sql

CREATE INDEX idx_scheduled_jobs_enabled ON scheduled_jobs(is_enabled, next_run_at) WHERE is_enabled = TRUE;
CREATE INDEX idx_scheduled_jobs_service ON scheduled_jobs(service_name, job_type);
CREATE INDEX idx_scheduled_jobs_reference ON scheduled_jobs(job_reference_type, job_reference_id);
CREATE INDEX idx_scheduled_jobs_next_run ON scheduled_jobs(next_run_at) WHERE is_enabled = TRUE;

CREATE INDEX idx_filter_fields_type_active ON filter_fields(field_type, is_active, sort_order);
CREATE INDEX idx_filter_fields_code ON filter_fields(field_code);
CREATE INDEX idx_filter_fields_active ON filter_fields(is_active);
CREATE INDEX idx_filter_fields_key ON filter_fields(field_key);

CREATE INDEX idx_filter_field_options_field_active ON filter_field_options(filter_field_id, is_active, sort_order);
CREATE INDEX idx_filter_field_options_value ON filter_field_options(option_value);
CREATE INDEX idx_filter_field_options_active ON filter_field_options(is_active);

CREATE INDEX idx_cities_active ON master_cities(is_active, city_name);
CREATE INDEX idx_cities_name ON master_cities(city_name);

CREATE INDEX idx_states_active ON master_states(is_active, state_name);
CREATE INDEX idx_states_name ON master_states(state_name);
CREATE INDEX idx_states_code ON master_states(state_code) WHERE state_code IS NOT NULL;

CREATE INDEX idx_pincodes_active ON master_pincodes(is_active, pincode);
CREATE INDEX idx_pincodes_pincode ON master_pincodes(pincode);
CREATE INDEX idx_pincodes_city ON master_pincodes(city_id) WHERE city_id IS NOT NULL;
CREATE INDEX idx_pincodes_state ON master_pincodes(state_id) WHERE state_id IS NOT NULL;

-- =====================================================
-- TEMPLATE MANAGEMENT SERVICE INDEXES
-- =====================================================

-- Templates table indexes
CREATE INDEX idx_templates_code ON templates(template_code);
CREATE INDEX idx_templates_channel ON templates(channel);
CREATE INDEX idx_templates_provider ON templates(provider);
CREATE INDEX idx_templates_active ON templates(is_active);
CREATE INDEX idx_templates_created_at ON templates(created_at);
CREATE INDEX idx_templates_channel_active ON templates(channel, is_active);

-- Template Content table indexes
CREATE INDEX idx_template_content_template_id ON template_content(template_id);
CREATE INDEX idx_template_content_language ON template_content(language_code);

-- Template Variables table indexes
CREATE INDEX idx_template_variables_template_id ON template_variables(template_id);
CREATE INDEX idx_template_variables_key ON template_variables(variable_key);
CREATE INDEX idx_template_variables_order ON template_variables(display_order);

-- NOTE: Indexes for campaign_templates, campaigns, campaign_executions tables
-- moved to UNUSED_FUTURE_SERVICES_SCRIPTS.sql (tables not yet implemented)

CREATE INDEX idx_variable_definitions_key ON variable_definitions(variable_key);
CREATE INDEX idx_variable_definitions_active ON variable_definitions(is_active);
CREATE INDEX idx_variable_definitions_category ON variable_definitions(category);
CREATE INDEX idx_variable_definitions_data_type ON variable_definitions(data_type);

-- =====================================================
-- INDEX COMMENTS
-- =====================================================

COMMENT ON INDEX idx_cases_status_created_at IS 'Composite index for filtering cases by status and date';
COMMENT ON INDEX idx_batch_errors_module_batch IS 'Composite index for module-specific batch error queries';
COMMENT ON INDEX idx_allocations_status_batch IS 'Composite index for batch status queries';
COMMENT ON INDEX idx_allocation_rules_criteria_gin IS 'GIN index for JSONB criteria field';
