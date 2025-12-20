package com.finx.communication.domain.dto.dialer;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for agent status update (login, logout, break, available)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialerAgentStatusRequest {

    @NotNull(message = "Agent ID is required")
    private Long agentId;

    @NotNull(message = "Status is required")
    private String status;              // LOGIN, LOGOUT, AVAILABLE, BREAK, BUSY, WRAP_UP

    private String breakReason;         // For BREAK status
    private String agentExtension;      // Softphone extension
    private String agentQueue;          // Queue assignment
}
