package com.finx.configurationsservice.service;

import com.finx.configurationsservice.domain.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AuditLogService {

    AuditLog log(String eventType, String entityType, String entityId, String entityName, String action,
                 Map<String, Object> oldValue, Map<String, Object> newValue, List<String> changedFields);

    AuditLog logWithActor(String eventType, String entityType, String entityId, String entityName, String action,
                          Long actorId, String actorName, String actorEmail, String actorRole,
                          Map<String, Object> oldValue, Map<String, Object> newValue, List<String> changedFields);

    AuditLog logCreate(String entityType, String entityId, String entityName, Map<String, Object> newValue);

    AuditLog logUpdate(String entityType, String entityId, String entityName,
                       Map<String, Object> oldValue, Map<String, Object> newValue, List<String> changedFields);

    AuditLog logDelete(String entityType, String entityId, String entityName, Map<String, Object> oldValue);

    AuditLog logView(String entityType, String entityId, String entityName);

    List<AuditLog> getAuditTrail(String entityType, String entityId);

    Page<AuditLog> getAuditTrailPaginated(String entityType, String entityId, Pageable pageable);

    Page<AuditLog> searchAuditLogs(String serviceName, String eventType, String entityType,
                                    Long actorId, LocalDateTime startDate, LocalDateTime endDate,
                                    Pageable pageable);
}
