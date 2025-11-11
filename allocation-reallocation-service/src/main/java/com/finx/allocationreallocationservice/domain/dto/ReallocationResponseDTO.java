package com.finx.allocationreallocationservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReallocationResponseDTO {
    private String jobId;
    private String status;
    private Long casesReallocated;
    private Long estimatedCases;
}
