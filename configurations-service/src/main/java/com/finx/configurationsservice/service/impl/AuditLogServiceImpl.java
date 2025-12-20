package com.finx.configurationsservice.service.impl;

import com.finx.configurationsservice.domain.entity.AuditLog;
import com.finx.configurationsservice.repository.AuditLogRepository;
import com.finx.configurationsservice.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public AuditLog log(String eventType, String entityType, String entityId, String entityName, String action,
                        Map<String, Object> oldValue, Map<String, Object> newValue, List<String> changedFields) {
        return logWithActor(eventType, entityType, entityId, entityName, action,
                null, null, null, null, oldValue, newValue, changedFields);
    }

    @Override
    @Transactional
    public AuditLog logWithActor(String eventType, String entityType, String entityId, String entityName, String action,
                                  Long actorId, String actorName, String actorEmail, String actorRole,
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
                .userId(actorId)
                .userName(actorName)
                .userRole(actorRole)
                .beforeValue(oldValue)
                .afterValue(newValue)
                .changedFields(changedFields)
                .description(generateChangeSummary(action, entityType, entityName, changedFields))
                .build();

        AuditLog saved = auditLogRepository.save(auditLog);
        log.debug("Audit log created: {} - {} - {}", entityType, entityId, action);
        return saved;
    }

    @Override
    @Transactional
    public AuditLog logCreate(String entityType, String entityId, String entityName, Map<String, Object> newValue) {
        return log(entityType.toUpperCase() + "_CREATED", entityType, entityId, entityName, "CREATE",
                null, newValue, null);
    }

    @Override
    @Transactional
    public AuditLog logUpdate(String entityType, String entityId, String entityName,
                              Map<String, Object> oldValue, Map<String, Object> newValue, List<String> changedFields) {
        return log(entityType.toUpperCase() + "_UPDATED", entityType, entityId, entityName, "UPDATE",
                oldValue, newValue, changedFields);
    }

    @Override
    @Transactional
    public AuditLog logDelete(String entityType, String entityId, String entityName, Map<String, Object> oldValue) {
        return log(entityType.toUpperCase() + "_DELETED", entityType, entityId, entityName, "DELETE",
                oldValue, null, null);
    }

    @Override
    @Transactional
    public AuditLog logView(String entityType, String entityId, String entityName) {
        return log(entityType.toUpperCase() + "_VIEWED", entityType, entityId, entityName, "VIEW",
                null, null, null);
    }

    @Override
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

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditTrailPaginated(String entityType, String entityId, Pageable pageable) {
        Long entityIdLong = null;
        try {
            if (entityId != null) {
                entityIdLong = Long.parseLong(entityId);
            }
        } catch (NumberFormatException e) {
            log.warn("Could not parse entityId to Long: {}", entityId);
        }
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityIdLong, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> searchAuditLogs(String serviceName, String eventType, String entityType,
                                           Long actorId, LocalDateTime startDate, LocalDateTime endDate,
                                           Pageable pageable) {
        // Map old parameters to new structure - serviceName/eventType not used directly
        return auditLogRepository.findWithFilters(eventType, entityType, actorId, startDate, endDate, pageable);
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
