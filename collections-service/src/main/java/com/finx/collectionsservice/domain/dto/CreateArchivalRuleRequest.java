package com.finx.collectionsservice.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateArchivalRuleRequest {

    @NotBlank(message = "Rule code is required")
    private String ruleCode;

    @NotBlank(message = "Rule name is required")
    private String ruleName;

    private String description;

    @NotBlank(message = "Criteria is required")
    private String criteria; // JSON string

    private String cronExpression;

    private String scheduleDescription;

    private Long createdBy;
}
