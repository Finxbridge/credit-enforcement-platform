package com.finx.strategyengineservice.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StrategyDetailDTO {

    private Long id;

    private String name;

    private String description;

    private String triggerType;

    private String scheduleExpression;

    private String eventType;

    private List<StrategyRuleDTO> rules;

    private List<StrategyActionDTO> actions;

    private String status;

    private Integer priority;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
