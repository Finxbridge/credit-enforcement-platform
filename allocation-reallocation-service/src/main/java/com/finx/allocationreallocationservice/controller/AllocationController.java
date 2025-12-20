package com.finx.allocationreallocationservice.controller;

import com.finx.allocationreallocationservice.domain.dto.*;
import com.finx.allocationreallocationservice.service.AllocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.bind.annotation.RequestPart;

@Slf4j
@RestController
@RequestMapping("/allocations")
@RequiredArgsConstructor
@Tag(name = "Allocation Management", description = "APIs for case allocation management")
public class AllocationController {

    private final AllocationService allocationService;
    private final com.finx.allocationreallocationservice.util.csv.CsvTemplateGenerator csvTemplateGenerator;

    @GetMapping("/upload/template")
    @Operation(summary = "Download allocation CSV template",
               description = "Download CSV template for bulk case allocation with optional sample data")
    public ResponseEntity<byte[]> downloadAllocationTemplate(
            @RequestParam(defaultValue = "false") boolean includeSample) {
        log.info("Downloading allocation template (includeSample: {})", includeSample);

        byte[] csvData = csvTemplateGenerator.generateAllocationTemplate(includeSample);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "allocation_template.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
    }

    @PostMapping("/upload")
    @Operation(summary = "Upload allocation CSV for bulk assignment")
    public ResponseEntity<CommonResponse<AllocationBatchUploadResponseDTO>> uploadAllocationBatch(
            @RequestPart("file") MultipartFile file) {
        log.info("Received allocation batch upload request");

        AllocationBatchUploadResponseDTO response = allocationService.uploadAllocationBatch(file);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(CommonResponse.success("Bulk allocation initiated.", response));
    }

    @GetMapping("/{batchId}/status")
    @Operation(summary = "Get validation + allocation summary")
    public ResponseEntity<CommonResponse<AllocationBatchStatusDTO>> getAllocationBatchStatus(
            @PathVariable String batchId) {
        log.info("Fetching allocation batch status for: {}", batchId);

        AllocationBatchStatusDTO status = allocationService.getAllocationBatchStatus(batchId);

        return ResponseEntity.ok(CommonResponse.success(
                "Allocation batch status retrieved successfully.", status));
    }

    @GetMapping("/{batchId}/errors")
    @Operation(summary = "Export failed allocation rows")
    public ResponseEntity<byte[]> exportFailedAllocationRows(@PathVariable String batchId) {
        log.info("Exporting failed allocation rows for batch: {}", batchId);

        byte[] csvData = allocationService.exportFailedAllocationRows(batchId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment",
                "failed_allocations_" + batchId + ".csv");

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
    }

    @GetMapping("/{batchId}/export")
    @Operation(summary = "Export all allocations for a batch",
               description = "Download CSV with all successfully allocated cases including primary and secondary agent IDs")
    public ResponseEntity<byte[]> exportAllocationBatch(@PathVariable String batchId) {
        log.info("Exporting all allocations for batch: {}", batchId);

        byte[] csvData = allocationService.exportAllocationBatch(batchId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment",
                "allocations_" + batchId + ".csv");

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
    }

    @GetMapping("/summary")
    @Operation(summary = "Get overall allocation stats")
    public ResponseEntity<CommonResponse<AllocationSummaryDTO>> getAllocationSummary() {
        log.info("Fetching overall allocation summary");

        AllocationSummaryDTO summary = allocationService.getAllocationSummary();

        return ResponseEntity.ok(CommonResponse.success(
                "Overall allocation statistics retrieved successfully.", summary));
    }

    @GetMapping("/summary/{date}")
    @Operation(summary = "Fetch summary by date")
    public ResponseEntity<CommonResponse<AllocationSummaryByDateDTO>> getAllocationSummaryByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Fetching allocation summary for date: {}", date);

        AllocationSummaryByDateDTO summary = allocationService.getAllocationSummaryByDate(date);

        return ResponseEntity.ok(CommonResponse.success(
                "Allocation summary for date retrieved successfully.", summary));
    }

    @GetMapping("/allocation-rules")
    @Operation(summary = "Get all allocation rules")
    public ResponseEntity<CommonResponse<List<AllocationRuleDTO>>> getAllAllocationRules() {
        log.info("Fetching all allocation rules");

        List<AllocationRuleDTO> rules = allocationService.getAllAllocationRules();

        return ResponseEntity.ok(CommonResponse.success(
                "Allocation rules retrieved successfully.", rules));
    }

    @GetMapping("/allocation-rules/{ruleId}")
    @Operation(summary = "Get a specific allocation rule")
    public ResponseEntity<CommonResponse<AllocationRuleDTO>> getAllocationRule(@PathVariable Long ruleId) {
        log.info("Fetching allocation rule: {}", ruleId);
        AllocationRuleDTO rule = allocationService.getAllocationRule(ruleId);
        return ResponseEntity.ok(CommonResponse.success("Allocation rule retrieved successfully.", rule));
    }

    @PostMapping("/allocation-rules")
    @Operation(summary = "Create a new allocation rule")
    public ResponseEntity<CommonResponse<AllocationRuleDTO>> createAllocationRule(
            @Valid @RequestBody AllocationRuleDTO ruleDTO) {
        log.info("Creating new allocation rule: {}", ruleDTO.getName());

        AllocationRuleDTO createdRule = allocationService.createAllocationRule(ruleDTO);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success("Allocation rule created successfully.", createdRule));
    }

    @PutMapping("/allocation-rules/{ruleId}")
    @Operation(summary = "Update existing allocation rule")
    public ResponseEntity<CommonResponse<AllocationRuleDTO>> updateAllocationRule(
            @PathVariable Long ruleId,
            @Valid @RequestBody AllocationRuleDTO ruleDTO) {
        log.info("Updating allocation rule: {}", ruleId);

        AllocationRuleDTO updatedRule = allocationService.updateAllocationRule(ruleId, ruleDTO);

        return ResponseEntity.ok(CommonResponse.success(
                "Allocation rule updated successfully.", updatedRule));
    }

    @DeleteMapping("/allocation-rules/{ruleId}")
    @Operation(summary = "Delete allocation rule")
    public ResponseEntity<CommonResponse<Void>> deleteAllocationRule(@PathVariable Long ruleId) {
        log.info("Deleting allocation rule: {}", ruleId);

        allocationService.deleteAllocationRule(ruleId);

        return ResponseEntity.ok(CommonResponse.successMessage("Allocation rule deleted successfully."));
    }

    @PostMapping("/allocation-rules/{ruleId}/simulate")
    @Operation(summary = "Simulate rule before applying")
    public ResponseEntity<CommonResponse<AllocationRuleSimulationDTO>> simulateAllocationRule(
            @PathVariable Long ruleId) {
        log.info("Simulating allocation rule: {}", ruleId);

        AllocationRuleSimulationDTO simulation = allocationService.simulateAllocationRule(ruleId);

        return ResponseEntity.ok(CommonResponse.success(
                "Allocation rule simulation completed successfully.", simulation));
    }

    @GetMapping("/cases/{caseId}/allocation")
    @Operation(summary = "View current allocation details")
    public ResponseEntity<CommonResponse<CaseAllocationDTO>> getCaseAllocation(@PathVariable Long caseId) {
        log.info("Fetching case allocation for case: {}", caseId);

        CaseAllocationDTO allocation = allocationService.getCaseAllocation(caseId);

        return ResponseEntity.ok(CommonResponse.success(
                "Case allocation details retrieved successfully.", allocation));
    }

    @GetMapping("/cases/{caseId}/allocation-history")
    @Operation(summary = "View allocation/reallocation history for a case")
    public ResponseEntity<CommonResponse<AllocationHistoryDTO>> getCaseAllocationHistory(
            @PathVariable Long caseId) {
        log.info("Fetching allocation history for case: {}", caseId);

        AllocationHistoryDTO history = allocationService.getCaseAllocationHistory(caseId);

        return ResponseEntity.ok(CommonResponse.success(
                "Case allocation history retrieved successfully.", history));
    }

    @GetMapping("/contacts/upload/template")
    @Operation(summary = "Download contact update CSV template",
               description = "Download CSV template for bulk contact updates. Specify updateType: MOBILE_UPDATE, EMAIL_UPDATE, or ADDRESS_UPDATE")
    public ResponseEntity<byte[]> downloadContactUpdateTemplate(
            @RequestParam(defaultValue = "false") boolean includeSample,
            @RequestParam(required = false) String updateType) {
        log.info("Downloading contact update template (type: {}, includeSample: {})", updateType, includeSample);

        byte[] csvData = csvTemplateGenerator.generateContactUpdateTemplate(includeSample, updateType);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        String filename = updateType != null ?
                "contact_update_" + updateType.toLowerCase() + "_template.csv" :
                "contact_update_template.csv";
        headers.setContentDispositionFormData("attachment", filename);

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
    }

    @PostMapping("/contacts/upload")
    @Operation(summary = "Upload CSV for bulk contact info update")
    public ResponseEntity<CommonResponse<AllocationBatchUploadResponseDTO>> uploadContactUpdateBatch(
            @RequestPart("file") MultipartFile file) {
        log.info("Received contact update batch upload request");

        AllocationBatchUploadResponseDTO response = allocationService.uploadContactUpdateBatch(file);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(CommonResponse.success("Bulk contact update initiated.", response));
    }

    @GetMapping("/contacts/{batchId}/status")
    @Operation(summary = "Get validation status of contact update batch")
    public ResponseEntity<CommonResponse<ContactUpdateBatchStatusDTO>> getContactUpdateBatchStatus(
            @PathVariable String batchId) {
        log.info("Fetching contact update batch status for: {}", batchId);

        ContactUpdateBatchStatusDTO status = allocationService.getContactUpdateBatchStatus(batchId);

        return ResponseEntity.ok(CommonResponse.success(
                "Contact update batch status retrieved successfully.", status));
    }

    @GetMapping("/contacts/{batchId}/errors")
    @Operation(summary = "Export failed contact update rows")
    public ResponseEntity<byte[]> exportFailedContactUpdateRows(@PathVariable String batchId) {
        log.info("Exporting failed contact update rows for batch: {}", batchId);

        byte[] csvData = allocationService.exportFailedContactUpdateRows(batchId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment",
                "failed_contacts_" + batchId + ".csv");

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
    }

    @GetMapping("/errors")
    @Operation(summary = "View all errors across allocations/reallocations/contact updates")
    public ResponseEntity<CommonResponse<List<ErrorDTO>>> getAllErrors() {
        log.info("Fetching all allocation errors");

        List<ErrorDTO> errors = allocationService.getAllErrors();

        return ResponseEntity.ok(CommonResponse.success("Errors retrieved successfully.", errors));
    }

    @GetMapping("/errors/{errorId}")
    @Operation(summary = "Get detailed error information")
    public ResponseEntity<CommonResponse<ErrorDTO>> getErrorDetails(@PathVariable String errorId) {
        log.info("Fetching error details for: {}", errorId);

        ErrorDTO error = allocationService.getErrorDetails(errorId);

        return ResponseEntity.ok(CommonResponse.success(
                "Detailed error information retrieved successfully.", error));
    }

    @GetMapping("/audit")
    @Operation(summary = "Fetch audit trail of allocation and reallocation activities")
    public ResponseEntity<CommonResponse<List<AuditLogDTO>>> getAllocationAudit() {
        log.info("Fetching allocation audit trail");

        List<AuditLogDTO> auditLogs = allocationService.getAllocationAudit();

        return ResponseEntity.ok(CommonResponse.success(
                "Audit trail retrieved successfully.", auditLogs));
    }

    @GetMapping("/audit/{caseId}")
    @Operation(summary = "Fetch audit log for a specific case")
    public ResponseEntity<CommonResponse<List<AuditLogDTO>>> getAllocationAuditForCase(
            @PathVariable Long caseId) {
        log.info("Fetching allocation audit trail for case: {}", caseId);

        List<AuditLogDTO> auditLogs = allocationService.getAllocationAuditForCase(caseId);

        return ResponseEntity.ok(CommonResponse.success(
                "Audit trail retrieved successfully.", auditLogs));
    }

    @GetMapping("/batches")
    @Operation(summary = "List/Search all batches")
    public ResponseEntity<CommonResponse<List<AllocationBatchDTO>>> getAllBatches(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Fetching all allocation batches with status: {}, dates: {} to {}, page: {}, size: {}",
                status, startDate, endDate, page, size);

        List<AllocationBatchDTO> batches = allocationService.getAllBatches(status, startDate, endDate, page, size);

        return ResponseEntity.ok(CommonResponse.success(
                "Allocation batches retrieved successfully.", batches));
    }

    @PostMapping("/allocation-rules/{ruleId}/apply")
    @Operation(summary = "Apply allocation rule to actual cases")
    public ResponseEntity<CommonResponse<AllocationRuleExecutionResponseDTO>> applyAllocationRule(
            @PathVariable Long ruleId,
            @Valid @RequestBody AllocationRuleExecutionRequestDTO request) {
        log.info("Applying allocation rule: {}", ruleId);

        AllocationRuleExecutionResponseDTO response = allocationService.applyAllocationRule(ruleId, request);

        return ResponseEntity.ok(CommonResponse.success(
                "Allocation rule applied successfully.", response));
    }

    @DeleteMapping("/cases/{caseId}")
    @Operation(summary = "Deallocate a specific case")
    public ResponseEntity<CommonResponse<String>> deallocateCase(
            @PathVariable Long caseId,
            @RequestParam String reason) {
        log.info("Deallocating case: {} with reason: {}", caseId, reason);

        allocationService.deallocateCase(caseId, reason);

        return ResponseEntity.ok(CommonResponse.success(
                "Case deallocated successfully.", "Case " + caseId + " has been deallocated"));
    }

    @PostMapping("/deallocate/bulk")
    @Operation(summary = "Bulk deallocate multiple cases")
    public ResponseEntity<CommonResponse<BulkDeallocationResponseDTO>> bulkDeallocate(
            @Valid @RequestBody BulkDeallocationRequestDTO request) {
        log.info("Bulk deallocating {} cases", request.getCaseIds().size());

        BulkDeallocationResponseDTO response = allocationService.bulkDeallocate(request);

        return ResponseEntity.ok(CommonResponse.success(
                "Bulk deallocation completed.", response));
    }

    @GetMapping("/agents/workload")
    @Operation(summary = "Get agent workload dashboard")
    public ResponseEntity<CommonResponse<List<AgentWorkloadDTO>>> getAgentWorkload(
            @RequestParam(required = false) List<Long> agentIds,
            @RequestParam(required = false) List<String> geographies) {
        log.info("Fetching agent workload for agentIds: {} and geographies: {}", agentIds, geographies);

        List<AgentWorkloadDTO> workload = allocationService.getAgentWorkload(agentIds, geographies);

        return ResponseEntity.ok(CommonResponse.success(
                "Agent workload retrieved successfully.", workload));
    }

    @GetMapping("/cases/allocated")
    @Operation(summary = "Get all allocated cases with assigned agents",
               description = "Returns list of all currently allocated cases with their assigned agent details. Supports filtering by agent ID and pagination.")
    public ResponseEntity<CommonResponse<List<CaseAllocationDTO>>> getAllAllocatedCases(
            @RequestParam(required = false) Long agentId,
            @RequestParam(required = false) String geography,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        log.info("Fetching all allocated cases - agentId: {}, geography: {}, page: {}, size: {}",
                agentId, geography, page, size);

        List<CaseAllocationDTO> allocations = allocationService.getAllAllocatedCases(agentId, geography, page, size);

        return ResponseEntity.ok(CommonResponse.success(
                "Allocated cases retrieved successfully.", allocations));
    }
}
