package com.finx.myworkflow.service;

import com.finx.myworkflow.domain.dto.AllocationHistoryDTO;
import com.finx.myworkflow.domain.dto.AuditLogDTO;
import com.finx.myworkflow.domain.dto.CaseEventDTO;
import com.finx.myworkflow.domain.dto.CaseSummaryDTO;
import com.finx.myworkflow.domain.dto.CaseTabsDataDTO;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface CaseDataService {

    /**
     * Get cases list for workflow - shows all cases for admin, allocated cases for collectors
     */
    Map<String, Object> getCasesForWorkflow(Long userId, boolean isAdmin, int page, int size);

    /**
     * Get case summary with key information
     */
    CaseSummaryDTO getCaseSummary(Long caseId, Long userId);

    /**
     * Get all tabs data for a case
     */
    CaseTabsDataDTO getAllTabsData(Long caseId, Long userId);

    /**
     * Get loan details tab data
     */
    Map<String, Object> getLoanDetails(Long caseId);

    /**
     * Get customer details tab data
     */
    Map<String, Object> getCustomerDetails(Long caseId);

    /**
     * Get repayment history for a case
     */
    List<Map<String, Object>> getRepaymentHistory(Long caseId);

    /**
     * Get PTP history for a case
     */
    List<Map<String, Object>> getPtpHistory(Long caseId);

    /**
     * Get notices for a case
     */
    List<Map<String, Object>> getNotices(Long caseId);

    /**
     * Get call logs for a case
     */
    List<Map<String, Object>> getCallLogs(Long caseId);

    /**
     * Get SMS history for a case
     */
    List<Map<String, Object>> getSmsHistory(Long caseId);

    /**
     * Get email history for a case
     */
    List<Map<String, Object>> getEmailHistory(Long caseId);

    /**
     * Get documents for a case
     */
    List<Map<String, Object>> getDocuments(Long caseId);

    /**
     * Get case events for a case with pagination
     */
    Page<CaseEventDTO> getCaseEvents(Long caseId, int page, int size);

    /**
     * Get case events for a case filtered by category
     */
    Page<CaseEventDTO> getCaseEventsByCategory(Long caseId, String category, int page, int size);

    /**
     * Get all case events for a case (no pagination)
     */
    List<CaseEventDTO> getAllCaseEvents(Long caseId);

    /**
     * Get allocation history for a case with pagination
     */
    Page<AllocationHistoryDTO> getAllocationHistory(Long caseId, int page, int size);

    /**
     * Get all allocation history for a case (no pagination)
     */
    List<AllocationHistoryDTO> getAllAllocationHistory(Long caseId);

    /**
     * Get audit logs for a case with pagination
     */
    Page<AuditLogDTO> getAuditLogs(Long caseId, int page, int size);

    /**
     * Get all audit logs for a case (no pagination)
     */
    List<AuditLogDTO> getAllAuditLogs(Long caseId);
}
