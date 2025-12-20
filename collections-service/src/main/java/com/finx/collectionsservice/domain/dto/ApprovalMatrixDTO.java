package com.finx.collectionsservice.domain.dto;

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
public class ApprovalMatrixDTO {
    private Long id;
    private String matrixCode;
    private String matrixName;
    private ApprovalType approvalType;
    private String description;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal minPercentage;
    private BigDecimal maxPercentage;
    private Integer approvalLevel;
    private Long approverRoleId;
    private String approverRoleName;
    private Long approverUserId;
    private String approverUserName;
    private Integer escalationHours;
    private Integer escalationLevel;
    private Long escalationRoleId;
    private Boolean autoApproveEnabled;
    private BigDecimal autoApproveBelowAmount;
    private Map<String, Object> criteria;
    private Boolean isActive;
    private Integer priorityOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
