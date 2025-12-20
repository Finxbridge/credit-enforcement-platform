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
        private String templateName;  // Template name/code for reference

        // Template ID from template-management-service (numeric ID like 23)
        // Used to fetch template details and resolve variables
        private Long templateId;
    }

    // ===================================
    // 3. FILTERS (New Flexible Multi-Filter System)
    // ===================================

    /**
     * List of filters - user can add multiple filters of any type
     * Each filter can be:
     * - Text Filter (Language, Product, State, Pincode)
     * - Numeric Filter (Overdue Amount, DPD, EMI Amount, etc.)
     * - Date Filter (Due Date, Disbursement Date, etc.)
     */
    @Valid
    private List<FilterDTO> filters;

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
