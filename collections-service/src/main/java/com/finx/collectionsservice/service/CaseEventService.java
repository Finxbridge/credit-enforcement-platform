package com.finx.collectionsservice.service;

import com.finx.collectionsservice.domain.entity.CaseEvent;
import com.finx.collectionsservice.repository.CaseEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for creating case events for collections actions (Repayment, OTS, PTP).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CaseEventService {

    private static final String SOURCE_SERVICE = "collections-service";

    private final CaseEventRepository caseEventRepository;

    // ==================== REPAYMENT EVENTS ====================

    /**
     * Log repayment created event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logRepaymentCreated(Long caseId, String loanAccountNumber, Long repaymentId,
                                     BigDecimal amount, String paymentMode, Long userId, String userName) {
        try {
            CaseEvent event = CaseEvent.builder()
                    .caseId(caseId)
                    .loanAccountNumber(loanAccountNumber)
                    .eventType("REPAYMENT_CREATED")
                    .eventCategory("COLLECTION")
                    .eventTitle("Repayment Recorded")
                    .eventDescription("Repayment of " + amount + " recorded via " + paymentMode)
                    .actorId(userId)
                    .actorName(userName)
                    .actorType("USER")
                    .sourceService(SOURCE_SERVICE)
                    .relatedEntityType("Repayment")
                    .relatedEntityId(repaymentId)
                    .paymentAmount(amount)
                    .paymentMode(paymentMode)
                    .newStatus("PENDING_APPROVAL")
                    .eventTimestamp(LocalDateTime.now())
                    .metadata(createMetadata("repaymentId", repaymentId, "amount", amount, "paymentMode", paymentMode))
                    .build();

            caseEventRepository.save(event);
            log.debug("Repayment created event logged for case: {}", caseId);
        } catch (Exception e) {
            log.error("Failed to log repayment created event for case {}: {}", caseId, e.getMessage());
        }
    }

    /**
     * Log repayment approved event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logRepaymentApproved(Long caseId, String loanAccountNumber, Long repaymentId,
                                      BigDecimal amount, Long approvedBy, String approverName) {
        try {
            CaseEvent event = CaseEvent.builder()
                    .caseId(caseId)
                    .loanAccountNumber(loanAccountNumber)
                    .eventType("REPAYMENT_APPROVED")
                    .eventCategory("COLLECTION")
                    .eventTitle("Repayment Approved")
                    .eventDescription("Repayment of " + amount + " approved by " + approverName)
                    .actorId(approvedBy)
                    .actorName(approverName)
                    .actorType("USER")
                    .sourceService(SOURCE_SERVICE)
                    .relatedEntityType("Repayment")
                    .relatedEntityId(repaymentId)
                    .paymentAmount(amount)
                    .oldStatus("PENDING_APPROVAL")
                    .newStatus("APPROVED")
                    .eventTimestamp(LocalDateTime.now())
                    .build();

            caseEventRepository.save(event);
            log.debug("Repayment approved event logged for case: {}", caseId);
        } catch (Exception e) {
            log.error("Failed to log repayment approved event for case {}: {}", caseId, e.getMessage());
        }
    }

    /**
     * Log repayment rejected event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logRepaymentRejected(Long caseId, String loanAccountNumber, Long repaymentId,
                                      BigDecimal amount, String reason, Long rejectedBy, String rejecterName) {
        try {
            CaseEvent event = CaseEvent.builder()
                    .caseId(caseId)
                    .loanAccountNumber(loanAccountNumber)
                    .eventType("REPAYMENT_REJECTED")
                    .eventCategory("COLLECTION")
                    .eventTitle("Repayment Rejected")
                    .eventDescription("Repayment of " + amount + " rejected. Reason: " + reason)
                    .actorId(rejectedBy)
                    .actorName(rejecterName)
                    .actorType("USER")
                    .sourceService(SOURCE_SERVICE)
                    .relatedEntityType("Repayment")
                    .relatedEntityId(repaymentId)
                    .paymentAmount(amount)
                    .oldStatus("PENDING_APPROVAL")
                    .newStatus("REJECTED")
                    .eventTimestamp(LocalDateTime.now())
                    .metadata(createMetadata("reason", reason))
                    .build();

            caseEventRepository.save(event);
            log.debug("Repayment rejected event logged for case: {}", caseId);
        } catch (Exception e) {
            log.error("Failed to log repayment rejected event for case {}: {}", caseId, e.getMessage());
        }
    }

    // ==================== PTP EVENTS ====================

    /**
     * Log PTP captured event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logPtpCaptured(Long caseId, String loanAccountNumber, Long ptpId,
                                BigDecimal amount, LocalDate ptpDate, Long userId, String userName) {
        try {
            CaseEvent event = CaseEvent.builder()
                    .caseId(caseId)
                    .loanAccountNumber(loanAccountNumber)
                    .eventType("PTP_CAPTURED")
                    .eventCategory("COLLECTION")
                    .eventTitle("PTP Commitment Captured")
                    .eventDescription("PTP commitment of " + amount + " captured for " + ptpDate)
                    .actorId(userId)
                    .actorName(userName)
                    .actorType("USER")
                    .sourceService(SOURCE_SERVICE)
                    .relatedEntityType("PTPCommitment")
                    .relatedEntityId(ptpId)
                    .ptpAmount(amount)
                    .ptpDate(ptpDate)
                    .ptpStatus("ACTIVE")
                    .newStatus("ACTIVE")
                    .eventTimestamp(LocalDateTime.now())
                    .build();

            caseEventRepository.save(event);
            log.debug("PTP captured event logged for case: {}", caseId);
        } catch (Exception e) {
            log.error("Failed to log PTP captured event for case {}: {}", caseId, e.getMessage());
        }
    }

    /**
     * Log PTP status change event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logPtpStatusChange(Long caseId, String loanAccountNumber, Long ptpId,
                                    BigDecimal amount, LocalDate ptpDate, String oldStatus, String newStatus,
                                    Long userId, String userName, String reason) {
        try {
            String description = "PTP status changed from " + oldStatus + " to " + newStatus;
            if (reason != null) {
                description += ". Reason: " + reason;
            }

            CaseEvent event = CaseEvent.builder()
                    .caseId(caseId)
                    .loanAccountNumber(loanAccountNumber)
                    .eventType("PTP_STATUS_CHANGED")
                    .eventSubtype(newStatus)
                    .eventCategory("COLLECTION")
                    .eventTitle("PTP Status Changed")
                    .eventDescription(description)
                    .actorId(userId)
                    .actorName(userName)
                    .actorType(userId != null ? "USER" : "SYSTEM")
                    .sourceService(SOURCE_SERVICE)
                    .relatedEntityType("PTPCommitment")
                    .relatedEntityId(ptpId)
                    .ptpAmount(amount)
                    .ptpDate(ptpDate)
                    .ptpStatus(newStatus)
                    .oldStatus(oldStatus)
                    .newStatus(newStatus)
                    .eventTimestamp(LocalDateTime.now())
                    .metadata(createMetadata("reason", reason))
                    .build();

            caseEventRepository.save(event);
            log.debug("PTP status change event logged for case: {}", caseId);
        } catch (Exception e) {
            log.error("Failed to log PTP status change event for case {}: {}", caseId, e.getMessage());
        }
    }

    // ==================== OTS EVENTS ====================

    /**
     * Log OTS created event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logOtsCreated(Long caseId, String loanAccountNumber, Long otsId,
                               BigDecimal otsAmount, BigDecimal originalAmount,
                               Long userId, String userName) {
        try {
            BigDecimal discount = originalAmount != null && otsAmount != null ?
                    originalAmount.subtract(otsAmount) : null;

            CaseEvent event = CaseEvent.builder()
                    .caseId(caseId)
                    .loanAccountNumber(loanAccountNumber)
                    .eventType("OTS_CREATED")
                    .eventCategory("COLLECTION")
                    .eventTitle("OTS Request Created")
                    .eventDescription("OTS request created for " + otsAmount +
                            (discount != null ? " (Discount: " + discount + ")" : ""))
                    .actorId(userId)
                    .actorName(userName)
                    .actorType("USER")
                    .sourceService(SOURCE_SERVICE)
                    .relatedEntityType("OTS")
                    .relatedEntityId(otsId)
                    .paymentAmount(otsAmount)
                    .newStatus("PENDING_APPROVAL")
                    .eventTimestamp(LocalDateTime.now())
                    .metadata(createMetadata("otsAmount", otsAmount, "originalAmount", originalAmount, "discount", discount))
                    .build();

            caseEventRepository.save(event);
            log.debug("OTS created event logged for case: {}", caseId);
        } catch (Exception e) {
            log.error("Failed to log OTS created event for case {}: {}", caseId, e.getMessage());
        }
    }

    /**
     * Log OTS approved event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logOtsApproved(Long caseId, String loanAccountNumber, Long otsId,
                                BigDecimal otsAmount, Long approvedBy, String approverName) {
        try {
            CaseEvent event = CaseEvent.builder()
                    .caseId(caseId)
                    .loanAccountNumber(loanAccountNumber)
                    .eventType("OTS_APPROVED")
                    .eventCategory("COLLECTION")
                    .eventTitle("OTS Approved")
                    .eventDescription("OTS request for " + otsAmount + " approved by " + approverName)
                    .actorId(approvedBy)
                    .actorName(approverName)
                    .actorType("USER")
                    .sourceService(SOURCE_SERVICE)
                    .relatedEntityType("OTS")
                    .relatedEntityId(otsId)
                    .paymentAmount(otsAmount)
                    .oldStatus("PENDING_APPROVAL")
                    .newStatus("APPROVED")
                    .eventTimestamp(LocalDateTime.now())
                    .build();

            caseEventRepository.save(event);
            log.debug("OTS approved event logged for case: {}", caseId);
        } catch (Exception e) {
            log.error("Failed to log OTS approved event for case {}: {}", caseId, e.getMessage());
        }
    }

    /**
     * Log OTS rejected event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logOtsRejected(Long caseId, String loanAccountNumber, Long otsId,
                                BigDecimal otsAmount, String reason, Long rejectedBy, String rejecterName) {
        try {
            CaseEvent event = CaseEvent.builder()
                    .caseId(caseId)
                    .loanAccountNumber(loanAccountNumber)
                    .eventType("OTS_REJECTED")
                    .eventCategory("COLLECTION")
                    .eventTitle("OTS Rejected")
                    .eventDescription("OTS request for " + otsAmount + " rejected. Reason: " + reason)
                    .actorId(rejectedBy)
                    .actorName(rejecterName)
                    .actorType("USER")
                    .sourceService(SOURCE_SERVICE)
                    .relatedEntityType("OTS")
                    .relatedEntityId(otsId)
                    .paymentAmount(otsAmount)
                    .oldStatus("PENDING_APPROVAL")
                    .newStatus("REJECTED")
                    .eventTimestamp(LocalDateTime.now())
                    .metadata(createMetadata("reason", reason))
                    .build();

            caseEventRepository.save(event);
            log.debug("OTS rejected event logged for case: {}", caseId);
        } catch (Exception e) {
            log.error("Failed to log OTS rejected event for case {}: {}", caseId, e.getMessage());
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
