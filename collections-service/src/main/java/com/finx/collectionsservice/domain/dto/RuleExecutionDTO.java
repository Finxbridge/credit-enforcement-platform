package com.finx.collectionsservice.domain.dto;

import com.finx.collectionsservice.domain.enums.RuleExecutionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Rule Execution response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleExecutionDTO {

    private Long id;
    private String executionId;
    private Long ruleId;
    private String ruleCode;
    private String ruleName;
    private RuleExecutionStatus status;
    private Boolean isSimulation;
    private Integer totalEligible;
    private Integer totalProcessed;
    private Integer totalSuccess;
    private Integer totalFailed;
    private Integer totalSkipped;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Long durationMs;
    private String triggeredBy;
    private Long executedBy;
    private LocalDateTime createdAt;
}
