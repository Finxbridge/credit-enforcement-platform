package com.finx.collectionsservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for case search results used in OTS creation
 * Contains all fields needed to auto-populate OTS create form
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OTSCaseSearchDTO {

    // Case Details
    private Long caseId;
    private String caseNumber;
    private String caseStatus;
    private String casePriority;

    // Customer Details
    private Long customerId;
    private String customerName;
    private String mobileNumber;
    private String emailAddress;
    private String address;
    private String city;
    private String state;
    private String pincode;

    // Loan Details
    private Long loanId;
    private String loanAccountNumber;
    private String productType;
    private String bankCode;

    // Outstanding Details
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private BigDecimal penaltyAmount;
    private BigDecimal totalOutstanding;
    private BigDecimal emiAmount;

    // Risk Details
    private Integer dpd;
    private String bucket;
    private LocalDate dueDate;

    // Allocation Details
    private Long allocatedToUserId;
    private Long allocatedToAgencyId;
    private String collectionCycle;
}
