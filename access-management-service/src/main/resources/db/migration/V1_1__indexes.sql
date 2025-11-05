CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_user_group_id ON users(user_group_id);

CREATE INDEX idx_user_groups_group_code ON user_groups(group_code);
CREATE INDEX idx_user_groups_group_type ON user_groups(group_type);
CREATE INDEX idx_user_groups_parent_group_id ON user_groups(parent_group_id);
CREATE INDEX idx_user_groups_is_active ON user_groups(is_active);

CREATE INDEX idx_role_groups_group_code ON role_groups(group_code);
CREATE INDEX idx_role_groups_display_order ON role_groups(display_order);

CREATE INDEX idx_roles_role_group_id ON roles(role_group_id);

CREATE INDEX IF NOT EXISTS idx_master_data_type ON master_data(data_type);
CREATE INDEX IF NOT EXISTS idx_master_data_code ON master_data(code);
CREATE INDEX IF NOT EXISTS idx_master_data_parent_code ON master_data(parent_code);
CREATE INDEX IF NOT EXISTS idx_master_data_active ON master_data(is_active);
CREATE INDEX IF NOT EXISTS idx_master_data_display_order ON master_data(display_order);

CREATE INDEX IF NOT EXISTS idx_customers_customer_code ON customers(customer_code);
CREATE INDEX IF NOT EXISTS idx_customers_mobile ON customers(mobile_number);
CREATE INDEX IF NOT EXISTS idx_customers_email ON customers(email);

CREATE INDEX IF NOT EXISTS idx_loan_details_account_number ON loan_details(loan_account_number);
CREATE INDEX IF NOT EXISTS idx_loan_details_primary_customer ON loan_details(primary_customer_id);
CREATE INDEX IF NOT EXISTS idx_loan_details_dpd ON loan_details(dpd);
CREATE INDEX IF NOT EXISTS idx_loan_details_bucket ON loan_details(bucket);
CREATE INDEX IF NOT EXISTS idx_loan_details_product_code ON loan_details(product_code);

CREATE INDEX IF NOT EXISTS idx_cases_case_number ON cases(case_number);
CREATE INDEX IF NOT EXISTS idx_cases_external_case_id ON cases(external_case_id);
CREATE INDEX IF NOT EXISTS idx_cases_case_status ON cases(case_status);
CREATE INDEX IF NOT EXISTS idx_cases_loan_id ON cases(loan_id);
CREATE INDEX IF NOT EXISTS idx_cases_allocated_to_user ON cases(allocated_to_user_id);
CREATE INDEX IF NOT EXISTS idx_cases_import_batch_id ON cases(import_batch_id);
CREATE INDEX IF NOT EXISTS idx_cases_created_at ON cases(created_at);
CREATE INDEX IF NOT EXISTS idx_cases_status_created_at ON cases(case_status, created_at);

CREATE INDEX IF NOT EXISTS idx_case_batches_batch_id ON case_batches(batch_id);
CREATE INDEX IF NOT EXISTS idx_case_batches_status ON case_batches(status);
CREATE INDEX IF NOT EXISTS idx_case_batches_source_type ON case_batches(source_type);
CREATE INDEX IF NOT EXISTS idx_case_batches_created_at ON case_batches(created_at);
CREATE INDEX IF NOT EXISTS idx_case_batches_uploaded_by ON case_batches(uploaded_by);

CREATE INDEX IF NOT EXISTS idx_batch_errors_batch_id ON batch_errors(batch_id);
CREATE INDEX IF NOT EXISTS idx_batch_errors_error_type ON batch_errors(error_type);
CREATE INDEX IF NOT EXISTS idx_batch_errors_external_case_id ON batch_errors(external_case_id);

CREATE INDEX idx_strategy_rules_strategy_id ON strategy_rules(strategy_id);
CREATE INDEX idx_strategy_actions_strategy_id ON strategy_actions(strategy_id);
CREATE INDEX idx_strategy_executions_strategy_id ON strategy_executions(strategy_id);
CREATE INDEX idx_strategy_executions_status ON strategy_executions(execution_status);

CREATE INDEX idx_campaign_templates_type ON campaign_templates(template_type);
CREATE INDEX idx_campaign_templates_code ON campaign_templates(template_code);

CREATE INDEX idx_campaigns_status ON campaigns(campaign_status);
CREATE INDEX idx_campaign_executions_campaign_id ON campaign_executions(campaign_id);
CREATE INDEX idx_campaign_executions_delivery_status ON campaign_executions(delivery_status);

CREATE INDEX idx_allocations_case_id ON allocations(case_id);
CREATE INDEX idx_allocations_allocated_to ON allocations(allocated_to_id, allocated_to_type);
CREATE INDEX idx_allocations_status ON allocations(allocation_status);
CREATE INDEX idx_allocations_allocated_at ON allocations(allocated_at);
CREATE INDEX idx_allocation_history_case_id ON allocation_history(case_id);

CREATE INDEX idx_telecalling_logs_case_id ON telecalling_logs(case_id);
CREATE INDEX idx_telecalling_logs_user_id ON telecalling_logs(user_id);
CREATE INDEX idx_telecalling_logs_disposition ON telecalling_logs(call_disposition);
CREATE INDEX idx_telecalling_logs_call_started ON telecalling_logs(call_started_at);
CREATE INDEX idx_telecalling_logs_next_followup ON telecalling_logs(next_followup_date);
CREATE INDEX idx_telecalling_history_case_id ON telecalling_history(case_id);

CREATE INDEX idx_receipts_receipt_number ON receipts(receipt_number);
CREATE INDEX idx_receipts_receipt_type ON receipts(receipt_type);
CREATE INDEX idx_receipts_generated_by ON receipts(generated_by);

CREATE INDEX idx_payment_transactions_transaction_id ON payment_transactions(transaction_id);
CREATE INDEX idx_payment_transactions_method ON payment_transactions(payment_method);
CREATE INDEX idx_payment_transactions_gateway ON payment_transactions(payment_gateway);
CREATE INDEX idx_payment_transactions_status ON payment_transactions(transaction_status);

CREATE INDEX idx_repayments_case_id ON repayments(case_id);
CREATE INDEX idx_repayments_transaction_id ON repayments(transaction_id);
CREATE INDEX idx_repayments_approval_status ON repayments(approval_status);
CREATE INDEX idx_repayments_payment_date ON repayments(payment_date);
CREATE INDEX idx_repayments_deposit_sla ON repayments(deposit_sla_status);
CREATE INDEX idx_repayments_receipt_id ON repayments(receipt_id);

CREATE INDEX idx_ots_settlements_case_id ON ots_settlements(case_id);
CREATE INDEX idx_ots_settlements_status ON ots_settlements(ots_status);
CREATE INDEX idx_ots_settlements_requested_by ON ots_settlements(requested_by);

CREATE INDEX idx_approval_workflows_entity ON approval_workflows(entity_type, entity_id);

CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);

CREATE INDEX idx_notice_dispatch_vendor_id ON notice_dispatch_details(vendor_id);
CREATE INDEX idx_notice_dispatch_tracking ON notice_dispatch_details(tracking_number);
CREATE INDEX idx_notice_dispatch_sla_status ON notice_dispatch_details(sla_status);

CREATE INDEX idx_notices_case_id ON notices(case_id);
CREATE INDEX idx_notices_dispatch_id ON notices(dispatch_id);
CREATE INDEX idx_notices_status ON notices(notice_status);
CREATE INDEX idx_notices_type ON notices(notice_type);

CREATE INDEX idx_notice_batches_status ON notice_batches(batch_status);
CREATE INDEX idx_notice_batch_items_batch_id ON notice_batch_items(batch_id);
CREATE INDEX idx_notice_batch_items_status ON notice_batch_items(item_status);

CREATE INDEX idx_vendor_performance_vendor_id ON vendor_performance(vendor_id);
CREATE INDEX idx_vendor_performance_period ON vendor_performance(period_start, period_end);

CREATE INDEX idx_notice_events_notice_id ON notice_events(notice_id);
CREATE INDEX idx_notice_events_type ON notice_events(event_type);

CREATE INDEX idx_notice_pod_notice_id ON notice_proof_of_delivery(notice_id);
CREATE INDEX idx_notice_pod_verification_status ON notice_proof_of_delivery(verification_status);

CREATE INDEX idx_session_user_id ON user_sessions(user_id);
CREATE INDEX idx_session_id ON user_sessions(session_id);
CREATE INDEX idx_session_active ON user_sessions(is_active);
CREATE INDEX idx_session_expires_at ON user_sessions(expires_at);

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
CREATE INDEX idx_sms_provider_message_id ON sms_messages(provider_message_id);
CREATE INDEX idx_sms_template_id ON sms_messages(template_id);

CREATE INDEX idx_whatsapp_message_id ON whatsapp_messages(message_id);
CREATE INDEX idx_whatsapp_mobile ON whatsapp_messages(mobile);
CREATE INDEX idx_whatsapp_status ON whatsapp_messages(status);
CREATE INDEX idx_whatsapp_campaign_id ON whatsapp_messages(campaign_id);
CREATE INDEX idx_whatsapp_case_id ON whatsapp_messages(case_id);
CREATE INDEX idx_whatsapp_created_at ON whatsapp_messages(created_at);
CREATE INDEX idx_whatsapp_provider_message_id ON whatsapp_messages(provider_message_id);
CREATE INDEX idx_whatsapp_template_id ON whatsapp_messages(template_id);

CREATE INDEX idx_email_message_id ON email_messages(message_id);
CREATE INDEX idx_email_to ON email_messages(email_to);
CREATE INDEX idx_email_status ON email_messages(status);
CREATE INDEX idx_email_campaign_id ON email_messages(campaign_id);
CREATE INDEX idx_email_case_id ON email_messages(case_id);
CREATE INDEX idx_email_created_at ON email_messages(created_at);
CREATE INDEX idx_email_provider_message_id ON email_messages(provider_message_id);
CREATE INDEX idx_email_template_id ON email_messages(template_id);

CREATE INDEX idx_webhook_message_id ON communication_webhooks(message_id);
CREATE INDEX idx_webhook_provider ON communication_webhooks(provider);
CREATE INDEX idx_webhook_event_type ON communication_webhooks(event_type);
CREATE INDEX idx_webhook_received_at ON communication_webhooks(received_at);
CREATE INDEX idx_webhook_status ON communication_webhooks(status);

CREATE INDEX idx_payment_transaction_id ON payment_gateway_transactions(transaction_id);
CREATE INDEX idx_payment_gateway_payment_id ON payment_gateway_transactions(gateway_payment_id);
CREATE INDEX idx_payment_gateway_order_id ON payment_gateway_transactions(gateway_order_id);
CREATE INDEX idx_payment_case_id ON payment_gateway_transactions(case_id);
CREATE INDEX idx_payment_status ON payment_gateway_transactions(status);
CREATE INDEX idx_payment_reconciled ON payment_gateway_transactions(reconciled);
CREATE INDEX idx_payment_created_at ON payment_gateway_transactions(created_at);

CREATE INDEX idx_dialer_call_id ON dialer_call_logs(call_id);
CREATE INDEX idx_dialer_dialer_call_id ON dialer_call_logs(dialer_call_id);
CREATE INDEX idx_dialer_agent_id ON dialer_call_logs(agent_id);
CREATE INDEX idx_dialer_case_id ON dialer_call_logs(case_id);
CREATE INDEX idx_dialer_status ON dialer_call_logs(call_status);
CREATE INDEX idx_dialer_queued_at ON dialer_call_logs(queued_at);
CREATE INDEX idx_dialer_created_at ON dialer_call_logs(created_at);

CREATE INDEX idx_alert_alert_id ON notice_alerts(alert_id);
CREATE INDEX idx_alert_notice_id ON notice_alerts(notice_id);
CREATE INDEX idx_alert_case_id ON notice_alerts(case_id);
CREATE INDEX idx_alert_user_id ON notice_alerts(user_id);
CREATE INDEX idx_alert_is_read ON notice_alerts(is_read);
CREATE INDEX idx_alert_created_at ON notice_alerts(created_at);
CREATE INDEX idx_alert_type ON notice_alerts(alert_type);

CREATE INDEX idx_scheduled_report_active ON scheduled_reports(is_active);
CREATE INDEX idx_scheduled_report_next_run ON scheduled_reports(next_run_at);
CREATE INDEX idx_scheduled_report_type ON scheduled_reports(report_type);
CREATE INDEX idx_scheduled_report_frequency ON scheduled_reports(schedule_frequency);

CREATE INDEX idx_generated_report_id ON generated_reports(report_id);
CREATE INDEX idx_generated_report_status ON generated_reports(status);
CREATE INDEX idx_generated_report_created_at ON generated_reports(created_at);
CREATE INDEX idx_generated_report_schedule_id ON generated_reports(schedule_id);
CREATE INDEX idx_generated_report_type ON generated_reports(report_type);

CREATE INDEX idx_case_note_case_id ON case_notes(case_id);
CREATE INDEX idx_case_note_created_at ON case_notes(created_at);
CREATE INDEX idx_case_note_created_by ON case_notes(created_by);
CREATE INDEX idx_case_note_type ON case_notes(note_type);
CREATE INDEX idx_case_note_important ON case_notes(is_important);

CREATE INDEX idx_archival_rule_active ON cycle_archival_rules(is_active);
CREATE INDEX idx_archival_rule_next_run ON cycle_archival_rules(next_run_at);
CREATE INDEX idx_archival_rule_frequency ON cycle_archival_rules(frequency);

CREATE INDEX idx_otp_requests_mobile_status_created ON otp_requests(mobile, status, created_at DESC);
CREATE INDEX idx_otp_requests_request_id ON otp_requests(request_id) WHERE status = 'SENT';
CREATE INDEX idx_otp_requests_provider_request_id ON otp_requests(provider_request_id);
CREATE INDEX idx_otp_requests_mobile_created ON otp_requests(mobile, created_at DESC);
CREATE INDEX idx_otp_requests_status_created ON otp_requests(status, created_at DESC);
CREATE INDEX idx_otp_requests_mobile_status_expires ON otp_requests(mobile, status, expires_at);
CREATE INDEX idx_otp_requests_mobile_purpose_created ON otp_requests(mobile, purpose, created_at DESC);

CREATE INDEX idx_cache_config_username ON cache_config(username);
CREATE INDEX idx_cache_config_is_active ON cache_config(is_active);

CREATE INDEX IF NOT EXISTS idx_sms_template_soft_ref ON sms_messages(template_id) WHERE template_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_sms_campaign_soft_ref ON sms_messages(campaign_id) WHERE campaign_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_sms_case_soft_ref ON sms_messages(case_id) WHERE case_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_sms_user_soft_ref ON sms_messages(user_id) WHERE user_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_whatsapp_template_soft_ref ON whatsapp_messages(template_id) WHERE template_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_whatsapp_campaign_soft_ref ON whatsapp_messages(campaign_id) WHERE campaign_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_whatsapp_case_soft_ref ON whatsapp_messages(case_id) WHERE case_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_whatsapp_user_soft_ref ON whatsapp_messages(user_id) WHERE user_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_email_template_soft_ref ON email_messages(template_id) WHERE template_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_email_campaign_soft_ref ON email_messages(campaign_id) WHERE campaign_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_email_case_soft_ref ON email_messages(case_id) WHERE case_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_email_user_soft_ref ON email_messages(user_id) WHERE user_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_payment_case_soft_ref ON payment_gateway_transactions(case_id) WHERE case_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_payment_created_by_soft_ref ON payment_gateway_transactions(created_by) WHERE created_by IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_dialer_agent_soft_ref ON dialer_call_logs(agent_id) WHERE agent_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_dialer_case_soft_ref ON dialer_call_logs(case_id) WHERE case_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_comm_provider_created_by_soft_ref ON communication_providers(created_by) WHERE created_by IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_comm_provider_updated_by_soft_ref ON communication_providers(updated_by) WHERE updated_by IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_notice_alert_user_soft_ref ON notice_alerts(user_id) WHERE user_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_notice_alert_case_soft_ref ON notice_alerts(case_id) WHERE case_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_notice_case_soft_ref ON notices(case_id) WHERE case_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_notice_template_soft_ref ON notices(template_id) WHERE template_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_notice_created_by_soft_ref ON notices(created_by) WHERE created_by IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_notice_batch_created_by_soft_ref ON notice_batches(created_by) WHERE created_by IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_notice_batch_item_case_soft_ref ON notice_batch_items(case_id) WHERE case_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_notice_event_created_by_soft_ref ON notice_events(created_by) WHERE created_by IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_notice_pod_uploaded_by_soft_ref ON notice_proof_of_delivery(uploaded_by) WHERE uploaded_by IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_notice_pod_verified_by_soft_ref ON notice_proof_of_delivery(verified_by) WHERE verified_by IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_allocations_allocated_to_soft_ref ON allocations(allocated_to_id) WHERE allocated_to_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_allocations_allocated_by_soft_ref ON allocations(allocated_by) WHERE allocated_by IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_allocation_hist_changed_by_soft_ref ON allocation_history(changed_by) WHERE changed_by IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_strategies_created_by_soft_ref ON strategies(created_by) WHERE created_by IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_strategy_exec_executed_by_soft_ref ON strategy_executions(executed_by) WHERE executed_by IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_campaigns_template_soft_ref ON campaigns(template_id) WHERE template_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_campaigns_created_by_soft_ref ON campaigns(created_by) WHERE created_by IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_campaign_exec_case_soft_ref ON campaign_executions(case_id) WHERE case_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_campaign_template_created_by_soft_ref ON campaign_templates(created_by) WHERE created_by IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_archival_rule_created_by_soft_ref ON cycle_archival_rules(created_by) WHERE created_by IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_cycle_closure_executed_by_soft_ref ON cycle_closures(executed_by) WHERE executed_by IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_case_note_created_by_soft_ref ON case_notes(created_by) WHERE created_by IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_case_note_updated_by_soft_ref ON case_notes(updated_by) WHERE updated_by IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_telecalling_case_soft_ref ON telecalling_logs(case_id) WHERE case_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_telecalling_user_soft_ref ON telecalling_logs(user_id) WHERE user_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_telecalling_hist_case_soft_ref ON telecalling_history(case_id) WHERE case_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_telecalling_hist_user_soft_ref ON telecalling_history(user_id) WHERE user_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_agencies_approved_by_soft_ref ON agencies(approved_by) WHERE approved_by IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_agency_user_user_soft_ref ON agency_users(user_id) WHERE user_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_agency_user_created_by_soft_ref ON agency_users(created_by) WHERE created_by IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_receipts_generated_by_soft_ref ON receipts(generated_by) WHERE generated_by IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_receipts_verified_by_soft_ref ON receipts(verified_by) WHERE verified_by IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_repayments_case_soft_ref ON repayments(case_id) WHERE case_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_repayments_collected_by_soft_ref ON repayments(collected_by) WHERE collected_by IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_repayments_approved_by_soft_ref ON repayments(approved_by) WHERE approved_by IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_ots_case_soft_ref ON ots_settlements(case_id) WHERE case_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_ots_requested_by_soft_ref ON ots_settlements(requested_by) WHERE requested_by IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_case_activities_case_soft_ref ON case_activities(case_id) WHERE case_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_case_activities_user_soft_ref ON case_activities(user_id) WHERE user_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_scheduled_report_created_by_soft_ref ON scheduled_reports(created_by) WHERE created_by IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_generated_report_created_by_soft_ref ON generated_reports(created_by) WHERE created_by IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_report_schedule_generated_by_soft_ref ON report_schedules(generated_by) WHERE generated_by IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_approval_workflow_approver_soft_ref ON approval_workflows(approver_id) WHERE approver_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_audit_log_user_soft_ref ON audit_logs(user_id) WHERE user_id IS NOT NULL;