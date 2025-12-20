package com.finx.agencymanagement.service;

import com.finx.agencymanagement.domain.dto.*;
import com.finx.agencymanagement.domain.enums.AgencyStatus;
import com.finx.agencymanagement.domain.enums.AgencyType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Agency Service Interface
 * Provides business operations for agency management
 *
 * Note: Agency user/agent creation is handled by access-management-service.
 * This service uses shared users table for agents.
 */
public interface AgencyService {

    // ==========================================
    // AGENCY CRUD OPERATIONS
    // ==========================================

    AgencyDTO createAgency(CreateAgencyRequest request, Long createdBy);

    AgencyDTO getAgencyById(Long agencyId);

    AgencyDTO getAgencyByCode(String agencyCode);

    Page<AgencyDTO> getAllAgencies(Pageable pageable);

    Page<AgencyDTO> getAgenciesByStatus(AgencyStatus status, Pageable pageable);

    Page<AgencyDTO> getAgenciesByType(AgencyType type, Pageable pageable);

    Page<AgencyDTO> searchAgencies(String searchTerm, Pageable pageable);

    AgencyDTO updateAgency(Long agencyId, CreateAgencyRequest request, Long updatedBy);

    void deleteAgency(Long agencyId, Long deletedBy);

    // ==========================================
    // AGENCY APPROVAL WORKFLOW
    // ==========================================

    List<AgencyDTO> getAgenciesPendingApproval();

    AgencyDTO approveAgency(Long agencyId, String notes, Long approvedBy);

    AgencyDTO rejectAgency(Long agencyId, String reason, Long rejectedBy);

    AgencyDTO processApproval(AgencyApprovalRequest request, Long processedBy);

    // ==========================================
    // AGENCY STATUS MANAGEMENT
    // ==========================================

    AgencyDTO activateAgency(Long agencyId, Long activatedBy);

    AgencyDTO deactivateAgency(Long agencyId, String reason, Long deactivatedBy);

    AgencyDTO suspendAgency(Long agencyId, String reason, Long suspendedBy);

    // ==========================================
    // ACTIVE AGENTS (from shared users table)
    // ==========================================

    /**
     * Get all active agents from users table
     */
    List<AgentDTO> getActiveAgents();

    /**
     * Get agents who have cases assigned within specific agency
     */
    List<AgentDTO> getAgentsByAgency(Long agencyId);

    // ==========================================
    // CASE ALLOCATION
    // ==========================================

    /**
     * Allocate cases to agency (first level allocation)
     * Removes previous agent allocation if any
     */
    void allocateCasesToAgency(AgencyCaseAllocationRequest request, Long allocatedBy);

    /**
     * Assign cases from agency to specific agent (second level allocation)
     * Updates agent's statistics and deallocates from previous agent if any
     */
    void assignCasesToAgent(Long agencyId, AgentCaseAssignmentRequest request, Long assignedBy);

    void deallocateCasesFromAgency(Long agencyId, List<Long> caseIds, String reason, Long deallocatedBy);

    Page<AgencyCaseAllocationDTO> getAgencyCaseAllocations(Long agencyId, Pageable pageable);

    /**
     * Get cases allocated to agency but not yet assigned to any agent
     */
    Page<AgencyCaseAllocationDTO> getUnassignedCases(Long agencyId, Pageable pageable);

    /**
     * Get ALL cases allocated to any agency but not yet assigned to any agent
     * Used for Case Assignment tab to show all unassigned cases across all agencies
     */
    Page<AgencyCaseAllocationDTO> getAllUnassignedCases(Pageable pageable);

    // ==========================================
    // DASHBOARD
    // ==========================================

    AgencyDashboardDTO getAgencyDashboard(Long agencyId);
}
