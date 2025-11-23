package com.finx.casesourcingservice.domain.dto;

import com.finx.casesourcingservice.domain.enums.PTPStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for PTP operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PTPResponse {

    private Long id;
    private Long caseId;
    private Long userId;
    private String userName;
    private LocalDate ptpDate;
    private BigDecimal ptpAmount;
    private LocalDateTime commitmentDate;
    private PTPStatus ptpStatus;
    private BigDecimal paymentReceivedAmount;
    private LocalDate paymentReceivedDate;
    private String brokenReason;
    private String notes;
    private Boolean reminderSent;
    private LocalDateTime reminderSentAt;
    private LocalDate followUpDate;
    private Boolean followUpCompleted;
    private String callDisposition;
    private LocalDateTime createdAt;
    private Long daysSinceCommitment;
    private Long daysUntilDue;
    private Boolean isOverdue;
}
