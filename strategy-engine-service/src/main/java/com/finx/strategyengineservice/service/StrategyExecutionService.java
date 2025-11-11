package com.finx.strategyengineservice.service;

import com.finx.strategyengineservice.domain.dto.ExecutionDTO;
import com.finx.strategyengineservice.domain.dto.ExecutionDetailDTO;
import com.finx.strategyengineservice.domain.dto.ExecutionInitiatedDTO;
import com.finx.strategyengineservice.domain.dto.ExecutionRunDetailsDTO;

import java.util.List;

public interface StrategyExecutionService {

    ExecutionInitiatedDTO executeStrategy(Long strategyId);

    List<ExecutionDTO> getAllExecutions();

    ExecutionDetailDTO getExecutionDetails(String executionId);

    ExecutionRunDetailsDTO getExecutionRunDetails(String executionId);
}
