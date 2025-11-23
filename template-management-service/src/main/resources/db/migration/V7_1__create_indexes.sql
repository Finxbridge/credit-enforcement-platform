-- TEMPLATE MANAGEMENT SERVICE - INDEXES
CREATE INDEX idx_campaign_templates_type ON campaign_templates(template_type);
CREATE INDEX idx_campaign_templates_code ON campaign_templates(template_code);
CREATE INDEX idx_campaign_templates_active ON campaign_templates(is_active);

CREATE INDEX idx_campaigns_status ON campaigns(campaign_status);
CREATE INDEX idx_campaigns_template_id ON campaigns(template_id);
CREATE INDEX idx_campaigns_scheduled_at ON campaigns(scheduled_at);

CREATE INDEX idx_campaign_executions_campaign_id ON campaign_executions(campaign_id);
CREATE INDEX idx_campaign_executions_delivery_status ON campaign_executions(delivery_status);
CREATE INDEX idx_campaign_executions_case_id ON campaign_executions(case_id);
