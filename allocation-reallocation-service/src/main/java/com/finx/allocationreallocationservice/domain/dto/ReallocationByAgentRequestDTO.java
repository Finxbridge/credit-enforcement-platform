package com.finx.allocationreallocationservice.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReallocationByAgentRequestDTO {
    /**
     * Source agent - can be numeric ID, username, or full name (firstName lastName)
     * Examples: "123", "john.doe", "John Doe"
     */
    @NotBlank(message = "fromAgent is required")
    private String fromAgent;

    /**
     * Target agent - can be numeric ID, username, or full name (firstName lastName)
     * Examples: "123", "john.doe", "John Doe"
     */
    @NotBlank(message = "toAgent is required")
    private String toAgent;

    private String reason;

    // Legacy support - if numeric IDs are passed directly
    private Long fromUserId;
    private Long toUserId;
}
