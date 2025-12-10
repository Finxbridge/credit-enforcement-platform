package com.finx.strategyengineservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Simplified response for strategy simulation
 * Contains only essential case information for preview
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulationResponse {
    private long matchedCasesCount;
    private List<SimulationCaseDTO> matchedCases;
}
