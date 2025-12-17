package com.finx.collectionsservice.domain.dto;

import com.finx.collectionsservice.domain.enums.OTSStatus;
import com.finx.collectionsservice.domain.enums.PaymentMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OTSRequestDTO {

    private Long id;
    private String otsNumber;
    private Long caseId;
    private String caseNumber;
    private String loanAccountNumber;
    private String customerName;
    private BigDecimal originalOutstanding;
    private BigDecimal proposedSettlement;
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;
    private PaymentMode paymentMode;
    private Integer installmentCount;
    private LocalDate paymentDeadline;
    private OTSStatus otsStatus;
    private Integer currentApprovalLevel;
    private Integer maxApprovalLevel;
    private Boolean borrowerConsent;
    private LocalDateTime intentCapturedAt;
    private Long intentCapturedBy;
    private String intentCapturedByName;
    private LocalDateTime settledAt;
    private BigDecimal settledAmount;
    private LocalDateTime createdAt;
}
