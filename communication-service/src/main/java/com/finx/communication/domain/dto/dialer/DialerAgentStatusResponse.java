package com.finx.communication.domain.dto.dialer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for agent status response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialerAgentStatusResponse {

    private Long agentId;
    private String agentName;
    private String status;
    private String previousStatus;
    private LocalDateTime statusChangedAt;
    private LocalDateTime loginTime;
    private Integer totalTalkTime;      // Today's talk time in seconds
    private Integer callsHandled;       // Today's call count
    private String currentQueue;
    private String message;

    // For batch status query
    private List<AgentStatus> agents;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgentStatus {
        private Long agentId;
        private String agentName;
        private String status;
        private String currentCallId;
        private Integer pendingCallsInQueue;
        private LocalDateTime lastActivityAt;
    }
}
