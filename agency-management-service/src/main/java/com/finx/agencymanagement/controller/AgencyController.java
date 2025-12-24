package com.finx.agencymanagement.controller;

import com.finx.agencymanagement.domain.dto.*;
import com.finx.agencymanagement.domain.enums.AgencyStatus;
import com.finx.agencymanagement.domain.enums.AgencyType;
import com.finx.agencymanagement.service.AgencyService;
import com.finx.agencymanagement.util.ResponseWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Agency Management operations.
 * Handles all agency-related endpoints including:
 * - Agency CRUD operations
 * - Agency approval workflow (maker-checker)
 * - Case allocation to agencies
 * - Get active agents for assignment
 * - Dashboard and reporting
 *
 * Note: Agency user/agent creation is handled by access-management-service.
 * This service uses shared users table for agents.
 */
@Slf4j
@RestController
@RequestMapping("/agency")
@RequiredArgsConstructor
public class AgencyController {

    private final AgencyService agencyService;

    // ==========================================
    // AGENCY MASTER / ONBOARDING ENDPOINTS
    // ==========================================

    /**
     * Create a new agency (onboarding)
     */
    @PostMapping
    public ResponseEntity<CommonResponse<AgencyDTO>> createAgency(
            @Valid @RequestBody CreateAgencyRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Creating new agency: {}", request.getAgencyName());
        AgencyDTO agency = agencyService.createAgency(request, userId);
        return ResponseWrapper.created("Agency created successfully and pending approval", agency);
    }

    /**
     * Get agency by ID
     */
    @GetMapping("/{agencyId}")
    public ResponseEntity<CommonResponse<AgencyDTO>> getAgencyById(
            @PathVariable Long agencyId) {
        log.info("Fetching agency with ID: {}", agencyId);
        AgencyDTO agency = agencyService.getAgencyById(agencyId);
        return ResponseWrapper.ok("Agency retrieved successfully", agency);
    }

    /**
     * Get agency by code
     */
    @GetMapping("/code/{agencyCode}")
    public ResponseEntity<CommonResponse<AgencyDTO>> getAgencyByCode(
            @PathVariable String agencyCode) {
        log.info("Fetching agency with code: {}", agencyCode);
        AgencyDTO agency = agencyService.getAgencyByCode(agencyCode);
        return ResponseWrapper.ok("Agency retrieved successfully", agency);
    }

    /**
     * Update agency details
     */
    @PutMapping("/{agencyId}")
    public ResponseEntity<CommonResponse<AgencyDTO>> updateAgency(
            @PathVariable Long agencyId,
            @Valid @RequestBody CreateAgencyRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Updating agency with ID: {}", agencyId);
        AgencyDTO agency = agencyService.updateAgency(agencyId, request, userId);
        return ResponseWrapper.ok("Agency updated successfully", agency);
    }

    /**
     * Delete agency (soft delete)
     */
    @DeleteMapping("/{agencyId}")
    public ResponseEntity<CommonResponse<String>> deleteAgency(
            @PathVariable Long agencyId,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Deleting agency with ID: {}", agencyId);
        agencyService.deleteAgency(agencyId, userId);
        return ResponseWrapper.okMessage("Agency deleted successfully");
    }

    /**
     * Get all agencies with pagination
     */
    @GetMapping
    public ResponseEntity<CommonResponse<Page<AgencyDTO>>> getAllAgencies(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Fetching all agencies with pagination");
        Page<AgencyDTO> agencies = agencyService.getAllAgencies(pageable);
        return ResponseWrapper.ok("Agencies retrieved successfully", agencies);
    }

    /**
     * Get agencies by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<CommonResponse<Page<AgencyDTO>>> getAgenciesByStatus(
            @PathVariable AgencyStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Fetching agencies with status: {}", status);
        Page<AgencyDTO> agencies = agencyService.getAgenciesByStatus(status, pageable);
        return ResponseWrapper.ok("Agencies retrieved successfully", agencies);
    }

    /**
     * Get agencies by type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<CommonResponse<Page<AgencyDTO>>> getAgenciesByType(
            @PathVariable AgencyType type,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Fetching agencies with type: {}", type);
        Page<AgencyDTO> agencies = agencyService.getAgenciesByType(type, pageable);
        return ResponseWrapper.ok("Agencies retrieved successfully", agencies);
    }

    /**
     * Search agencies
     */
    @GetMapping("/search")
    public ResponseEntity<CommonResponse<Page<AgencyDTO>>> searchAgencies(
            @RequestParam String term,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Searching agencies with term: {}", term);
        Page<AgencyDTO> agencies = agencyService.searchAgencies(term, pageable);
        return ResponseWrapper.ok("Agencies retrieved successfully", agencies);
    }

    // ==========================================
    // AGENCY APPROVAL WORKFLOW ENDPOINTS
    // ==========================================

    /**
     * Get agencies pending approval
     */
    @GetMapping("/pending-approval")
    public ResponseEntity<CommonResponse<List<AgencyDTO>>> getPendingApprovalAgencies() {
        log.info("Fetching agencies pending approval");
        List<AgencyDTO> agencies = agencyService.getAgenciesPendingApproval();
        return ResponseWrapper.ok("Pending approval agencies retrieved successfully", agencies);
    }

    /**
     * Approve an agency
     */
    @PostMapping("/{agencyId}/approve")
    public ResponseEntity<CommonResponse<AgencyDTO>> approveAgency(
            @PathVariable Long agencyId,
            @RequestParam(required = false) String notes,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Approving agency with ID: {}", agencyId);
        AgencyDTO agency = agencyService.approveAgency(agencyId, notes, userId);
        return ResponseWrapper.ok("Agency approved successfully", agency);
    }

    /**
     * Reject an agency
     */
    @PostMapping("/{agencyId}/reject")
    public ResponseEntity<CommonResponse<AgencyDTO>> rejectAgency(
            @PathVariable Long agencyId,
            @RequestParam String reason,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Rejecting agency with ID: {}", agencyId);
        AgencyDTO agency = agencyService.rejectAgency(agencyId, reason, userId);
        return ResponseWrapper.ok("Agency rejected", agency);
    }

    /**
     * Process approval (approve or reject)
     */
    @PostMapping("/process-approval")
    public ResponseEntity<CommonResponse<AgencyDTO>> processApproval(
            @Valid @RequestBody AgencyApprovalRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Processing approval for agency: {}", request.getAgencyId());
        AgencyDTO agency = agencyService.processApproval(request, userId);
        return ResponseWrapper.ok("Approval processed successfully", agency);
    }

    // ==========================================
    // AGENCY STATUS MANAGEMENT ENDPOINTS
    // ==========================================

    /**
     * Activate an agency
     */
    @PostMapping("/{agencyId}/activate")
    public ResponseEntity<CommonResponse<AgencyDTO>> activateAgency(
            @PathVariable Long agencyId,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Activating agency with ID: {}", agencyId);
        AgencyDTO agency = agencyService.activateAgency(agencyId, userId);
        return ResponseWrapper.ok("Agency activated successfully", agency);
    }

    /**
     * Deactivate an agency
     */
    @PostMapping("/{agencyId}/deactivate")
    public ResponseEntity<CommonResponse<AgencyDTO>> deactivateAgency(
            @PathVariable Long agencyId,
            @RequestParam(required = false) String reason,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Deactivating agency with ID: {}", agencyId);
        AgencyDTO agency = agencyService.deactivateAgency(agencyId, reason, userId);
        return ResponseWrapper.ok("Agency deactivated successfully", agency);
    }

    /**
     * Suspend an agency
     */
    @PostMapping("/{agencyId}/suspend")
    public ResponseEntity<CommonResponse<AgencyDTO>> suspendAgency(
            @PathVariable Long agencyId,
            @RequestParam(required = false) String reason,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Suspending agency with ID: {}", agencyId);
        AgencyDTO agency = agencyService.suspendAgency(agencyId, reason, userId);
        return ResponseWrapper.ok("Agency suspended successfully", agency);
    }

    // ==========================================
    // ACTIVE AGENTS ENDPOINT
    // ==========================================

    /**
     * Get all active agents from users table
     * Used for dropdown when assigning cases to agents within an agency
     */
    @GetMapping("/agents/active")
    public ResponseEntity<CommonResponse<List<AgentDTO>>> getActiveAgents() {
        log.info("Fetching all active agents");
        List<AgentDTO> agents = agencyService.getActiveAgents();
        return ResponseWrapper.ok("Active agents retrieved successfully", agents);
    }

    /**
     * Get active agents by agency - agents who have cases allocated to specific agency
     */
    @GetMapping("/{agencyId}/agents")
    public ResponseEntity<CommonResponse<List<AgentDTO>>> getAgencyAgents(
            @PathVariable Long agencyId) {
        log.info("Fetching agents for agency ID: {}", agencyId);
        List<AgentDTO> agents = agencyService.getAgentsByAgency(agencyId);
        return ResponseWrapper.ok("Agency agents retrieved successfully", agents);
    }

    // ==========================================
    // CASE ALLOCATION ENDPOINTS
    // ==========================================

    /**
     * Allocate cases to agency
     * When cases are allocated to agency, they are removed from previous agent allocation
     */
    @PostMapping("/allocate-cases")
    public ResponseEntity<CommonResponse<String>> allocateCasesToAgency(
            @Valid @RequestBody AgencyCaseAllocationRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Allocating {} cases to agency ID: {}", request.getCaseIds().size(), request.getAgencyId());
        agencyService.allocateCasesToAgency(request, userId);
        return ResponseWrapper.okMessage("Cases allocated to agency successfully");
    }

    /**
     * Assign cases from agency to agent
     * Second level allocation - from agency to specific agent
     */
    @PostMapping("/{agencyId}/assign-to-agent")
    public ResponseEntity<CommonResponse<String>> assignCasesToAgent(
            @PathVariable Long agencyId,
            @Valid @RequestBody AgentCaseAssignmentRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Assigning {} cases from agency {} to agent {}",
                request.getCaseIds().size(), agencyId, request.getAgentId());
        agencyService.assignCasesToAgent(agencyId, request, userId);
        return ResponseWrapper.okMessage("Cases assigned to agent successfully");
    }

    /**
     * Deallocate cases from agency
     */
    @PostMapping("/{agencyId}/deallocate-cases")
    public ResponseEntity<CommonResponse<String>> deallocateCasesFromAgency(
            @PathVariable Long agencyId,
            @RequestBody List<Long> caseIds,
            @RequestParam(required = false) String reason,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Deallocating {} cases from agency ID: {}", caseIds.size(), agencyId);
        agencyService.deallocateCasesFromAgency(agencyId, caseIds, reason, userId);
        return ResponseWrapper.okMessage("Cases deallocated from agency successfully");
    }

    /**
     * Get case allocations for agency
     */
    @GetMapping("/{agencyId}/case-allocations")
    public ResponseEntity<CommonResponse<Page<AgencyCaseAllocationDTO>>> getAgencyCaseAllocations(
            @PathVariable Long agencyId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Fetching case allocations for agency ID: {}", agencyId);
        Page<AgencyCaseAllocationDTO> allocations = agencyService.getAgencyCaseAllocations(agencyId, pageable);
        return ResponseWrapper.ok("Case allocations retrieved successfully", allocations);
    }

    /**
     * Get cases allocated to agency but not yet assigned to any agent
     */
    @GetMapping("/{agencyId}/unassigned-cases")
    public ResponseEntity<CommonResponse<Page<AgencyCaseAllocationDTO>>> getUnassignedCases(
            @PathVariable Long agencyId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Fetching unassigned cases for agency ID: {}", agencyId);
        Page<AgencyCaseAllocationDTO> allocations = agencyService.getUnassignedCases(agencyId, pageable);
        return ResponseWrapper.ok("Unassigned cases retrieved successfully", allocations);
    }

    /**
     * Get ALL cases allocated to agencies but not yet assigned to any agent
     * This returns unassigned cases across ALL agencies for the Case Assignment tab
     */
    @GetMapping("/cases/unassigned-to-agent")
    public ResponseEntity<CommonResponse<Page<AgencyCaseAllocationDTO>>> getAllUnassignedCases(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Fetching all unassigned cases across all agencies");
        Page<AgencyCaseAllocationDTO> allocations = agencyService.getAllUnassignedCases(pageable);
        return ResponseWrapper.ok("Unassigned cases retrieved successfully", allocations);
    }

    /**
     * Get cases from allocation-service that are NOT yet allocated to ANY agency
     * These are cases available for agency allocation
     */
    @GetMapping("/cases/unallocated-to-agency")
    public ResponseEntity<CommonResponse<Page<AgencyCaseAllocationDTO>>> getCasesNotAllocatedToAgency(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Fetching cases not allocated to any agency");
        Page<AgencyCaseAllocationDTO> allocations = agencyService.getCasesNotAllocatedToAgency(pageable);
        return ResponseWrapper.ok("Unallocated cases retrieved successfully", allocations);
    }

    /**
     * Get ALL allocated cases with their assignment status
     * Shows cases that are: UNALLOCATED, ALLOCATED_TO_AGENCY, or ASSIGNED_TO_AGENT
     * Also shows all assignments for each case (can be assigned to multiple agencies/agents)
     */
    @GetMapping("/cases/all-with-status")
    public ResponseEntity<CommonResponse<Page<AgencyCaseAllocationDTO>>> getAllAllocatedCasesWithStatus(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Fetching all allocated cases with assignment status");
        Page<AgencyCaseAllocationDTO> allocations = agencyService.getAllAllocatedCasesWithStatus(pageable);
        return ResponseWrapper.ok("Allocated cases with status retrieved successfully", allocations);
    }

    // ==========================================
    // DASHBOARD ENDPOINTS
    // ==========================================

    /**
     * Get agency dashboard metrics
     */
    @GetMapping("/{agencyId}/dashboard")
    public ResponseEntity<CommonResponse<AgencyDashboardDTO>> getAgencyDashboard(
            @PathVariable Long agencyId) {
        log.info("Fetching dashboard for agency ID: {}", agencyId);
        AgencyDashboardDTO dashboard = agencyService.getAgencyDashboard(agencyId);
        return ResponseWrapper.ok("Agency dashboard retrieved successfully", dashboard);
    }
}
