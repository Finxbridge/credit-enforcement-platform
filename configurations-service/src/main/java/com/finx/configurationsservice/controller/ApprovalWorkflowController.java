package com.finx.configurationsservice.controller;

import com.finx.configurationsservice.domain.dto.ApprovalWorkflowDTO;
import com.finx.configurationsservice.domain.dto.CommonResponse;
import com.finx.configurationsservice.domain.dto.CreateApprovalWorkflowRequest;
import com.finx.configurationsservice.domain.enums.WorkflowType;
import com.finx.configurationsservice.service.ApprovalWorkflowService;
import com.finx.configurationsservice.util.ResponseWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/config/approval-workflows")
@RequiredArgsConstructor
public class ApprovalWorkflowController {

    private final ApprovalWorkflowService workflowService;

    @PostMapping
    public ResponseEntity<CommonResponse<ApprovalWorkflowDTO>> createWorkflow(
            @Valid @RequestBody CreateApprovalWorkflowRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        log.info("POST /approval-workflows - Creating workflow: {}", request.getWorkflowCode());
        ApprovalWorkflowDTO response = workflowService.createWorkflow(request, userId);
        return ResponseWrapper.created("Approval workflow created successfully", response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<ApprovalWorkflowDTO>> getWorkflowById(@PathVariable Long id) {
        log.info("GET /approval-workflows/{} - Fetching workflow", id);
        ApprovalWorkflowDTO response = workflowService.getWorkflowById(id);
        return ResponseWrapper.ok("Approval workflow retrieved successfully", response);
    }

    @GetMapping("/code/{workflowCode}")
    public ResponseEntity<CommonResponse<ApprovalWorkflowDTO>> getWorkflowByCode(@PathVariable String workflowCode) {
        log.info("GET /approval-workflows/code/{} - Fetching workflow", workflowCode);
        ApprovalWorkflowDTO response = workflowService.getWorkflowByCode(workflowCode);
        return ResponseWrapper.ok("Approval workflow retrieved successfully", response);
    }

    @GetMapping("/type/{type}/active")
    public ResponseEntity<CommonResponse<ApprovalWorkflowDTO>> getActiveWorkflowByType(@PathVariable WorkflowType type) {
        log.info("GET /approval-workflows/type/{}/active - Fetching active workflow", type);
        ApprovalWorkflowDTO response = workflowService.getActiveWorkflowByType(type);
        return ResponseWrapper.ok("Active approval workflow retrieved successfully", response);
    }

    @GetMapping("/active")
    public ResponseEntity<CommonResponse<List<ApprovalWorkflowDTO>>> getActiveWorkflows() {
        log.info("GET /approval-workflows/active - Fetching active workflows");
        List<ApprovalWorkflowDTO> workflows = workflowService.getActiveWorkflows();
        return ResponseWrapper.ok("Active approval workflows retrieved successfully", workflows);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<CommonResponse<List<ApprovalWorkflowDTO>>> getWorkflowsByType(@PathVariable WorkflowType type) {
        log.info("GET /approval-workflows/type/{} - Fetching workflows", type);
        List<ApprovalWorkflowDTO> workflows = workflowService.getWorkflowsByType(type);
        return ResponseWrapper.ok("Approval workflows retrieved successfully", workflows);
    }

    @GetMapping
    public ResponseEntity<CommonResponse<Page<ApprovalWorkflowDTO>>> getAllWorkflows(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /approval-workflows - Fetching all workflows");
        Page<ApprovalWorkflowDTO> workflows = workflowService.getAllWorkflows(pageable);
        return ResponseWrapper.ok("Approval workflows retrieved successfully", workflows);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<ApprovalWorkflowDTO>> updateWorkflow(
            @PathVariable Long id,
            @Valid @RequestBody CreateApprovalWorkflowRequest request) {
        log.info("PUT /approval-workflows/{} - Updating workflow", id);
        ApprovalWorkflowDTO response = workflowService.updateWorkflow(id, request);
        return ResponseWrapper.ok("Approval workflow updated successfully", response);
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<CommonResponse<ApprovalWorkflowDTO>> activateWorkflow(@PathVariable Long id) {
        log.info("POST /approval-workflows/{}/activate - Activating workflow", id);
        ApprovalWorkflowDTO response = workflowService.activateWorkflow(id);
        return ResponseWrapper.ok("Approval workflow activated successfully", response);
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<CommonResponse<ApprovalWorkflowDTO>> deactivateWorkflow(@PathVariable Long id) {
        log.info("POST /approval-workflows/{}/deactivate - Deactivating workflow", id);
        ApprovalWorkflowDTO response = workflowService.deactivateWorkflow(id);
        return ResponseWrapper.ok("Approval workflow deactivated successfully", response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteWorkflow(@PathVariable Long id) {
        log.info("DELETE /approval-workflows/{} - Deleting workflow", id);
        workflowService.deleteWorkflow(id);
        return ResponseWrapper.ok("Approval workflow deleted successfully", null);
    }

    @GetMapping("/{id}/levels/count")
    public ResponseEntity<CommonResponse<Integer>> getApprovalLevelCount(@PathVariable Long id) {
        log.info("GET /approval-workflows/{}/levels/count - Getting level count", id);
        int count = workflowService.getApprovalLevelCount(id);
        return ResponseWrapper.ok("Approval level count retrieved", count);
    }
}
