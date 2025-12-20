-- =====================================================
-- V1_32: Update Variable Definitions - Fix CSV Column Mapping
-- =====================================================
-- This migration fixes entity paths to match actual CSV columns
-- and adds missing variables from the CSV upload
-- =====================================================

-- Fix customer_address: CSV uses PRIMARY ADDRESS which maps to primaryAddress field
UPDATE variable_definitions
SET entity_path = 'loan.primaryCustomer.primaryAddress',
    description = 'Customer primary address (CSV: PRIMARY ADDRESS)'
WHERE variable_key = 'customer_address';

-- =====================================================
-- ADD NEW CUSTOMER VARIABLES
-- =====================================================

-- Secondary mobile (CSV: SECONDARY MOBILE NUMBER)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'customer_secondary_mobile', 'Secondary Mobile', 'loan.primaryCustomer.secondaryMobileNumber', 'STRING', 'Secondary mobile number (CSV: SECONDARY MOBILE NUMBER)', 'CUSTOMER', '9876543210', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'customer_secondary_mobile');

-- Residential phone (CSV: RESI PHONE)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'customer_resi_phone', 'Residential Phone', 'loan.primaryCustomer.resiPhone', 'STRING', 'Residential phone (CSV: RESI PHONE)', 'CUSTOMER', '4024567890', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'customer_resi_phone');

-- Secondary address (CSV: SECONDARY ADDRESS)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'customer_secondary_address', 'Secondary Address', 'loan.primaryCustomer.secondaryAddress', 'STRING', 'Customer secondary address (CSV: SECONDARY ADDRESS)', 'CUSTOMER', '456 Sub Road', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'customer_secondary_address');

-- Father/Spouse name (CSV: FATHER SPOUSE NAME)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'customer_father_spouse', 'Father/Spouse Name', 'loan.primaryCustomer.fatherSpouseName', 'STRING', 'Father or spouse name (CSV: FATHER SPOUSE NAME)', 'CUSTOMER', 'Ramesh Kumar', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'customer_father_spouse');

-- Employer (CSV: EMPLOYER OR BUSINESS ENTITY)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'customer_employer', 'Employer/Business', 'loan.primaryCustomer.employerOrBusinessEntity', 'STRING', 'Employer or business entity (CSV: EMPLOYER OR BUSINESS ENTITY)', 'CUSTOMER', 'ABC Corp', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'customer_employer');

-- =====================================================
-- ADD REFERENCE VARIABLES
-- =====================================================

INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'reference_1_name', 'Reference 1 Name', 'loan.primaryCustomer.reference1Name', 'STRING', 'Reference 1 name (CSV: REFERENCE 1 NAME)', 'REFERENCE', 'Suresh', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'reference_1_name');

INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'reference_1_number', 'Reference 1 Number', 'loan.primaryCustomer.reference1Number', 'STRING', 'Reference 1 phone (CSV: REFERENCE 1 NUMBER)', 'REFERENCE', '9876543211', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'reference_1_number');

INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'reference_2_name', 'Reference 2 Name', 'loan.primaryCustomer.reference2Name', 'STRING', 'Reference 2 name (CSV: REFERENCE 2 NAME)', 'REFERENCE', 'Priya', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'reference_2_name');

INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'reference_2_number', 'Reference 2 Number', 'loan.primaryCustomer.reference2Number', 'STRING', 'Reference 2 phone (CSV: REFERENCE 2 NUMBER)', 'REFERENCE', '9876543212', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'reference_2_number');

-- =====================================================
-- ADD NEW LOAN VARIABLES
-- =====================================================

-- Lender (CSV: LENDER)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'lender', 'Lender', 'loan.lender', 'STRING', 'Lender name (CSV: LENDER)', 'LOAN', 'A Finance', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'lender');

-- Scheme code (CSV: SCHEME CODE)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'scheme_code', 'Scheme Code', 'loan.schemeCode', 'STRING', 'Scheme code (CSV: SCHEME CODE)', 'LOAN', 'PL001', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'scheme_code');

-- Product sourcing type (CSV: PRODUCT SOURCING TYPE)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'product_sourcing_type', 'Product Sourcing Type', 'loan.productSourcingType', 'STRING', 'Product sourcing type (CSV: PRODUCT SOURCING TYPE)', 'LOAN', 'DSA', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'product_sourcing_type');

-- Co-lender (CSV: CO LENDER)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'co_lender', 'Co-Lender', 'loan.coLender', 'STRING', 'Co-lender name (CSV: CO LENDER)', 'LOAN', 'XYZ Bank', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'co_lender');

-- Reference lender (CSV: REFERENCE LENDER)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'reference_lender', 'Reference Lender', 'loan.referenceLender', 'STRING', 'Reference lender (CSV: REFERENCE LENDER)', 'LOAN', 'ABC Bank', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'reference_lender');

-- =====================================================
-- ADD LOAN AMOUNT VARIABLES
-- =====================================================

-- POS (CSV: POS)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'pos', 'POS', 'loan.pos', 'DECIMAL', 'Principal Outstanding (CSV: POS)', 'LOAN', '95000.00', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'pos');

-- TOS (CSV: TOS)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'tos', 'TOS', 'loan.tos', 'DECIMAL', 'Total Outstanding (CSV: TOS)', 'LOAN', '120000.00', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'tos');

-- Loan amount (CSV: LOAN AMOUNT OR LIMIT)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'loan_amount', 'Loan Amount', 'loan.loanAmount', 'DECIMAL', 'Loan amount or limit (CSV: LOAN AMOUNT OR LIMIT)', 'LOAN', '500000.00', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'loan_amount');

-- Charges (CSV: CHARGES)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'charges', 'Charges', 'loan.charges', 'DECIMAL', 'Charges (CSV: CHARGES)', 'LOAN', '2000.00', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'charges');

-- OD Interest (CSV: OD INTEREST)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'od_interest', 'OD Interest', 'loan.odInterest', 'DECIMAL', 'Overdue interest (CSV: OD INTEREST)', 'LOAN', '3000.00', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'od_interest');

-- Principal overdue (CSV: PRINCIPAL OVERDUE)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'principal_overdue', 'Principal Overdue', 'loan.principalOverdue', 'DECIMAL', 'Principal overdue (CSV: PRINCIPAL OVERDUE)', 'LOAN', '80000.00', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'principal_overdue');

-- Interest overdue (CSV: INTEREST OVERDUE)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'interest_overdue', 'Interest Overdue', 'loan.interestOverdue', 'DECIMAL', 'Interest overdue (CSV: INTEREST OVERDUE)', 'LOAN', '30000.00', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'interest_overdue');

-- Fees overdue (CSV: FEES OVERDUE)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'fees_overdue', 'Fees Overdue', 'loan.feesOverdue', 'DECIMAL', 'Fees overdue (CSV: FEES OVERDUE)', 'LOAN', '10000.00', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'fees_overdue');

-- Penalty overdue (CSV: PENALTY OVERDUE)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'penalty_overdue', 'Penalty Overdue', 'loan.penaltyOverdue', 'DECIMAL', 'Penalty overdue (CSV: PENALTY OVERDUE)', 'LOAN', '5000.00', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'penalty_overdue');

-- ROI (CSV: ROI)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'roi', 'ROI', 'loan.roi', 'DECIMAL', 'Rate of interest (CSV: ROI)', 'LOAN', '12.5', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'roi');

-- Loan duration (CSV: LOAN DURATION)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'loan_duration', 'Loan Duration', 'loan.loanDuration', 'STRING', 'Loan duration (CSV: LOAN DURATION)', 'LOAN', '24 months', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'loan_duration');

-- =====================================================
-- ADD EMI VARIABLES
-- =====================================================

-- EMI start date (CSV: EMI START DATE)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'emi_start_date', 'EMI Start Date', 'loan.emiStartDate', 'DATE', 'EMI start date (CSV: EMI START DATE)', 'LOAN', '2024-01-15', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'emi_start_date');

-- No of paid EMI (CSV: NO OF PAID EMI)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'no_of_paid_emi', 'No of Paid EMI', 'loan.noOfPaidEmi', 'INTEGER', 'Number of paid EMIs (CSV: NO OF PAID EMI)', 'LOAN', '10', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'no_of_paid_emi');

-- No of pending EMI (CSV: NO OF PENDING EMI)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'no_of_pending_emi', 'No of Pending EMI', 'loan.noOfPendingEmi', 'INTEGER', 'Number of pending EMIs (CSV: NO OF PENDING EMI)', 'LOAN', '14', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'no_of_pending_emi');

-- EMI overdue from (CSV: Emi Overdue From)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'emi_overdue_from', 'EMI Overdue From', 'loan.emiOverdueFrom', 'DATE', 'EMI overdue from date (CSV: Emi Overdue From)', 'LOAN', '2024-10-15', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'emi_overdue_from');

-- Next EMI date (CSV: Next EMI Date)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'next_emi_date', 'Next EMI Date', 'loan.nextEmiDate', 'DATE', 'Next EMI date (CSV: Next EMI Date)', 'LOAN', '2025-01-15', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'next_emi_date');

-- =====================================================
-- ADD DATE VARIABLES
-- =====================================================

-- Writeoff date (CSV: WRITEOFF DATE)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'writeoff_date', 'Writeoff Date', 'loan.writeoffDate', 'DATE', 'Writeoff date (CSV: WRITEOFF DATE)', 'LOAN', '2025-01-01', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'writeoff_date');

-- =====================================================
-- ADD DPD & BUCKET VARIABLES
-- =====================================================

-- Risk bucket (CSV: RISK BUCKET)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'risk_bucket', 'Risk Bucket', 'loan.riskBucket', 'STRING', 'Risk bucket (CSV: RISK BUCKET)', 'LOAN', 'X', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'risk_bucket');

-- SOM bucket (CSV: SOM BUCKET)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'som_bucket', 'SOM Bucket', 'loan.somBucket', 'STRING', 'SOM bucket (CSV: SOM BUCKET)', 'LOAN', 'B1', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'som_bucket');

-- SOM DPD (CSV: SOM DPD)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'som_dpd', 'SOM DPD', 'loan.somDpd', 'INTEGER', 'SOM DPD (CSV: SOM DPD)', 'LOAN', '30', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'som_dpd');

-- Cycle due (CSV: CYCLE DUE)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'cycle_due', 'Cycle Due', 'loan.cycleDue', 'STRING', 'Cycle due (CSV: CYCLE DUE)', 'LOAN', '2', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'cycle_due');

-- =====================================================
-- ADD PAYMENT VARIABLES
-- =====================================================

-- Last payment date (CSV: LAST PAYMENT DATE)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'last_payment_date', 'Last Payment Date', 'loan.lastPaymentDate', 'DATE', 'Last payment date (CSV: LAST PAYMENT DATE)', 'LOAN', '2024-11-01', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'last_payment_date');

-- Last payment mode (CSV: LAST PAYMENT MODE)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'last_payment_mode', 'Last Payment Mode', 'loan.lastPaymentMode', 'STRING', 'Last payment mode (CSV: LAST PAYMENT MODE)', 'LOAN', 'NEFT', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'last_payment_mode');

-- Last paid amount (CSV: LAST PAID AMOUNT)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'last_paid_amount', 'Last Paid Amount', 'loan.lastPaidAmount', 'DECIMAL', 'Last paid amount (CSV: LAST PAID AMOUNT)', 'LOAN', '12000.00', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'last_paid_amount');

-- =====================================================
-- ADD CASE VARIABLES
-- =====================================================

-- Location (CSV: LOCATION)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'location', 'Location', 'location', 'STRING', 'Location (CSV: LOCATION)', 'CASE', 'Telangana', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'location');

-- Zone (CSV: ZONE)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'zone', 'Zone', 'zone', 'STRING', 'Zone (CSV: ZONE)', 'CASE', 'WEST', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'zone');

-- Primary agent (CSV: PRIMARY AGENT)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'primary_agent', 'Primary Agent', 'primaryAgent', 'STRING', 'Primary agent (CSV: PRIMARY AGENT)', 'CASE', 'Agent1', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'primary_agent');

-- Secondary agent (CSV: SECONDARY AGENT)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'secondary_agent', 'Secondary Agent', 'secondaryAgent', 'STRING', 'Secondary agent (CSV: SECONDARY AGENT)', 'CASE', 'Agent2', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'secondary_agent');

-- Agency name (CSV: AGENCY NAME)
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'agency_name', 'Agency Name', 'agencyName', 'STRING', 'Agency name (CSV: AGENCY NAME)', 'CASE', 'ABC Agency', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'agency_name');

-- =====================================================
-- ADD ASSET VARIABLES
-- =====================================================

INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'asset_details', 'Asset Details', 'assetDetails', 'STRING', 'Asset details (CSV: ASSET DETAILS)', 'ASSET', 'Honda City', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'asset_details');

INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'vehicle_registration_number', 'Vehicle Registration Number', 'vehicleRegistrationNumber', 'STRING', 'Vehicle registration (CSV: VEHICLE REGISTRATION NUMBER)', 'ASSET', 'TS09AB1234', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'vehicle_registration_number');

INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'chassis_number', 'Chassis Number', 'chassisNumber', 'STRING', 'Chassis number (CSV: CHASSIS NUMBER)', 'ASSET', 'CH123456', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'chassis_number');

INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'model_make', 'Model Make', 'modelMake', 'STRING', 'Model make (CSV: MODEL MAKE)', 'ASSET', 'Honda', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'model_make');

INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'dealer_name', 'Dealer Name', 'dealerName', 'STRING', 'Dealer name (CSV: DEALER NAME)', 'ASSET', 'ABC Motors', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'dealer_name');

INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active)
SELECT 'dealer_address', 'Dealer Address', 'dealerAddress', 'STRING', 'Dealer address (CSV: DEALER ADDRESS)', 'ASSET', '123 Dealer Street', true
WHERE NOT EXISTS (SELECT 1 FROM variable_definitions WHERE variable_key = 'dealer_address');

-- =====================================================
-- ADD MISSING COLUMN TO NOTICES TABLE
-- =====================================================
-- Required by my-workflow-service Notice entity

ALTER TABLE notices ADD COLUMN IF NOT EXISTS sent_at TIMESTAMP;

-- Add index for sent_at column for better query performance
CREATE INDEX IF NOT EXISTS idx_notices_sent_at ON notices(sent_at);
