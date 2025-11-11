package com.finx.strategyengineservice.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateStrategyRequest {

    @NotBlank(message = "Strategy name is required")
    private String name;

    private String description;

    private String strategyType;

    @NotNull(message = "Trigger type is required")
    private String triggerType;

    private String scheduleExpression;

    private String eventType;

    private List<StrategyRuleDTO> rules;

    private List<StrategyActionDTO> actions;

    private String status;

    private Integer priority;
}
