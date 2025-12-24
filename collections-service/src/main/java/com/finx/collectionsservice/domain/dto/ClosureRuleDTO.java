package com.finx.collectionsservice.domain.dto;

import com.finx.collectionsservice.domain.enums.ClosureRuleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Closure Rule response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClosureRuleDTO {

    private Long id;
    private String ruleCode;
    private String ruleName;
    private String description;
    private ClosureRuleType ruleType;
    private String cronExpression;
    private Boolean isScheduled;
    private String closureReason;
    private Integer minZeroOutstandingDays;
    private Integer minInactivityDays;
    private String includeBuckets;
    private String excludeStatuses;
    private Boolean isActive;
    private Integer priority;
    private LocalDateTime lastExecutedAt;
    private Integer lastExecutionCount;
    private Long totalCasesClosed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;

    // Computed field for next scheduled run
    private String nextScheduledRun;
}
