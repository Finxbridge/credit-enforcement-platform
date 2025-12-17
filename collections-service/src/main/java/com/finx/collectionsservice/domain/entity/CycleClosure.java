package com.finx.collectionsservice.domain.entity;

import com.finx.collectionsservice.domain.enums.ClosureStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Cycle Closure Entity
 * Tracks case closure executions and archived cases
 */
@Entity
@Table(name = "cycle_closure_cases")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CycleClosure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "execution_id", nullable = false, length = 100)
    private String executionId;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "case_number", length = 50)
    private String caseNumber;

    @Column(name = "loan_account_number", length = 50)
    private String loanAccountNumber;

    @Column(name = "customer_name", length = 255)
    private String customerName;

    @Column(name = "dpd_at_closure")
    private Integer dpdAtClosure;

    @Column(name = "bucket_at_closure", length = 10)
    private String bucketAtClosure;

    @Column(name = "outstanding_at_closure", precision = 15, scale = 2)
    private BigDecimal outstandingAtClosure;

    @Column(name = "status_before_closure", length = 20)
    private String statusBeforeClosure;

    @Enumerated(EnumType.STRING)
    @Column(name = "closure_status", length = 20)
    private ClosureStatus closureStatus;

    @Column(name = "closure_reason", length = 100)
    private String closureReason;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (closureStatus == null) {
            closureStatus = ClosureStatus.PENDING;
        }
    }
}
