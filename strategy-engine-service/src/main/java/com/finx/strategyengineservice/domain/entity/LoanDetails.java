package com.finx.strategyengineservice.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Read-only LoanDetails entity for strategy execution
 * Maps to loan_details table in shared database
 * Used for filtering cases based on loan attributes (DPD, bucket, outstanding, etc.)
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

    @Column(name = "loan_account_number", unique = true, nullable = false, length = 50)
    private String loanAccountNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_customer_id", nullable = false)
    private Customer primaryCustomer;

    @Column(name = "bank_code", length = 50)
    private String bankCode;

    @Column(name = "product_code", length = 50)
    private String productCode;

    @Column(name = "loan_type", length = 50)
    private String loanType;

    @Column(name = "loan_status", length = 20)
    private String loanStatus;

    @Column(name = "principal_amount", precision = 15, scale = 2)
    private BigDecimal principalAmount;

    @Column(name = "outstanding_amount", precision = 15, scale = 2)
    private BigDecimal outstandingAmount;

    @Column(name = "overdue_amount", precision = 15, scale = 2)
    private BigDecimal overdueAmount;

    @Column(name = "interest_rate", precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "dpd")
    private Integer dpd;

    @Column(name = "bucket", length = 20)
    private String bucket;

    @Column(name = "disbursement_date")
    private LocalDate disbursementDate;

    @Column(name = "maturity_date")
    private LocalDate maturityDate;

    @Column(name = "last_payment_date")
    private LocalDate lastPaymentDate;

    @Column(name = "last_payment_amount", precision = 15, scale = 2)
    private BigDecimal lastPaymentAmount;

    @Column(name = "emi_amount", precision = 15, scale = 2)
    private BigDecimal emiAmount;

    @Column(name = "tenure_months")
    private Integer tenureMonths;

    @Column(name = "remaining_tenure")
    private Integer remainingTenure;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;
}
