package com.finx.strategyengineservice.controller;

import com.finx.strategyengineservice.domain.dto.CommonResponse;
import com.finx.strategyengineservice.domain.dto.ExecutionDTO;
import com.finx.strategyengineservice.domain.dto.ExecutionDetailDTO;
import com.finx.strategyengineservice.domain.dto.ExecutionInitiatedDTO;
import com.finx.strategyengineservice.domain.dto.ExecutionRunDetailsDTO;
import com.finx.strategyengineservice.service.StrategyExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Strategy Execution Controller
 * Handles manual execution and execution history
 */
@RestController
@RequestMapping("/strategies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Strategy Execution", description = "Execute strategies and view execution history")
public class StrategyExecutionController {

    private final StrategyExecutionService executionService;

    @PostMapping("/{strategyId}/execute")
    @Operation(summary = "Manually trigger strategy", description = "Manually trigger a strategy execution immediately")
    public ResponseEntity<CommonResponse<ExecutionInitiatedDTO>> executeStrategy(
            @PathVariable Long strategyId) {
        log.info("POST /api/v1/strategies/{}/execute - Execute strategy", strategyId);
        ExecutionInitiatedDTO execution = executionService.executeStrategy(strategyId);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(CommonResponse.success("Strategy execution initiated.", execution));
    }

    @GetMapping("/executions")
    @Operation(summary = "List all execution runs", description = "Get list of all strategy execution runs with summary")
    public ResponseEntity<CommonResponse<List<ExecutionDTO>>> getAllExecutions() {
        log.info("GET /api/v1/strategies/executions - List all executions");
        List<ExecutionDTO> executions = executionService.getAllExecutions();
        return ResponseEntity.ok(CommonResponse.success("Strategy executions retrieved successfully.", executions));
    }

    @GetMapping("/executions/{executionId}")
    @Operation(summary = "Get execution details", description = "Get details of a single strategy execution")
    public ResponseEntity<CommonResponse<ExecutionDetailDTO>> getExecutionDetails(
            @PathVariable String executionId) {
        log.info("GET /api/v1/strategies/executions/{} - Get execution details", executionId);
        ExecutionDetailDTO execution = executionService.getExecutionDetails(executionId);
        return ResponseEntity.ok(CommonResponse.success("Strategy execution status retrieved successfully.", execution));
    }

    @GetMapping("/executions/{executionId}/details")
    @Operation(summary = "Get detailed run info", description = "Get detailed run information including error logs")
    public ResponseEntity<CommonResponse<ExecutionRunDetailsDTO>> getExecutionRunDetails(
            @PathVariable String executionId) {
        log.info("GET /api/v1/strategies/executions/{}/details - Get execution run details", executionId);
        ExecutionRunDetailsDTO execution = executionService.getExecutionRunDetails(executionId);
        return ResponseEntity.ok(CommonResponse.success("Strategy execution details retrieved successfully.", execution));
    }
}
