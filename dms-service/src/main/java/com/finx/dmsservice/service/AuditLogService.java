package com.finx.dmsservice.service;

import com.finx.dmsservice.domain.entity.AuditLog;
import com.finx.dmsservice.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logEvent(String entityType, String entityId, String entityName,
                         String action, Long actorId, String ipAddress, String summary) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .serviceName("dms-service")
                    .eventType(action)
                    .eventCategory("DOCUMENT")
                    .entityType(entityType)
                    .entityId(entityId)
                    .entityName(entityName)
                    .action(action)
                    .actorId(actorId)
                    .actorType("USER")
                    .ipAddress(ipAddress)
                    .changeSummary(summary)
                    .severity("INFO")
                    .eventTimestamp(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log created for {} {} - {}", entityType, entityId, action);
        } catch (Exception e) {
            log.error("Failed to create audit log for {} {}: {}", entityType, entityId, e.getMessage());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logNoticeEvent(Long noticeId, String noticeNumber, String action,
                               Long actorId, String summary) {
        logEvent("NOTICE_DOCUMENT", noticeId != null ? noticeId.toString() : null,
                noticeNumber, action, actorId, null, summary);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logDocumentEvent(Long documentId, String documentName, String action,
                                 Long actorId, String ipAddress, String summary) {
        logEvent("DOCUMENT", documentId != null ? documentId.toString() : null,
                documentName, action, actorId, ipAddress, summary);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logExportEvent(Long jobId, String action, Long actorId, String summary) {
        logEvent("EXPORT_JOB", jobId != null ? jobId.toString() : null,
                "Export Job " + jobId, action, actorId, null, summary);
    }
}
