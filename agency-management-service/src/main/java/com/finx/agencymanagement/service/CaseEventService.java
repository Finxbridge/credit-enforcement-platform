package com.finx.agencymanagement.service;

import com.finx.agencymanagement.domain.entity.CaseEvent;
import com.finx.agencymanagement.repository.CaseEventRepository;
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
 * Service for creating case events for agency management actions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CaseEventService {

    private static final String SOURCE_SERVICE = "agency-management-service";

    private final CaseEventRepository caseEventRepository;

    /**
     * Log case allocated to agency event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logCaseAllocatedToAgency(Long caseId, String loanAccountNumber, Long agencyId,
                                          String agencyCode, String agencyName,
                                          Long allocatedBy, String allocatedByName) {
        try {
            CaseEvent event = CaseEvent.builder()
                    .caseId(caseId)
                    .loanAccountNumber(loanAccountNumber)
                    .eventType("AGENCY_ALLOCATED")
                    .eventCategory("ALLOCATION")
                    .eventTitle("Case Allocated to Agency")
                    .eventDescription("Case allocated to agency: " + agencyName + " (" + agencyCode + ")")
                    .actorId(allocatedBy)
                    .actorName(allocatedByName)
                    .actorType("USER")
                    .sourceService(SOURCE_SERVICE)
                    .relatedEntityType("Agency")
                    .relatedEntityId(agencyId)
                    .oldStatus("ALLOCATED")
                    .newStatus("ALLOCATED_TO_AGENCY")
                    .eventTimestamp(LocalDateTime.now())
                    .metadata(createMetadata("agencyId", agencyId, "agencyCode", agencyCode, "agencyName", agencyName))
                    .build();

            caseEventRepository.save(event);
            log.debug("Agency allocation event logged for case: {}", caseId);
        } catch (Exception e) {
            log.error("Failed to log agency allocation event for case {}: {}", caseId, e.getMessage());
        }
    }

    /**
     * Log case deallocated from agency event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logCaseDeallocatedFromAgency(Long caseId, String loanAccountNumber, Long agencyId,
                                              String agencyCode, String agencyName, String reason,
                                              Long deallocatedBy, String deallocatedByName) {
        try {
            CaseEvent event = CaseEvent.builder()
                    .caseId(caseId)
                    .loanAccountNumber(loanAccountNumber)
                    .eventType("AGENCY_DEALLOCATED")
                    .eventCategory("ALLOCATION")
                    .eventTitle("Case Deallocated from Agency")
                    .eventDescription("Case deallocated from agency: " + agencyName +
                            (reason != null ? ". Reason: " + reason : ""))
                    .actorId(deallocatedBy)
                    .actorName(deallocatedByName)
                    .actorType("USER")
                    .sourceService(SOURCE_SERVICE)
                    .relatedEntityType("Agency")
                    .relatedEntityId(agencyId)
                    .oldStatus("ALLOCATED_TO_AGENCY")
                    .newStatus("ALLOCATED")
                    .eventTimestamp(LocalDateTime.now())
                    .metadata(createMetadata("agencyId", agencyId, "agencyCode", agencyCode, "reason", reason))
                    .build();

            caseEventRepository.save(event);
            log.debug("Agency deallocation event logged for case: {}", caseId);
        } catch (Exception e) {
            log.error("Failed to log agency deallocation event for case {}: {}", caseId, e.getMessage());
        }
    }

    /**
     * Log case assigned to agent event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logCaseAssignedToAgent(Long caseId, String loanAccountNumber, Long agentId,
                                        String agentName, Long agencyId, String agencyName,
                                        Long assignedBy, String assignedByName) {
        try {
            CaseEvent event = CaseEvent.builder()
                    .caseId(caseId)
                    .loanAccountNumber(loanAccountNumber)
                    .eventType("AGENT_ASSIGNED")
                    .eventCategory("ALLOCATION")
                    .eventTitle("Case Assigned to Agent")
                    .eventDescription("Case assigned to agent: " + agentName + " (Agency: " + agencyName + ")")
                    .actorId(assignedBy)
                    .actorName(assignedByName)
                    .actorType("USER")
                    .sourceService(SOURCE_SERVICE)
                    .toAgentId(agentId)
                    .relatedEntityType("Agency")
                    .relatedEntityId(agencyId)
                    .oldStatus("ALLOCATED_TO_AGENCY")
                    .newStatus("ASSIGNED_TO_AGENT")
                    .eventTimestamp(LocalDateTime.now())
                    .metadata(createMetadata("agentId", agentId, "agentName", agentName,
                            "agencyId", agencyId, "agencyName", agencyName))
                    .build();

            caseEventRepository.save(event);
            log.debug("Agent assignment event logged for case: {}", caseId);
        } catch (Exception e) {
            log.error("Failed to log agent assignment event for case {}: {}", caseId, e.getMessage());
        }
    }

    /**
     * Log case reassigned to different agent event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logCaseReassignedToAgent(Long caseId, String loanAccountNumber,
                                          Long fromAgentId, String fromAgentName,
                                          Long toAgentId, String toAgentName,
                                          Long agencyId, String agencyName,
                                          Long reassignedBy, String reassignedByName, String reason) {
        try {
            CaseEvent event = CaseEvent.builder()
                    .caseId(caseId)
                    .loanAccountNumber(loanAccountNumber)
                    .eventType("AGENT_REASSIGNED")
                    .eventCategory("ALLOCATION")
                    .eventTitle("Case Reassigned to Agent")
                    .eventDescription("Case reassigned from " + fromAgentName + " to " + toAgentName +
                            (reason != null ? ". Reason: " + reason : ""))
                    .actorId(reassignedBy)
                    .actorName(reassignedByName)
                    .actorType("USER")
                    .sourceService(SOURCE_SERVICE)
                    .fromAgentId(fromAgentId)
                    .toAgentId(toAgentId)
                    .relatedEntityType("Agency")
                    .relatedEntityId(agencyId)
                    .newStatus("ASSIGNED_TO_AGENT")
                    .eventTimestamp(LocalDateTime.now())
                    .metadata(createMetadata(
                            "fromAgentId", fromAgentId, "fromAgentName", fromAgentName,
                            "toAgentId", toAgentId, "toAgentName", toAgentName,
                            "agencyId", agencyId, "reason", reason))
                    .build();

            caseEventRepository.save(event);
            log.debug("Agent reassignment event logged for case: {}", caseId);
        } catch (Exception e) {
            log.error("Failed to log agent reassignment event for case {}: {}", caseId, e.getMessage());
        }
    }

    /**
     * Log case unassigned from agent event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logCaseUnassignedFromAgent(Long caseId, String loanAccountNumber,
                                            Long agentId, String agentName, String reason,
                                            Long unassignedBy, String unassignedByName) {
        try {
            CaseEvent event = CaseEvent.builder()
                    .caseId(caseId)
                    .loanAccountNumber(loanAccountNumber)
                    .eventType("AGENT_UNASSIGNED")
                    .eventCategory("ALLOCATION")
                    .eventTitle("Case Unassigned from Agent")
                    .eventDescription("Case unassigned from agent: " + agentName +
                            (reason != null ? ". Reason: " + reason : ""))
                    .actorId(unassignedBy)
                    .actorName(unassignedByName)
                    .actorType("USER")
                    .sourceService(SOURCE_SERVICE)
                    .fromAgentId(agentId)
                    .oldStatus("ASSIGNED_TO_AGENT")
                    .newStatus("ALLOCATED_TO_AGENCY")
                    .eventTimestamp(LocalDateTime.now())
                    .metadata(createMetadata("agentId", agentId, "agentName", agentName, "reason", reason))
                    .build();

            caseEventRepository.save(event);
            log.debug("Agent unassignment event logged for case: {}", caseId);
        } catch (Exception e) {
            log.error("Failed to log agent unassignment event for case {}: {}", caseId, e.getMessage());
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
