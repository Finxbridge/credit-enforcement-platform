package com.finx.agencymanagement.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finx.agencymanagement.constants.CacheConstants;
import com.finx.agencymanagement.domain.dto.*;
import com.finx.agencymanagement.domain.entity.Agency;
import com.finx.agencymanagement.domain.entity.AgencyAuditLog;
import com.finx.agencymanagement.domain.entity.AgencyCaseAllocation;
import com.finx.agencymanagement.domain.entity.AllocationHistory;
import com.finx.agencymanagement.domain.entity.User;
import com.finx.agencymanagement.domain.enums.AgencyStatus;
import com.finx.agencymanagement.domain.enums.AgencyType;
import com.finx.agencymanagement.domain.enums.AllocationAction;
import com.finx.agencymanagement.exception.BusinessException;
import com.finx.agencymanagement.exception.ResourceNotFoundException;
import com.finx.agencymanagement.mapper.AgencyMapper;
import com.finx.agencymanagement.repository.AgencyAuditLogRepository;
import com.finx.agencymanagement.repository.AgencyCaseAllocationRepository;
import com.finx.agencymanagement.repository.AgencyRepository;
import com.finx.agencymanagement.repository.AllocationHistoryRepository;
import com.finx.agencymanagement.repository.CaseAllocationReadRepository;
import com.finx.agencymanagement.repository.UserRepository;
import com.finx.agencymanagement.service.AgencyService;
import com.finx.agencymanagement.service.CaseEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Agency Service Implementation
 * Handles agency management, case allocation to agencies, and agent assignment
 *
 * Note: User/Agent creation is handled by access-management-service.
 * This service uses the shared users table for agent operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgencyServiceImpl implements AgencyService {

    private final AgencyRepository agencyRepository;
    private final AgencyCaseAllocationRepository caseAllocationRepository;
    private final CaseAllocationReadRepository caseAllocationReadRepository;
    private final AgencyAuditLogRepository auditLogRepository;
    private final AllocationHistoryRepository allocationHistoryRepository;
    private final UserRepository userRepository;
    private final AgencyMapper agencyMapper;
    private final ObjectMapper objectMapper;
    private final CaseEventService caseEventService;

    // ========================================
    // Agency CRUD Operations
    // ========================================

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.AGENCY_CACHE, allEntries = true)
    public AgencyDTO createAgency(CreateAgencyRequest request, Long createdBy) {
        log.info("Creating agency: {}", request.getAgencyName());

        // Validate PAN uniqueness if provided
        if (request.getPanNumber() != null && !request.getPanNumber().isEmpty()) {
            if (agencyRepository.existsByPanNumber(request.getPanNumber())) {
                throw new BusinessException("Agency with PAN number '" + request.getPanNumber() + "' already exists");
            }
        }

        // Validate GST uniqueness if provided
        if (request.getGstNumber() != null && !request.getGstNumber().isEmpty()) {
            if (agencyRepository.existsByGstNumber(request.getGstNumber())) {
                throw new BusinessException("Agency with GST number '" + request.getGstNumber() + "' already exists");
            }
        }

        Agency agency = agencyMapper.toEntity(request);
        agency.setAgencyCode(generateAgencyCode(request.getAgencyType()));
        agency.setStatus(AgencyStatus.PENDING_APPROVAL);
        agency.setSubmittedAt(LocalDateTime.now());
        agency.setSubmittedBy(createdBy);
        agency.setCreatedBy(createdBy);
        agency.setIsActive(true);

        Agency savedAgency = agencyRepository.save(agency);

        createAuditLog("AGENCY_CREATED", "AGENCY", savedAgency.getId(), "CREATE",
                createdBy, null, savedAgency, null);

        log.info("Agency created with code: {}", savedAgency.getAgencyCode());
        return agencyMapper.toDto(savedAgency);
    }

    @Override
    @Cacheable(value = CacheConstants.AGENCY_CACHE, key = "#agencyId")
    public AgencyDTO getAgencyById(Long agencyId) {
        log.info("Fetching agency by ID: {}", agencyId);
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", agencyId));
        return agencyMapper.toDto(agency);
    }

    @Override
    public AgencyDTO getAgencyByCode(String agencyCode) {
        log.info("Fetching agency by code: {}", agencyCode);
        Agency agency = agencyRepository.findByAgencyCode(agencyCode)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", agencyCode));
        return agencyMapper.toDto(agency);
    }

    @Override
    public Page<AgencyDTO> getAllAgencies(Pageable pageable) {
        log.info("Fetching all agencies with pagination");
        return agencyRepository.findAll(pageable)
                .map(agencyMapper::toDto);
    }

    @Override
    public Page<AgencyDTO> getAgenciesByStatus(AgencyStatus status, Pageable pageable) {
        log.info("Fetching agencies by status: {}", status);
        return agencyRepository.findByStatus(status, pageable)
                .map(agencyMapper::toDto);
    }

    @Override
    public Page<AgencyDTO> getAgenciesByType(AgencyType type, Pageable pageable) {
        log.info("Fetching agencies by type: {}", type);
        return agencyRepository.findByAgencyType(type, pageable)
                .map(agencyMapper::toDto);
    }

    @Override
    public Page<AgencyDTO> searchAgencies(String searchTerm, Pageable pageable) {
        log.info("Searching agencies with term: {}", searchTerm);
        return agencyRepository.searchAgencies(searchTerm, pageable)
                .map(agencyMapper::toDto);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.AGENCY_CACHE, allEntries = true)
    public AgencyDTO updateAgency(Long agencyId, CreateAgencyRequest request, Long updatedBy) {
        log.info("Updating agency: {}", agencyId);
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", agencyId));

        Agency oldAgency = cloneAgency(agency);

        // Update basic fields
        if (request.getAgencyName() != null) agency.setAgencyName(request.getAgencyName());
        if (request.getAgencyType() != null) agency.setAgencyType(request.getAgencyType());
        if (request.getContactPerson() != null) agency.setContactPerson(request.getContactPerson());
        if (request.getContactEmail() != null) agency.setContactEmail(request.getContactEmail());
        if (request.getContactPhone() != null) agency.setContactPhone(request.getContactPhone());
        if (request.getAddress() != null) agency.setAddressLine1(request.getAddress());
        if (request.getCity() != null) agency.setCity(request.getCity());
        if (request.getState() != null) agency.setState(request.getState());
        if (request.getPincode() != null) agency.setPincode(request.getPincode());
        if (request.getIfscCode() != null) agency.setBankIfsc(request.getIfscCode());
        if (request.getBankName() != null) agency.setBankName(request.getBankName());
        if (request.getBankAccountNumber() != null) agency.setBankAccountNumber(request.getBankAccountNumber());
        if (request.getCommissionRate() != null) agency.setCommissionPercentage(request.getCommissionRate());
        if (request.getMaxCaseLimit() != null) agency.setMaximumCases(request.getMaxCaseLimit());
        if (request.getContractStartDate() != null) agency.setContractStartDate(request.getContractStartDate());
        if (request.getContractEndDate() != null) agency.setContractEndDate(request.getContractEndDate());
        if (request.getPanNumber() != null) agency.setPanNumber(request.getPanNumber());
        if (request.getGstNumber() != null) agency.setGstNumber(request.getGstNumber());
        if (request.getNotes() != null) agency.setApprovalNotes(request.getNotes());

        // Update optional fields (sent as JSON strings from frontend)
        if (request.getKycDocuments() != null) agency.setKycDocuments(request.getKycDocuments());
        if (request.getServiceAreas() != null) agency.setServiceAreas(request.getServiceAreas());
        if (request.getServicePincodes() != null) agency.setServicePincodes(request.getServicePincodes());

        agency.setUpdatedBy(updatedBy);
        Agency updatedAgency = agencyRepository.save(agency);

        createAuditLog("AGENCY_UPDATED", "AGENCY", agencyId, "UPDATE",
                updatedBy, oldAgency, updatedAgency, null);

        return agencyMapper.toDto(updatedAgency);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.AGENCY_CACHE, allEntries = true)
    public void deleteAgency(Long agencyId, Long deletedBy) {
        log.info("Deleting agency: {}", agencyId);
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", agencyId));

        Long activeAllocations = caseAllocationRepository.countActiveAllocationsByAgencyId(agencyId);
        if (activeAllocations > 0) {
            throw new BusinessException("Cannot delete agency with active case allocations. Please deallocate cases first.");
        }

        agency.setIsActive(false);
        agency.setStatus(AgencyStatus.TERMINATED);
        agency.setUpdatedBy(deletedBy);
        agencyRepository.save(agency);

        createAuditLog("AGENCY_DELETED", "AGENCY", agencyId, "DELETE",
                deletedBy, agency, null, null);
    }

    // ========================================
    // Agency Approval Workflow
    // ========================================

    @Override
    public List<AgencyDTO> getAgenciesPendingApproval() {
        log.info("Fetching agencies pending approval");
        return agencyRepository.findByStatus(AgencyStatus.PENDING_APPROVAL)
                .stream()
                .map(agencyMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.AGENCY_CACHE, allEntries = true)
    public AgencyDTO approveAgency(Long agencyId, String notes, Long approvedBy) {
        log.info("Approving agency: {}", agencyId);
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", agencyId));

        if (agency.getStatus() != AgencyStatus.PENDING_APPROVAL) {
            throw new BusinessException("Agency is not in PENDING_APPROVAL status");
        }

        agency.setStatus(AgencyStatus.APPROVED);
        agency.setApprovedAt(LocalDateTime.now());
        agency.setApprovedBy(approvedBy);
        agency.setApprovalNotes(notes);
        agency.setUpdatedBy(approvedBy);

        Agency updatedAgency = agencyRepository.save(agency);

        createAuditLog("AGENCY_APPROVED", "AGENCY", agencyId, "APPROVE",
                approvedBy, null, updatedAgency, notes);

        return agencyMapper.toDto(updatedAgency);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.AGENCY_CACHE, allEntries = true)
    public AgencyDTO rejectAgency(Long agencyId, String reason, Long rejectedBy) {
        log.info("Rejecting agency: {}", agencyId);
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", agencyId));

        if (agency.getStatus() != AgencyStatus.PENDING_APPROVAL) {
            throw new BusinessException("Agency is not in PENDING_APPROVAL status");
        }

        agency.setStatus(AgencyStatus.REJECTED);
        agency.setRejectedAt(LocalDateTime.now());
        agency.setRejectedBy(rejectedBy);
        agency.setRejectionReason(reason);
        agency.setUpdatedBy(rejectedBy);

        Agency updatedAgency = agencyRepository.save(agency);

        createAuditLog("AGENCY_REJECTED", "AGENCY", agencyId, "REJECT",
                rejectedBy, null, updatedAgency, reason);

        return agencyMapper.toDto(updatedAgency);
    }

    @Override
    @Transactional
    public AgencyDTO processApproval(AgencyApprovalRequest request, Long processedBy) {
        if (Boolean.TRUE.equals(request.getApproved())) {
            return approveAgency(request.getAgencyId(), request.getNotes(), processedBy);
        } else {
            return rejectAgency(request.getAgencyId(), request.getReason(), processedBy);
        }
    }

    // ========================================
    // Agency Status Management
    // ========================================

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.AGENCY_CACHE, allEntries = true)
    public AgencyDTO activateAgency(Long agencyId, Long activatedBy) {
        log.info("Activating agency: {}", agencyId);
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", agencyId));

        if (agency.getStatus() != AgencyStatus.APPROVED && agency.getStatus() != AgencyStatus.INACTIVE) {
            throw new BusinessException("Agency must be APPROVED or INACTIVE to activate");
        }

        agency.setStatus(AgencyStatus.ACTIVE);
        agency.setIsActive(true);
        agency.setUpdatedBy(activatedBy);

        Agency updatedAgency = agencyRepository.save(agency);

        createAuditLog("AGENCY_ACTIVATED", "AGENCY", agencyId, "ACTIVATE",
                activatedBy, null, updatedAgency, null);

        return agencyMapper.toDto(updatedAgency);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.AGENCY_CACHE, allEntries = true)
    public AgencyDTO deactivateAgency(Long agencyId, String reason, Long deactivatedBy) {
        log.info("Deactivating agency: {}", agencyId);
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", agencyId));

        agency.setStatus(AgencyStatus.INACTIVE);
        agency.setIsActive(false);
        agency.setUpdatedBy(deactivatedBy);

        Agency updatedAgency = agencyRepository.save(agency);

        createAuditLog("AGENCY_DEACTIVATED", "AGENCY", agencyId, "DEACTIVATE",
                deactivatedBy, null, updatedAgency, reason);

        return agencyMapper.toDto(updatedAgency);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.AGENCY_CACHE, allEntries = true)
    public AgencyDTO suspendAgency(Long agencyId, String reason, Long suspendedBy) {
        log.info("Suspending agency: {}", agencyId);
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", agencyId));

        agency.setStatus(AgencyStatus.SUSPENDED);
        agency.setUpdatedBy(suspendedBy);

        Agency updatedAgency = agencyRepository.save(agency);

        createAuditLog("AGENCY_SUSPENDED", "AGENCY", agencyId, "SUSPEND",
                suspendedBy, null, updatedAgency, reason);

        return agencyMapper.toDto(updatedAgency);
    }

    // ========================================
    // Active Agents (from shared users table)
    // ========================================

    @Override
    public List<AgentDTO> getActiveAgents() {
        log.info("Fetching all active agents from users table");
        return userRepository.findAllActiveAgents()
                .stream()
                .map(this::toAgentDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AgentDTO> getAgentsByAgency(Long agencyId) {
        log.info("Fetching agents for agency: {}", agencyId);
        return userRepository.findAgentsByAgencyId(agencyId)
                .stream()
                .map(this::toAgentDTO)
                .collect(Collectors.toList());
    }

    // ========================================
    // Case Allocation
    // ========================================

    @Override
    @Transactional
    public void allocateCasesToAgency(AgencyCaseAllocationRequest request, Long allocatedBy) {
        log.info("Allocating {} cases to agency: {}", request.getCaseIds().size(), request.getAgencyId());

        Agency agency = agencyRepository.findById(request.getAgencyId())
                .orElseThrow(() -> new ResourceNotFoundException("Agency", request.getAgencyId()));

        if (agency.getStatus() != AgencyStatus.ACTIVE && agency.getStatus() != AgencyStatus.APPROVED) {
            throw new BusinessException("Cannot allocate cases to agency that is not ACTIVE or APPROVED");
        }

        String batchId = UUID.randomUUID().toString();
        int allocatedCount = 0;
        int skippedCount = 0;
        List<AllocationHistory> historyEntries = new ArrayList<>();

        for (Long caseId : request.getCaseIds()) {
            // Check if case already allocated to THIS specific agency
            // A case CAN be allocated to multiple agencies, but not twice to the same agency
            java.util.Optional<AgencyCaseAllocation> existingAllocation =
                    caseAllocationRepository.findByCaseIdAndAgencyIdAndStatus(caseId, request.getAgencyId());

            if (existingAllocation.isPresent()) {
                log.info("Case {} already allocated to agency {}, skipping", caseId, agency.getAgencyCode());
                skippedCount++;
                continue;
            }

            // Create new allocation (allows same case to be allocated to multiple agencies)
            AgencyCaseAllocation allocation = new AgencyCaseAllocation();
            allocation.setAgencyId(request.getAgencyId());
            allocation.setCaseId(caseId);
            allocation.setAllocationStatus("ALLOCATED");
            allocation.setAllocatedAt(LocalDateTime.now());
            allocation.setAllocatedBy(allocatedBy);
            allocation.setBatchId(batchId);
            allocation.setNotes(request.getNotes());
            caseAllocationRepository.save(allocation);
            allocatedCount++;

            // Record allocation history directly in DB
            historyEntries.add(AllocationHistory.builder()
                    .caseId(caseId)
                    .action(AllocationAction.AGENCY_ALLOCATED.name())
                    .newOwnerType("AGENCY")
                    .agencyId(agency.getId())
                    .agencyCode(agency.getAgencyCode())
                    .agencyName(agency.getAgencyName())
                    .allocatedBy(allocatedBy)
                    .batchId(batchId)
                    .reason(request.getNotes())
                    .allocatedAt(LocalDateTime.now())
                    .build());
        }

        // Update agency case count only for newly allocated cases
        if (allocatedCount > 0) {
            agency.setTotalCasesAllocated((agency.getTotalCasesAllocated() != null ? agency.getTotalCasesAllocated() : 0) + allocatedCount);
            agency.setActiveCasesCount((agency.getActiveCasesCount() != null ? agency.getActiveCasesCount() : 0) + allocatedCount);
            agencyRepository.save(agency);

            // Save allocation history directly to database
            saveAllocationHistory(historyEntries);

            // Log case events for agency allocation
            for (Long caseId : request.getCaseIds()) {
                caseEventService.logCaseAllocatedToAgency(
                        caseId, null, agency.getId(), agency.getAgencyCode(),
                        agency.getAgencyName(), allocatedBy, null);
            }
        }

        createAuditLog("CASES_ALLOCATED_TO_AGENCY", "AGENCY", request.getAgencyId(), "ALLOCATE",
                allocatedBy, null, request.getCaseIds(),
                "Allocated " + allocatedCount + " cases" + (skippedCount > 0 ? ", skipped " + skippedCount + " already allocated" : ""));

        log.info("Allocated {} cases to agency {} with batch: {} (skipped: {})",
                allocatedCount, agency.getAgencyCode(), batchId, skippedCount);
    }

    @Override
    @Transactional
    public void assignCasesToAgent(Long agencyId, AgentCaseAssignmentRequest request, Long assignedBy) {
        log.info("Assigning {} cases from agency {} to agent {}",
                request.getCaseIds().size(), agencyId, request.getAgentId());

        // Verify agency exists
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", agencyId));

        // Verify agent exists and belongs to an agency (is an agency agent, not primary allocation agent)
        User agent = userRepository.findById(request.getAgentId())
                .orElseThrow(() -> new ResourceNotFoundException("Agent", request.getAgentId()));

        // Safety check: Only allow assignment to agency agents (not primary allocation agents)
        if (agent.getAgencyId() == null) {
            throw new BusinessException("Agent " + agent.getUsername() + " is not an agency agent. Cannot assign cases.");
        }

        int assignedCount = 0;
        int reassignedCount = 0;
        List<AllocationHistory> historyEntries = new ArrayList<>();

        for (Long caseId : request.getCaseIds()) {
            // Check if case already exists in agency_case_allocations
            java.util.Optional<AgencyCaseAllocation> existingAllocation =
                    caseAllocationRepository.findByCaseIdAndAllocationStatus(caseId, "ALLOCATED");

            Long previousAgentId = null;
            String previousAgentName = null;

            if (existingAllocation.isPresent()) {
                // Case is already in agency_case_allocations - update agent assignment
                AgencyCaseAllocation allocation = existingAllocation.get();

                // If already assigned to another agency agent, decrement their count
                // Only decrement if the previous agent is an agency agent (has agencyId set)
                if (allocation.getAgentId() != null && !allocation.getAgentId().equals(request.getAgentId())) {
                    User previousAgent = userRepository.findById(allocation.getAgentId()).orElse(null);
                    if (previousAgent != null && previousAgent.getAgencyId() != null) {
                        userRepository.decrementCaseCount(allocation.getAgentId(), 1);
                        previousAgentId = previousAgent.getId();
                        previousAgentName = previousAgent.getFirstName() + " " + previousAgent.getLastName();
                        reassignedCount++;
                    } else {
                        log.warn("Previous agent {} is not an agency agent, skipping decrement", allocation.getAgentId());
                    }
                }

                // Update agent assignment
                allocation.setAgentId(request.getAgentId());
                caseAllocationRepository.save(allocation);
                assignedCount++;

            } else {
                // Case is NOT in agency_case_allocations - it's coming from allocations table
                // Verify case exists in allocations table
                com.finx.agencymanagement.domain.entity.CaseAllocation sourceAllocation =
                        caseAllocationReadRepository.findByCaseIdAndStatus(caseId).orElse(null);

                if (sourceAllocation == null) {
                    log.warn("Case {} not found in allocations table, skipping", caseId);
                    continue;
                }

                // Create new entry in agency_case_allocations
                AgencyCaseAllocation newAllocation = new AgencyCaseAllocation();
                newAllocation.setCaseId(caseId);
                newAllocation.setExternalCaseId(sourceAllocation.getExternalCaseId());
                newAllocation.setAgencyId(agencyId);
                newAllocation.setAgentId(request.getAgentId());
                newAllocation.setAllocationStatus("ALLOCATED");
                newAllocation.setAllocatedAt(LocalDateTime.now());
                newAllocation.setAllocatedBy(assignedBy);
                newAllocation.setNotes("Assigned from allocation-service to agent. Original allocated_to: " + sourceAllocation.getAllocatedToId());

                caseAllocationRepository.save(newAllocation);
                assignedCount++;

                log.debug("Created new agency_case_allocation for case {} with agent {}", caseId, request.getAgentId());
            }

            // Record allocation history directly in DB
            AllocationAction action = previousAgentId != null ? AllocationAction.AGENT_REASSIGNED : AllocationAction.AGENT_ASSIGNED;
            historyEntries.add(AllocationHistory.builder()
                    .caseId(caseId)
                    .action(action.name())
                    .allocatedToUserId(agent.getId())
                    .allocatedToUsername(agent.getFirstName() + " " + agent.getLastName())
                    .newOwnerType("AGENT")
                    .allocatedFromUserId(previousAgentId)
                    .previousOwnerType(previousAgentId != null ? "AGENT" : null)
                    .agencyId(agency.getId())
                    .agencyCode(agency.getAgencyCode())
                    .agencyName(agency.getAgencyName())
                    .allocatedBy(assignedBy)
                    .reason(request.getNotes())
                    .allocatedAt(LocalDateTime.now())
                    .build());
        }

        // Increment agent's case count for newly assigned cases
        if (assignedCount > 0) {
            userRepository.incrementCaseCount(request.getAgentId(), assignedCount - reassignedCount);

            // Save allocation history directly to database
            saveAllocationHistory(historyEntries);

            // Log case events for agent assignment
            String agentName = agent.getFirstName() + " " + agent.getLastName();
            for (Long caseId : request.getCaseIds()) {
                caseEventService.logCaseAssignedToAgent(
                        caseId, null, agent.getId(), agentName,
                        agency.getId(), agency.getAgencyName(), assignedBy, null);
            }
        }

        // Update agency statistics
        agency.setActiveCasesCount((agency.getActiveCasesCount() != null ? agency.getActiveCasesCount() : 0) + (assignedCount - reassignedCount));
        agency.setTotalCasesAllocated((agency.getTotalCasesAllocated() != null ? agency.getTotalCasesAllocated() : 0) + (assignedCount - reassignedCount));
        agencyRepository.save(agency);

        createAuditLog("CASES_ASSIGNED_TO_AGENT", "AGENCY", agencyId, "ASSIGN",
                assignedBy, null, request,
                "Assigned " + assignedCount + " cases to agent " + agent.getUsername() +
                (reassignedCount > 0 ? " (" + reassignedCount + " reassigned from other agents)" : ""));

        log.info("Assigned {} cases to agent {} (reassigned: {})", assignedCount, agent.getUsername(), reassignedCount);
    }

    @Override
    @Transactional
    public void deallocateCasesFromAgency(Long agencyId, List<Long> caseIds, String reason, Long deallocatedBy) {
        log.info("Deallocating {} cases from agency: {}", caseIds.size(), agencyId);

        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", agencyId));

        int deallocatedCount = 0;
        List<AllocationHistory> historyEntries = new ArrayList<>();

        for (Long caseId : caseIds) {
            java.util.Optional<AgencyCaseAllocation> optAllocation =
                    caseAllocationRepository.findByCaseIdAndAllocationStatus(caseId, "ALLOCATED");

            if (optAllocation.isPresent()) {
                AgencyCaseAllocation allocation = optAllocation.get();
                if (allocation.getAgencyId().equals(agencyId)) {
                    Long previousAgentId = allocation.getAgentId();
                    String previousAgentName = null;

                    // Decrement agent count if assigned to an agency agent
                    // Only update stats for agency agents (not primary allocation agents)
                    if (allocation.getAgentId() != null) {
                        User agentUser = userRepository.findById(allocation.getAgentId()).orElse(null);
                        if (agentUser != null && agentUser.getAgencyId() != null) {
                            userRepository.decrementCaseCount(allocation.getAgentId(), 1);
                            previousAgentName = agentUser.getFirstName() + " " + agentUser.getLastName();
                        }
                    }

                    allocation.setAllocationStatus("DEALLOCATED");
                    allocation.setDeallocatedAt(LocalDateTime.now());
                    allocation.setDeallocatedBy(deallocatedBy);
                    allocation.setDeallocationReason(reason);
                    caseAllocationRepository.save(allocation);
                    deallocatedCount++;

                    // Record deallocation history directly in DB
                    historyEntries.add(AllocationHistory.builder()
                            .caseId(caseId)
                            .action(AllocationAction.AGENCY_DEALLOCATED.name())
                            .allocatedFromUserId(previousAgentId)
                            .previousOwnerType(previousAgentId != null ? "AGENT" : "AGENCY")
                            .agencyId(agency.getId())
                            .agencyCode(agency.getAgencyCode())
                            .agencyName(agency.getAgencyName())
                            .allocatedBy(deallocatedBy)
                            .reason(reason)
                            .allocatedAt(LocalDateTime.now())
                            .build());
                }
            }
        }

        // Update agency case count
        agency.setActiveCasesCount(Math.max(0, (agency.getActiveCasesCount() != null ? agency.getActiveCasesCount() : 0) - deallocatedCount));
        agencyRepository.save(agency);

        // Save deallocation history directly to database
        if (!historyEntries.isEmpty()) {
            saveAllocationHistory(historyEntries);

            // Log case events for agency deallocation
            for (Long caseId : caseIds) {
                caseEventService.logCaseDeallocatedFromAgency(
                        caseId, null, agency.getId(), agency.getAgencyCode(),
                        agency.getAgencyName(), reason, deallocatedBy, null);
            }
        }

        createAuditLog("CASES_DEALLOCATED_FROM_AGENCY", "AGENCY", agencyId, "DEALLOCATE",
                deallocatedBy, caseIds, null, reason);

        log.info("Deallocated {} cases from agency: {}", deallocatedCount, agency.getAgencyCode());
    }

    @Override
    public Page<AgencyCaseAllocationDTO> getAgencyCaseAllocations(Long agencyId, Pageable pageable) {
        log.info("Fetching case allocations for agency: {}", agencyId);
        Page<AgencyCaseAllocation> allocations = caseAllocationRepository.findByAgencyIdAndAllocationStatus(agencyId, "ALLOCATED", pageable);

        // Batch load agent names to avoid N+1 queries
        Set<Long> agentIds = allocations.getContent().stream()
                .map(AgencyCaseAllocation::getAgentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, User> agentMap = agentIds.isEmpty()
                ? Map.of()
                : userRepository.findAllById(agentIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u));

        return allocations.map(allocation -> toAllocationDTOWithAgentName(allocation, agentMap));
    }

    @Override
    public Page<AgencyCaseAllocationDTO> getUnassignedCases(Long agencyId, Pageable pageable) {
        log.info("Fetching unassigned cases for agency: {}", agencyId);
        return caseAllocationRepository.findUnassignedCasesByAgencyId(agencyId, pageable)
                .map(this::toAllocationDTO);
    }

    @Override
    public Page<AgencyCaseAllocationDTO> getAllUnassignedCases(Pageable pageable) {
        log.info("Fetching cases from allocation-service that are not yet assigned to any agent in agency-management");
        // Query allocations table to get cases that are:
        // 1. Allocated in allocation-reallocation-service (status = 'ALLOCATED')
        // 2. NOT already assigned to an agent in agency_case_allocations table
        return caseAllocationReadRepository.findCasesNotAssignedToAgent(pageable)
                .map(this::toCaseAllocationDTO);
    }

    @Override
    public Page<AgencyCaseAllocationDTO> getCasesNotAllocatedToAgency(Pageable pageable) {
        log.info("Fetching cases from allocation-service that are not yet allocated to any agency");
        // Query allocations table to get cases that are:
        // 1. Allocated in allocation-reallocation-service (status = 'ALLOCATED')
        // 2. NOT in agency_case_allocations table at all
        return caseAllocationReadRepository.findCasesNotAllocatedToAgency(pageable)
                .map(this::toCaseAllocationDTO);
    }

    @Override
    public Page<AgencyCaseAllocationDTO> getAllAllocatedCasesWithStatus(Pageable pageable) {
        log.info("Fetching all allocated cases with assignment status");

        // Get all allocated cases from allocations table
        Page<com.finx.agencymanagement.domain.entity.CaseAllocation> allocatedCases =
                caseAllocationReadRepository.findAllAllocatedCases(pageable);

        // Get all case IDs from the current page
        List<Long> caseIds = allocatedCases.getContent().stream()
                .map(com.finx.agencymanagement.domain.entity.CaseAllocation::getCaseId)
                .toList();

        // Get all agency assignments for these cases
        List<AgencyCaseAllocation> agencyAssignments = caseIds.isEmpty()
                ? java.util.Collections.emptyList()
                : caseAllocationRepository.findAllocationsByCaseIds(caseIds);

        // Group assignments by case ID
        java.util.Map<Long, List<AgencyCaseAllocation>> assignmentsByCase = agencyAssignments.stream()
                .collect(java.util.stream.Collectors.groupingBy(AgencyCaseAllocation::getCaseId));

        // Build lookup maps for agency and user names
        java.util.Set<Long> agencyIds = agencyAssignments.stream()
                .map(AgencyCaseAllocation::getAgencyId)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());

        java.util.Set<Long> agentIds = agencyAssignments.stream()
                .map(AgencyCaseAllocation::getAgentId)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());

        java.util.Map<Long, Agency> agencyMap = agencyIds.isEmpty()
                ? java.util.Collections.emptyMap()
                : agencyRepository.findAllById(agencyIds).stream()
                        .collect(java.util.stream.Collectors.toMap(Agency::getId, a -> a));

        java.util.Map<Long, User> userMap = agentIds.isEmpty()
                ? java.util.Collections.emptyMap()
                : userRepository.findAllById(agentIds).stream()
                        .collect(java.util.stream.Collectors.toMap(User::getId, u -> u));

        // Convert to DTOs with assignment status
        return allocatedCases.map(allocation -> {
            List<AgencyCaseAllocation> caseAssignments = assignmentsByCase.getOrDefault(allocation.getCaseId(), java.util.Collections.emptyList());

            // Determine assignment status
            String assignmentStatus;
            if (caseAssignments.isEmpty()) {
                assignmentStatus = "UNALLOCATED";
            } else if (caseAssignments.stream().anyMatch(a -> a.getAgentId() != null)) {
                assignmentStatus = "ASSIGNED_TO_AGENT";
            } else {
                assignmentStatus = "ALLOCATED_TO_AGENCY";
            }

            // Build assignment info list
            List<AgencyCaseAllocationDTO.CaseAssignmentInfo> assignmentInfos = caseAssignments.stream()
                    .map(a -> {
                        Agency agency = agencyMap.get(a.getAgencyId());
                        User agent = a.getAgentId() != null ? userMap.get(a.getAgentId()) : null;
                        return AgencyCaseAllocationDTO.CaseAssignmentInfo.builder()
                                .agencyId(a.getAgencyId())
                                .agencyName(agency != null ? agency.getAgencyName() : null)
                                .agencyCode(agency != null ? agency.getAgencyCode() : null)
                                .agentId(a.getAgentId())
                                .agentName(agent != null ? agent.getFirstName() + " " + agent.getLastName() : null)
                                .assignedAt(a.getAllocatedAt())
                                .build();
                    })
                    .toList();

            return AgencyCaseAllocationDTO.builder()
                    .caseId(allocation.getCaseId())
                    .externalCaseId(allocation.getExternalCaseId())
                    .allocationStatus(allocation.getAllocationStatus())
                    .assignmentStatus(assignmentStatus)
                    .allocatedAt(allocation.getAllocatedAt())
                    .allocatedBy(allocation.getAllocatedBy())
                    .assignments(assignmentInfos)
                    .assignmentCount(caseAssignments.size())
                    .build();
        });
    }

    /**
     * Convert CaseAllocation (from allocations table) to AgencyCaseAllocationDTO
     */
    private AgencyCaseAllocationDTO toCaseAllocationDTO(com.finx.agencymanagement.domain.entity.CaseAllocation allocation) {
        return AgencyCaseAllocationDTO.builder()
                .caseId(allocation.getCaseId())
                .externalCaseId(allocation.getExternalCaseId())
                .allocationStatus(allocation.getAllocationStatus())
                .assignmentStatus("UNALLOCATED")
                .allocatedAt(allocation.getAllocatedAt())
                .allocatedBy(allocation.getAllocatedBy())
                // agencyId and agentId are null since case is not yet allocated to agency/agent
                .agencyId(null)
                .agentId(null)
                .assignmentCount(0)
                .build();
    }

    // ========================================
    // Dashboard
    // ========================================

    @Override
    @Cacheable(value = CacheConstants.AGENCY_STATS_CACHE, key = "#agencyId")
    public AgencyDashboardDTO getAgencyDashboard(Long agencyId) {
        log.info("Fetching dashboard for agency: {}", agencyId);

        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", agencyId));

        // Calculate PTP broken rate only if there's actual PTP data
        // If ptpSuccessRate is null or 0, it could mean no PTPs exist, so broken rate should also be 0
        BigDecimal ptpKeptRate = agency.getPtpSuccessRate() != null ? agency.getPtpSuccessRate() : BigDecimal.ZERO;
        BigDecimal ptpBrokenRate = BigDecimal.ZERO;

        // Only calculate broken rate if there's a meaningful kept rate (meaning PTPs exist)
        // If kept rate > 0, then broken rate = 100 - kept rate
        // If kept rate = 0 but we have PTP data, broken rate could be 100
        // For now, if kept rate is 0, assume no PTPs and show 0 for both
        if (ptpKeptRate.compareTo(BigDecimal.ZERO) > 0) {
            ptpBrokenRate = BigDecimal.valueOf(100).subtract(ptpKeptRate);
        }

        return AgencyDashboardDTO.builder()
                .agencyId(agencyId)
                .agencyName(agency.getAgencyName())
                .totalCases(agency.getTotalCasesAllocated())
                .activeCases(agency.getActiveCasesCount())
                .resolvedCases(agency.getTotalCasesResolved())
                .totalCollected(BigDecimal.ZERO)
                .commissionEarned(BigDecimal.ZERO)
                .avgResolutionDays(0)
                .ptpKeptRate(ptpKeptRate)
                .ptpBrokenRate(ptpBrokenRate)
                .build();
    }

    // ========================================
    // Helper Methods
    // ========================================

    private String generateAgencyCode(AgencyType type) {
        String prefix = switch (type) {
            case INTERNAL -> "IN";
            case EXTERNAL -> "EX";
            case LEGAL -> "LG";
            case FIELD -> "FD";
        };
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void createAuditLog(String eventType, String entityType, Long entityId, String action,
                                Long actorId, Object oldValue, Object newValue, String notes) {
        try {
            AgencyAuditLog auditLog = new AgencyAuditLog();
            auditLog.setEventType(eventType);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setAction(action);
            auditLog.setActorId(actorId);
            auditLog.setActorType("USER");
            auditLog.setNotes(notes);

            if (oldValue != null) {
                auditLog.setOldValue(objectMapper.writeValueAsString(oldValue));
            }
            if (newValue != null) {
                auditLog.setNewValue(objectMapper.writeValueAsString(newValue));
            }

            auditLogRepository.save(auditLog);
        } catch (JsonProcessingException e) {
            log.error("Error creating audit log", e);
        }
    }

    private Agency cloneAgency(Agency agency) {
        Agency clone = new Agency();
        clone.setId(agency.getId());
        clone.setAgencyCode(agency.getAgencyCode());
        clone.setAgencyName(agency.getAgencyName());
        clone.setAgencyType(agency.getAgencyType());
        clone.setStatus(agency.getStatus());
        clone.setContactPerson(agency.getContactPerson());
        clone.setContactEmail(agency.getContactEmail());
        clone.setContactPhone(agency.getContactPhone());
        return clone;
    }

    private AgentDTO toAgentDTO(User user) {
        return AgentDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .state(user.getState())
                .city(user.getCity())
                .maxCaseCapacity(user.getMaxCaseCapacity())
                .currentCaseCount(user.getCurrentCaseCount())
                .status(user.getStatus())
                .build();
    }

    private AgencyCaseAllocationDTO toAllocationDTO(AgencyCaseAllocation allocation) {
        return AgencyCaseAllocationDTO.builder()
                .id(allocation.getId())
                .agencyId(allocation.getAgencyId())
                .caseId(allocation.getCaseId())
                .externalCaseId(allocation.getExternalCaseId())
                .agentId(allocation.getAgentId())
                .allocationStatus(allocation.getAllocationStatus())
                .batchId(allocation.getBatchId())
                .notes(allocation.getNotes())
                .allocatedAt(allocation.getAllocatedAt())
                .allocatedBy(allocation.getAllocatedBy())
                .deallocatedAt(allocation.getDeallocatedAt())
                .deallocatedBy(allocation.getDeallocatedBy())
                .deallocatedReason(allocation.getDeallocationReason())
                .build();
    }

    private AgencyCaseAllocationDTO toAllocationDTOWithAgentName(AgencyCaseAllocation allocation, Map<Long, User> agentMap) {
        String agentName = null;
        if (allocation.getAgentId() != null && agentMap.containsKey(allocation.getAgentId())) {
            User agent = agentMap.get(allocation.getAgentId());
            agentName = agent.getFirstName() + " " + agent.getLastName();
        }

        return AgencyCaseAllocationDTO.builder()
                .id(allocation.getId())
                .agencyId(allocation.getAgencyId())
                .caseId(allocation.getCaseId())
                .externalCaseId(allocation.getExternalCaseId())
                .agentId(allocation.getAgentId())
                .agentName(agentName)
                .allocationStatus(allocation.getAllocationStatus())
                .batchId(allocation.getBatchId())
                .notes(allocation.getNotes())
                .allocatedAt(allocation.getAllocatedAt())
                .allocatedBy(allocation.getAllocatedBy())
                .deallocatedAt(allocation.getDeallocatedAt())
                .deallocatedBy(allocation.getDeallocatedBy())
                .deallocatedReason(allocation.getDeallocationReason())
                .build();
    }

    /**
     * Save allocation history entries directly to the shared database.
     * This eliminates inter-service HTTP calls and improves response time.
     */
    private void saveAllocationHistory(List<AllocationHistory> historyEntries) {
        if (historyEntries == null || historyEntries.isEmpty()) {
            return;
        }

        try {
            allocationHistoryRepository.saveAll(historyEntries);
            log.debug("Saved {} allocation history entries directly to database", historyEntries.size());
        } catch (Exception e) {
            // Log error but don't fail the main operation
            log.error("Failed to save allocation history to database: {}. History entries: {}",
                    e.getMessage(), historyEntries.size());
        }
    }
}
