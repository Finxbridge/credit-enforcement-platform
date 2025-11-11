package com.finx.allocationreallocationservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationSummaryByDateDTO {
    private String date;
    private Long totalAllocations;
    private Long successfulAllocations;
    private Long failedAllocations;
}
