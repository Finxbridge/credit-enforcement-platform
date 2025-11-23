package com.finx.casesourcingservice.domain.dto.csv;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a single row in the case upload CSV
 * Contains only required fields for simplified case upload
 * Uses OpenCSV annotations for flexible column mapping
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseCsvRowDTO {

    private Integer rowNumber;

    // Case Information
    @CsvBindByName(column = "case_id", required = true)
    private String externalCaseId;

    @CsvBindByName(column = "loan_id", required = true)
    private String loanAccountNumber;

    // Customer Information
    @CsvBindByName(column = "customer_code", required = true)
    private String customerCode;

    @CsvBindByName(column = "customer_name", required = true)
    private String fullName;

    @CsvBindByName(column = "phone", required = true)
    private String mobileNumber;

    @CsvBindByName(column = "geography", required = true)
    private String geographyCode;

    @CsvBindByName(column = "language", required = true)
    private String language;

    // Loan Financial Details
    @CsvBindByName(column = "outstanding", required = true)
    private String totalOutstanding;

    @CsvBindByName(column = "dpd", required = true)
    private String dpd; // String for CSV parsing, will convert to Integer
}
