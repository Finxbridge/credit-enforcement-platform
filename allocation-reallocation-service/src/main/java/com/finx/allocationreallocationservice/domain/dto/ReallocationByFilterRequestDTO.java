package com.finx.allocationreallocationservice.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReallocationByFilterRequestDTO {
    private Map<String, Object> filterCriteria;

    /**
     * Target agent - can be numeric ID, username, or full name (firstName lastName)
     * Examples: "123", "john.doe", "John Doe"
     */
    @NotBlank(message = "toAgent is required")
    private String toAgent;

    private String reason;

    // Legacy support - if numeric ID is passed directly
    private Long toUserId;
}
