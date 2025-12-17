package com.finx.collectionsservice.domain.dto;

import com.finx.collectionsservice.domain.enums.RuleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArchivalRuleDTO {
    private Long id;
    private String ruleCode;
    private String ruleName;
    private String description;
    private String criteria; // JSON string
    private String cronExpression;
    private String scheduleDescription;
    private RuleStatus status;
    private Integer executionCount;
    private LocalDateTime lastExecutionAt;
    private String lastExecutionResult;
    private Integer lastCasesArchived;
    private LocalDateTime nextExecutionAt;
    private Long totalCasesArchived;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime updatedAt;
    private Long updatedBy;
}
