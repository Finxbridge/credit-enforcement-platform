package com.finx.strategyengineservice.controller;

import com.finx.strategyengineservice.domain.dto.CommonResponse;
import com.finx.strategyengineservice.domain.dto.DashboardResponse;
import com.finx.strategyengineservice.domain.dto.StrategyRequest;
import com.finx.strategyengineservice.domain.dto.StrategyResponse;
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

/**
 * Strategy Controller
 * Single endpoint to create/update complete strategy with all configurations
 *
 * Replaces the need for multiple API calls to:
 * - StrategyController (basic info)
 * - StrategyFilterController (filters)
 * - StrategyTemplateController (template)
 * - StrategyTriggerController (schedule)
 */
@RestController
@RequestMapping("/strategies/v2")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Unified Strategy Management", description = "Single API for complete strategy configuration")
public class StrategyController {

    private final StrategyService strategyService;

    /**
     * Create complete strategy with all configurations in a single API call
     *
     * Request includes:
     * 1. Rule Name (strategy name)
     * 2. Template Selection (type + template ID)
     * 3. Filters (numeric: outstanding/payment, text: language/product/pincode/state/bucket, DPD)
     * 4. Priority
     * 5. Schedule (daily/weekly)
     * 6. Status (DRAFT/ACTIVE/INACTIVE)
     */
    @PostMapping
    @Operation(
        summary = "Create complete strategy",
        description = "Create a new strategy with all configurations (filters, template, schedule) in a single API call"
    )
    public ResponseEntity<CommonResponse<StrategyResponse>> createStrategy(
            @Valid @RequestBody StrategyRequest request) {

        log.info("POST /api/v1/strategies/v2 - Create unified strategy: {}", request.getStrategyName());

        StrategyResponse response = strategyService.createStrategy(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success("Strategy created successfully with all configurations.", response));
    }

    /**
     * Update complete strategy configuration
     */
    @PutMapping("/{strategyId}")
    @Operation(
        summary = "Update complete strategy",
        description = "Update entire strategy configuration in a single API call"
    )
    public ResponseEntity<CommonResponse<StrategyResponse>> updateStrategy(
            @PathVariable Long strategyId,
            @Valid @RequestBody StrategyRequest request) {

        log.info("PUT /api/v1/strategies/v2/{} - Update unified strategy", strategyId);

        StrategyResponse response = strategyService.updateStrategy(strategyId, request);

        return ResponseEntity.ok(CommonResponse.success("Strategy updated successfully.", response));
    }

    /**
     * Get complete strategy details
     */
    @GetMapping("/{strategyId}")
    @Operation(
        summary = "Get complete strategy details",
        description = "Get all strategy configurations including filters, template, and schedule"
    )
    public ResponseEntity<CommonResponse<StrategyResponse>> getStrategy(
            @PathVariable Long strategyId) {

        log.info("GET /api/v1/strategies/v2/{} - Get unified strategy", strategyId);

        StrategyResponse response = strategyService.getStrategy(strategyId);

        return ResponseEntity.ok(CommonResponse.success("Strategy retrieved successfully.", response));
    }

    /**
     * List all strategies with summary
     */
    @GetMapping
    @Operation(
        summary = "List all strategies",
        description = "Get list of all strategies with summary information"
    )
    public ResponseEntity<CommonResponse<List<StrategyResponse>>> getAllStrategies(
            @RequestParam(required = false) String status) {

        log.info("GET /api/v1/strategies/v2 - List all strategies (status: {})", status);

        List<StrategyResponse> responses = strategyService.getAllStrategies(status);

        return ResponseEntity.ok(CommonResponse.success("Strategies retrieved successfully.", responses));
    }

    /**
     * Delete strategy
     */
    @DeleteMapping("/{strategyId}")
    @Operation(
        summary = "Delete strategy",
        description = "Delete strategy and all related configurations"
    )
    public ResponseEntity<CommonResponse<Void>> deleteStrategy(@PathVariable Long strategyId) {

        log.info("DELETE /api/v1/strategies/v2/{} - Delete strategy", strategyId);

        strategyService.deleteStrategy(strategyId);

        return ResponseEntity.ok(CommonResponse.successMessage("Strategy deleted successfully."));
    }

    /**
     * Activate/Deactivate strategy
     */
    @PatchMapping("/{strategyId}/status")
    @Operation(
        summary = "Change strategy status",
        description = "Activate, deactivate, or set strategy to draft"
    )
    public ResponseEntity<CommonResponse<StrategyResponse>> updateStatus(
            @PathVariable Long strategyId,
            @RequestParam String status) {

        log.info("PATCH /api/v1/strategies/v2/{}/status - Update status to: {}", strategyId, status);

        StrategyResponse response = strategyService.updateStrategyStatus(strategyId, status);

        return ResponseEntity.ok(CommonResponse.success("Strategy status updated successfully.", response));
    }

    /**
     * Simulate strategy to see how many cases will be affected
     */
    @PostMapping("/{strategyId}/simulate")
    @Operation(
        summary = "Simulate strategy execution",
        description = "See how many cases match the filter criteria without actually executing"
    )
    public ResponseEntity<CommonResponse<StrategyResponse>> simulateStrategy(
            @PathVariable Long strategyId) {

        log.info("POST /api/v1/strategies/v2/{}/simulate - Simulate strategy", strategyId);

        StrategyResponse response = strategyService.simulateStrategy(strategyId);

        return ResponseEntity.ok(CommonResponse.success("Strategy simulation completed.", response));
    }

    /**
     * Enable/Disable scheduler for strategy
     */
    @PatchMapping("/{strategyId}/scheduler")
    @Operation(
        summary = "Enable/Disable strategy scheduler",
        description = "Turn automated execution on or off"
    )
    public ResponseEntity<CommonResponse<StrategyResponse>> toggleScheduler(
            @PathVariable Long strategyId,
            @RequestParam Boolean enabled) {

        log.info("PATCH /api/v1/strategies/v2/{}/scheduler - Set enabled: {}", strategyId, enabled);

        StrategyResponse response = strategyService.toggleScheduler(strategyId, enabled);

        return ResponseEntity.ok(CommonResponse.success(
                enabled ? "Strategy scheduler enabled." : "Strategy scheduler disabled.", response));
    }

    /**
     * Get dashboard metrics with summary and all strategies
     */
    @GetMapping("/dashboard")
    @Operation(
        summary = "Get dashboard metrics",
        description = "Get strategy dashboard with all metrics and summary statistics including strategy name, last run, next run, channel, success rate, and active rules count"
    )
    public ResponseEntity<CommonResponse<DashboardResponse>> getDashboard() {

        log.info("GET /api/v1/strategies/v2/dashboard - Get dashboard");

        DashboardResponse response = strategyService.getDashboardMetrics();

        return ResponseEntity.ok(CommonResponse.success("Dashboard data retrieved successfully.", response));
    }
}
