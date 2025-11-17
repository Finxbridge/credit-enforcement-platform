package com.finx.strategyengineservice.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request for creating/updating strategy with all configurations in a single call
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyRequest {

    // ===================================
    // 1. BASIC STRATEGY INFO
    // ===================================

    @NotBlank(message = "Strategy name is required")
    @Size(min = 3, max = 255, message = "Strategy name must be between 3 and 255 characters")
    private String strategyName;

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "DRAFT|ACTIVE|INACTIVE", message = "Status must be DRAFT, ACTIVE, or INACTIVE")
    private String status;  // DRAFT, ACTIVE, INACTIVE

    @NotNull(message = "Priority is required")
    @Min(value = 0, message = "Priority must be >= 0")
    @Max(value = 100, message = "Priority must be <= 100")
    private Integer priority;  // Strategy execution priority (0-100, higher = higher priority)

    private String description;

    // ===================================
    // 2. CHANNEL CONFIGURATION
    // ===================================

    @NotNull(message = "Channel configuration is required")
    @Valid
    private Channel channel;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Channel {

        @NotBlank(message = "Channel type is required")
        @Pattern(regexp = "SMS|WHATSAPP|EMAIL|IVR|NOTICE", message = "Channel type must be SMS, WHATSAPP, EMAIL, IVR, or NOTICE")
        private String type;  // SMS, WHATSAPP, EMAIL, IVR, NOTICE

        @NotBlank(message = "Template name is required")
        private String templateName;  // Template name/code to lookup template ID

        // Optional: Can be provided directly or looked up from templateName
        private Long templateId;
    }

    // ===================================
    // 3. FILTERS
    // ===================================

    @NotNull(message = "Filters configuration is required")
    @Valid
    private Filters filters;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Filters {

        // Text Filters (multiple selections allowed)
        private List<String> language;   // ['ENGLISH', 'TELUGU', 'HINDI']
        private List<String> product;    // ['PERSONAL_LOAN', 'HOME_LOAN']
        private List<String> pincode;    // ['500001', '500072']
        private List<String> state;      // ['TELANGANA', 'ANDHRA PRADESH']
        private List<String> bucket;     // ['B1', 'B2', 'B3']

        // DPD Range Filter (required)
        @NotNull(message = "DPD range is required")
        @Valid
        private DpdRange dpdRange;

        // Outstanding Amount Filter (optional, supports simple value or range)
        private Double outstandingAmount;  // Simple filter: >= this amount
        private OutstandingRange outstandingRange;  // Advanced: custom range
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DpdRange {

        @NotNull(message = "DPD 'from' value is required")
        @Min(value = 0, message = "DPD 'from' must be >= 0")
        private Integer from;

        @NotNull(message = "DPD 'to' value is required")
        @Min(value = 0, message = "DPD 'to' must be >= 0")
        private Integer to;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OutstandingRange {

        @NotNull(message = "Outstanding 'from' value is required")
        @Min(value = 0, message = "Outstanding 'from' must be >= 0")
        private Double from;

        @NotNull(message = "Outstanding 'to' value is required")
        @Min(value = 0, message = "Outstanding 'to' must be >= 0")
        private Double to;
    }

    // ===================================
    // 4. SCHEDULE CONFIGURATION
    // ===================================

    @NotNull(message = "Schedule configuration is required")
    @Valid
    private Schedule schedule;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Schedule {

        @NotBlank(message = "Frequency is required")
        @Pattern(regexp = "DAILY|WEEKLY|MONTHLY", message = "Frequency must be DAILY, WEEKLY, or MONTHLY")
        private String frequency;  // DAILY, WEEKLY, MONTHLY

        @NotBlank(message = "Time is required")
        @Pattern(regexp = "^([01]\\d|2[0-3]):([0-5]\\d)$", message = "Time must be in HH:mm format (e.g., 10:30)")
        private String time;  // HH:mm format (e.g., "10:30")

        // Days of week for DAILY and WEEKLY frequency
        // For DAILY: Use ["DAILY"] or list specific days like ["MONDAY", "TUESDAY"]
        // For WEEKLY: List specific days like ["MONDAY", "WEDNESDAY", "FRIDAY"]
        private List<String> days;

        // Day of month for MONTHLY frequency (1-31)
        @Min(value = 1, message = "Day of month must be between 1 and 31")
        @Max(value = 31, message = "Day of month must be between 1 and 31")
        private Integer dayOfMonth;

        // Timezone
        @Builder.Default
        private String timezone = "Asia/Kolkata";
    }
}
