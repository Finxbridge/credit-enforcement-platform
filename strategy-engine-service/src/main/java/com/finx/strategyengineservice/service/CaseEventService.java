package com.finx.strategyengineservice.service;

import com.finx.strategyengineservice.domain.entity.CaseEvent;
import com.finx.strategyengineservice.repository.CaseEventRepository;
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
 * Service for creating case events for strategy execution actions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CaseEventService {

    private static final String SOURCE_SERVICE = "strategy-engine-service";

    private final CaseEventRepository caseEventRepository;

    /**
     * Log SMS sent event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSmsSent(Long caseId, String loanAccountNumber, String templateCode, String status,
                           Long strategyId, String strategyName, String recipient) {
        try {
            CaseEvent event = CaseEvent.builder()
                    .caseId(caseId)
                    .loanAccountNumber(loanAccountNumber)
                    .eventType("SMS_SENT")
                    .eventCategory("COMMUNICATION")
                    .eventTitle("SMS Sent")
                    .eventDescription("SMS sent using template: " + templateCode + " to " + recipient)
                    .actorType("STRATEGY")
                    .actorName(strategyName)
                    .sourceService(SOURCE_SERVICE)
                    .communicationChannel("SMS")
                    .communicationStatus(status)
                    .relatedEntityType("Strategy")
                    .relatedEntityId(strategyId)
                    .eventTimestamp(LocalDateTime.now())
                    .metadata(createMetadata("templateCode", templateCode, "recipient", recipient, "status", status))
                    .build();

            caseEventRepository.save(event);
            log.debug("SMS event logged for case: {}", caseId);
        } catch (Exception e) {
            log.error("Failed to log SMS event for case {}: {}", caseId, e.getMessage());
        }
    }

    /**
     * Log Email sent event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logEmailSent(Long caseId, String loanAccountNumber, String templateCode, String status,
                             Long strategyId, String strategyName, String recipient) {
        try {
            CaseEvent event = CaseEvent.builder()
                    .caseId(caseId)
                    .loanAccountNumber(loanAccountNumber)
                    .eventType("EMAIL_SENT")
                    .eventCategory("COMMUNICATION")
                    .eventTitle("Email Sent")
                    .eventDescription("Email sent using template: " + templateCode + " to " + recipient)
                    .actorType("STRATEGY")
                    .actorName(strategyName)
                    .sourceService(SOURCE_SERVICE)
                    .communicationChannel("EMAIL")
                    .communicationStatus(status)
                    .relatedEntityType("Strategy")
                    .relatedEntityId(strategyId)
                    .eventTimestamp(LocalDateTime.now())
                    .metadata(createMetadata("templateCode", templateCode, "recipient", recipient, "status", status))
                    .build();

            caseEventRepository.save(event);
            log.debug("Email event logged for case: {}", caseId);
        } catch (Exception e) {
            log.error("Failed to log Email event for case {}: {}", caseId, e.getMessage());
        }
    }

    /**
     * Log WhatsApp sent event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logWhatsAppSent(Long caseId, String loanAccountNumber, String templateCode, String status,
                                Long strategyId, String strategyName, String recipient) {
        try {
            CaseEvent event = CaseEvent.builder()
                    .caseId(caseId)
                    .loanAccountNumber(loanAccountNumber)
                    .eventType("WHATSAPP_SENT")
                    .eventCategory("COMMUNICATION")
                    .eventTitle("WhatsApp Sent")
                    .eventDescription("WhatsApp message sent using template: " + templateCode + " to " + recipient)
                    .actorType("STRATEGY")
                    .actorName(strategyName)
                    .sourceService(SOURCE_SERVICE)
                    .communicationChannel("WHATSAPP")
                    .communicationStatus(status)
                    .relatedEntityType("Strategy")
                    .relatedEntityId(strategyId)
                    .eventTimestamp(LocalDateTime.now())
                    .metadata(createMetadata("templateCode", templateCode, "recipient", recipient, "status", status))
                    .build();

            caseEventRepository.save(event);
            log.debug("WhatsApp event logged for case: {}", caseId);
        } catch (Exception e) {
            log.error("Failed to log WhatsApp event for case {}: {}", caseId, e.getMessage());
        }
    }

    /**
     * Log Notice generated event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logNoticeGenerated(Long caseId, String loanAccountNumber, String noticeType, String status,
                                   Long strategyId, String strategyName, String recipientName) {
        try {
            CaseEvent event = CaseEvent.builder()
                    .caseId(caseId)
                    .loanAccountNumber(loanAccountNumber)
                    .eventType("NOTICE_GENERATED")
                    .eventSubtype(noticeType)
                    .eventCategory("COMMUNICATION")
                    .eventTitle("Notice Generated")
                    .eventDescription("Notice (" + noticeType + ") generated for " + recipientName)
                    .actorType("STRATEGY")
                    .actorName(strategyName)
                    .sourceService(SOURCE_SERVICE)
                    .communicationChannel("NOTICE")
                    .communicationStatus(status)
                    .relatedEntityType("Strategy")
                    .relatedEntityId(strategyId)
                    .eventTimestamp(LocalDateTime.now())
                    .metadata(createMetadata("noticeType", noticeType, "recipientName", recipientName, "status", status))
                    .build();

            caseEventRepository.save(event);
            log.debug("Notice event logged for case: {}", caseId);
        } catch (Exception e) {
            log.error("Failed to log Notice event for case {}: {}", caseId, e.getMessage());
        }
    }

    /**
     * Log strategy execution event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logStrategyExecution(Long caseId, String loanAccountNumber, Long strategyId, String strategyName,
                                      String executionStatus, int actionsExecuted) {
        try {
            CaseEvent event = CaseEvent.builder()
                    .caseId(caseId)
                    .loanAccountNumber(loanAccountNumber)
                    .eventType("STRATEGY_EXECUTED")
                    .eventCategory("WORKFLOW")
                    .eventTitle("Strategy Executed")
                    .eventDescription("Strategy '" + strategyName + "' executed with " + actionsExecuted + " actions")
                    .actorType("STRATEGY")
                    .actorName(strategyName)
                    .sourceService(SOURCE_SERVICE)
                    .relatedEntityType("Strategy")
                    .relatedEntityId(strategyId)
                    .newStatus(executionStatus)
                    .eventTimestamp(LocalDateTime.now())
                    .metadata(createMetadata("strategyId", strategyId, "strategyName", strategyName,
                            "actionsExecuted", actionsExecuted, "status", executionStatus))
                    .build();

            caseEventRepository.save(event);
            log.debug("Strategy execution event logged for case: {}", caseId);
        } catch (Exception e) {
            log.error("Failed to log strategy execution event for case {}: {}", caseId, e.getMessage());
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
