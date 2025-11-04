package com.finx.casesourcingservice.domain.dto.csv;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO representing a single row in the case upload CSV
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseCsvRowDTO {

    private Integer rowNumber;

    // Case Information
    private String externalCaseId;
    private String loanAccountNumber;

    // Customer Information
    private String customerCode;
    private String fullName;
    private String mobileNumber;
    private String alternateMobile;
    private String email;
    private String address;
    private String city;
    private String state;
    private String pincode;

    // Loan Financial Details
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private BigDecimal penaltyAmount;
    private BigDecimal totalOutstanding;
    private BigDecimal emiAmount;
    private Integer dpd;
    private String bucket;

    // Additional Information
    private String productType;
    private String productCode;
    private String bankCode;
    private String disbursementDate;  // String for CSV parsing, will convert to LocalDate
    private String dueDate;           // String for CSV parsing, will convert to LocalDate
}
