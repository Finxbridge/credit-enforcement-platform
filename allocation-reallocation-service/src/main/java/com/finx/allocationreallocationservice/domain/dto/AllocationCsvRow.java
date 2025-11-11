package com.finx.allocationreallocationservice.domain.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class AllocationCsvRow {

    @CsvBindByName(column = "case_id", required = true)
    private String caseId;

    @CsvBindByName(column = "external_case_id")
    private String externalCaseId;

    @CsvBindByName(column = "loan_account_number")
    private String loanAccountNumber;

    @CsvBindByName(column = "customer_name")
    private String customerName;

    @CsvBindByName(column = "primary_agent_id", required = true)
    private String primaryAgentId;

    @CsvBindByName(column = "secondary_agent_id")
    private String secondaryAgentId;

    @CsvBindByName(column = "allocation_type")
    private String allocationType;

    @CsvBindByName(column = "allocation_percentage")
    private String allocationPercentage;

    @CsvBindByName(column = "geography")
    private String geography;

    @CsvBindByName(column = "bucket")
    private String bucket;

    @CsvBindByName(column = "priority")
    private String priority;

    @CsvBindByName(column = "remarks")
    private String remarks;
}
