package com.finx.allocationreallocationservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkDeallocationResponseDTO {
    private String jobId;
    private Integer totalCases;
    private Integer successfulDeallocations;
    private Integer failedDeallocations;
    private String status;
}
