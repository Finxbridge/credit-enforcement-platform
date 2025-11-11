package com.finx.strategyengineservice.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExecutionDTO {

    private String executionId;

    private Long strategyId;

    private String strategyName;

    private String status;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private Integer totalCasesProcessed;

    private Integer successfulActions;

    private Integer failedActions;
}
