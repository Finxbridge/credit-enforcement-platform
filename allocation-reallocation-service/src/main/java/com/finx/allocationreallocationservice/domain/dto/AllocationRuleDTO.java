package com.finx.allocationreallocationservice.domain.dto;

import jakarta.validation.constraints.NotBlank;
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

    // Rule type: GEOGRAPHY or CAPACITY_BASED
    @NotBlank(message = "Rule type is required (GEOGRAPHY or CAPACITY_BASED)")
    private String ruleType;

    // Geography filtering (required for GEOGRAPHY rule type)
    private List<String> states;      // Filter by state (e.g., "Telangana", "Maharashtra")
    private List<String> cities;      // Filter by city (e.g., "Hyderabad", "Mumbai")

    // Legacy criteria field for backward compatibility
    private Map<String, Object> criteria;

    private String status;
    private Integer priority;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
