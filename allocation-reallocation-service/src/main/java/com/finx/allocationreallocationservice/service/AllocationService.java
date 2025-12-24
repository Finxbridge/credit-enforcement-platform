package com.finx.allocationreallocationservice.service;

import com.finx.allocationreallocationservice.domain.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface AllocationService {

    // Allocation Batch Operations
    AllocationBatchUploadResponseDTO uploadAllocationBatch(MultipartFile file);

    AllocationBatchStatusDTO getAllocationBatchStatus(String batchId);

    byte[] exportFailedAllocationRows(String batchId);

    byte[] exportAllocationBatch(String batchId);

    List<AllocationBatchDTO> getAllBatches(String status, LocalDate startDate, LocalDate endDate, int page, int size);

    // Allocation Summary
    AllocationSummaryDTO getAllocationSummary();

    AllocationSummaryByDateDTO getAllocationSummaryByDate(LocalDate date);

    // Allocation Rules
    List<AllocationRuleDTO> getAllAllocationRules();

    AllocationRuleDTO getAllocationRule(Long ruleId);

    AllocationRuleDTO createAllocationRule(AllocationRuleDTO ruleDTO);

    AllocationRuleDTO updateAllocationRule(Long ruleId, AllocationRuleDTO ruleDTO);

    void deleteAllocationRule(Long ruleId);

    AllocationRuleSimulationDTO simulateAllocationRule(Long ruleId);

    AllocationRuleExecutionResponseDTO applyAllocationRule(Long ruleId, AllocationRuleExecutionRequestDTO request);

    // Case Allocation
    CaseAllocationDTO getCaseAllocation(Long caseId);

    AllocationHistoryDTO getCaseAllocationHistory(Long caseId);

    void deallocateCase(Long caseId, String reason);

    BulkDeallocationResponseDTO bulkDeallocate(BulkDeallocationRequestDTO request);

    // Agent Workload
    List<AgentWorkloadDTO> getAgentWorkload(List<Long> agentIds, List<String> geographies);

    // Allocated Cases
    List<CaseAllocationDTO> getAllAllocatedCases(Long agentId, String geography, int page, int size);

    // Contact Update Operations
    AllocationBatchUploadResponseDTO uploadContactUpdateBatch(MultipartFile file);

    ContactUpdateBatchStatusDTO getContactUpdateBatchStatus(String batchId);

    byte[] exportFailedContactUpdateRows(String batchId);

    // Error Management
    List<ErrorDTO> getAllErrors();

    ErrorDTO getErrorDetails(String errorId);

    // Audit Trail
    List<AuditLogDTO> getAllocationAudit();

    List<AuditLogDTO> getAllocationAuditForCase(Long caseId);

    // Allocation History - for external services to record allocation actions
    void saveAllocationHistory(CreateAllocationHistoryRequest request);

    void saveAllocationHistoryBatch(List<CreateAllocationHistoryRequest> requests);
}
