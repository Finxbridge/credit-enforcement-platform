-- =====================================================
-- CREDIT ENFORCEMENT PLATFORM - PROVIDER, LMS & AUDIT TABLES
-- Migration V1_8: Provider Configuration, LMS Configuration,
-- Audit Logs, Routing Rules, Dispatch Tracking
-- =====================================================

-- =====================================================
-- PROVIDER CONFIGURATION (Third-party integrations)
-- SMS, WhatsApp, Email, Dialer, Payment Providers
-- =====================================================

CREATE TABLE IF NOT EXISTS providers (
    id BIGSERIAL PRIMARY KEY,
    provider_code VARCHAR(50) UNIQUE NOT NULL,
    provider_name VARCHAR(200) NOT NULL,
    provider_type VARCHAR(30) NOT NULL, -- SMS, WHATSAPP, EMAIL, DIALER, PAYMENT
    description TEXT,
    -- Connection Details
    endpoint_url VARCHAR(500),
    auth_type VARCHAR(30) NOT NULL, -- API_KEY, OAUTH2, BASIC_AUTH
    api_key VARCHAR(500), -- encrypted
    api_secret VARCHAR(500), -- encrypted
    username VARCHAR(100),
    password VARCHAR(500), -- encrypted
    oauth_client_id VARCHAR(200),
    oauth_client_secret VARCHAR(500), -- encrypted
    oauth_token_url VARCHAR(500),
    -- Provider Specific Config
    namespace VARCHAR(100), -- for WhatsApp Business API
    sender_id VARCHAR(50), -- for SMS
    from_email VARCHAR(100), -- for Email
    from_name VARCHAR(100),
    -- Webhook Configuration
    webhook_url VARCHAR(500),
    webhook_secret VARCHAR(255),
    -- Rate Limits
    rate_limit_per_second INTEGER,
    rate_limit_per_minute INTEGER,
    rate_limit_per_day INTEGER,
    -- Additional Configuration (JSONB for flexibility)
    additional_config JSONB,
    headers_config JSONB, -- custom headers
    -- Status & Health
    is_active BOOLEAN DEFAULT TRUE,
    is_default BOOLEAN DEFAULT FALSE,
    priority_order INTEGER DEFAULT 0,
    last_tested_at TIMESTAMP,
    last_test_status VARCHAR(20), -- SUCCESS, FAILED
    last_test_message TEXT,
    last_used_at TIMESTAMP,
    success_count BIGINT DEFAULT 0,
    failure_count BIGINT DEFAULT 0,
    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

-- Provider Test History
CREATE TABLE IF NOT EXISTS provider_test_history (
    id BIGSERIAL PRIMARY KEY,
    provider_id BIGINT NOT NULL REFERENCES providers(id),
    test_type VARCHAR(30) NOT NULL, -- CONNECTIVITY, SEND_TEST, WEBHOOK
    test_status VARCHAR(20) NOT NULL, -- SUCCESS, FAILED, TIMEOUT
    response_time_ms INTEGER,
    response_code INTEGER,
    response_message TEXT,
    error_details TEXT,
    tested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tested_by BIGINT
);

-- =====================================================
-- LMS SOURCING CONFIGURATION
-- Integration with Loan Management Systems
-- =====================================================

CREATE TABLE IF NOT EXISTS lms_configurations (
    id BIGSERIAL PRIMARY KEY,
    lms_code VARCHAR(50) UNIQUE NOT NULL,
    lms_name VARCHAR(200) NOT NULL,
    lms_type VARCHAR(30) NOT NULL, -- FINACLE, FLEXCUBE, CUSTOM_API, FILE_BASED
    description TEXT,
    -- Connection Details
    connection_url VARCHAR(500),
    database_name VARCHAR(100),
    schema_name VARCHAR(100),
    username VARCHAR(100), -- encrypted
    password VARCHAR(500), -- encrypted
    -- API Configuration (for CUSTOM_API type)
    api_endpoint VARCHAR(500),
    api_auth_type VARCHAR(30), -- API_KEY, OAUTH2, BASIC_AUTH
    api_key VARCHAR(500),
    api_secret VARCHAR(500),
    -- File Configuration (for FILE_BASED type)
    file_location VARCHAR(500),
    file_format VARCHAR(20), -- CSV, XLSX, JSON, XML
    file_delimiter VARCHAR(10),
    file_encoding VARCHAR(20) DEFAULT 'UTF-8',
    -- Sync Configuration
    sync_frequency VARCHAR(30) DEFAULT 'DAILY', -- HOURLY, DAILY, WEEKLY, MANUAL
    sync_schedule VARCHAR(100), -- cron expression
    sync_start_time TIME,
    batch_size INTEGER DEFAULT 1000,
    -- Field Mappings
    field_mappings JSONB, -- mapping LMS fields to our fields
    -- Identifiers
    lms_identifier VARCHAR(100), -- unique identifier in LMS
    payment_type_id VARCHAR(50),
    product_types JSONB, -- list of product types to sync
    -- Status & Health
    is_active BOOLEAN DEFAULT TRUE,
    last_sync_at TIMESTAMP,
    last_sync_status VARCHAR(20), -- SUCCESS, PARTIAL, FAILED
    last_sync_message TEXT,
    last_sync_records INTEGER,
    last_sync_duration_seconds INTEGER,
    total_records_synced BIGINT DEFAULT 0,
    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

-- LMS Sync History
CREATE TABLE IF NOT EXISTS lms_sync_history (
    id BIGSERIAL PRIMARY KEY,
    lms_id BIGINT NOT NULL REFERENCES lms_configurations(id),
    sync_type VARCHAR(30) NOT NULL, -- FULL, INCREMENTAL, MANUAL
    sync_status VARCHAR(20) NOT NULL, -- RUNNING, SUCCESS, PARTIAL, FAILED
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    duration_seconds INTEGER,
    total_records INTEGER DEFAULT 0,
    new_records INTEGER DEFAULT 0,
    updated_records INTEGER DEFAULT 0,
    failed_records INTEGER DEFAULT 0,
    skipped_records INTEGER DEFAULT 0,
    error_message TEXT,
    error_details JSONB, -- detailed error logs
    sync_batch_id VARCHAR(100),
    triggered_by VARCHAR(30) DEFAULT 'SCHEDULER', -- SCHEDULER, MANUAL, WEBHOOK
    triggered_by_user BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- AUDIT LOG ENHANCEMENTS (Extend existing audit_logs table)
-- audit_logs table already exists from V1_0, adding new columns
-- =====================================================

-- Add new columns to existing audit_logs table for cross-service audit trail
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS service_name VARCHAR(50);
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS event_type VARCHAR(100);
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS event_category VARCHAR(50);
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS entity_name VARCHAR(255);
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS actor_id BIGINT;
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS actor_type VARCHAR(30) DEFAULT 'USER';
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS actor_name VARCHAR(100);
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS actor_email VARCHAR(100);
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS actor_role VARCHAR(50);
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS change_summary TEXT;
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS correlation_id VARCHAR(100);
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS session_id VARCHAR(100);
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS tags JSONB;
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS severity VARCHAR(20) DEFAULT 'INFO';
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS event_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Update comments for new columns
COMMENT ON COLUMN audit_logs.service_name IS 'Service that generated the audit log';
COMMENT ON COLUMN audit_logs.event_type IS 'Type of event (PROVIDER_CREATED, LMS_SYNC_STARTED, etc.)';
COMMENT ON COLUMN audit_logs.event_category IS 'Category: CONFIGURATION, SECURITY, DATA, COMMUNICATION';
COMMENT ON COLUMN audit_logs.actor_type IS 'Actor type: USER, SYSTEM, WEBHOOK, SCHEDULER';
COMMENT ON COLUMN audit_logs.severity IS 'Severity: INFO, WARNING, ERROR, CRITICAL';

-- =====================================================
-- NOTICE ROUTING RULES
-- Rules for automatic vendor assignment
-- =====================================================

CREATE TABLE IF NOT EXISTS routing_rules (
    id BIGSERIAL PRIMARY KEY,
    rule_code VARCHAR(50) UNIQUE NOT NULL,
    rule_name VARCHAR(200) NOT NULL,
    description TEXT,
    rule_priority INTEGER DEFAULT 0, -- higher = more priority
    -- Matching Criteria (JSONB for flexibility)
    criteria JSONB NOT NULL, -- pincode ranges, regions, notice types, dpd ranges, etc.
    -- Vendor Assignment
    primary_vendor_id BIGINT REFERENCES notice_vendors(id),
    secondary_vendor_id BIGINT REFERENCES notice_vendors(id),
    fallback_vendor_id BIGINT REFERENCES notice_vendors(id),
    -- SLA Configuration
    dispatch_sla_hours INTEGER DEFAULT 24,
    delivery_sla_days INTEGER DEFAULT 7,
    -- Cost Configuration
    max_cost_per_dispatch DECIMAL(10,2),
    -- Status
    is_active BOOLEAN DEFAULT TRUE,
    valid_from DATE,
    valid_until DATE,
    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

-- =====================================================
-- DISPATCH TRACKING
-- Track notice dispatch and delivery status
-- =====================================================

CREATE TABLE IF NOT EXISTS dispatch_tracking (
    id BIGSERIAL PRIMARY KEY,
    tracking_id VARCHAR(100) UNIQUE NOT NULL,
    notice_id BIGINT NOT NULL REFERENCES notices(id),
    notice_number VARCHAR(50),
    vendor_id BIGINT REFERENCES notice_vendors(id),
    vendor_name VARCHAR(255),
    -- Shipment Details
    tracking_number VARCHAR(100),
    carrier_name VARCHAR(100),
    service_type VARCHAR(50), -- SPEED_POST, REGISTERED_POST, COURIER
    -- Status Tracking
    dispatch_status VARCHAR(30) DEFAULT 'PENDING', -- PENDING, DISPATCHED, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, RTO, FAILED
    current_location VARCHAR(255),
    current_status_remarks TEXT,
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    dispatched_at TIMESTAMP,
    picked_up_at TIMESTAMP,
    in_transit_at TIMESTAMP,
    out_for_delivery_at TIMESTAMP,
    delivered_at TIMESTAMP,
    rto_initiated_at TIMESTAMP,
    rto_received_at TIMESTAMP,
    -- Delivery Details
    delivery_attempt_count INTEGER DEFAULT 0,
    last_attempt_at TIMESTAMP,
    last_attempt_status VARCHAR(50),
    last_attempt_remarks TEXT,
    -- SLA Tracking
    expected_dispatch_by TIMESTAMP,
    expected_delivery_by TIMESTAMP,
    dispatch_sla_breached BOOLEAN DEFAULT FALSE,
    delivery_sla_breached BOOLEAN DEFAULT FALSE,
    sla_breach_notified BOOLEAN DEFAULT FALSE,
    -- RTO Details
    rto_reason VARCHAR(255),
    rto_action VARCHAR(50), -- RESEND, MARK_UNDELIVERABLE, ESCALATE
    rto_action_taken_at TIMESTAMP,
    rto_action_taken_by BIGINT,
    -- Proof of Delivery
    pod_id BIGINT,
    pod_uploaded_at TIMESTAMP,
    -- Cost
    dispatch_cost DECIMAL(10,2),
    -- Audit Fields
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT
);

-- Dispatch Status History
CREATE TABLE IF NOT EXISTS dispatch_status_history (
    id BIGSERIAL PRIMARY KEY,
    dispatch_id BIGINT NOT NULL REFERENCES dispatch_tracking(id),
    status VARCHAR(30) NOT NULL,
    location VARCHAR(255),
    remarks TEXT,
    event_timestamp TIMESTAMP NOT NULL,
    source VARCHAR(30) DEFAULT 'SYSTEM', -- SYSTEM, VENDOR_API, MANUAL
    raw_data JSONB, -- raw response from vendor API
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT
);

-- =====================================================
-- SLA BREACH TRACKING
-- Track and manage SLA breaches
-- =====================================================

CREATE TABLE IF NOT EXISTS sla_breaches (
    id BIGSERIAL PRIMARY KEY,
    breach_id VARCHAR(100) UNIQUE NOT NULL,
    breach_type VARCHAR(30) NOT NULL, -- DISPATCH_SLA, DELIVERY_SLA, RESPONSE_SLA
    entity_type VARCHAR(50) NOT NULL, -- Notice, Dispatch, Communication
    entity_id BIGINT NOT NULL,
    entity_reference VARCHAR(100),
    -- Breach Details
    sla_hours INTEGER,
    expected_by TIMESTAMP NOT NULL,
    breached_at TIMESTAMP NOT NULL,
    breach_duration_hours INTEGER,
    -- Responsible Party
    vendor_id BIGINT,
    vendor_name VARCHAR(255),
    assigned_user_id BIGINT,
    assigned_user_name VARCHAR(100),
    -- Escalation
    breach_severity VARCHAR(20) DEFAULT 'LOW', -- LOW, MEDIUM, HIGH, CRITICAL
    is_escalated BOOLEAN DEFAULT FALSE,
    escalated_at TIMESTAMP,
    escalated_to BIGINT,
    escalated_by BIGINT,
    escalation_level INTEGER DEFAULT 0,
    escalation_notes TEXT,
    -- Resolution
    is_resolved BOOLEAN DEFAULT FALSE,
    resolved_at TIMESTAMP,
    resolved_by BIGINT,
    resolution_notes TEXT,
    resolution_action VARCHAR(50), -- WAIVED, PENALTY_APPLIED, VENDOR_NOTIFIED
    penalty_amount DECIMAL(10,2),
    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- INDEXES FOR NEW TABLES
-- =====================================================

-- Provider indexes
CREATE INDEX IF NOT EXISTS idx_providers_type ON providers(provider_type);
CREATE INDEX IF NOT EXISTS idx_providers_active ON providers(is_active);
CREATE INDEX IF NOT EXISTS idx_providers_default ON providers(provider_type, is_default) WHERE is_default = TRUE;
CREATE INDEX IF NOT EXISTS idx_provider_test_history_provider ON provider_test_history(provider_id);
CREATE INDEX IF NOT EXISTS idx_provider_test_history_tested_at ON provider_test_history(tested_at DESC);

-- LMS Configuration indexes
CREATE INDEX IF NOT EXISTS idx_lms_configurations_type ON lms_configurations(lms_type);
CREATE INDEX IF NOT EXISTS idx_lms_configurations_active ON lms_configurations(is_active);
CREATE INDEX IF NOT EXISTS idx_lms_sync_history_lms ON lms_sync_history(lms_id);
CREATE INDEX IF NOT EXISTS idx_lms_sync_history_status ON lms_sync_history(sync_status);
CREATE INDEX IF NOT EXISTS idx_lms_sync_history_started ON lms_sync_history(started_at DESC);

-- Audit Log indexes
CREATE INDEX IF NOT EXISTS idx_audit_logs_service ON audit_logs(service_name);
CREATE INDEX IF NOT EXISTS idx_audit_logs_event_type ON audit_logs(event_type);
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_actor ON audit_logs(actor_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_timestamp ON audit_logs(event_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_audit_logs_correlation ON audit_logs(correlation_id);

-- Routing Rules indexes
CREATE INDEX IF NOT EXISTS idx_routing_rules_active ON routing_rules(is_active);
CREATE INDEX IF NOT EXISTS idx_routing_rules_priority ON routing_rules(rule_priority DESC);
CREATE INDEX IF NOT EXISTS idx_routing_rules_vendor ON routing_rules(primary_vendor_id);

-- Dispatch Tracking indexes
CREATE INDEX IF NOT EXISTS idx_dispatch_tracking_notice ON dispatch_tracking(notice_id);
CREATE INDEX IF NOT EXISTS idx_dispatch_tracking_vendor ON dispatch_tracking(vendor_id);
CREATE INDEX IF NOT EXISTS idx_dispatch_tracking_status ON dispatch_tracking(dispatch_status);
CREATE INDEX IF NOT EXISTS idx_dispatch_tracking_tracking_number ON dispatch_tracking(tracking_number);
CREATE INDEX IF NOT EXISTS idx_dispatch_tracking_sla_breach ON dispatch_tracking(dispatch_sla_breached, delivery_sla_breached);
CREATE INDEX IF NOT EXISTS idx_dispatch_status_history_dispatch ON dispatch_status_history(dispatch_id);

-- SLA Breach indexes
CREATE INDEX IF NOT EXISTS idx_sla_breaches_type ON sla_breaches(breach_type);
CREATE INDEX IF NOT EXISTS idx_sla_breaches_entity ON sla_breaches(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_sla_breaches_vendor ON sla_breaches(vendor_id);
CREATE INDEX IF NOT EXISTS idx_sla_breaches_resolved ON sla_breaches(is_resolved);
CREATE INDEX IF NOT EXISTS idx_sla_breaches_escalated ON sla_breaches(is_escalated);
CREATE INDEX IF NOT EXISTS idx_sla_breaches_severity ON sla_breaches(breach_severity);

-- =====================================================
-- TABLE COMMENTS
-- =====================================================

COMMENT ON TABLE providers IS 'Third-party communication and payment provider configurations';
COMMENT ON TABLE provider_test_history IS 'History of provider connectivity and functionality tests';
COMMENT ON TABLE lms_configurations IS 'Loan Management System integration configurations';
COMMENT ON TABLE lms_sync_history IS 'History of LMS data synchronization jobs';
COMMENT ON TABLE audit_logs IS 'Cross-service audit trail for all significant events';
COMMENT ON TABLE routing_rules IS 'Rules for automatic vendor assignment based on criteria';
COMMENT ON TABLE dispatch_tracking IS 'Real-time tracking of notice dispatch and delivery';
COMMENT ON TABLE dispatch_status_history IS 'Status change history for each dispatch';
COMMENT ON TABLE sla_breaches IS 'SLA breach tracking and escalation management';
