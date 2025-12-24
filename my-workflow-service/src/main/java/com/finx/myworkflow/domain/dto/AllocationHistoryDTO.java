package com.finx.myworkflow.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AllocationHistoryDTO {

    private Long id;
    private Long caseId;
    private String externalCaseId;

    // Allocated To
    private Long allocatedToUserId;
    private String allocatedToUsername;
    private String newOwnerType;

    // Allocated From
    private Long allocatedFromUserId;
    private String previousOwnerType;

    // Action
    private String action;
    private String actionDisplayName;
    private String reason;

    // Changed By
    private Long allocatedBy;
    private String allocatedByName;

    // Timestamps
    private LocalDateTime allocatedAt;
    private LocalDateTime createdAt;

    // Batch
    private String batchId;

    // Agency
    private Long agencyId;
    private String agencyCode;
    private String agencyName;
}
