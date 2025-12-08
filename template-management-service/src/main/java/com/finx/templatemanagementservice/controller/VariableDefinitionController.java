package com.finx.templatemanagementservice.controller;

import com.finx.templatemanagementservice.domain.dto.CommonResponse;
import com.finx.templatemanagementservice.domain.dto.CreateVariableDefinitionRequest;
import com.finx.templatemanagementservice.domain.dto.VariableDefinitionDTO;
import com.finx.templatemanagementservice.service.VariableDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/templates/variables")
@RequiredArgsConstructor
@Tag(name = "Variable Definitions", description = "Centralized variable registry management")
public class VariableDefinitionController {

    private final VariableDefinitionService variableDefinitionService;

    @PostMapping
    @Operation(summary = "Create Variable Definition", description = "Create a new reusable variable definition")
    public ResponseEntity<CommonResponse<VariableDefinitionDTO>> createVariable(
            @Valid @RequestBody CreateVariableDefinitionRequest request) {
        log.info("Request to create variable: {}", request.getVariableKey());
        VariableDefinitionDTO variable = variableDefinitionService.createVariable(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success("Variable created successfully", variable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Variable by ID", description = "Retrieve variable definition by ID")
    public ResponseEntity<CommonResponse<VariableDefinitionDTO>> getVariable(@PathVariable Long id) {
        log.info("Request to get variable: {}", id);
        VariableDefinitionDTO variable = variableDefinitionService.getVariable(id);
        return ResponseEntity.ok(CommonResponse.success("Variable retrieved successfully", variable));
    }

    @GetMapping("/key/{variableKey}")
    @Operation(summary = "Get Variable by Key", description = "Retrieve variable definition by unique key")
    public ResponseEntity<CommonResponse<VariableDefinitionDTO>> getVariableByKey(@PathVariable String variableKey) {
        log.info("Request to get variable by key: {}", variableKey);
        VariableDefinitionDTO variable = variableDefinitionService.getVariableByKey(variableKey);
        return ResponseEntity.ok(CommonResponse.success("Variable retrieved successfully", variable));
    }

    @GetMapping
    @Operation(summary = "Get All Variables", description = "Retrieve all variable definitions")
    public ResponseEntity<CommonResponse<List<VariableDefinitionDTO>>> getAllVariables(
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(required = false) String category) {
        log.info("Request to get variables - activeOnly: {}, category: {}", activeOnly, category);

        List<VariableDefinitionDTO> variables;
        if (category != null) {
            variables = variableDefinitionService.getVariablesByCategory(category);
        } else if (Boolean.TRUE.equals(activeOnly)) {
            variables = variableDefinitionService.getActiveVariables();
        } else {
            variables = variableDefinitionService.getAllVariables();
        }

        return ResponseEntity.ok(CommonResponse.success(
                "Variables retrieved successfully", variables));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Variable", description = "Update existing variable definition")
    public ResponseEntity<CommonResponse<VariableDefinitionDTO>> updateVariable(
            @PathVariable Long id,
            @Valid @RequestBody CreateVariableDefinitionRequest request) {
        log.info("Request to update variable: {}", id);
        VariableDefinitionDTO variable = variableDefinitionService.updateVariable(id, request);
        return ResponseEntity.ok(CommonResponse.success("Variable updated successfully", variable));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Variable", description = "Delete variable definition")
    public ResponseEntity<CommonResponse<Void>> deleteVariable(@PathVariable Long id) {
        log.info("Request to delete variable: {}", id);
        variableDefinitionService.deleteVariable(id);
        return ResponseEntity.ok(CommonResponse.success("Variable deleted successfully", null));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Toggle Variable Status", description = "Enable or disable variable")
    public ResponseEntity<CommonResponse<Void>> toggleVariableStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        log.info("Request to toggle variable status: {} to {}", id, isActive);
        variableDefinitionService.toggleVariableStatus(id, isActive);
        return ResponseEntity.ok(CommonResponse.success("Variable status updated successfully", null));
    }
}
