package com.finx.strategyengineservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response for strategy with all configurations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyResponse {

    private Long strategyId;
    private String strategyCode;
    private String ruleName;
    private String status;
    private Integer priority;
    private String description;

    // Template info
    private TemplateInfo template;

    // Filter summary
    private FilterSummary filters;

    // Schedule info
    private ScheduleInfo schedule;

    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastRunAt;
    private Integer totalExecutions;
    private Integer successCount;
    private Integer failureCount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateInfo {
        private String templateType;
        private Long templateId;
        private String templateName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterSummary {
        // Numeric filters
        private String outstandingPrincipal;  // "Greater than 50000"
        private String paymentAmount;         // "Between 1000 and 5000"

        // Text filters
        private List<String> languages;
        private List<String> products;
        private List<String> pincodes;
        private List<String> states;
        private List<String> buckets;

        // DPD
        private String dpd;  // "Greater than 30" or "Between 30 and 60"

        // Total cases matching (for simulation)
        private Integer estimatedCasesMatched;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleInfo {
        private String frequency;
        private String schedule;  // Human readable: "Daily at 09:00" or "Weekly on Monday, Wednesday, Friday at 10:00"
        private LocalDateTime nextRunAt;
        private Boolean isEnabled;
    }
}
