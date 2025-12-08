-- =====================================================
-- CREDIT ENFORCEMENT PLATFORM - NEW SERVICES TABLES
-- Agency Management, My Workflow, Collections, Notice Management,
-- Configurations, DMS Service Tables
-- =====================================================

-- =====================================================
-- AGENCY MANAGEMENT SERVICE TABLES (Port: 8088)
-- =====================================================

CREATE TABLE IF NOT EXISTS agencies (
    id BIGSERIAL PRIMARY KEY,
    agency_code VARCHAR(50) UNIQUE NOT NULL,
    agency_name VARCHAR(200) NOT NULL,
    agency_type VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_APPROVAL',
    -- Contact Information
    contact_person VARCHAR(100),
    contact_email VARCHAR(100),
    contact_phone VARCHAR(20),
    alternate_phone VARCHAR(20),
    -- Address
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    pincode VARCHAR(10),
    country VARCHAR(50) DEFAULT 'India',
    -- KYC Documents
    pan_number VARCHAR(20),
    gst_number VARCHAR(20),
    registration_number VARCHAR(50),
    kyc_documents JSONB,
    -- Bank Details
    bank_name VARCHAR(100),
    bank_account_number VARCHAR(50),
    bank_ifsc VARCHAR(20),
    bank_branch VARCHAR(100),
    -- Contract Details
    contract_start_date DATE,
    contract_end_date DATE,
    commission_percentage DECIMAL(5,2),
    minimum_cases INTEGER,
    maximum_cases INTEGER,
    -- Service Areas
    service_areas JSONB,
    service_pincodes JSONB,
    -- Performance Metrics
    total_cases_allocated INTEGER DEFAULT 0,
    total_cases_resolved INTEGER DEFAULT 0,
    resolution_rate DECIMAL(5,2) DEFAULT 0,
    ptp_success_rate DECIMAL(5,2) DEFAULT 0,
    active_cases_count INTEGER DEFAULT 0,
    -- Approval Workflow
    submitted_at TIMESTAMP,
    submitted_by BIGINT,
    approved_at TIMESTAMP,
    approved_by BIGINT,
    approval_notes TEXT,
    rejected_at TIMESTAMP,
    rejected_by BIGINT,
    rejection_reason TEXT,
    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS agency_users (
    id BIGSERIAL PRIMARY KEY,
    agency_id BIGINT NOT NULL,
    user_code VARCHAR(50) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),
    email VARCHAR(100),
    mobile_number VARCHAR(20) NOT NULL,
    alternate_mobile VARCHAR(20),
    role VARCHAR(30) NOT NULL,
    designation VARCHAR(100),
    -- Address
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    pincode VARCHAR(10),
    -- Work Configuration
    assigned_pincodes JSONB,
    assigned_geographies JSONB,
    max_case_capacity INTEGER DEFAULT 50,
    current_case_count INTEGER DEFAULT 0,
    -- Performance Metrics
    total_cases_handled INTEGER DEFAULT 0,
    cases_resolved INTEGER DEFAULT 0,
    ptp_captured INTEGER DEFAULT 0,
    ptp_kept INTEGER DEFAULT 0,
    -- Status
    status VARCHAR(20) DEFAULT 'ACTIVE',
    is_active BOOLEAN DEFAULT TRUE,
    deactivated_at TIMESTAMP,
    deactivated_by BIGINT,
    deactivation_reason TEXT,
    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

CREATE TABLE IF NOT EXISTS agency_case_allocations (
    id BIGSERIAL PRIMARY KEY,
    agency_id BIGINT NOT NULL,
    case_id BIGINT NOT NULL,
    external_case_id VARCHAR(100),
    agency_user_id BIGINT,
    allocation_status VARCHAR(30) DEFAULT 'ALLOCATED',
    allocated_at TIMESTAMP,
    allocated_by BIGINT,
    deallocated_at TIMESTAMP,
    deallocated_by BIGINT,
    deallocation_reason TEXT,
    batch_id VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS agency_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    actor_id BIGINT,
    actor_type VARCHAR(30) DEFAULT 'USER',
    actor_name VARCHAR(100),
    old_value JSONB,
    new_value JSONB,
    changed_fields JSONB,
    metadata JSONB,
    ip_address VARCHAR(50),
    user_agent VARCHAR(255),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- MY WORKFLOW SERVICE TABLES (Port: 8089)
-- =====================================================

CREATE TABLE IF NOT EXISTS workflow_queues (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    case_id BIGINT NOT NULL,
    queue_type VARCHAR(30) DEFAULT 'NORMAL',
    priority_score INTEGER DEFAULT 0,
    priority_reason VARCHAR(255),
    queue_status VARCHAR(20) DEFAULT 'PENDING',
    last_action_at TIMESTAMP,
    last_action_type VARCHAR(50),
    next_action_date DATE,
    next_action_type VARCHAR(50),
    case_number VARCHAR(50),
    customer_name VARCHAR(255),
    mobile_number VARCHAR(15),
    dpd INTEGER,
    bucket VARCHAR(10),
    outstanding_amount DECIMAL(15,2),
    ptp_date DATE,
    ptp_amount DECIMAL(15,2),
    added_to_queue_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    removed_from_queue_at TIMESTAMP,
    worked_duration_seconds INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_user_case_queue UNIQUE (user_id, case_id)
);

CREATE TABLE IF NOT EXISTS workflow_actions (
    id BIGSERIAL PRIMARY KEY,
    action_id VARCHAR(100) UNIQUE NOT NULL,
    case_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    action_subtype VARCHAR(50),
    action_channel VARCHAR(30),
    action_data JSONB,
    action_outcome VARCHAR(50),
    action_notes TEXT,
    follow_up_required BOOLEAN DEFAULT FALSE,
    follow_up_date DATE,
    follow_up_type VARCHAR(50),
    action_started_at TIMESTAMP,
    action_completed_at TIMESTAMP,
    action_duration_seconds INTEGER,
    communication_id BIGINT,
    communication_type VARCHAR(30),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT
);

CREATE TABLE IF NOT EXISTS case_notes (
    id BIGSERIAL PRIMARY KEY,
    case_id BIGINT NOT NULL,
    note_type VARCHAR(50) DEFAULT 'GENERAL',
    note_text TEXT NOT NULL,
    is_important BOOLEAN DEFAULT FALSE,
    is_pinned BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT
);

CREATE TABLE IF NOT EXISTS case_bookmarks (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    case_id BIGINT NOT NULL,
    bookmark_type VARCHAR(30) DEFAULT 'FAVORITE',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_user_case_bookmark UNIQUE (user_id, case_id)
);

-- =====================================================
-- COLLECTIONS SERVICE TABLES (Port: 8090)
-- =====================================================

-- Archival Rules (for ArchivalRule entity)
CREATE TABLE IF NOT EXISTS archival_rules (
    id BIGSERIAL PRIMARY KEY,
    rule_code VARCHAR(50) UNIQUE NOT NULL,
    rule_name VARCHAR(100) NOT NULL,
    description TEXT,
    criteria TEXT,
    cron_expression VARCHAR(100),
    schedule_description VARCHAR(255),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    execution_count INTEGER DEFAULT 0,
    last_execution_at TIMESTAMP,
    last_execution_result VARCHAR(20),
    last_cases_archived INTEGER,
    next_execution_at TIMESTAMP,
    total_cases_archived BIGINT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT
);

-- Cycle Closure Cases (for CycleClosure entity)
CREATE TABLE IF NOT EXISTS cycle_closure_cases (
    id BIGSERIAL PRIMARY KEY,
    execution_id VARCHAR(100) NOT NULL,
    case_id BIGINT NOT NULL,
    case_number VARCHAR(50),
    loan_account_number VARCHAR(50),
    customer_name VARCHAR(255),
    dpd_at_closure INTEGER,
    bucket_at_closure VARCHAR(10),
    outstanding_at_closure DECIMAL(15,2),
    status_before_closure VARCHAR(20),
    closure_status VARCHAR(20) DEFAULT 'PENDING',
    closure_reason VARCHAR(100),
    archived_at TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- OTS Requests
CREATE TABLE IF NOT EXISTS ots_requests (
    id BIGSERIAL PRIMARY KEY,
    ots_number VARCHAR(50) UNIQUE NOT NULL,
    case_id BIGINT NOT NULL,
    loan_account_number VARCHAR(50),
    customer_name VARCHAR(255),
    original_outstanding DECIMAL(15,2) NOT NULL,
    proposed_settlement DECIMAL(15,2) NOT NULL,
    discount_percentage DECIMAL(5,2),
    discount_amount DECIMAL(15,2),
    waiver_breakdown TEXT,
    payment_mode VARCHAR(30),
    installment_count INTEGER,
    installment_schedule TEXT,
    payment_deadline DATE,
    intent_captured_at TIMESTAMP,
    intent_captured_by BIGINT,
    intent_notes TEXT,
    borrower_consent BOOLEAN DEFAULT FALSE,
    consent_document_url VARCHAR(500),
    request_raised_at TIMESTAMP,
    request_raised_by BIGINT,
    request_notes TEXT,
    ots_status VARCHAR(20) DEFAULT 'INTENT_CAPTURED',
    current_approval_level INTEGER DEFAULT 0,
    max_approval_level INTEGER DEFAULT 2,
    letter_id BIGINT,
    letter_generated_at TIMESTAMP,
    letter_downloaded_at TIMESTAMP,
    letter_downloaded_by BIGINT,
    settled_at TIMESTAMP,
    settled_amount DECIMAL(15,2),
    cancelled_at TIMESTAMP,
    cancelled_by BIGINT,
    cancellation_reason TEXT,
    expired_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

-- Repayments
CREATE TABLE IF NOT EXISTS repayments (
    id BIGSERIAL PRIMARY KEY,
    repayment_number VARCHAR(50) UNIQUE,
    case_id BIGINT NOT NULL,
    transaction_id BIGINT NULL,
    payment_amount DECIMAL(15,2) NOT NULL,
    payment_date DATE NOT NULL,
    payment_mode VARCHAR(30),
    approval_status VARCHAR(20) DEFAULT 'PENDING',
    current_approval_level INTEGER DEFAULT 1,
    approved_by BIGINT NULL,
    approved_at TIMESTAMP NULL,
    rejection_reason TEXT,
    correction_notes TEXT,
    deposit_required_by TIMESTAMP NULL,
    deposited_at TIMESTAMP NULL,
    deposit_sla_status VARCHAR(20),
    deposit_sla_breach_hours INTEGER,
    is_reconciled BOOLEAN DEFAULT FALSE,
    reconciled_at TIMESTAMP NULL,
    reconciled_by BIGINT NULL,
    reconciliation_batch_id VARCHAR(100),
    collected_by BIGINT,
    collection_location VARCHAR(255),
    receipt_id BIGINT NULL,
    notes TEXT,
    ots_id BIGINT,
    is_ots_payment BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

-- Receipts (for Receipt entity)
CREATE TABLE IF NOT EXISTS receipts (
    id BIGSERIAL PRIMARY KEY,
    receipt_number VARCHAR(50) UNIQUE NOT NULL,
    repayment_id BIGINT NOT NULL,
    repayment_number VARCHAR(50),
    case_id BIGINT NOT NULL,
    loan_account_number VARCHAR(50),
    customer_name VARCHAR(255),
    amount DECIMAL(15,2),
    payment_mode VARCHAR(20),
    payment_date TIMESTAMP,
    payment_reference VARCHAR(100),
    format VARCHAR(20) DEFAULT 'PDF',
    pdf_url VARCHAR(500),
    generated_at TIMESTAMP,
    generated_by BIGINT,
    downloaded_at TIMESTAMP,
    downloaded_by BIGINT,
    download_count INTEGER DEFAULT 0,
    emailed_at TIMESTAMP,
    emailed_to VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Settlement Letters (for SettlementLetter entity)
CREATE TABLE IF NOT EXISTS settlement_letters (
    id BIGSERIAL PRIMARY KEY,
    letter_number VARCHAR(50) UNIQUE NOT NULL,
    ots_id BIGINT NOT NULL,
    ots_number VARCHAR(50),
    case_id BIGINT NOT NULL,
    loan_account_number VARCHAR(50),
    customer_name VARCHAR(255),
    original_outstanding DECIMAL(15,2),
    settlement_amount DECIMAL(15,2),
    waiver_amount DECIMAL(15,2),
    discount_percentage DECIMAL(5,2),
    payment_deadline TIMESTAMP,
    template_id BIGINT,
    letter_content TEXT,
    pdf_url VARCHAR(500),
    status VARCHAR(20) DEFAULT 'DRAFT',
    generated_at TIMESTAMP,
    generated_by BIGINT,
    downloaded_at TIMESTAMP,
    downloaded_by BIGINT,
    download_count INTEGER DEFAULT 0,
    sent_at TIMESTAMP,
    sent_via VARCHAR(20),
    expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- NOTICE MANAGEMENT SERVICE TABLES (Port: 8091)
-- =====================================================

CREATE TABLE IF NOT EXISTS notice_vendors (
    id BIGSERIAL PRIMARY KEY,
    vendor_code VARCHAR(50) UNIQUE NOT NULL,
    vendor_name VARCHAR(255) NOT NULL,
    vendor_type VARCHAR(30) NOT NULL,
    contact_person VARCHAR(100),
    contact_email VARCHAR(100),
    contact_mobile VARCHAR(15),
    address TEXT,
    api_endpoint VARCHAR(500),
    service_areas TEXT,
    default_dispatch_sla_hours INTEGER DEFAULT 24,
    default_delivery_sla_days INTEGER DEFAULT 7,
    cost_per_dispatch DECIMAL(10,2),
    is_active BOOLEAN DEFAULT TRUE,
    priority_order INTEGER DEFAULT 0,
    delivery_rate DECIMAL(5,2),
    rto_rate DECIMAL(5,2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

CREATE TABLE IF NOT EXISTS notices (
    id BIGSERIAL PRIMARY KEY,
    notice_number VARCHAR(50) UNIQUE NOT NULL,
    case_id BIGINT NOT NULL,
    loan_account_number VARCHAR(50),
    customer_name VARCHAR(255),
    notice_type VARCHAR(50) NOT NULL,
    notice_subtype VARCHAR(50),
    template_id BIGINT,
    language_code VARCHAR(10) DEFAULT 'en',
    generated_content TEXT,
    pdf_url VARCHAR(500),
    pdf_hash VARCHAR(255),
    page_count INTEGER,
    file_size_kb INTEGER,
    recipient_name VARCHAR(255),
    recipient_address TEXT,
    recipient_city VARCHAR(100),
    recipient_state VARCHAR(100),
    recipient_pincode VARCHAR(10),
    notice_status VARCHAR(30) DEFAULT 'DRAFT',
    generated_at TIMESTAMP,
    generated_by BIGINT,
    dispatch_id BIGINT,
    vendor_id BIGINT,
    dispatched_at TIMESTAMP,
    dispatched_by BIGINT,
    tracking_number VARCHAR(100),
    carrier_name VARCHAR(100),
    expected_delivery_at TIMESTAMP,
    delivered_at TIMESTAMP,
    pod_id BIGINT,
    rto_at TIMESTAMP,
    rto_reason VARCHAR(255),
    rto_action_taken VARCHAR(100),
    dispatch_sla_breach BOOLEAN DEFAULT FALSE,
    delivery_sla_breach BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

CREATE TABLE IF NOT EXISTS notice_proof_of_delivery (
    id BIGSERIAL PRIMARY KEY,
    pod_number VARCHAR(50) UNIQUE NOT NULL,
    notice_id BIGINT NOT NULL,
    pod_type VARCHAR(30) NOT NULL,
    pod_file_url VARCHAR(500),
    pod_file_type VARCHAR(20),
    pod_file_size_kb INTEGER,
    pod_file_hash VARCHAR(255),
    recipient_name VARCHAR(255),
    recipient_relationship VARCHAR(50),
    recipient_id_type VARCHAR(50),
    recipient_id_number VARCHAR(100),
    recipient_signature_url VARCHAR(500),
    recipient_photo_url VARCHAR(500),
    delivered_at TIMESTAMP NOT NULL,
    delivered_location VARCHAR(255),
    gps_latitude DECIMAL(10,8),
    gps_longitude DECIMAL(11,8),
    delivery_remarks TEXT,
    vendor_id BIGINT,
    vendor_agent_name VARCHAR(100),
    vendor_agent_id VARCHAR(50),
    verification_status VARCHAR(20) DEFAULT 'PENDING',
    verified_by BIGINT,
    verified_at TIMESTAMP,
    verification_remarks TEXT,
    rejection_reason TEXT,
    auto_verified BOOLEAN DEFAULT FALSE,
    auto_verification_score INTEGER,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    uploaded_by BIGINT
);

-- =====================================================
-- CONFIGURATIONS SERVICE TABLES (Port: 8092)
-- =====================================================

CREATE TABLE IF NOT EXISTS organizations (
    id BIGSERIAL PRIMARY KEY,
    org_code VARCHAR(50) UNIQUE NOT NULL,
    org_name VARCHAR(255) NOT NULL,
    legal_name VARCHAR(255),
    logo_url VARCHAR(500),
    primary_color VARCHAR(20),
    secondary_color VARCHAR(20),
    email VARCHAR(100),
    phone VARCHAR(20),
    website VARCHAR(255),
    address TEXT,
    default_currency VARCHAR(10) DEFAULT 'INR',
    default_language VARCHAR(10) DEFAULT 'en',
    default_timezone VARCHAR(50) DEFAULT 'Asia/Kolkata',
    date_format VARCHAR(20) DEFAULT 'DD/MM/YYYY',
    license_type VARCHAR(50),
    license_valid_until DATE,
    max_users INTEGER,
    max_cases INTEGER,
    enabled_features JSONB,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS offices (
    id BIGSERIAL PRIMARY KEY,
    office_code VARCHAR(50) UNIQUE NOT NULL,
    office_name VARCHAR(255) NOT NULL,
    office_type VARCHAR(30) NOT NULL,
    parent_office_id BIGINT,
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    pincode VARCHAR(10),
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    phone VARCHAR(20),
    email VARCHAR(100),
    opening_date DATE,
    work_calendar_id BIGINT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT
);

CREATE TABLE IF NOT EXISTS work_calendars (
    id BIGSERIAL PRIMARY KEY,
    calendar_code VARCHAR(50) UNIQUE NOT NULL,
    calendar_name VARCHAR(200) NOT NULL,
    working_days JSONB NOT NULL,
    work_start_time TIME DEFAULT '09:00',
    work_end_time TIME DEFAULT '18:00',
    break_start_time TIME,
    break_end_time TIME,
    non_working_day_behavior VARCHAR(30) DEFAULT 'NEXT_WORKING_DAY',
    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT
);

CREATE TABLE IF NOT EXISTS holidays (
    id BIGSERIAL PRIMARY KEY,
    holiday_code VARCHAR(50) UNIQUE NOT NULL,
    holiday_name VARCHAR(200) NOT NULL,
    holiday_type VARCHAR(30) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    applicable_states JSONB,
    applicable_offices JSONB,
    reschedule_strategy VARCHAR(30) DEFAULT 'NEXT_WORKING_DAY',
    fixed_replacement_date DATE,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT
);

CREATE TABLE IF NOT EXISTS currencies (
    id BIGSERIAL PRIMARY KEY,
    currency_code VARCHAR(10) UNIQUE NOT NULL,
    currency_name VARCHAR(100) NOT NULL,
    currency_symbol VARCHAR(10) NOT NULL,
    decimal_places INTEGER DEFAULT 2,
    exchange_rate DECIMAL(15,6) DEFAULT 1.0,
    exchange_rate_updated_at TIMESTAMP,
    is_base_currency BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS password_policies (
    id BIGSERIAL PRIMARY KEY,
    policy_code VARCHAR(50) UNIQUE NOT NULL,
    policy_name VARCHAR(200) NOT NULL,
    policy_level VARCHAR(20) NOT NULL,
    min_length INTEGER DEFAULT 8,
    max_length INTEGER DEFAULT 128,
    require_uppercase BOOLEAN DEFAULT TRUE,
    require_lowercase BOOLEAN DEFAULT TRUE,
    require_number BOOLEAN DEFAULT TRUE,
    require_special_char BOOLEAN DEFAULT TRUE,
    special_chars_allowed VARCHAR(100) DEFAULT '!@#$%^&*()_+-=[]{}|;:,.<>?',
    password_history_count INTEGER DEFAULT 5,
    prevent_reuse_days INTEGER DEFAULT 90,
    password_expiry_days INTEGER DEFAULT 90,
    warn_before_expiry_days INTEGER DEFAULT 7,
    max_failed_attempts INTEGER DEFAULT 5,
    lockout_duration_minutes INTEGER DEFAULT 30,
    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS approval_workflows (
    id BIGSERIAL PRIMARY KEY,
    workflow_code VARCHAR(50) UNIQUE NOT NULL,
    workflow_name VARCHAR(200) NOT NULL,
    workflow_type VARCHAR(50) NOT NULL,
    approval_levels JSONB NOT NULL,
    escalation_enabled BOOLEAN DEFAULT TRUE,
    escalation_hours INTEGER DEFAULT 24,
    auto_approve_enabled BOOLEAN DEFAULT FALSE,
    auto_approve_criteria JSONB,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT
);

-- =====================================================
-- DMS SERVICE TABLES (Port: 8093)
-- =====================================================

CREATE TABLE IF NOT EXISTS documents (
    id BIGSERIAL PRIMARY KEY,
    document_id VARCHAR(100) UNIQUE NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    document_subtype VARCHAR(50),
    category_id BIGINT,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    document_name VARCHAR(255) NOT NULL,
    description TEXT,
    file_url VARCHAR(500) NOT NULL,
    file_name VARCHAR(255),
    file_type VARCHAR(50),
    file_size_bytes BIGINT,
    file_hash VARCHAR(255),
    storage_provider VARCHAR(50),
    storage_path VARCHAR(500),
    storage_bucket VARCHAR(100),
    metadata TEXT,
    tags TEXT,
    document_status VARCHAR(20) DEFAULT 'ACTIVE',
    is_archived BOOLEAN DEFAULT FALSE,
    archived_at TIMESTAMP,
    archived_by BIGINT,
    version_number INTEGER DEFAULT 1,
    parent_document_id BIGINT,
    retention_days INTEGER,
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

CREATE TABLE IF NOT EXISTS document_categories (
    id BIGSERIAL PRIMARY KEY,
    category_code VARCHAR(50) UNIQUE NOT NULL,
    category_name VARCHAR(200) NOT NULL,
    parent_category_id BIGINT,
    description TEXT,
    allowed_file_types JSONB,
    max_file_size_mb INTEGER DEFAULT 10,
    retention_days INTEGER,
    display_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS document_access_logs (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    access_type VARCHAR(30) NOT NULL,
    access_ip VARCHAR(45),
    access_user_agent VARCHAR(255),
    access_reason VARCHAR(255),
    shared_with JSONB,
    accessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS document_export_jobs (
    id BIGSERIAL PRIMARY KEY,
    job_id VARCHAR(100) UNIQUE NOT NULL,
    export_type VARCHAR(30) NOT NULL,
    export_format VARCHAR(20) NOT NULL,
    filter_criteria TEXT,
    document_ids TEXT,
    total_documents INTEGER DEFAULT 0,
    exported_documents INTEGER DEFAULT 0,
    failed_documents INTEGER DEFAULT 0,
    export_file_url VARCHAR(500),
    export_file_size_bytes BIGINT,
    job_status VARCHAR(20) DEFAULT 'PENDING',
    error_message TEXT,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT
);

-- =====================================================
-- TABLE COMMENTS FOR NEW SERVICES
-- =====================================================

COMMENT ON TABLE agencies IS 'Agency master for external collection partners';
COMMENT ON TABLE agency_users IS 'Users belonging to an agency with their roles';
COMMENT ON TABLE agency_case_allocations IS 'Case allocations to agencies';
COMMENT ON TABLE agency_audit_logs IS 'Audit logs for agency management service';

COMMENT ON TABLE workflow_queues IS 'Case work queue for collectors with priority scoring';
COMMENT ON TABLE workflow_actions IS 'All actions performed on cases through workflow';
COMMENT ON TABLE case_notes IS 'Case notes and comments';
COMMENT ON TABLE case_bookmarks IS 'User bookmarked/favorite cases';

COMMENT ON TABLE archival_rules IS 'Rules for automatic case archival/closure';
COMMENT ON TABLE cycle_closure_cases IS 'Tracks each case closure record';
COMMENT ON TABLE ots_requests IS 'One-time settlement requests';
COMMENT ON TABLE repayments IS 'Repayment tracking';
COMMENT ON TABLE receipts IS 'Payment receipts';
COMMENT ON TABLE settlement_letters IS 'Generated settlement letters';

COMMENT ON TABLE notice_vendors IS 'Notice dispatch vendors (courier, print, digital)';
COMMENT ON TABLE notices IS 'Legal and communication notices';
COMMENT ON TABLE notice_proof_of_delivery IS 'Proof of delivery documents';

COMMENT ON TABLE organizations IS 'Organization/tenant configuration';
COMMENT ON TABLE offices IS 'Office/branch hierarchy';
COMMENT ON TABLE work_calendars IS 'Working days and hours configuration';
COMMENT ON TABLE holidays IS 'Holiday calendar with reschedule strategies';
COMMENT ON TABLE currencies IS 'Supported currencies';
COMMENT ON TABLE password_policies IS 'Password policy configurations';
COMMENT ON TABLE approval_workflows IS 'Generic approval workflow configurations';

COMMENT ON TABLE documents IS 'Central document repository';
COMMENT ON TABLE document_categories IS 'Document categorization';
COMMENT ON TABLE document_access_logs IS 'Audit trail for document access';
COMMENT ON TABLE document_export_jobs IS 'Bulk/filtered document export jobs';
