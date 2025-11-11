package com.finx.allocationreallocationservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentWorkloadDTO {
    private Long agentId;
    private String agentName;
    private String geography;
    private Integer totalAllocated;
    private Integer activeAllocations;
    private Integer capacity;
    private Integer availableCapacity;
    private Double utilizationPercentage;
}
