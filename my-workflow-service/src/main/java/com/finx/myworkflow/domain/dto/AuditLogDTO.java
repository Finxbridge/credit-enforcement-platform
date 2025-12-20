package com.finx.myworkflow.domain.dto;

import com.finx.myworkflow.domain.enums.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {
    private Long id;
    private String auditId;
    private AuditAction action;
    private String entityType;
    private Long entityId;
    private Long caseId;
    private Long userId;
    private String userName;
    private String oldValues;  // JSON string
    private String newValues;  // JSON string
    private String changes;    // JSON string
    private String description;
    private String ipAddress;
    private LocalDateTime createdAt;
}
