package com.finx.auth.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActiveSessionDTO {
    private String sessionId;
    private String deviceType;
    private String ipAddress;
    private LocalDateTime lastActivityAt;
    private String userAgent;
}
