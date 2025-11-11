package com.finx.allocationreallocationservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finx.allocationreallocationservice.client.UserManagementClient;
import com.finx.allocationreallocationservice.client.dto.UserDTO;
import com.finx.allocationreallocationservice.domain.dto.*;
import com.finx.allocationreallocationservice.domain.entity.*;
import com.finx.allocationreallocationservice.domain.enums.AllocationAction;
import com.finx.allocationreallocationservice.domain.enums.AllocationStatus;
import com.finx.allocationreallocationservice.domain.enums.BatchStatus;
import com.finx.allocationreallocationservice.domain.enums.RuleStatus;
import com.finx.allocationreallocationservice.exception.BusinessException;
import com.finx.allocationreallocationservice.repository.*;
import com.finx.allocationreallocationservice.service.AllocationService;
import com.finx.allocationreallocationservice.util.csv.CsvExporter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.finx.allocationreallocationservice.client.CaseSourcingClient;
import com.finx.allocationreallocationservice.client.dto.UnallocatedCaseDTO;
import com.finx.allocationreallocationservice.client.dto.CustomPageImpl;
import org.springframework.data.domain.Page;
import com.finx.allocationreallocationservice.service.async.AllocationBatchProcessingService;
import org.springframework.web.multipart.MultipartFile;
import com.finx.allocationreallocationservice.exception.ResourceNotFoundException;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronization;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AllocationServiceImpl implements AllocationService {

    private final AllocationBatchRepository allocationBatchRepository;
    private final AllocationRuleRepository allocationRuleRepository;
    private final CaseAllocationRepository caseAllocationRepository;
    private final AllocationHistoryRepository allocationHistoryRepository;
    private final BatchErrorRepository batchErrorRepository;
    private final ContactUpdateBatchRepository contactUpdateBatchRepository;
    private final AllocationBatchProcessingService batchProcessingService;
    private final CsvExporter csvExporter;
    private final ObjectMapper objectMapper;
    private final UserManagementClient userManagementClient;
    private final AuditLogRepository auditLogRepository;
    private final CaseSourcingClient caseSourcingClient;

    @Override
    @Transactional
    public AllocationBatchUploadResponseDTO uploadAllocationBatch(MultipartFile file) {
        log.info("Processing allocation batch upload: {}", file.getOriginalFilename());

        String batchId = "ALLOC_BATCH_" + System.currentTimeMillis();

        AllocationBatch batch = AllocationBatch.builder()
                .batchId(batchId)
                .totalCases(0)
                .successfulAllocations(0)
                .failedAllocations(0)
                .status(BatchStatus.PROCESSING)
                .fileName(file.getOriginalFilename())
                .uploadedAt(LocalDateTime.now())
                .build();

        allocationBatchRepository.save(batch);

        Path tempFilePath;
        try {
            tempFilePath = Files.createTempFile("alloc_upload_", ".csv");
            file.transferTo(tempFilePath);
        } catch (IOException e) {
            log.error("Failed to save uploaded file to temporary location", e);
            throw new BusinessException("Failed to process file upload: " + e.getMessage());
        }

        // Register async processing to run after transaction commits
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                batchProcessingService.processAllocationBatchAsync(batchId, tempFilePath.toString());
            }
        });

        return AllocationBatchUploadResponseDTO.builder()
                .batchId(batchId)
                .totalCases(0)
                .status(BatchStatus.PROCESSING.name())
                .build();
    }

    @Override
    @Cacheable(value = "allocationBatchStatus", key = "#batchId")
    public AllocationBatchStatusDTO getAllocationBatchStatus(String batchId) {
        log.info("Fetching allocation batch status for: {}", batchId);

        AllocationBatch batch = allocationBatchRepository.findByBatchId(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Allocation batch not found: " + batchId));

        return AllocationBatchStatusDTO.builder()
                .batchId(batch.getBatchId())
                .totalCases(batch.getTotalCases())
                .successful(batch.getSuccessfulAllocations())
                .failed(batch.getFailedAllocations())
                .status(batch.getStatus().name())
                .build();
    }

    @Override
    public byte[] exportFailedAllocationRows(String batchId) {
        log.info("Exporting failed allocation rows for batch: {}", batchId);

        if (!allocationBatchRepository.existsByBatchId(batchId)) {
            throw new ResourceNotFoundException("Allocation batch not found: " + batchId);
        }

        List<BatchError> errors = batchErrorRepository.findByBatchId(batchId);

        if (errors.isEmpty()) {
            throw new BusinessException("No errors found for batch: " + batchId);
        }

        return csvExporter.exportBatchErrors(errors);
    }

    @Override
    @Cacheable(value = "allocationSummary")
    public AllocationSummaryDTO getAllocationSummary() {
        log.info("Fetching overall allocation summary");

        Long total = allocationBatchRepository.countTotalAllocations();
        Long successful = allocationBatchRepository.countSuccessfulAllocations();
        Long failed = allocationBatchRepository.countFailedAllocations();
        Long pending = allocationBatchRepository.countByStatus(BatchStatus.PROCESSING);

        return AllocationSummaryDTO.builder()
                .totalAllocations(total != null ? total : 0L)
                .successfulAllocations(successful != null ? successful : 0L)
                .failedAllocations(failed != null ? failed : 0L)
                .pendingAllocations(pending != null ? pending : 0L)
                .build();
    }

    @Override
    public AllocationSummaryByDateDTO getAllocationSummaryByDate(LocalDate date) {
        log.info("Fetching allocation summary for date: {}", date);

        LocalDateTime dateTime = date.atStartOfDay();
        Long total = allocationBatchRepository.countTotalAllocationsByDate(dateTime);
        Long successful = allocationBatchRepository.countSuccessfulAllocationsByDate(dateTime);
        Long failed = allocationBatchRepository.countFailedAllocationsByDate(dateTime);

        return AllocationSummaryByDateDTO.builder()
                .date(date.toString())
                .totalAllocations(total != null ? total : 0L)
                .successfulAllocations(successful != null ? successful : 0L)
                .failedAllocations(failed != null ? failed : 0L)
                .build();
    }

    @Override
    @Cacheable(value = "allocationRules")
    public List<AllocationRuleDTO> getAllAllocationRules() {
        log.info("Fetching all allocation rules");

        return allocationRuleRepository.findAll().stream()
                .map(this::mapToRuleDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "allocationRules", key = "#ruleId")
    public AllocationRuleDTO getAllocationRule(Long ruleId) {
        log.info("Fetching allocation rule: {}", ruleId);
        AllocationRule rule = allocationRuleRepository.findById(ruleId)
                .orElseThrow(() -> new BusinessException("Allocation rule not found: " + ruleId));
        return mapToRuleDTO(rule);
    }

    @Override
    @Transactional
    @CacheEvict(value = "allocationRules", allEntries = true)
    public AllocationRuleDTO createAllocationRule(AllocationRuleDTO ruleDTO) {
        log.info("Creating new allocation rule: {}", ruleDTO.getName());

        // Build criteria map from explicit fields
        Map<String, Object> criteriaMap = buildCriteriaMap(ruleDTO);

        AllocationRule rule = AllocationRule.builder()
                .name(ruleDTO.getName())
                .description(ruleDTO.getDescription())
                .criteria(criteriaMap)
                .status(ruleDTO.getStatus() != null ? RuleStatus.valueOf(ruleDTO.getStatus()) : RuleStatus.ACTIVE)
                .priority(ruleDTO.getPriority() != null ? ruleDTO.getPriority() : 0)
                .build();

        AllocationRule savedRule = allocationRuleRepository.save(rule);

        saveAuditLog("ALLOCATION_RULE", savedRule.getId(), "CREATE", null, ruleDTO);

        return mapToRuleDTO(savedRule);
    }

    @Override
    @Transactional
    @CacheEvict(value = "allocationRules", allEntries = true)
    public AllocationRuleDTO updateAllocationRule(Long ruleId, AllocationRuleDTO ruleDTO) {
        log.info("Updating allocation rule: {}", ruleId);

        AllocationRule rule = allocationRuleRepository.findById(ruleId)
                .orElseThrow(() -> new BusinessException("Allocation rule not found: " + ruleId));

        saveAuditLog("ALLOCATION_RULE", ruleId, "UPDATE", mapToRuleDTO(rule), ruleDTO);

        if (ruleDTO.getName() != null) {
            rule.setName(ruleDTO.getName());
        }
        if (ruleDTO.getDescription() != null) {
            rule.setDescription(ruleDTO.getDescription());
        }
        if (ruleDTO.getStatus() != null) {
            rule.setStatus(RuleStatus.valueOf(ruleDTO.getStatus()));
        }
        if (ruleDTO.getPriority() != null) {
            rule.setPriority(ruleDTO.getPriority());
        }

        // Update criteria from explicit fields
        Map<String, Object> criteriaMap = buildCriteriaMap(ruleDTO);
        if (!criteriaMap.isEmpty()) {
            rule.setCriteria(criteriaMap);
        }

        AllocationRule updatedRule = allocationRuleRepository.save(rule);
        return mapToRuleDTO(updatedRule);
    }

    @Override
    @Transactional
    @CacheEvict(value = "allocationRules", allEntries = true)
    public void deleteAllocationRule(Long ruleId) {
        log.info("Deleting allocation rule: {}", ruleId);

        AllocationRule rule = allocationRuleRepository.findById(ruleId)
                .orElseThrow(() -> new BusinessException("Allocation rule not found: " + ruleId));

        saveAuditLog("ALLOCATION_RULE", ruleId, "DELETE", mapToRuleDTO(rule), null);

        allocationRuleRepository.deleteById(ruleId);
    }

    @Override
    public AllocationRuleSimulationDTO simulateAllocationRule(Long ruleId) {
        log.info("Simulating allocation rule: {}", ruleId);

        AllocationRule rule = allocationRuleRepository.findById(ruleId)
                .orElseThrow(() -> new BusinessException("Allocation rule not found: " + ruleId));

        // Get list of unallocated cases
        List<Long> unallocatedCaseIds = getUnallocatedCaseIds();
        long estimatedCases = unallocatedCaseIds.size();

        List<AllocationRuleSimulationDTO.SimulatedAllocationDTO> simulations = new ArrayList<>();

        Map<String, Object> criteria = rule.getCriteria();
        String ruleType = (String) criteria.getOrDefault("ruleType", criteria.get("type"));

        if ("PERCENTAGE_SPLIT".equals(ruleType)) {
            simulations = simulatePercentageSplit(criteria, estimatedCases);
        } else if ("CAPACITY_BASED".equals(ruleType)) {
            simulations = simulateCapacityBased(criteria, estimatedCases);
        } else if ("GEOGRAPHY".equals(ruleType)) {
            simulations = simulateGeographyBased(criteria, estimatedCases);
        } else {
            log.warn("Unknown allocation rule type: {}. No simulation performed.", ruleType);
        }

        return AllocationRuleSimulationDTO.builder()
                .ruleId(ruleId)
                .estimatedCases(estimatedCases)
                .simulatedAllocations(simulations)
                .build();
    }

    private List<AllocationRuleSimulationDTO.SimulatedAllocationDTO> simulatePercentageSplit(
            Map<String, Object> criteria, long totalCases) {
        List<AllocationRuleSimulationDTO.SimulatedAllocationDTO> simulations = new ArrayList<>();
        List<Map<String, Object>> splits = (List<Map<String, Object>>) criteria.get("splits");

        if (splits != null) {
            for (Map<String, Object> split : splits) {
                Long userId = ((Number) split.get("userId")).longValue();
                double percentage = ((Number) split.get("percentage")).doubleValue();
                long casesCount = (long) (totalCases * (percentage / 100.0));
                simulations.add(AllocationRuleSimulationDTO.SimulatedAllocationDTO.builder()
                        .userId(userId)
                        .casesCount(casesCount)
                        .build());
            }
        }
        return simulations;
    }

    private List<AllocationRuleSimulationDTO.SimulatedAllocationDTO> simulateCapacityBased(
            Map<String, Object> criteria, long totalCases) {
        List<AllocationRuleSimulationDTO.SimulatedAllocationDTO> simulations = new ArrayList<>();

        // Get max capacity per agent from criteria
        Integer maxCapacity = criteria.get("maxCapacity") != null ?
                ((Number) criteria.get("maxCapacity")).intValue() : 100;

        // Get agent IDs or geography filter if specified
        List<String> geographies = (List<String>) criteria.get("geography");
        List<Long> agentIds = (List<Long>) criteria.get("agentIds");

        List<UserDTO> availableAgents = new ArrayList<>();

        // Get agents from either agentIds or geography
        if (agentIds != null && !agentIds.isEmpty()) {
            // Use specified agent IDs
            for (Long agentId : agentIds) {
                try {
                    UserDTO user = userManagementClient.getUserById(agentId);
                    if (user != null) {
                        availableAgents.add(user);
                    }
                } catch (Exception e) {
                    log.warn("Failed to fetch user with ID {}: {}", agentId, e.getMessage());
                }
            }
        } else if (geographies != null && !geographies.isEmpty()) {
            // Query agents by geography
            try {
                availableAgents = userManagementClient.getUsersByGeography(geographies);
            } catch (Exception e) {
                log.error("Failed to fetch users by geography {}: {}", geographies, e.getMessage());
            }
        } else {
            // No specific agents or geography - get all active agents
            try {
                availableAgents = userManagementClient.getActiveAgents();
            } catch (Exception e) {
                log.error("Failed to fetch active agents: {}", e.getMessage());
            }
        }

        if (availableAgents.isEmpty()) {
            log.warn("No agents available for capacity-based allocation simulation");
            return simulations;
        }

        // Distribute cases based on available capacity
        for (UserDTO agent : availableAgents) {
            if (totalCases <= 0) {
                break;
            }

            long currentWorkload = caseAllocationRepository.countByPrimaryAgentIdAndStatus(
                    agent.getId(), AllocationStatus.ALLOCATED);
            long availableCapacity = Math.max(0, maxCapacity - currentWorkload);

            if (availableCapacity > 0) {
                long allocatedCases = Math.min(availableCapacity, totalCases);
                if (allocatedCases > 0) {
                    simulations.add(AllocationRuleSimulationDTO.SimulatedAllocationDTO.builder()
                            .userId(agent.getId())
                            .casesCount(allocatedCases)
                            .build());
                    totalCases -= allocatedCases;
                }
            }
        }

        return simulations;
    }

    private List<AllocationRuleSimulationDTO.SimulatedAllocationDTO> simulateGeographyBased(
            Map<String, Object> criteria, long totalCases) {
        List<AllocationRuleSimulationDTO.SimulatedAllocationDTO> simulations = new ArrayList<>();

        // Get geography list from criteria
        List<String> geographies = (List<String>) criteria.get("geographies");
        List<Long> agentIds = (List<Long>) criteria.get("agentIds");

        List<UserDTO> availableAgents = new ArrayList<>();

        if (agentIds != null && !agentIds.isEmpty()) {
            // Use specified agent IDs
            for (Long agentId : agentIds) {
                try {
                    UserDTO user = userManagementClient.getUserById(agentId);
                    if (user != null) {
                        availableAgents.add(user);
                    }
                } catch (Exception e) {
                    log.warn("Failed to fetch user with ID {}: {}", agentId, e.getMessage());
                }
            }
        } else if (geographies != null && !geographies.isEmpty()) {
            // Query agents by geography
            log.info("Fetching agents for geography-based allocation: {}", geographies);
            try {
                availableAgents = userManagementClient.getUsersByGeography(geographies);
            } catch (Exception e) {
                log.error("Failed to fetch users by geography {}: {}", geographies, e.getMessage());
            }
        } else {
            log.warn("Geography-based allocation requires either 'geographies' or 'agentIds' in criteria");
            return simulations;
        }

        if (availableAgents.isEmpty()) {
            log.warn("No agents available for geography-based allocation");
            return simulations;
        }

        // Distribute cases evenly among available agents
        long casesPerAgent = totalCases / availableAgents.size();
        long remainder = totalCases % availableAgents.size();

        for (int i = 0; i < availableAgents.size(); i++) {
            long casesForAgent = casesPerAgent + (i < remainder ? 1 : 0);

            if (casesForAgent > 0) {
                simulations.add(AllocationRuleSimulationDTO.SimulatedAllocationDTO.builder()
                        .userId(availableAgents.get(i).getId())
                        .casesCount(casesForAgent)
                        .build());
            }
        }

        return simulations;
    }

    private List<Long> getUnallocatedCaseIds() {
        List<Long> unallocatedCaseIds = new ArrayList<>();
        int page = 0;
        int size = 100;
        Page<UnallocatedCaseDTO> unallocatedCasesPage;
        do {
            CommonResponse<CustomPageImpl<UnallocatedCaseDTO>> response = caseSourcingClient.getUnallocatedCases(page, size);
            unallocatedCasesPage = response.getPayload();
            unallocatedCaseIds.addAll(unallocatedCasesPage.getContent().stream()
                    .map(UnallocatedCaseDTO::getId)
                    .collect(Collectors.toList()));
            page++;
        } while (unallocatedCasesPage.hasNext());
        return unallocatedCaseIds;
    }


    @Override
    @Cacheable(value = "caseAllocation", key = "#caseId")
    public CaseAllocationDTO getCaseAllocation(Long caseId) {
        log.info("Fetching case allocation for case: {}", caseId);

        CaseAllocation allocation = caseAllocationRepository.findFirstByCaseIdOrderByAllocatedAtDesc(caseId)
                .orElseThrow(() -> new BusinessException("Case allocation not found for case: " + caseId));

        UserDTO primaryAgent = null;
        if (allocation.getPrimaryAgentId() != null) {
            primaryAgent = userManagementClient.getUserById(allocation.getPrimaryAgentId());
        }

        UserDTO secondaryAgent = null;
        if (allocation.getSecondaryAgentId() != null) {
            secondaryAgent = userManagementClient.getUserById(allocation.getSecondaryAgentId());
        }

        return CaseAllocationDTO.builder()
                .caseId(allocation.getCaseId())
                .primaryAgent(primaryAgent != null ?
                    CaseAllocationDTO.AgentDTO.builder()
                        .userId(primaryAgent.getId())
                        .username(primaryAgent.getUsername())
                        .build() : null)
                .secondaryAgent(secondaryAgent != null ?
                    CaseAllocationDTO.AgentDTO.builder()
                        .userId(secondaryAgent.getId())
                        .username(secondaryAgent.getUsername())
                        .build() : null)
                .allocatedAt(allocation.getAllocatedAt())
                .build();
    }

    @Override
    @Cacheable(value = "allocationHistory", key = "#caseId")
    public AllocationHistoryDTO getCaseAllocationHistory(Long caseId) {
        log.info("Fetching allocation history for case: {}", caseId);

        List<AllocationHistory> history = allocationHistoryRepository.findByCaseIdOrderByAllocatedAtDesc(caseId);

        List<AllocationHistoryDTO.HistoryItemDTO> historyItems = history.stream()
                .map(h -> {
                    UserDTO user = userManagementClient.getUserById(h.getAllocatedToUserId());
                    return AllocationHistoryDTO.HistoryItemDTO.builder()
                        .allocatedToUserId(h.getAllocatedToUserId())
                        .allocatedToUsername(user != null ? user.getUsername() : "N/A")
                        .allocatedAt(h.getAllocatedAt())
                        .action(h.getAction().name())
                        .reason(h.getReason())
                        .build();
                })
                .collect(Collectors.toList());

        return AllocationHistoryDTO.builder()
                .caseId(caseId)
                .history(historyItems)
                .build();
    }

    @Override
    @Transactional
    public AllocationBatchUploadResponseDTO uploadContactUpdateBatch(MultipartFile file) {
        log.info("Processing contact update batch upload: {}", file.getOriginalFilename());

        String batchId = "CONTACT_BATCH_" + System.currentTimeMillis();

        ContactUpdateBatch batch = ContactUpdateBatch.builder()
                .batchId(batchId)
                .totalRecords(0)
                .successfulUpdates(0)
                .failedUpdates(0)
                .status(BatchStatus.PROCESSING)
                .fileName(file.getOriginalFilename())
                .uploadedAt(LocalDateTime.now())
                .build();

        contactUpdateBatchRepository.save(batch);

        saveAuditLog("CONTACT_UPDATE_BATCH", null, "CREATE", null, batch);


        Path tempFilePath;
        try {
            tempFilePath = Files.createTempFile("contact_upload_", ".csv");
            file.transferTo(tempFilePath);
        } catch (IOException e) {
            log.error("Failed to save uploaded file to temporary location", e);
            throw new BusinessException("Failed to process file upload: " + e.getMessage());
        }

        // Register async processing to run after transaction commits
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                batchProcessingService.processContactUpdateBatchAsync(batchId, tempFilePath.toString());
            }
        });

        return AllocationBatchUploadResponseDTO.builder()
                .batchId(batchId)
                .totalCases(0) // In the response DTO, it's still totalCases, which might be a minor bug in the DTO design
                .status(BatchStatus.PROCESSING.name())
                .build();
    }

    @Override
    public ContactUpdateBatchStatusDTO getContactUpdateBatchStatus(String batchId) {
        log.info("Fetching contact update batch status for: {}", batchId);

        ContactUpdateBatch batch = contactUpdateBatchRepository.findByBatchId(batchId)
                .orElseThrow(() -> new BusinessException("Contact update batch not found: " + batchId));

        return ContactUpdateBatchStatusDTO.builder()
                .batchId(batch.getBatchId())
                .totalRecords(batch.getTotalRecords())
                .successfulUpdates(batch.getSuccessfulUpdates())
                .failedUpdates(batch.getFailedUpdates())
                .status(batch.getStatus().name())
                .build();
    }

    @Override
    public byte[] exportFailedContactUpdateRows(String batchId) {
        log.info("Exporting failed contact update rows for batch: {}", batchId);

        if (!contactUpdateBatchRepository.existsByBatchId(batchId)) {
            throw new ResourceNotFoundException("Contact update batch not found: " + batchId);
        }

        List<BatchError> errors = batchErrorRepository.findByBatchId(batchId);

        if (errors.isEmpty()) {
            throw new BusinessException("No errors found for batch: " + batchId);
        }

        return csvExporter.exportBatchErrors(errors);
    }

    @Override
    @Cacheable(value = "allocationErrors")
    public List<ErrorDTO> getAllErrors() {
        log.info("Fetching all allocation errors");

        return batchErrorRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToErrorDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ErrorDTO getErrorDetails(String errorId) {
        log.info("Fetching error details for: {}", errorId);

        BatchError error = batchErrorRepository.findByErrorId(errorId)
                .orElseThrow(() -> new BusinessException("Error not found: " + errorId));

        return mapToErrorDTOWithDetails(error);
    }

    @Override
    @Cacheable(value = "allocationAudit")
    public List<AuditLogDTO> getAllocationAudit() {
        log.info("Fetching allocation audit trail");
        return auditLogRepository.findAll().stream()
                .map(this::mapToAuditLogDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuditLogDTO> getAllocationAuditForCase(Long caseId) {
        log.info("Fetching allocation audit trail for case: {}", caseId);
        return auditLogRepository.findByEntityIdAndEntityType(caseId, "CASE_ALLOCATION").stream()
                .map(this::mapToAuditLogDTO)
                .collect(Collectors.toList());
    }

    private void saveAuditLog(String entityType, Long entityId, String action, Object before, Object after) {
        try {
            Map<String, Object> changesMap = new java.util.HashMap<>();
            changesMap.put("before", before);
            changesMap.put("after", after);
            String changes = objectMapper.writeValueAsString(changesMap);
            AuditLog auditLog = AuditLog.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .changedFields(changes)
                    .build();
            auditLogRepository.save(auditLog);
        } catch (JsonProcessingException e) {
            log.error("Error creating audit log", e);
        }
    }

    private AuditLogDTO mapToAuditLogDTO(AuditLog auditLog) {
        UserDTO user = auditLog.getUserId() != null ? userManagementClient.getUserById(auditLog.getUserId()) : null;
        Map<String, AuditLogDTO.ChangeDTO> changesMap = null;
        if (auditLog.getChangedFields() != null) {
            try {
                changesMap = objectMapper.readValue(auditLog.getChangedFields(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, AuditLogDTO.ChangeDTO>>() {});
            } catch (JsonProcessingException e) {
                log.error("Error deserializing audit log changes for auditId: {}", auditLog.getId(), e);
                // Optionally, handle this error more gracefully, e.g., return an empty map or a map with an error message
            }
        }
        return AuditLogDTO.builder()
                .auditId(auditLog.getId().toString())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .action(auditLog.getAction())
                .userId(auditLog.getUserId())
                .username(user != null ? user.getUsername() : "N/A")
                .timestamp(auditLog.getTimestamp())
                .changes(changesMap)
                .build();
    }


    private AllocationRuleDTO mapToRuleDTO(AllocationRule rule) {
        Map<String, Object> criteria = rule.getCriteria();

        AllocationRuleDTO.AllocationRuleDTOBuilder builder = AllocationRuleDTO.builder()
                .id(rule.getId())
                .name(rule.getName())
                .description(rule.getDescription())
                .criteria(criteria)
                .status(rule.getStatus().name())
                .priority(rule.getPriority())
                .createdBy(rule.getCreatedBy())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt());

        // Extract explicit fields from criteria
        if (criteria != null) {
            if (criteria.containsKey("ruleType")) {
                builder.ruleType((String) criteria.get("ruleType"));
            }
            if (criteria.containsKey("geographies")) {
                builder.geographies((List<String>) criteria.get("geographies"));
            }
            if (criteria.containsKey("buckets")) {
                builder.buckets((List<String>) criteria.get("buckets"));
            }
            if (criteria.containsKey("maxCasesPerAgent")) {
                builder.maxCasesPerAgent((Integer) criteria.get("maxCasesPerAgent"));
            }
            if (criteria.containsKey("agentIds")) {
                List<?> rawAgentIds = (List<?>) criteria.get("agentIds");
                List<Long> agentIds = rawAgentIds.stream()
                        .map(id -> id instanceof Integer ? ((Integer) id).longValue() : (Long) id)
                        .collect(Collectors.toList());
                builder.agentIds(agentIds);
            }
            if (criteria.containsKey("percentages")) {
                builder.percentages((List<Integer>) criteria.get("percentages"));
            }
        }

        return builder.build();
    }

    private Map<String, Object> buildCriteriaMap(AllocationRuleDTO ruleDTO) {
        Map<String, Object> criteriaMap = new java.util.HashMap<>();

        if (ruleDTO.getRuleType() != null) {
            criteriaMap.put("ruleType", ruleDTO.getRuleType());
        }
        if (ruleDTO.getGeographies() != null) {
            criteriaMap.put("geographies", ruleDTO.getGeographies());
        }
        if (ruleDTO.getBuckets() != null) {
            criteriaMap.put("buckets", ruleDTO.getBuckets());
        }
        if (ruleDTO.getMaxCasesPerAgent() != null) {
            criteriaMap.put("maxCasesPerAgent", ruleDTO.getMaxCasesPerAgent());
        }
        if (ruleDTO.getAgentIds() != null) {
            criteriaMap.put("agentIds", ruleDTO.getAgentIds());
        }
        if (ruleDTO.getPercentages() != null) {
            criteriaMap.put("percentages", ruleDTO.getPercentages());
        }

        // If criteria map is provided, merge it (explicit fields take precedence)
        if (ruleDTO.getCriteria() != null) {
            Map<String, Object> legacyCriteria = new java.util.HashMap<>(ruleDTO.getCriteria());
            legacyCriteria.putAll(criteriaMap);
            return legacyCriteria;
        }

        return criteriaMap;
    }

    private ErrorDTO mapToErrorDTO(BatchError error) {
        return ErrorDTO.builder()
                .errorId(error.getErrorId())
                .module(error.getModule() != null ? error.getModule() : "ALLOCATION")
                .type(error.getErrorType().name())
                .message(error.getErrorMessage())
                .timestamp(error.getCreatedAt())
                .build();
    }

    private ErrorDTO mapToErrorDTOWithDetails(BatchError error) {
        List<ErrorDTO.AffectedRecordDTO> affectedRecords = new ArrayList<>();
        if (error.getCaseId() != null || error.getExternalCaseId() != null) {
            affectedRecords.add(ErrorDTO.AffectedRecordDTO.builder()
                    .caseId(error.getCaseId())
                    .externalId(error.getExternalCaseId())
                    .build());
        }

        return ErrorDTO.builder()
                .errorId(error.getErrorId())
                .module(error.getModule() != null ? error.getModule() : "ALLOCATION")
                .type(error.getErrorType().name())
                .message(error.getErrorMessage())
                .details(error.getErrorMessage())
                .timestamp(error.getCreatedAt())
                .affectedRecords(affectedRecords)
                .build();
    }

    // NEW IMPLEMENTATIONS

    @Override
    public List<AllocationBatchDTO> getAllBatches(String status, LocalDate startDate, LocalDate endDate, int page, int size) {
        log.info("Fetching all batches with filters - status: {}, startDate: {}, endDate: {}", status, startDate, endDate);

        List<AllocationBatch> batches;
        if (status != null || startDate != null || endDate != null) {
            batches = allocationBatchRepository.findAll().stream()
                    .filter(batch -> status == null || batch.getStatus().name().equals(status))
                    .filter(batch -> startDate == null || !batch.getUploadedAt().toLocalDate().isBefore(startDate))
                    .filter(batch -> endDate == null || !batch.getUploadedAt().toLocalDate().isAfter(endDate))
                    .skip((long) page * size)
                    .limit(size)
                    .collect(Collectors.toList());
        } else {
            batches = allocationBatchRepository.findAll().stream()
                    .skip((long) page * size)
                    .limit(size)
                    .collect(Collectors.toList());
        }

        return batches.stream()
                .map(this::mapToAllocationBatchDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AllocationRuleExecutionResponseDTO applyAllocationRule(Long ruleId, AllocationRuleExecutionRequestDTO request) {
        log.info("Applying allocation rule: {} with maxCases: {}, dryRun: {}", ruleId, request.getMaxCases(), request.getDryRun());

        AllocationRule rule = allocationRuleRepository.findById(ruleId)
                .orElseThrow(() -> new BusinessException("Allocation rule not found: " + ruleId));

        if (rule.getStatus() != RuleStatus.ACTIVE) {
            throw new BusinessException("Cannot apply inactive rule: " + ruleId);
        }

        // Simulate first to get distribution
        AllocationRuleSimulationDTO simulation = simulateAllocationRule(ruleId);

        List<AllocationRuleExecutionResponseDTO.AllocationResultDTO> results = new ArrayList<>();
        int totalAllocated = 0;

        if (!request.getDryRun()) {
            // Get unallocated cases
            List<Long> unallocatedCaseIds = getUnallocatedCaseIds();
            int maxCases = request.getMaxCases() != null ? request.getMaxCases() : unallocatedCaseIds.size();

            List<CaseAllocation> allocations = new ArrayList<>();
            List<AllocationHistory> historyEntries = new ArrayList<>();

            int caseIndex = 0;
            for (AllocationRuleSimulationDTO.SimulatedAllocationDTO simAlloc : simulation.getSimulatedAllocations()) {
                int casesToAllocate = simAlloc.getCasesCount().intValue();
                casesToAllocate = Math.min(casesToAllocate, maxCases - totalAllocated);

                for (int i = 0; i < casesToAllocate && caseIndex < unallocatedCaseIds.size(); i++, caseIndex++) {
                    Long caseId = unallocatedCaseIds.get(caseIndex);

                    allocations.add(CaseAllocation.builder()
                            .caseId(caseId)
                            .primaryAgentId(simAlloc.getUserId())
                            .allocatedToType("USER")
                            .allocationType("PRIMARY")
                            .status(AllocationStatus.ALLOCATED)
                            .allocationRuleId(ruleId)
                            .allocatedAt(LocalDateTime.now())
                            .build());

                    historyEntries.add(AllocationHistory.builder()
                            .caseId(caseId)
                            .allocatedToUserId(simAlloc.getUserId())
                            .newOwnerType("USER")
                            .action(com.finx.allocationreallocationservice.domain.enums.AllocationAction.ALLOCATED)
                            .reason("Rule-based allocation: " + rule.getName())
                            .allocatedAt(LocalDateTime.now())
                            .build());
                }

                UserDTO user = userManagementClient.getUserById(simAlloc.getUserId());
                results.add(AllocationRuleExecutionResponseDTO.AllocationResultDTO.builder()
                        .userId(simAlloc.getUserId())
                        .username(user != null ? user.getUsername() : "Unknown")
                        .casesAllocated(casesToAllocate)
                        .build());

                totalAllocated += casesToAllocate;
                if (totalAllocated >= maxCases) break;
            }

            caseAllocationRepository.saveAll(allocations);
            allocationHistoryRepository.saveAll(historyEntries);
        } else {
            // Dry run - just return simulation results
            for (AllocationRuleSimulationDTO.SimulatedAllocationDTO simAlloc : simulation.getSimulatedAllocations()) {
                UserDTO user = userManagementClient.getUserById(simAlloc.getUserId());
                results.add(AllocationRuleExecutionResponseDTO.AllocationResultDTO.builder()
                        .userId(simAlloc.getUserId())
                        .username(user != null ? user.getUsername() : "Unknown")
                        .casesAllocated(simAlloc.getCasesCount().intValue())
                        .build());
                totalAllocated += simAlloc.getCasesCount().intValue();
            }
        }

        return AllocationRuleExecutionResponseDTO.builder()
                .executionId(UUID.randomUUID().toString())
                .ruleId(ruleId)
                .ruleName(rule.getName())
                .totalCasesAllocated(totalAllocated)
                .dryRun(request.getDryRun())
                .status(request.getDryRun() ? "SIMULATED" : "COMPLETED")
                .allocationResults(results)
                .build();
    }

    @Transactional
    public void deallocateCase(Long caseId, String reason) {
        log.info("Deallocating case: {} with reason: {}", caseId, reason);

        CaseAllocation allocation = caseAllocationRepository.findFirstByCaseIdOrderByAllocatedAtDesc(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case allocation not found for case: " + caseId));

        Long previousAgentId = allocation.getPrimaryAgentId();

        allocation.setStatus(AllocationStatus.DEALLOCATED);
        allocation.setDeallocatedAt(LocalDateTime.now());
        caseAllocationRepository.save(allocation);

        AllocationHistory history = AllocationHistory.builder()
                .caseId(caseId)
                .allocatedFromUserId(previousAgentId)
                .previousOwnerType("USER")
                .action(com.finx.allocationreallocationservice.domain.enums.AllocationAction.DEALLOCATED)
                .reason(reason)
                .allocatedAt(LocalDateTime.now())
                .build();
        allocationHistoryRepository.save(history);

        saveAuditLog("CASE_ALLOCATION", allocation.getId(), "DEALLOCATE", allocation, null);
    }

    @Override
    @Transactional
    public BulkDeallocationResponseDTO bulkDeallocate(BulkDeallocationRequestDTO request) {
        log.info("Bulk deallocating {} cases", request.getCaseIds().size());

        String jobId = "DEALLOC_JOB_" + System.currentTimeMillis();
        int successful = 0;
        int failed = 0;

        for (Long caseId : request.getCaseIds()) {
            try {
                deallocateCase(caseId, request.getReason());
                successful++;
            } catch (Exception e) {
                log.error("Failed to deallocate case {}: {}", caseId, e.getMessage());
                failed++;
            }
        }

        return BulkDeallocationResponseDTO.builder()
                .jobId(jobId)
                .totalCases(request.getCaseIds().size())
                .successfulDeallocations(successful)
                .failedDeallocations(failed)
                .status("COMPLETED")
                .build();
    }

    @Override
    public List<AgentWorkloadDTO> getAgentWorkload(List<Long> agentIds, List<String> geographies) {
        log.info("Fetching agent workload for agentIds: {}, geographies: {}", agentIds, geographies);

        List<UserDTO> agents = new ArrayList<>();

        if (agentIds != null && !agentIds.isEmpty()) {
            for (Long agentId : agentIds) {
                try {
                    UserDTO user = userManagementClient.getUserById(agentId);
                    if (user != null) agents.add(user);
                } catch (Exception e) {
                    log.warn("Failed to fetch user {}: {}", agentId, e.getMessage());
                }
            }
        } else if (geographies != null && !geographies.isEmpty()) {
            try {
                agents = userManagementClient.getUsersByGeography(geographies);
            } catch (Exception e) {
                log.error("Failed to fetch users by geography: {}", e.getMessage());
            }
        } else {
            try {
                agents = userManagementClient.getActiveAgents();
            } catch (Exception e) {
                log.error("Failed to fetch active agents: {}", e.getMessage());
            }
        }

        return agents.stream()
                .map(agent -> {
                    long allocated = caseAllocationRepository.countByPrimaryAgentIdAndStatus(
                            agent.getId(), AllocationStatus.ALLOCATED);
                    int capacity = agent.getCapacity() != null ? agent.getCapacity() : 100;
                    int available = Math.max(0, capacity - (int) allocated);
                    double utilization = capacity > 0 ? (allocated * 100.0 / capacity) : 0.0;

                    return AgentWorkloadDTO.builder()
                            .agentId(agent.getId())
                            .agentName(agent.getUsername())
                            .geography(agent.getGeography())
                            .totalAllocated((int) allocated)
                            .activeAllocations((int) allocated)
                            .capacity(capacity)
                            .availableCapacity(available)
                            .utilizationPercentage(Math.round(utilization * 100.0) / 100.0)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private AllocationBatchDTO mapToAllocationBatchDTO(AllocationBatch batch) {
        return AllocationBatchDTO.builder()
                .batchId(batch.getBatchId())
                .fileName(batch.getFileName())
                .totalCases(batch.getTotalCases())
                .successfulAllocations(batch.getSuccessfulAllocations())
                .failedAllocations(batch.getFailedAllocations())
                .status(batch.getStatus().name())
                .uploadedAt(batch.getUploadedAt())
                .completedAt(batch.getCompletedAt())
                .build();
    }
}
