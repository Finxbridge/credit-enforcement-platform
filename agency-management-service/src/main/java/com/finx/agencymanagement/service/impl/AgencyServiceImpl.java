package com.finx.agencymanagement.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finx.agencymanagement.constants.CacheConstants;
import com.finx.agencymanagement.domain.dto.*;
import com.finx.agencymanagement.domain.entity.Agency;
import com.finx.agencymanagement.domain.entity.AgencyAuditLog;
import com.finx.agencymanagement.domain.entity.AgencyCaseAllocation;
import com.finx.agencymanagement.domain.entity.User;
import com.finx.agencymanagement.domain.enums.AgencyStatus;
import com.finx.agencymanagement.domain.enums.AgencyType;
import com.finx.agencymanagement.exception.BusinessException;
import com.finx.agencymanagement.exception.ResourceNotFoundException;
import com.finx.agencymanagement.mapper.AgencyMapper;
import com.finx.agencymanagement.repository.AgencyAuditLogRepository;
import com.finx.agencymanagement.repository.AgencyCaseAllocationRepository;
import com.finx.agencymanagement.repository.AgencyRepository;
import com.finx.agencymanagement.repository.UserRepository;
import com.finx.agencymanagement.service.AgencyService;
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
import java.util.List;
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
    private final AgencyAuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final AgencyMapper agencyMapper;
    private final ObjectMapper objectMapper;

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

        for (Long caseId : request.getCaseIds()) {
            // Check if case already allocated to an agency
            caseAllocationRepository.findByCaseIdAndAllocationStatus(caseId, "ALLOCATED")
                    .ifPresent(existing -> {
                        existing.setAllocationStatus("DEALLOCATED");
                        existing.setDeallocatedAt(LocalDateTime.now());
                        existing.setDeallocatedBy(allocatedBy);
                        existing.setDeallocationReason("Reallocated to agency " + agency.getAgencyCode());
                        caseAllocationRepository.save(existing);
                    });

            // Create new allocation
            AgencyCaseAllocation allocation = new AgencyCaseAllocation();
            allocation.setAgencyId(request.getAgencyId());
            allocation.setCaseId(caseId);
            allocation.setAllocationStatus("ALLOCATED");
            allocation.setAllocatedAt(LocalDateTime.now());
            allocation.setAllocatedBy(allocatedBy);
            allocation.setBatchId(batchId);
            allocation.setNotes(request.getNotes());
            caseAllocationRepository.save(allocation);
        }

        // Update agency case count
        agency.setTotalCasesAllocated((agency.getTotalCasesAllocated() != null ? agency.getTotalCasesAllocated() : 0) + request.getCaseIds().size());
        agency.setActiveCasesCount((agency.getActiveCasesCount() != null ? agency.getActiveCasesCount() : 0) + request.getCaseIds().size());
        agencyRepository.save(agency);

        createAuditLog("CASES_ALLOCATED_TO_AGENCY", "AGENCY", request.getAgencyId(), "ALLOCATE",
                allocatedBy, null, request.getCaseIds(), "Allocated " + request.getCaseIds().size() + " cases");

        log.info("Allocated {} cases to agency {} with batch: {}", request.getCaseIds().size(), agency.getAgencyCode(), batchId);
    }

    @Override
    @Transactional
    public void assignCasesToAgent(Long agencyId, AgentCaseAssignmentRequest request, Long assignedBy) {
        log.info("Assigning {} cases from agency {} to agent {}",
                request.getCaseIds().size(), agencyId, request.getAgentId());

        // Verify agency exists
        agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", agencyId));

        // Verify agent exists
        User agent = userRepository.findById(request.getAgentId())
                .orElseThrow(() -> new ResourceNotFoundException("Agent", request.getAgentId()));

        // Check if cases belong to this agency and are not already assigned
        for (Long caseId : request.getCaseIds()) {
            AgencyCaseAllocation allocation = caseAllocationRepository.findByCaseIdAndAllocationStatus(caseId, "ALLOCATED")
                    .orElseThrow(() -> new BusinessException("Case " + caseId + " is not allocated to any agency"));

            if (!allocation.getAgencyId().equals(agencyId)) {
                throw new BusinessException("Case " + caseId + " is not allocated to agency " + agencyId);
            }

            // If already assigned to another agent, decrement their count
            if (allocation.getAgentId() != null && !allocation.getAgentId().equals(request.getAgentId())) {
                userRepository.decrementCaseCount(allocation.getAgentId(), 1);
            }
        }

        // Assign cases to agent
        int updated = caseAllocationRepository.assignCasesToAgent(agencyId, request.getCaseIds(), request.getAgentId());

        // Increment agent's case count
        userRepository.incrementCaseCount(request.getAgentId(), updated);

        createAuditLog("CASES_ASSIGNED_TO_AGENT", "AGENCY", agencyId, "ASSIGN",
                assignedBy, null, request, "Assigned " + updated + " cases to agent " + agent.getUsername());

        log.info("Assigned {} cases to agent {}", updated, agent.getUsername());
    }

    @Override
    @Transactional
    public void deallocateCasesFromAgency(Long agencyId, List<Long> caseIds, String reason, Long deallocatedBy) {
        log.info("Deallocating {} cases from agency: {}", caseIds.size(), agencyId);

        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", agencyId));

        int deallocatedCount = 0;
        for (Long caseId : caseIds) {
            caseAllocationRepository.findByCaseIdAndAllocationStatus(caseId, "ALLOCATED")
                    .ifPresent(allocation -> {
                        if (allocation.getAgencyId().equals(agencyId)) {
                            // Decrement agent count if assigned
                            if (allocation.getAgentId() != null) {
                                userRepository.decrementCaseCount(allocation.getAgentId(), 1);
                            }

                            allocation.setAllocationStatus("DEALLOCATED");
                            allocation.setDeallocatedAt(LocalDateTime.now());
                            allocation.setDeallocatedBy(deallocatedBy);
                            allocation.setDeallocationReason(reason);
                            caseAllocationRepository.save(allocation);
                        }
                    });
            deallocatedCount++;
        }

        // Update agency case count
        agency.setActiveCasesCount(Math.max(0, (agency.getActiveCasesCount() != null ? agency.getActiveCasesCount() : 0) - deallocatedCount));
        agencyRepository.save(agency);

        createAuditLog("CASES_DEALLOCATED_FROM_AGENCY", "AGENCY", agencyId, "DEALLOCATE",
                deallocatedBy, caseIds, null, reason);

        log.info("Deallocated {} cases from agency: {}", deallocatedCount, agency.getAgencyCode());
    }

    @Override
    public Page<AgencyCaseAllocationDTO> getAgencyCaseAllocations(Long agencyId, Pageable pageable) {
        log.info("Fetching case allocations for agency: {}", agencyId);
        return caseAllocationRepository.findByAgencyIdAndAllocationStatus(agencyId, "ALLOCATED", pageable)
                .map(this::toAllocationDTO);
    }

    @Override
    public Page<AgencyCaseAllocationDTO> getUnassignedCases(Long agencyId, Pageable pageable) {
        log.info("Fetching unassigned cases for agency: {}", agencyId);
        return caseAllocationRepository.findUnassignedCasesByAgencyId(agencyId, pageable)
                .map(this::toAllocationDTO);
    }

    @Override
    public Page<AgencyCaseAllocationDTO> getAllUnassignedCases(Pageable pageable) {
        log.info("Fetching all unassigned cases across all agencies");
        return caseAllocationRepository.findAllUnassignedCases(pageable)
                .map(this::toAllocationDTO);
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

        return AgencyDashboardDTO.builder()
                .agencyId(agencyId)
                .agencyName(agency.getAgencyName())
                .totalCases(agency.getTotalCasesAllocated())
                .activeCases(agency.getActiveCasesCount())
                .resolvedCases(agency.getTotalCasesResolved())
                .totalCollected(BigDecimal.ZERO)
                .commissionEarned(BigDecimal.ZERO)
                .avgResolutionDays(0)
                .ptpKeptRate(agency.getPtpSuccessRate())
                .ptpBrokenRate(BigDecimal.valueOf(100).subtract(agency.getPtpSuccessRate() != null ? agency.getPtpSuccessRate() : BigDecimal.ZERO))
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
}
