package com.finx.allocationreallocationservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationBatchStatusDTO {
    private String batchId;
    private Integer totalCases;
    private Integer successful;
    private Integer failed;
    private String status;
}
