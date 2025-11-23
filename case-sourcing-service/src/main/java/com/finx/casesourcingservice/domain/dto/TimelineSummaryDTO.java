package com.finx.casesourcingservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for case timeline summary statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineSummaryDTO {

    // Activity counts
    private Integer totalEvents;
    private Integer totalCalls;
    private Integer totalPTPs;
    private Integer totalPayments;
    private Integer totalNotes;
    private Integer totalMessages; // SMS + Email + WhatsApp

    // Contact statistics
    private Integer connectedCalls;
    private Integer failedCalls;
    private LocalDateTime lastContactedAt;
    private String lastContactResult;

    // PTP statistics
    private Integer activePTPs;
    private Integer keptPTPs;
    private Integer brokenPTPs;
    private BigDecimal totalPTPAmount;
    private BigDecimal collectedAmount;

    // Timeline range
    private LocalDateTime firstEventAt;
    private LocalDateTime lastEventAt;
    private Integer daysSinceLastActivity;
}
