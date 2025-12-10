package com.finx.allocationreallocationservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finx.allocationreallocationservice.client.dto.UserDTO;
import com.finx.allocationreallocationservice.domain.dto.*;
import com.finx.allocationreallocationservice.domain.entity.*;
import com.finx.allocationreallocationservice.domain.enums.AllocationAction;
import com.finx.allocationreallocationservice.domain.enums.AllocationStatus;
import com.finx.allocationreallocationservice.domain.enums.BatchStatus;
import com.finx.allocationreallocationservice.domain.enums.RuleStatus;
import com.finx.allocationreallocationservice.exception.BusinessException;
import com.finx.allocationreallocationservice.exception.ValidationException;
import com.finx.allocationreallocationservice.repository.*;
import com.finx.allocationreallocationservice.service.AllocationService;
import com.finx.allocationreallocationservice.util.csv.CsvExporter;
import com.finx.allocationreallocationservice.client.CaseSourcingServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final AuditLogRepository auditLogRepository;
    private final CaseReadRepository caseReadRepository;
    private final UserRepository userRepository;
    private final CaseSourcingServiceClient caseSourcingServiceClient;

    @SuppressWarnings("null")
    @Override
    @Transactional
    public AllocationBatchUploadResponseDTO uploadAllocationBatch(MultipartFile file) {
        log.info("Processing allocation batch upload: {}", file.getOriginalFilename());

        String batchId = "ALLOC_BATCH_" + System.currentTimeMillis();

        Path tempFilePath;
        try {
            tempFilePath = Files.createTempFile("alloc_upload_", ".csv");
            file.transferTo(tempFilePath);
        } catch (IOException e) {
            log.error("Failed to save uploaded file to temporary location", e);
            throw new BusinessException("Failed to process file upload: " + e.getMessage());
        }

        // Quick count of CSV rows (excluding header)
        int estimatedRowCount = 0;
        try {
            estimatedRowCount = countCsvRows(tempFilePath);
        } catch (Exception e) {
            log.warn("Failed to count CSV rows: {}", e.getMessage());
        }

        AllocationBatch batch = AllocationBatch.builder()
                .batchId(batchId)
                .totalCases(estimatedRowCount)
                .successfulAllocations(0)
                .failedAllocations(0)
                .status(BatchStatus.PROCESSING)
                .fileName(file.getOriginalFilename())
                .uploadedAt(LocalDateTime.now())
                .build();

        allocationBatchRepository.save(batch);

        // Register async processing to run after transaction commits
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                batchProcessingService.processAllocationBatchAsync(batchId, tempFilePath.toString());
            }
        });

        return AllocationBatchUploadResponseDTO.builder()
                .batchId(batchId)
                .totalCases(estimatedRowCount)
                .status(BatchStatus.PROCESSING.name())
                .build();
    }

    private int countCsvRows(Path filePath) throws IOException {
        try (java.io.BufferedReader reader = java.nio.file.Files.newBufferedReader(filePath)) {
            // Skip header line
            reader.readLine();

            // Count data rows (skip empty lines)
            int count = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    count++;
                }
            }
            return count;
        }
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
    public byte[] exportAllocationBatch(String batchId) {
        log.info("Exporting ALL allocations (success + failure) for batch: {}", batchId);

        if (!allocationBatchRepository.existsByBatchId(batchId)) {
            throw new ResourceNotFoundException("Allocation batch not found: " + batchId);
        }

        // Find all successful allocations for this batch
        List<CaseAllocation> allocations = caseAllocationRepository.findByBatchId(batchId);

        // Find all errors for this batch
        List<BatchError> errors = batchErrorRepository.findByBatchId(batchId);

        // Check if there's anything to export
        if (allocations.isEmpty() && errors.isEmpty()) {
            throw new BusinessException("No records found for batch: " + batchId);
        }

        log.info("Exporting batch {}: {} success allocations, {} failure records",
                 batchId, allocations.size(), errors.size());

        // Export all data - success allocations with STATUS="SUCCESS", errors with STATUS="FAILURE"
        return csvExporter.exportAllBatchData(allocations, errors);
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

    @SuppressWarnings("null")
    @Override
    @Cacheable(value = "allocationRules", key = "#ruleId")
    public AllocationRuleDTO getAllocationRule(Long ruleId) {
        log.info("Fetching allocation rule: {}", ruleId);
        AllocationRule rule = allocationRuleRepository.findById(ruleId)
                .orElseThrow(() -> new BusinessException("Allocation rule not found: " + ruleId));
        return mapToRuleDTO(rule);
    }

    @SuppressWarnings("null")
    @Override
    @Transactional
    @CacheEvict(value = "allocationRules", allEntries = true)
    public AllocationRuleDTO createAllocationRule(AllocationRuleDTO ruleDTO) {
        log.info("Creating new allocation rule: {}", ruleDTO.getName());

        // Validate mandatory fields
        if (ruleDTO.getName() == null || ruleDTO.getName().trim().isEmpty()) {
            throw new ValidationException("name", "Rule name is required");
        }
        if (ruleDTO.getRuleType() == null || ruleDTO.getRuleType().trim().isEmpty()) {
            throw new ValidationException("ruleType", "Rule type is required");
        }

        // Validate at least one geography filter is provided
        boolean hasStates = ruleDTO.getStates() != null && !ruleDTO.getStates().isEmpty();
        boolean hasCities = ruleDTO.getCities() != null && !ruleDTO.getCities().isEmpty();
        boolean hasLocations = ruleDTO.getLocations() != null && !ruleDTO.getLocations().isEmpty();
        boolean hasLegacyGeographies = ruleDTO.getGeographies() != null && !ruleDTO.getGeographies().isEmpty();

        if (!hasStates && !hasCities && !hasLocations && !hasLegacyGeographies) {
            throw new ValidationException("geography",
                    "At least one geography filter is required (states, cities, or locations)");
        }

        // Validate rule type
        String ruleType = ruleDTO.getRuleType();
        if (!"PERCENTAGE_SPLIT".equals(ruleType) && !"CAPACITY_BASED".equals(ruleType)
                && !"GEOGRAPHY".equals(ruleType)) {
            throw new ValidationException("ruleType",
                    "Invalid rule type. Must be one of: PERCENTAGE_SPLIT, CAPACITY_BASED, GEOGRAPHY");
        }

        // Build criteria map from explicit fields (excluding agentIds and percentages
        // for new rules)
        Map<String, Object> criteriaMap = buildCriteriaMapForCreate(ruleDTO);

        AllocationRule rule = AllocationRule.builder()
                .name(ruleDTO.getName())
                .description(ruleDTO.getDescription())
                .criteria(criteriaMap)
                .status(ruleDTO.getStatus() != null ? RuleStatus.valueOf(ruleDTO.getStatus()) : RuleStatus.DRAFT)
                .priority(ruleDTO.getPriority() != null ? ruleDTO.getPriority() : 0)
                .build();

        AllocationRule savedRule = allocationRuleRepository.save(rule);

        log.info("Created allocation rule {} with status {}", savedRule.getId(), savedRule.getStatus());
        saveAuditLog("ALLOCATION_RULE", savedRule.getId(), "CREATE", null, ruleDTO);

        return mapToRuleDTO(savedRule);
    }

    @SuppressWarnings("null")
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

    @SuppressWarnings("null")
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

    @SuppressWarnings("null")
    @Override
    @Transactional
    @CacheEvict(value = "allocationRules", allEntries = true)
    public AllocationRuleSimulationDTO simulateAllocationRule(Long ruleId) {
        log.info("Simulating allocation rule: {}", ruleId);

        AllocationRule rule = allocationRuleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Allocation rule not found: " + ruleId));

        // Enforce lifecycle: simulate() allowed if status == DRAFT or READY_FOR_APPLY
        if (rule.getStatus() != RuleStatus.DRAFT && rule.getStatus() != RuleStatus.READY_FOR_APPLY) {
            throw new ValidationException(
                    "Rule must be in DRAFT or READY_FOR_APPLY status to simulate. Current status: " + rule.getStatus());
        }

        Map<String, Object> criteria = rule.getCriteria();
        String ruleType = (String) criteria.getOrDefault("ruleType", criteria.get("type"));

        // Extract multi-field geography filters
        List<String> states = (List<String>) criteria.get("states");
        List<String> cities = (List<String>) criteria.get("cities");
        List<String> locations = (List<String>) criteria.get("locations");
        List<String> geographies = (List<String>) criteria.get("geographies"); // Legacy field
        List<String> buckets = (List<String>) criteria.get("buckets");

        // Determine which geography filter to use
        boolean hasNewGeoFilters = (states != null && !states.isEmpty())
                || (cities != null && !cities.isEmpty())
                || (locations != null && !locations.isEmpty());

        // Fetch unallocated cases matching the rule's geography and bucket filters
        List<com.finx.allocationreallocationservice.domain.entity.Case> matchingCases;
        if (hasNewGeoFilters) {
            // Use new multi-field geography filtering
            matchingCases = getUnallocatedCasesMatchingMultiFilters(states, cities, locations, buckets);
        } else {
            // Fall back to legacy geography filtering
            matchingCases = getUnallocatedCasesMatchingFilters(geographies, buckets);
        }
        int unallocatedCasesCount = matchingCases.size();

        // Extract case IDs for the response
        List<Long> caseIds = matchingCases.stream()
                .map(com.finx.allocationreallocationservice.domain.entity.Case::getId)
                .collect(Collectors.toList());

        log.info("Found {} unallocated cases matching rule filters (states: {}, cities: {}, locations: {}, buckets: {})",
                unallocatedCasesCount, states, cities, locations, buckets);

        // Automatically detect eligible agents based on rule's geographies
        // For agent detection, combine all geography types
        List<String> allGeographies = new ArrayList<>();
        if (states != null) allGeographies.addAll(states);
        if (cities != null) allGeographies.addAll(cities);
        if (locations != null) allGeographies.addAll(locations);
        if (geographies != null && allGeographies.isEmpty()) allGeographies.addAll(geographies);

        List<UserDTO> eligibleAgentsList = new ArrayList<>();
        if (!allGeographies.isEmpty()) {
            try {
                List<User> users = userRepository.findByGeographies(allGeographies.toArray(new String[0]));
                eligibleAgentsList = users.stream()
                        .map(this::mapToUserDTO)
                        .collect(Collectors.toList());
                log.info("Found {} eligible agents for geographies: {}", eligibleAgentsList.size(), allGeographies);
            } catch (Exception e) {
                log.error("Failed to fetch users by geography {}: {}", allGeographies, e.getMessage());
                throw new BusinessException("Failed to fetch eligible agents: " + e.getMessage());
            }
        } else {
            log.warn("No geographies specified in rule. Cannot determine eligible agents.");
            throw new BusinessException("Rule must have at least one geography filter (states, cities, or locations) to detect eligible agents");
        }

        if (eligibleAgentsList.isEmpty()) {
            throw new BusinessException("No eligible agents found for the specified geographies: " + allGeographies);
        }

        // Build eligible agents list with capacity info
        List<AllocationRuleSimulationDTO.EligibleAgentDTO> eligibleAgents = eligibleAgentsList.stream()
                .map(agent -> {
                    long currentWorkload = caseAllocationRepository.countByPrimaryAgentIdAndStatus(
                            agent.getId(), AllocationStatus.ALLOCATED);
                    int capacity = agent.getCapacity() != null ? agent.getCapacity() : 100;
                    int availableCapacity = Math.max(0, capacity - (int) currentWorkload);

                    return AllocationRuleSimulationDTO.EligibleAgentDTO.builder()
                            .agentId(agent.getId())
                            .agentName(agent.getUsername())
                            .capacity(capacity)
                            .currentWorkload((int) currentWorkload)
                            .availableCapacity(availableCapacity)
                            .build();
                })
                .collect(Collectors.toList());

        // Calculate suggested distribution based on rule type
        Map<String, Integer> suggestedDistribution = new java.util.HashMap<>();

        if ("PERCENTAGE_SPLIT".equals(ruleType)) {
            suggestedDistribution = calculateEqualPercentageDistribution(eligibleAgents, unallocatedCasesCount);
        } else if ("CAPACITY_BASED".equals(ruleType)) {
            suggestedDistribution = calculateCapacityBasedDistribution(eligibleAgents, unallocatedCasesCount);
        } else if ("GEOGRAPHY".equals(ruleType)) {
            suggestedDistribution = calculateGeographyBasedDistribution(eligibleAgents, unallocatedCasesCount);
        } else {
            log.warn("Unknown allocation rule type: {}. Using equal distribution.", ruleType);
            suggestedDistribution = calculateEqualPercentageDistribution(eligibleAgents, unallocatedCasesCount);
        }

        // Update rule status to READY_FOR_APPLY only if currently DRAFT
        if (rule.getStatus() == RuleStatus.DRAFT) {
            rule.setStatus(RuleStatus.READY_FOR_APPLY);
            allocationRuleRepository.save(rule);
            log.info("Updated rule {} status from DRAFT to READY_FOR_APPLY", ruleId);

            saveAuditLog("ALLOCATION_RULE", ruleId, "SIMULATE", null,
                    Map.of("status", "READY_FOR_APPLY", "unallocatedCases", unallocatedCasesCount));
        } else {
            log.info("Rule {} already in READY_FOR_APPLY status, re-simulation successful", ruleId);

            saveAuditLog("ALLOCATION_RULE", ruleId, "RE-SIMULATE", null,
                    Map.of("status", "READY_FOR_APPLY", "unallocatedCases", unallocatedCasesCount));
        }

        // Extract agent IDs for easy use in apply API
        List<Long> agentIds = eligibleAgents.stream()
                .map(AllocationRuleSimulationDTO.EligibleAgentDTO::getAgentId)
                .collect(Collectors.toList());

        return AllocationRuleSimulationDTO.builder()
                .ruleId(ruleId)
                .unallocatedCases(unallocatedCasesCount)
                .caseIds(caseIds)
                .agentIds(agentIds)
                .eligibleAgents(eligibleAgents)
                .suggestedDistribution(suggestedDistribution)
                .build();
    }

    /**
     * Legacy method for backward compatibility - uses single geographies list
     */
    private List<com.finx.allocationreallocationservice.domain.entity.Case> getUnallocatedCasesMatchingFilters(
            List<String> geographies, List<String> buckets) {
        // For backward compatibility, treat geographies as locations
        return getUnallocatedCasesMatchingMultiFilters(null, null, geographies, buckets);
    }

    /**
     * New multi-field geography filtering method
     * Filters by states, cities, and/or locations
     */
    private List<com.finx.allocationreallocationservice.domain.entity.Case> getUnallocatedCasesMatchingMultiFilters(
            List<String> states, List<String> cities, List<String> locations, List<String> buckets) {
        List<com.finx.allocationreallocationservice.domain.entity.Case> allMatchingCases = new ArrayList<>();
        int page = 0;
        int size = 1000; // Larger page size for better performance
        Page<com.finx.allocationreallocationservice.domain.entity.Case> casesPage;

        boolean hasStates = states != null && !states.isEmpty();
        boolean hasCities = cities != null && !cities.isEmpty();
        boolean hasLocations = locations != null && !locations.isEmpty();
        boolean hasBuckets = buckets != null && !buckets.isEmpty();

        do {
            Pageable pageable = PageRequest.of(page, size);

            // Choose the appropriate query based on which filters are provided
            if (hasBuckets) {
                // With buckets - use the flexible multi-geography query
                casesPage = caseReadRepository.findUnallocatedCasesByMultiGeographyAndBucket(
                        hasStates ? states : null,
                        hasCities ? cities : null,
                        hasLocations ? locations : null,
                        buckets, pageable);
            } else if (hasStates && hasCities && hasLocations) {
                casesPage = caseReadRepository.findUnallocatedCasesByStatesAndCitiesAndLocations(states, cities, locations, pageable);
            } else if (hasStates && hasCities) {
                casesPage = caseReadRepository.findUnallocatedCasesByStatesAndCities(states, cities, pageable);
            } else if (hasStates && hasLocations) {
                casesPage = caseReadRepository.findUnallocatedCasesByStatesAndLocations(states, locations, pageable);
            } else if (hasCities && hasLocations) {
                casesPage = caseReadRepository.findUnallocatedCasesByCitiesAndLocations(cities, locations, pageable);
            } else if (hasStates) {
                casesPage = caseReadRepository.findUnallocatedCasesByStates(states, pageable);
            } else if (hasCities) {
                casesPage = caseReadRepository.findUnallocatedCasesByCities(cities, pageable);
            } else if (hasLocations) {
                casesPage = caseReadRepository.findUnallocatedCasesByLocations(locations, pageable);
            } else {
                // No geography filters - get all ACTIVE unallocated cases
                casesPage = caseReadRepository.findByCaseStatusAndActive("UNALLOCATED", pageable);
            }

            allMatchingCases.addAll(casesPage.getContent());
            page++;
        } while (casesPage.hasNext());

        log.info("Fetched {} unallocated cases matching multi-geography filters (states: {}, cities: {}, locations: {}, buckets: {})",
                allMatchingCases.size(), states, cities, locations, buckets);

        return allMatchingCases;
    }

    private Map<String, Integer> calculateEqualPercentageDistribution(
            List<AllocationRuleSimulationDTO.EligibleAgentDTO> eligibleAgents, int totalCases) {
        Map<String, Integer> distribution = new java.util.HashMap<>();

        if (eligibleAgents.isEmpty() || totalCases == 0) {
            return distribution;
        }

        // Equal percentage split among all eligible agents
        int agentCount = eligibleAgents.size();
        int casesPerAgent = totalCases / agentCount;
        int remainder = totalCases % agentCount;

        for (int i = 0; i < eligibleAgents.size(); i++) {
            AllocationRuleSimulationDTO.EligibleAgentDTO agent = eligibleAgents.get(i);
            int casesToAllocate = casesPerAgent + (i < remainder ? 1 : 0);
            distribution.put(agent.getAgentId().toString(), casesToAllocate);
        }

        log.info("PERCENTAGE_SPLIT distribution: {} agents, {} cases each (approx)", agentCount, casesPerAgent);
        return distribution;
    }

    private Map<String, Integer> calculateCapacityBasedDistribution(
            List<AllocationRuleSimulationDTO.EligibleAgentDTO> eligibleAgents, int totalCases) {
        Map<String, Integer> distribution = new java.util.HashMap<>();

        if (eligibleAgents.isEmpty() || totalCases == 0) {
            return distribution;
        }

        // Calculate total available capacity across all agents
        int totalAvailableCapacity = eligibleAgents.stream()
                .mapToInt(AllocationRuleSimulationDTO.EligibleAgentDTO::getAvailableCapacity)
                .sum();

        if (totalAvailableCapacity == 0) {
            log.warn("No available capacity among eligible agents. Using equal distribution.");
            return calculateEqualPercentageDistribution(eligibleAgents, totalCases);
        }

        int remainingCases = totalCases;

        // Distribute based on available capacity proportion
        for (int i = 0; i < eligibleAgents.size(); i++) {
            AllocationRuleSimulationDTO.EligibleAgentDTO agent = eligibleAgents.get(i);
            int availableCapacity = agent.getAvailableCapacity();

            int casesToAllocate;
            if (i == eligibleAgents.size() - 1) {
                // Last agent gets remaining cases
                casesToAllocate = remainingCases;
            } else {
                // Calculate proportional allocation based on available capacity
                casesToAllocate = (int) Math.round((double) totalCases * availableCapacity / totalAvailableCapacity);
                casesToAllocate = Math.min(casesToAllocate, availableCapacity); // Don't exceed capacity
                casesToAllocate = Math.min(casesToAllocate, remainingCases); // Don't exceed remaining cases
            }

            if (casesToAllocate > 0) {
                distribution.put(agent.getAgentId().toString(), casesToAllocate);
                remainingCases -= casesToAllocate;
            }
        }

        log.info("CAPACITY_BASED distribution: total capacity = {}, total cases = {}", totalAvailableCapacity,
                totalCases);
        return distribution;
    }

    private Map<String, Integer> calculateGeographyBasedDistribution(
            List<AllocationRuleSimulationDTO.EligibleAgentDTO> eligibleAgents, int totalCases) {
        // For GEOGRAPHY type, distribute equally among agents in matching geographies
        return calculateEqualPercentageDistribution(eligibleAgents, totalCases);
    }

    /**
     * Map User entity to UserDTO
     */
    private UserDTO mapToUserDTO(User user) {
        if (user == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setMobileNumber(user.getMobileNumber());
        dto.setStatus(user.getStatus());
        dto.setAssignedGeographies(user.getAssignedGeographies());
        dto.setMaxCaseCapacity(user.getMaxCaseCapacity());
        dto.setCurrentCaseCount(user.getCurrentCaseCount());
        dto.setAllocationPercentage(user.getAllocationPercentage());
        dto.setAllocationBucket(user.getAllocationBucket());
        dto.setTeamId(user.getTeamId());
        dto.setCapacity(user.getMaxCaseCapacity()); // Alias for capacity
        return dto;
    }

    private void validatePercentages(List<Integer> percentages, int agentCount) {
        if (percentages == null || percentages.isEmpty()) {
            throw new ValidationException("percentages", "Percentages are required for PERCENTAGE_SPLIT rule type");
        }

        if (percentages.size() != agentCount) {
            throw new ValidationException("percentages",
                    "Number of percentages (" + percentages.size() + ") must match number of agents (" + agentCount
                            + ")");
        }

        int sum = percentages.stream().mapToInt(Integer::intValue).sum();
        if (sum != 100) {
            throw new ValidationException("percentages",
                    "Percentages must sum to 100. Current sum: " + sum);
        }

        for (Integer percentage : percentages) {
            if (percentage < 0 || percentage > 100) {
                throw new ValidationException("percentages",
                        "Each percentage must be between 0 and 100. Found: " + percentage);
            }
        }
    }

    private List<AllocationRuleExecutionResponseDTO.AllocationResultDTO> allocateByPercentage(
            List<Long> caseIds, List<UserDTO> agents, List<Integer> percentages,
            int totalCases, Long ruleId, String ruleName,
            Map<Long, com.finx.allocationreallocationservice.domain.entity.Case> caseMap) {

        List<AllocationRuleExecutionResponseDTO.AllocationResultDTO> results = new ArrayList<>();
        List<CaseAllocation> allocations = new ArrayList<>();
        List<AllocationHistory> historyEntries = new ArrayList<>();
        Map<Long, Integer> agentCaseCount = new HashMap<>();

        int caseIndex = 0;

        for (int i = 0; i < agents.size(); i++) {
            UserDTO agent = agents.get(i);
            Integer percentage = percentages.get(i);

            // Calculate cases for this agent based on percentage
            int casesForAgent;
            if (i == agents.size() - 1) {
                // Last agent gets all remaining cases
                casesForAgent = totalCases - caseIndex;
            } else {
                casesForAgent = (int) Math.round((double) totalCases * percentage / 100.0);
            }

            int allocated = 0;
            for (int j = 0; j < casesForAgent && caseIndex < caseIds.size(); j++, caseIndex++) {
                Long caseId = caseIds.get(caseIndex);
                com.finx.allocationreallocationservice.domain.entity.Case caseEntity = caseMap.get(caseId);
                String geographyCode = caseEntity != null ? caseEntity.getGeographyCode() : null;

                allocations.add(CaseAllocation.builder()
                        .caseId(caseId)
                        .primaryAgentId(agent.getId())
                        .allocatedToType("USER")
                        .allocationType("PRIMARY")
                        .status(AllocationStatus.ALLOCATED)
                        .allocationRuleId(ruleId)
                        .allocatedAt(LocalDateTime.now())
                        .workloadPercentage(new java.math.BigDecimal("100.00"))
                        .geographyCode(geographyCode)
                        .build());

                historyEntries.add(AllocationHistory.builder()
                        .caseId(caseId)
                        .allocatedToUserId(agent.getId())
                        .newOwnerType("USER")
                        .action(AllocationAction.ALLOCATED)
                        .reason("PERCENTAGE_SPLIT allocation: " + ruleName + " (" + percentage + "%)")
                        .allocatedAt(LocalDateTime.now())
                        .build());

                allocated++;
            }

            if (allocated > 0) {
                agentCaseCount.put(agent.getId(), allocated);
                results.add(AllocationRuleExecutionResponseDTO.AllocationResultDTO.builder()
                        .agentId(agent.getId())
                        .allocated(allocated)
                        .build());
            }
        }

        caseAllocationRepository.saveAll(allocations);
        allocationHistoryRepository.saveAll(historyEntries);

        // Update user statistics
        updateUserStatistics(agentCaseCount);

        log.info("PERCENTAGE_SPLIT allocation completed: {} cases allocated to {} agents", caseIndex, agents.size());
        return results;
    }

    private List<AllocationRuleExecutionResponseDTO.AllocationResultDTO> allocateByCapacity(
            List<Long> caseIds, List<UserDTO> agents, int totalCases, Long ruleId, String ruleName,
            Map<Long, com.finx.allocationreallocationservice.domain.entity.Case> caseMap) {

        List<AllocationRuleExecutionResponseDTO.AllocationResultDTO> results = new ArrayList<>();
        List<CaseAllocation> allocations = new ArrayList<>();
        List<AllocationHistory> historyEntries = new ArrayList<>();
        Map<Long, Integer> agentCaseCount = new HashMap<>();

        // Calculate available capacity for each agent
        Map<Long, Integer> agentCapacity = new HashMap<>();
        int totalAvailableCapacity = 0;

        for (UserDTO agent : agents) {
            long currentWorkload = caseAllocationRepository.countByPrimaryAgentIdAndStatus(
                    agent.getId(), AllocationStatus.ALLOCATED);
            int capacity = agent.getCapacity() != null ? agent.getCapacity() : 100;
            int availableCapacity = Math.max(0, capacity - (int) currentWorkload);

            agentCapacity.put(agent.getId(), availableCapacity);
            totalAvailableCapacity += availableCapacity;
        }

        if (totalAvailableCapacity == 0) {
            log.warn("No available capacity among agents. Using equal distribution.");
            return allocateEqually(caseIds, agents, totalCases, ruleId, ruleName, caseMap);
        }

        int caseIndex = 0;

        for (UserDTO agent : agents) {
            if (caseIndex >= totalCases)
                break;

            int availableCapacity = agentCapacity.get(agent.getId());
            if (availableCapacity <= 0)
                continue;

            // Calculate cases for this agent based on capacity proportion
            int casesForAgent = (int) Math.round((double) totalCases * availableCapacity / totalAvailableCapacity);
            casesForAgent = Math.min(casesForAgent, availableCapacity);
            casesForAgent = Math.min(casesForAgent, totalCases - caseIndex);

            int allocated = 0;
            for (int i = 0; i < casesForAgent && caseIndex < caseIds.size(); i++, caseIndex++) {
                Long caseId = caseIds.get(caseIndex);
                com.finx.allocationreallocationservice.domain.entity.Case caseEntity = caseMap.get(caseId);
                String geographyCode = caseEntity != null ? caseEntity.getGeographyCode() : null;

                allocations.add(CaseAllocation.builder()
                        .caseId(caseId)
                        .primaryAgentId(agent.getId())
                        .allocatedToType("USER")
                        .allocationType("PRIMARY")
                        .status(AllocationStatus.ALLOCATED)
                        .allocationRuleId(ruleId)
                        .allocatedAt(LocalDateTime.now())
                        .workloadPercentage(new java.math.BigDecimal("100.00"))
                        .geographyCode(geographyCode)
                        .build());

                historyEntries.add(AllocationHistory.builder()
                        .caseId(caseId)
                        .allocatedToUserId(agent.getId())
                        .newOwnerType("USER")
                        .action(AllocationAction.ALLOCATED)
                        .reason("CAPACITY_BASED allocation: " + ruleName)
                        .allocatedAt(LocalDateTime.now())
                        .build());

                allocated++;
            }

            if (allocated > 0) {
                agentCaseCount.put(agent.getId(), allocated);
                results.add(AllocationRuleExecutionResponseDTO.AllocationResultDTO.builder()
                        .agentId(agent.getId())
                        .allocated(allocated)
                        .build());
            }
        }

        caseAllocationRepository.saveAll(allocations);
        allocationHistoryRepository.saveAll(historyEntries);

        // Update user statistics
        updateUserStatistics(agentCaseCount);

        log.info("CAPACITY_BASED allocation completed: {} cases allocated to {} agents", caseIndex, agents.size());
        return results;
    }

    private List<AllocationRuleExecutionResponseDTO.AllocationResultDTO> allocateByGeography(
            List<Long> caseIds, List<UserDTO> agents, int totalCases, Long ruleId, String ruleName,
            Map<Long, com.finx.allocationreallocationservice.domain.entity.Case> caseMap) {
        // Geography-based allocation uses equal distribution among agents
        return allocateEqually(caseIds, agents, totalCases, ruleId, ruleName, caseMap);
    }

    private List<AllocationRuleExecutionResponseDTO.AllocationResultDTO> allocateEqually(
            List<Long> caseIds, List<UserDTO> agents, int totalCases, Long ruleId, String ruleName,
            Map<Long, com.finx.allocationreallocationservice.domain.entity.Case> caseMap) {

        List<AllocationRuleExecutionResponseDTO.AllocationResultDTO> results = new ArrayList<>();
        List<CaseAllocation> allocations = new ArrayList<>();
        List<AllocationHistory> historyEntries = new ArrayList<>();
        Map<Long, Integer> agentCaseCount = new HashMap<>();

        int casesPerAgent = totalCases / agents.size();
        int remainder = totalCases % agents.size();
        int caseIndex = 0;

        for (int i = 0; i < agents.size(); i++) {
            UserDTO agent = agents.get(i);

            // Distribute remainder to first few agents
            int casesForAgent = casesPerAgent + (i < remainder ? 1 : 0);

            int allocated = 0;
            for (int j = 0; j < casesForAgent && caseIndex < caseIds.size(); j++, caseIndex++) {
                Long caseId = caseIds.get(caseIndex);
                com.finx.allocationreallocationservice.domain.entity.Case caseEntity = caseMap.get(caseId);
                String geographyCode = caseEntity != null ? caseEntity.getGeographyCode() : null;

                allocations.add(CaseAllocation.builder()
                        .caseId(caseId)
                        .primaryAgentId(agent.getId())
                        .allocatedToType("USER")
                        .allocationType("PRIMARY")
                        .status(AllocationStatus.ALLOCATED)
                        .allocationRuleId(ruleId)
                        .allocatedAt(LocalDateTime.now())
                        .workloadPercentage(new java.math.BigDecimal("100.00"))
                        .geographyCode(geographyCode)
                        .build());

                historyEntries.add(AllocationHistory.builder()
                        .caseId(caseId)
                        .allocatedToUserId(agent.getId())
                        .newOwnerType("USER")
                        .action(AllocationAction.ALLOCATED)
                        .reason("Equal allocation: " + ruleName)
                        .allocatedAt(LocalDateTime.now())
                        .build());

                allocated++;
            }

            if (allocated > 0) {
                agentCaseCount.put(agent.getId(), allocated);
                results.add(AllocationRuleExecutionResponseDTO.AllocationResultDTO.builder()
                        .agentId(agent.getId())
                        .allocated(allocated)
                        .build());
            }
        }

        caseAllocationRepository.saveAll(allocations);
        allocationHistoryRepository.saveAll(historyEntries);

        // Update user statistics
        updateUserStatistics(agentCaseCount);

        log.info("Equal allocation completed: {} cases allocated to {} agents", caseIndex, agents.size());
        return results;
    }

    /**
     * Update user statistics after allocation
     * Updates current_case_count and allocation_percentage
     * 
     * @param agentCaseCount Map of agentId to number of cases allocated
     */
    @SuppressWarnings("null")
    private void updateUserStatistics(Map<Long, Integer> agentCaseCount) {
        log.info("Updating user statistics for {} agents", agentCaseCount.size());

        for (Map.Entry<Long, Integer> entry : agentCaseCount.entrySet()) {
            Long agentId = entry.getKey();
            Integer casesAllocated = entry.getValue();

            try {
                User user = userRepository.findById(agentId).orElse(null);
                if (user == null) {
                    log.warn("User {} not found for statistics update", agentId);
                    continue;
                }

                // Update current_case_count
                Integer currentCaseCount = user.getCurrentCaseCount() != null ? user.getCurrentCaseCount() : 0;
                Integer newCaseCount = currentCaseCount + casesAllocated;
                user.setCurrentCaseCount(newCaseCount);

                // Calculate and update allocation_percentage
                // Formula: (current_case_count / max_case_capacity) * 100
                Integer maxCapacity = user.getMaxCaseCapacity() != null ? user.getMaxCaseCapacity() : 100;
                if (maxCapacity > 0) {
                    double allocationPercentage = ((double) newCaseCount / maxCapacity) * 100.0;
                    // Round to 2 decimal places
                    allocationPercentage = Math.round(allocationPercentage * 100.0) / 100.0;
                    user.setAllocationPercentage(allocationPercentage);
                } else {
                    log.warn("User {} has invalid max capacity: {}", agentId, maxCapacity);
                    user.setAllocationPercentage(0.0);
                }

                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);

                log.info("Updated user {} statistics: currentCaseCount={}, allocationPercentage={}%",
                        agentId, newCaseCount, user.getAllocationPercentage());

            } catch (Exception e) {
                log.error("Failed to update statistics for user {}: {}", agentId, e.getMessage(), e);
            }
        }
    }

    @SuppressWarnings("null")
    @Override
    @Cacheable(value = "caseAllocation", key = "#caseId")
    public CaseAllocationDTO getCaseAllocation(Long caseId) {
        log.info("Fetching case allocation for case: {}", caseId);

        CaseAllocation allocation = caseAllocationRepository.findFirstByCaseIdOrderByAllocatedAtDesc(caseId)
                .orElseThrow(() -> new BusinessException("Case allocation not found for case: " + caseId));

        UserDTO primaryAgent = null;
        if (allocation.getPrimaryAgentId() != null) {
            primaryAgent = userRepository.findById(allocation.getPrimaryAgentId())
                    .map(this::mapToUserDTO)
                    .orElse(null);
        }

        UserDTO secondaryAgent = null;
        if (allocation.getSecondaryAgentId() != null) {
            secondaryAgent = userRepository.findById(allocation.getSecondaryAgentId())
                    .map(this::mapToUserDTO)
                    .orElse(null);
        }

        return CaseAllocationDTO.builder()
                .caseId(allocation.getCaseId())
                .primaryAgent(primaryAgent != null ? CaseAllocationDTO.AgentDTO.builder()
                        .userId(primaryAgent.getId())
                        .username(primaryAgent.getUsername())
                        .build() : null)
                .secondaryAgent(secondaryAgent != null ? CaseAllocationDTO.AgentDTO.builder()
                        .userId(secondaryAgent.getId())
                        .username(secondaryAgent.getUsername())
                        .build() : null)
                .allocatedAt(allocation.getAllocatedAt())
                .build();
    }

    @SuppressWarnings("null")
    @Override
    @Cacheable(value = "allocationHistory", key = "#caseId")
    public AllocationHistoryDTO getCaseAllocationHistory(Long caseId) {
        log.info("Fetching allocation history for case: {}", caseId);

        List<AllocationHistory> history = allocationHistoryRepository.findByCaseIdOrderByAllocatedAtDesc(caseId);

        List<AllocationHistoryDTO.HistoryItemDTO> historyItems = history.stream()
                .map(h -> {
                    UserDTO user = userRepository.findById(h.getAllocatedToUserId())
                            .map(this::mapToUserDTO)
                            .orElse(null);
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

    @SuppressWarnings("null")
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

        ContactUpdateBatch savedBatch = contactUpdateBatchRepository.save(batch);

        saveAuditLog("CONTACT_UPDATE_BATCH", savedBatch.getId(), "CREATE", null, savedBatch);

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
                .totalCases(0) // In the response DTO, it's still totalCases, which might be a minor bug in the
                               // DTO design
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

    @SuppressWarnings("null")
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

    @SuppressWarnings("null")
    private AuditLogDTO mapToAuditLogDTO(AuditLog auditLog) {
        UserDTO user = auditLog.getUserId() != null
                ? userRepository.findById(auditLog.getUserId()).map(this::mapToUserDTO).orElse(null)
                : null;
        Map<String, AuditLogDTO.ChangeDTO> changesMap = null;
        if (auditLog.getChangedFields() != null) {
            try {
                changesMap = objectMapper.readValue(auditLog.getChangedFields(),
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, AuditLogDTO.ChangeDTO>>() {
                        });
            } catch (JsonProcessingException e) {
                log.error("Error deserializing audit log changes for auditId: {}", auditLog.getId(), e);
                // Optionally, handle this error more gracefully, e.g., return an empty map or a
                // map with an error message
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
            // New multi-field geography filters
            if (criteria.containsKey("states")) {
                builder.states((List<String>) criteria.get("states"));
            }
            if (criteria.containsKey("cities")) {
                builder.cities((List<String>) criteria.get("cities"));
            }
            if (criteria.containsKey("locations")) {
                builder.locations((List<String>) criteria.get("locations"));
            }
            // Legacy field
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
        // New multi-field geography filters
        if (ruleDTO.getStates() != null && !ruleDTO.getStates().isEmpty()) {
            criteriaMap.put("states", ruleDTO.getStates());
        }
        if (ruleDTO.getCities() != null && !ruleDTO.getCities().isEmpty()) {
            criteriaMap.put("cities", ruleDTO.getCities());
        }
        if (ruleDTO.getLocations() != null && !ruleDTO.getLocations().isEmpty()) {
            criteriaMap.put("locations", ruleDTO.getLocations());
        }
        // Legacy geography field (for backward compatibility)
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

    private Map<String, Object> buildCriteriaMapForCreate(AllocationRuleDTO ruleDTO) {
        Map<String, Object> criteriaMap = new java.util.HashMap<>();

        if (ruleDTO.getRuleType() != null) {
            criteriaMap.put("ruleType", ruleDTO.getRuleType());
        }
        // New multi-field geography filters
        if (ruleDTO.getStates() != null && !ruleDTO.getStates().isEmpty()) {
            criteriaMap.put("states", ruleDTO.getStates());
        }
        if (ruleDTO.getCities() != null && !ruleDTO.getCities().isEmpty()) {
            criteriaMap.put("cities", ruleDTO.getCities());
        }
        if (ruleDTO.getLocations() != null && !ruleDTO.getLocations().isEmpty()) {
            criteriaMap.put("locations", ruleDTO.getLocations());
        }
        // Legacy geography field (for backward compatibility)
        if (ruleDTO.getGeographies() != null) {
            criteriaMap.put("geographies", ruleDTO.getGeographies());
        }
        if (ruleDTO.getBuckets() != null) {
            criteriaMap.put("buckets", ruleDTO.getBuckets());
        }
        if (ruleDTO.getMaxCasesPerAgent() != null) {
            criteriaMap.put("maxCasesPerAgent", ruleDTO.getMaxCasesPerAgent());
        }
        // Intentionally ignoring agentIds and percentages for create operation
        // These will be configured via a separate endpoint

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
    public List<AllocationBatchDTO> getAllBatches(String status, LocalDate startDate, LocalDate endDate, int page,
            int size) {
        log.info("Fetching all batches with filters - status: {}, startDate: {}, endDate: {}", status, startDate,
                endDate);

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

    @SuppressWarnings("null")
    @Override
    @Transactional
    @CacheEvict(value = "allocationRules", allEntries = true)
    public AllocationRuleExecutionResponseDTO applyAllocationRule(Long ruleId,
            AllocationRuleExecutionRequestDTO request) {
        log.info("Applying allocation rule: {} with agentIds: {}, percentages: {}",
                ruleId, request.getAgentIds(), request.getPercentages());

        // Fetch and validate rule
        AllocationRule rule = allocationRuleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Allocation rule not found: " + ruleId));

        // Enforce lifecycle: apply() allowed only if status == READY_FOR_APPLY
        if (rule.getStatus() != RuleStatus.READY_FOR_APPLY) {
            throw new ValidationException(
                    "Simulation required before applying rule. Current status: " + rule.getStatus());
        }

        // Get rule criteria
        Map<String, Object> criteria = rule.getCriteria();
        String ruleType = (String) criteria.getOrDefault("ruleType", criteria.get("type"));

        // Extract multi-field geography filters
        List<String> states = (List<String>) criteria.get("states");
        List<String> cities = (List<String>) criteria.get("cities");
        List<String> locations = (List<String>) criteria.get("locations");
        List<String> geographies = (List<String>) criteria.get("geographies"); // Legacy field
        List<String> buckets = (List<String>) criteria.get("buckets");

        // Determine which geography filter to use
        boolean hasNewGeoFilters = (states != null && !states.isEmpty())
                || (cities != null && !cities.isEmpty())
                || (locations != null && !locations.isEmpty());

        // Validate agentIds are provided
        List<Long> agentIds = request.getAgentIds();
        if (agentIds == null || agentIds.isEmpty()) {
            throw new ValidationException("agentIds", "Agent IDs are required for applying allocation rule");
        }

        // Validate percentages for PERCENTAGE_SPLIT
        if ("PERCENTAGE_SPLIT".equals(ruleType)) {
            validatePercentages(request.getPercentages(), agentIds.size());
        }

        // Validate all agent IDs exist and fetch agent details
        List<UserDTO> agents = new ArrayList<>();
        for (Long agentId : agentIds) {
            try {
                User user = userRepository.findById(agentId)
                        .orElseThrow(
                                () -> new ValidationException("agentIds", "Agent with ID " + agentId + " not found"));
                agents.add(mapToUserDTO(user));
            } catch (ValidationException e) {
                throw e;
            } catch (Exception e) {
                log.error("Failed to fetch agent with ID {}: {}", agentId, e.getMessage());
                throw new ValidationException("agentIds", "Agent with ID " + agentId + " is invalid or unavailable");
            }
        }

        // Determine which cases to allocate
        List<Long> unallocatedCaseIds;

        if (request.getCaseIds() != null && !request.getCaseIds().isEmpty()) {
            // Use provided case IDs from simulate response
            unallocatedCaseIds = request.getCaseIds();
            log.info("Using {} provided case IDs from request", unallocatedCaseIds.size());

            // Validate that provided cases exist and are unallocated
            List<com.finx.allocationreallocationservice.domain.entity.Case> requestedCases = caseReadRepository
                    .findAllById(unallocatedCaseIds);

            if (requestedCases.size() != unallocatedCaseIds.size()) {
                throw new ValidationException("caseIds", "Some provided case IDs do not exist");
            }

            // Verify all cases are unallocated
            for (com.finx.allocationreallocationservice.domain.entity.Case caseEntity : requestedCases) {
                if (caseEntity.getAllocatedToUserId() != null) {
                    throw new ValidationException("caseIds",
                            "Case ID " + caseEntity.getId() + " is already allocated");
                }
            }
        } else {
            // Fetch all matching unallocated cases based on rule criteria
            List<com.finx.allocationreallocationservice.domain.entity.Case> matchingCases;
            if (hasNewGeoFilters) {
                // Use new multi-field geography filtering
                matchingCases = getUnallocatedCasesMatchingMultiFilters(states, cities, locations, buckets);
            } else {
                // Fall back to legacy geography filtering
                matchingCases = getUnallocatedCasesMatchingFilters(geographies, buckets);
            }

            unallocatedCaseIds = matchingCases.stream()
                    .map(com.finx.allocationreallocationservice.domain.entity.Case::getId)
                    .collect(Collectors.toList());

            log.info("Fetched {} unallocated cases matching rule criteria", unallocatedCaseIds.size());
        }

        if (unallocatedCaseIds.isEmpty()) {
            throw new BusinessException("No unallocated cases available for allocation");
        }

        int maxCases = request.getMaxCases() != null ? request.getMaxCases() : unallocatedCaseIds.size();
        int casesToAllocate = Math.min(maxCases, unallocatedCaseIds.size());

        log.info("Found {} unallocated cases, allocating {} cases to {} agents",
                unallocatedCaseIds.size(), casesToAllocate, agents.size());

        // Fetch case entities to get geography codes
        List<com.finx.allocationreallocationservice.domain.entity.Case> cases = caseReadRepository
                .findAllById(unallocatedCaseIds);

        // Create a map of caseId to Case entity for quick lookup
        Map<Long, com.finx.allocationreallocationservice.domain.entity.Case> caseMap = cases.stream()
                .collect(Collectors.toMap(
                        com.finx.allocationreallocationservice.domain.entity.Case::getId,
                        c -> c));

        // Allocate cases based on rule type
        List<AllocationRuleExecutionResponseDTO.AllocationResultDTO> results;
        if ("PERCENTAGE_SPLIT".equals(ruleType)) {
            results = allocateByPercentage(unallocatedCaseIds, agents, request.getPercentages(), casesToAllocate,
                    ruleId, rule.getName(), caseMap);
        } else if ("CAPACITY_BASED".equals(ruleType)) {
            results = allocateByCapacity(unallocatedCaseIds, agents, casesToAllocate, ruleId, rule.getName(), caseMap);
        } else if ("GEOGRAPHY".equals(ruleType)) {
            results = allocateByGeography(unallocatedCaseIds, agents, casesToAllocate, ruleId, rule.getName(), caseMap);
        } else {
            // Default to equal distribution
            results = allocateEqually(unallocatedCaseIds, agents, casesToAllocate, ruleId, rule.getName(), caseMap);
        }

        int totalAllocated = results.stream()
                .mapToInt(AllocationRuleExecutionResponseDTO.AllocationResultDTO::getAllocated)
                .sum();

        // Update rule status to ACTIVE
        rule.setStatus(RuleStatus.ACTIVE);
        allocationRuleRepository.save(rule);

        log.info("Successfully allocated {} cases and marked rule {} as ACTIVE", totalAllocated, ruleId);

        saveAuditLog("ALLOCATION_RULE", ruleId, "APPLY",
                Map.of("previousStatus", "READY_FOR_APPLY"),
                Map.of("status", "ACTIVE", "totalCasesAllocated", totalAllocated));

        // Evict unallocated cases cache in case-sourcing-service
        evictCaseSourcingCache();

        return AllocationRuleExecutionResponseDTO.builder()
                .ruleId(ruleId)
                .totalCasesAllocated(totalAllocated)
                .allocations(results)
                .status(RuleStatus.ACTIVE.name())
                .build();
    }

    @SuppressWarnings("null")
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

        // Evict unallocated cases cache after bulk deallocation
        if (successful > 0) {
            evictCaseSourcingCache();
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
            agents = agentIds.stream()
                    .map(agentId -> {
                        try {
                            return userRepository.findById(agentId).map(this::mapToUserDTO).orElse(null);
                        } catch (Exception e) {
                            log.warn("Failed to fetch user {}: {}", agentId, e.getMessage());
                            return null;
                        }
                    })
                    .filter(user -> user != null)
                    .collect(Collectors.toList());
        } else if (geographies != null && !geographies.isEmpty()) {
            try {
                List<User> users = userRepository.findByGeographies(geographies.toArray(new String[0]));
                agents = users.stream()
                        .map(this::mapToUserDTO)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                log.error("Failed to fetch users by geography: {}", e.getMessage());
            }
        } else {
            try {
                List<User> users = userRepository.findAllActiveUsers();
                agents = users.stream()
                        .map(this::mapToUserDTO)
                        .collect(Collectors.toList());
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

    /**
     * Evict unallocated cases cache in case-sourcing-service.
     * Called after allocation/deallocation to ensure the unallocated cases list is refreshed.
     */
    private void evictCaseSourcingCache() {
        try {
            caseSourcingServiceClient.evictUnallocatedCasesCache();
            log.info("Successfully evicted unallocated cases cache in case-sourcing-service");
        } catch (Exception e) {
            // Log error but don't fail the allocation - cache will eventually be refreshed by TTL
            log.warn("Failed to evict unallocated cases cache in case-sourcing-service: {}", e.getMessage());
        }
    }
}
