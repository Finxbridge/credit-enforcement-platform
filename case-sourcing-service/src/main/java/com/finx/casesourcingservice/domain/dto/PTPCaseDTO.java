package com.finx.casesourcingservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for cases with PTP information
 * Used for PTP due list and broken PTP list
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PTPCaseDTO {

    // Case Information
    private Long caseId;
    private String caseNumber;
    private String loanAccountNumber;
    private String externalCaseId;
    private String caseStatus;

    // Customer Information
    private String customerName;
    private String mobileNumber;
    private String email;
    private String city;
    private String state;

    // Loan Information
    private BigDecimal totalOutstanding;
    private Integer dpd;
    private String bucket;
    private String productType;

    // PTP Information
    private Long ptpId;
    private LocalDate ptpDate;
    private BigDecimal ptpAmount;
    private String ptpStatus;
    private LocalDate commitmentDate;
    private String ptpNotes;
    private Long daysOverdue;
    private Boolean reminderSent;

    // Agent Information
    private Long userId;
    private String userName;
}
