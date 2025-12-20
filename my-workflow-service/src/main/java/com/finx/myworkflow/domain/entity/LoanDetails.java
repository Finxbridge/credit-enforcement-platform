package com.finx.myworkflow.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Read-only LoanDetails entity for workflow
 * Includes comprehensive fields from case-sourcing CSV upload
 */
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

    // Account identification
    @Column(name = "loan_account_number", length = 50)
    private String loanAccountNumber;

    @Column(name = "lender", length = 100)
    private String lender;

    @Column(name = "co_lender", length = 100)
    private String coLender;

    @Column(name = "product_type", length = 50)
    private String productType;

    @Column(name = "scheme_code", length = 50)
    private String schemeCode;

    // Amounts
    @Column(name = "loan_amount", precision = 15, scale = 2)
    private BigDecimal loanAmount;

    @Column(name = "total_outstanding", precision = 15, scale = 2)
    private BigDecimal totalOutstanding;

    @Column(name = "pos", precision = 15, scale = 2)
    private BigDecimal pos;

    @Column(name = "tos", precision = 15, scale = 2)
    private BigDecimal tos;

    @Column(name = "emi_amount", precision = 15, scale = 2)
    private BigDecimal emiAmount;

    @Column(name = "penalty_amount", precision = 15, scale = 2)
    private BigDecimal penaltyAmount;

    @Column(name = "charges", precision = 15, scale = 2)
    private BigDecimal charges;

    @Column(name = "od_interest", precision = 15, scale = 2)
    private BigDecimal odInterest;

    // Overdue breakdown
    @Column(name = "principal_overdue", precision = 15, scale = 2)
    private BigDecimal principalOverdue;

    @Column(name = "interest_overdue", precision = 15, scale = 2)
    private BigDecimal interestOverdue;

    @Column(name = "fees_overdue", precision = 15, scale = 2)
    private BigDecimal feesOverdue;

    @Column(name = "penalty_overdue", precision = 15, scale = 2)
    private BigDecimal penaltyOverdue;

    // EMI details
    @Column(name = "emi_start_date")
    private LocalDate emiStartDate;

    @Column(name = "no_of_paid_emi")
    private Integer noOfPaidEmi;

    @Column(name = "no_of_pending_emi")
    private Integer noOfPendingEmi;

    @Column(name = "emi_overdue_from")
    private LocalDate emiOverdueFrom;

    @Column(name = "next_emi_date")
    private LocalDate nextEmiDate;

    // DPD & Bucket
    @Column(name = "dpd")
    private Integer dpd;

    @Column(name = "bucket", length = 20)
    private String bucket;

    @Column(name = "risk_bucket", length = 20)
    private String riskBucket;

    @Column(name = "som_bucket", length = 20)
    private String somBucket;

    @Column(name = "som_dpd")
    private Integer somDpd;

    @Column(name = "cycle_due", length = 50)
    private String cycleDue;

    // Rates
    @Column(name = "roi", precision = 10, scale = 4)
    private BigDecimal roi;

    @Column(name = "loan_duration", length = 50)
    private String loanDuration;

    // Dates
    @Column(name = "loan_disbursement_date")
    private LocalDate loanDisbursementDate;

    @Column(name = "loan_maturity_date")
    private LocalDate loanMaturityDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    // Payment info
    @Column(name = "last_payment_date")
    private LocalDate lastPaymentDate;

    @Column(name = "last_payment_mode", length = 50)
    private String lastPaymentMode;

    @Column(name = "last_paid_amount", precision = 15, scale = 2)
    private BigDecimal lastPaidAmount;

    // Bank details
    @Column(name = "beneficiary_account_number", length = 50)
    private String beneficiaryAccountNumber;

    @Column(name = "beneficiary_account_name", length = 100)
    private String beneficiaryAccountName;

    @Column(name = "repayment_bank_name", length = 100)
    private String repaymentBankName;

    @Column(name = "repayment_ifsc_code", length = 20)
    private String repaymentIfscCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_customer_id")
    private Customer primaryCustomer;
}
