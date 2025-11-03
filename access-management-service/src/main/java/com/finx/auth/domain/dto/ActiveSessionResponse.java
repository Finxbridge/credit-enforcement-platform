package com.finx.auth.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Active Session Response DTO
 * Purpose: Return active sessions for user
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActiveSessionResponse {

    private Long userId;
    private String username;
    private List<SessionInfo> sessions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SessionInfo {
        private String sessionId;
        private String deviceType;
        private String ipAddress;
        private LocalDateTime lastActivityAt;
        private LocalDateTime expiresAt;
        private Boolean isActive;
        private LocalDateTime createdAt;
    }
}
