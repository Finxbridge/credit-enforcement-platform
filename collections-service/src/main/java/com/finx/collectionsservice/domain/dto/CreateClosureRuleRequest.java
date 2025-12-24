package com.finx.collectionsservice.domain.dto;

import com.finx.collectionsservice.domain.enums.ClosureRuleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a closure rule
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateClosureRuleRequest {

    @NotBlank(message = "Rule name is required")
    @Size(max = 100, message = "Rule name must not exceed 100 characters")
    private String ruleName;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Rule type is required")
    private ClosureRuleType ruleType;

    @Size(max = 100, message = "Cron expression must not exceed 100 characters")
    private String cronExpression;

    private Boolean isScheduled;

    @NotBlank(message = "Closure reason is required")
    @Size(max = 100, message = "Closure reason must not exceed 100 characters")
    private String closureReason;

    // Minimum days with zero outstanding before closure (default: 0)
    private Integer minZeroOutstandingDays;

    // Minimum days of inactivity before closure
    private Integer minInactivityDays;

    // Include only specific buckets (comma-separated: X,1,2,3)
    private String includeBuckets;

    // Exclude specific case statuses (comma-separated)
    private String excludeStatuses;

    private Boolean isActive;

    private Integer priority;
}
