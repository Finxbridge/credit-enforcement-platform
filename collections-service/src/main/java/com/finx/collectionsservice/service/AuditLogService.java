package com.finx.collectionsservice.service;

import com.finx.collectionsservice.domain.entity.AuditLog;
import com.finx.collectionsservice.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private static final String SERVICE_NAME = "collections-service";
    private static final String EVENT_CATEGORY = "COLLECTIONS";

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public AuditLog log(String eventType, String entityType, Long entityId, String entityName, String action,
                        Map<String, Object> oldValue, Map<String, Object> newValue, List<String> changedFields) {
        AuditLog auditLog = AuditLog.builder()
                .auditId(UUID.randomUUID().toString().substring(0, 36))
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .beforeValue(oldValue)
                .afterValue(newValue)
                .changedFields(changedFields)
                .description(generateChangeSummary(action, entityType, entityName, changedFields))
                .createdAt(LocalDateTime.now())
                .build();

        AuditLog saved = auditLogRepository.save(auditLog);
        log.debug("Audit log created: {} - {} - {} - {}", eventType, entityType, entityId, action);
        return saved;
    }

    @Transactional
    public AuditLog logCreate(String entityType, Long entityId, String entityName, Map<String, Object> newValue) {
        return log(entityType.toUpperCase() + "_CREATED", entityType, entityId, entityName, "CREATE",
                null, newValue, null);
    }

    @Transactional
    public AuditLog logUpdate(String entityType, Long entityId, String entityName,
                              Map<String, Object> oldValue, Map<String, Object> newValue, List<String> changedFields) {
        return log(entityType.toUpperCase() + "_UPDATED", entityType, entityId, entityName, "UPDATE",
                oldValue, newValue, changedFields);
    }

    @Transactional
    public AuditLog logDelete(String entityType, Long entityId, String entityName, Map<String, Object> oldValue) {
        return log(entityType.toUpperCase() + "_DELETED", entityType, entityId, entityName, "DELETE",
                oldValue, null, null);
    }

    @Transactional
    public AuditLog logStatusChange(String entityType, Long entityId, String entityName,
                                     String oldStatus, String newStatus) {
        return log(entityType.toUpperCase() + "_STATUS_CHANGED", entityType, entityId, entityName, "UPDATE",
                Map.of("status", oldStatus), Map.of("status", newStatus), List.of("status"));
    }

    @Transactional
    public AuditLog logApprovalAction(String entityType, Long entityId, String entityName,
                                       String action, String actorName, String remarks) {
        return log("APPROVAL_" + action.toUpperCase(), entityType, entityId, entityName, action,
                null, Map.of("action", action, "actor", actorName, "remarks", remarks != null ? remarks : ""), null);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getAuditTrail(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
    }

    /**
     * Simple event logging method for backward compatibility
     */
    @Transactional
    public void logEvent(String eventType, String entityType, Long entityId, String entityName,
                         Long userId, Object details, String action) {
        log(eventType, entityType, entityId, entityName, action,
                null, details != null ? Map.of("details", details) : null, null);
    }

    private String generateChangeSummary(String action, String entityType, String entityName, List<String> changedFields) {
        StringBuilder summary = new StringBuilder();
        summary.append(action).append(" ").append(entityType);
        if (entityName != null) {
            summary.append(" '").append(entityName).append("'");
        }
        if (changedFields != null && !changedFields.isEmpty()) {
            summary.append(". Changed fields: ").append(String.join(", ", changedFields));
        }
        return summary.toString();
    }

    private String determineSeverity(String action) {
        return switch (action.toUpperCase()) {
            case "DELETE", "REJECT" -> "WARNING";
            case "APPROVE", "CREATE", "UPDATE" -> "INFO";
            default -> "INFO";
        };
    }
}
