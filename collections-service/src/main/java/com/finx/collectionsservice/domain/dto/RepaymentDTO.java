package com.finx.collectionsservice.domain.dto;

import com.finx.collectionsservice.domain.enums.PaymentMode;
import com.finx.collectionsservice.domain.enums.RepaymentStatus;
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
public class RepaymentDTO {

    private Long id;
    private String repaymentNumber;
    private Long caseId;
    private String caseNumber;
    private String customerName;
    private BigDecimal paymentAmount;
    private LocalDate paymentDate;
    private PaymentMode paymentMode;
    private RepaymentStatus approvalStatus;
    private Integer currentApprovalLevel;
    private Long approvedBy;
    private String approverName;
    private LocalDateTime approvedAt;
    private String rejectionReason;
    private String correctionNotes;
    private String depositSlaStatus;
    private Integer depositSlaBreachHours;
    private Boolean isReconciled;
    private Long collectedBy;
    private String collectorName;
    private String collectionLocation;
    private String notes;
    private Boolean isOtsPayment;
    private LocalDateTime createdAt;
}
