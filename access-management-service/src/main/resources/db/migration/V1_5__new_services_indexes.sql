-- =====================================================
-- CREDIT ENFORCEMENT PLATFORM - NEW SERVICES INDEXES
-- Agency Management, My Workflow, Collections, Notice Management,
-- Configurations, DMS Service Indexes
-- =====================================================

-- =====================================================
-- AGENCY MANAGEMENT SERVICE INDEXES
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_agencies_code ON agencies(agency_code);
CREATE INDEX IF NOT EXISTS idx_agencies_type ON agencies(agency_type);
CREATE INDEX IF NOT EXISTS idx_agencies_status ON agencies(status);
CREATE INDEX IF NOT EXISTS idx_agencies_is_active ON agencies(is_active);
CREATE INDEX IF NOT EXISTS idx_agencies_service_areas ON agencies USING GIN (service_areas);

CREATE INDEX IF NOT EXISTS idx_agency_users_agency_id ON agency_users(agency_id);
CREATE INDEX IF NOT EXISTS idx_agency_users_user_code ON agency_users(user_code);
CREATE INDEX IF NOT EXISTS idx_agency_users_role ON agency_users(role);
CREATE INDEX IF NOT EXISTS idx_agency_users_active ON agency_users(is_active);

CREATE INDEX IF NOT EXISTS idx_agency_case_allocations_agency_id ON agency_case_allocations(agency_id);
CREATE INDEX IF NOT EXISTS idx_agency_case_allocations_case_id ON agency_case_allocations(case_id);
CREATE INDEX IF NOT EXISTS idx_agency_case_allocations_status ON agency_case_allocations(allocation_status);
CREATE INDEX IF NOT EXISTS idx_agency_case_allocations_batch_id ON agency_case_allocations(batch_id);

CREATE INDEX IF NOT EXISTS idx_agency_audit_logs_event_type ON agency_audit_logs(event_type);
CREATE INDEX IF NOT EXISTS idx_agency_audit_logs_entity ON agency_audit_logs(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_agency_audit_logs_created_at ON agency_audit_logs(created_at);

-- =====================================================
-- MY WORKFLOW SERVICE INDEXES
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_workflow_queues_user_id ON workflow_queues(user_id);
CREATE INDEX IF NOT EXISTS idx_workflow_queues_case_id ON workflow_queues(case_id);
CREATE INDEX IF NOT EXISTS idx_workflow_queues_status ON workflow_queues(queue_status);
CREATE INDEX IF NOT EXISTS idx_workflow_queues_type ON workflow_queues(queue_type);
CREATE INDEX IF NOT EXISTS idx_workflow_queues_priority ON workflow_queues(priority_score DESC);
CREATE INDEX IF NOT EXISTS idx_workflow_queues_next_action ON workflow_queues(next_action_date);
CREATE INDEX IF NOT EXISTS idx_workflow_queues_user_status ON workflow_queues(user_id, queue_status);

CREATE INDEX IF NOT EXISTS idx_workflow_actions_case_id ON workflow_actions(case_id);
CREATE INDEX IF NOT EXISTS idx_workflow_actions_user_id ON workflow_actions(user_id);
CREATE INDEX IF NOT EXISTS idx_workflow_actions_type ON workflow_actions(action_type);
CREATE INDEX IF NOT EXISTS idx_workflow_actions_created_at ON workflow_actions(created_at);
CREATE INDEX IF NOT EXISTS idx_workflow_actions_follow_up ON workflow_actions(follow_up_date) WHERE follow_up_required = TRUE;

CREATE INDEX IF NOT EXISTS idx_case_notes_case_id ON case_notes(case_id);
CREATE INDEX IF NOT EXISTS idx_case_notes_type ON case_notes(note_type);
CREATE INDEX IF NOT EXISTS idx_case_notes_important ON case_notes(is_important) WHERE is_important = TRUE;

CREATE INDEX IF NOT EXISTS idx_case_bookmarks_user_id ON case_bookmarks(user_id);
CREATE INDEX IF NOT EXISTS idx_case_bookmarks_case_id ON case_bookmarks(case_id);

-- =====================================================
-- COLLECTIONS SERVICE INDEXES
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_archival_rules_status ON archival_rules(status);
CREATE INDEX IF NOT EXISTS idx_archival_rules_active ON archival_rules(is_active);
CREATE INDEX IF NOT EXISTS idx_archival_rules_next_execution ON archival_rules(next_execution_at);

CREATE INDEX IF NOT EXISTS idx_cycle_closure_cases_execution_id ON cycle_closure_cases(execution_id);
CREATE INDEX IF NOT EXISTS idx_cycle_closure_cases_case_id ON cycle_closure_cases(case_id);
CREATE INDEX IF NOT EXISTS idx_cycle_closure_cases_status ON cycle_closure_cases(closure_status);

CREATE INDEX IF NOT EXISTS idx_ots_requests_case_id ON ots_requests(case_id);
CREATE INDEX IF NOT EXISTS idx_ots_requests_status ON ots_requests(ots_status);
CREATE INDEX IF NOT EXISTS idx_ots_requests_loan ON ots_requests(loan_account_number);

CREATE INDEX IF NOT EXISTS idx_repayments_case_id ON repayments(case_id);
CREATE INDEX IF NOT EXISTS idx_repayments_approval_status ON repayments(approval_status);
CREATE INDEX IF NOT EXISTS idx_repayments_payment_date ON repayments(payment_date);
CREATE INDEX IF NOT EXISTS idx_repayments_deposit_sla ON repayments(deposit_sla_status);

CREATE INDEX IF NOT EXISTS idx_receipts_case_id ON receipts(case_id);
CREATE INDEX IF NOT EXISTS idx_receipts_repayment_id ON receipts(repayment_id);

CREATE INDEX IF NOT EXISTS idx_settlement_letters_ots_id ON settlement_letters(ots_id);
CREATE INDEX IF NOT EXISTS idx_settlement_letters_case_id ON settlement_letters(case_id);

-- =====================================================
-- NOTICE MANAGEMENT SERVICE INDEXES
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_notice_vendors_code ON notice_vendors(vendor_code);
CREATE INDEX IF NOT EXISTS idx_notice_vendors_type ON notice_vendors(vendor_type);
CREATE INDEX IF NOT EXISTS idx_notice_vendors_active ON notice_vendors(is_active);

CREATE INDEX IF NOT EXISTS idx_notices_case_id ON notices(case_id);
CREATE INDEX IF NOT EXISTS idx_notices_status ON notices(notice_status);
CREATE INDEX IF NOT EXISTS idx_notices_type ON notices(notice_type);
CREATE INDEX IF NOT EXISTS idx_notices_vendor_id ON notices(vendor_id);
CREATE INDEX IF NOT EXISTS idx_notices_tracking_number ON notices(tracking_number);

CREATE INDEX IF NOT EXISTS idx_notice_pod_notice_id ON notice_proof_of_delivery(notice_id);
CREATE INDEX IF NOT EXISTS idx_notice_pod_status ON notice_proof_of_delivery(verification_status);

-- =====================================================
-- CONFIGURATIONS SERVICE INDEXES
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_offices_parent_id ON offices(parent_office_id);
CREATE INDEX IF NOT EXISTS idx_offices_type ON offices(office_type);
CREATE INDEX IF NOT EXISTS idx_offices_active ON offices(is_active);

CREATE INDEX IF NOT EXISTS idx_work_calendars_active ON work_calendars(is_active);
CREATE INDEX IF NOT EXISTS idx_work_calendars_default ON work_calendars(is_default);

CREATE INDEX IF NOT EXISTS idx_holidays_dates ON holidays(start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_holidays_type ON holidays(holiday_type);
CREATE INDEX IF NOT EXISTS idx_holidays_active ON holidays(is_active);

CREATE INDEX IF NOT EXISTS idx_currencies_active ON currencies(is_active);
CREATE INDEX IF NOT EXISTS idx_currencies_base ON currencies(is_base_currency);

CREATE INDEX IF NOT EXISTS idx_password_policies_active ON password_policies(is_active);
CREATE INDEX IF NOT EXISTS idx_password_policies_default ON password_policies(is_default);

CREATE INDEX IF NOT EXISTS idx_approval_workflows_type ON approval_workflows(workflow_type);
CREATE INDEX IF NOT EXISTS idx_approval_workflows_active ON approval_workflows(is_active);

-- =====================================================
-- DMS SERVICE INDEXES
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_documents_type ON documents(document_type);
CREATE INDEX IF NOT EXISTS idx_documents_entity ON documents(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_documents_status ON documents(document_status);
CREATE INDEX IF NOT EXISTS idx_documents_created_at ON documents(created_at);

CREATE INDEX IF NOT EXISTS idx_document_categories_parent ON document_categories(parent_category_id);
CREATE INDEX IF NOT EXISTS idx_document_categories_active ON document_categories(is_active);

CREATE INDEX IF NOT EXISTS idx_document_access_logs_document_id ON document_access_logs(document_id);
CREATE INDEX IF NOT EXISTS idx_document_access_logs_user_id ON document_access_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_document_access_logs_accessed_at ON document_access_logs(accessed_at);

CREATE INDEX IF NOT EXISTS idx_document_export_jobs_status ON document_export_jobs(job_status);
CREATE INDEX IF NOT EXISTS idx_document_export_jobs_created_by ON document_export_jobs(created_by);
