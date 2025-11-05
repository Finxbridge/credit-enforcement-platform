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
    updated_by BIGINT,
    CONSTRAINT fk_user_groups_parent FOREIGN KEY (parent_group_id) REFERENCES user_groups(id) ON DELETE SET NULL
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
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_roles_role_group FOREIGN KEY (role_group_id) REFERENCES role_groups(id) ON DELETE SET NULL
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
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT unique_user_role UNIQUE (user_id, role_id)
);

CREATE TABLE role_permissions (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,
    CONSTRAINT unique_role_permission UNIQUE (role_id, permission_id)
);

CREATE TABLE master_data (
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

CREATE TABLE system_config (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value TEXT NOT NULL,
    data_type VARCHAR(20) DEFAULT 'STRING',
    description VARCHAR(255),
    is_encrypted BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
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

CREATE TABLE user_sessions (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(255) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    access_token VARCHAR(500),
    refresh_token VARCHAR(500),
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
    created_by BIGINT,
    CONSTRAINT fk_generated_report_schedule FOREIGN KEY (schedule_id) REFERENCES scheduled_reports(id) ON DELETE SET NULL
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

CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    customer_code VARCHAR(50) UNIQUE NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    mobile_number VARCHAR(15),
    alternate_mobile VARCHAR(15),
    email VARCHAR(100),
    alternate_email VARCHAR(100),
    pan_number VARCHAR(20),
    aadhar_number VARCHAR(20),
    date_of_birth DATE,
    gender VARCHAR(10),
    occupation VARCHAR(100),
    customer_type VARCHAR(20) DEFAULT 'INDIVIDUAL',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE loan_details (
    id BIGSERIAL PRIMARY KEY,
    loan_account_number VARCHAR(50) UNIQUE NOT NULL,
    bank_code VARCHAR(50),
    product_code VARCHAR(50),
    primary_customer_id BIGINT NOT NULL,
    co_borrower_customer_id BIGINT,
    guarantor_customer_id BIGINT,
    loan_disbursement_date DATE,
    loan_maturity_date DATE,
    principal_amount DECIMAL(15,2),
    interest_rate DECIMAL(5,2),
    tenure_months INTEGER,
    emi_amount DECIMAL(15,2),
    source_system VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_loan_primary_customer FOREIGN KEY (primary_customer_id) REFERENCES customers(id),
    CONSTRAINT fk_loan_co_borrower FOREIGN KEY (co_borrower_customer_id) REFERENCES customers(id),
    CONSTRAINT fk_loan_guarantor FOREIGN KEY (guarantor_customer_id) REFERENCES customers(id)
);

CREATE TABLE cases (
    id BIGSERIAL PRIMARY KEY,
    case_number VARCHAR(50) UNIQUE NOT NULL,
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
    updated_by BIGINT,
    CONSTRAINT fk_cases_loan FOREIGN KEY (loan_id) REFERENCES loan_details(id)
);

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
    CONSTRAINT fk_allocations_case FOREIGN KEY (case_id) REFERENCES cases(id)
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
    CONSTRAINT fk_allocation_history_case FOREIGN KEY (case_id) REFERENCES cases(id)
);

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
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
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
    CONSTRAINT fk_strategy_rules_strategy FOREIGN KEY (strategy_id) REFERENCES strategies(id) ON DELETE CASCADE
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
    CONSTRAINT fk_strategy_actions_strategy FOREIGN KEY (strategy_id) REFERENCES strategies(id) ON DELETE CASCADE
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
    CONSTRAINT fk_strategy_executions_strategy FOREIGN KEY (strategy_id) REFERENCES strategies(id) ON DELETE CASCADE
);

CREATE TABLE campaign_templates (
    id BIGSERIAL PRIMARY KEY,
    template_code VARCHAR(50) UNIQUE NOT NULL,
    template_name VARCHAR(255) NOT NULL,
    template_type VARCHAR(20) NOT NULL,
    subject VARCHAR(500) NULL,
    content TEXT NOT NULL,
    language_code VARCHAR(10) DEFAULT 'en',
    version INTEGER DEFAULT 1,
    is_active BOOLEAN DEFAULT TRUE,
    approved_by BIGINT NULL,
    approved_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT
);

CREATE TABLE campaigns (
    id BIGSERIAL PRIMARY KEY,
    campaign_code VARCHAR(50) UNIQUE NOT NULL,
    campaign_name VARCHAR(255) NOT NULL,
    campaign_type VARCHAR(20) NOT NULL,
    template_id BIGINT NOT NULL,
    target_audience_filter JSONB,
    scheduled_at TIMESTAMP NULL,
    execution_mode VARCHAR(20) DEFAULT 'MANUAL',
    campaign_status VARCHAR(20) DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT
);

CREATE TABLE campaign_executions (
    id BIGSERIAL PRIMARY KEY,
    campaign_id BIGINT NOT NULL,
    case_id BIGINT NOT NULL,
    recipient_type VARCHAR(20),
    recipient_contact VARCHAR(100),
    message_content TEXT,
    delivery_status VARCHAR(20),
    provider_response JSONB,
    delivery_attempts INTEGER DEFAULT 0,
    opened_at TIMESTAMP NULL,
    clicked_at TIMESTAMP NULL,
    sent_at TIMESTAMP NULL,
    delivered_at TIMESTAMP NULL,
    failed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_campaign_executions_campaign FOREIGN KEY (campaign_id) REFERENCES campaigns(id) ON DELETE CASCADE
);

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
    CONSTRAINT fk_agency_users_agency FOREIGN KEY (agency_id) REFERENCES agencies(id) ON DELETE CASCADE,
    CONSTRAINT unique_agency_user UNIQUE (agency_id, user_id)
);

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
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_repayments_transaction FOREIGN KEY (transaction_id) REFERENCES payment_transactions(id),
    CONSTRAINT fk_repayments_receipt FOREIGN KEY (receipt_id) REFERENCES receipts(id)
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

CREATE TABLE dashboard_metrics_cache (
    id BIGSERIAL PRIMARY KEY,
    metric_date DATE NOT NULL,
    metric_type VARCHAR(50) NOT NULL,
    metric_json JSONB NOT NULL,
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_metrics_cache UNIQUE (metric_date, metric_type)
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
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notice_dispatch_vendor FOREIGN KEY (vendor_id) REFERENCES notice_vendors(id)
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
    created_by BIGINT,
    CONSTRAINT fk_notices_dispatch FOREIGN KEY (dispatch_id) REFERENCES notice_dispatch_details(id)
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
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notice_batch_items_batch FOREIGN KEY (batch_id) REFERENCES notice_batches(id) ON DELETE CASCADE,
    CONSTRAINT fk_notice_batch_items_notice FOREIGN KEY (notice_id) REFERENCES notices(id)
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
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vendor_performance_vendor FOREIGN KEY (vendor_id) REFERENCES notice_vendors(id) ON DELETE CASCADE
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
    created_by BIGINT,
    CONSTRAINT fk_notice_events_notice FOREIGN KEY (notice_id) REFERENCES notices(id) ON DELETE CASCADE
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
    uploaded_by BIGINT,
    CONSTRAINT fk_notice_pod_notice FOREIGN KEY (notice_id) REFERENCES notices(id) ON DELETE CASCADE
);

CREATE TABLE cache_config (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);