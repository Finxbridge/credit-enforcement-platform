package com.finx.collectionsservice.controller;

import com.finx.collectionsservice.domain.dto.*;
import com.finx.collectionsservice.domain.enums.ApprovalStatus;
import com.finx.collectionsservice.domain.enums.ApprovalType;
import com.finx.collectionsservice.service.ApprovalService;
import com.finx.collectionsservice.util.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/collections/approvals")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Approvals", description = "APIs for managing maker-checker approval workflow")
public class ApprovalController {

    private final ApprovalService approvalService;

    // ==================== MATRIX ENDPOINTS ====================

    @PostMapping("/matrix")
    @Operation(summary = "Create approval matrix", description = "Create a new approval matrix rule")
    public ResponseEntity<CommonResponse<ApprovalMatrixDTO>> createMatrix(
            @Valid @RequestBody CreateApprovalMatrixRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        log.info("Creating approval matrix: {} by user: {}", request.getMatrixCode(), userId);
        ApprovalMatrixDTO created = approvalService.createMatrix(request, userId);
        return ResponseWrapper.created("Approval matrix created successfully", created);
    }

    @GetMapping("/matrix/{id}")
    @Operation(summary = "Get matrix by ID", description = "Get approval matrix by ID")
    public ResponseEntity<CommonResponse<ApprovalMatrixDTO>> getMatrixById(@PathVariable Long id) {
        log.info("Getting approval matrix: {}", id);
        ApprovalMatrixDTO matrix = approvalService.getMatrixById(id);
        return ResponseWrapper.ok("Approval matrix retrieved successfully", matrix);
    }

    @GetMapping("/matrix/code/{matrixCode}")
    @Operation(summary = "Get matrix by code", description = "Get approval matrix by code")
    public ResponseEntity<CommonResponse<ApprovalMatrixDTO>> getMatrixByCode(@PathVariable String matrixCode) {
        log.info("Getting approval matrix by code: {}", matrixCode);
        ApprovalMatrixDTO matrix = approvalService.getMatrixByCode(matrixCode);
        return ResponseWrapper.ok("Approval matrix retrieved successfully", matrix);
    }

    @GetMapping("/matrix/active")
    @Operation(summary = "Get active matrices", description = "Get all active approval matrices")
    public ResponseEntity<CommonResponse<List<ApprovalMatrixDTO>>> getActiveMatrices() {
        log.info("Getting active approval matrices");
        List<ApprovalMatrixDTO> matrices = approvalService.getActiveMatrices();
        return ResponseWrapper.ok("Active approval matrices retrieved successfully", matrices);
    }

    @GetMapping("/matrix/type/{type}")
    @Operation(summary = "Get matrices by type", description = "Get approval matrices by approval type")
    public ResponseEntity<CommonResponse<List<ApprovalMatrixDTO>>> getMatricesByType(
            @PathVariable ApprovalType type) {
        log.info("Getting approval matrices by type: {}", type);
        List<ApprovalMatrixDTO> matrices = approvalService.getMatricesByType(type);
        return ResponseWrapper.ok("Approval matrices retrieved successfully", matrices);
    }

    @GetMapping("/matrix")
    @Operation(summary = "Get all matrices", description = "Get all approval matrices with filters")
    public ResponseEntity<CommonResponse<Page<ApprovalMatrixDTO>>> getAllMatrices(
            @Parameter(description = "Filter by approval type")
            @RequestParam(required = false) ApprovalType type,
            @Parameter(description = "Filter by active status")
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Getting all approval matrices");
        Page<ApprovalMatrixDTO> matrices = approvalService.getAllMatrices(type, isActive, pageable);
        return ResponseWrapper.ok("Approval matrices retrieved successfully", matrices);
    }

    @PutMapping("/matrix/{id}")
    @Operation(summary = "Update matrix", description = "Update an existing approval matrix")
    public ResponseEntity<CommonResponse<ApprovalMatrixDTO>> updateMatrix(
            @PathVariable Long id,
            @Valid @RequestBody CreateApprovalMatrixRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        log.info("Updating approval matrix: {} by user: {}", id, userId);
        ApprovalMatrixDTO updated = approvalService.updateMatrix(id, request, userId);
        return ResponseWrapper.ok("Approval matrix updated successfully", updated);
    }

    @PutMapping("/matrix/{id}/activate")
    @Operation(summary = "Activate matrix", description = "Activate an approval matrix")
    public ResponseEntity<CommonResponse<ApprovalMatrixDTO>> activateMatrix(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        log.info("Activating approval matrix: {} by user: {}", id, userId);
        ApprovalMatrixDTO activated = approvalService.activateMatrix(id, userId);
        return ResponseWrapper.ok("Approval matrix activated successfully", activated);
    }

    @PutMapping("/matrix/{id}/deactivate")
    @Operation(summary = "Deactivate matrix", description = "Deactivate an approval matrix")
    public ResponseEntity<CommonResponse<ApprovalMatrixDTO>> deactivateMatrix(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        log.info("Deactivating approval matrix: {} by user: {}", id, userId);
        ApprovalMatrixDTO deactivated = approvalService.deactivateMatrix(id, userId);
        return ResponseWrapper.ok("Approval matrix deactivated successfully", deactivated);
    }

    @DeleteMapping("/matrix/{id}")
    @Operation(summary = "Delete matrix", description = "Delete an approval matrix")
    public ResponseEntity<CommonResponse<String>> deleteMatrix(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        log.info("Deleting approval matrix: {} by user: {}", id, userId);
        approvalService.deleteMatrix(id, userId);
        return ResponseWrapper.okMessage("Approval matrix deleted successfully");
    }

    // ==================== REQUEST ENDPOINTS ====================

    @PostMapping("/requests")
    @Operation(summary = "Create approval request", description = "Create a new approval request")
    public ResponseEntity<CommonResponse<ApprovalRequestDTO>> createApprovalRequest(
            @Valid @RequestBody CreateApprovalRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        log.info("Creating approval request for type: {} by user: {}", request.getApprovalType(), userId);
        ApprovalRequestDTO created = approvalService.createApprovalRequest(request, userId);
        return ResponseWrapper.created("Approval request created successfully", created);
    }

    @GetMapping("/requests/{id}")
    @Operation(summary = "Get request by ID", description = "Get approval request by ID")
    public ResponseEntity<CommonResponse<ApprovalRequestDTO>> getApprovalRequestById(@PathVariable Long id) {
        log.info("Getting approval request: {}", id);
        ApprovalRequestDTO request = approvalService.getApprovalRequestById(id);
        return ResponseWrapper.ok("Approval request retrieved successfully", request);
    }

    @GetMapping("/requests/number/{requestNumber}")
    @Operation(summary = "Get request by number", description = "Get approval request by request number")
    public ResponseEntity<CommonResponse<ApprovalRequestDTO>> getApprovalRequestByNumber(
            @PathVariable String requestNumber) {
        log.info("Getting approval request by number: {}", requestNumber);
        ApprovalRequestDTO request = approvalService.getApprovalRequestByNumber(requestNumber);
        return ResponseWrapper.ok("Approval request retrieved successfully", request);
    }

    @GetMapping("/requests/case/{caseId}")
    @Operation(summary = "Get requests by case", description = "Get all approval requests for a case")
    public ResponseEntity<CommonResponse<List<ApprovalRequestDTO>>> getApprovalRequestsByCaseId(
            @PathVariable Long caseId) {
        log.info("Getting approval requests for case: {}", caseId);
        List<ApprovalRequestDTO> requests = approvalService.getApprovalRequestsByCaseId(caseId);
        return ResponseWrapper.ok("Approval requests retrieved successfully", requests);
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending approvals", description = "Get pending approvals for current user/role")
    public ResponseEntity<CommonResponse<Page<ApprovalRequestDTO>>> getPendingApprovals(
            @Parameter(description = "Filter by role ID")
            @RequestParam(required = false) Long roleId,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Getting pending approvals for user: {} role: {}", userId, roleId);
        Page<ApprovalRequestDTO> requests;
        if (roleId != null) {
            requests = approvalService.getPendingApprovalsByRole(roleId, pageable);
        } else {
            requests = approvalService.getPendingApprovalsByUser(userId, pageable);
        }
        return ResponseWrapper.ok("Pending approvals retrieved successfully", requests);
    }

    @GetMapping("/my-requests")
    @Operation(summary = "Get my requests", description = "Get approval requests created by current user")
    public ResponseEntity<CommonResponse<Page<ApprovalRequestDTO>>> getMyRequests(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Getting requests for user: {}", userId);
        Page<ApprovalRequestDTO> requests = approvalService.getMyRequests(userId, pageable);
        return ResponseWrapper.ok("Requests retrieved successfully", requests);
    }

    @GetMapping("/requests")
    @Operation(summary = "Search requests", description = "Search approval requests with filters")
    public ResponseEntity<CommonResponse<Page<ApprovalRequestDTO>>> searchApprovalRequests(
            @Parameter(description = "Filter by approval type")
            @RequestParam(required = false) ApprovalType type,
            @Parameter(description = "Filter by status")
            @RequestParam(required = false) ApprovalStatus status,
            @Parameter(description = "Filter by case ID")
            @RequestParam(required = false) Long caseId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Searching approval requests");
        Page<ApprovalRequestDTO> requests = approvalService.searchApprovalRequests(type, status, caseId, pageable);
        return ResponseWrapper.ok("Approval requests retrieved successfully", requests);
    }

    // ==================== ACTION ENDPOINTS ====================

    @PostMapping("/requests/{id}/action")
    @Operation(summary = "Process approval action", description = "Process approval, rejection, or escalation")
    public ResponseEntity<CommonResponse<ApprovalRequestDTO>> processApproval(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalActionRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        log.info("Processing approval action: {} for request: {} by user: {}", request.getAction(), id, userId);
        ApprovalRequestDTO processed = approvalService.processApproval(id, request, userId);
        return ResponseWrapper.ok("Approval action processed successfully", processed);
    }

    @PostMapping("/requests/{id}/approve")
    @Operation(summary = "Approve request", description = "Approve an approval request")
    public ResponseEntity<CommonResponse<ApprovalRequestDTO>> approveRequest(
            @PathVariable Long id,
            @RequestParam(required = false) String remarks,
            @RequestParam(required = false) java.math.BigDecimal approvedAmount,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        log.info("Approving request: {} by user: {}", id, userId);
        ApprovalRequestDTO approved = approvalService.approveRequest(id, remarks, approvedAmount, userId);
        return ResponseWrapper.ok("Request approved successfully", approved);
    }

    @PostMapping("/requests/{id}/reject")
    @Operation(summary = "Reject request", description = "Reject an approval request")
    public ResponseEntity<CommonResponse<ApprovalRequestDTO>> rejectRequest(
            @PathVariable Long id,
            @RequestParam String reason,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        log.info("Rejecting request: {} by user: {}", id, userId);
        ApprovalRequestDTO rejected = approvalService.rejectRequest(id, reason, userId);
        return ResponseWrapper.ok("Request rejected successfully", rejected);
    }

    @PostMapping("/requests/{id}/escalate")
    @Operation(summary = "Escalate request", description = "Escalate an approval request")
    public ResponseEntity<CommonResponse<ApprovalRequestDTO>> escalateRequest(
            @PathVariable Long id,
            @RequestParam String reason,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        log.info("Escalating request: {} by user: {}", id, userId);
        ApprovalRequestDTO escalated = approvalService.escalateRequest(id, reason, userId);
        return ResponseWrapper.ok("Request escalated successfully", escalated);
    }

    @GetMapping("/requests/{id}/history")
    @Operation(summary = "Get approval history", description = "Get approval action history for a request")
    public ResponseEntity<CommonResponse<List<ApprovalHistoryDTO>>> getApprovalHistory(@PathVariable Long id) {
        log.info("Getting approval history for request: {}", id);
        List<ApprovalHistoryDTO> history = approvalService.getApprovalHistory(id);
        return ResponseWrapper.ok("Approval history retrieved successfully", history);
    }

    // ==================== COUNTS ====================

    @GetMapping("/counts")
    @Operation(summary = "Get pending counts", description = "Get count of pending approvals")
    public ResponseEntity<CommonResponse<Map<String, Long>>> getPendingCounts(
            @RequestParam(required = false) Long roleId,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        log.info("Getting pending counts for user: {} role: {}", userId, roleId);
        Long byRole = roleId != null ? approvalService.countPendingByRole(roleId) : 0L;
        Long byUser = approvalService.countPendingByUser(userId);
        return ResponseWrapper.ok("Pending counts retrieved successfully",
                Map.of("pendingByRole", byRole, "pendingByUser", byUser));
    }
}
