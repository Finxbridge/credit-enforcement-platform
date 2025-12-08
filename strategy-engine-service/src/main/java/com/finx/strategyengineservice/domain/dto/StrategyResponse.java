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
    private String strategyName;
    private String status;
    private Integer priority;
    private String description;

    // Channel info
    private Channel channel;

    // Filter summary
    private Filters filters;

    // Schedule info
    private Schedule schedule;

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
    public static class Channel {
        private String type;
        private String templateId;  // Provider template ID (MSG91 template ID)
        private String templateName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Filters {
        // DPD Range (simplified response)
        private String dpdRange;  // "15-30" or human-readable format

        // Outstanding Amount (simplified response)
        private String outstandingAmount;  // "≥ 1000" or "1000-5000"

        // Text filters (singular names to match request)
        private List<String> language;
        private List<String> product;
        private List<String> pincode;
        private List<String> state;
        private List<String> bucket;

        // Total cases matching (for simulation)
        private Integer estimatedCasesMatched;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Schedule {
        private String frequency;      // DAILY, WEEKLY, MONTHLY
        private String time;            // HH:mm format
        private List<String> days;      // For DAILY/WEEKLY
        private Integer dayOfMonth;     // For MONTHLY
        private String scheduleText;    // Human readable: "Daily at 09:00"
        private LocalDateTime nextRunAt;
        private Boolean isEnabled;
    }
}
