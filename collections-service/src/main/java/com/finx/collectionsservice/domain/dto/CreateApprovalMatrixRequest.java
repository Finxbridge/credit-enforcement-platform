package com.finx.collectionsservice.domain.dto;

import com.finx.collectionsservice.domain.enums.ApprovalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateApprovalMatrixRequest {

    @NotBlank(message = "Matrix code is required")
    @Size(max = 50, message = "Matrix code must not exceed 50 characters")
    private String matrixCode;

    @NotBlank(message = "Matrix name is required")
    @Size(max = 200, message = "Matrix name must not exceed 200 characters")
    private String matrixName;

    @NotNull(message = "Approval type is required")
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
}
