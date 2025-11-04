CREATE INDEX IF NOT EXISTS idx_customers_customer_code ON customers(customer_code);
CREATE INDEX IF NOT EXISTS idx_customers_mobile ON customers(mobile_number);
CREATE INDEX IF NOT EXISTS idx_customers_email ON customers(email);

CREATE INDEX IF NOT EXISTS idx_loan_details_account_number ON loan_details(loan_account_number);
CREATE INDEX IF NOT EXISTS idx_loan_details_primary_customer ON loan_details(primary_customer_id);
CREATE INDEX IF NOT EXISTS idx_loan_details_dpd ON loan_details(dpd);
CREATE INDEX IF NOT EXISTS idx_loan_details_bucket ON loan_details(bucket);
CREATE INDEX IF NOT EXISTS idx_loan_details_product_code ON loan_details(product_code);

CREATE INDEX IF NOT EXISTS idx_cases_case_number ON cases(case_number);
CREATE INDEX IF NOT EXISTS idx_cases_external_case_id ON cases(external_case_id);
CREATE INDEX IF NOT EXISTS idx_cases_case_status ON cases(case_status);
CREATE INDEX IF NOT EXISTS idx_cases_loan_id ON cases(loan_id);
CREATE INDEX IF NOT EXISTS idx_cases_allocated_to_user ON cases(allocated_to_user_id);
CREATE INDEX IF NOT EXISTS idx_cases_import_batch_id ON cases(import_batch_id);
CREATE INDEX IF NOT EXISTS idx_cases_created_at ON cases(created_at);
CREATE INDEX IF NOT EXISTS idx_cases_status_created_at ON cases(case_status, created_at);

CREATE INDEX IF NOT EXISTS idx_case_batches_batch_id ON case_batches(batch_id);
CREATE INDEX IF NOT EXISTS idx_case_batches_status ON case_batches(status);
CREATE INDEX IF NOT EXISTS idx_case_batches_source_type ON case_batches(source_type);
CREATE INDEX IF NOT EXISTS idx_case_batches_created_at ON case_batches(created_at);
CREATE INDEX IF NOT EXISTS idx_case_batches_uploaded_by ON case_batches(uploaded_by);

CREATE INDEX IF NOT EXISTS idx_batch_errors_batch_id ON batch_errors(batch_id);
CREATE INDEX IF NOT EXISTS idx_batch_errors_error_type ON batch_errors(error_type);
CREATE INDEX IF NOT EXISTS idx_batch_errors_external_case_id ON batch_errors(external_case_id);
