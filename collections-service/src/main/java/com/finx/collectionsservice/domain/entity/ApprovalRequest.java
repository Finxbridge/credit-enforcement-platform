package com.finx.collectionsservice.domain.entity;

import com.finx.collectionsservice.domain.enums.ApprovalStatus;
import com.finx.collectionsservice.domain.enums.ApprovalType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "approval_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_number", unique = true, nullable = false, length = 50)
    private String requestNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_type", nullable = false, length = 30)
    private ApprovalType approvalType;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 20)
    private ApprovalStatus approvalStatus;

    // Entity reference
    @Column(name = "entity_type", length = 50)
    private String entityType; // OTS, WAIVER, SETTLEMENT, etc.

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "entity_reference", length = 100)
    private String entityReference;

    // Case reference
    @Column(name = "case_id")
    private Long caseId;

    @Column(name = "loan_account_number", length = 50)
    private String loanAccountNumber;

    @Column(name = "customer_name", length = 255)
    private String customerName;

    // Request details
    @Column(name = "requested_amount", precision = 15, scale = 2)
    private BigDecimal requestedAmount;

    @Column(name = "requested_percentage", precision = 5, scale = 2)
    private BigDecimal requestedPercentage;

    @Column(name = "request_reason", columnDefinition = "TEXT")
    private String requestReason;

    @Type(JsonType.class)
    @Column(name = "request_details", columnDefinition = "jsonb")
    private Map<String, Object> requestDetails;

    // Requester
    @Column(name = "requested_by")
    private Long requestedBy;

    @Column(name = "requested_by_name", length = 100)
    private String requestedByName;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    // Current approval level
    @Column(name = "current_level")
    private Integer currentLevel;

    @Column(name = "max_levels")
    private Integer maxLevels;

    // Current approver
    @Column(name = "current_approver_role_id")
    private Long currentApproverRoleId;

    @Column(name = "current_approver_user_id")
    private Long currentApproverUserId;

    // Approval/Rejection
    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_by_name", length = 100)
    private String approvedByName;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approved_amount", precision = 15, scale = 2)
    private BigDecimal approvedAmount;

    @Column(name = "approval_remarks", columnDefinition = "TEXT")
    private String approvalRemarks;

    // Rejection
    @Column(name = "rejected_by")
    private Long rejectedBy;

    @Column(name = "rejected_by_name", length = 100)
    private String rejectedByName;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    // Escalation
    @Column(name = "escalated_at")
    private LocalDateTime escalatedAt;

    @Column(name = "escalation_reason", length = 255)
    private String escalationReason;

    // Expiry
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        requestedAt = LocalDateTime.now();
        if (approvalStatus == null) approvalStatus = ApprovalStatus.PENDING;
        if (currentLevel == null) currentLevel = 1;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
