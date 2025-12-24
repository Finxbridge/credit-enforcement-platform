package com.finx.casesourcingservice.service;

import com.finx.casesourcingservice.domain.entity.CaseEvent;
import com.finx.casesourcingservice.repository.CaseEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Service for creating case events in the centralized case_events table.
 * Events are created asynchronously to not impact main transaction performance.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CaseEventService {

    private static final String SOURCE_SERVICE = "case-sourcing-service";

    private final CaseEventRepository caseEventRepository;

    /**
     * Log a case creation event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logCaseCreated(Long caseId, String loanAccountNumber, Long userId, String userName,
                               String batchId, String sourceType) {
        try {
            CaseEvent event = CaseEvent.builder()
                    .caseId(caseId)
                    .loanAccountNumber(loanAccountNumber)
                    .eventType("CASE_CREATED")
                    .eventCategory("SYSTEM")
                    .eventTitle("Case Created")
                    .eventDescription("New case created from " + sourceType + " upload")
                    .actorId(userId)
                    .actorName(userName)
                    .actorType("USER")
                    .sourceService(SOURCE_SERVICE)
                    .relatedEntityType("CaseBatch")
                    .newStatus("UNALLOCATED")
                    .eventTimestamp(LocalDateTime.now())
                    .metadata(Map.of(
                            "batchId", batchId != null ? batchId : "",
                            "sourceType", sourceType != null ? sourceType : "CSV"
                    ))
                    .build();

            caseEventRepository.save(event);
            log.debug("Case creation event logged for case: {}", caseId);
        } catch (Exception e) {
            log.error("Failed to log case creation event for case {}: {}", caseId, e.getMessage());
        }
    }

    /**
     * Log a case status change event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logCaseStatusChange(Long caseId, String loanAccountNumber, String oldStatus, String newStatus,
                                    Long userId, String userName, String reason) {
        try {
            CaseEvent event = CaseEvent.builder()
                    .caseId(caseId)
                    .loanAccountNumber(loanAccountNumber)
                    .eventType("STATUS_CHANGED")
                    .eventCategory("WORKFLOW")
                    .eventTitle("Case Status Changed")
                    .eventDescription("Case status changed from " + oldStatus + " to " + newStatus +
                            (reason != null ? ". Reason: " + reason : ""))
                    .actorId(userId)
                    .actorName(userName)
                    .actorType(userId != null ? "USER" : "SYSTEM")
                    .sourceService(SOURCE_SERVICE)
                    .oldStatus(oldStatus)
                    .newStatus(newStatus)
                    .eventTimestamp(LocalDateTime.now())
                    .build();

            caseEventRepository.save(event);
            log.debug("Case status change event logged for case: {} ({} -> {})", caseId, oldStatus, newStatus);
        } catch (Exception e) {
            log.error("Failed to log case status change event for case {}: {}", caseId, e.getMessage());
        }
    }

    /**
     * Log a batch upload event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logBatchUpload(String batchId, int totalRecords, int successCount, int failureCount,
                               Long userId, String userName, String fileName) {
        try {
            // Create a system-level event (no specific case)
            CaseEvent event = CaseEvent.builder()
                    .caseId(0L) // System event
                    .eventType("BATCH_UPLOAD_COMPLETED")
                    .eventCategory("SYSTEM")
                    .eventTitle("Batch Upload Completed")
                    .eventDescription(String.format("Batch upload completed: %d total, %d success, %d failed",
                            totalRecords, successCount, failureCount))
                    .actorId(userId)
                    .actorName(userName)
                    .actorType("USER")
                    .sourceService(SOURCE_SERVICE)
                    .relatedEntityType("CaseBatch")
                    .eventTimestamp(LocalDateTime.now())
                    .metadata(Map.of(
                            "batchId", batchId,
                            "fileName", fileName != null ? fileName : "",
                            "totalRecords", totalRecords,
                            "successCount", successCount,
                            "failureCount", failureCount
                    ))
                    .build();

            caseEventRepository.save(event);
            log.debug("Batch upload event logged for batch: {}", batchId);
        } catch (Exception e) {
            log.error("Failed to log batch upload event for batch {}: {}", batchId, e.getMessage());
        }
    }

    /**
     * Log a case closure event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logCaseClosure(Long caseId, String loanAccountNumber, String closureReason,
                               Long userId, String userName) {
        try {
            CaseEvent event = CaseEvent.builder()
                    .caseId(caseId)
                    .loanAccountNumber(loanAccountNumber)
                    .eventType("CASE_CLOSED")
                    .eventCategory("WORKFLOW")
                    .eventTitle("Case Closed")
                    .eventDescription("Case closed. Reason: " + closureReason)
                    .actorId(userId)
                    .actorName(userName)
                    .actorType(userId != null ? "USER" : "SYSTEM")
                    .sourceService(SOURCE_SERVICE)
                    .oldStatus("ACTIVE")
                    .newStatus("CLOSED")
                    .eventTimestamp(LocalDateTime.now())
                    .metadata(Map.of("closureReason", closureReason != null ? closureReason : ""))
                    .build();

            caseEventRepository.save(event);
            log.debug("Case closure event logged for case: {}", caseId);
        } catch (Exception e) {
            log.error("Failed to log case closure event for case {}: {}", caseId, e.getMessage());
        }
    }

    /**
     * Log a case reopen event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logCaseReopen(Long caseId, String loanAccountNumber, String reason,
                              Long userId, String userName) {
        try {
            CaseEvent event = CaseEvent.builder()
                    .caseId(caseId)
                    .loanAccountNumber(loanAccountNumber)
                    .eventType("CASE_REOPENED")
                    .eventCategory("WORKFLOW")
                    .eventTitle("Case Reopened")
                    .eventDescription("Case reopened. Reason: " + reason)
                    .actorId(userId)
                    .actorName(userName)
                    .actorType(userId != null ? "USER" : "SYSTEM")
                    .sourceService(SOURCE_SERVICE)
                    .oldStatus("CLOSED")
                    .newStatus("ACTIVE")
                    .eventTimestamp(LocalDateTime.now())
                    .build();

            caseEventRepository.save(event);
            log.debug("Case reopen event logged for case: {}", caseId);
        } catch (Exception e) {
            log.error("Failed to log case reopen event for case {}: {}", caseId, e.getMessage());
        }
    }

    /**
     * Generic event logging method
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logEvent(Long caseId, String loanAccountNumber, String eventType, String eventCategory,
                         String title, String description, Long userId, String userName,
                         Map<String, Object> metadata) {
        try {
            CaseEvent event = CaseEvent.builder()
                    .caseId(caseId)
                    .loanAccountNumber(loanAccountNumber)
                    .eventType(eventType)
                    .eventCategory(eventCategory)
                    .eventTitle(title)
                    .eventDescription(description)
                    .actorId(userId)
                    .actorName(userName)
                    .actorType(userId != null ? "USER" : "SYSTEM")
                    .sourceService(SOURCE_SERVICE)
                    .eventTimestamp(LocalDateTime.now())
                    .metadata(metadata)
                    .build();

            caseEventRepository.save(event);
            log.debug("Event logged for case {}: {}", caseId, eventType);
        } catch (Exception e) {
            log.error("Failed to log event {} for case {}: {}", eventType, caseId, e.getMessage());
        }
    }
}
