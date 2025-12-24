package com.finx.collectionsservice.domain.entity;

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
 * Used for reading loan data for OTS case search
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

    @Column(name = "principal_amount", precision = 15, scale = 2)
    private BigDecimal principalAmount;

    @Column(name = "interest_amount", precision = 15, scale = 2)
    private BigDecimal interestAmount;

    @Column(name = "penalty_amount", precision = 15, scale = 2)
    private BigDecimal penaltyAmount;

    @Column(name = "total_outstanding", precision = 15, scale = 2)
    private BigDecimal totalOutstanding;

    @Column(name = "emi_amount", precision = 15, scale = 2)
    private BigDecimal emiAmount;

    @Column(name = "dpd")
    private Integer dpd;

    @Column(name = "bucket", length = 10)
    private String bucket;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
