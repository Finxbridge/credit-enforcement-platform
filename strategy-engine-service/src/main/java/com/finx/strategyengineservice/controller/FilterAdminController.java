package com.finx.strategyengineservice.controller;

import com.finx.strategyengineservice.domain.dto.CommonResponse;
import com.finx.strategyengineservice.domain.entity.FilterField;
import com.finx.strategyengineservice.service.FilterFieldService;
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
 * Admin Controller for managing filter fields dynamically
 * Allows adding/editing/activating/deactivating filters without code changes
 */
@RestController
@RequestMapping("/strategies/admin/filters")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Filter Admin", description = "Admin APIs to manage filter fields dynamically")
public class FilterAdminController {

    private final FilterFieldService filterFieldService;

    /**
     * Get all filters (including inactive)
     */
    @GetMapping
    @Operation(
        summary = "Get all filters",
        description = "Returns all filter fields including inactive ones"
    )
    public ResponseEntity<CommonResponse<List<FilterField>>> getAllFilters() {
        log.info("GET /api/v1/admin/filters - Get all filters");

        List<FilterField> filters = filterFieldService.getAllActiveFilters();

        return ResponseEntity.ok(CommonResponse.success("Filters retrieved successfully", filters));
    }

    /**
     * Get filter by code
     */
    @GetMapping("/{code}")
    @Operation(
        summary = "Get filter by code",
        description = "Returns a specific filter field by its code"
    )
    public ResponseEntity<CommonResponse<FilterField>> getFilterByCode(@PathVariable String code) {
        log.info("GET /api/v1/admin/filters/{} - Get filter by code", code);

        FilterField filter = filterFieldService.getFilterByCode(code);

        return ResponseEntity.ok(CommonResponse.success("Filter retrieved successfully", filter));
    }

    /**
     * Create new filter field
     */
    @PostMapping
    @Operation(
        summary = "Create new filter field",
        description = "Add a new filter field to the system"
    )
    public ResponseEntity<CommonResponse<FilterField>> createFilter(@Valid @RequestBody FilterField filterField) {
        log.info("POST /api/v1/admin/filters - Create new filter: {}", filterField.getFieldCode());

        FilterField created = filterFieldService.createFilter(filterField);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success("Filter created successfully", created));
    }

    /**
     * Update existing filter field
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update filter field",
        description = "Update an existing filter field"
    )
    public ResponseEntity<CommonResponse<FilterField>> updateFilter(
            @PathVariable Long id,
            @Valid @RequestBody FilterField filterField) {

        log.info("PUT /api/v1/admin/filters/{} - Update filter", id);

        FilterField updated = filterFieldService.updateFilter(id, filterField);

        return ResponseEntity.ok(CommonResponse.success("Filter updated successfully", updated));
    }

    /**
     * Toggle filter active status
     */
    @PatchMapping("/{id}/status")
    @Operation(
        summary = "Toggle filter status",
        description = "Activate or deactivate a filter field"
    )
    public ResponseEntity<CommonResponse<Void>> toggleFilterStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {

        log.info("PATCH /api/v1/admin/filters/{}/status - Toggle status to: {}", id, isActive);

        filterFieldService.toggleFilterStatus(id, isActive);

        String message = isActive ? "Filter activated successfully" : "Filter deactivated successfully";
        return ResponseEntity.ok(CommonResponse.successMessage(message));
    }

    /**
     * Delete filter field
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete filter field",
        description = "Permanently delete a filter field"
    )
    public ResponseEntity<CommonResponse<Void>> deleteFilter(@PathVariable Long id) {
        log.info("DELETE /api/v1/admin/filters/{} - Delete filter", id);

        filterFieldService.deleteFilter(id);

        return ResponseEntity.ok(CommonResponse.successMessage("Filter deleted successfully"));
    }
}
