package com.finx.collectionsservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for PTP statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PTPStatsDTO {

    private Long userId;
    private String userName;

    // Count Statistics
    private Long totalPTPs;
    private Long pendingPTPs;
    private Long keptPTPs;
    private Long brokenPTPs;
    private Long renewedPTPs;
    private Long partialPTPs;

    // Amount Statistics
    private BigDecimal totalPTPAmount;
    private BigDecimal collectedAmount;
    private BigDecimal pendingAmount;

    // Performance Metrics
    private Double keepRate;
    private Double brokenRate;

    // Today's Summary
    private Long ptpsDueToday;
    private Long ptpsOverdueToday;
    private BigDecimal amountDueToday;
}
