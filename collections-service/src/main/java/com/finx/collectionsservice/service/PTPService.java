package com.finx.collectionsservice.service;

import com.finx.collectionsservice.domain.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for PTP (Promise to Pay) management
 */
public interface PTPService {

    /**
     * Capture PTP commitment from borrower
     */
    PTPResponse capturePTP(CapturePTPRequest request);

    /**
     * Get all PTPs due on a specific date
     */
    List<PTPCaseDTO> getPTPsDue(LocalDate dueDate, Long userId);

    /**
     * Get all broken PTPs (past due date with PENDING status)
     */
    List<PTPCaseDTO> getBrokenPTPs(Long userId);

    /**
     * Get PTP details by ID
     */
    PTPResponse getPTPById(Long ptpId);

    /**
     * Get all PTPs for a specific case
     */
    List<PTPResponse> getPTPsByCase(Long caseId);

    /**
     * Update PTP status
     */
    PTPResponse updatePTPStatus(Long ptpId, UpdatePTPRequest request);

    /**
     * Get PTP statistics for a user
     */
    PTPStatsDTO getPTPStats(Long userId);

    /**
     * Get PTPs paginated by status
     */
    Page<PTPResponse> getPTPsByStatus(String status, Pageable pageable);

    /**
     * Process broken PTPs (auto-mark as BROKEN when past due date)
     */
    Integer processOverduePTPs();

    /**
     * Send PTP reminders
     */
    Integer sendPTPReminders();
}
