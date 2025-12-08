package com.finx.strategyengineservice.domain.dto;

import com.finx.strategyengineservice.domain.entity.Case;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SimulationResponse {
    private long matchedCasesCount;
    private List<Case> matchedCases;
}
