package com.finx.collectionsservice.service;

import com.finx.collectionsservice.domain.dto.*;
import com.finx.collectionsservice.domain.enums.ApprovalStatus;
import com.finx.collectionsservice.domain.enums.ApprovalType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ApprovalService {

    // Matrix Management
    ApprovalMatrixDTO createMatrix(CreateApprovalMatrixRequest request, Long userId);
    ApprovalMatrixDTO getMatrixById(Long id);
    ApprovalMatrixDTO getMatrixByCode(String matrixCode);
    List<ApprovalMatrixDTO> getActiveMatrices();
    List<ApprovalMatrixDTO> getMatricesByType(ApprovalType type);
    Page<ApprovalMatrixDTO> getAllMatrices(ApprovalType type, Boolean isActive, Pageable pageable);
    ApprovalMatrixDTO updateMatrix(Long id, CreateApprovalMatrixRequest request, Long userId);
    ApprovalMatrixDTO activateMatrix(Long id, Long userId);
    ApprovalMatrixDTO deactivateMatrix(Long id, Long userId);
    void deleteMatrix(Long id, Long userId);

    // Approval Request Management
    ApprovalRequestDTO createApprovalRequest(CreateApprovalRequest request, Long userId);
    ApprovalRequestDTO getApprovalRequestById(Long id);
    ApprovalRequestDTO getApprovalRequestByNumber(String requestNumber);
    List<ApprovalRequestDTO> getApprovalRequestsByCaseId(Long caseId);
    Page<ApprovalRequestDTO> getPendingApprovalsByRole(Long roleId, Pageable pageable);
    Page<ApprovalRequestDTO> getPendingApprovalsByUser(Long userId, Pageable pageable);
    Page<ApprovalRequestDTO> getMyRequests(Long userId, Pageable pageable);
    Page<ApprovalRequestDTO> searchApprovalRequests(ApprovalType type, ApprovalStatus status, Long caseId, Pageable pageable);

    // Approval Actions
    ApprovalRequestDTO processApproval(Long requestId, ApprovalActionRequest request, Long userId);
    ApprovalRequestDTO approveRequest(Long requestId, String remarks, java.math.BigDecimal approvedAmount, Long userId);
    ApprovalRequestDTO rejectRequest(Long requestId, String reason, Long userId);
    ApprovalRequestDTO escalateRequest(Long requestId, String reason, Long userId);

    // Approval History
    List<ApprovalHistoryDTO> getApprovalHistory(Long requestId);

    // Counts
    Long countPendingByRole(Long roleId);
    Long countPendingByUser(Long userId);

    // Auto-expiry
    void processExpiredRequests();
}
