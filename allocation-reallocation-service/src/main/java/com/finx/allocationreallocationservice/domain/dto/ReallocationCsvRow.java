package com.finx.allocationreallocationservice.domain.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class ReallocationCsvRow {

    @CsvBindByName(column = "case_id", required = true)
    private String caseId;

    @CsvBindByName(column = "external_case_id")
    private String externalCaseId;

    @CsvBindByName(column = "loan_account_number")
    private String loanAccountNumber;

    @CsvBindByName(column = "current_agent_id", required = true)
    private String currentAgentId;

    @CsvBindByName(column = "new_agent_id", required = true)
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
}
