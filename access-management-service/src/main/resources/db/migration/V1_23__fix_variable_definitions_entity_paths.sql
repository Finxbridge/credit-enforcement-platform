-- =====================================================
-- V1_23: Fix Variable Definitions Entity Paths
-- =====================================================
-- Entity Structure:
--   Case
--     └── loan (LoanDetails)
--           ├── primaryCustomer (Customer)
--           ├── coBorrower (Customer)
--           └── guarantor (Customer)
-- =====================================================

-- First, clear existing variable definitions and insert fresh with correct paths
DELETE FROM variable_definitions;

-- =====================================================
-- CUSTOMER VARIABLES (via loan.primaryCustomer.*)
-- =====================================================
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active) VALUES
('customer_name', 'Customer Name', 'loan.primaryCustomer.fullName', 'STRING', 'Full name of the customer', 'CUSTOMER', 'Naveen Kumar', true),
('customer_mobile', 'Customer Mobile', 'loan.primaryCustomer.mobileNumber', 'STRING', 'Mobile number of the customer', 'CUSTOMER', '9876543210', true),
('customer_alternate_mobile', 'Alternate Mobile', 'loan.primaryCustomer.alternateMobile', 'STRING', 'Alternate mobile number', 'CUSTOMER', '9876543211', true),
('customer_email', 'Customer Email', 'loan.primaryCustomer.email', 'STRING', 'Email address of the customer', 'CUSTOMER', 'naveen@example.com', true),
('customer_alternate_email', 'Alternate Email', 'loan.primaryCustomer.alternateEmail', 'STRING', 'Alternate email address', 'CUSTOMER', 'naveen.alt@example.com', true),
('customer_address', 'Customer Address', 'loan.primaryCustomer.address', 'STRING', 'Customer address', 'CUSTOMER', '123 Main Street, Banjara Hills', true),
('customer_city', 'Customer City', 'loan.primaryCustomer.city', 'STRING', 'Customer city', 'CUSTOMER', 'Hyderabad', true),
('customer_state', 'Customer State', 'loan.primaryCustomer.state', 'STRING', 'Customer state', 'CUSTOMER', 'Telangana', true),
('customer_pincode', 'Customer Pincode', 'loan.primaryCustomer.pincode', 'STRING', 'Customer pincode', 'CUSTOMER', '500034', true),
('customer_pan', 'Customer PAN', 'loan.primaryCustomer.panNumber', 'STRING', 'Customer PAN number', 'CUSTOMER', 'ABCDE1234F', true),
('customer_aadhar', 'Customer Aadhar', 'loan.primaryCustomer.aadharNumber', 'STRING', 'Customer Aadhar number', 'CUSTOMER', '1234-5678-9012', true),
('customer_dob', 'Date of Birth', 'loan.primaryCustomer.dateOfBirth', 'DATE', 'Customer date of birth', 'CUSTOMER', '1990-01-15', true),
('customer_gender', 'Customer Gender', 'loan.primaryCustomer.gender', 'STRING', 'Customer gender', 'CUSTOMER', 'Male', true),
('customer_occupation', 'Customer Occupation', 'loan.primaryCustomer.occupation', 'STRING', 'Customer occupation', 'CUSTOMER', 'Software Engineer', true),
('customer_language', 'Language Preference', 'loan.primaryCustomer.languagePreference', 'STRING', 'Customer language preference', 'CUSTOMER', 'en', true);

-- =====================================================
-- CO-BORROWER VARIABLES (via loan.coBorrower.*)
-- =====================================================
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active) VALUES
('coborrower_name', 'Co-Borrower Name', 'loan.coBorrower.fullName', 'STRING', 'Co-borrower full name', 'CO_BORROWER', 'Priya Kumar', true),
('coborrower_mobile', 'Co-Borrower Mobile', 'loan.coBorrower.mobileNumber', 'STRING', 'Co-borrower mobile number', 'CO_BORROWER', '9876543212', true),
('coborrower_email', 'Co-Borrower Email', 'loan.coBorrower.email', 'STRING', 'Co-borrower email', 'CO_BORROWER', 'priya@example.com', true),
('coborrower_address', 'Co-Borrower Address', 'loan.coBorrower.address', 'STRING', 'Co-borrower address', 'CO_BORROWER', '456 Park Street', true);

-- =====================================================
-- GUARANTOR VARIABLES (via loan.guarantor.*)
-- =====================================================
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active) VALUES
('guarantor_name', 'Guarantor Name', 'loan.guarantor.fullName', 'STRING', 'Guarantor full name', 'GUARANTOR', 'Ramesh Kumar', true),
('guarantor_mobile', 'Guarantor Mobile', 'loan.guarantor.mobileNumber', 'STRING', 'Guarantor mobile number', 'GUARANTOR', '9876543213', true),
('guarantor_email', 'Guarantor Email', 'loan.guarantor.email', 'STRING', 'Guarantor email', 'GUARANTOR', 'ramesh@example.com', true),
('guarantor_address', 'Guarantor Address', 'loan.guarantor.address', 'STRING', 'Guarantor address', 'GUARANTOR', '789 Lake View', true);

-- =====================================================
-- LOAN VARIABLES (via loan.*)
-- =====================================================
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active) VALUES
('loan_account_number', 'Loan Account Number', 'loan.loanAccountNumber', 'STRING', 'Loan account number', 'LOAN', 'LN00001', true),
('bank_code', 'Bank Code', 'loan.bankCode', 'STRING', 'Bank code', 'LOAN', 'HDFC', true),
('product_code', 'Product Code', 'loan.productCode', 'STRING', 'Loan product code', 'LOAN', 'PL001', true),
('product_type', 'Product Type', 'loan.productType', 'STRING', 'Loan product type', 'LOAN', 'Personal Loan', true),
('loan_disbursement_date', 'Disbursement Date', 'loan.loanDisbursementDate', 'DATE', 'Loan disbursement date', 'LOAN', '2023-01-15', true),
('loan_maturity_date', 'Maturity Date', 'loan.loanMaturityDate', 'DATE', 'Loan maturity date', 'LOAN', '2026-01-15', true),
('principal_amount', 'Principal Amount', 'loan.principalAmount', 'DECIMAL', 'Loan principal amount', 'LOAN', '500000.00', true),
('interest_amount', 'Interest Amount', 'loan.interestAmount', 'DECIMAL', 'Interest amount', 'LOAN', '75000.00', true),
('penalty_amount', 'Penalty Amount', 'loan.penaltyAmount', 'DECIMAL', 'Penalty amount', 'LOAN', '5000.00', true),
('total_outstanding', 'Total Outstanding', 'loan.totalOutstanding', 'DECIMAL', 'Total outstanding amount', 'LOAN', '125000.00', true),
('interest_rate', 'Interest Rate', 'loan.interestRate', 'DECIMAL', 'Interest rate percentage', 'LOAN', '12.50', true),
('tenure_months', 'Tenure Months', 'loan.tenureMonths', 'INTEGER', 'Loan tenure in months', 'LOAN', '36', true),
('emi_amount', 'EMI Amount', 'loan.emiAmount', 'DECIMAL', 'Monthly EMI amount', 'LOAN', '16000.00', true),
('dpd', 'Days Past Due', 'loan.dpd', 'INTEGER', 'Days past due', 'LOAN', '30', true),
('bucket', 'Bucket', 'loan.bucket', 'STRING', 'DPD bucket (X, 1, 2, 3, etc.)', 'LOAN', '1', true),
('due_date', 'Due Date', 'loan.dueDate', 'DATE', 'Payment due date', 'LOAN', '2024-01-15', true);

-- =====================================================
-- CASE VARIABLES (direct Case entity fields)
-- =====================================================
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active) VALUES
('case_number', 'Case Number', 'caseNumber', 'STRING', 'Case reference number', 'CASE', 'CASE-2024-001', true),
('case_status', 'Case Status', 'caseStatus', 'STRING', 'Case status', 'CASE', 'ACTIVE', true),
('case_priority', 'Case Priority', 'casePriority', 'STRING', 'Case priority level', 'CASE', 'HIGH', true),
('city_code', 'City Code', 'cityCode', 'STRING', 'City code', 'CASE', 'HYD', true),
('state_code', 'State Code', 'stateCode', 'STRING', 'State code', 'CASE', 'TS', true),
('geography_code', 'Geography Code', 'geographyCode', 'STRING', 'Geography code', 'CASE', 'SOUTH', true),
('ptp_date', 'PTP Date', 'ptpDate', 'DATE', 'Promise to pay date', 'CASE', '2024-01-20', true),
('ptp_amount', 'PTP Amount', 'ptpAmount', 'DECIMAL', 'Promise to pay amount', 'CASE', '25000.00', true),
('ptp_status', 'PTP Status', 'ptpStatus', 'STRING', 'PTP status', 'CASE', 'PENDING', true),
('next_followup_date', 'Next Followup Date', 'nextFollowupDate', 'DATE', 'Next followup date', 'CASE', '2024-01-18', true),
('collection_cycle', 'Collection Cycle', 'collectionCycle', 'STRING', 'Collection cycle', 'CASE', 'JAN-2024', true);

-- =====================================================
-- COMPANY VARIABLES (static values - handled specially)
-- =====================================================
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, default_value, is_active) VALUES
('company_name', 'Company Name', 'static.companyName', 'STRING', 'Company name', 'COMPANY', 'FinXBridge', 'FinXBridge', true),
('company_phone', 'Company Phone', 'static.companyPhone', 'STRING', 'Company contact number', 'COMPANY', '+91-1800-123-4567', '+91-1800-123-4567', true),
('company_email', 'Company Email', 'static.companyEmail', 'STRING', 'Company email address', 'COMPANY', 'support@finxbridge.com', 'support@finxbridge.com', true),
('company_address', 'Company Address', 'static.companyAddress', 'STRING', 'Company address', 'COMPANY', 'Tower A, Tech Park, Hyderabad', 'Tower A, Tech Park, Hyderabad', true),
('company_website', 'Company Website', 'static.companyWebsite', 'STRING', 'Company website URL', 'COMPANY', 'https://finxbridge.com', 'https://finxbridge.com', true);

-- =====================================================
-- DYNAMIC/COMPUTED VARIABLES (handled specially in code)
-- =====================================================
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active) VALUES
('payment_link', 'Payment Link', 'generated.paymentLink', 'STRING', 'Payment link URL', 'DYNAMIC', 'https://pay.finxbridge.com/abc123', true),
('current_date', 'Current Date', 'computed.currentDate', 'DATE', 'Current date', 'DYNAMIC', '2024-01-13', true),
('current_time', 'Current Time', 'computed.currentTime', 'STRING', 'Current time', 'DYNAMIC', '10:30 AM', true),
('notice_number', 'Notice Number', 'generated.noticeNumber', 'STRING', 'Generated notice number', 'DYNAMIC', 'NTC-2024-001', true),
('notice_date', 'Notice Date', 'computed.noticeDate', 'DATE', 'Notice generation date', 'DYNAMIC', '2024-01-13', true);

-- =====================================================
-- WhatsApp-specific body variables (for MSG91 mapping)
-- These map to body_1, body_2, body_3 etc. in WhatsApp templates
-- =====================================================
INSERT INTO variable_definitions (variable_key, display_name, entity_path, data_type, description, category, example_value, is_active) VALUES
('body_1', 'Body Variable 1', 'loan.primaryCustomer.fullName', 'STRING', 'WhatsApp body variable 1 - Customer Name', 'WHATSAPP', 'Naveen Kumar', true),
('body_2', 'Body Variable 2', 'loan.loanAccountNumber', 'STRING', 'WhatsApp body variable 2 - Loan Account', 'WHATSAPP', 'LN00001', true),
('body_3', 'Body Variable 3', 'loan.totalOutstanding', 'DECIMAL', 'WhatsApp body variable 3 - Outstanding', 'WHATSAPP', '125000.00', true),
('body_4', 'Body Variable 4', 'loan.emiAmount', 'DECIMAL', 'WhatsApp body variable 4 - EMI Amount', 'WHATSAPP', '16000.00', true),
('body_5', 'Body Variable 5', 'loan.dueDate', 'DATE', 'WhatsApp body variable 5 - Due Date', 'WHATSAPP', '2024-01-15', true),
('body_6', 'Body Variable 6', 'loan.dpd', 'INTEGER', 'WhatsApp body variable 6 - DPD', 'WHATSAPP', '30', true);

-- =====================================================
-- Verify inserted data
-- =====================================================
-- SELECT variable_key, entity_path, category FROM variable_definitions ORDER BY category, variable_key;

-- =====================================================
-- ENTITY PATH REFERENCE GUIDE
-- =====================================================
--
-- Case (direct fields):
--   caseNumber, caseStatus, casePriority, cityCode, stateCode
--   geographyCode, ptpDate, ptpAmount, ptpStatus, nextFollowupDate
--   collectionCycle
--
-- LoanDetails (via loan.):
--   loan.loanAccountNumber, loan.bankCode, loan.productCode, loan.productType
--   loan.loanDisbursementDate, loan.loanMaturityDate
--   loan.principalAmount, loan.interestAmount, loan.penaltyAmount
--   loan.totalOutstanding, loan.interestRate, loan.tenureMonths
--   loan.emiAmount, loan.dpd, loan.bucket, loan.dueDate
--
-- Customer (via loan.primaryCustomer.):
--   loan.primaryCustomer.fullName, loan.primaryCustomer.mobileNumber
--   loan.primaryCustomer.alternateMobile, loan.primaryCustomer.email
--   loan.primaryCustomer.alternateEmail, loan.primaryCustomer.address
--   loan.primaryCustomer.city, loan.primaryCustomer.state
--   loan.primaryCustomer.pincode, loan.primaryCustomer.panNumber
--   loan.primaryCustomer.aadharNumber, loan.primaryCustomer.dateOfBirth
--   loan.primaryCustomer.gender, loan.primaryCustomer.occupation
--   loan.primaryCustomer.languagePreference
--
-- Co-Borrower (via loan.coBorrower.):
--   loan.coBorrower.fullName, loan.coBorrower.mobileNumber, etc.
--
-- Guarantor (via loan.guarantor.):
--   loan.guarantor.fullName, loan.guarantor.mobileNumber, etc.
--
-- Static (handled in code with default values):
--   static.companyName, static.companyPhone, static.companyEmail
--
-- Dynamic/Computed (generated at runtime):
--   generated.paymentLink, computed.currentDate, generated.noticeNumber
-- =====================================================
