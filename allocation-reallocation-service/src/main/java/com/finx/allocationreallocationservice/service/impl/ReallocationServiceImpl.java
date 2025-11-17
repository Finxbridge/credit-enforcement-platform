package com.finx.allocationreallocationservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finx.allocationreallocationservice.domain.dto.AllocationBatchStatusDTO;
import com.finx.allocationreallocationservice.domain.dto.AllocationBatchUploadResponseDTO;
import com.finx.allocationreallocationservice.domain.dto.ReallocationByAgentRequestDTO;
import com.finx.allocationreallocationservice.domain.dto.ReallocationByFilterRequestDTO;
import com.finx.allocationreallocationservice.domain.dto.ReallocationResponseDTO;
import com.finx.allocationreallocationservice.domain.entity.AllocationBatch;
import com.finx.allocationreallocationservice.domain.entity.AllocationHistory;
import com.finx.allocationreallocationservice.domain.entity.AuditLog;
import com.finx.allocationreallocationservice.domain.entity.BatchError;
import com.finx.allocationreallocationservice.domain.entity.CaseAllocation;
import com.finx.allocationreallocationservice.domain.enums.AllocationAction;
import com.finx.allocationreallocationservice.domain.enums.BatchStatus;
import com.finx.allocationreallocationservice.exception.ResourceNotFoundException;
import com.finx.allocationreallocationservice.repository.AllocationBatchRepository;
import com.finx.allocationreallocationservice.repository.AllocationHistoryRepository;
import com.finx.allocationreallocationservice.repository.AuditLogRepository;
import com.finx.allocationreallocationservice.repository.BatchErrorRepository;
import com.finx.allocationreallocationservice.repository.CaseAllocationRepository;
import com.finx.allocationreallocationservice.service.ReallocationService;
import com.finx.allocationreallocationservice.service.async.AllocationBatchProcessingService;
import com.finx.allocationreallocationservice.util.csv.CsvExporter;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.finx.allocationreallocationservice.exception.BusinessException;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronization;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReallocationServiceImpl implements ReallocationService {

    private final AllocationBatchRepository allocationBatchRepository;
    private final CaseAllocationRepository caseAllocationRepository;
    private final AllocationHistoryRepository allocationHistoryRepository;
    private final AllocationBatchProcessingService batchProcessingService;
    private final AuditLogRepository auditLogRepository;
    private final BatchErrorRepository batchErrorRepository;
    private final CsvExporter csvExporter;
    private final ObjectMapper objectMapper;
    private final com.finx.allocationreallocationservice.repository.UserRepository userRepository;
    private final com.finx.allocationreallocationservice.repository.CaseReadRepository caseReadRepository;

    @SuppressWarnings("null")
    @Override
    @Transactional
    public AllocationBatchUploadResponseDTO uploadReallocationBatch(MultipartFile file) {
        log.info("Processing reallocation batch upload: {}", file.getOriginalFilename());

        String batchId = "REALLOC_BATCH_" + System.currentTimeMillis();

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
            tempFilePath = Files.createTempFile("realloc_upload_", ".csv");
            file.transferTo(tempFilePath);
        } catch (IOException e) {
            log.error("Failed to save uploaded file to temporary location", e);
            throw new BusinessException("Failed to process file upload: " + e.getMessage());
        }

        // Register async processing to run after transaction commits
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                batchProcessingService.processReallocationBatchAsync(batchId, tempFilePath.toString());
            }
        });

        return AllocationBatchUploadResponseDTO.builder()
                .batchId(batchId)
                .totalCases(0)
                .status(BatchStatus.PROCESSING.name())
                .build();
    }

    @SuppressWarnings("null")
    @Override
    @Transactional
    public ReallocationResponseDTO reallocateByAgent(ReallocationByAgentRequestDTO request) {
        log.info("Processing reallocation from user {} to user {}",
                request.getFromUserId(), request.getToUserId());

        String jobId = "REALLOC_JOB_" + System.currentTimeMillis();

        List<CaseAllocation> allocations = caseAllocationRepository.findByPrimaryAgentId(request.getFromUserId());

        if (allocations.isEmpty()) {
            return ReallocationResponseDTO.builder()
                    .jobId(jobId)
                    .status("COMPLETED")
                    .casesReallocated(0L)
                    .build();
        }

        List<CaseAllocation> oldAllocations = allocations.stream().map(alloc -> alloc.toBuilder().build())
                .collect(Collectors.toList());

        int casesReallocated = allocations.size();

        // Update allocations with new agent and maintain/update fields
        allocations.forEach(alloc -> {
            alloc.setPrimaryAgentId(request.getToUserId());

            // Maintain workload_percentage (default 100.00 if not set)
            if (alloc.getWorkloadPercentage() == null) {
                alloc.setWorkloadPercentage(new java.math.BigDecimal("100.00"));
            }

            // Update geography code from case entity if possible
            try {
                java.util.Optional<com.finx.allocationreallocationservice.domain.entity.Case> caseOpt = caseReadRepository
                        .findById(alloc.getCaseId());
                if (caseOpt.isPresent() && caseOpt.get().getGeographyCode() != null) {
                    alloc.setGeographyCode(caseOpt.get().getGeographyCode());
                }
            } catch (Exception e) {
                log.warn("Failed to fetch case entity for caseId {}: {}", alloc.getCaseId(), e.getMessage());
            }
        });

        caseAllocationRepository.saveAll(allocations);

        List<AllocationHistory> history = allocations.stream()
                .map(alloc -> AllocationHistory.builder()
                        .caseId(alloc.getCaseId())
                        .allocatedToUserId(request.getToUserId())
                        .allocatedFromUserId(request.getFromUserId())
                        .newOwnerType("USER")
                        .previousOwnerType("USER")
                        .allocatedAt(LocalDateTime.now())
                        .action(AllocationAction.REALLOCATED)
                        .reason(request.getReason())
                        .build())
                .collect(Collectors.toList());
        allocationHistoryRepository.saveAll(history);

        for (int i = 0; i < allocations.size(); i++) {
            saveAuditLog("CASE_ALLOCATION", allocations.get(i).getId(), "REALLOCATE_BY_AGENT", oldAllocations.get(i),
                    allocations.get(i));
        }

        // Update user statistics
        java.util.Map<Long, Integer> decrements = new java.util.HashMap<>();
        java.util.Map<Long, Integer> increments = new java.util.HashMap<>();
        decrements.put(request.getFromUserId(), casesReallocated);
        increments.put(request.getToUserId(), casesReallocated);
        updateUserStatisticsForReallocation(decrements, increments);

        return ReallocationResponseDTO.builder()
                .jobId(jobId)
                .status("COMPLETED")
                .casesReallocated((long) casesReallocated)
                .build();
    }

    @SuppressWarnings("null")
    @Override
    @Transactional
    public ReallocationResponseDTO reallocateByFilter(ReallocationByFilterRequestDTO request) {
        log.info("Processing reallocation by filter to user {}", request.getToUserId());

        String jobId = "REALLOC_JOB_" + System.currentTimeMillis();

        Specification<CaseAllocation> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (request.getFilterCriteria() != null) {
                // This is a simplified example. A real implementation would need to handle
                // different fields and operators.
                if (request.getFilterCriteria().get("bucket") != null) {
                    predicates
                            .add(criteriaBuilder.equal(root.get("bucket"), request.getFilterCriteria().get("bucket")));
                }
                if (request.getFilterCriteria().get("status") != null) {
                    predicates
                            .add(criteriaBuilder.equal(root.get("status"), request.getFilterCriteria().get("status")));
                }
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        List<CaseAllocation> allocations = caseAllocationRepository.findAll(spec);

        if (allocations.isEmpty()) {
            return ReallocationResponseDTO.builder()
                    .jobId(jobId)
                    .status("COMPLETED")
                    .estimatedCases(0L)
                    .build();
        }

        List<CaseAllocation> oldAllocations = allocations.stream().map(alloc -> alloc.toBuilder().build())
                .collect(Collectors.toList());

        // Track agent changes for statistics update
        java.util.Map<Long, Integer> decrements = new java.util.HashMap<>();
        java.util.Map<Long, Integer> increments = new java.util.HashMap<>();

        // Update allocations with new agent and maintain/update fields
        allocations.forEach(alloc -> {
            Long oldAgentId = alloc.getPrimaryAgentId();
            decrements.put(oldAgentId, decrements.getOrDefault(oldAgentId, 0) + 1);
            alloc.setPrimaryAgentId(request.getToUserId());

            // Maintain workload_percentage (default 100.00 if not set)
            if (alloc.getWorkloadPercentage() == null) {
                alloc.setWorkloadPercentage(new java.math.BigDecimal("100.00"));
            }

            // Update geography code from case entity if possible
            try {
                java.util.Optional<com.finx.allocationreallocationservice.domain.entity.Case> caseOpt = caseReadRepository
                        .findById(alloc.getCaseId());
                if (caseOpt.isPresent() && caseOpt.get().getGeographyCode() != null) {
                    alloc.setGeographyCode(caseOpt.get().getGeographyCode());
                }
            } catch (Exception e) {
                log.warn("Failed to fetch case entity for caseId {}: {}", alloc.getCaseId(), e.getMessage());
            }
        });
        increments.put(request.getToUserId(), allocations.size());

        caseAllocationRepository.saveAll(allocations);

        List<AllocationHistory> history = new ArrayList<>();
        for (int i = 0; i < allocations.size(); i++) {
            CaseAllocation oldAlloc = oldAllocations.get(i);
            CaseAllocation newAlloc = allocations.get(i);
            history.add(AllocationHistory.builder()
                    .caseId(newAlloc.getCaseId())
                    .allocatedToUserId(request.getToUserId())
                    .allocatedFromUserId(oldAlloc.getPrimaryAgentId())
                    .newOwnerType("USER")
                    .previousOwnerType("USER")
                    .allocatedAt(LocalDateTime.now())
                    .action(AllocationAction.REALLOCATED)
                    .reason(request.getReason())
                    .build());
        }
        allocationHistoryRepository.saveAll(history);

        for (int i = 0; i < allocations.size(); i++) {
            saveAuditLog("CASE_ALLOCATION", allocations.get(i).getId(), "REALLOCATE_BY_FILTER", oldAllocations.get(i),
                    allocations.get(i));
        }

        // Update user statistics
        updateUserStatisticsForReallocation(decrements, increments);

        return ReallocationResponseDTO.builder()
                .jobId(jobId)
                .status("COMPLETED")
                .estimatedCases((long) allocations.size())
                .build();
    }

    @Override
    public AllocationBatchStatusDTO getReallocationBatchStatus(String batchId) {
        log.info("Fetching reallocation batch status for: {}", batchId);

        AllocationBatch batch = allocationBatchRepository.findByBatchId(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Reallocation batch not found: " + batchId));

        return AllocationBatchStatusDTO.builder()
                .batchId(batch.getBatchId())
                .totalCases(batch.getTotalCases())
                .successful(batch.getSuccessfulAllocations())
                .failed(batch.getFailedAllocations())
                .status(batch.getStatus().name())
                .build();
    }

    @Override
    public byte[] exportFailedReallocationRows(String batchId) {
        log.info("Exporting failed reallocation rows for batch: {}", batchId);

        if (!allocationBatchRepository.existsByBatchId(batchId)) {
            throw new ResourceNotFoundException("Reallocation batch not found: " + batchId);
        }

        List<BatchError> errors = batchErrorRepository.findByBatchId(batchId);

        if (errors.isEmpty()) {
            throw new BusinessException("No errors found for batch: " + batchId);
        }

        return csvExporter.exportBatchErrors(errors);
    }

    /**
     * Update user statistics after reallocation
     * Decreases current_case_count for old agents and increases for new agents
     * Recalculates allocation_percentage for all affected agents
     * 
     * @param agentDecrements Map of agentId to number of cases removed
     * @param agentIncrements Map of agentId to number of cases added
     */
    @SuppressWarnings("null")
    private void updateUserStatisticsForReallocation(Map<Long, Integer> agentDecrements,
            Map<Long, Integer> agentIncrements) {
        log.info("Updating user statistics for reallocation: {} agents decremented, {} agents incremented",
                agentDecrements.size(), agentIncrements.size());

        // Process decrements (cases removed from old agents)
        for (Map.Entry<Long, Integer> entry : agentDecrements.entrySet()) {
            Long agentId = entry.getKey();
            Integer casesRemoved = entry.getValue();

            try {
                com.finx.allocationreallocationservice.domain.entity.User user = userRepository.findById(agentId)
                        .orElse(null);
                if (user == null) {
                    log.warn("User {} not found for statistics update (decrement)", agentId);
                    continue;
                }

                // Decrease current_case_count
                Integer currentCaseCount = user.getCurrentCaseCount() != null ? user.getCurrentCaseCount() : 0;
                Integer newCaseCount = Math.max(0, currentCaseCount - casesRemoved); // Ensure non-negative
                user.setCurrentCaseCount(newCaseCount);

                // Recalculate allocation_percentage
                Integer maxCapacity = user.getMaxCaseCapacity() != null ? user.getMaxCaseCapacity() : 100;
                if (maxCapacity > 0) {
                    double allocationPercentage = ((double) newCaseCount / maxCapacity) * 100.0;
                    allocationPercentage = Math.round(allocationPercentage * 100.0) / 100.0;
                    user.setAllocationPercentage(allocationPercentage);
                } else {
                    user.setAllocationPercentage(0.0);
                }

                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);

                log.info(
                        "Decremented user {} statistics: removed {} cases, currentCaseCount={}, allocationPercentage={}%",
                        agentId, casesRemoved, newCaseCount, user.getAllocationPercentage());

            } catch (Exception e) {
                log.error("Failed to update statistics for user {} (decrement): {}", agentId, e.getMessage(), e);
            }
        }

        // Process increments (cases added to new agents)
        for (Map.Entry<Long, Integer> entry : agentIncrements.entrySet()) {
            Long agentId = entry.getKey();
            Integer casesAdded = entry.getValue();

            try {
                com.finx.allocationreallocationservice.domain.entity.User user = userRepository.findById(agentId)
                        .orElse(null);
                if (user == null) {
                    log.warn("User {} not found for statistics update (increment)", agentId);
                    continue;
                }

                // Increase current_case_count
                Integer currentCaseCount = user.getCurrentCaseCount() != null ? user.getCurrentCaseCount() : 0;
                Integer newCaseCount = currentCaseCount + casesAdded;
                user.setCurrentCaseCount(newCaseCount);

                // Recalculate allocation_percentage
                Integer maxCapacity = user.getMaxCaseCapacity() != null ? user.getMaxCaseCapacity() : 100;
                if (maxCapacity > 0) {
                    double allocationPercentage = ((double) newCaseCount / maxCapacity) * 100.0;
                    allocationPercentage = Math.round(allocationPercentage * 100.0) / 100.0;
                    user.setAllocationPercentage(allocationPercentage);
                } else {
                    user.setAllocationPercentage(0.0);
                }

                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);

                log.info(
                        "Incremented user {} statistics: added {} cases, currentCaseCount={}, allocationPercentage={}%",
                        agentId, casesAdded, newCaseCount, user.getAllocationPercentage());

            } catch (Exception e) {
                log.error("Failed to update statistics for user {} (increment): {}", agentId, e.getMessage(), e);
            }
        }
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
        } catch (Exception e) {
            log.error("Error creating audit log", e);
        }
    }
}
