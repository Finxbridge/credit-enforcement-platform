package com.finx.myworkflow.service;

import com.finx.myworkflow.domain.dto.AuditLogDTO;
import com.finx.myworkflow.domain.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AuditLogService {

    AuditLogDTO logAction(AuditAction action, String entityType, Long entityId, Long caseId,
                          Long userId, Map<String, Object> oldValues, Map<String, Object> newValues,
                          String description);

    AuditLogDTO logAction(AuditAction action, String entityType, Long entityId, Long caseId,
                          Long userId, String description);

    AuditLogDTO logAction(AuditAction action, String entityType, Long entityId, Long caseId,
                          Long userId, Map<String, Object> newValues, String description);

    AuditLogDTO getAuditLog(Long id);

    AuditLogDTO getAuditLogByAuditId(String auditId);

    Page<AuditLogDTO> getCaseAuditTrail(Long caseId, Pageable pageable);

    Page<AuditLogDTO> getUserAuditTrail(Long userId, Pageable pageable);

    Page<AuditLogDTO> getEntityAuditTrail(String entityType, Long entityId, Pageable pageable);

    Page<AuditLogDTO> getAuditLogsByAction(AuditAction action, Pageable pageable);

    Page<AuditLogDTO> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    List<AuditLogDTO> getRecentUserActivity(Long userId, int lastHours);

    Map<String, Long> getActionStatistics(int lastDays);
}
