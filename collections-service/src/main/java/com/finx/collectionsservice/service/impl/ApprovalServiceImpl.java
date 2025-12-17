package com.finx.collectionsservice.service.impl;

import com.finx.collectionsservice.domain.dto.*;
import com.finx.collectionsservice.domain.entity.ApprovalHistory;
import com.finx.collectionsservice.domain.entity.ApprovalMatrix;
import com.finx.collectionsservice.domain.entity.ApprovalRequest;
import com.finx.collectionsservice.domain.enums.ApprovalStatus;
import com.finx.collectionsservice.domain.enums.ApprovalType;
import com.finx.collectionsservice.exception.BusinessException;
import com.finx.collectionsservice.exception.ResourceNotFoundException;
import com.finx.collectionsservice.repository.ApprovalHistoryRepository;
import com.finx.collectionsservice.repository.ApprovalMatrixRepository;
import com.finx.collectionsservice.repository.ApprovalRequestRepository;
import com.finx.collectionsservice.service.ApprovalService;
import com.finx.collectionsservice.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalMatrixRepository matrixRepository;
    private final ApprovalRequestRepository requestRepository;
    private final ApprovalHistoryRepository historyRepository;
    private final AuditLogService auditLogService;

    // ==================== MATRIX MANAGEMENT ====================

    @Override
    @Transactional
    public ApprovalMatrixDTO createMatrix(CreateApprovalMatrixRequest request, Long userId) {
        if (matrixRepository.existsByMatrixCode(request.getMatrixCode())) {
            throw new BusinessException("Matrix with code already exists: " + request.getMatrixCode());
        }

        ApprovalMatrix matrix = ApprovalMatrix.builder()
                .matrixCode(request.getMatrixCode())
                .matrixName(request.getMatrixName())
                .approvalType(request.getApprovalType())
                .description(request.getDescription())
                .minAmount(request.getMinAmount())
                .maxAmount(request.getMaxAmount())
                .minPercentage(request.getMinPercentage())
                .maxPercentage(request.getMaxPercentage())
                .approvalLevel(request.getApprovalLevel())
                .approverRoleId(request.getApproverRoleId())
                .approverRoleName(request.getApproverRoleName())
                .approverUserId(request.getApproverUserId())
                .approverUserName(request.getApproverUserName())
                .escalationHours(request.getEscalationHours())
                .escalationLevel(request.getEscalationLevel())
                .escalationRoleId(request.getEscalationRoleId())
                .autoApproveEnabled(request.getAutoApproveEnabled())
                .autoApproveBelowAmount(request.getAutoApproveBelowAmount())
                .criteria(request.getCriteria())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .priorityOrder(request.getPriorityOrder())
                .createdBy(userId)
                .build();

        ApprovalMatrix saved = matrixRepository.save(matrix);

        auditLogService.logCreate("ApprovalMatrix", saved.getId(), saved.getMatrixName(),
                Map.of("matrixCode", saved.getMatrixCode(), "approvalType", saved.getApprovalType().name()));

        log.info("Created approval matrix: {} - {}", saved.getMatrixCode(), saved.getMatrixName());
        return mapMatrixToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ApprovalMatrixDTO getMatrixById(Long id) {
        ApprovalMatrix matrix = matrixRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Approval matrix not found with id: " + id));
        return mapMatrixToDTO(matrix);
    }

    @Override
    @Transactional(readOnly = true)
    public ApprovalMatrixDTO getMatrixByCode(String matrixCode) {
        ApprovalMatrix matrix = matrixRepository.findByMatrixCode(matrixCode)
                .orElseThrow(() -> new ResourceNotFoundException("Approval matrix not found with code: " + matrixCode));
        return mapMatrixToDTO(matrix);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalMatrixDTO> getActiveMatrices() {
        return matrixRepository.findByIsActiveTrueOrderByPriorityOrderDesc()
                .stream()
                .map(this::mapMatrixToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalMatrixDTO> getMatricesByType(ApprovalType type) {
        return matrixRepository.findByApprovalTypeAndIsActiveTrueOrderByPriorityOrderDesc(type)
                .stream()
                .map(this::mapMatrixToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApprovalMatrixDTO> getAllMatrices(ApprovalType type, Boolean isActive, Pageable pageable) {
        return matrixRepository.findWithFilters(type, isActive, pageable)
                .map(this::mapMatrixToDTO);
    }

    @Override
    @Transactional
    public ApprovalMatrixDTO updateMatrix(Long id, CreateApprovalMatrixRequest request, Long userId) {
        ApprovalMatrix matrix = matrixRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Approval matrix not found with id: " + id));

        Map<String, Object> oldValue = Map.of("matrixName", matrix.getMatrixName());

        matrix.setMatrixName(request.getMatrixName());
        matrix.setDescription(request.getDescription());
        matrix.setMinAmount(request.getMinAmount());
        matrix.setMaxAmount(request.getMaxAmount());
        matrix.setMinPercentage(request.getMinPercentage());
        matrix.setMaxPercentage(request.getMaxPercentage());
        matrix.setApprovalLevel(request.getApprovalLevel());
        matrix.setApproverRoleId(request.getApproverRoleId());
        matrix.setApproverRoleName(request.getApproverRoleName());
        matrix.setApproverUserId(request.getApproverUserId());
        matrix.setApproverUserName(request.getApproverUserName());
        matrix.setEscalationHours(request.getEscalationHours());
        matrix.setEscalationLevel(request.getEscalationLevel());
        matrix.setEscalationRoleId(request.getEscalationRoleId());
        matrix.setAutoApproveEnabled(request.getAutoApproveEnabled());
        matrix.setAutoApproveBelowAmount(request.getAutoApproveBelowAmount());
        matrix.setCriteria(request.getCriteria());
        matrix.setPriorityOrder(request.getPriorityOrder());
        matrix.setUpdatedBy(userId);

        ApprovalMatrix saved = matrixRepository.save(matrix);

        auditLogService.logUpdate("ApprovalMatrix", saved.getId(), saved.getMatrixName(),
                oldValue, Map.of("matrixName", saved.getMatrixName()), List.of("matrixName"));

        log.info("Updated approval matrix: {}", saved.getMatrixCode());
        return mapMatrixToDTO(saved);
    }

    @Override
    @Transactional
    public ApprovalMatrixDTO activateMatrix(Long id, Long userId) {
        ApprovalMatrix matrix = matrixRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Approval matrix not found with id: " + id));

        matrix.setIsActive(true);
        matrix.setUpdatedBy(userId);
        ApprovalMatrix saved = matrixRepository.save(matrix);

        auditLogService.logStatusChange("ApprovalMatrix", saved.getId(),
                saved.getMatrixName(), "INACTIVE", "ACTIVE");

        log.info("Activated approval matrix: {}", saved.getMatrixCode());
        return mapMatrixToDTO(saved);
    }

    @Override
    @Transactional
    public ApprovalMatrixDTO deactivateMatrix(Long id, Long userId) {
        ApprovalMatrix matrix = matrixRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Approval matrix not found with id: " + id));

        matrix.setIsActive(false);
        matrix.setUpdatedBy(userId);
        ApprovalMatrix saved = matrixRepository.save(matrix);

        auditLogService.logStatusChange("ApprovalMatrix", saved.getId(),
                saved.getMatrixName(), "ACTIVE", "INACTIVE");

        log.info("Deactivated approval matrix: {}", saved.getMatrixCode());
        return mapMatrixToDTO(saved);
    }

    @Override
    @Transactional
    public void deleteMatrix(Long id, Long userId) {
        ApprovalMatrix matrix = matrixRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Approval matrix not found with id: " + id));

        auditLogService.logDelete("ApprovalMatrix", matrix.getId(), matrix.getMatrixName(),
                Map.of("matrixCode", matrix.getMatrixCode()));

        matrixRepository.delete(matrix);
        log.info("Deleted approval matrix: {}", matrix.getMatrixCode());
    }

    // ==================== APPROVAL REQUEST MANAGEMENT ====================

    @Override
    @Transactional
    public ApprovalRequestDTO createApprovalRequest(CreateApprovalRequest request, Long userId) {
        String requestNumber = "APR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Find matching matrix for determining approver
        List<ApprovalMatrix> matrices = matrixRepository.findMatchingMatrixByAmount(
                request.getApprovalType(), request.getRequestedAmount());

        ApprovalMatrix matrix = matrices.isEmpty() ? null : matrices.get(0);

        // Check for auto-approval
        if (matrix != null && Boolean.TRUE.equals(matrix.getAutoApproveEnabled()) &&
                matrix.getAutoApproveBelowAmount() != null &&
                request.getRequestedAmount().compareTo(matrix.getAutoApproveBelowAmount()) < 0) {

            ApprovalRequest approvalRequest = buildApprovalRequest(request, requestNumber, userId, matrix);
            approvalRequest.setApprovalStatus(ApprovalStatus.AUTO_APPROVED);
            approvalRequest.setApprovedAt(LocalDateTime.now());
            approvalRequest.setApprovedAmount(request.getRequestedAmount());
            approvalRequest.setApprovalRemarks("Auto-approved based on matrix rules");

            ApprovalRequest saved = requestRepository.save(approvalRequest);
            createHistory(saved, "AUTO_APPROVED", ApprovalStatus.PENDING, ApprovalStatus.AUTO_APPROVED,
                    userId, "System", "Auto-approved", null);

            auditLogService.logCreate("ApprovalRequest", saved.getId(), saved.getRequestNumber(),
                    Map.of("status", "AUTO_APPROVED", "amount", request.getRequestedAmount()));

            log.info("Auto-approved request: {} for amount: {}", saved.getRequestNumber(), request.getRequestedAmount());
            return mapRequestToDTO(saved);
        }

        ApprovalRequest approvalRequest = buildApprovalRequest(request, requestNumber, userId, matrix);

        if (matrix != null && matrix.getEscalationHours() != null) {
            approvalRequest.setExpiresAt(LocalDateTime.now().plusHours(matrix.getEscalationHours()));
        }

        ApprovalRequest saved = requestRepository.save(approvalRequest);
        createHistory(saved, "SUBMITTED", null, ApprovalStatus.PENDING, userId, null, "Request submitted", null);

        auditLogService.logCreate("ApprovalRequest", saved.getId(), saved.getRequestNumber(),
                Map.of("status", "PENDING", "amount", request.getRequestedAmount(), "type", request.getApprovalType().name()));

        log.info("Created approval request: {} for type: {}", saved.getRequestNumber(), request.getApprovalType());
        return mapRequestToDTO(saved);
    }

    private ApprovalRequest buildApprovalRequest(CreateApprovalRequest request, String requestNumber,
                                                  Long userId, ApprovalMatrix matrix) {
        return ApprovalRequest.builder()
                .requestNumber(requestNumber)
                .approvalType(request.getApprovalType())
                .approvalStatus(ApprovalStatus.PENDING)
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .entityReference(request.getEntityReference())
                .caseId(request.getCaseId())
                .loanAccountNumber(request.getLoanAccountNumber())
                .customerName(request.getCustomerName())
                .requestedAmount(request.getRequestedAmount())
                .requestedPercentage(request.getRequestedPercentage())
                .requestReason(request.getRequestReason())
                .requestDetails(request.getRequestDetails())
                .requestedBy(userId)
                .currentLevel(1)
                .maxLevels(matrix != null ? matrix.getApprovalLevel() : 1)
                .currentApproverRoleId(matrix != null ? matrix.getApproverRoleId() : null)
                .currentApproverUserId(matrix != null ? matrix.getApproverUserId() : null)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApprovalRequestDTO getApprovalRequestById(Long id) {
        ApprovalRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Approval request not found with id: " + id));
        return mapRequestToDTO(request);
    }

    @Override
    @Transactional(readOnly = true)
    public ApprovalRequestDTO getApprovalRequestByNumber(String requestNumber) {
        ApprovalRequest request = requestRepository.findByRequestNumber(requestNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Approval request not found with number: " + requestNumber));
        return mapRequestToDTO(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalRequestDTO> getApprovalRequestsByCaseId(Long caseId) {
        return requestRepository.findByCaseId(caseId)
                .stream()
                .map(this::mapRequestToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApprovalRequestDTO> getPendingApprovalsByRole(Long roleId, Pageable pageable) {
        return requestRepository.findPendingByApproverRole(roleId, pageable)
                .map(this::mapRequestToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApprovalRequestDTO> getPendingApprovalsByUser(Long userId, Pageable pageable) {
        return requestRepository.findPendingByApproverUser(userId, pageable)
                .map(this::mapRequestToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApprovalRequestDTO> getMyRequests(Long userId, Pageable pageable) {
        return requestRepository.findByRequester(userId, pageable)
                .map(this::mapRequestToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApprovalRequestDTO> searchApprovalRequests(ApprovalType type, ApprovalStatus status,
                                                            Long caseId, Pageable pageable) {
        return requestRepository.findWithFilters(type, status, caseId, pageable)
                .map(this::mapRequestToDTO);
    }

    // ==================== APPROVAL ACTIONS ====================

    @Override
    @Transactional
    public ApprovalRequestDTO processApproval(Long requestId, ApprovalActionRequest request, Long userId) {
        return switch (request.getAction().toUpperCase()) {
            case "APPROVE" -> approveRequest(requestId, request.getRemarks(), request.getApprovedAmount(), userId);
            case "REJECT" -> rejectRequest(requestId, request.getRejectionReason(), userId);
            case "ESCALATE" -> escalateRequest(requestId, request.getEscalationReason(), userId);
            default -> throw new BusinessException("Invalid action: " + request.getAction());
        };
    }

    @Override
    @Transactional
    public ApprovalRequestDTO approveRequest(Long requestId, String remarks, BigDecimal approvedAmount, Long userId) {
        ApprovalRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval request not found with id: " + requestId));

        if (request.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException("Request is not in pending status");
        }

        ApprovalStatus oldStatus = request.getApprovalStatus();
        request.setApprovalStatus(ApprovalStatus.APPROVED);
        request.setApprovedBy(userId);
        request.setApprovedAt(LocalDateTime.now());
        request.setApprovedAmount(approvedAmount != null ? approvedAmount : request.getRequestedAmount());
        request.setApprovalRemarks(remarks);

        ApprovalRequest saved = requestRepository.save(request);
        createHistory(saved, "APPROVED", oldStatus, ApprovalStatus.APPROVED, userId, null, remarks, approvedAmount);

        auditLogService.logApprovalAction("ApprovalRequest", saved.getId(),
                saved.getRequestNumber(), "APPROVED", "User:" + userId, remarks);

        log.info("Approved request: {} by user: {}", saved.getRequestNumber(), userId);
        return mapRequestToDTO(saved);
    }

    @Override
    @Transactional
    public ApprovalRequestDTO rejectRequest(Long requestId, String reason, Long userId) {
        ApprovalRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval request not found with id: " + requestId));

        if (request.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException("Request is not in pending status");
        }

        ApprovalStatus oldStatus = request.getApprovalStatus();
        request.setApprovalStatus(ApprovalStatus.REJECTED);
        request.setRejectedBy(userId);
        request.setRejectedAt(LocalDateTime.now());
        request.setRejectionReason(reason);

        ApprovalRequest saved = requestRepository.save(request);
        createHistory(saved, "REJECTED", oldStatus, ApprovalStatus.REJECTED, userId, null, reason, null);

        auditLogService.logApprovalAction("ApprovalRequest", saved.getId(),
                saved.getRequestNumber(), "REJECTED", "User:" + userId, reason);

        log.info("Rejected request: {} by user: {}", saved.getRequestNumber(), userId);
        return mapRequestToDTO(saved);
    }

    @Override
    @Transactional
    public ApprovalRequestDTO escalateRequest(Long requestId, String reason, Long userId) {
        ApprovalRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval request not found with id: " + requestId));

        if (request.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException("Request is not in pending status");
        }

        ApprovalStatus oldStatus = request.getApprovalStatus();
        request.setApprovalStatus(ApprovalStatus.ESCALATED);
        request.setEscalatedAt(LocalDateTime.now());
        request.setEscalationReason(reason);
        request.setCurrentLevel(request.getCurrentLevel() + 1);

        ApprovalRequest saved = requestRepository.save(request);
        createHistory(saved, "ESCALATED", oldStatus, ApprovalStatus.ESCALATED, userId, null, reason, null);

        auditLogService.logApprovalAction("ApprovalRequest", saved.getId(),
                saved.getRequestNumber(), "ESCALATED", "User:" + userId, reason);

        log.info("Escalated request: {} by user: {}", saved.getRequestNumber(), userId);
        return mapRequestToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalHistoryDTO> getApprovalHistory(Long requestId) {
        return historyRepository.findByApprovalRequestIdOrderByActionTimestampAsc(requestId)
                .stream()
                .map(this::mapHistoryToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Long countPendingByRole(Long roleId) {
        return requestRepository.countPendingByRole(roleId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countPendingByUser(Long userId) {
        return requestRepository.countPendingByUser(userId);
    }

    @Override
    @Transactional
    public void processExpiredRequests() {
        List<ApprovalRequest> expiredRequests = requestRepository.findExpiredPendingRequests(LocalDateTime.now());

        for (ApprovalRequest request : expiredRequests) {
            request.setApprovalStatus(ApprovalStatus.EXPIRED);
            requestRepository.save(request);

            createHistory(request, "EXPIRED", ApprovalStatus.PENDING, ApprovalStatus.EXPIRED,
                    null, "System", "Request expired due to timeout", null);

            auditLogService.logStatusChange("ApprovalRequest", request.getId(),
                    request.getRequestNumber(), "PENDING", "EXPIRED");

            log.info("Expired approval request: {}", request.getRequestNumber());
        }

        if (!expiredRequests.isEmpty()) {
            log.info("Processed {} expired approval requests", expiredRequests.size());
        }
    }

    // ==================== HELPER METHODS ====================

    private void createHistory(ApprovalRequest request, String action, ApprovalStatus fromStatus,
                               ApprovalStatus toStatus, Long actorId, String actorName,
                               String remarks, BigDecimal approvedAmount) {
        ApprovalHistory history = ApprovalHistory.builder()
                .approvalRequest(request)
                .action(action)
                .actionLevel(request.getCurrentLevel())
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .actorId(actorId)
                .actorName(actorName)
                .remarks(remarks)
                .approvedAmount(approvedAmount)
                .actionTimestamp(LocalDateTime.now())
                .build();
        historyRepository.save(history);
    }

    private ApprovalMatrixDTO mapMatrixToDTO(ApprovalMatrix matrix) {
        return ApprovalMatrixDTO.builder()
                .id(matrix.getId())
                .matrixCode(matrix.getMatrixCode())
                .matrixName(matrix.getMatrixName())
                .approvalType(matrix.getApprovalType())
                .description(matrix.getDescription())
                .minAmount(matrix.getMinAmount())
                .maxAmount(matrix.getMaxAmount())
                .minPercentage(matrix.getMinPercentage())
                .maxPercentage(matrix.getMaxPercentage())
                .approvalLevel(matrix.getApprovalLevel())
                .approverRoleId(matrix.getApproverRoleId())
                .approverRoleName(matrix.getApproverRoleName())
                .approverUserId(matrix.getApproverUserId())
                .approverUserName(matrix.getApproverUserName())
                .escalationHours(matrix.getEscalationHours())
                .escalationLevel(matrix.getEscalationLevel())
                .escalationRoleId(matrix.getEscalationRoleId())
                .autoApproveEnabled(matrix.getAutoApproveEnabled())
                .autoApproveBelowAmount(matrix.getAutoApproveBelowAmount())
                .criteria(matrix.getCriteria())
                .isActive(matrix.getIsActive())
                .priorityOrder(matrix.getPriorityOrder())
                .createdAt(matrix.getCreatedAt())
                .updatedAt(matrix.getUpdatedAt())
                .build();
    }

    private ApprovalRequestDTO mapRequestToDTO(ApprovalRequest request) {
        return ApprovalRequestDTO.builder()
                .id(request.getId())
                .requestNumber(request.getRequestNumber())
                .approvalType(request.getApprovalType())
                .approvalStatus(request.getApprovalStatus())
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .entityReference(request.getEntityReference())
                .caseId(request.getCaseId())
                .loanAccountNumber(request.getLoanAccountNumber())
                .customerName(request.getCustomerName())
                .requestedAmount(request.getRequestedAmount())
                .requestedPercentage(request.getRequestedPercentage())
                .requestReason(request.getRequestReason())
                .requestDetails(request.getRequestDetails())
                .requestedBy(request.getRequestedBy())
                .requestedByName(request.getRequestedByName())
                .requestedAt(request.getRequestedAt())
                .currentLevel(request.getCurrentLevel())
                .maxLevels(request.getMaxLevels())
                .currentApproverRoleId(request.getCurrentApproverRoleId())
                .currentApproverUserId(request.getCurrentApproverUserId())
                .approvedBy(request.getApprovedBy())
                .approvedByName(request.getApprovedByName())
                .approvedAt(request.getApprovedAt())
                .approvedAmount(request.getApprovedAmount())
                .approvalRemarks(request.getApprovalRemarks())
                .rejectedBy(request.getRejectedBy())
                .rejectedByName(request.getRejectedByName())
                .rejectedAt(request.getRejectedAt())
                .rejectionReason(request.getRejectionReason())
                .escalatedAt(request.getEscalatedAt())
                .escalationReason(request.getEscalationReason())
                .expiresAt(request.getExpiresAt())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }

    private ApprovalHistoryDTO mapHistoryToDTO(ApprovalHistory history) {
        return ApprovalHistoryDTO.builder()
                .id(history.getId())
                .approvalRequestId(history.getApprovalRequest().getId())
                .action(history.getAction())
                .actionLevel(history.getActionLevel())
                .fromStatus(history.getFromStatus())
                .toStatus(history.getToStatus())
                .actorId(history.getActorId())
                .actorName(history.getActorName())
                .actorRole(history.getActorRole())
                .remarks(history.getRemarks())
                .approvedAmount(history.getApprovedAmount())
                .metadata(history.getMetadata())
                .actionTimestamp(history.getActionTimestamp())
                .build();
    }
}
