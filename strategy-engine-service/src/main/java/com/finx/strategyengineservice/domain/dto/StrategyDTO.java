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
public class StrategyDTO {

    private Long id;

    private String name;

    private String status;

    private LocalDateTime lastRun;

    private Integer successCount;

    private Integer failureCount;

    private String triggerType;

    private String description;

    private LocalDateTime createdAt;
}
