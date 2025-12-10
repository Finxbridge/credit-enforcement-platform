package com.finx.allocationreallocationservice.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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

    // Legacy field - kept for backward compatibility
    @Deprecated
    private List<String> geographies;

    // New multi-field geography filtering (at least one must be provided)
    private List<String> states;      // Filter by state (e.g., "Maharashtra", "Karnataka")
    private List<String> cities;      // Filter by city (e.g., "Mumbai", "Pune")
    private List<String> locations;   // Filter by location/branch (e.g., "BRANCH_001")

    // Optional - can be used for filtering cases by bucket when applying rule
    private List<String> buckets;

    private Integer maxCasesPerAgent;

    // Optional - will be configured later via separate endpoint
    private List<Long> agentIds;

    // Optional - will be configured later via separate endpoint
    private List<Integer> percentages;

    // Legacy criteria field for backward compatibility
    private Map<String, Object> criteria;

    private String status;
    private Integer priority;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
