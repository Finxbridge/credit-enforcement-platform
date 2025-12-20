-- =====================================================
-- V1_14: Case Sourcing CSV Columns Enhancement
-- Adds all columns from client's unified CSV format for:
-- - Case Sourcing
-- - Allocation
-- - Reallocation
-- =====================================================

-- =====================================================
-- 1. CUSTOMERS TABLE ALTERATIONS
-- =====================================================

-- Add new customer fields from CSV
ALTER TABLE customers ADD COLUMN IF NOT EXISTS customer_id VARCHAR(50);
ALTER TABLE customers ADD COLUMN IF NOT EXISTS secondary_mobile_number VARCHAR(15);
ALTER TABLE customers ADD COLUMN IF NOT EXISTS resi_phone VARCHAR(20);
ALTER TABLE customers ADD COLUMN IF NOT EXISTS additional_phone_2 VARCHAR(20);
ALTER TABLE customers ADD COLUMN IF NOT EXISTS primary_address VARCHAR(1000);
ALTER TABLE customers ADD COLUMN IF NOT EXISTS secondary_address VARCHAR(1000);
ALTER TABLE customers ADD COLUMN IF NOT EXISTS father_spouse_name VARCHAR(255);
ALTER TABLE customers ADD COLUMN IF NOT EXISTS employer_or_business_entity VARCHAR(255);
ALTER TABLE customers ADD COLUMN IF NOT EXISTS reference_1_name VARCHAR(255);
ALTER TABLE customers ADD COLUMN IF NOT EXISTS reference_1_number VARCHAR(20);
ALTER TABLE customers ADD COLUMN IF NOT EXISTS reference_2_name VARCHAR(255);
ALTER TABLE customers ADD COLUMN IF NOT EXISTS reference_2_number VARCHAR(20);

-- Extend email column to 255 characters
ALTER TABLE customers ALTER COLUMN email TYPE VARCHAR(255);

-- Comments for customers table
COMMENT ON COLUMN customers.customer_id IS 'External customer ID from CSV';
COMMENT ON COLUMN customers.secondary_mobile_number IS 'Secondary mobile number from CSV';
COMMENT ON COLUMN customers.resi_phone IS 'Residential phone number';
COMMENT ON COLUMN customers.additional_phone_2 IS 'Additional contact phone';
COMMENT ON COLUMN customers.primary_address IS 'Primary address from CSV';
COMMENT ON COLUMN customers.secondary_address IS 'Secondary/alternate address';
COMMENT ON COLUMN customers.father_spouse_name IS 'Father or spouse name';
COMMENT ON COLUMN customers.employer_or_business_entity IS 'Employer or business name';
COMMENT ON COLUMN customers.reference_1_name IS 'Reference 1 contact name';
COMMENT ON COLUMN customers.reference_1_number IS 'Reference 1 phone number';
COMMENT ON COLUMN customers.reference_2_name IS 'Reference 2 contact name';
COMMENT ON COLUMN customers.reference_2_number IS 'Reference 2 phone number';

-- =====================================================
-- 2. LOAN_DETAILS TABLE ALTERATIONS
-- =====================================================

-- Lender information
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS lender VARCHAR(100);
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS co_lender VARCHAR(100);
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS reference_lender VARCHAR(100);

-- Loan amounts
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS loan_amount DECIMAL(15,2);
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS pos DECIMAL(15,2);
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS tos DECIMAL(15,2);
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS charges DECIMAL(15,2);
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS od_interest DECIMAL(15,2);

-- Overdue breakdown
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS principal_overdue DECIMAL(15,2);
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS interest_overdue DECIMAL(15,2);
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS fees_overdue DECIMAL(15,2);
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS penalty_overdue DECIMAL(15,2);

-- EMI details
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS emi_start_date DATE;
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS no_of_paid_emi INTEGER;
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS no_of_pending_emi INTEGER;
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS emi_overdue_from DATE;
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS next_emi_date DATE;

-- Interest rate & Loan duration
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS roi DECIMAL(5,2);
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS loan_duration VARCHAR(50);

-- Writeoff
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS writeoff_date DATE;

-- DPD & Bucket
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS risk_bucket VARCHAR(20);
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS som_bucket VARCHAR(20);
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS som_dpd INTEGER;
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS cycle_due VARCHAR(20);

-- Product & Scheme
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS scheme_code VARCHAR(50);
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS product_sourcing_type VARCHAR(50);

-- Credit card specific
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS minimum_amount_due DECIMAL(15,2);
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS card_outstanding DECIMAL(15,2);
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS statement_date DATE;
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS statement_month VARCHAR(20);
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS card_status VARCHAR(30);
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS last_billed_amount DECIMAL(15,2);
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS last_4_digits VARCHAR(4);

-- Payment information
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS last_payment_date DATE;
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS last_payment_mode VARCHAR(50);
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS last_paid_amount DECIMAL(15,2);

-- Repayment bank details
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS beneficiary_account_number VARCHAR(50);
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS beneficiary_account_name VARCHAR(255);
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS repayment_bank_name VARCHAR(100);
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS repayment_ifsc_code VARCHAR(20);
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS reference_url VARCHAR(500);

-- Block status
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS block_1 VARCHAR(100);
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS block_1_date DATE;
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS block_2 VARCHAR(100);
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS block_2_date DATE;

-- Sourcing
ALTER TABLE loan_details ADD COLUMN IF NOT EXISTS sourcing_rm_name VARCHAR(255);

-- Comments for loan_details table
COMMENT ON COLUMN loan_details.lender IS 'Lender name from CSV';
COMMENT ON COLUMN loan_details.co_lender IS 'Co-lender name';
COMMENT ON COLUMN loan_details.reference_lender IS 'Reference lender';
COMMENT ON COLUMN loan_details.loan_amount IS 'Loan amount or limit';
COMMENT ON COLUMN loan_details.pos IS 'Principal Outstanding';
COMMENT ON COLUMN loan_details.tos IS 'Total Outstanding';
COMMENT ON COLUMN loan_details.charges IS 'Additional charges';
COMMENT ON COLUMN loan_details.od_interest IS 'Overdue interest';
COMMENT ON COLUMN loan_details.principal_overdue IS 'Principal overdue amount';
COMMENT ON COLUMN loan_details.interest_overdue IS 'Interest overdue amount';
COMMENT ON COLUMN loan_details.fees_overdue IS 'Fees overdue amount';
COMMENT ON COLUMN loan_details.penalty_overdue IS 'Penalty overdue amount';
COMMENT ON COLUMN loan_details.emi_start_date IS 'EMI start date';
COMMENT ON COLUMN loan_details.no_of_paid_emi IS 'Number of paid EMIs';
COMMENT ON COLUMN loan_details.no_of_pending_emi IS 'Number of pending EMIs';
COMMENT ON COLUMN loan_details.emi_overdue_from IS 'EMI overdue from date';
COMMENT ON COLUMN loan_details.next_emi_date IS 'Next EMI date';
COMMENT ON COLUMN loan_details.roi IS 'Rate of Interest';
COMMENT ON COLUMN loan_details.loan_duration IS 'Loan duration (e.g., 24 months)';
COMMENT ON COLUMN loan_details.writeoff_date IS 'Writeoff date if applicable';
COMMENT ON COLUMN loan_details.risk_bucket IS 'Risk bucket classification';
COMMENT ON COLUMN loan_details.som_bucket IS 'SOM bucket classification';
COMMENT ON COLUMN loan_details.som_dpd IS 'SOM DPD';
COMMENT ON COLUMN loan_details.cycle_due IS 'Cycle due';
COMMENT ON COLUMN loan_details.scheme_code IS 'Scheme code';
COMMENT ON COLUMN loan_details.product_sourcing_type IS 'Product sourcing type';
COMMENT ON COLUMN loan_details.minimum_amount_due IS 'Minimum amount due (credit card)';
COMMENT ON COLUMN loan_details.card_outstanding IS 'Card outstanding (credit card)';
COMMENT ON COLUMN loan_details.statement_date IS 'Statement date (credit card)';
COMMENT ON COLUMN loan_details.statement_month IS 'Statement month (credit card)';
COMMENT ON COLUMN loan_details.card_status IS 'Card status (credit card)';
COMMENT ON COLUMN loan_details.last_billed_amount IS 'Last billed amount';
COMMENT ON COLUMN loan_details.last_4_digits IS 'Last 4 digits of card';
COMMENT ON COLUMN loan_details.last_payment_date IS 'Last payment date';
COMMENT ON COLUMN loan_details.last_payment_mode IS 'Last payment mode';
COMMENT ON COLUMN loan_details.last_paid_amount IS 'Last paid amount';
COMMENT ON COLUMN loan_details.beneficiary_account_number IS 'Beneficiary account number for repayment';
COMMENT ON COLUMN loan_details.beneficiary_account_name IS 'Beneficiary account name';
COMMENT ON COLUMN loan_details.repayment_bank_name IS 'Repayment bank name';
COMMENT ON COLUMN loan_details.repayment_ifsc_code IS 'Repayment bank IFSC code';
COMMENT ON COLUMN loan_details.reference_url IS 'Reference URL';
COMMENT ON COLUMN loan_details.block_1 IS 'Block 1 status';
COMMENT ON COLUMN loan_details.block_1_date IS 'Block 1 date';
COMMENT ON COLUMN loan_details.block_2 IS 'Block 2 status';
COMMENT ON COLUMN loan_details.block_2_date IS 'Block 2 date';
COMMENT ON COLUMN loan_details.sourcing_rm_name IS 'Sourcing RM name';

-- =====================================================
-- 3. CASES TABLE ALTERATIONS
-- =====================================================

-- Agent allocation
ALTER TABLE cases ADD COLUMN IF NOT EXISTS primary_agent VARCHAR(100);
ALTER TABLE cases ADD COLUMN IF NOT EXISTS secondary_agent VARCHAR(100);
ALTER TABLE cases ADD COLUMN IF NOT EXISTS agency_name VARCHAR(255);

-- Geography
ALTER TABLE cases ADD COLUMN IF NOT EXISTS location VARCHAR(100);
ALTER TABLE cases ADD COLUMN IF NOT EXISTS zone VARCHAR(50);

-- Asset details
ALTER TABLE cases ADD COLUMN IF NOT EXISTS asset_details VARCHAR(500);
ALTER TABLE cases ADD COLUMN IF NOT EXISTS vehicle_registration_number VARCHAR(50);
ALTER TABLE cases ADD COLUMN IF NOT EXISTS vehicle_identification_number VARCHAR(50);
ALTER TABLE cases ADD COLUMN IF NOT EXISTS chassis_number VARCHAR(50);
ALTER TABLE cases ADD COLUMN IF NOT EXISTS model_make VARCHAR(100);
ALTER TABLE cases ADD COLUMN IF NOT EXISTS battery_id VARCHAR(50);

-- Dealer information
ALTER TABLE cases ADD COLUMN IF NOT EXISTS dealer_name VARCHAR(255);
ALTER TABLE cases ADD COLUMN IF NOT EXISTS dealer_address VARCHAR(500);

-- Flags
ALTER TABLE cases ADD COLUMN IF NOT EXISTS review_flag VARCHAR(20);

-- Comments for cases table
COMMENT ON COLUMN cases.primary_agent IS 'Primary agent name/ID from CSV';
COMMENT ON COLUMN cases.secondary_agent IS 'Secondary agent name/ID from CSV';
COMMENT ON COLUMN cases.agency_name IS 'Allocated agency name';
COMMENT ON COLUMN cases.location IS 'Location from CSV';
COMMENT ON COLUMN cases.zone IS 'Zone from CSV';
COMMENT ON COLUMN cases.asset_details IS 'Asset details';
COMMENT ON COLUMN cases.vehicle_registration_number IS 'Vehicle registration number';
COMMENT ON COLUMN cases.vehicle_identification_number IS 'Vehicle identification number';
COMMENT ON COLUMN cases.chassis_number IS 'Chassis number';
COMMENT ON COLUMN cases.model_make IS 'Model/Make of vehicle';
COMMENT ON COLUMN cases.battery_id IS 'Battery ID for EV';
COMMENT ON COLUMN cases.dealer_name IS 'Dealer name';
COMMENT ON COLUMN cases.dealer_address IS 'Dealer address';
COMMENT ON COLUMN cases.review_flag IS 'Review flag status';

-- =====================================================
-- 4. INDEXES FOR NEW COLUMNS
-- =====================================================

-- Customers indexes
CREATE INDEX IF NOT EXISTS idx_customers_customer_id ON customers(customer_id) WHERE customer_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_customers_secondary_mobile ON customers(secondary_mobile_number) WHERE secondary_mobile_number IS NOT NULL;

-- Loan details indexes
CREATE INDEX IF NOT EXISTS idx_loan_lender ON loan_details(lender) WHERE lender IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_loan_scheme_code ON loan_details(scheme_code) WHERE scheme_code IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_loan_risk_bucket ON loan_details(risk_bucket) WHERE risk_bucket IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_loan_som_bucket ON loan_details(som_bucket) WHERE som_bucket IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_loan_som_dpd ON loan_details(som_dpd) WHERE som_dpd IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_loan_emi_start_date ON loan_details(emi_start_date) WHERE emi_start_date IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_loan_last_payment_date ON loan_details(last_payment_date) WHERE last_payment_date IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_loan_card_status ON loan_details(card_status) WHERE card_status IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_loan_writeoff_date ON loan_details(writeoff_date) WHERE writeoff_date IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_loan_product_sourcing_type ON loan_details(product_sourcing_type) WHERE product_sourcing_type IS NOT NULL;

-- Cases indexes
CREATE INDEX IF NOT EXISTS idx_cases_primary_agent ON cases(primary_agent) WHERE primary_agent IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_cases_secondary_agent ON cases(secondary_agent) WHERE secondary_agent IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_cases_agency_name ON cases(agency_name) WHERE agency_name IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_cases_location ON cases(location) WHERE location IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_cases_zone ON cases(zone) WHERE zone IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_cases_vehicle_reg ON cases(vehicle_registration_number) WHERE vehicle_registration_number IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_cases_chassis ON cases(chassis_number) WHERE chassis_number IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_cases_review_flag ON cases(review_flag) WHERE review_flag IS NOT NULL;

-- =====================================================
-- 5. COMPOSITE INDEXES FOR COMMON QUERIES
-- =====================================================

-- DPD + Location filtering
CREATE INDEX IF NOT EXISTS idx_loan_dpd_location ON loan_details(dpd, product_type);

-- Risk bucket + DPD
CREATE INDEX IF NOT EXISTS idx_loan_risk_dpd ON loan_details(risk_bucket, dpd) WHERE risk_bucket IS NOT NULL;

-- Product + DPD filtering
CREATE INDEX IF NOT EXISTS idx_loan_product_dpd ON loan_details(product_type, dpd);
