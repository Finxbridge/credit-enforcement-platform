package com.finx.casesourcingservice.domain.dto.csv;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a single row in the case upload CSV
 * Uses OpenCSV annotations for flexible column mapping
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseCsvRowDTO {

    private Integer rowNumber;

    // Case Information
    @CsvBindByName(column = "externalCaseId", required = true)
    private String externalCaseId;

    @CsvBindByName(column = "loanAccountNumber", required = true)
    private String loanAccountNumber;

    // Customer Information
    @CsvBindByName(column = "customerCode", required = true)
    private String customerCode;

    @CsvBindByName(column = "fullName", required = true)
    private String fullName;

    @CsvBindByName(column = "mobileNumber", required = true)
    private String mobileNumber;

    @CsvBindByName(column = "alternateMobile")
    private String alternateMobile;

    @CsvBindByName(column = "email")
    private String email;

    @CsvBindByName(column = "address")
    private String address;

    @CsvBindByName(column = "city")
    private String city;

    @CsvBindByName(column = "state")
    private String state;

    @CsvBindByName(column = "pincode")
    private String pincode;

    @CsvBindByName(column = "geographyCode", required = true)
    private String geographyCode;

    // Loan Financial Details
    @CsvBindByName(column = "principalAmount")

    @CsvBindByName(column = "totalOutstanding", required = true)

    @CsvBindByName(column = "dpd", required = true)
    private String dpd; // String for CSV parsing, will convert to Integer

    @CsvBindByName(column = "bucket")
    private String bucket;

    // Additional Information
    @CsvBindByName(column = "productType")
    private String productType;

    @CsvBindByName(column = "bankCode")
    private String bankCode;

    @CsvBindByName(column = "disbursementDate")
    private String disbursementDate; // String for CSV parsing, will convert to LocalDate

    @CsvBindByName(column = "dueDate")
    private String dueDate; // String for CSV parsing, will convert to LocalDate
}
