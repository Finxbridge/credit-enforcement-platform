package com.finx.casesourcingservice.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_details")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==================== BASIC LOAN INFORMATION ====================

    @Column(name = "loan_account_number", unique = true, length = 50)
    private String loanAccountNumber; // ACCOUNT NO

    @Column(name = "lender", length = 100)
    private String lender; // LENDER

    @Column(name = "bank_code", length = 50)
    private String bankCode;

    @Column(name = "product_code", length = 50)
    private String productCode;

    @Column(name = "product_type", length = 100)
    private String productType; // PRODUCT

    @Column(name = "scheme_code", length = 50)
    private String schemeCode; // SCHEME CODE

    @Column(name = "product_sourcing_type", length = 50)
    private String productSourcingType; // PRODUCT SOURCING TYPE

    @Column(name = "co_lender", length = 100)
    private String coLender; // CO LENDER

    @Column(name = "reference_lender", length = 100)
    private String referenceLender; // REFERENCE LENDER

    // ==================== CUSTOMER RELATIONSHIPS ====================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_customer_id", nullable = false)
    private Customer primaryCustomer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "co_borrower_customer_id")
    private Customer coBorrower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guarantor_customer_id")
    private Customer guarantor;

    // ==================== LOAN AMOUNTS ====================

    @Column(name = "loan_amount", precision = 15, scale = 2)
    private BigDecimal loanAmount; // LOAN AMOUNT OR LIMIT

    @Column(name = "principal_amount", precision = 15, scale = 2)
    private BigDecimal principalAmount;

    @Column(name = "interest_amount", precision = 15, scale = 2)
    private BigDecimal interestAmount;

    @Column(name = "penalty_amount", precision = 15, scale = 2)
    private BigDecimal penaltyAmount; // PENALTY AMOUNT

    @Column(name = "charges", precision = 15, scale = 2)
    private BigDecimal charges; // CHARGES

    @Column(name = "od_interest", precision = 15, scale = 2)
    private BigDecimal odInterest; // OD INTEREST

    @Column(name = "total_outstanding", precision = 15, scale = 2)
    private BigDecimal totalOutstanding; // OVERDUE AMOUNT

    @Column(name = "pos", precision = 15, scale = 2)
    private BigDecimal pos; // POS (Principal Outstanding)

    @Column(name = "tos", precision = 15, scale = 2)
    private BigDecimal tos; // TOS (Total Outstanding)

    // ==================== OVERDUE BREAKDOWN ====================

    @Column(name = "principal_overdue", precision = 15, scale = 2)
    private BigDecimal principalOverdue; // PRINCIPAL OVERDUE

    @Column(name = "interest_overdue", precision = 15, scale = 2)
    private BigDecimal interestOverdue; // INTEREST OVERDUE

    @Column(name = "fees_overdue", precision = 15, scale = 2)
    private BigDecimal feesOverdue; // FEES OVERDUE

    @Column(name = "penalty_overdue", precision = 15, scale = 2)
    private BigDecimal penaltyOverdue; // PENALTY OVERDUE

    // ==================== EMI DETAILS ====================

    @Column(name = "emi_amount", precision = 15, scale = 2)
    private BigDecimal emiAmount; // EMI AMOUNT

    @Column(name = "emi_start_date")
    private LocalDate emiStartDate; // EMI START DATE

    @Column(name = "no_of_paid_emi")
    private Integer noOfPaidEmi; // NO OF PAID EMI

    @Column(name = "no_of_pending_emi")
    private Integer noOfPendingEmi; // NO OF PENDING EMI

    @Column(name = "emi_overdue_from")
    private LocalDate emiOverdueFrom; // Emi Overdue From

    @Column(name = "next_emi_date")
    private LocalDate nextEmiDate; // Next EMI Date

    // ==================== LOAN TENURE ====================

    @Column(name = "tenure_months")
    private Integer tenureMonths;

    @Column(name = "loan_duration", length = 50)
    private String loanDuration; // LOAN DURATION (e.g., "24 months")

    @Column(name = "interest_rate", precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "roi", precision = 5, scale = 2)
    private BigDecimal roi; // ROI

    // ==================== IMPORTANT DATES ====================

    @Column(name = "loan_disbursement_date")
    private LocalDate loanDisbursementDate; // DATE OF DISBURSEMENT

    @Column(name = "loan_maturity_date")
    private LocalDate loanMaturityDate; // MATURITY DATE

    @Column(name = "due_date")
    private LocalDate dueDate; // DUE DATE

    @Column(name = "writeoff_date")
    private LocalDate writeoffDate; // WRITEOFF DATE

    // ==================== DPD & BUCKET ====================

    @Column(name = "dpd")
    private Integer dpd; // DPD

    @Column(name = "bucket", length = 10)
    private String bucket;

    @Column(name = "risk_bucket", length = 20)
    private String riskBucket; // RISK BUCKET

    @Column(name = "som_bucket", length = 20)
    private String somBucket; // SOM BUCKET

    @Column(name = "som_dpd")
    private Integer somDpd; // SOM DPD

    @Column(name = "cycle_due", length = 20)
    private String cycleDue; // CYCLE DUE

    // ==================== CREDIT CARD SPECIFIC ====================

    @Column(name = "minimum_amount_due", precision = 15, scale = 2)
    private BigDecimal minimumAmountDue; // MINIMUM AMOUNT DUE

    @Column(name = "card_outstanding", precision = 15, scale = 2)
    private BigDecimal cardOutstanding; // CARD OUTSTANDING

    @Column(name = "statement_date")
    private LocalDate statementDate; // STATEMENT DATE

    @Column(name = "statement_month", length = 20)
    private String statementMonth; // STATEMENT MONTH

    @Column(name = "card_status", length = 30)
    private String cardStatus; // CARD STATUS

    @Column(name = "last_billed_amount", precision = 15, scale = 2)
    private BigDecimal lastBilledAmount; // LAST BILLED AMOUNT

    @Column(name = "last_4_digits", length = 4)
    private String last4Digits; // LAST 4 DIGITS

    // ==================== PAYMENT INFORMATION ====================

    @Column(name = "last_payment_date")
    private LocalDate lastPaymentDate; // LAST PAYMENT DATE

    @Column(name = "last_payment_mode", length = 50)
    private String lastPaymentMode; // LAST PAYMENT MODE

    @Column(name = "last_paid_amount", precision = 15, scale = 2)
    private BigDecimal lastPaidAmount; // LAST PAID AMOUNT

    // ==================== REPAYMENT BANK DETAILS ====================

    @Column(name = "beneficiary_account_number", length = 50)
    private String beneficiaryAccountNumber; // BENEFICIARY ACCOUNT Number

    @Column(name = "beneficiary_account_name", length = 255)
    private String beneficiaryAccountName; // BENEFICIARY ACCOUNT NAME

    @Column(name = "repayment_bank_name", length = 100)
    private String repaymentBankName; // REPAYMENT BANK NAME

    @Column(name = "repayment_ifsc_code", length = 20)
    private String repaymentIfscCode; // REPAYMENT IFSC CODE

    @Column(name = "reference_url", length = 500)
    private String referenceUrl; // REFERENCE URL

    // ==================== BLOCK STATUS ====================

    @Column(name = "block_1", length = 100)
    private String block1; // BLOCK 1

    @Column(name = "block_1_date")
    private LocalDate block1Date; // BLOCK 1 DATE

    @Column(name = "block_2", length = 100)
    private String block2; // BLOCK 2

    @Column(name = "block_2_date")
    private LocalDate block2Date; // BLOCK 2 DATE

    // ==================== SOURCING ====================

    @Column(name = "sourcing_rm_name", length = 255)
    private String sourcingRmName; // SOURCING RM NAME

    @Column(name = "source_system", length = 50)
    private String sourceSystem;

    // ==================== TIMESTAMPS ====================

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
