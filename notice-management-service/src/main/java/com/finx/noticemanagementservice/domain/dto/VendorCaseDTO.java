package com.finx.noticemanagementservice.domain.dto;

import com.finx.noticemanagementservice.domain.enums.DispatchStatus;
import com.finx.noticemanagementservice.domain.enums.NoticeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorCaseDTO {

    private Long dispatchId;
    private String trackingId;

    // Notice Details
    private Long noticeId;
    private String noticeNumber;
    private NoticeType noticeType;
    private String noticeSubtype;

    // Customer Details
    private Long caseId;
    private String loanAccountNumber;
    private String customerName;

    // Recipient Details
    private String recipientName;
    private String recipientAddress;
    private String recipientCity;
    private String recipientState;
    private String recipientPincode;

    // Dispatch Details
    private DispatchStatus dispatchStatus;
    private String trackingNumber;
    private String carrierName;
    private String serviceType;
    private LocalDateTime createdAt;
    private LocalDateTime dispatchedAt;
    private LocalDateTime expectedDispatchBy;
    private LocalDateTime expectedDeliveryBy;
    private LocalDateTime deliveredAt;

    // SLA Info
    private Boolean dispatchSlaBreached;
    private Boolean deliverySlaBreached;
    private Long hoursToDispatchSla;
    private Long daysToDeliverySla;

    // Document Info
    private String documentUrl;
    private Integer pageCount;
}
