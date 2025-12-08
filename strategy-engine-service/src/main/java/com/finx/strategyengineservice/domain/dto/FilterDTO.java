package com.finx.strategyengineservice.domain.dto;

import com.finx.strategyengineservice.domain.enums.DateOperator;
import com.finx.strategyengineservice.domain.enums.NumericOperator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Generic filter DTO that can represent Text, Numeric, or Date filters
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterDTO {

    @NotBlank(message = "Filter field is required")
    private String field;  // e.g., "OVERDUE_AMOUNT", "LANGUAGE", "DUE_DATE"

    @NotBlank(message = "Filter type is required")
    private String filterType;  // TEXT, NUMERIC, DATE

    // For Text Filters - List of selected values
    private List<String> values;

    // For Numeric Filters
    private NumericOperator numericOperator;
    private Double minValue;
    private Double maxValue;
    private Double exactValue;

    // For Date Filters
    private DateOperator dateOperator;
    private LocalDate fromDate;
    private LocalDate toDate;
    private LocalDate specificDate;
}
