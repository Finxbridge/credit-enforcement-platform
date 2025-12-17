package com.finx.myworkflow.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for workflow case list
 * Maps exactly to frontend WorkflowCaseListItem type
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowCaseListDTO {

    private Long caseId;
    private String caseNumber;
    private String loanAccountNumber;
    private String customerName;
    private String mobileNumber;
    private String lender;
    private Integer dpd;
    private String bucket;
    private String region;
    private String city;
    private String state;
    private BigDecimal totalOutstanding;
    private BigDecimal overdueAmount;
    private String caseStatus;
    private Long allocatedToUserId;
    private String allocatedAgent;
    private LocalDateTime lastEventDate;
    private LocalDateTime createdAt;
}
