package com.finx.collectionsservice.controller;

import com.finx.collectionsservice.domain.dto.ArchivalRuleDTO;
import com.finx.collectionsservice.domain.dto.CommonResponse;
import com.finx.collectionsservice.domain.dto.CreateArchivalRuleRequest;
import com.finx.collectionsservice.service.ArchivalRuleService;
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

@Slf4j
@RestController
@RequestMapping("/collections/archival-rules")
@RequiredArgsConstructor
@Tag(name = "Archival Rules", description = "APIs for managing case archival rules")
public class ArchivalRuleController {

    private final ArchivalRuleService archivalRuleService;

    @PostMapping
    @Operation(summary = "Create archival rule", description = "Create a new archival rule")
    public ResponseEntity<CommonResponse<ArchivalRuleDTO>> createRule(
            @Valid @RequestBody CreateArchivalRuleRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("POST /archival-rules - Creating archival rule: {}", request.getRuleName());
        ArchivalRuleDTO response = archivalRuleService.createRule(request, userId);
        return ResponseWrapper.created("Archival rule created successfully", response);
    }

    @GetMapping("/{ruleId}")
    @Operation(summary = "Get rule by ID", description = "Get archival rule details")
    public ResponseEntity<CommonResponse<ArchivalRuleDTO>> getRule(@PathVariable Long ruleId) {
        log.info("GET /archival-rules/{} - Fetching rule", ruleId);
        ArchivalRuleDTO response = archivalRuleService.getRuleById(ruleId);
        return ResponseWrapper.ok("Archival rule retrieved successfully", response);
    }

    @GetMapping("/code/{ruleCode}")
    @Operation(summary = "Get rule by code", description = "Get archival rule by code")
    public ResponseEntity<CommonResponse<ArchivalRuleDTO>> getRuleByCode(@PathVariable String ruleCode) {
        log.info("GET /archival-rules/code/{} - Fetching rule", ruleCode);
        ArchivalRuleDTO response = archivalRuleService.getRuleByCode(ruleCode);
        return ResponseWrapper.ok("Archival rule retrieved successfully", response);
    }

    @GetMapping
    @Operation(summary = "Get all rules", description = "Get all archival rules with pagination")
    public ResponseEntity<CommonResponse<Page<ArchivalRuleDTO>>> getAllRules(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /archival-rules - Fetching all rules");
        Page<ArchivalRuleDTO> rules = archivalRuleService.getAllRules(pageable);
        return ResponseWrapper.ok("Archival rules retrieved successfully", rules);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active rules", description = "Get all active archival rules")
    public ResponseEntity<CommonResponse<List<ArchivalRuleDTO>>> getActiveRules() {
        log.info("GET /archival-rules/active - Fetching active rules");
        List<ArchivalRuleDTO> rules = archivalRuleService.getActiveRules();
        return ResponseWrapper.ok("Active archival rules retrieved successfully", rules);
    }

    @PutMapping("/{ruleId}")
    @Operation(summary = "Update rule", description = "Update an existing archival rule")
    public ResponseEntity<CommonResponse<ArchivalRuleDTO>> updateRule(
            @PathVariable Long ruleId,
            @Valid @RequestBody CreateArchivalRuleRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("PUT /archival-rules/{} - Updating rule", ruleId);
        ArchivalRuleDTO response = archivalRuleService.updateRule(ruleId, request, userId);
        return ResponseWrapper.ok("Archival rule updated successfully", response);
    }

    @PostMapping("/{ruleId}/activate")
    @Operation(summary = "Activate rule", description = "Activate an archival rule")
    public ResponseEntity<CommonResponse<ArchivalRuleDTO>> activateRule(
            @PathVariable Long ruleId,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("POST /archival-rules/{}/activate - Activating rule", ruleId);
        ArchivalRuleDTO response = archivalRuleService.activateRule(ruleId, userId);
        return ResponseWrapper.ok("Archival rule activated successfully", response);
    }

    @PostMapping("/{ruleId}/deactivate")
    @Operation(summary = "Deactivate rule", description = "Deactivate an archival rule")
    public ResponseEntity<CommonResponse<ArchivalRuleDTO>> deactivateRule(
            @PathVariable Long ruleId,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("POST /archival-rules/{}/deactivate - Deactivating rule", ruleId);
        ArchivalRuleDTO response = archivalRuleService.deactivateRule(ruleId, userId);
        return ResponseWrapper.ok("Archival rule deactivated successfully", response);
    }

    @PostMapping("/{ruleId}/execute")
    @Operation(summary = "Execute rule", description = "Manually execute an archival rule")
    public ResponseEntity<CommonResponse<ArchivalRuleDTO>> executeRule(
            @PathVariable Long ruleId,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("POST /archival-rules/{}/execute - Executing rule", ruleId);
        ArchivalRuleDTO response = archivalRuleService.executeRule(ruleId, userId);
        return ResponseWrapper.ok("Archival rule executed successfully", response);
    }

    @DeleteMapping("/{ruleId}")
    @Operation(summary = "Delete rule", description = "Delete an archival rule")
    public ResponseEntity<CommonResponse<Void>> deleteRule(
            @PathVariable Long ruleId,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("DELETE /archival-rules/{} - Deleting rule", ruleId);
        archivalRuleService.deleteRule(ruleId, userId);
        return ResponseWrapper.ok("Archival rule deleted successfully", null);
    }
}
