package com.finx.allocationreallocationservice.service;

import com.finx.allocationreallocationservice.domain.entity.CaseEvent;
import com.finx.allocationreallocationservice.repository.CaseEventRepository;
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
 * Service for creating case events for allocation actions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CaseEventService {

    private static final String SOURCE_SERVICE = "allocation-reallocation-service";

    private final CaseEventRepository caseEventRepository;

    /**
     * Log case allocation event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logCaseAllocated(Long caseId, String loanAccountNumber, Long toUserId, String toUserName,
                                  Long allocatedBy, String allocatedByName, String reason) {
        try {
            CaseEvent event = CaseEvent.builder()
                    .caseId(caseId)
                    .loanAccountNumber(loanAccountNumber)
                    .eventType("CASE_ALLOCATED")
                    .eventCategory("ALLOCATION")
                    .eventTitle("Case Allocated")
                    .eventDescription("Case allocated to " + toUserName + (reason != null ? ". Reason: " + reason : ""))
                    .actorId(allocatedBy)
                    .actorName(allocatedByName)
                    .actorType(allocatedBy != null ? "USER" : "SYSTEM")
                    .sourceService(SOURCE_SERVICE)
                    .toAgentId(toUserId)
                    .oldStatus("UNALLOCATED")
                    .newStatus("ALLOCATED")
                    .eventTimestamp(LocalDateTime.now())
                    .metadata(createMetadata("toUserId", toUserId, "toUserName", toUserName, "reason", reason))
                    .build();

            caseEventRepository.save(event);
            log.debug("Case allocation event logged for case: {}", caseId);
        } catch (Exception e) {
            log.error("Failed to log case allocation event for case {}: {}", caseId, e.getMessage());
        }
    }

    /**
     * Log case reallocation event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logCaseReallocated(Long caseId, String loanAccountNumber,
                                    Long fromUserId, String fromUserName,
                                    Long toUserId, String toUserName,
                                    Long reallocatedBy, String reallocatedByName, String reason) {
        try {
            CaseEvent event = CaseEvent.builder()
                    .caseId(caseId)
                    .loanAccountNumber(loanAccountNumber)
                    .eventType("CASE_REALLOCATED")
                    .eventCategory("ALLOCATION")
                    .eventTitle("Case Reallocated")
                    .eventDescription("Case reallocated from " + fromUserName + " to " + toUserName +
                            (reason != null ? ". Reason: " + reason : ""))
                    .actorId(reallocatedBy)
                    .actorName(reallocatedByName)
                    .actorType(reallocatedBy != null ? "USER" : "SYSTEM")
                    .sourceService(SOURCE_SERVICE)
                    .fromAgentId(fromUserId)
                    .toAgentId(toUserId)
                    .newStatus("ALLOCATED")
                    .eventTimestamp(LocalDateTime.now())
                    .metadata(createMetadata(
                            "fromUserId", fromUserId, "fromUserName", fromUserName,
                            "toUserId", toUserId, "toUserName", toUserName, "reason", reason))
                    .build();

            caseEventRepository.save(event);
            log.debug("Case reallocation event logged for case: {}", caseId);
        } catch (Exception e) {
            log.error("Failed to log case reallocation event for case {}: {}", caseId, e.getMessage());
        }
    }

    /**
     * Log case deallocation event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logCaseDeallocated(Long caseId, String loanAccountNumber,
                                    Long fromUserId, String fromUserName,
                                    Long deallocatedBy, String deallocatedByName, String reason) {
        try {
            CaseEvent event = CaseEvent.builder()
                    .caseId(caseId)
                    .loanAccountNumber(loanAccountNumber)
                    .eventType("CASE_DEALLOCATED")
                    .eventCategory("ALLOCATION")
                    .eventTitle("Case Deallocated")
                    .eventDescription("Case deallocated from " + fromUserName +
                            (reason != null ? ". Reason: " + reason : ""))
                    .actorId(deallocatedBy)
                    .actorName(deallocatedByName)
                    .actorType(deallocatedBy != null ? "USER" : "SYSTEM")
                    .sourceService(SOURCE_SERVICE)
                    .fromAgentId(fromUserId)
                    .oldStatus("ALLOCATED")
                    .newStatus("UNALLOCATED")
                    .eventTimestamp(LocalDateTime.now())
                    .metadata(createMetadata("fromUserId", fromUserId, "fromUserName", fromUserName, "reason", reason))
                    .build();

            caseEventRepository.save(event);
            log.debug("Case deallocation event logged for case: {}", caseId);
        } catch (Exception e) {
            log.error("Failed to log case deallocation event for case {}: {}", caseId, e.getMessage());
        }
    }

    /**
     * Log bulk allocation event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logBulkAllocation(String batchId, int totalCases, int successCount, int failureCount,
                                   Long allocatedBy, String allocatedByName, String ruleId) {
        try {
            CaseEvent event = CaseEvent.builder()
                    .caseId(0L) // System event
                    .eventType("BULK_ALLOCATION_COMPLETED")
                    .eventCategory("ALLOCATION")
                    .eventTitle("Bulk Allocation Completed")
                    .eventDescription(String.format("Bulk allocation completed: %d total, %d success, %d failed",
                            totalCases, successCount, failureCount))
                    .actorId(allocatedBy)
                    .actorName(allocatedByName)
                    .actorType(allocatedBy != null ? "USER" : "SYSTEM")
                    .sourceService(SOURCE_SERVICE)
                    .relatedEntityType("AllocationBatch")
                    .eventTimestamp(LocalDateTime.now())
                    .metadata(createMetadata(
                            "batchId", batchId,
                            "totalCases", totalCases,
                            "successCount", successCount,
                            "failureCount", failureCount,
                            "ruleId", ruleId))
                    .build();

            caseEventRepository.save(event);
            log.debug("Bulk allocation event logged for batch: {}", batchId);
        } catch (Exception e) {
            log.error("Failed to log bulk allocation event for batch {}: {}", batchId, e.getMessage());
        }
    }

    /**
     * Log rule-based allocation event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logRuleBasedAllocation(Long caseId, String loanAccountNumber, Long ruleId, String ruleName,
                                        Long toUserId, String toUserName, Long triggeredBy, String triggeredByName) {
        try {
            CaseEvent event = CaseEvent.builder()
                    .caseId(caseId)
                    .loanAccountNumber(loanAccountNumber)
                    .eventType("RULE_BASED_ALLOCATION")
                    .eventSubtype(ruleName)
                    .eventCategory("ALLOCATION")
                    .eventTitle("Rule-Based Allocation")
                    .eventDescription("Case allocated via rule: " + ruleName + " to " + toUserName)
                    .actorId(triggeredBy)
                    .actorName(triggeredByName)
                    .actorType("SYSTEM")
                    .sourceService(SOURCE_SERVICE)
                    .relatedEntityType("AllocationRule")
                    .relatedEntityId(ruleId)
                    .toAgentId(toUserId)
                    .oldStatus("UNALLOCATED")
                    .newStatus("ALLOCATED")
                    .eventTimestamp(LocalDateTime.now())
                    .metadata(createMetadata("ruleId", ruleId, "ruleName", ruleName, "toUserId", toUserId))
                    .build();

            caseEventRepository.save(event);
            log.debug("Rule-based allocation event logged for case: {}", caseId);
        } catch (Exception e) {
            log.error("Failed to log rule-based allocation event for case {}: {}", caseId, e.getMessage());
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
