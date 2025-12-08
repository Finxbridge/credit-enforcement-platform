package com.finx.strategyengineservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO containing all filter metadata for frontend dropdowns
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterMetadataResponse {

    private List<FilterFieldOption> textFilters;
    private List<FilterFieldOption> numericFilters;
    private List<FilterFieldOption> dateFilters;
    private List<OperatorOption> numericOperators;
    private List<OperatorOption> dateOperators;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterFieldOption {
        private String code;         // e.g., "OVERDUE_AMOUNT"
        private String displayName;  // e.g., "Overdue Amount"
        private String description;  // Optional description
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperatorOption {
        private String code;         // e.g., "GREATER_THAN_EQUAL"
        private String symbol;       // e.g., ">="
        private String displayName;  // e.g., "Greater Than or Equal"
        private String description;  // e.g., "Value should be Minimum Value"
    }
}
