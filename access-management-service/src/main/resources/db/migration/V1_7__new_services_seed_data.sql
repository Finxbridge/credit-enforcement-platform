-- =====================================================
-- CREDIT ENFORCEMENT PLATFORM - NEW SERVICES SEED DATA
-- Agency Management, My Workflow, Collections, Notice Management,
-- Configurations, DMS Service Seed Data
-- =====================================================

-- =====================================================
-- CONFIGURATIONS SERVICE SEED DATA
-- =====================================================

-- Default currency
INSERT INTO currencies (currency_code, currency_name, currency_symbol, is_base_currency, is_active)
VALUES ('INR', 'Indian Rupee', '₹', TRUE, TRUE)
ON CONFLICT (currency_code) DO NOTHING;

-- Default password policy
INSERT INTO password_policies (
    policy_code,
    policy_name,
    policy_level,
    min_length,
    require_uppercase,
    require_lowercase,
    require_number,
    require_special_char,
    password_history_count,
    password_expiry_days,
    max_failed_attempts,
    lockout_duration_minutes,
    is_default
) VALUES (
    'DEFAULT_STRONG',
    'Default Strong Policy',
    'STRONG',
    8,
    TRUE,
    TRUE,
    TRUE,
    TRUE,
    5,
    90,
    5,
    30,
    TRUE
) ON CONFLICT (policy_code) DO NOTHING;

-- Default work calendar
INSERT INTO work_calendars (
    calendar_code,
    calendar_name,
    working_days,
    work_start_time,
    work_end_time,
    is_default,
    is_active
) VALUES (
    'DEFAULT_CALENDAR',
    'Default Work Calendar',
    '{"monday": true, "tuesday": true, "wednesday": true, "thursday": true, "friday": true, "saturday": true, "sunday": false}'::jsonb,
    '09:00',
    '18:00',
    TRUE,
    TRUE
) ON CONFLICT (calendar_code) DO NOTHING;

-- =====================================================
-- DMS SERVICE SEED DATA
-- =====================================================

-- Document categories
INSERT INTO document_categories (category_code, category_name, description, allowed_file_types, max_file_size_mb, display_order)
VALUES
    ('KYC', 'KYC Documents', 'Know Your Customer documents', '["pdf", "jpg", "jpeg", "png"]'::jsonb, 10, 1),
    ('RECEIPTS', 'Payment Receipts', 'Payment and collection receipts', '["pdf", "jpg", "jpeg", "png"]'::jsonb, 5, 2),
    ('NOTICES', 'Legal Notices', 'Legal and demand notices', '["pdf"]'::jsonb, 20, 3),
    ('SETTLEMENT', 'Settlement Documents', 'OTS and settlement letters', '["pdf"]'::jsonb, 15, 4),
    ('POD', 'Proof of Delivery', 'Notice delivery proofs', '["pdf", "jpg", "jpeg", "png"]'::jsonb, 10, 5),
    ('AGREEMENTS', 'Loan Agreements', 'Loan and contract documents', '["pdf"]'::jsonb, 30, 6),
    ('OTHERS', 'Other Documents', 'Miscellaneous documents', '["pdf", "jpg", "jpeg", "png", "doc", "docx"]'::jsonb, 10, 99)
ON CONFLICT (category_code) DO NOTHING;

-- =====================================================
-- NOTICE MANAGEMENT SERVICE SEED DATA
-- =====================================================

-- Default notice types (added to master_data if not using separate table)
-- These would typically go into master_data table which already exists

-- =====================================================
-- APPROVAL WORKFLOWS SEED DATA
-- =====================================================

-- OTS Approval Workflow
INSERT INTO approval_workflows (
    workflow_code,
    workflow_name,
    workflow_type,
    approval_levels,
    escalation_enabled,
    escalation_hours,
    is_active
) VALUES (
    'OTS_APPROVAL',
    'OTS Approval Workflow',
    'OTS',
    '[
        {"level": 1, "role": "SUPERVISOR", "max_discount_percent": 10},
        {"level": 2, "role": "MANAGER", "max_discount_percent": 25},
        {"level": 3, "role": "SENIOR_MANAGER", "max_discount_percent": 50}
    ]'::jsonb,
    TRUE,
    24,
    TRUE
) ON CONFLICT (workflow_code) DO NOTHING;

-- Repayment Approval Workflow
INSERT INTO approval_workflows (
    workflow_code,
    workflow_name,
    workflow_type,
    approval_levels,
    escalation_enabled,
    escalation_hours,
    is_active
) VALUES (
    'REPAYMENT_APPROVAL',
    'Repayment Approval Workflow',
    'REPAYMENT',
    '[
        {"level": 1, "role": "SUPERVISOR", "max_amount": 50000},
        {"level": 2, "role": "MANAGER", "max_amount": 200000}
    ]'::jsonb,
    TRUE,
    8,
    TRUE
) ON CONFLICT (workflow_code) DO NOTHING;
