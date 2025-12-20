-- =====================================================
-- CREDIT ENFORCEMENT PLATFORM - CONSOLIDATED TABLES
-- All CREATE TABLE statements in one file
-- =====================================================

-- =====================================================
-- ACCESS MANAGEMENT SERVICE TABLES
-- =====================================================

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
    audit_id VARCHAR(50) UNIQUE,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    action VARCHAR(50) NOT NULL,
    case_id BIGINT,
    user_id BIGINT,
    user_name VARCHAR(100),
    user_role VARCHAR(50),
    ip_address VARCHAR(45),
    user_agent VARCHAR(255),
    before_value JSONB,
    after_value JSONB,
    old_values TEXT,
    new_values TEXT,
    changes TEXT,
    changed_fields JSONB,
    description TEXT,
    metadata JSONB,
    request_id VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE otp_requests (
    id BIGSERIAL PRIMARY KEY,
    request_id VARCHAR(100) UNIQUE NOT NULL,
    mobile VARCHAR(15) NOT NULL,
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

CREATE TABLE system_config (
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

-- =====================================================
-- MASTER DATA SERVICE TABLES
-- =====================================================

CREATE TABLE master_data (
    id BIGSERIAL PRIMARY KEY,
    data_type VARCHAR(50) NOT NULL,
    code VARCHAR(100) NOT NULL,
    value VARCHAR(255) NOT NULL,
    display_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_master_data UNIQUE (data_type, code)
);

-- =====================================================
-- CASE SOURCING SERVICE TABLES
-- =====================================================

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

CREATE TABLE cases (
    id BIGSERIAL PRIMARY KEY,
    case_number VARCHAR(50) UNIQUE NOT NULL,
    external_case_id VARCHAR(100),
    loan_id BIGINT NOT NULL,
    case_status VARCHAR(20) DEFAULT 'UNALLOCATED',
    case_priority VARCHAR(20) DEFAULT 'MEDIUM',
    status INTEGER DEFAULT 200,  -- Case lifecycle status: 200 = ACTIVE, 400 = CLOSED
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

-- NOTE: case_notes, case_activities, telecalling_logs, telecalling_history,
-- receipts, payment_transactions, repayments, ots_settlements tables
-- moved to UNUSED_FUTURE_SERVICES_SCRIPTS.sql (no JPA entities implemented)

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

-- Case Closure table - Tracks case closure and reopen history
CREATE TABLE case_closure (
    id BIGSERIAL PRIMARY KEY,
    case_id BIGINT NOT NULL,
    case_number VARCHAR(50),
    loan_id BIGINT,
    loan_account_number VARCHAR(50),
    customer_id BIGINT,
    customer_name VARCHAR(200),
    action VARCHAR(20) NOT NULL,  -- CLOSED or REOPENED
    previous_status INTEGER,       -- Status before action (200 or 400)
    new_status INTEGER NOT NULL,   -- Status after action (200 or 400)
    closure_reason VARCHAR(255),   -- Reason for closure (required for CLOSED action)
    outstanding_amount NUMERIC(15, 2),
    dpd INTEGER,
    bucket VARCHAR(20),
    closed_by BIGINT,
    closed_by_name VARCHAR(100),
    closed_at TIMESTAMP NOT NULL,
    remarks VARCHAR(500),
    batch_id VARCHAR(100),  -- For bulk closure tracking
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_action CHECK (action IN ('CLOSED', 'REOPENED')),
    CONSTRAINT chk_new_status CHECK (new_status IN (200, 400)),
    CONSTRAINT chk_previous_status CHECK (previous_status IS NULL OR previous_status IN (200, 400))
);

-- =====================================================
-- ALLOCATION REALLOCATION SERVICE TABLES
-- =====================================================

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

-- NOTE: reallocation_jobs table moved to UNUSED_FUTURE_SERVICES_SCRIPTS.sql (no JPA entity)

-- =====================================================
-- COMMUNICATION SERVICE TABLES
-- =====================================================

-- NOTE: communication_providers table moved to UNUSED_FUTURE_SERVICES_SCRIPTS.sql (no JPA entity)

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

CREATE TABLE voice_call_logs (
    id BIGSERIAL PRIMARY KEY,
    call_id VARCHAR(100) UNIQUE NOT NULL,
    provider_call_id VARCHAR(100),
    provider VARCHAR(50) DEFAULT 'MSG91',
    agent_id BIGINT,
    case_id BIGINT,
    user_id BIGINT,
    customer_mobile VARCHAR(15) NOT NULL,
    caller_id VARCHAR(15),
    call_type VARCHAR(30) NOT NULL,
    template VARCHAR(100),
    call_status VARCHAR(50),
    call_duration INTEGER,
    recording_url TEXT,
    disposition VARCHAR(100),
    notes TEXT,
    initiated_at TIMESTAMP,
    answered_at TIMESTAMP,
    ended_at TIMESTAMP,
    provider_response JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
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
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- NOTE: communication_webhooks table moved to UNUSED_FUTURE_SERVICES_SCRIPTS.sql (no JPA entity)

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
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT
);

-- =====================================================
-- STRATEGY ENGINE SERVICE TABLES
-- =====================================================

CREATE TABLE strategies (
    id BIGSERIAL PRIMARY KEY,
    strategy_code VARCHAR(50) UNIQUE NOT NULL,
    strategy_name VARCHAR(255) NOT NULL,
    strategy_type VARCHAR(20) NOT NULL,
    description TEXT,
    trigger_frequency VARCHAR(20),
    trigger_time TIME NULL,
    trigger_days VARCHAR(50) NULL,
    is_active BOOLEAN DEFAULT TRUE,
    priority INTEGER DEFAULT 0,
    effective_from DATE,
    effective_to DATE,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'DRAFT',
    last_run_at TIMESTAMP NULL,
    success_count INTEGER DEFAULT 0,
    failure_count INTEGER DEFAULT 0,
    schedule_expression VARCHAR(100),
    event_type VARCHAR(50),
    updated_by BIGINT
);

CREATE TABLE strategy_rules (
    id BIGSERIAL PRIMARY KEY,
    strategy_id BIGINT NOT NULL,
    rule_name VARCHAR(255) NOT NULL,
    rule_order INTEGER DEFAULT 0,
    conditions JSONB NOT NULL,
    logical_operator VARCHAR(5) DEFAULT 'AND',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    field_name VARCHAR(100),
    operator VARCHAR(50),
    field_value TEXT
);

CREATE TABLE strategy_actions (
    id BIGSERIAL PRIMARY KEY,
    strategy_id BIGINT NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    action_order INTEGER DEFAULT 0,
    action_config JSONB NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    template_id VARCHAR(100),
    channel VARCHAR(50),
    priority INTEGER DEFAULT 0,
    variable_mapping JSONB
);

CREATE TABLE strategy_executions (
    id BIGSERIAL PRIMARY KEY,
    strategy_id BIGINT NOT NULL,
    execution_type VARCHAR(20),
    total_records_evaluated INTEGER DEFAULT 0,
    records_matched INTEGER DEFAULT 0,
    records_processed INTEGER DEFAULT 0,
    records_failed INTEGER DEFAULT 0,
    execution_status VARCHAR(20),
    error_message TEXT,
    execution_log JSONB,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    executed_by BIGINT,
    execution_id VARCHAR(100) UNIQUE,
    strategy_name VARCHAR(255),
    estimated_cases_affected INTEGER,
    successful_actions INTEGER DEFAULT 0,
    failed_actions INTEGER DEFAULT 0,
    execution_metadata JSONB,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- NOTE: strategy_execution_details table moved to UNUSED_FUTURE_SERVICES_SCRIPTS.sql (no JPA entity)

CREATE TABLE scheduled_jobs (
    id BIGSERIAL PRIMARY KEY,
    service_name VARCHAR(100) NOT NULL,
    job_name VARCHAR(255) NOT NULL,
    job_type VARCHAR(50) NOT NULL,
    job_reference_id BIGINT NULL,
    job_reference_type VARCHAR(50) NULL,
    is_enabled BOOLEAN DEFAULT FALSE,
    schedule_type VARCHAR(20) NOT NULL,
    schedule_time TIME NULL,
    schedule_days VARCHAR(100) NULL,
    cron_expression VARCHAR(100) NULL,
    timezone VARCHAR(50) DEFAULT 'Asia/Kolkata',
    next_run_at TIMESTAMP NULL,
    last_run_at TIMESTAMP NULL,
    last_run_status VARCHAR(20) NULL,
    last_run_message TEXT NULL,
    run_count INTEGER DEFAULT 0,
    failure_count INTEGER DEFAULT 0,
    avg_execution_time_ms BIGINT NULL,
    job_config JSONB NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

CREATE TABLE filter_fields (
    id BIGSERIAL PRIMARY KEY,
    field_code VARCHAR(100) UNIQUE NOT NULL,
    field_key VARCHAR(100) NOT NULL,
    field_type VARCHAR(20) NOT NULL CHECK (field_type IN ('TEXT', 'NUMERIC', 'DATE')),
    display_name VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    is_attribute BOOLEAN DEFAULT FALSE,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

CREATE TABLE filter_field_options (
    id BIGSERIAL PRIMARY KEY,
    filter_field_id BIGINT NOT NULL REFERENCES filter_fields(id) ON DELETE CASCADE,
    option_value VARCHAR(255) NOT NULL,
    option_label VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_field_option UNIQUE(filter_field_id, option_value)
);

CREATE TABLE master_cities (
    id BIGSERIAL PRIMARY KEY,
    city_name VARCHAR(255) NOT NULL UNIQUE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE master_states (
    id BIGSERIAL PRIMARY KEY,
    state_name VARCHAR(255) NOT NULL UNIQUE,
    state_code VARCHAR(10),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE master_pincodes (
    id BIGSERIAL PRIMARY KEY,
    pincode VARCHAR(10) NOT NULL UNIQUE,
    city_id BIGINT REFERENCES master_cities(id) ON DELETE SET NULL,
    state_id BIGINT REFERENCES master_states(id) ON DELETE SET NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- TEMPLATE MANAGEMENT SERVICE TABLES
-- =====================================================

-- Templates table (matches Template.java entity)
CREATE TABLE templates (
    id BIGSERIAL PRIMARY KEY,
    template_name VARCHAR(100) NOT NULL,
    template_code VARCHAR(50) UNIQUE NOT NULL,
    channel VARCHAR(20) NOT NULL,
    provider VARCHAR(50),
    provider_template_id VARCHAR(100),
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Template Content table (matches TemplateContent.java entity)
CREATE TABLE template_content (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL REFERENCES templates(id) ON DELETE CASCADE,
    language_code VARCHAR(10) DEFAULT 'en',
    subject VARCHAR(255),
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_template_language UNIQUE (template_id, language_code)
);

-- Template Variables table (matches TemplateVariable.java entity)
CREATE TABLE template_variables (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL REFERENCES templates(id) ON DELETE CASCADE,
    variable_name VARCHAR(50) NOT NULL,
    variable_key VARCHAR(50) NOT NULL,
    data_type VARCHAR(20) DEFAULT 'TEXT',
    default_value TEXT,
    is_required BOOLEAN DEFAULT FALSE,
    description VARCHAR(255),
    display_order INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_template_variable UNIQUE (template_id, variable_name)
);

-- NOTE: campaign_templates, campaigns, campaign_executions tables
-- moved to UNUSED_FUTURE_SERVICES_SCRIPTS.sql (no JPA entities implemented)

CREATE TABLE variable_definitions (
    id BIGSERIAL PRIMARY KEY,
    variable_key VARCHAR(100) UNIQUE NOT NULL,
    display_name VARCHAR(200) NOT NULL,
    entity_path VARCHAR(200) NOT NULL,
    data_type VARCHAR(50) NOT NULL,
    default_value VARCHAR(500),
    transformer VARCHAR(50),
    description TEXT,
    category VARCHAR(50),
    example_value VARCHAR(200),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- =====================================================
-- TABLE COMMENTS
-- =====================================================

COMMENT ON TABLE users IS 'User accounts for the platform';
COMMENT ON TABLE user_groups IS 'User groups for organizing users';
COMMENT ON TABLE role_groups IS 'Groups for organizing roles';
COMMENT ON TABLE roles IS 'User roles and permissions';
COMMENT ON TABLE permissions IS 'Individual permissions that can be assigned to roles';
COMMENT ON TABLE user_roles IS 'User-role assignments';
COMMENT ON TABLE role_permissions IS 'Role-permission assignments';
COMMENT ON TABLE user_sessions IS 'User session tracking';
COMMENT ON TABLE audit_logs IS 'Audit log for tracking changes';
COMMENT ON TABLE otp_requests IS 'OTP generation and verification requests';
COMMENT ON TABLE system_config IS 'System configuration key-value pairs';
COMMENT ON TABLE third_party_integration_master IS 'Third-party integration configurations';
COMMENT ON TABLE master_data IS 'Centralized master data repository';
COMMENT ON TABLE customers IS 'Customer master data';
COMMENT ON TABLE loan_details IS 'Loan account details';
COMMENT ON TABLE cases IS 'Collection cases';
COMMENT ON TABLE case_batches IS 'Batch upload tracking';
COMMENT ON TABLE batch_errors IS 'Validation errors from batch uploads';
COMMENT ON TABLE ptp_commitments IS 'Promise to Pay commitments';
COMMENT ON TABLE case_closure IS 'Tracks case closure and reopen history for audit trail';
COMMENT ON COLUMN cases.status IS 'Case lifecycle status: 200 = ACTIVE, 400 = CLOSED';
COMMENT ON COLUMN case_closure.action IS 'Action type: CLOSED or REOPENED';
COMMENT ON COLUMN case_closure.previous_status IS 'Case status before this action (200=ACTIVE, 400=CLOSED)';
COMMENT ON COLUMN case_closure.new_status IS 'Case status after this action (200=ACTIVE, 400=CLOSED)';
COMMENT ON COLUMN case_closure.batch_id IS 'Batch ID for bulk closure operations';
COMMENT ON COLUMN case_closure.outstanding_amount IS 'Outstanding amount at the time of closure';
COMMENT ON COLUMN case_closure.dpd IS 'Days Past Due at the time of closure';
COMMENT ON COLUMN case_closure.bucket IS 'Bucket classification at the time of closure';
COMMENT ON TABLE allocations IS 'Case allocations to agents/agencies';
COMMENT ON TABLE allocation_history IS 'Allocation change history';
COMMENT ON TABLE allocation_batches IS 'Bulk allocation tracking';
COMMENT ON TABLE allocation_rules IS 'Automatic allocation rules';
COMMENT ON TABLE contact_update_batches IS 'Contact update batch tracking';
COMMENT ON TABLE sms_messages IS 'SMS message tracking';
COMMENT ON TABLE email_messages IS 'Email message tracking';
COMMENT ON TABLE whatsapp_messages IS 'WhatsApp message tracking';
COMMENT ON TABLE voice_call_logs IS 'Voice call logs';
COMMENT ON TABLE dialer_call_logs IS 'Dialer call logs';
COMMENT ON TABLE payment_gateway_transactions IS 'Payment gateway transactions';
COMMENT ON TABLE strategies IS 'Strategy definitions';
COMMENT ON TABLE strategy_rules IS 'Strategy rules and conditions';
COMMENT ON TABLE strategy_actions IS 'Strategy actions';
COMMENT ON TABLE strategy_executions IS 'Strategy execution history';
COMMENT ON TABLE scheduled_jobs IS 'Scheduled job configurations';
COMMENT ON TABLE filter_fields IS 'Filter field metadata';
COMMENT ON TABLE filter_field_options IS 'Filter field options';
COMMENT ON TABLE master_cities IS 'Master city data';
COMMENT ON TABLE master_states IS 'Master state data';
COMMENT ON TABLE master_pincodes IS 'Master pincode data';
COMMENT ON TABLE templates IS 'Communication templates';
COMMENT ON TABLE template_content IS 'Template content with multilingual support';
COMMENT ON TABLE template_variables IS 'Template variables and placeholders';
COMMENT ON TABLE variable_definitions IS 'Template variable definitions';
COMMENT ON COLUMN customers.language_preference IS 'Preferred language for customer communication';
