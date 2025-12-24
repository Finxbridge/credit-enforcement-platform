package com.finx.myworkflow.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseTabsDataDTO {

    private Long caseId;

    // Loan Details Tab
    private Map<String, Object> loanDetails;

    // Customer Details Tab
    private Map<String, Object> customerDetails;
    private List<Map<String, Object>> contacts;
    private List<Map<String, Object>> addresses;

    // Collections Tab
    private List<Map<String, Object>> repayments;
    private List<Map<String, Object>> ptps;
    private Map<String, Object> collectionSummary;

    // Notice Tab
    private List<Map<String, Object>> notices;
    private List<Map<String, Object>> pods;

    // Communication Tab
    private List<Map<String, Object>> callLogs;
    private List<Map<String, Object>> smsHistory;
    private List<Map<String, Object>> emailHistory;

    // Documents Tab
    private List<Map<String, Object>> documents;

    // Events Tab - All case events
    private List<CaseEventDTO> events;

    // Allocation History Tab
    private List<AllocationHistoryDTO> allocationHistory;

    // Audit Trail Tab
    private List<AuditLogDTO> auditTrail;
}
