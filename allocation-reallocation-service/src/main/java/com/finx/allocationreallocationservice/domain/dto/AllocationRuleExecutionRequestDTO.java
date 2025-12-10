package com.finx.allocationreallocationservice.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for applying allocation rules.
 * For both GEOGRAPHY and CAPACITY_BASED rules:
 * - Agents are auto-detected based on rule criteria
 * - All matching unallocated cases are allocated
 *
 * Simply pass an empty body: {}
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AllocationRuleExecutionRequestDTO {
    // No fields required - everything is auto-detected from the rule
    // Empty body {} is sufficient for apply
}
