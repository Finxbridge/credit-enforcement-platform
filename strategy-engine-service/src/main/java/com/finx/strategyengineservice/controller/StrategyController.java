package com.finx.strategyengineservice.controller;

import com.finx.strategyengineservice.domain.dto.*;
import com.finx.strategyengineservice.service.StrategyExecutionService;
import com.finx.strategyengineservice.service.StrategyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/strategies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Strategy Management", description = "APIs for managing strategies")
public class StrategyController {

    private final StrategyService strategyService;
    private final StrategyExecutionService executionService;

    @GetMapping
    @Operation(summary = "List all strategies", description = "Get list of all strategies with status and statistics")
    public ResponseEntity<CommonResponse<List<StrategyDTO>>> getAllStrategies() {
        log.info("GET /api/v1/strategies - List all strategies");
        List<StrategyDTO> strategies = strategyService.getAllStrategies();
        return ResponseEntity.ok(CommonResponse.success("Strategies retrieved successfully.", strategies));
    }

    @PostMapping("/{strategyId}/simulate")
    @Operation(summary = "Simulate a strategy", description = "Run simulation to see potential impact before execution")
    public ResponseEntity<CommonResponse<SimulationResultDTO>> simulateStrategy(
            @PathVariable Long strategyId) {
        log.info("POST /api/v1/strategies/{}/simulate - Simulate strategy", strategyId);
        SimulationResultDTO result = strategyService.simulateStrategy(strategyId);
        return ResponseEntity.ok(CommonResponse.success("Strategy simulation completed successfully.", result));
    }

    @PostMapping
    @Operation(summary = "Create a new strategy", description = "Create a new strategy with rules and actions")
    public ResponseEntity<CommonResponse<StrategyDTO>> createStrategy(
            @Valid @RequestBody CreateStrategyRequest request) {
        log.info("POST /api/v1/strategies - Create strategy: {}", request.getName());
        StrategyDTO strategy = strategyService.createStrategy(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success("Strategy created successfully.", strategy));
    }

    @GetMapping("/{strategyId}")
    @Operation(summary = "Get strategy details", description = "Get detailed information about a specific strategy")
    public ResponseEntity<CommonResponse<StrategyDetailDTO>> getStrategyDetails(
            @PathVariable Long strategyId) {
        log.info("GET /api/v1/strategies/{} - Get strategy details", strategyId);
        StrategyDetailDTO strategy = strategyService.getStrategyById(strategyId);
        return ResponseEntity.ok(CommonResponse.success("Strategy details retrieved successfully.", strategy));
    }

    @PostMapping("/{strategyId}/filters")
    @Operation(summary = "Define or update filters", description = "Define or update filtering rules for a strategy")
    public ResponseEntity<CommonResponse<FiltersResponse>> updateFilters(
            @PathVariable Long strategyId,
            @Valid @RequestBody FiltersRequest request) {
        log.info("POST /api/v1/strategies/{}/filters - Update filters", strategyId);
        FiltersResponse response = strategyService.updateFilters(strategyId, request);
        return ResponseEntity.ok(CommonResponse.success("Strategy filters updated successfully.", response));
    }

    @GetMapping("/{strategyId}/filters")
    @Operation(summary = "Get applied filters", description = "Get current filtering rules for a strategy")
    public ResponseEntity<CommonResponse<FiltersResponse>> getFilters(
            @PathVariable Long strategyId) {
        log.info("GET /api/v1/strategies/{}/filters - Get filters", strategyId);
        FiltersResponse response = strategyService.getFilters(strategyId);
        return ResponseEntity.ok(CommonResponse.success("Strategy filters retrieved successfully.", response));
    }

    @PostMapping("/{strategyId}/template")
    @Operation(summary = "Add or update template", description = "Add or update template information for a strategy")
    public ResponseEntity<CommonResponse<TemplateInfoResponse>> updateTemplate(
            @PathVariable Long strategyId,
            @Valid @RequestBody TemplateInfoRequest request) {
        log.info("POST /api/v1/strategies/{}/template - Update template", strategyId);
        TemplateInfoResponse response = strategyService.updateTemplate(strategyId, request);
        return ResponseEntity.ok(CommonResponse.success("Strategy template updated successfully.", response));
    }

    @GetMapping("/{strategyId}/template")
    @Operation(summary = "Get template info", description = "Fetch existing template information for a strategy")
    public ResponseEntity<CommonResponse<TemplateInfoResponse>> getTemplate(
            @PathVariable Long strategyId) {
        log.info("GET /api/v1/strategies/{}/template - Get template", strategyId);
        TemplateInfoResponse response = strategyService.getTemplate(strategyId);
        return ResponseEntity.ok(CommonResponse.success("Strategy template retrieved successfully.", response));
    }

    @PostMapping("/{strategyId}/trigger")
    @Operation(summary = "Configure trigger", description = "Configure trigger frequency for a strategy")
    public ResponseEntity<CommonResponse<TriggerConfigResponse>> configureTrigger(
            @PathVariable Long strategyId,
            @Valid @RequestBody TriggerConfigRequest request) {
        log.info("POST /api/v1/strategies/{}/trigger - Configure trigger", strategyId);
        TriggerConfigResponse response = strategyService.configureTrigger(strategyId, request);
        return ResponseEntity.ok(CommonResponse.success("Strategy trigger configured successfully.", response));
    }

    @PutMapping("/{strategyId}/trigger")
    @Operation(summary = "Update trigger config", description = "Update trigger configuration for a strategy")
    public ResponseEntity<CommonResponse<TriggerConfigResponse>> updateTrigger(
            @PathVariable Long strategyId,
            @Valid @RequestBody TriggerConfigRequest request) {
        log.info("PUT /api/v1/strategies/{}/trigger - Update trigger", strategyId);
        TriggerConfigResponse response = strategyService.updateTrigger(strategyId, request);
        return ResponseEntity.ok(CommonResponse.success("Strategy trigger updated successfully.", response));
    }

    @PutMapping("/{strategyId}")
    @Operation(summary = "Edit strategy", description = "Edit rule details, filters, or triggers of an existing strategy")
    public ResponseEntity<CommonResponse<StrategyDTO>> updateStrategy(
            @PathVariable Long strategyId,
            @Valid @RequestBody UpdateStrategyRequest request) {
        log.info("PUT /api/v1/strategies/{} - Update strategy", strategyId);
        StrategyDTO strategy = strategyService.updateStrategy(strategyId, request);
        return ResponseEntity.ok(CommonResponse.success("Strategy updated successfully.", strategy));
    }

    @DeleteMapping("/{strategyId}")
    @Operation(summary = "Delete strategy", description = "Delete a strategy permanently")
    public ResponseEntity<CommonResponse<Void>> deleteStrategy(
            @PathVariable Long strategyId) {
        log.info("DELETE /api/v1/strategies/{} - Delete strategy", strategyId);
        strategyService.deleteStrategy(strategyId);
        return ResponseEntity.ok(CommonResponse.successMessage("Strategy deleted successfully."));
    }

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
