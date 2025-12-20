-- V1_34: Add trigger to update loan_details.total_outstanding when repayment is approved
-- This enables independent service deployment without inter-service API calls

-- =====================================================
-- Function: update_loan_outstanding_on_repayment
-- Description: Updates the loan's total_outstanding when a repayment is approved
-- Trigger: Fires on INSERT or UPDATE of repayments table
-- Logic: When approval_status changes to 'APPROVED', subtract payment_amount from loan's total_outstanding
-- =====================================================

CREATE OR REPLACE FUNCTION update_loan_outstanding_on_repayment()
RETURNS TRIGGER AS $$
DECLARE
    v_loan_id BIGINT;
    v_current_outstanding DECIMAL(15,2);
    v_new_outstanding DECIMAL(15,2);
BEGIN
    -- Only process when status is APPROVED and either:
    -- 1. This is an INSERT with status = 'APPROVED' (auto-approved digital payments)
    -- 2. This is an UPDATE where status changed to 'APPROVED'
    IF NEW.approval_status = 'APPROVED' AND
       (TG_OP = 'INSERT' OR (TG_OP = 'UPDATE' AND OLD.approval_status != 'APPROVED')) THEN

        -- Get the loan_id from the case
        SELECT c.loan_id INTO v_loan_id
        FROM cases c
        WHERE c.id = NEW.case_id;

        IF v_loan_id IS NOT NULL THEN
            -- Get current outstanding
            SELECT total_outstanding INTO v_current_outstanding
            FROM loan_details
            WHERE id = v_loan_id;

            -- Calculate new outstanding (ensure it doesn't go negative)
            v_new_outstanding := GREATEST(0, COALESCE(v_current_outstanding, 0) - NEW.payment_amount);

            -- Update the loan's total_outstanding
            UPDATE loan_details
            SET total_outstanding = v_new_outstanding,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = v_loan_id;

            -- Log the update for audit purposes
            RAISE NOTICE 'Repayment %: Updated loan % outstanding from % to % (payment: %)',
                NEW.repayment_number, v_loan_id, v_current_outstanding, v_new_outstanding, NEW.payment_amount;
        ELSE
            RAISE WARNING 'Repayment %: Could not find loan for case_id %', NEW.repayment_number, NEW.case_id;
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger on repayments table
DROP TRIGGER IF EXISTS trg_update_loan_outstanding_on_repayment ON repayments;
CREATE TRIGGER trg_update_loan_outstanding_on_repayment
    AFTER INSERT OR UPDATE OF approval_status ON repayments
    FOR EACH ROW
    EXECUTE FUNCTION update_loan_outstanding_on_repayment();

-- =====================================================
-- Function: calculate_case_total_paid
-- Description: Calculates total paid amount for a case from approved repayments
-- Usage: SELECT calculate_case_total_paid(case_id) to get total paid
-- =====================================================

CREATE OR REPLACE FUNCTION calculate_case_total_paid(p_case_id BIGINT)
RETURNS DECIMAL(15,2) AS $$
DECLARE
    v_total_paid DECIMAL(15,2);
BEGIN
    SELECT COALESCE(SUM(payment_amount), 0) INTO v_total_paid
    FROM repayments
    WHERE case_id = p_case_id
      AND approval_status = 'APPROVED';

    RETURN v_total_paid;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- View: case_payment_summary
-- Description: Provides payment summary for cases including total paid and outstanding
-- Usage: SELECT * FROM case_payment_summary WHERE case_id = ?
-- =====================================================

CREATE OR REPLACE VIEW case_payment_summary AS
SELECT
    c.id AS case_id,
    c.case_number,
    c.loan_id,
    ld.loan_account_number,
    ld.total_outstanding AS current_outstanding,
    COALESCE((
        SELECT SUM(r.payment_amount)
        FROM repayments r
        WHERE r.case_id = c.id AND r.approval_status = 'APPROVED'
    ), 0) AS total_paid,
    COALESCE((
        SELECT COUNT(*)
        FROM repayments r
        WHERE r.case_id = c.id AND r.approval_status = 'APPROVED'
    ), 0) AS payment_count,
    COALESCE((
        SELECT MAX(r.payment_date)
        FROM repayments r
        WHERE r.case_id = c.id AND r.approval_status = 'APPROVED'
    ), NULL) AS last_payment_date,
    COALESCE((
        SELECT COUNT(*)
        FROM repayments r
        WHERE r.case_id = c.id AND r.approval_status = 'PENDING'
    ), 0) AS pending_approvals_count
FROM cases c
LEFT JOIN loan_details ld ON c.loan_id = ld.id;

-- Add comment
COMMENT ON VIEW case_payment_summary IS 'Aggregated payment summary for cases - shows total paid, outstanding, and payment counts';
COMMENT ON FUNCTION update_loan_outstanding_on_repayment() IS 'Trigger function to update loan outstanding when repayment is approved';
COMMENT ON FUNCTION calculate_case_total_paid(BIGINT) IS 'Utility function to calculate total paid amount for a case';
