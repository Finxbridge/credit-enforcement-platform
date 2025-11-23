-- ACCESS MANAGEMENT SERVICE - INDEXES

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

-- MASTER DATA SERVICE - INDEXES
CREATE INDEX idx_master_data_type ON master_data(data_type);
CREATE INDEX idx_master_data_code ON master_data(code);
CREATE INDEX idx_master_data_parent_code ON master_data(parent_code);
CREATE INDEX idx_master_data_active ON master_data(is_active);
CREATE INDEX idx_master_data_display_order ON master_data(display_order);

CREATE INDEX idx_system_config_key ON system_config(config_key);
CREATE INDEX idx_system_config_active ON system_config(is_active);
CREATE INDEX idx_system_config_category ON system_config(config_category);

CREATE INDEX idx_third_party_integration_type ON third_party_integration_master(integration_type);
CREATE INDEX idx_third_party_integration_active ON third_party_integration_master(is_active);

-- ===================================================
-- CASE SOURCING SERVICE - INDEXES
-- ===================================================

-- ===================================================
-- CUSTOMER DOMAIN INDEXES
-- ===================================================

CREATE INDEX idx_customers_customer_code ON customers(customer_code);
CREATE INDEX idx_customers_mobile ON customers(mobile_number);
CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_customers_is_active ON customers(is_active);

-- ===================================================
-- LOAN DOMAIN INDEXES
-- ===================================================

CREATE INDEX idx_loan_details_account_number ON loan_details(loan_account_number);
CREATE INDEX idx_loan_details_primary_customer ON loan_details(primary_customer_id);
CREATE INDEX idx_loan_details_dpd ON loan_details(dpd);
CREATE INDEX idx_loan_details_bucket ON loan_details(bucket);
CREATE INDEX idx_loan_details_product_code ON loan_details(product_code);
CREATE INDEX idx_loan_details_bank_code ON loan_details(bank_code);

-- ===================================================
-- CASE DOMAIN INDEXES
-- ===================================================

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

CREATE INDEX idx_case_note_case_id ON case_notes(case_id);
CREATE INDEX idx_case_note_created_at ON case_notes(created_at);
CREATE INDEX idx_case_note_created_by ON case_notes(created_by);
CREATE INDEX idx_case_note_type ON case_notes(note_type);
CREATE INDEX idx_case_note_important ON case_notes(is_important);

CREATE INDEX idx_case_activities_case_id ON case_activities(case_id);
CREATE INDEX idx_case_activities_user_id ON case_activities(user_id);
CREATE INDEX idx_case_activities_activity_type ON case_activities(activity_type);
CREATE INDEX idx_case_activities_created_at ON case_activities(created_at);

-- ===================================================
-- TELECALLING DOMAIN INDEXES
-- ===================================================

CREATE INDEX idx_telecalling_logs_case_id ON telecalling_logs(case_id);
CREATE INDEX idx_telecalling_logs_user_id ON telecalling_logs(user_id);
CREATE INDEX idx_telecalling_logs_disposition ON telecalling_logs(call_disposition);
CREATE INDEX idx_telecalling_logs_call_started ON telecalling_logs(call_started_at);
CREATE INDEX idx_telecalling_logs_next_followup ON telecalling_logs(next_followup_date);
CREATE INDEX idx_telecalling_logs_created_at ON telecalling_logs(created_at);

CREATE INDEX idx_telecalling_history_case_id ON telecalling_history(case_id);
CREATE INDEX idx_telecalling_history_user_id ON telecalling_history(user_id);
CREATE INDEX idx_telecalling_history_archived_at ON telecalling_history(archived_at);

-- ===================================================
-- PAYMENT & RECEIPT DOMAIN INDEXES
-- ===================================================

CREATE INDEX idx_receipts_receipt_number ON receipts(receipt_number);
CREATE INDEX idx_receipts_receipt_type ON receipts(receipt_type);
CREATE INDEX idx_receipts_generated_by ON receipts(generated_by);
CREATE INDEX idx_receipts_is_verified ON receipts(is_verified);

CREATE INDEX idx_payment_transactions_transaction_id ON payment_transactions(transaction_id);
CREATE INDEX idx_payment_transactions_method ON payment_transactions(payment_method);
CREATE INDEX idx_payment_transactions_gateway ON payment_transactions(payment_gateway);
CREATE INDEX idx_payment_transactions_status ON payment_transactions(transaction_status);
CREATE INDEX idx_payment_transactions_date ON payment_transactions(transaction_date);

CREATE INDEX idx_repayments_case_id ON repayments(case_id);
CREATE INDEX idx_repayments_transaction_id ON repayments(transaction_id);
CREATE INDEX idx_repayments_approval_status ON repayments(approval_status);
CREATE INDEX idx_repayments_payment_date ON repayments(payment_date);
CREATE INDEX idx_repayments_deposit_sla ON repayments(deposit_sla_status);
CREATE INDEX idx_repayments_receipt_id ON repayments(receipt_id);
CREATE INDEX idx_repayments_collected_by ON repayments(collected_by);
CREATE INDEX idx_repayments_is_reconciled ON repayments(is_reconciled);

CREATE INDEX idx_ots_settlements_case_id ON ots_settlements(case_id);
CREATE INDEX idx_ots_settlements_status ON ots_settlements(ots_status);
CREATE INDEX idx_ots_settlements_requested_by ON ots_settlements(requested_by);
CREATE INDEX idx_ots_settlements_created_at ON ots_settlements(created_at);

-- ===================================================
-- PTP (Promise to Pay) DOMAIN INDEXES
-- ===================================================

CREATE INDEX idx_ptp_commitments_case_id ON ptp_commitments(case_id);
CREATE INDEX idx_ptp_commitments_user_id ON ptp_commitments(user_id);
CREATE INDEX idx_ptp_commitments_ptp_date ON ptp_commitments(ptp_date);
CREATE INDEX idx_ptp_commitments_ptp_status ON ptp_commitments(ptp_status);
CREATE INDEX idx_ptp_commitments_follow_up_date ON ptp_commitments(follow_up_date);
CREATE INDEX idx_ptp_commitments_created_at ON ptp_commitments(created_at);
CREATE INDEX idx_ptp_commitments_status_date ON ptp_commitments(ptp_status, ptp_date);

-- ===================================================
-- PERFORMANCE OPTIMIZATION COMMENTS
-- ===================================================

COMMENT ON INDEX idx_cases_status_created_at IS 'Composite index for filtering cases by status and date';
COMMENT ON INDEX idx_batch_errors_module_batch IS 'Composite index for module-specific batch error queries';

-- ===================================================
-- ALLOCATION REALLOCATION SERVICE - INDEXES
-- ===================================================

-- Allocations indexes
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

-- Allocation history indexes
CREATE INDEX idx_allocation_history_case_id ON allocation_history(case_id);
CREATE INDEX idx_allocation_history_external_case_id ON allocation_history(external_case_id);
CREATE INDEX idx_allocation_history_batch_id ON allocation_history(batch_id);
CREATE INDEX idx_allocation_history_username ON allocation_history(allocated_to_username);
CREATE INDEX idx_allocation_history_case_changed_at ON allocation_history(case_id, changed_at DESC);
CREATE INDEX idx_allocation_history_new_owner_changed_at ON allocation_history(new_owner_id, changed_at DESC);

-- Allocation batches indexes
CREATE INDEX idx_allocation_batches_batch_id ON allocation_batches(batch_id);
CREATE INDEX idx_allocation_batches_status ON allocation_batches(status);
CREATE INDEX idx_allocation_batches_uploaded_at ON allocation_batches(uploaded_at DESC);
CREATE INDEX idx_allocation_batches_uploaded_by ON allocation_batches(uploaded_by);
CREATE INDEX idx_allocation_batches_status_uploaded_at ON allocation_batches(status, uploaded_at DESC);

-- Allocation rules indexes
CREATE INDEX idx_allocation_rules_status ON allocation_rules(status);
CREATE INDEX idx_allocation_rules_priority ON allocation_rules(priority DESC);
CREATE INDEX idx_allocation_rules_status_priority ON allocation_rules(status, priority DESC);
CREATE INDEX idx_allocation_rules_created_by ON allocation_rules(created_by);
CREATE INDEX idx_allocation_rules_criteria_gin ON allocation_rules USING GIN (criteria);

-- Contact update batches indexes
CREATE INDEX idx_contact_update_batches_batch_id ON contact_update_batches(batch_id);
CREATE INDEX idx_contact_update_batches_status ON contact_update_batches(status);
CREATE INDEX idx_contact_update_batches_uploaded_at ON contact_update_batches(uploaded_at DESC);
CREATE INDEX idx_contact_update_batches_uploaded_by ON contact_update_batches(uploaded_by);

-- Reallocation jobs indexes
CREATE INDEX idx_reallocation_jobs_job_id ON reallocation_jobs(job_id);
CREATE INDEX idx_reallocation_jobs_status ON reallocation_jobs(status);
CREATE INDEX idx_reallocation_jobs_job_type ON reallocation_jobs(job_type);
CREATE INDEX idx_reallocation_jobs_from_user ON reallocation_jobs(from_user_id);
CREATE INDEX idx_reallocation_jobs_to_user ON reallocation_jobs(to_user_id);
CREATE INDEX idx_reallocation_jobs_started_at ON reallocation_jobs(started_at DESC);
CREATE INDEX idx_reallocation_jobs_created_by ON reallocation_jobs(created_by);
CREATE INDEX idx_reallocation_jobs_filter_criteria_gin ON reallocation_jobs USING GIN (filter_criteria);
CREATE INDEX idx_reallocation_jobs_status_type ON reallocation_jobs(status, job_type);
CREATE INDEX idx_reallocation_jobs_user_status ON reallocation_jobs(to_user_id, status);

-- ===================================================
-- PERFORMANCE OPTIMIZATION COMMENTS
-- ===================================================

COMMENT ON INDEX idx_allocations_status_batch IS 'Composite index for batch status queries';
COMMENT ON INDEX idx_allocation_rules_criteria_gin IS 'GIN index for JSONB criteria field - enables efficient JSON queries';
COMMENT ON INDEX idx_reallocation_jobs_filter_criteria_gin IS 'GIN index for JSONB filter_criteria field';

-- COMMUNICATION SERVICE - INDEXES
CREATE INDEX idx_provider_type ON communication_providers(provider_type);
CREATE INDEX idx_provider_active ON communication_providers(is_active);
CREATE INDEX idx_provider_priority ON communication_providers(priority);

CREATE INDEX idx_otp_request_id ON otp_requests(request_id);
CREATE INDEX idx_otp_mobile ON otp_requests(mobile);
CREATE INDEX idx_otp_status ON otp_requests(status);
CREATE INDEX idx_otp_expires_at ON otp_requests(expires_at);
CREATE INDEX idx_otp_user_id ON otp_requests(user_id);
CREATE INDEX idx_otp_created_at ON otp_requests(created_at);

CREATE INDEX idx_sms_message_id ON sms_messages(message_id);
CREATE INDEX idx_sms_mobile ON sms_messages(mobile);
CREATE INDEX idx_sms_status ON sms_messages(status);
CREATE INDEX idx_sms_campaign_id ON sms_messages(campaign_id);
CREATE INDEX idx_sms_case_id ON sms_messages(case_id);
CREATE INDEX idx_sms_created_at ON sms_messages(created_at);

CREATE INDEX idx_whatsapp_message_id ON whatsapp_messages(message_id);
CREATE INDEX idx_whatsapp_mobile ON whatsapp_messages(mobile);
CREATE INDEX idx_whatsapp_status ON whatsapp_messages(status);
CREATE INDEX idx_whatsapp_campaign_id ON whatsapp_messages(campaign_id);
CREATE INDEX idx_whatsapp_case_id ON whatsapp_messages(case_id);
CREATE INDEX idx_whatsapp_created_at ON whatsapp_messages(created_at);

CREATE INDEX idx_email_message_id ON email_messages(message_id);
CREATE INDEX idx_email_to ON email_messages(email_to);
CREATE INDEX idx_email_status ON email_messages(status);
CREATE INDEX idx_email_campaign_id ON email_messages(campaign_id);
CREATE INDEX idx_email_case_id ON email_messages(case_id);
CREATE INDEX idx_email_created_at ON email_messages(created_at);

CREATE INDEX idx_webhook_message_id ON communication_webhooks(message_id);
CREATE INDEX idx_webhook_provider ON communication_webhooks(provider);
CREATE INDEX idx_webhook_event_type ON communication_webhooks(event_type);
CREATE INDEX idx_webhook_received_at ON communication_webhooks(received_at);
CREATE INDEX idx_webhook_status ON communication_webhooks(status);

CREATE INDEX idx_dialer_call_id ON dialer_call_logs(call_id);
CREATE INDEX idx_dialer_dialer_call_id ON dialer_call_logs(dialer_call_id);
CREATE INDEX idx_dialer_agent_id ON dialer_call_logs(agent_id);
CREATE INDEX idx_dialer_case_id ON dialer_call_logs(case_id);
CREATE INDEX idx_dialer_status ON dialer_call_logs(call_status);
CREATE INDEX idx_dialer_queued_at ON dialer_call_logs(queued_at);
CREATE INDEX idx_dialer_created_at ON dialer_call_logs(created_at);

CREATE INDEX idx_payment_transaction_id ON payment_gateway_transactions(transaction_id);
CREATE INDEX idx_payment_gateway_payment_id ON payment_gateway_transactions(gateway_payment_id);
CREATE INDEX idx_payment_gateway_order_id ON payment_gateway_transactions(gateway_order_id);
CREATE INDEX idx_payment_case_id ON payment_gateway_transactions(case_id);
CREATE INDEX idx_payment_status ON payment_gateway_transactions(status);
CREATE INDEX idx_payment_reconciled ON payment_gateway_transactions(reconciled);
CREATE INDEX idx_payment_created_at ON payment_gateway_transactions(created_at);


