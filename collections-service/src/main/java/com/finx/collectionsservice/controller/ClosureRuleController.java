package com.finx.collectionsservice.controller;

import com.finx.collectionsservice.domain.dto.*;
import com.finx.collectionsservice.domain.enums.ClosureRuleType;
import com.finx.collectionsservice.service.ClosureRuleService;
import com.finx.collectionsservice.util.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/collections/closure/rules")
@RequiredArgsConstructor
@Tag(name = "Closure Rules", description = "APIs for closure rule management with scheduling")
public class ClosureRuleController {

    private final ClosureRuleService closureRuleService;

    // ==================== Rule CRUD APIs ====================

    @PostMapping
    @Operation(summary = "Create closure rule", description = "Create a new closure rule with optional cron scheduling")
    public ResponseEntity<CommonResponse<ClosureRuleDTO>> createRule(
            @Valid @RequestBody CreateClosureRuleRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        log.info("POST /closure/rules - Creating rule: {}", request.getRuleName());
        ClosureRuleDTO rule = closureRuleService.createRule(request, userId);
        return ResponseWrapper.created("Closure rule created successfully", rule);
    }

    @PutMapping("/{ruleId}")
    @Operation(summary = "Update closure rule", description = "Update an existing closure rule")
    public ResponseEntity<CommonResponse<ClosureRuleDTO>> updateRule(
            @PathVariable Long ruleId,
            @Valid @RequestBody CreateClosureRuleRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        log.info("PUT /closure/rules/{} - Updating rule", ruleId);
        ClosureRuleDTO rule = closureRuleService.updateRule(ruleId, request, userId);
        return ResponseWrapper.ok("Closure rule updated successfully", rule);
    }

    @GetMapping("/{ruleId}")
    @Operation(summary = "Get rule by ID", description = "Get closure rule details by ID")
    public ResponseEntity<CommonResponse<ClosureRuleDTO>> getRuleById(@PathVariable Long ruleId) {
        log.info("GET /closure/rules/{} - Fetching rule", ruleId);
        ClosureRuleDTO rule = closureRuleService.getRuleById(ruleId);
        return ResponseWrapper.ok("Rule retrieved successfully", rule);
    }

    @GetMapping("/code/{ruleCode}")
    @Operation(summary = "Get rule by code", description = "Get closure rule details by rule code")
    public ResponseEntity<CommonResponse<ClosureRuleDTO>> getRuleByCode(@PathVariable String ruleCode) {
        log.info("GET /closure/rules/code/{} - Fetching rule by code", ruleCode);
        ClosureRuleDTO rule = closureRuleService.getRuleByCode(ruleCode);
        return ResponseWrapper.ok("Rule retrieved successfully", rule);
    }

    @GetMapping
    @Operation(summary = "Search rules", description = "Search closure rules with filters and pagination")
    public ResponseEntity<CommonResponse<Page<ClosureRuleDTO>>> searchRules(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) ClosureRuleType ruleType,
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /closure/rules - Searching rules with term: {}, type: {}, active: {}",
                searchTerm, ruleType, isActive);
        Page<ClosureRuleDTO> rules = closureRuleService.searchRules(searchTerm, ruleType, isActive, pageable);
        return ResponseWrapper.ok("Rules retrieved successfully", rules);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active rules", description = "Get all active closure rules")
    public ResponseEntity<CommonResponse<List<ClosureRuleDTO>>> getActiveRules() {
        log.info("GET /closure/rules/active - Fetching active rules");
        List<ClosureRuleDTO> rules = closureRuleService.getActiveRules();
        return ResponseWrapper.ok("Active rules retrieved successfully", rules);
    }

    @DeleteMapping("/{ruleId}")
    @Operation(summary = "Delete rule", description = "Delete a closure rule")
    public ResponseEntity<CommonResponse<Void>> deleteRule(@PathVariable Long ruleId) {
        log.info("DELETE /closure/rules/{} - Deleting rule", ruleId);
        closureRuleService.deleteRule(ruleId);
        return ResponseWrapper.ok("Rule deleted successfully", null);
    }

    // ==================== Rule Status APIs ====================

    @PostMapping("/{ruleId}/activate")
    @Operation(summary = "Activate rule", description = "Activate a closure rule")
    public ResponseEntity<CommonResponse<ClosureRuleDTO>> activateRule(
            @PathVariable Long ruleId,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        log.info("POST /closure/rules/{}/activate - Activating rule", ruleId);
        ClosureRuleDTO rule = closureRuleService.activateRule(ruleId, userId);
        return ResponseWrapper.ok("Rule activated successfully", rule);
    }

    @PostMapping("/{ruleId}/deactivate")
    @Operation(summary = "Deactivate rule", description = "Deactivate a closure rule")
    public ResponseEntity<CommonResponse<ClosureRuleDTO>> deactivateRule(
            @PathVariable Long ruleId,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        log.info("POST /closure/rules/{}/deactivate - Deactivating rule", ruleId);
        ClosureRuleDTO rule = closureRuleService.deactivateRule(ruleId, userId);
        return ResponseWrapper.ok("Rule deactivated successfully", rule);
    }

    // ==================== Simulation & Execution APIs ====================

    @PostMapping("/{ruleId}/simulate")
    @Operation(summary = "Simulate rule", description = "Simulate rule execution - returns eligible cases without closing them")
    public ResponseEntity<CommonResponse<SimulationResultDTO>> simulateRule(@PathVariable Long ruleId) {
        log.info("POST /closure/rules/{}/simulate - Simulating rule", ruleId);
        SimulationResultDTO result = closureRuleService.simulateRule(ruleId);
        return ResponseWrapper.ok("Simulation completed successfully", result);
    }

    @PostMapping("/{ruleId}/execute")
    @Operation(summary = "Execute rule", description = "Execute rule - actually closes eligible cases")
    public ResponseEntity<CommonResponse<RuleExecutionDTO>> executeRule(
            @PathVariable Long ruleId,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        log.info("POST /closure/rules/{}/execute - Executing rule", ruleId);
        RuleExecutionDTO result = closureRuleService.executeRule(ruleId, userId);
        return ResponseWrapper.ok("Rule executed successfully", result);
    }

    // ==================== Execution History APIs ====================

    @GetMapping("/{ruleId}/executions")
    @Operation(summary = "Get rule execution history", description = "Get execution history for a specific rule")
    public ResponseEntity<CommonResponse<Page<RuleExecutionDTO>>> getRuleExecutionHistory(
            @PathVariable Long ruleId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /closure/rules/{}/executions - Fetching execution history", ruleId);
        Page<RuleExecutionDTO> history = closureRuleService.getRuleExecutionHistory(ruleId, pageable);
        return ResponseWrapper.ok("Execution history retrieved successfully", history);
    }

    @GetMapping("/executions/{executionId}")
    @Operation(summary = "Get execution by ID", description = "Get execution details by execution ID")
    public ResponseEntity<CommonResponse<RuleExecutionDTO>> getExecutionById(@PathVariable String executionId) {
        log.info("GET /closure/rules/executions/{} - Fetching execution", executionId);
        RuleExecutionDTO execution = closureRuleService.getExecutionById(executionId);
        return ResponseWrapper.ok("Execution retrieved successfully", execution);
    }

    @GetMapping("/executions/recent")
    @Operation(summary = "Get recent executions", description = "Get recent rule executions across all rules")
    public ResponseEntity<CommonResponse<List<RuleExecutionDTO>>> getRecentExecutions(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /closure/rules/executions/recent - Fetching {} recent executions", limit);
        List<RuleExecutionDTO> executions = closureRuleService.getRecentExecutions(limit);
        return ResponseWrapper.ok("Recent executions retrieved successfully", executions);
    }

    // ==================== Dashboard & Utility APIs ====================

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard stats", description = "Get cycle closure dashboard statistics")
    public ResponseEntity<CommonResponse<CycleClosureDashboardDTO>> getDashboardStats() {
        log.info("GET /closure/rules/dashboard - Fetching dashboard stats");
        CycleClosureDashboardDTO dashboard = closureRuleService.getDashboardStats();
        return ResponseWrapper.ok("Dashboard stats retrieved successfully", dashboard);
    }

    @PostMapping("/validate-cron")
    @Operation(summary = "Validate cron expression", description = "Validate a cron expression")
    public ResponseEntity<CommonResponse<Map<String, Object>>> validateCronExpression(
            @RequestBody Map<String, String> request) {
        String cronExpression = request.get("cronExpression");
        log.info("POST /closure/rules/validate-cron - Validating: {}", cronExpression);

        boolean isValid = closureRuleService.validateCronExpression(cronExpression);
        String nextRun = isValid ? closureRuleService.getNextScheduledRun(cronExpression) : null;

        Map<String, Object> response = Map.of(
            "valid", isValid,
            "cronExpression", cronExpression,
            "nextScheduledRun", nextRun != null ? nextRun : "N/A"
        );

        return ResponseWrapper.ok(isValid ? "Valid cron expression" : "Invalid cron expression", response);
    }

    @GetMapping("/types")
    @Operation(summary = "Get rule types", description = "Get all available closure rule types")
    public ResponseEntity<CommonResponse<ClosureRuleType[]>> getRuleTypes() {
        log.info("GET /closure/rules/types - Fetching rule types");
        return ResponseWrapper.ok("Rule types retrieved successfully", ClosureRuleType.values());
    }
}
