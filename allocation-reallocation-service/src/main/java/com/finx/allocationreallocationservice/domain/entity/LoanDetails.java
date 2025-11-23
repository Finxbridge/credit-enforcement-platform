package com.finx.allocationreallocationservice.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Read-only entity for LoanDetails table
 * Used for reading loan data from case-sourcing database
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

    @Column(name = "loan_account_number", unique = true, length = 50)
    private String loanAccountNumber;

    @Column(name = "bank_code", length = 50)
    private String bankCode;

    @Column(name = "product_code", length = 50)
    private String productCode;

    @Column(name = "product_type", length = 100)
    private String productType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_customer_id", nullable = false)
    private Customer primaryCustomer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "co_borrower_customer_id")
    private Customer coBorrower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guarantor_customer_id")
    private Customer guarantor;

    @Column(name = "loan_disbursement_date")
    private LocalDate loanDisbursementDate;

    @Column(name = "loan_maturity_date")
    private LocalDate loanMaturityDate;

    @Column(name = "principal_amount", precision = 15, scale = 2)
    private BigDecimal principalAmount;

    @Column(name = "interest_amount", precision = 15, scale = 2)
    private BigDecimal interestAmount;

    @Column(name = "penalty_amount", precision = 15, scale = 2)
    private BigDecimal penaltyAmount;

    @Column(name = "total_outstanding", precision = 15, scale = 2)
    private BigDecimal totalOutstanding;

    @Column(name = "interest_rate", precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "tenure_months")
    private Integer tenureMonths;

    @Column(name = "emi_amount", precision = 15, scale = 2)
    private BigDecimal emiAmount;

    @Column(name = "dpd")
    private Integer dpd;

    @Column(name = "bucket", length = 10)
    private String bucket;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "source_system", length = 50)
    private String sourceSystem;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
