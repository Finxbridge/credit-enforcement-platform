package com.finx.strategyengineservice.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Simplified Generic filter DTO that can represent Text, Numeric, or Date filters
 *
 * Unified structure for all filter types:
 * - field: The field to filter on (e.g., "DPD", "OVERDUE_AMOUNT", "LANGUAGE", "DUE_DATE")
 * - filterType: TEXT, NUMERIC, or DATE
 * - operator: The comparison operator (e.g., ">=", "<=", "=", "RANGE", "IN", "BETWEEN")
 * - value1: Primary value (number, date string, or single text value)
 * - value2: Secondary value (for RANGE/BETWEEN operators)
 * - values: List of values (for TEXT filters with multiple selections)
 *
 * Examples:
 * - Numeric >= : {"field": "DPD", "filterType": "NUMERIC", "operator": ">=", "value1": "30"}
 * - Numeric <= : {"field": "PENDING_EMI", "filterType": "NUMERIC", "operator": "<=", "value1": "10"}
 * - Numeric = : {"field": "PAID_EMI", "filterType": "NUMERIC", "operator": "=", "value1": "5"}
 * - Numeric RANGE: {"field": "OVERDUE_AMOUNT", "filterType": "NUMERIC", "operator": "RANGE", "value1": "10000", "value2": "100000"}
 * - Text IN: {"field": "LANGUAGE", "filterType": "TEXT", "operator": "IN", "values": ["HINDI", "ENGLISH"]}
 * - Date: {"field": "DUE_DATE", "filterType": "DATE", "operator": ">=", "value1": "2024-01-01"}
 * - Date BETWEEN: {"field": "DUE_DATE", "filterType": "DATE", "operator": "BETWEEN", "value1": "2024-01-01", "value2": "2024-12-31"}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterDTO {

    @NotBlank(message = "Filter field is required")
    private String field;  // e.g., "OVERDUE_AMOUNT", "LANGUAGE", "DUE_DATE", "DPD"

    @NotBlank(message = "Filter type is required")
    private String filterType;  // TEXT, NUMERIC, DATE

    @NotBlank(message = "Operator is required")
    private String operator;  // ">=", "<=", "=", "RANGE", "IN", "BETWEEN", ">", "<"

    // Primary value - used for all single-value operations
    private String value1;

    // Secondary value - used for RANGE/BETWEEN operations
    private String value2;

    // For Text Filters - List of selected values (when operator is "IN")
    private List<String> values;
}
