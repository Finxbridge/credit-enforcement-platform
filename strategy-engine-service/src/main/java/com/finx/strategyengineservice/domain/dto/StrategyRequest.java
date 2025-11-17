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

    @NotBlank(message = "Rule name is required")
    @Size(min = 3, max = 255, message = "Rule name must be between 3 and 255 characters")
    private String ruleName;  // Strategy name

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "DRAFT|ACTIVE|INACTIVE", message = "Status must be DRAFT, ACTIVE, or INACTIVE")
    private String status;  // DRAFT, ACTIVE, INACTIVE

    @NotNull(message = "Priority is required")
    @Min(value = 0, message = "Priority must be >= 0")
    @Max(value = 100, message = "Priority must be <= 100")
    private Integer priority;  // Strategy execution priority (0-100, higher = higher priority)

    private String description;

    // ===================================
    // 2. TEMPLATE SELECTION
    // ===================================

    @NotNull(message = "Template configuration is required")
    @Valid
    private TemplateConfig templateConfig;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateConfig {

        @NotBlank(message = "Template type is required")
        @Pattern(regexp = "SMS|WHATSAPP|EMAIL|IVR|NOTICE", message = "Template type must be SMS, WHATSAPP, EMAIL, IVR, or NOTICE")
        private String templateType;  // SMS, WHATSAPP, EMAIL, IVR, NOTICE

        @NotNull(message = "Template ID is required")
        private Long templateId;  // Selected from dropdown (campaign_templates.id)

        private String templateName;  // Display name (for reference)
    }

    // ===================================
    // 3. FILTERS
    // ===================================

    @NotNull(message = "Filter configuration is required")
    @Valid
    private FilterConfig filterConfig;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterConfig {

        // Numeric Filters
        private NumericFilter outstandingPrincipal;
        private NumericFilter paymentAmount;

        // Text Filters (multiple selections allowed)
        private List<String> languages;  // ['EN', 'HI', 'MR']
        private List<String> products;   // ['PERSONAL_LOAN', 'HOME_LOAN']
        private List<String> pincodes;   // ['400001', '400002']
        private List<String> states;     // ['MH', 'GJ', 'DL']
        private List<String> buckets;    // ['X1', 'X2', 'X3']

        // DPD Filter (special numeric filter)
        @NotNull(message = "DPD filter is required")
        @Valid
        private DpdFilter dpd;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NumericFilter {

        @NotBlank(message = "Operator is required")
        @Pattern(regexp = "EQUALS|GREATER_THAN|LESS_THAN|GREATER_THAN_OR_EQUAL|LESS_THAN_OR_EQUAL|BETWEEN",
                message = "Invalid operator")
        private String operator;  // EQUALS, GREATER_THAN, LESS_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN_OR_EQUAL, BETWEEN

        private Double value;  // For single value operators

        private Double minValue;  // For BETWEEN operator
        private Double maxValue;  // For BETWEEN operator
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DpdFilter {

        @NotBlank(message = "DPD operator is required")
        @Pattern(regexp = "EQUALS|GREATER_THAN|LESS_THAN|GREATER_THAN_OR_EQUAL|LESS_THAN_OR_EQUAL|BETWEEN",
                message = "Invalid DPD operator")
        private String operator;

        private Integer value;  // DPD value for single operators

        private Integer minDpd;  // For BETWEEN (e.g., 30-60 DPD)
        private Integer maxDpd;
    }

    // ===================================
    // 4. SCHEDULE CONFIGURATION
    // ===================================

    @NotNull(message = "Schedule configuration is required")
    @Valid
    private ScheduleConfig scheduleConfig;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleConfig {

        @NotBlank(message = "Frequency is required")
        @Pattern(regexp = "DAILY|WEEKLY|EVENT_BASED", message = "Frequency must be DAILY, WEEKLY, or EVENT_BASED")
        private String frequency;  // DAILY, WEEKLY, EVENT_BASED

        // For DAILY
        private String dailyTime;  // HH:mm format (e.g., "09:00")

        // For WEEKLY
        private List<String> weeklyDays;  // ['MONDAY', 'WEDNESDAY', 'FRIDAY']
        private String weeklyTime;  // HH:mm format

        // Timezone
        @Builder.Default
        private String timezone = "Asia/Kolkata";
    }
}
