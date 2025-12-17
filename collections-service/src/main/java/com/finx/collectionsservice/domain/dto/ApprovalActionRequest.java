package com.finx.collectionsservice.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalActionRequest {

    @NotBlank(message = "Action is required")
    private String action; // APPROVE, REJECT, ESCALATE, RETURN

    private BigDecimal approvedAmount; // For partial approvals

    private String remarks;

    private String rejectionReason;

    private String escalationReason;
}
