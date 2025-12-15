package com.finx.dmsservice.domain.dto;

import com.finx.dmsservice.domain.enums.NoticeStatus;
import com.finx.dmsservice.domain.enums.NoticeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeSearchCriteria {

    private String noticeNumber;
    private NoticeType noticeType;
    private NoticeStatus noticeStatus;
    private Long caseId;
    private String loanAccountNumber;
    private String customerName;
    private Long customerId;

    // DPD Range
    private Integer minDpd;
    private Integer maxDpd;
    private String bucket;

    // Amount Range
    private BigDecimal minAmount;
    private BigDecimal maxAmount;

    // Date Range
    private LocalDate generatedDateFrom;
    private LocalDate generatedDateTo;
    private LocalDate dispatchedDateFrom;
    private LocalDate dispatchedDateTo;
    private LocalDate deliveredDateFrom;
    private LocalDate deliveredDateTo;

    // Location
    private String region;
    private String state;
    private String city;
    private String pincode;

    // Product
    private String productType;
    private String productName;

    // Template
    private Long templateId;

    // Vendor
    private Long dispatchVendorId;

    // Response
    private Boolean hasResponse;
    private Boolean isOverdue;
}
