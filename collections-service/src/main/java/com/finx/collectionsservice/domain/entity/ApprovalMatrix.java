package com.finx.collectionsservice.domain.entity;

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
@Table(name = "approval_matrix")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalMatrix {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "matrix_code", unique = true, nullable = false, length = 50)
    private String matrixCode;

    @Column(name = "matrix_name", nullable = false, length = 200)
    private String matrixName;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_type", nullable = false, length = 30)
    private ApprovalType approvalType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Amount-based thresholds
    @Column(name = "min_amount", precision = 15, scale = 2)
    private BigDecimal minAmount;

    @Column(name = "max_amount", precision = 15, scale = 2)
    private BigDecimal maxAmount;

    // Percentage-based thresholds (for waivers)
    @Column(name = "min_percentage", precision = 5, scale = 2)
    private BigDecimal minPercentage;

    @Column(name = "max_percentage", precision = 5, scale = 2)
    private BigDecimal maxPercentage;

    // Approval hierarchy
    @Column(name = "approval_level")
    private Integer approvalLevel;

    @Column(name = "approver_role_id")
    private Long approverRoleId;

    @Column(name = "approver_role_name", length = 100)
    private String approverRoleName;

    // Can also specify specific user
    @Column(name = "approver_user_id")
    private Long approverUserId;

    @Column(name = "approver_user_name", length = 100)
    private String approverUserName;

    // Escalation settings
    @Column(name = "escalation_hours")
    private Integer escalationHours;

    @Column(name = "escalation_level")
    private Integer escalationLevel;

    @Column(name = "escalation_role_id")
    private Long escalationRoleId;

    // Auto-approval settings
    @Column(name = "auto_approve_enabled")
    private Boolean autoApproveEnabled;

    @Column(name = "auto_approve_below_amount", precision = 15, scale = 2)
    private BigDecimal autoApproveBelowAmount;

    // Additional criteria
    @Type(JsonType.class)
    @Column(name = "criteria", columnDefinition = "jsonb")
    private Map<String, Object> criteria;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "priority_order")
    private Integer priorityOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) isActive = true;
        if (priorityOrder == null) priorityOrder = 0;
        if (approvalLevel == null) approvalLevel = 1;
        if (autoApproveEnabled == null) autoApproveEnabled = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
