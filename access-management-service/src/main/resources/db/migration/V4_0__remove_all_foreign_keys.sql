-- ===================================================
-- Remove All Foreign Key Constraints
-- Version: 4.0
-- Description: Removes all FK constraints to prepare for microservices database separation
-- Reason: Moving to service-per-database pattern with application-level referential integrity
-- ===================================================

-- ===================================================
-- SECTION 1: ACCESS MANAGEMENT SERVICE DOMAIN
-- Remove FKs within user/role/permission domain
-- ===================================================

-- User Groups self-referential FK
ALTER TABLE user_groups DROP CONSTRAINT IF EXISTS fk_user_groups_parent;

-- Roles → Role Groups FK
ALTER TABLE roles DROP CONSTRAINT IF EXISTS fk_roles_role_group;

-- User-Role junction table FKs
ALTER TABLE user_roles DROP CONSTRAINT IF EXISTS fk_user_roles_user;
ALTER TABLE user_roles DROP CONSTRAINT IF EXISTS fk_user_roles_role;

-- Role-Permission junction table FKs
ALTER TABLE role_permissions DROP CONSTRAINT IF EXISTS fk_role_permissions_role;
ALTER TABLE role_permissions DROP CONSTRAINT IF EXISTS fk_role_permissions_permission;

COMMENT ON TABLE user_roles IS 'FK removed: Validate user_id and role_id via application layer';
COMMENT ON TABLE role_permissions IS 'FK removed: Validate role_id and permission_id via application layer';

-- ===================================================
-- SECTION 2: REPORTING SERVICE DOMAIN
-- Remove FKs within reporting domain
-- ===================================================

-- Generated Reports → Scheduled Reports FK
ALTER TABLE generated_reports DROP CONSTRAINT IF EXISTS fk_generated_report_schedule;

COMMENT ON TABLE generated_reports IS 'FK removed: Validate schedule_id via application layer';

-- ===================================================
-- SECTION 3: CASE SOURCING SERVICE DOMAIN
-- Remove FKs within case/customer/loan domain
-- ===================================================

-- Loan Details → Customers FKs (primary customer, co-borrower, guarantor)
ALTER TABLE loan_details DROP CONSTRAINT IF EXISTS fk_loan_primary_customer;
ALTER TABLE loan_details DROP CONSTRAINT IF EXISTS fk_loan_co_borrower;
ALTER TABLE loan_details DROP CONSTRAINT IF EXISTS fk_loan_guarantor;

-- Cases → Loan Details FK
ALTER TABLE cases DROP CONSTRAINT IF EXISTS fk_cases_loan;

COMMENT ON TABLE loan_details IS 'FK removed: Validate customer_id references via application layer';
COMMENT ON TABLE cases IS 'FK removed: Validate loan_id via application layer or Feign client';

-- ===================================================
-- SECTION 4: ALLOCATION-REALLOCATION SERVICE DOMAIN (CROSS-SERVICE)
-- Remove FKs that reference case-sourcing-service tables
-- ===================================================

-- Allocations → Cases FK (CROSS-SERVICE REFERENCE)
ALTER TABLE allocations DROP CONSTRAINT IF EXISTS fk_allocations_case;

-- Allocation History → Cases FK (CROSS-SERVICE REFERENCE)
ALTER TABLE allocation_history DROP CONSTRAINT IF EXISTS fk_allocation_history_case;

COMMENT ON TABLE allocations IS 'FK removed: Validate case_id via Feign client to case-sourcing-service';
COMMENT ON TABLE allocation_history IS 'FK removed: Validate case_id via Feign client to case-sourcing-service';

-- ===================================================
-- SECTION 5: STRATEGY ENGINE SERVICE DOMAIN
-- Remove FKs within strategy domain
-- ===================================================

-- Strategy Rules → Strategies FK
ALTER TABLE strategy_rules DROP CONSTRAINT IF EXISTS fk_strategy_rules_strategy;

-- Strategy Actions → Strategies FK
ALTER TABLE strategy_actions DROP CONSTRAINT IF EXISTS fk_strategy_actions_strategy;

-- Strategy Executions → Strategies FK
ALTER TABLE strategy_executions DROP CONSTRAINT IF EXISTS fk_strategy_executions_strategy;

COMMENT ON TABLE strategy_rules IS 'FK removed: Validate strategy_id via application layer';
COMMENT ON TABLE strategy_actions IS 'FK removed: Validate strategy_id via application layer';
COMMENT ON TABLE strategy_executions IS 'FK removed: Validate strategy_id via application layer';

-- ===================================================
-- SECTION 6: CAMPAIGN SERVICE DOMAIN
-- Remove FKs within campaign domain
-- ===================================================

-- Campaign Executions → Campaigns FK
ALTER TABLE campaign_executions DROP CONSTRAINT IF EXISTS fk_campaign_executions_campaign;

COMMENT ON TABLE campaign_executions IS 'FK removed: Validate campaign_id via application layer';

-- ===================================================
-- SECTION 7: AGENCY MANAGEMENT SERVICE DOMAIN
-- Remove FKs within agency domain
-- ===================================================

-- Agency Users → Agencies FK
ALTER TABLE agency_users DROP CONSTRAINT IF EXISTS fk_agency_users_agency;

COMMENT ON TABLE agency_users IS 'FK removed: Validate agency_id via application layer';

-- ===================================================
-- SECTION 8: PAYMENT/REPAYMENT SERVICE DOMAIN
-- Remove FKs within payment domain
-- ===================================================

-- Repayments → Payment Transactions FK
ALTER TABLE repayments DROP CONSTRAINT IF EXISTS fk_repayments_transaction;

-- Repayments → Receipts FK
ALTER TABLE repayments DROP CONSTRAINT IF EXISTS fk_repayments_receipt;

COMMENT ON TABLE repayments IS 'FK removed: Validate transaction_id and receipt_id via application layer';

-- ===================================================
-- SECTION 9: NOTICE SERVICE DOMAIN
-- Remove FKs within notice/dispatch domain
-- ===================================================

-- Notice Dispatch Details → Notice Vendors FK
ALTER TABLE notice_dispatch_details DROP CONSTRAINT IF EXISTS fk_notice_dispatch_vendor;

-- Notices → Notice Dispatch Details FK
ALTER TABLE notices DROP CONSTRAINT IF EXISTS fk_notices_dispatch;

-- Notice Batch Items → Notice Batches FK
ALTER TABLE notice_batch_items DROP CONSTRAINT IF EXISTS fk_notice_batch_items_batch;

-- Notice Batch Items → Notices FK
ALTER TABLE notice_batch_items DROP CONSTRAINT IF EXISTS fk_notice_batch_items_notice;

-- Vendor Performance → Notice Vendors FK
ALTER TABLE vendor_performance DROP CONSTRAINT IF EXISTS fk_vendor_performance_vendor;

-- Notice Events → Notices FK
ALTER TABLE notice_events DROP CONSTRAINT IF EXISTS fk_notice_events_notice;

-- Notice Proof of Delivery → Notices FK
ALTER TABLE notice_proof_of_delivery DROP CONSTRAINT IF EXISTS fk_notice_pod_notice;

COMMENT ON TABLE notice_dispatch_details IS 'FK removed: Validate vendor_id via application layer';
COMMENT ON TABLE notices IS 'FK removed: Validate dispatch_id via application layer';
COMMENT ON TABLE notice_batch_items IS 'FK removed: Validate batch_id and notice_id via application layer';
COMMENT ON TABLE vendor_performance IS 'FK removed: Validate vendor_id via application layer';
COMMENT ON TABLE notice_events IS 'FK removed: Validate notice_id via application layer';
COMMENT ON TABLE notice_proof_of_delivery IS 'FK removed: Validate notice_id via application layer';

-- ===================================================
-- SUMMARY OF CHANGES
-- ===================================================
-- Total Foreign Keys Removed: 30
--
-- NEXT STEPS FOR DEVELOPMENT TEAM:
-- 1. Implement application-level validation in service layer
-- 2. Use @Transactional with proper rollback handling
-- 3. For cross-service references, use Feign clients for validation
-- 4. Implement soft deletes (is_deleted flag) to prevent orphaned records
-- 5. Add database indexes on foreign key columns for performance
-- 6. Consider implementing saga pattern for distributed transactions
-- 7. Use event-driven architecture for eventual consistency
--
-- BENEFITS:
-- ✅ Services can be split into separate databases
-- ✅ Independent deployment and scaling
-- ✅ Service autonomy and loose coupling
-- ✅ Different database technologies per service (polyglot persistence)
-- ✅ Easier testing with mock data
--
-- TRADE-OFFS:
-- ⚠️  No database-level referential integrity
-- ⚠️  Must implement validation in application code
-- ⚠️  Potential for orphaned records if not handled carefully
-- ⚠️  Eventual consistency instead of immediate consistency
-- ===================================================
