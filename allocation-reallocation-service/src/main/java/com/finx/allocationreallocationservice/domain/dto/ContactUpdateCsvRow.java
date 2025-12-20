package com.finx.allocationreallocationservice.domain.dto;

import com.finx.allocationreallocationservice.domain.enums.ContactUpdateType;
import com.opencsv.bean.CsvBindByName;
import lombok.Data;

/**
 * Unified CSV row for contact updates
 * Supports MOBILE_UPDATE, EMAIL_UPDATE, and ADDRESS_UPDATE
 * Uses loan_id for consistency with case sourcing and allocation CSVs
 */
@Data
public class ContactUpdateCsvRow {

    @CsvBindByName(column = "loan_id", required = true)
    private String loanId;

    @CsvBindByName(column = "update_type", required = true)
    private String updateType;

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

    @CsvBindByName(column = "remarks")
    private String remarks;
}
