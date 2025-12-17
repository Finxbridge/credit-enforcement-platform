package com.finx.collectionsservice.domain.dto;

import com.finx.collectionsservice.domain.enums.ApprovalStatus;
import com.finx.collectionsservice.domain.enums.ApprovalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequestDTO {
    private Long id;
    private String requestNumber;
    private ApprovalType approvalType;
    private ApprovalStatus approvalStatus;
    private String entityType;
    private Long entityId;
    private String entityReference;
    private Long caseId;
    private String loanAccountNumber;
    private String customerName;
    private BigDecimal requestedAmount;
    private BigDecimal requestedPercentage;
    private String requestReason;
    private Map<String, Object> requestDetails;
    private Long requestedBy;
    private String requestedByName;
    private LocalDateTime requestedAt;
    private Integer currentLevel;
    private Integer maxLevels;
    private Long currentApproverRoleId;
    private Long currentApproverUserId;
    private Long approvedBy;
    private String approvedByName;
    private LocalDateTime approvedAt;
    private BigDecimal approvedAmount;
    private String approvalRemarks;
    private Long rejectedBy;
    private String rejectedByName;
    private LocalDateTime rejectedAt;
    private String rejectionReason;
    private LocalDateTime escalatedAt;
    private String escalationReason;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
