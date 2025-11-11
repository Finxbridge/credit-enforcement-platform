package com.finx.allocationreallocationservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {
    private String auditId;
    private String entityType;
    private Long entityId;
    private String action;
    private Long userId;
    private String username;
    private LocalDateTime timestamp;
    private Map<String, ChangeDTO> changes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangeDTO {
        private Object old;
        private Object newValue;
    }
}
