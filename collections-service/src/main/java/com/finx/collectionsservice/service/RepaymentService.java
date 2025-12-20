package com.finx.collectionsservice.service;

import com.finx.collectionsservice.domain.dto.*;
import com.finx.collectionsservice.domain.enums.RepaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for Repayment management
 */
public interface RepaymentService {

    // ==================== Core Repayment APIs ====================

    /**
     * Create a new repayment (cash/cheque manual capture)
     */
    RepaymentDTO createRepayment(CreateRepaymentRequest request, Long userId);

    /**
     * Get repayment by ID
     */
    RepaymentDTO getRepayment(Long repaymentId);

    /**
     * Get repayment by repayment number
     */
    RepaymentDTO getRepaymentByNumber(String repaymentNumber);

    /**
     * Get all repayments for a case
     */
    List<RepaymentDTO> getCaseRepayments(Long caseId);

    /**
     * Get repayments by status with pagination
     */
    Page<RepaymentDTO> getRepaymentsByStatus(RepaymentStatus status, Pageable pageable);

    /**
     * Search repayments with filters
     */
    Page<RepaymentDTO> searchRepayments(String searchTerm, RepaymentStatus status,
                                         LocalDate fromDate, LocalDate toDate, Pageable pageable);

    // ==================== Approval APIs (Maker-Checker) ====================

    /**
     * Approve a repayment
     */
    RepaymentDTO approveRepayment(Long repaymentId, Long approverId, String comments);

    /**
     * Reject a repayment
     */
    RepaymentDTO rejectRepayment(Long repaymentId, Long approverId, String reason);

    /**
     * Get pending approvals
     */
    Page<RepaymentDTO> getPendingApprovals(Pageable pageable);

    // ==================== Dashboard APIs ====================

    /**
     * Get repayment dashboard statistics
     */
    RepaymentDashboardDTO getDashboardStats();

    /**
     * Get SLA monitoring dashboard statistics
     */
    SlaDashboardDTO getSlaDashboardStats();

    /**
     * Get SLA breached repayments
     */
    List<RepaymentDTO> getSlaBreachedRepayments();

    // ==================== Reconciliation APIs ====================

    /**
     * Get pending reconciliation list
     */
    Page<ReconciliationDTO> getPendingReconciliation(Pageable pageable);

    /**
     * Update reconciliation status
     */
    ReconciliationDTO updateReconciliationStatus(ReconciliationUpdateRequest request, Long userId);

    /**
     * Bulk reconcile repayments
     */
    List<ReconciliationDTO> bulkReconcile(BulkReconciliationRequest request, Long userId);

    // ==================== Partial Payment APIs ====================

    /**
     * Record partial payment adjustment
     */
    RepaymentDTO recordPartialPayment(PartialPaymentRequest request, Long userId);

    // ==================== Receipt APIs ====================

    /**
     * Generate receipt for a repayment
     */
    byte[] generateReceipt(Long repaymentId);

    /**
     * Get receipt by repayment ID (returns receipt details/metadata)
     */
    RepaymentDTO getReceiptDetails(Long repaymentId);
}
