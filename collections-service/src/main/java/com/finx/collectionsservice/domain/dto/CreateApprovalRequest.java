package com.finx.collectionsservice.domain.dto;

import com.finx.collectionsservice.domain.enums.ApprovalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateApprovalRequest {

    @NotNull(message = "Approval type is required")
    private ApprovalType approvalType;

    @NotBlank(message = "Entity type is required")
    private String entityType;

    private Long entityId;
    private String entityReference;

    private Long caseId;
    private String loanAccountNumber;
    private String customerName;

    @NotNull(message = "Requested amount is required")
    private BigDecimal requestedAmount;

    private BigDecimal requestedPercentage;

    @NotBlank(message = "Request reason is required")
    private String requestReason;

    private Map<String, Object> requestDetails;
}
