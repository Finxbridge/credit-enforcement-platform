package com.finx.allocationreallocationservice.domain.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

/**
 * DTO for reallocation CSV upload.
 * Supports two formats:
 * 1. New format: ACCOUNT NO, PRIMARY AGENT (new agent), SECONDARY AGENT
 * 2. Legacy format: case_id, current_agent_id, new_agent_id
 *
 * For the new format, the system will lookup case by ACCOUNT NO and resolve agents by ID or username.
 */
@Data
public class ReallocationCsvRow {

    // Internal tracking
    private Integer rowNumber;

    // ==================== NEW FORMAT (matches case-sourcing CSV) ====================

    @CsvBindByName(column = "ACCOUNT NO")
    private String accountNo;

    @CsvBindByName(column = "PRIMARY AGENT")
    private String primaryAgent;

    @CsvBindByName(column = "SECONDARY AGENT")
    private String secondaryAgent;

    // ==================== LEGACY FORMAT ====================

    @CsvBindByName(column = "case_id")
    private String caseId;

    @CsvBindByName(column = "external_case_id")
    private String externalCaseId;

    @CsvBindByName(column = "loan_account_number")
    private String loanAccountNumber;

    @CsvBindByName(column = "current_agent_id")
    private String currentAgentId;

    @CsvBindByName(column = "new_agent_id")
    private String newAgentId;

    @CsvBindByName(column = "reallocation_reason")
    private String reallocationReason;

    @CsvBindByName(column = "reallocation_type")
    private String reallocationType;

    @CsvBindByName(column = "effective_date")
    private String effectiveDate;

    @CsvBindByName(column = "priority")
    private String priority;

    @CsvBindByName(column = "remarks")
    private String remarks;

    // ==================== ADDITIONAL FIELDS FROM CASE-SOURCING CSV ====================
    // These are optional and used when reusing the case-sourcing CSV format

    @CsvBindByName(column = "LENDER")
    private String lender;

    @CsvBindByName(column = "CUSTOMER NAME")
    private String customerName;

    @CsvBindByName(column = "MOBILE NO")
    private String mobileNo;

    @CsvBindByName(column = "OVERDUE AMOUNT")
    private String overdueAmount;

    @CsvBindByName(column = "DPD")
    private String dpd;

    @CsvBindByName(column = "PRODUCT")
    private String product;

    @CsvBindByName(column = "LOCATION")
    private String location;

    @CsvBindByName(column = "STATUS")
    private String status;

    @CsvBindByName(column = "REMARKS")
    private String remarksFromCsv;

    /**
     * Check if this row uses the new case-sourcing format
     */
    public boolean isNewFormat() {
        return accountNo != null && !accountNo.trim().isEmpty() &&
               primaryAgent != null && !primaryAgent.trim().isEmpty();
    }

    /**
     * Get the effective account/loan identifier
     */
    public String getEffectiveAccountNo() {
        if (accountNo != null && !accountNo.trim().isEmpty()) {
            return accountNo;
        }
        if (loanAccountNumber != null && !loanAccountNumber.trim().isEmpty()) {
            return loanAccountNumber;
        }
        return null;
    }
}
