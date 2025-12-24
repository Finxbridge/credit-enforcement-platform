package com.finx.noticemanagementservice.service;

import com.finx.noticemanagementservice.domain.entity.CaseEvent;
import com.finx.noticemanagementservice.repository.CaseEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for creating case events for notice management actions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CaseEventService {

    private static final String SOURCE_SERVICE = "notice-management-service";

    private final CaseEventRepository caseEventRepository;

    /**
     * Log notice created event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logNoticeCreated(Long caseId, String loanAccountNumber, Long noticeId,
                                  String noticeType, String recipientName,
                                  Long userId, String userName) {
        try {
            CaseEvent event = CaseEvent.builder()
                    .caseId(caseId)
                    .loanAccountNumber(loanAccountNumber)
                    .eventType("NOTICE_CREATED")
                    .eventSubtype(noticeType)
                    .eventCategory("COMMUNICATION")
                    .eventTitle("Notice Created")
                    .eventDescription("Notice (" + noticeType + ") created for " + recipientName)
                    .actorId(userId)
                    .actorName(userName)
                    .actorType(userId != null ? "USER" : "SYSTEM")
                    .sourceService(SOURCE_SERVICE)
                    .relatedEntityType("Notice")
                    .relatedEntityId(noticeId)
                    .communicationChannel("NOTICE")
                    .communicationStatus("CREATED")
                    .newStatus("CREATED")
                    .eventTimestamp(LocalDateTime.now())
                    .metadata(createMetadata("noticeType", noticeType, "recipientName", recipientName))
                    .build();

            caseEventRepository.save(event);
            log.debug("Notice created event logged for case: {}", caseId);
        } catch (Exception e) {
            log.error("Failed to log notice created event for case {}: {}", caseId, e.getMessage());
        }
    }

    /**
     * Log notice generated/printed event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logNoticeGenerated(Long caseId, String loanAccountNumber, Long noticeId,
                                    String noticeType, String documentPath,
                                    Long userId, String userName) {
        try {
            CaseEvent event = CaseEvent.builder()
                    .caseId(caseId)
                    .loanAccountNumber(loanAccountNumber)
                    .eventType("NOTICE_GENERATED")
                    .eventSubtype(noticeType)
                    .eventCategory("COMMUNICATION")
                    .eventTitle("Notice Generated")
                    .eventDescription("Notice (" + noticeType + ") document generated")
                    .actorId(userId)
                    .actorName(userName)
                    .actorType(userId != null ? "USER" : "SYSTEM")
                    .sourceService(SOURCE_SERVICE)
                    .relatedEntityType("Notice")
                    .relatedEntityId(noticeId)
                    .communicationChannel("NOTICE")
                    .communicationStatus("GENERATED")
                    .oldStatus("CREATED")
                    .newStatus("GENERATED")
                    .eventTimestamp(LocalDateTime.now())
                    .metadata(createMetadata("noticeType", noticeType, "documentPath", documentPath))
                    .build();

            caseEventRepository.save(event);
            log.debug("Notice generated event logged for case: {}", caseId);
        } catch (Exception e) {
            log.error("Failed to log notice generated event for case {}: {}", caseId, e.getMessage());
        }
    }

    /**
     * Log notice dispatched event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logNoticeDispatched(Long caseId, String loanAccountNumber, Long noticeId,
                                     String noticeType, String dispatchMode, String trackingNumber,
                                     Long userId, String userName) {
        try {
            CaseEvent event = CaseEvent.builder()
                    .caseId(caseId)
                    .loanAccountNumber(loanAccountNumber)
                    .eventType("NOTICE_DISPATCHED")
                    .eventSubtype(noticeType)
                    .eventCategory("COMMUNICATION")
                    .eventTitle("Notice Dispatched")
                    .eventDescription("Notice (" + noticeType + ") dispatched via " + dispatchMode +
                            (trackingNumber != null ? " (Tracking: " + trackingNumber + ")" : ""))
                    .actorId(userId)
                    .actorName(userName)
                    .actorType(userId != null ? "USER" : "SYSTEM")
                    .sourceService(SOURCE_SERVICE)
                    .relatedEntityType("Notice")
                    .relatedEntityId(noticeId)
                    .communicationChannel("NOTICE")
                    .communicationStatus("DISPATCHED")
                    .oldStatus("GENERATED")
                    .newStatus("DISPATCHED")
                    .eventTimestamp(LocalDateTime.now())
                    .metadata(createMetadata("noticeType", noticeType, "dispatchMode", dispatchMode, "trackingNumber", trackingNumber))
                    .build();

            caseEventRepository.save(event);
            log.debug("Notice dispatched event logged for case: {}", caseId);
        } catch (Exception e) {
            log.error("Failed to log notice dispatched event for case {}: {}", caseId, e.getMessage());
        }
    }

    /**
     * Log notice delivered event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logNoticeDelivered(Long caseId, String loanAccountNumber, Long noticeId,
                                    String noticeType, String deliveryDate, String receivedBy,
                                    Long userId, String userName) {
        try {
            CaseEvent event = CaseEvent.builder()
                    .caseId(caseId)
                    .loanAccountNumber(loanAccountNumber)
                    .eventType("NOTICE_DELIVERED")
                    .eventSubtype(noticeType)
                    .eventCategory("COMMUNICATION")
                    .eventTitle("Notice Delivered")
                    .eventDescription("Notice (" + noticeType + ") delivered" +
                            (receivedBy != null ? " to " + receivedBy : "") +
                            (deliveryDate != null ? " on " + deliveryDate : ""))
                    .actorId(userId)
                    .actorName(userName)
                    .actorType(userId != null ? "USER" : "SYSTEM")
                    .sourceService(SOURCE_SERVICE)
                    .relatedEntityType("Notice")
                    .relatedEntityId(noticeId)
                    .communicationChannel("NOTICE")
                    .communicationStatus("DELIVERED")
                    .oldStatus("DISPATCHED")
                    .newStatus("DELIVERED")
                    .eventTimestamp(LocalDateTime.now())
                    .metadata(createMetadata("noticeType", noticeType, "deliveryDate", deliveryDate, "receivedBy", receivedBy))
                    .build();

            caseEventRepository.save(event);
            log.debug("Notice delivered event logged for case: {}", caseId);
        } catch (Exception e) {
            log.error("Failed to log notice delivered event for case {}: {}", caseId, e.getMessage());
        }
    }

    /**
     * Log notice RTO (Return to Origin) event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logNoticeRto(Long caseId, String loanAccountNumber, Long noticeId,
                              String noticeType, String rtoReason,
                              Long userId, String userName) {
        try {
            CaseEvent event = CaseEvent.builder()
                    .caseId(caseId)
                    .loanAccountNumber(loanAccountNumber)
                    .eventType("NOTICE_RTO")
                    .eventSubtype(noticeType)
                    .eventCategory("COMMUNICATION")
                    .eventTitle("Notice RTO")
                    .eventDescription("Notice (" + noticeType + ") returned to origin" +
                            (rtoReason != null ? ". Reason: " + rtoReason : ""))
                    .actorId(userId)
                    .actorName(userName)
                    .actorType(userId != null ? "USER" : "SYSTEM")
                    .sourceService(SOURCE_SERVICE)
                    .relatedEntityType("Notice")
                    .relatedEntityId(noticeId)
                    .communicationChannel("NOTICE")
                    .communicationStatus("RTO")
                    .oldStatus("DISPATCHED")
                    .newStatus("RTO")
                    .eventTimestamp(LocalDateTime.now())
                    .metadata(createMetadata("noticeType", noticeType, "rtoReason", rtoReason))
                    .build();

            caseEventRepository.save(event);
            log.debug("Notice RTO event logged for case: {}", caseId);
        } catch (Exception e) {
            log.error("Failed to log notice RTO event for case {}: {}", caseId, e.getMessage());
        }
    }

    /**
     * Log notice cancelled event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logNoticeCancelled(Long caseId, String loanAccountNumber, Long noticeId,
                                    String noticeType, String reason,
                                    Long userId, String userName) {
        try {
            CaseEvent event = CaseEvent.builder()
                    .caseId(caseId)
                    .loanAccountNumber(loanAccountNumber)
                    .eventType("NOTICE_CANCELLED")
                    .eventSubtype(noticeType)
                    .eventCategory("COMMUNICATION")
                    .eventTitle("Notice Cancelled")
                    .eventDescription("Notice (" + noticeType + ") cancelled" +
                            (reason != null ? ". Reason: " + reason : ""))
                    .actorId(userId)
                    .actorName(userName)
                    .actorType("USER")
                    .sourceService(SOURCE_SERVICE)
                    .relatedEntityType("Notice")
                    .relatedEntityId(noticeId)
                    .communicationChannel("NOTICE")
                    .communicationStatus("CANCELLED")
                    .newStatus("CANCELLED")
                    .eventTimestamp(LocalDateTime.now())
                    .metadata(createMetadata("noticeType", noticeType, "reason", reason))
                    .build();

            caseEventRepository.save(event);
            log.debug("Notice cancelled event logged for case: {}", caseId);
        } catch (Exception e) {
            log.error("Failed to log notice cancelled event for case {}: {}", caseId, e.getMessage());
        }
    }

    /**
     * Log bulk notice generation event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logBulkNoticeGeneration(String noticeType, int totalNotices, int successCount, int failedCount,
                                         Long userId, String userName) {
        try {
            CaseEvent event = CaseEvent.builder()
                    .eventType("BULK_NOTICE_GENERATED")
                    .eventSubtype(noticeType)
                    .eventCategory("COMMUNICATION")
                    .eventTitle("Bulk Notice Generation")
                    .eventDescription("Bulk notice generation completed: " + successCount + " success, " + failedCount + " failed out of " + totalNotices)
                    .actorId(userId)
                    .actorName(userName)
                    .actorType(userId != null ? "USER" : "SYSTEM")
                    .sourceService(SOURCE_SERVICE)
                    .communicationChannel("NOTICE")
                    .communicationStatus("COMPLETED")
                    .eventTimestamp(LocalDateTime.now())
                    .metadata(createMetadata("noticeType", noticeType, "totalNotices", totalNotices,
                            "successCount", successCount, "failedCount", failedCount))
                    .build();

            caseEventRepository.save(event);
            log.debug("Bulk notice generation event logged");
        } catch (Exception e) {
            log.error("Failed to log bulk notice generation event: {}", e.getMessage());
        }
    }

    private Map<String, Object> createMetadata(Object... keyValues) {
        Map<String, Object> metadata = new HashMap<>();
        for (int i = 0; i < keyValues.length - 1; i += 2) {
            if (keyValues[i + 1] != null) {
                metadata.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
            }
        }
        return metadata;
    }
}
