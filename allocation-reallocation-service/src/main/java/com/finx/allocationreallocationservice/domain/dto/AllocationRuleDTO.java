package com.finx.allocationreallocationservice.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationRuleDTO {
    private Long id;

    @NotBlank(message = "Rule name is required")
    private String name;

    private String description;

    // Rule configuration - All mandatory fields
    @NotBlank(message = "Rule type is required (PERCENTAGE_SPLIT, CAPACITY_BASED, GEOGRAPHY)")
    private String ruleType;

    @NotEmpty(message = "At least one geography is required")
    private List<String> geographies;

    // Optional - can be used for filtering cases by bucket when applying rule
    private List<String> buckets;

    @NotNull(message = "Max cases per agent is required")
    @Positive(message = "Max cases per agent must be positive")
    private Integer maxCasesPerAgent;

    @NotEmpty(message = "At least one agent ID is required")
    private List<Long> agentIds;

    // Only required for PERCENTAGE_SPLIT rule type
    private List<Integer> percentages;

    // Legacy criteria field for backward compatibility
    private Map<String, Object> criteria;

    private String status;
    private Integer priority;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
