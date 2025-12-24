-- V1_42: Update trigger to also update last_payment_date, last_paid_amount, last_payment_mode
-- This enhances the V1_34 trigger to update all payment-related fields in loan_details

-- =====================================================
-- Function: update_loan_outstanding_on_repayment (UPDATED)
-- Description: Updates the loan's outstanding and payment fields when a repayment is approved
-- Trigger: Fires on INSERT or UPDATE of repayments table
-- Logic: When approval_status changes to 'APPROVED':
--   1. Subtract payment_amount from loan's total_outstanding
--   2. Update last_payment_date, last_paid_amount, last_payment_mode
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
    -- 2. This is an UPDATE where status changed to 'APPROVED' (maker-checker approved)
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

            -- Update the loan's total_outstanding AND payment tracking fields
            UPDATE loan_details
            SET total_outstanding = v_new_outstanding,
                last_payment_date = NEW.payment_date,
                last_paid_amount = NEW.payment_amount,
                last_payment_mode = NEW.payment_mode,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = v_loan_id;

            -- Log the update for audit purposes
            RAISE NOTICE 'Repayment %: Updated loan % - outstanding: % -> %, payment: %, mode: %, date: %',
                NEW.repayment_number,
                v_loan_id,
                v_current_outstanding,
                v_new_outstanding,
                NEW.payment_amount,
                NEW.payment_mode,
                NEW.payment_date;
        ELSE
            RAISE WARNING 'Repayment %: Could not find loan for case_id %', NEW.repayment_number, NEW.case_id;
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Note: The trigger itself doesn't need to be recreated since the function is replaced
-- The existing trigger trg_update_loan_outstanding_on_repayment will use the updated function

COMMENT ON FUNCTION update_loan_outstanding_on_repayment() IS 'Trigger function to update loan outstanding and payment tracking fields when repayment is approved (V1_42 enhanced)';
