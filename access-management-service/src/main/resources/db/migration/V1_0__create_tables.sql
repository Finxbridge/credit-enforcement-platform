-- ACCESS MANAGEMENT SERVICE - TABLES

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    mobile_number VARCHAR(15),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    user_group_id BIGINT NULL,
    assigned_geographies JSONB,
    max_case_capacity INTEGER DEFAULT 100,
    current_case_count INTEGER DEFAULT 0,
    allocation_percentage DOUBLE PRECISION DEFAULT 100.00,
    allocation_bucket VARCHAR(50),
    team_id BIGINT,
    failed_login_attempts INTEGER DEFAULT 0,
    account_locked_until TIMESTAMP NULL,
    last_login_at TIMESTAMP NULL,
    session_id VARCHAR(255) NULL,
    session_expires_at TIMESTAMP NULL,
    is_first_login BOOLEAN DEFAULT TRUE,
    otp_secret VARCHAR(255) NULL,
    otp_expires_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

CREATE TABLE user_groups (
    id BIGSERIAL PRIMARY KEY,
    group_name VARCHAR(100) UNIQUE NOT NULL,
    group_code VARCHAR(50) UNIQUE NOT NULL,
    group_type VARCHAR(50) NOT NULL,
    parent_group_id BIGINT NULL,
    description VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

CREATE TABLE role_groups (
    id BIGSERIAL PRIMARY KEY,
    group_name VARCHAR(100) UNIQUE NOT NULL,
    group_code VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    display_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    role_name VARCHAR(50) UNIQUE NOT NULL,
    role_code VARCHAR(50) UNIQUE NOT NULL,
    role_group_id BIGINT NULL,
    description VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    permission_name VARCHAR(100) UNIQUE NOT NULL,
    permission_code VARCHAR(50) UNIQUE NOT NULL,
    resource VARCHAR(100) NOT NULL,
    action VARCHAR(20) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_roles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by BIGINT,
    CONSTRAINT unique_user_role UNIQUE (user_id, role_id)
);

CREATE TABLE role_permissions (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_role_permission UNIQUE (role_id, permission_id)
);

CREATE TABLE user_sessions (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(255) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    access_token TEXT,
    refresh_token TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    device_type VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    last_activity_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    terminated_at TIMESTAMP,
    termination_reason VARCHAR(100)
);

CREATE TABLE cache_config (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    user_id BIGINT,
    user_role VARCHAR(50),
    ip_address VARCHAR(45),
    user_agent VARCHAR(255),
    before_value JSONB,
    after_value JSONB,
    changed_fields JSONB,
    description TEXT,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE otp_requests (
    id BIGSERIAL PRIMARY KEY,
    request_id VARCHAR(100) UNIQUE NOT NULL,
    mobile VARCHAR(15),
    email VARCHAR(100),
    otp_code VARCHAR(10),
    otp_hash VARCHAR(255) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    purpose VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempt_count INTEGER DEFAULT 0,
    max_attempts INTEGER DEFAULT 3,
    provider VARCHAR(50) DEFAULT 'MSG91',
    provider_request_id VARCHAR(100),
    provider_response TEXT,
    sent_at TIMESTAMP,
    verified_at TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    user_id BIGINT
);


CREATE TABLE IF NOT EXISTS system_config (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value TEXT NOT NULL,
    is_encrypted BOOLEAN DEFAULT FALSE,
	data_type VARCHAR(20) DEFAULT 'STRING',
    config_description TEXT,
    description VARCHAR(255),
    config_category VARCHAR(50),
    is_sensitive BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT
);

CREATE TABLE third_party_integration_master (
    id BIGSERIAL PRIMARY KEY,
    integration_name VARCHAR(100) NOT NULL,
    integration_type VARCHAR(50),
    api_endpoint VARCHAR(500),
    api_key_encrypted VARCHAR(500),
    api_secret_encrypted VARCHAR(500),
    config_json JSONB,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- ===================================================
-- MASTER DATA SERVICE - TABLE CREATION
-- Domain: Master Data, System Config, Integrations
-- ===================================================

CREATE TABLE IF NOT EXISTS master_data (
    id BIGSERIAL PRIMARY KEY,
    data_type VARCHAR(50) NOT NULL,
    code VARCHAR(100) NOT NULL,
    value VARCHAR(255) NOT NULL,
    parent_code VARCHAR(100) NULL,
    display_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_master_data UNIQUE (data_type, code)
);



CREATE TABLE IF NOT EXISTS third_party_integration_master (
    id BIGSERIAL PRIMARY KEY,
    integration_name VARCHAR(100) NOT NULL,
    integration_type VARCHAR(50),
    api_endpoint VARCHAR(500),
    api_key_encrypted VARCHAR(500),
    api_secret_encrypted VARCHAR(500),
    config_json JSONB,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE master_data IS 'Centralized master data repository for dropdowns and configurations';
COMMENT ON TABLE third_party_integration_master IS 'Third-party integration configurations';

-- ===================================================
-- CASE SOURCING SERVICE - TABLE CREATION
-- Domain: Customers, Loans, Cases, Payments, Telecalling
-- ===================================================

-- ===================================================
-- CUSTOMER DOMAIN
-- ===================================================

CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    customer_code VARCHAR(50) UNIQUE NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    mobile_number VARCHAR(15),
    alternate_mobile VARCHAR(15),
    email VARCHAR(100),
    alternate_email VARCHAR(100),
    address VARCHAR(500),
    city VARCHAR(100),
    state VARCHAR(100),
    pincode VARCHAR(10),
    pan_number VARCHAR(20),
    aadhar_number VARCHAR(20),
    date_of_birth DATE,
    gender VARCHAR(10),
    occupation VARCHAR(100),
    customer_type VARCHAR(20) DEFAULT 'INDIVIDUAL',
    is_active BOOLEAN DEFAULT TRUE,
    language_preference VARCHAR(10) DEFAULT 'en',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE loan_details (
    id BIGSERIAL PRIMARY KEY,
    loan_account_number VARCHAR(50) UNIQUE NOT NULL,
    bank_code VARCHAR(50),
    product_code VARCHAR(50),
    product_type VARCHAR(100),
    primary_customer_id BIGINT NOT NULL,
    co_borrower_customer_id BIGINT,
    guarantor_customer_id BIGINT,
    loan_disbursement_date DATE,
    loan_maturity_date DATE,
    principal_amount DECIMAL(15,2),
    interest_amount DECIMAL(15,2),
    penalty_amount DECIMAL(15,2),
    total_outstanding DECIMAL(15,2),
    interest_rate DECIMAL(5,2),
    tenure_months INTEGER,
    emi_amount DECIMAL(15,2),
    dpd INTEGER,
    bucket VARCHAR(10),
    due_date DATE,
    source_system VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===================================================
-- CASE DOMAIN
-- ===================================================

CREATE TABLE cases (
    id BIGSERIAL PRIMARY KEY,
    case_number VARCHAR(50) UNIQUE NOT NULL,
    external_case_id VARCHAR(100),
    loan_id BIGINT NOT NULL,
    case_status VARCHAR(20) DEFAULT 'UNALLOCATED',
    case_priority VARCHAR(20) DEFAULT 'MEDIUM',
    case_opened_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    case_closed_at TIMESTAMP,
    case_closure_reason VARCHAR(100),
    allocated_to_user_id BIGINT,
    allocated_to_agency_id BIGINT,
    allocated_at TIMESTAMP,
    geography_code VARCHAR(50),
    city_code VARCHAR(50),
    state_code VARCHAR(50),
    ptp_date DATE,
    ptp_amount DECIMAL(15,2),
    ptp_status VARCHAR(20),
    next_followup_date DATE,
    source_type VARCHAR(20),
    source_file_name VARCHAR(255),
    import_batch_id VARCHAR(100),
    collection_cycle VARCHAR(50),
    is_archived BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

CREATE TABLE case_batches (
    id BIGSERIAL PRIMARY KEY,
    batch_id VARCHAR(100) UNIQUE NOT NULL,
    source_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_cases INTEGER DEFAULT 0,
    valid_cases INTEGER DEFAULT 0,
    invalid_cases INTEGER DEFAULT 0,
    duplicate_cases INTEGER DEFAULT 0,
    uploaded_by VARCHAR(255),
    file_name VARCHAR(500),
    file_path VARCHAR(1000),
    validation_job_id VARCHAR(100),
    completed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE batch_errors (
    id BIGSERIAL PRIMARY KEY,
    batch_id VARCHAR(100) NOT NULL,
    row_number INTEGER,
    external_case_id VARCHAR(100),
    error_type VARCHAR(50),
    error_message TEXT,
    field_name VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    error_id VARCHAR(100) UNIQUE,
    case_id BIGINT,
    module VARCHAR(50) DEFAULT 'CASE_SOURCING'
);

CREATE TABLE case_notes (
    id BIGSERIAL PRIMARY KEY,
    case_id BIGINT NOT NULL,
    note_type VARCHAR(50) DEFAULT 'GENERAL',
    note_text TEXT NOT NULL,
    is_important BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT
);

CREATE TABLE case_activities (
    id BIGSERIAL PRIMARY KEY,
    case_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    activity_type VARCHAR(50) NOT NULL,
    activity_subtype VARCHAR(50),
    details TEXT,
    activity_metadata JSONB,
    next_followup_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===================================================
-- TELECALLING DOMAIN
-- ===================================================

CREATE TABLE telecalling_logs (
    id BIGSERIAL PRIMARY KEY,
    case_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    call_type VARCHAR(20),
    dialed_number VARCHAR(15),
    bridge_number VARCHAR(15),
    call_disposition VARCHAR(30),
    sub_disposition VARCHAR(50),
    call_started_at TIMESTAMP NULL,
    call_ended_at TIMESTAMP NULL,
    call_duration_seconds INTEGER DEFAULT 0,
    talk_time_seconds INTEGER DEFAULT 0,
    recording_url VARCHAR(500),
    recording_duration_seconds INTEGER,
    agent_notes TEXT,
    next_followup_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE telecalling_history (
    id BIGSERIAL PRIMARY KEY,
    case_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    call_details JSONB,
    archived_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===================================================
-- PAYMENT & RECEIPT DOMAIN
-- ===================================================

CREATE TABLE receipts (
    id BIGSERIAL PRIMARY KEY,
    receipt_number VARCHAR(50) UNIQUE NOT NULL,
    receipt_type VARCHAR(20) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_type VARCHAR(20),
    file_size_kb INTEGER,
    file_hash VARCHAR(255),
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    generated_by BIGINT,
    is_verified BOOLEAN DEFAULT FALSE,
    verified_by BIGINT NULL,
    verified_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE payment_transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(100) UNIQUE NOT NULL,
    payment_method VARCHAR(20),
    payment_gateway VARCHAR(50),
    payment_reference VARCHAR(100),
    cheque_number VARCHAR(50),
    cheque_date DATE,
    bank_name VARCHAR(100),
    payment_link_url VARCHAR(500),
    gateway_response JSONB,
    transaction_status VARCHAR(20) DEFAULT 'PENDING',
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE repayments (
    id BIGSERIAL PRIMARY KEY,
    case_id BIGINT NOT NULL,
    transaction_id BIGINT NULL,
    payment_amount DECIMAL(15,2) NOT NULL,
    payment_date DATE NOT NULL,
    approval_status VARCHAR(20) DEFAULT 'PENDING',
    approved_by BIGINT NULL,
    approved_at TIMESTAMP NULL,
    rejection_reason TEXT,
    deposit_required_by TIMESTAMP NULL,
    deposited_at TIMESTAMP NULL,
    deposit_sla_status VARCHAR(20),
    is_reconciled BOOLEAN DEFAULT FALSE,
    reconciled_at TIMESTAMP NULL,
    reconciled_by BIGINT NULL,
    collected_by BIGINT,
    receipt_id BIGINT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ots_settlements (
    id BIGSERIAL PRIMARY KEY,
    case_id BIGINT NOT NULL,
    original_amount DECIMAL(15,2) NOT NULL,
    settlement_amount DECIMAL(15,2) NOT NULL,
    discount_percentage DECIMAL(5,2),
    discount_amount DECIMAL(15,2),
    offer_valid_from DATE,
    offer_valid_to DATE,
    payment_terms TEXT,
    ots_status VARCHAR(20) DEFAULT 'PENDING',
    requested_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===================================================
-- PTP (Promise to Pay) DOMAIN
-- ===================================================

CREATE TABLE ptp_commitments (
    id BIGSERIAL PRIMARY KEY,
    case_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    ptp_date DATE NOT NULL,
    ptp_amount DECIMAL(15,2) NOT NULL,
    commitment_date TIMESTAMP NOT NULL,
    ptp_status VARCHAR(20) NOT NULL,
    payment_received_amount DECIMAL(15,2),
    payment_received_date DATE,
    broken_reason VARCHAR(500),
    notes TEXT,
    reminder_sent BOOLEAN DEFAULT FALSE,
    reminder_sent_at TIMESTAMP,
    follow_up_date DATE,
    follow_up_completed BOOLEAN DEFAULT FALSE,
    call_disposition VARCHAR(50),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by BIGINT
);

-- ===================================================
-- COMMENTS
-- ===================================================

COMMENT ON TABLE customers IS 'Customer master data including primary and co-borrowers';
COMMENT ON TABLE loan_details IS 'Loan account details and outstanding amounts';
COMMENT ON TABLE cases IS 'Collection cases linked to loan accounts';
COMMENT ON TABLE case_batches IS 'Batch upload tracking for case sourcing';
COMMENT ON TABLE batch_errors IS 'Validation errors from batch uploads';
COMMENT ON TABLE case_notes IS 'Notes and comments on cases';
COMMENT ON TABLE case_activities IS 'Activity log for case interactions';
COMMENT ON TABLE telecalling_logs IS 'Telecalling activity logs';
COMMENT ON TABLE receipts IS 'Payment receipts and documentation';
COMMENT ON TABLE payment_transactions IS 'Payment transaction details';
COMMENT ON TABLE repayments IS 'Repayment records for cases';
COMMENT ON TABLE ots_settlements IS 'One-time settlement offers and approvals';
COMMENT ON TABLE ptp_commitments IS 'Promise to Pay commitments and tracking';

COMMENT ON COLUMN customers.language_preference IS 'Preferred language for customer communication (e.g., en, hi, ta, etc.)';


-- ===================================================
-- ALLOCATION REALLOCATION SERVICE - TABLE CREATION
-- Domain: Allocations, Reallocation, Batch Processing
-- ===================================================

CREATE TABLE allocations (
    id BIGSERIAL PRIMARY KEY,
    case_id BIGINT NOT NULL,
    allocated_to_id BIGINT NOT NULL,
    allocated_to_type VARCHAR(20) NOT NULL,
    allocation_type VARCHAR(20) DEFAULT 'PRIMARY',
    workload_percentage DECIMAL(5,2),
    geography_code VARCHAR(50),
    allocation_status VARCHAR(20) DEFAULT 'ACTIVE',
    allocated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deallocated_at TIMESTAMP NULL,
    allocated_by BIGINT,
    external_case_id VARCHAR(100),
    secondary_agent_id BIGINT,
    allocation_rule_id BIGINT,
    batch_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE allocation_history (
    id BIGSERIAL PRIMARY KEY,
    case_id BIGINT NOT NULL,
    previous_owner_id BIGINT NULL,
    previous_owner_type VARCHAR(20) NULL,
    new_owner_id BIGINT NOT NULL,
    new_owner_type VARCHAR(20) NOT NULL,
    action_type VARCHAR(20),
    reason VARCHAR(500),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    changed_by BIGINT,
    external_case_id VARCHAR(100),
    allocated_to_username VARCHAR(100),
    batch_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE allocation_batches (
    id BIGSERIAL PRIMARY KEY,
    batch_id VARCHAR(100) UNIQUE NOT NULL,
    total_cases INTEGER DEFAULT 0,
    successful_allocations INTEGER DEFAULT 0,
    failed_allocations INTEGER DEFAULT 0,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PROCESSING', 'COMPLETED', 'FAILED', 'PARTIALLY_COMPLETED')),
    uploaded_by BIGINT,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    file_name VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE allocation_rules (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    criteria JSONB NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'DRAFT', 'READY_FOR_APPLY')),
    priority INTEGER DEFAULT 0,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE contact_update_batches (
    id BIGSERIAL PRIMARY KEY,
    batch_id VARCHAR(100) UNIQUE NOT NULL,
    total_records INTEGER DEFAULT 0,
    successful_updates INTEGER DEFAULT 0,
    failed_updates INTEGER DEFAULT 0,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PROCESSING', 'COMPLETED', 'FAILED', 'PARTIALLY_COMPLETED')),
    uploaded_by BIGINT,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    file_name VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE reallocation_jobs (
    id BIGSERIAL PRIMARY KEY,
    job_id VARCHAR(100) UNIQUE NOT NULL,
    job_type VARCHAR(50) NOT NULL CHECK (job_type IN ('BY_AGENT', 'BY_FILTER', 'BULK_UPLOAD')),
    from_user_id BIGINT,
    to_user_id BIGINT,
    filter_criteria JSONB,
    reason VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PROCESSING' CHECK (status IN ('PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED')),
    total_cases INTEGER DEFAULT 0,
    processed_cases INTEGER DEFAULT 0,
    successful_cases INTEGER DEFAULT 0,
    failed_cases INTEGER DEFAULT 0,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ===================================================
-- COMMENTS
-- ===================================================

COMMENT ON TABLE allocations IS 'Tracks current case allocations to agents/agencies';
COMMENT ON TABLE allocation_history IS 'Historical log of all allocation changes';
COMMENT ON TABLE allocation_batches IS 'Tracks bulk allocation CSV upload batches';
COMMENT ON TABLE allocation_rules IS 'Stores allocation rules for automatic case allocation';
COMMENT ON TABLE contact_update_batches IS 'Tracks bulk contact information update batches';
COMMENT ON TABLE reallocation_jobs IS 'Tracks reallocation jobs executed by agent or filter criteria';

-- ===================================================
-- COMMUNICATION SERVICE - TABLE CREATION
-- Domain: Providers, OTP, SMS, WhatsApp, Email
-- ===================================================

CREATE TABLE communication_providers (
    id BIGSERIAL PRIMARY KEY,
    provider_type VARCHAR(50) NOT NULL,
    provider_name VARCHAR(100) NOT NULL,
    display_name VARCHAR(100),
    config JSONB NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    priority INTEGER DEFAULT 1,
    rate_limit_per_day INTEGER,
    cost_per_unit DECIMAL(10, 4),
    balance_credits DECIMAL(15, 2),
    last_balance_check_at TIMESTAMP,
    test_status VARCHAR(20),
    last_test_at TIMESTAMP,
    last_test_response TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);



CREATE TABLE sms_messages (
    id BIGSERIAL PRIMARY KEY,
    message_id VARCHAR(100) UNIQUE NOT NULL,
    mobile VARCHAR(15) NOT NULL,
    template_id BIGINT,
    template_code VARCHAR(50),
    message_content TEXT NOT NULL,
    sender_id VARCHAR(20) DEFAULT 'FINXCO',
    provider VARCHAR(50) DEFAULT 'MSG91',
    provider_message_id VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'QUEUED',
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    cost DECIMAL(10, 4),
    campaign_id BIGINT,
    case_id BIGINT,
    user_id BIGINT,
    scheduled_at TIMESTAMP,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason TEXT,
    dlr_status VARCHAR(50),
    dlr_received_at TIMESTAMP,
    provider_response TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE whatsapp_messages (
    id BIGSERIAL PRIMARY KEY,
    message_id VARCHAR(100) UNIQUE NOT NULL,
    mobile VARCHAR(15) NOT NULL,
    template_name VARCHAR(100),
    template_id BIGINT,
    language VARCHAR(10) DEFAULT 'en',
    message_content TEXT,
    message_type VARCHAR(20) DEFAULT 'TEXT',
    media_url TEXT,
    media_filename VARCHAR(255),
    provider VARCHAR(50) DEFAULT 'MSG91',
    provider_message_id VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'QUEUED',
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    cost DECIMAL(10, 4),
    campaign_id BIGINT,
    case_id BIGINT,
    user_id BIGINT,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    read_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason TEXT,
    provider_response TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE email_messages (
    id BIGSERIAL PRIMARY KEY,
    message_id VARCHAR(100) UNIQUE NOT NULL,
    email_to VARCHAR(100) NOT NULL,
    email_cc VARCHAR(255),
    email_bcc VARCHAR(255),
    from_email VARCHAR(100) NOT NULL,
    from_name VARCHAR(100),
    reply_to VARCHAR(100),
    subject VARCHAR(255) NOT NULL,
    body_html TEXT,
    body_text TEXT,
    template_id BIGINT,
    template_code VARCHAR(50),
    has_attachments BOOLEAN DEFAULT FALSE,
    attachment_urls TEXT,
    provider VARCHAR(50) DEFAULT 'SENDGRID',
    provider_message_id VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'QUEUED',
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    campaign_id BIGINT,
    case_id BIGINT,
    user_id BIGINT,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    opened_at TIMESTAMP,
    clicked_at TIMESTAMP,
    bounced_at TIMESTAMP,
    bounce_type VARCHAR(50),
    bounce_reason TEXT,
    failed_at TIMESTAMP,
    failure_reason TEXT,
    provider_response TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE communication_webhooks (
    id BIGSERIAL PRIMARY KEY,
    webhook_id VARCHAR(100) UNIQUE NOT NULL,
    provider VARCHAR(50) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    message_id VARCHAR(100),
    payload JSONB NOT NULL,
    status VARCHAR(20) DEFAULT 'RECEIVED',
    processed_at TIMESTAMP,
    error_message TEXT,
    received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE dialer_call_logs (
    id BIGSERIAL PRIMARY KEY,
    call_id VARCHAR(100) UNIQUE NOT NULL,
    dialer_call_id VARCHAR(100),
    dialer_name VARCHAR(50) DEFAULT 'OZONETEL',
    agent_id BIGINT,
    case_id BIGINT,
    customer_mobile VARCHAR(15) NOT NULL,
    call_type VARCHAR(20) NOT NULL,
    call_status VARCHAR(50),
    call_duration INTEGER,
    recording_url TEXT,
    disposition VARCHAR(100),
    notes TEXT,
    queue_priority INTEGER,
    queued_at TIMESTAMP,
    initiated_at TIMESTAMP,
    answered_at TIMESTAMP,
    ended_at TIMESTAMP,
    dialer_response JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

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



COMMENT ON TABLE communication_providers IS 'Third-party communication provider configurations';
COMMENT ON TABLE otp_requests IS 'OTP generation and verification requests';
COMMENT ON TABLE sms_messages IS 'SMS message tracking and delivery status';
COMMENT ON TABLE whatsapp_messages IS 'WhatsApp message tracking and delivery status';
COMMENT ON TABLE email_messages IS 'Email message tracking and delivery status';
COMMENT ON TABLE communication_webhooks IS 'Webhook events from communication providers';
COMMENT ON TABLE dialer_call_logs IS 'Dialer system call logs and tracking';
COMMENT ON TABLE payment_gateway_transactions IS 'Payment gateway transaction tracking';
COMMENT ON TABLE system_config IS 'System configuration key-value pairs';
