package com.finx.myworkflow.domain.dto;

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
public class CaseSummaryDTO {

    private Long caseId;
    private String caseNumber;
    private String status;
    private String priority;

    // Customer Info
    private String customerName;
    private String customerId;
    private String primaryPhone;
    private String primaryEmail;

    // Loan Info
    private String loanAccountNumber;
    private String productType;
    private BigDecimal principalOutstanding;
    private BigDecimal totalDue;
    private Integer dpd;
    private String bucket;
    private LocalDate dueDate;
    private LocalDate nextDueDate;

    // Collection Info
    private BigDecimal totalCollected;
    private BigDecimal lastPaymentAmount;
    private LocalDate lastPaymentDate;
    private LocalDate lastContactDate;
    private String lastContactOutcome;

    // PTP Info
    private LocalDate activePtpDate;
    private BigDecimal activePtpAmount;
    private String ptpStatus;

    // Notice Info
    private Integer totalNotices;
    private String lastNoticeType;
    private LocalDate lastNoticeDate;

    // Assignment
    private String assignedAgent;
    private String assignedTeam;
    private LocalDateTime assignedAt;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
