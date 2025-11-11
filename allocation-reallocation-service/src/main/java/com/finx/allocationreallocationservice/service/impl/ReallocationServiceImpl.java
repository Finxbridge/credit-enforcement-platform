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
import java.util.UUID;
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

        List<CaseAllocation> oldAllocations = allocations.stream().map(alloc -> alloc.toBuilder().build()).collect(Collectors.toList());

        allocations.forEach(alloc -> alloc.setPrimaryAgentId(request.getToUserId()));
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
            saveAuditLog("CASE_ALLOCATION", allocations.get(i).getId(), "REALLOCATE_BY_AGENT", oldAllocations.get(i), allocations.get(i));
        }

        return ReallocationResponseDTO.builder()
                .jobId(jobId)
                .status("COMPLETED")
                .casesReallocated((long) allocations.size())
                .build();
    }

    @Override
    @Transactional
    public ReallocationResponseDTO reallocateByFilter(ReallocationByFilterRequestDTO request) {
        log.info("Processing reallocation by filter to user {}", request.getToUserId());

        String jobId = "REALLOC_JOB_" + System.currentTimeMillis();

        Specification<CaseAllocation> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (request.getFilterCriteria() != null) {
                // This is a simplified example. A real implementation would need to handle different fields and operators.
                if (request.getFilterCriteria().get("bucket") != null) {
                    predicates.add(criteriaBuilder.equal(root.get("bucket"), request.getFilterCriteria().get("bucket")));
                }
                if (request.getFilterCriteria().get("status") != null) {
                    predicates.add(criteriaBuilder.equal(root.get("status"), request.getFilterCriteria().get("status")));
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

        List<CaseAllocation> oldAllocations = allocations.stream().map(alloc -> alloc.toBuilder().build()).collect(Collectors.toList());

        allocations.forEach(alloc -> alloc.setPrimaryAgentId(request.getToUserId()));
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
            saveAuditLog("CASE_ALLOCATION", allocations.get(i).getId(), "REALLOCATE_BY_FILTER", oldAllocations.get(i), allocations.get(i));
        }

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
