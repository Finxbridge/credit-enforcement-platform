package com.finx.noticemanagementservice.service;

import com.finx.noticemanagementservice.domain.entity.AuditLog;
import com.finx.noticemanagementservice.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public AuditLog log(String eventType, String entityType, String entityId, String entityName, String action,
                        Map<String, Object> oldValue, Map<String, Object> newValue, List<String> changedFields) {
        // Parse entityId to Long if possible
        Long entityIdLong = null;
        try {
            if (entityId != null) {
                entityIdLong = Long.parseLong(entityId);
            }
        } catch (NumberFormatException e) {
            log.warn("Could not parse entityId to Long: {}", entityId);
        }

        AuditLog auditLog = AuditLog.builder()
                .entityType(entityType)
                .entityId(entityIdLong)
                .action(action)
                .beforeValue(oldValue)
                .afterValue(newValue)
                .changedFields(changedFields)
                .description(generateChangeSummary(action, entityType, entityName, changedFields))
                .build();

        AuditLog saved = auditLogRepository.save(auditLog);
        log.debug("Audit log created: {} - {} - {}", entityType, entityId, action);
        return saved;
    }

    @Transactional
    public AuditLog logCreate(String entityType, String entityId, String entityName, Map<String, Object> newValue) {
        return log(entityType.toUpperCase() + "_CREATED", entityType, entityId, entityName, "CREATE",
                null, newValue, null);
    }

    @Transactional
    public AuditLog logUpdate(String entityType, String entityId, String entityName,
                              Map<String, Object> oldValue, Map<String, Object> newValue, List<String> changedFields) {
        return log(entityType.toUpperCase() + "_UPDATED", entityType, entityId, entityName, "UPDATE",
                oldValue, newValue, changedFields);
    }

    @Transactional
    public AuditLog logDelete(String entityType, String entityId, String entityName, Map<String, Object> oldValue) {
        return log(entityType.toUpperCase() + "_DELETED", entityType, entityId, entityName, "DELETE",
                oldValue, null, null);
    }

    @Transactional
    public AuditLog logStatusChange(String entityType, String entityId, String entityName,
                                     String oldStatus, String newStatus) {
        return log(entityType.toUpperCase() + "_STATUS_CHANGED", entityType, entityId, entityName, "UPDATE",
                Map.of("status", oldStatus), Map.of("status", newStatus), List.of("status"));
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getAuditTrail(String entityType, String entityId) {
        Long entityIdLong = null;
        try {
            if (entityId != null) {
                entityIdLong = Long.parseLong(entityId);
            }
        } catch (NumberFormatException e) {
            log.warn("Could not parse entityId to Long: {}", entityId);
            return List.of();
        }
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityIdLong);
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
}
