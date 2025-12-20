-- =====================================================
-- CREDIT ENFORCEMENT PLATFORM - NEW SERVICES TRIGGERS
-- Agency Management, My Workflow, Collections, Notice Management,
-- Configurations, DMS Service Triggers
-- =====================================================

-- Note: The update_updated_at_column() function already exists in V1_2

-- =====================================================
-- AGENCY MANAGEMENT SERVICE TRIGGERS
-- =====================================================

DROP TRIGGER IF EXISTS update_agencies_updated_at ON agencies;
CREATE TRIGGER update_agencies_updated_at
    BEFORE UPDATE ON agencies
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_agency_users_updated_at ON agency_users;
CREATE TRIGGER update_agency_users_updated_at
    BEFORE UPDATE ON agency_users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_agency_case_allocations_updated_at ON agency_case_allocations;
CREATE TRIGGER update_agency_case_allocations_updated_at
    BEFORE UPDATE ON agency_case_allocations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- MY WORKFLOW SERVICE TRIGGERS
-- =====================================================

DROP TRIGGER IF EXISTS update_workflow_queues_updated_at ON workflow_queues;
CREATE TRIGGER update_workflow_queues_updated_at
    BEFORE UPDATE ON workflow_queues
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_case_notes_updated_at ON case_notes;
CREATE TRIGGER update_case_notes_updated_at
    BEFORE UPDATE ON case_notes
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- COLLECTIONS SERVICE TRIGGERS
-- =====================================================

DROP TRIGGER IF EXISTS update_archival_rules_updated_at ON archival_rules;
CREATE TRIGGER update_archival_rules_updated_at
    BEFORE UPDATE ON archival_rules
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_ots_requests_updated_at ON ots_requests;
CREATE TRIGGER update_ots_requests_updated_at
    BEFORE UPDATE ON ots_requests
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_repayments_updated_at ON repayments;
CREATE TRIGGER update_repayments_updated_at
    BEFORE UPDATE ON repayments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_receipts_updated_at ON receipts;
CREATE TRIGGER update_receipts_updated_at
    BEFORE UPDATE ON receipts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- NOTICE MANAGEMENT SERVICE TRIGGERS
-- =====================================================

DROP TRIGGER IF EXISTS update_notice_vendors_updated_at ON notice_vendors;
CREATE TRIGGER update_notice_vendors_updated_at
    BEFORE UPDATE ON notice_vendors
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_notices_updated_at ON notices;
CREATE TRIGGER update_notices_updated_at
    BEFORE UPDATE ON notices
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- CONFIGURATIONS SERVICE TRIGGERS
-- =====================================================

DROP TRIGGER IF EXISTS update_organizations_updated_at ON organizations;
CREATE TRIGGER update_organizations_updated_at
    BEFORE UPDATE ON organizations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_offices_updated_at ON offices;
CREATE TRIGGER update_offices_updated_at
    BEFORE UPDATE ON offices
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_work_calendars_updated_at ON work_calendars;
CREATE TRIGGER update_work_calendars_updated_at
    BEFORE UPDATE ON work_calendars
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_holidays_updated_at ON holidays;
CREATE TRIGGER update_holidays_updated_at
    BEFORE UPDATE ON holidays
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_currencies_updated_at ON currencies;
CREATE TRIGGER update_currencies_updated_at
    BEFORE UPDATE ON currencies
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_password_policies_updated_at ON password_policies;
CREATE TRIGGER update_password_policies_updated_at
    BEFORE UPDATE ON password_policies
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_approval_workflows_updated_at ON approval_workflows;
CREATE TRIGGER update_approval_workflows_updated_at
    BEFORE UPDATE ON approval_workflows
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- DMS SERVICE TRIGGERS
-- =====================================================

DROP TRIGGER IF EXISTS update_documents_updated_at ON documents;
CREATE TRIGGER update_documents_updated_at
    BEFORE UPDATE ON documents
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
