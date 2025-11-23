package com.finx.casesourcingservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for advanced case search
 * FR-CS-5: Multi-field case search
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseSearchRequest {

    // Case identifiers
    private String caseNumber;
    private String externalCaseId;
    private String loanAccountNumber;

    // Customer information
    private String customerName;
    private String customerCode;
    private String mobileNumber;
    private String email;

    // Location filters
    private String city;
    private String state;
    private String pincode;
    private String geographyCode;

    // Loan filters
    private String productType;
    private String bankCode;
    private String bucket;
    private Integer minDpd;
    private Integer maxDpd;
    private Double minOutstanding;
    private Double maxOutstanding;

    // Case status filters
    private String caseStatus;
    private String ptpStatus;
    private Long allocatedToUserId;
    private Long allocatedToAgencyId;

    // Date filters
    private String caseOpenedFrom;
    private String caseOpenedTo;
    private String ptpDateFrom;
    private String ptpDateTo;
}
