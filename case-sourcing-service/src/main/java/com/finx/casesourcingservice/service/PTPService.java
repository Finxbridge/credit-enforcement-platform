package com.finx.casesourcingservice.service;

import com.finx.casesourcingservice.domain.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for PTP (Promise to Pay) management
 * Functional Requirements: FR-PTP-1, FR-PTP-2, FR-PTP-3
 */
public interface PTPService {

    /**
     * FR-PTP-1: Capture PTP commitment from borrower
     *
     * @param request PTP capture request
     * @return PTPResponse with created PTP details
     */
    PTPResponse capturePTP(CapturePTPRequest request);

    /**
     * FR-PTP-2: Get all PTPs due on a specific date
     *
     * @param dueDate Date to check for due PTPs
     * @param userId  Optional user filter
     * @return List of cases with PTPs due on the specified date
     */
    List<PTPCaseDTO> getPTPsDue(LocalDate dueDate, Long userId);

    /**
     * FR-PTP-3: Get all broken PTPs (past due date with PENDING status)
     *
     * @param userId Optional user filter
     * @return List of cases with broken PTPs requiring follow-up
     */
    List<PTPCaseDTO> getBrokenPTPs(Long userId);

    /**
     * Get PTP details by ID
     *
     * @param ptpId PTP commitment ID
     * @return PTPResponse with PTP details
     */
    PTPResponse getPTPById(Long ptpId);

    /**
     * Get all PTPs for a specific case
     *
     * @param caseId Case ID
     * @return List of PTP commitments for the case
     */
    List<PTPResponse> getPTPsByCase(Long caseId);

    /**
     * Update PTP status (mark as KEPT, BROKEN, RENEWED, etc.)
     *
     * @param ptpId   PTP commitment ID
     * @param request Update request with new status
     * @return Updated PTPResponse
     */
    PTPResponse updatePTPStatus(Long ptpId, UpdatePTPRequest request);

    /**
     * Get PTP statistics for a user
     *
     * @param userId User ID
     * @return Statistics including pending, kept, broken counts
     */
    PTPStatsDTO getPTPStats(Long userId);

    /**
     * Get PTPs paginated by status
     *
     * @param status   PTP status filter
     * @param pageable Pagination parameters
     * @return Page of PTP commitments
     */
    Page<PTPResponse> getPTPsByStatus(String status, Pageable pageable);

    /**
     * Process broken PTPs (auto-mark as BROKEN when past due date)
     * Should be run by scheduled job daily
     *
     * @return Count of PTPs marked as broken
     */
    Integer processOverduePTPs();

    /**
     * Send PTP reminders (1 day before due date)
     * Should be run by scheduled job daily
     *
     * @return Count of reminders sent
     */
    Integer sendPTPReminders();
}
