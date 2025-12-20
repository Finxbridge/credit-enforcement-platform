-- =====================================================
-- V1_35: Fix Column Lengths for User-Provided Content
-- Increase column lengths to prevent truncation errors
-- for names, paths, URLs, and other user-provided content
-- Uses DO blocks with IF EXISTS checks for optional tables
-- =====================================================

-- =====================================================
-- 1. DOCUMENTS TABLE (dms-service) - Core table, always exists
-- =====================================================
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'documents') THEN
        ALTER TABLE documents ALTER COLUMN document_name TYPE TEXT;
        ALTER TABLE documents ALTER COLUMN document_id TYPE VARCHAR(100);
        ALTER TABLE documents ALTER COLUMN file_url TYPE VARCHAR(1000);
        ALTER TABLE documents ALTER COLUMN file_name TYPE TEXT;
        ALTER TABLE documents ALTER COLUMN file_type TYPE VARCHAR(100);
        ALTER TABLE documents ALTER COLUMN storage_path TYPE VARCHAR(1000);
        ALTER TABLE documents ALTER COLUMN storage_bucket TYPE VARCHAR(255);
    END IF;
END $$;

-- =====================================================
-- 2. TEMPLATES TABLE (template-management-service)
-- =====================================================
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'templates') THEN
        ALTER TABLE templates ALTER COLUMN template_name TYPE VARCHAR(255);
        ALTER TABLE templates ALTER COLUMN template_code TYPE VARCHAR(255);

        -- Only alter if column exists
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'templates' AND column_name = 'provider_template_id') THEN
            ALTER TABLE templates ALTER COLUMN provider_template_id TYPE VARCHAR(255);
        END IF;

        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'templates' AND column_name = 'document_url') THEN
            ALTER TABLE templates ALTER COLUMN document_url TYPE VARCHAR(2000);
        END IF;

        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'templates' AND column_name = 'document_storage_path') THEN
            ALTER TABLE templates ALTER COLUMN document_storage_path TYPE VARCHAR(1000);
        END IF;

        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'templates' AND column_name = 'document_original_name') THEN
            ALTER TABLE templates ALTER COLUMN document_original_name TYPE TEXT;
        END IF;
    END IF;
END $$;

-- =====================================================
-- 3. TEMPLATE_VARIABLES TABLE
-- =====================================================
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'template_variables') THEN
        ALTER TABLE template_variables ALTER COLUMN variable_name TYPE VARCHAR(255);
        ALTER TABLE template_variables ALTER COLUMN variable_key TYPE VARCHAR(255);
    END IF;
END $$;

-- =====================================================
-- 4. TEMPLATE_CONTENT TABLE
-- =====================================================
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'template_content') THEN
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'template_content' AND column_name = 'subject') THEN
            ALTER TABLE template_content ALTER COLUMN subject TYPE VARCHAR(500);
        END IF;
    END IF;
END $$;

-- =====================================================
-- 5. NOTICE_DOCUMENTS TABLE
-- =====================================================
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'notice_documents') THEN
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'notice_documents' AND column_name = 'file_url') THEN
            ALTER TABLE notice_documents ALTER COLUMN file_url TYPE VARCHAR(2000);
        END IF;
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'notice_documents' AND column_name = 'file_name') THEN
            ALTER TABLE notice_documents ALTER COLUMN file_name TYPE TEXT;
        END IF;
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'notice_documents' AND column_name = 'storage_path') THEN
            ALTER TABLE notice_documents ALTER COLUMN storage_path TYPE VARCHAR(1000);
        END IF;
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'notice_documents' AND column_name = 'template_name') THEN
            ALTER TABLE notice_documents ALTER COLUMN template_name TYPE VARCHAR(255);
        END IF;
    END IF;
END $$;

-- =====================================================
-- 6. NOTICES TABLE
-- =====================================================
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'notices') THEN
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'notices' AND column_name = 'pdf_url') THEN
            ALTER TABLE notices ALTER COLUMN pdf_url TYPE VARCHAR(2000);
        END IF;
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'notices' AND column_name = 'original_document_url') THEN
            ALTER TABLE notices ALTER COLUMN original_document_url TYPE VARCHAR(2000);
        END IF;
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'notices' AND column_name = 'processed_document_url') THEN
            ALTER TABLE notices ALTER COLUMN processed_document_url TYPE VARCHAR(2000);
        END IF;
    END IF;
END $$;

-- =====================================================
-- 7. WHATSAPP_MESSAGES TABLE
-- =====================================================
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'whatsapp_messages') THEN
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'whatsapp_messages' AND column_name = 'template_name') THEN
            ALTER TABLE whatsapp_messages ALTER COLUMN template_name TYPE VARCHAR(255);
        END IF;
    END IF;
END $$;

-- =====================================================
-- 8. SMS_MESSAGES TABLE
-- =====================================================
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'sms_messages') THEN
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'sms_messages' AND column_name = 'template_code') THEN
            ALTER TABLE sms_messages ALTER COLUMN template_code TYPE VARCHAR(255);
        END IF;
    END IF;
END $$;

-- =====================================================
-- 9. EMAIL_MESSAGES TABLE
-- =====================================================
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'email_messages') THEN
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'email_messages' AND column_name = 'template_code') THEN
            ALTER TABLE email_messages ALTER COLUMN template_code TYPE VARCHAR(255);
        END IF;
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'email_messages' AND column_name = 'subject') THEN
            ALTER TABLE email_messages ALTER COLUMN subject TYPE VARCHAR(500);
        END IF;
    END IF;
END $$;

-- =====================================================
-- 10. RECEIPTS TABLE
-- =====================================================
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'receipts') THEN
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'receipts' AND column_name = 'pdf_url') THEN
            ALTER TABLE receipts ALTER COLUMN pdf_url TYPE VARCHAR(2000);
        END IF;
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'receipts' AND column_name = 'pdf_storage_path') THEN
            ALTER TABLE receipts ALTER COLUMN pdf_storage_path TYPE VARCHAR(1000);
        END IF;
    END IF;
END $$;

-- =====================================================
-- 11. SETTLEMENT_LETTERS TABLE
-- =====================================================
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'settlement_letters') THEN
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'settlement_letters' AND column_name = 'pdf_url') THEN
            ALTER TABLE settlement_letters ALTER COLUMN pdf_url TYPE VARCHAR(2000);
        END IF;
    END IF;
END $$;

-- =====================================================
-- 12. OTS_REQUESTS TABLE
-- =====================================================
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'ots_requests') THEN
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'ots_requests' AND column_name = 'consent_document_url') THEN
            ALTER TABLE ots_requests ALTER COLUMN consent_document_url TYPE VARCHAR(2000);
        END IF;
    END IF;
END $$;

-- =====================================================
-- 13. CASE_SOURCING / IMPORT RELATED TABLES
-- =====================================================
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'case_import_batches') THEN
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'case_import_batches' AND column_name = 'source_file_name') THEN
            ALTER TABLE case_import_batches ALTER COLUMN source_file_name TYPE TEXT;
        END IF;
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'case_import_batches' AND column_name = 'file_name') THEN
            ALTER TABLE case_import_batches ALTER COLUMN file_name TYPE TEXT;
        END IF;
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'case_import_batches' AND column_name = 'file_path') THEN
            ALTER TABLE case_import_batches ALTER COLUMN file_path TYPE VARCHAR(1000);
        END IF;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'batch_exports') THEN
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'batch_exports' AND column_name = 'file_name') THEN
            ALTER TABLE batch_exports ALTER COLUMN file_name TYPE TEXT;
        END IF;
    END IF;
END $$;

-- =====================================================
-- 14. VARIABLE_DEFINITIONS TABLE
-- =====================================================
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'variable_definitions') THEN
        ALTER TABLE variable_definitions ALTER COLUMN variable_key TYPE VARCHAR(255);
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'variable_definitions' AND column_name = 'entity_path') THEN
            ALTER TABLE variable_definitions ALTER COLUMN entity_path TYPE VARCHAR(500);
        END IF;
    END IF;
END $$;

-- =====================================================
-- 15. PROVIDER CONFIGURATIONS (URLs)
-- =====================================================
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'communication_providers') THEN
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'communication_providers' AND column_name = 'endpoint_url') THEN
            ALTER TABLE communication_providers ALTER COLUMN endpoint_url TYPE VARCHAR(2000);
        END IF;
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'communication_providers' AND column_name = 'oauth_token_url') THEN
            ALTER TABLE communication_providers ALTER COLUMN oauth_token_url TYPE VARCHAR(2000);
        END IF;
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'communication_providers' AND column_name = 'webhook_url') THEN
            ALTER TABLE communication_providers ALTER COLUMN webhook_url TYPE VARCHAR(2000);
        END IF;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'lms_integrations') THEN
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'lms_integrations' AND column_name = 'connection_url') THEN
            ALTER TABLE lms_integrations ALTER COLUMN connection_url TYPE VARCHAR(2000);
        END IF;
    END IF;
END $$;

-- =====================================================
-- 16. PROOF OF DELIVERY (notice-management)
-- =====================================================
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'notice_proof_of_delivery') THEN
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'notice_proof_of_delivery' AND column_name = 'pod_file_url') THEN
            ALTER TABLE notice_proof_of_delivery ALTER COLUMN pod_file_url TYPE VARCHAR(2000);
        END IF;
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'notice_proof_of_delivery' AND column_name = 'recipient_signature_url') THEN
            ALTER TABLE notice_proof_of_delivery ALTER COLUMN recipient_signature_url TYPE VARCHAR(2000);
        END IF;
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'notice_proof_of_delivery' AND column_name = 'recipient_photo_url') THEN
            ALTER TABLE notice_proof_of_delivery ALTER COLUMN recipient_photo_url TYPE VARCHAR(2000);
        END IF;
    END IF;
END $$;

-- =====================================================
-- 17. ORGANIZATIONS TABLE
-- =====================================================
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'organizations') THEN
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'organizations' AND column_name = 'logo_url') THEN
            ALTER TABLE organizations ALTER COLUMN logo_url TYPE VARCHAR(2000);
        END IF;
    END IF;
END $$;

-- =====================================================
-- 18. DOCUMENT ACCESS LOGS TABLE
-- =====================================================
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'document_access_logs') THEN
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'document_access_logs' AND column_name = 'access_user_agent') THEN
            ALTER TABLE document_access_logs ALTER COLUMN access_user_agent TYPE VARCHAR(1000);
        END IF;
    END IF;
END $$;

-- =====================================================
-- COMMENTS
-- =====================================================
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'documents') THEN
        COMMENT ON TABLE documents IS 'Simple document storage - column lengths increased to handle long filenames and URLs';
    END IF;
END $$;
