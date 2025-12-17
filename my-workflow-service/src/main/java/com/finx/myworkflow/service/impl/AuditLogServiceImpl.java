package com.finx.myworkflow.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finx.myworkflow.domain.dto.AuditLogDTO;
import com.finx.myworkflow.domain.entity.AuditLog;
import com.finx.myworkflow.domain.enums.AuditAction;
import com.finx.myworkflow.exception.ResourceNotFoundException;
import com.finx.myworkflow.repository.AuditLogRepository;
import com.finx.myworkflow.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLogDTO logAction(AuditAction action, String entityType, Long entityId, Long caseId,
                                  Long userId, Map<String, Object> oldValues, Map<String, Object> newValues,
                                  String description) {
        log.debug("Logging audit action: {} on {}:{}", action, entityType, entityId);

        Map<String, Object> changes = computeChanges(oldValues, newValues);

        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .caseId(caseId)
                .userId(userId)
                .oldValues(toJson(oldValues))
                .newValues(toJson(newValues))
                .changes(toJson(changes))
                .description(description)
                .build();

        auditLog = auditLogRepository.save(auditLog);
        log.info("Audit log created: {} for action {} on {}:{}", auditLog.getAuditId(), action, entityType, entityId);

        return toDTO(auditLog);
    }

    private String toJson(Map<String, Object> map) {
        if (map == null) return null;
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize map to JSON", e);
            return null;
        }
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLogDTO logAction(AuditAction action, String entityType, Long entityId, Long caseId,
                                  Long userId, String description) {
        return logAction(action, entityType, entityId, caseId, userId, null, null, description);
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLogDTO logAction(AuditAction action, String entityType, Long entityId, Long caseId,
                                  Long userId, Map<String, Object> newValues, String description) {
        return logAction(action, entityType, entityId, caseId, userId, null, newValues, description);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLogDTO getAuditLog(Long id) {
        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AuditLog", "id", id));
        return toDTO(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLogDTO getAuditLogByAuditId(String auditId) {
        AuditLog auditLog = auditLogRepository.findByAuditId(auditId)
                .orElseThrow(() -> new ResourceNotFoundException("AuditLog", "auditId", auditId));
        return toDTO(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> getCaseAuditTrail(Long caseId, Pageable pageable) {
        return auditLogRepository.findByCaseIdOrderByCreatedAtDesc(caseId, pageable)
                .map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> getUserAuditTrail(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> getEntityAuditTrail(String entityType, Long entityId, Pageable pageable) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId, pageable)
                .map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> getAuditLogsByAction(AuditAction action, Pageable pageable) {
        return auditLogRepository.findByActionOrderByCreatedAtDesc(action, pageable)
                .map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.findByDateRange(startDate, endDate, pageable)
                .map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDTO> getRecentUserActivity(Long userId, int lastHours) {
        LocalDateTime since = LocalDateTime.now().minusHours(lastHours);
        return auditLogRepository.findRecentUserActivity(userId, since).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getActionStatistics(int lastDays) {
        LocalDateTime since = LocalDateTime.now().minusDays(lastDays);
        List<Object[]> stats = auditLogRepository.getActionStatistics(since);

        Map<String, Long> result = new HashMap<>();
        for (Object[] stat : stats) {
            String action = stat[0] != null ? stat[0].toString() : "UNKNOWN";
            Long count = stat[1] != null ? (Long) stat[1] : 0L;
            result.put(action, count);
        }
        return result;
    }

    private Map<String, Object> computeChanges(Map<String, Object> oldValues, Map<String, Object> newValues) {
        if (oldValues == null || newValues == null) {
            return null;
        }

        Map<String, Object> changes = new HashMap<>();
        for (Map.Entry<String, Object> entry : newValues.entrySet()) {
            String key = entry.getKey();
            Object newValue = entry.getValue();
            Object oldValue = oldValues.get(key);

            if (!java.util.Objects.equals(oldValue, newValue)) {
                Map<String, Object> change = new HashMap<>();
                change.put("old", oldValue);
                change.put("new", newValue);
                changes.put(key, change);
            }
        }
        return changes.isEmpty() ? null : changes;
    }

    private AuditLogDTO toDTO(AuditLog auditLog) {
        return AuditLogDTO.builder()
                .id(auditLog.getId())
                .auditId(auditLog.getAuditId())
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .caseId(auditLog.getCaseId())
                .userId(auditLog.getUserId())
                .userName(auditLog.getUserName())
                .oldValues(auditLog.getOldValues())
                .newValues(auditLog.getNewValues())
                .changes(auditLog.getChanges())
                .description(auditLog.getDescription())
                .ipAddress(auditLog.getIpAddress())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }
}
