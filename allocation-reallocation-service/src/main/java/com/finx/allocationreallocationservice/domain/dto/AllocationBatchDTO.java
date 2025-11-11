package com.finx.allocationreallocationservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationBatchDTO {
    private String batchId;
    private String fileName;
    private Integer totalCases;
    private Integer successfulAllocations;
    private Integer failedAllocations;
    private String status;
    private LocalDateTime uploadedAt;
    private LocalDateTime completedAt;
}
