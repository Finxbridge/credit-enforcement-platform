package com.finx.noticemanagementservice.controller;

import com.finx.noticemanagementservice.domain.dto.CommonResponse;
import com.finx.noticemanagementservice.domain.dto.CreateRoutingRuleRequest;
import com.finx.noticemanagementservice.domain.dto.RoutingRuleDTO;
import com.finx.noticemanagementservice.domain.dto.UpdateRoutingRuleRequest;
import com.finx.noticemanagementservice.service.RoutingRuleService;
import com.finx.noticemanagementservice.util.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

@RestController
@RequestMapping("/api/v1/notices/routing")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Routing Rules", description = "APIs for managing dispatch routing rules")
public class RoutingRuleController {

    private final RoutingRuleService routingRuleService;

    @PostMapping("/rules")
    @Operation(summary = "Create routing rule", description = "Create a new routing rule for vendor selection")
    public ResponseEntity<CommonResponse<RoutingRuleDTO>> createRoutingRule(
            @Valid @RequestBody CreateRoutingRuleRequest request) {
        log.info("Creating routing rule: {}", request.getRuleCode());
        RoutingRuleDTO created = routingRuleService.createRoutingRule(request);
        return ResponseWrapper.created("Routing rule created successfully", created);
    }

    @GetMapping("/rules/{id}")
    @Operation(summary = "Get routing rule by ID", description = "Get routing rule details by ID")
    public ResponseEntity<CommonResponse<RoutingRuleDTO>> getRoutingRuleById(
            @PathVariable Long id) {
        log.info("Getting routing rule with id: {}", id);
        RoutingRuleDTO rule = routingRuleService.getRoutingRuleById(id);
        return ResponseWrapper.ok("Routing rule retrieved successfully", rule);
    }

    @GetMapping("/rules/code/{ruleCode}")
    @Operation(summary = "Get routing rule by code", description = "Get routing rule details by code")
    public ResponseEntity<CommonResponse<RoutingRuleDTO>> getRoutingRuleByCode(
            @PathVariable String ruleCode) {
        log.info("Getting routing rule with code: {}", ruleCode);
        RoutingRuleDTO rule = routingRuleService.getRoutingRuleByCode(ruleCode);
        return ResponseWrapper.ok("Routing rule retrieved successfully", rule);
    }

    @GetMapping("/rules/active")
    @Operation(summary = "Get active routing rules", description = "Get all active routing rules ordered by priority")
    public ResponseEntity<CommonResponse<List<RoutingRuleDTO>>> getActiveRoutingRules() {
        log.info("Getting active routing rules");
        List<RoutingRuleDTO> rules = routingRuleService.getActiveRoutingRules();
        return ResponseWrapper.ok("Active routing rules retrieved successfully", rules);
    }

    @GetMapping("/rules/valid")
    @Operation(summary = "Get valid routing rules", description = "Get routing rules that are currently valid based on date range")
    public ResponseEntity<CommonResponse<List<RoutingRuleDTO>>> getValidRoutingRules() {
        log.info("Getting valid routing rules");
        List<RoutingRuleDTO> rules = routingRuleService.getValidRoutingRules();
        return ResponseWrapper.ok("Valid routing rules retrieved successfully", rules);
    }

    @GetMapping("/rules")
    @Operation(summary = "Get all routing rules", description = "Get all routing rules with pagination and filters")
    public ResponseEntity<CommonResponse<Page<RoutingRuleDTO>>> getAllRoutingRules(
            @Parameter(description = "Filter by active status")
            @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Search term for rule name or code")
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Getting all routing rules with filters - isActive: {}, search: {}", isActive, search);
        Page<RoutingRuleDTO> rules = routingRuleService.getAllRoutingRules(isActive, search, pageable);
        return ResponseWrapper.ok("Routing rules retrieved successfully", rules);
    }

    @PutMapping("/rules/{id}")
    @Operation(summary = "Update routing rule", description = "Update an existing routing rule")
    public ResponseEntity<CommonResponse<RoutingRuleDTO>> updateRoutingRule(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoutingRuleRequest request) {
        log.info("Updating routing rule with id: {}", id);
        RoutingRuleDTO updated = routingRuleService.updateRoutingRule(id, request);
        return ResponseWrapper.ok("Routing rule updated successfully", updated);
    }

    @PutMapping("/rules/{id}/activate")
    @Operation(summary = "Activate routing rule", description = "Activate an inactive routing rule")
    public ResponseEntity<CommonResponse<RoutingRuleDTO>> activateRoutingRule(
            @PathVariable Long id) {
        log.info("Activating routing rule with id: {}", id);
        RoutingRuleDTO activated = routingRuleService.activateRoutingRule(id);
        return ResponseWrapper.ok("Routing rule activated successfully", activated);
    }

    @PutMapping("/rules/{id}/deactivate")
    @Operation(summary = "Deactivate routing rule", description = "Deactivate an active routing rule")
    public ResponseEntity<CommonResponse<RoutingRuleDTO>> deactivateRoutingRule(
            @PathVariable Long id) {
        log.info("Deactivating routing rule with id: {}", id);
        RoutingRuleDTO deactivated = routingRuleService.deactivateRoutingRule(id);
        return ResponseWrapper.ok("Routing rule deactivated successfully", deactivated);
    }

    @DeleteMapping("/rules/{id}")
    @Operation(summary = "Delete routing rule", description = "Delete a routing rule")
    public ResponseEntity<CommonResponse<Void>> deleteRoutingRule(
            @PathVariable Long id) {
        log.info("Deleting routing rule with id: {}", id);
        routingRuleService.deleteRoutingRule(id);
        return ResponseWrapper.okMessage("Routing rule deleted successfully");
    }

    @PostMapping("/rules/match")
    @Operation(summary = "Find matching rule", description = "Find the best matching routing rule for given criteria")
    public ResponseEntity<CommonResponse<RoutingRuleDTO>> findMatchingRule(
            @RequestBody Map<String, Object> criteria) {
        log.info("Finding matching rule for criteria: {}", criteria);
        RoutingRuleDTO rule = routingRuleService.findMatchingRule(criteria);
        if (rule != null) {
            return ResponseWrapper.ok("Matching rule found", rule);
        }
        return ResponseWrapper.ok("No matching rule found", null);
    }
}
