-- ===================================================
-- TEMPLATE MANAGEMENT SERVICE - TABLE CREATION
-- Domain: Templates, Campaigns
-- ===================================================

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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE campaign_templates IS 'Communication templates for campaigns';
COMMENT ON TABLE campaigns IS 'Campaign definitions and configurations';
COMMENT ON TABLE campaign_executions IS 'Campaign execution tracking and delivery status';
