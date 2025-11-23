package com.finx.casesourcingservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for case search results
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseSearchResultDTO {

    // Case Information
    private Long caseId;
    private String caseNumber;
    private String externalCaseId;
    private String caseStatus;
    private String casePriority;
    private LocalDateTime caseOpenedAt;

    // Customer Information
    private String customerCode;
    private String customerName;
    private String mobileNumber;
    private String email;
    private String city;
    private String state;
    private String pincode;

    // Loan Information
    private String loanAccountNumber;
    private String productType;
    private String bankCode;
    private BigDecimal totalOutstanding;
    private Integer dpd;
    private String bucket;

    // Allocation Information
    private Long allocatedToUserId;
    private String allocatedToUserName;
    private LocalDateTime allocatedAt;

    // PTP Information
    private LocalDate ptpDate;
    private BigDecimal ptpAmount;
    private String ptpStatus;

    // Activity Summary
    private Integer totalActivities;
    private LocalDateTime lastContactedAt;
    private String lastDisposition;
}
