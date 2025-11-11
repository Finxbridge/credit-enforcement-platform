package com.finx.allocationreallocationservice.domain.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class ContactUpdateCsvRow {

    @CsvBindByName(column = "case_id", required = true)
    private String caseId;

    @CsvBindByName(column = "external_case_id")
    private String externalCaseId;

    @CsvBindByName(column = "loan_account_number")
    private String loanAccountNumber;

    @CsvBindByName(column = "customer_name")
    private String customerName;

    @CsvBindByName(column = "mobile_number")
    private String mobileNumber;

    @CsvBindByName(column = "alternate_mobile")
    private String alternateMobile;

    @CsvBindByName(column = "email")
    private String email;

    @CsvBindByName(column = "alternate_email")
    private String alternateEmail;

    @CsvBindByName(column = "address")
    private String address;

    @CsvBindByName(column = "city")
    private String city;

    @CsvBindByName(column = "state")
    private String state;

    @CsvBindByName(column = "pincode")
    private String pincode;

    @CsvBindByName(column = "update_type")
    private String updateType;

    @CsvBindByName(column = "remarks")
    private String remarks;
}
