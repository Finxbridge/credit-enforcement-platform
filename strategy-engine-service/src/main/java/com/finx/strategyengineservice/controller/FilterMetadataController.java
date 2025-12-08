package com.finx.strategyengineservice.controller;

import com.finx.strategyengineservice.domain.dto.CommonResponse;
import com.finx.strategyengineservice.domain.dto.FilterMetadataResponse;
import com.finx.strategyengineservice.domain.dto.FilterMetadataResponseV2;
import com.finx.strategyengineservice.domain.enums.*;
import com.finx.strategyengineservice.service.FilterFieldService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller to provide filter metadata for frontend dropdowns
 * Now supports both static (enum-based) and dynamic (database-driven) approaches
 */
@RestController
@RequestMapping("/strategies/filters")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Filter Metadata", description = "APIs to get available filters and operators for strategy configuration")
public class FilterMetadataController {

    private final FilterFieldService filterFieldService;

    /**
     * Get all filter metadata from DATABASE (Dynamic - matches your sample response format)
     * This is the RECOMMENDED endpoint for frontend - supports active/inactive filters
     */
    @GetMapping("/metadata/v2")
    @Operation(
        summary = "Get dynamic filter metadata from database",
        description = "Returns all active filters with options from database. Supports active/inactive management."
    )
    public ResponseEntity<FilterMetadataResponseV2> getFilterMetadataV2() {
        log.info("GET /api/v2/filters/metadata/v2 - Fetching dynamic filter metadata from database");

        FilterMetadataResponseV2 response = filterFieldService.getAllFilterMetadata();

        return ResponseEntity.ok(response);
    }

    /**
     * Get all available filter fields and operators (STATIC - from enums)
     * Kept for backwards compatibility
     */
    @GetMapping("/metadata")
    @Operation(
        summary = "Get all filter metadata (static)",
        description = "Returns all available text, numeric, and date filters along with their operators from enums"
    )
    public ResponseEntity<CommonResponse<FilterMetadataResponse>> getFilterMetadata() {
        log.info("GET /api/v1/filters/metadata - Fetching filter metadata");

        FilterMetadataResponse response = FilterMetadataResponse.builder()
                .textFilters(getTextFilters())
                .numericFilters(getNumericFilters())
                .dateFilters(getDateFilters())
                .numericOperators(getNumericOperators())
                .dateOperators(getDateOperators())
                .build();

        return ResponseEntity.ok(CommonResponse.success("Filter metadata retrieved successfully", response));
    }

    /**
     * Get text filter fields only
     */
    @GetMapping("/text-filters")
    @Operation(
        summary = "Get text filter fields",
        description = "Returns all available text filter fields (Language, Product, State, Pincode)"
    )
    public ResponseEntity<CommonResponse<List<FilterMetadataResponse.FilterFieldOption>>> getTextFilterFields() {
        log.info("GET /api/v1/filters/text-filters - Fetching text filters");

        List<FilterMetadataResponse.FilterFieldOption> textFilters = getTextFilters();

        return ResponseEntity.ok(CommonResponse.success("Text filters retrieved successfully", textFilters));
    }

    /**
     * Get numeric filter fields only
     */
    @GetMapping("/numeric-filters")
    @Operation(
        summary = "Get numeric filter fields",
        description = "Returns all available numeric filter fields (Overdue Amount, DPD, EMI Amount, etc.)"
    )
    public ResponseEntity<CommonResponse<List<FilterMetadataResponse.FilterFieldOption>>> getNumericFilterFields() {
        log.info("GET /api/v1/filters/numeric-filters - Fetching numeric filters");

        List<FilterMetadataResponse.FilterFieldOption> numericFilters = getNumericFilters();

        return ResponseEntity.ok(CommonResponse.success("Numeric filters retrieved successfully", numericFilters));
    }

    /**
     * Get date filter fields only
     */
    @GetMapping("/date-filters")
    @Operation(
        summary = "Get date filter fields",
        description = "Returns all available date filter fields (Disbursement Date, Due Date, etc.)"
    )
    public ResponseEntity<CommonResponse<List<FilterMetadataResponse.FilterFieldOption>>> getDateFilterFields() {
        log.info("GET /api/v1/filters/date-filters - Fetching date filters");

        List<FilterMetadataResponse.FilterFieldOption> dateFilters = getDateFilters();

        return ResponseEntity.ok(CommonResponse.success("Date filters retrieved successfully", dateFilters));
    }

    /**
     * Get numeric operators
     */
    @GetMapping("/numeric-operators")
    @Operation(
        summary = "Get numeric operators",
        description = "Returns all available numeric operators (>=, <=, =, RANGE)"
    )
    public ResponseEntity<CommonResponse<List<FilterMetadataResponse.OperatorOption>>> getNumericOperatorsList() {
        log.info("GET /api/v1/filters/numeric-operators - Fetching numeric operators");

        List<FilterMetadataResponse.OperatorOption> operators = getNumericOperators();

        return ResponseEntity.ok(CommonResponse.success("Numeric operators retrieved successfully", operators));
    }

    /**
     * Get date operators
     */
    @GetMapping("/date-operators")
    @Operation(
        summary = "Get date operators",
        description = "Returns all available date operators (Older Than, Newer Than, Interval)"
    )
    public ResponseEntity<CommonResponse<List<FilterMetadataResponse.OperatorOption>>> getDateOperatorsList() {
        log.info("GET /api/v1/filters/date-operators - Fetching date operators");

        List<FilterMetadataResponse.OperatorOption> operators = getDateOperators();

        return ResponseEntity.ok(CommonResponse.success("Date operators retrieved successfully", operators));
    }

    // ===================================
    // Helper methods to build metadata
    // ===================================

    private List<FilterMetadataResponse.FilterFieldOption> getTextFilters() {
        return Arrays.stream(TextFilterField.values())
                .map(field -> FilterMetadataResponse.FilterFieldOption.builder()
                        .code(field.name())
                        .displayName(formatDisplayName(field.name()))
                        .description("Filter by " + formatDisplayName(field.name()))
                        .build())
                .collect(Collectors.toList());
    }

    private List<FilterMetadataResponse.FilterFieldOption> getNumericFilters() {
        return Arrays.stream(NumericFilterField.values())
                .map(field -> FilterMetadataResponse.FilterFieldOption.builder()
                        .code(field.name())
                        .displayName(field.getDisplayName())
                        .description("Filter by " + field.getDisplayName())
                        .build())
                .collect(Collectors.toList());
    }

    private List<FilterMetadataResponse.FilterFieldOption> getDateFilters() {
        return Arrays.stream(DateFilterField.values())
                .map(field -> FilterMetadataResponse.FilterFieldOption.builder()
                        .code(field.name())
                        .displayName(field.getDisplayName())
                        .description("Filter by " + field.getDisplayName())
                        .build())
                .collect(Collectors.toList());
    }

    private List<FilterMetadataResponse.OperatorOption> getNumericOperators() {
        return Arrays.asList(
                FilterMetadataResponse.OperatorOption.builder()
                        .code(NumericOperator.GREATER_THAN_EQUAL.name())
                        .symbol(NumericOperator.GREATER_THAN_EQUAL.getSymbol())
                        .displayName("Greater Than or Equal")
                        .description("Value should be Minimum Value")
                        .build(),
                FilterMetadataResponse.OperatorOption.builder()
                        .code(NumericOperator.LESS_THAN_EQUAL.name())
                        .symbol(NumericOperator.LESS_THAN_EQUAL.getSymbol())
                        .displayName("Less Than or Equal")
                        .description("Value should be Maximum Value")
                        .build(),
                FilterMetadataResponse.OperatorOption.builder()
                        .code(NumericOperator.EQUAL.name())
                        .symbol(NumericOperator.EQUAL.getSymbol())
                        .displayName("Equal")
                        .description("Add exact value")
                        .build(),
                FilterMetadataResponse.OperatorOption.builder()
                        .code(NumericOperator.RANGE.name())
                        .symbol(NumericOperator.RANGE.getSymbol())
                        .displayName("Range")
                        .description("Values should be Minimum and Maximum")
                        .build()
        );
    }

    private List<FilterMetadataResponse.OperatorOption> getDateOperators() {
        return Arrays.asList(
                FilterMetadataResponse.OperatorOption.builder()
                        .code(DateOperator.OLDER_THAN.name())
                        .symbol("<")
                        .displayName("Older Than")
                        .description("Date is older than specified date")
                        .build(),
                FilterMetadataResponse.OperatorOption.builder()
                        .code(DateOperator.NEWER_THAN.name())
                        .symbol(">")
                        .displayName("Newer Than")
                        .description("Date is newer than specified date")
                        .build(),
                FilterMetadataResponse.OperatorOption.builder()
                        .code(DateOperator.INTERVAL.name())
                        .symbol("BETWEEN")
                        .displayName("Interval")
                        .description("Date is between two dates")
                        .build()
        );
    }

    private String formatDisplayName(String fieldName) {
        return Arrays.stream(fieldName.split("_"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }
}
