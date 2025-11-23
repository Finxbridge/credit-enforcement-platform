-- ===================================================
-- UNUSED / FUTURE SERVICES - SQL SCRIPTS
-- ===================================================
-- This file contains table definitions for services that are NOT YET IMPLEMENTED
-- These tables were originally in access-management-service but belong to future microservices
--
-- FUTURE SERVICES IDENTIFIED:
-- 1. Payment Gateway Service
-- 2. Dialer Service
-- 3. Reporting Service
-- 4. Notice Service
-- 5. Agency Service
-- 6. Workflow Service
-- 7. Audit Service
-- 8. Archival Service
--
-- USAGE: When implementing a new service, extract the relevant tables from this file
-- and create proper migration scripts in that service's db/migration folder
-- ===================================================

-- ===================================================
-- PAYMENT GATEWAY SERVICE (Future)
-- ===================================================

CREATE TABLE payment_gateway_transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(100) UNIQUE NOT NULL,
    gateway_order_id VARCHAR(100),
    gateway_payment_id VARCHAR(100),
    gateway_name VARCHAR(50) DEFAULT 'RAZORPAY',
    case_id BIGINT,
    loan_account_number VARCHAR(50),
    amount DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'INR',
    payment_method VARCHAR(50),
    payment_link TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'CREATED',
    customer_mobile VARCHAR(15),
    customer_email VARCHAR(100),
    payment_received_at TIMESTAMP,
    refund_amount DECIMAL(15, 2),
    refunded_at TIMESTAMP,
    failure_reason TEXT,
    gateway_response JSONB,
    webhook_received BOOLEAN DEFAULT FALSE,
    webhook_received_at TIMESTAMP,
    reconciled BOOLEAN DEFAULT FALSE,
    reconciled_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT
);

CREATE INDEX idx_payment_transaction_id ON payment_gateway_transactions(transaction_id);
CREATE INDEX idx_payment_gateway_payment_id ON payment_gateway_transactions(gateway_payment_id);
CREATE INDEX idx_payment_gateway_order_id ON payment_gateway_transactions(gateway_order_id);
CREATE INDEX idx_payment_case_id ON payment_gateway_transactions(case_id);
CREATE INDEX idx_payment_status ON payment_gateway_transactions(status);
CREATE INDEX idx_payment_reconciled ON payment_gateway_transactions(reconciled);
CREATE INDEX idx_payment_created_at ON payment_gateway_transactions(created_at);

-- ===================================================
-- REPORTING SERVICE (Future)
-- ===================================================

CREATE TABLE scheduled_reports (
    id BIGSERIAL PRIMARY KEY,
    report_name VARCHAR(200) NOT NULL,
    report_type VARCHAR(100) NOT NULL,
    schedule_frequency VARCHAR(20) NOT NULL,
    schedule_time TIME NOT NULL,
    schedule_day_of_week INTEGER,
    schedule_day_of_month INTEGER,
    recipients JSONB NOT NULL,
    filters JSONB,
    format VARCHAR(10) DEFAULT 'CSV',
    is_active BOOLEAN DEFAULT TRUE,
    last_run_at TIMESTAMP,
    last_run_status VARCHAR(20),
    next_run_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT
);

CREATE TABLE generated_reports (
    id BIGSERIAL PRIMARY KEY,
    report_id VARCHAR(100) UNIQUE NOT NULL,
    report_name VARCHAR(200) NOT NULL,
    report_type VARCHAR(100) NOT NULL,
    schedule_id BIGINT,
    filters JSONB,
    format VARCHAR(10) DEFAULT 'CSV',
    file_path TEXT,
    file_url TEXT,
    file_size_bytes BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'PROCESSING',
    row_count INTEGER,
    generated_at TIMESTAMP,
    expires_at TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT
);

CREATE TABLE report_schedules (
    id BIGSERIAL PRIMARY KEY,
    report_name VARCHAR(255) NOT NULL,
    report_type VARCHAR(50) NOT NULL,
    filter_json JSONB,
    frequency VARCHAR(20) DEFAULT 'DAILY',
    last_generated_at TIMESTAMP NULL,
    next_run_at TIMESTAMP NULL,
    report_status VARCHAR(20) DEFAULT 'ACTIVE',
    generated_by BIGINT,
    delivery_channel VARCHAR(50) DEFAULT 'EMAIL',
    recipient_list TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for reporting
CREATE INDEX idx_scheduled_report_active ON scheduled_reports(is_active);
CREATE INDEX idx_scheduled_report_next_run ON scheduled_reports(next_run_at);
CREATE INDEX idx_scheduled_report_type ON scheduled_reports(report_type);
CREATE INDEX idx_scheduled_report_frequency ON scheduled_reports(schedule_frequency);

CREATE INDEX idx_generated_report_id ON generated_reports(report_id);
CREATE INDEX idx_generated_report_status ON generated_reports(status);
CREATE INDEX idx_generated_report_created_at ON generated_reports(created_at);
CREATE INDEX idx_generated_report_schedule_id ON generated_reports(schedule_id);
CREATE INDEX idx_generated_report_type ON generated_reports(report_type);

-- ===================================================
-- NOTICE SERVICE (Future)
-- ===================================================

CREATE TABLE notice_vendors (
    id BIGSERIAL PRIMARY KEY,
    vendor_code VARCHAR(50) UNIQUE NOT NULL,
    vendor_name VARCHAR(255) NOT NULL,
    vendor_type VARCHAR(20),
    contact_person VARCHAR(100),
    contact_email VARCHAR(100),
    contact_mobile VARCHAR(15),
    address TEXT,
    api_endpoint VARCHAR(500),
    api_key_encrypted VARCHAR(500),
    integration_config JSONB,
    default_delivery_sla_hours INTEGER DEFAULT 72,
    priority_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notice_dispatch_details (
    id BIGSERIAL PRIMARY KEY,
    dispatch_method VARCHAR(20),
    vendor_id BIGINT NULL,
    vendor_job_id VARCHAR(100),
    tracking_number VARCHAR(100),
    dispatch_sla_hours INTEGER DEFAULT 24,
    delivery_sla_hours INTEGER DEFAULT 72,
    dispatched_at TIMESTAMP NULL,
    expected_delivery_at TIMESTAMP NULL,
    delivered_at TIMESTAMP NULL,
    sla_status VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notices (
    id BIGSERIAL PRIMARY KEY,
    notice_number VARCHAR(50) UNIQUE NOT NULL,
    case_id BIGINT NOT NULL,
    notice_type VARCHAR(50),
    template_id BIGINT NOT NULL,
    dispatch_id BIGINT NULL,
    generated_content TEXT,
    pdf_url VARCHAR(500),
    pdf_hash VARCHAR(255),
    language_code VARCHAR(10) DEFAULT 'en',
    page_count INTEGER,
    file_size_kb INTEGER,
    notice_status VARCHAR(20) DEFAULT 'DRAFT',
    generated_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT
);

CREATE TABLE notice_batches (
    id BIGSERIAL PRIMARY KEY,
    batch_number VARCHAR(50) UNIQUE NOT NULL,
    total_notices INTEGER DEFAULT 0,
    generated_notices INTEGER DEFAULT 0,
    failed_notices INTEGER DEFAULT 0,
    batch_status VARCHAR(20),
    started_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT
);

CREATE TABLE notice_batch_items (
    id BIGSERIAL PRIMARY KEY,
    batch_id BIGINT NOT NULL,
    notice_id BIGINT NULL,
    case_id BIGINT NOT NULL,
    item_status VARCHAR(20),
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE vendor_performance (
    id BIGSERIAL PRIMARY KEY,
    vendor_id BIGINT NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    total_dispatched INTEGER DEFAULT 0,
    total_delivered INTEGER DEFAULT 0,
    total_rto INTEGER DEFAULT 0,
    total_pending INTEGER DEFAULT 0,
    delivery_percentage DECIMAL(5,2),
    rto_percentage DECIMAL(5,2),
    avg_delivery_time_hours DECIMAL(10,2),
    sla_compliance_percentage DECIMAL(5,2),
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notice_events (
    id BIGSERIAL PRIMARY KEY,
    notice_id BIGINT NOT NULL,
    event_type VARCHAR(30),
    event_description TEXT,
    event_source VARCHAR(20),
    event_location VARCHAR(255),
    event_timestamp TIMESTAMP NULL,
    vendor_response JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT
);

CREATE TABLE notice_proof_of_delivery (
    id BIGSERIAL PRIMARY KEY,
    notice_id BIGINT NOT NULL,
    pod_type VARCHAR(20),
    pod_file_url VARCHAR(500),
    recipient_name VARCHAR(255),
    recipient_relationship VARCHAR(50),
    recipient_signature_url VARCHAR(500),
    delivered_at TIMESTAMP NULL,
    delivered_location VARCHAR(255),
    gps_coordinates VARCHAR(100),
    verification_status VARCHAR(20) DEFAULT 'PENDING',
    verified_by BIGINT NULL,
    verified_at TIMESTAMP NULL,
    verification_comments TEXT,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    uploaded_by BIGINT
);

CREATE TABLE notice_alerts (
    id BIGSERIAL PRIMARY KEY,
    alert_id VARCHAR(100) UNIQUE NOT NULL,
    notice_id BIGINT NOT NULL,
    case_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    alert_type VARCHAR(50) NOT NULL,
    alert_message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Notice service indexes
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

CREATE INDEX idx_alert_alert_id ON notice_alerts(alert_id);
CREATE INDEX idx_alert_notice_id ON notice_alerts(notice_id);
CREATE INDEX idx_alert_case_id ON notice_alerts(case_id);
CREATE INDEX idx_alert_user_id ON notice_alerts(user_id);
CREATE INDEX idx_alert_is_read ON notice_alerts(is_read);
CREATE INDEX idx_alert_created_at ON notice_alerts(created_at);
CREATE INDEX idx_alert_type ON notice_alerts(alert_type);

-- ===================================================
-- AGENCY SERVICE (Future)
-- ===================================================

CREATE TABLE agencies (
    id BIGSERIAL PRIMARY KEY,
    agency_code VARCHAR(50) UNIQUE NOT NULL,
    agency_name VARCHAR(255) NOT NULL,
    contact_person VARCHAR(100),
    contact_email VARCHAR(100),
    contact_mobile VARCHAR(15),
    address TEXT,
    pan_number VARCHAR(20),
    gstin VARCHAR(20),
    kyc_documents JSONB,
    onboarding_status VARCHAR(20) DEFAULT 'PENDING',
    approved_by BIGINT NULL,
    approved_at TIMESTAMP NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE agency_users (
    id BIGSERIAL PRIMARY KEY,
    agency_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    agency_role VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    CONSTRAINT unique_agency_user UNIQUE (agency_id, user_id)
);

CREATE INDEX idx_agencies_code ON agencies(agency_code);
CREATE INDEX idx_agencies_is_active ON agencies(is_active);

CREATE INDEX idx_agency_users_agency_id ON agency_users(agency_id);
CREATE INDEX idx_agency_users_user_id ON agency_users(user_id);

-- ===================================================
-- WORKFLOW SERVICE (Future)
-- ===================================================

CREATE TABLE approval_workflows (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(50),
    entity_id BIGINT NOT NULL,
    approval_level INTEGER DEFAULT 1,
    approver_id BIGINT NOT NULL,
    action VARCHAR(20),
    comments TEXT,
    actioned_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_approval_workflows_entity ON approval_workflows(entity_type, entity_id);
CREATE INDEX idx_approval_workflows_approver ON approval_workflows(approver_id);

-- ===================================================
-- ARCHIVAL SERVICE (Future)
-- ===================================================

CREATE TABLE cycle_archival_rules (
    id BIGSERIAL PRIMARY KEY,
    rule_name VARCHAR(200) NOT NULL,
    rule_description TEXT,
    frequency VARCHAR(20) NOT NULL,
    filter_criteria JSONB NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    last_run_at TIMESTAMP,
    last_run_status VARCHAR(20),
    cases_archived_count INTEGER DEFAULT 0,
    next_run_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT
);

CREATE TABLE cycle_closures (
    id BIGSERIAL PRIMARY KEY,
    cycle_type VARCHAR(20) NOT NULL,
    criteria_json JSONB NOT NULL,
    archived_case_count INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'PENDING',
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    executed_by BIGINT,
    remarks TEXT
);

CREATE TABLE dashboard_metrics_cache (
    id BIGSERIAL PRIMARY KEY,
    metric_date DATE NOT NULL,
    metric_type VARCHAR(50) NOT NULL,
    metric_json JSONB NOT NULL,
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_metrics_cache UNIQUE (metric_date, metric_type)
);

CREATE INDEX idx_archival_rule_active ON cycle_archival_rules(is_active);
CREATE INDEX idx_archival_rule_next_run ON cycle_archival_rules(next_run_at);
CREATE INDEX idx_archival_rule_frequency ON cycle_archival_rules(frequency);

CREATE INDEX idx_cycle_closure_status ON cycle_closures(status);
CREATE INDEX idx_cycle_closure_executed_by ON cycle_closures(executed_by);

CREATE INDEX idx_dashboard_metrics_cache_date ON dashboard_metrics_cache(metric_date);
CREATE INDEX idx_dashboard_metrics_cache_type ON dashboard_metrics_cache(metric_type);

-- ===================================================
-- END OF UNUSED / FUTURE SERVICES SCRIPTS
-- ===================================================
