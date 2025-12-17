package com.finx.collectionsservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Dashboard statistics for Repayment Dashboard Screen
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepaymentDashboardDTO {

    // Daily Stats
    private Long todayTotalCount;
    private BigDecimal todayTotalAmount;
    private Long todayPendingCount;
    private Long todayApprovedCount;
    private Long todayRejectedCount;

    // Monthly Stats
    private Long monthTotalCount;
    private BigDecimal monthTotalAmount;
    private BigDecimal monthTargetAmount;
    private Double monthAchievementPercentage;

    // Payment Mode Breakdown
    private Map<String, Long> paymentModeCount;
    private Map<String, BigDecimal> paymentModeAmount;

    // Pending Actions
    private Long pendingApprovalCount;
    private Long slaBreachedCount;
    private Long pendingReconciliationCount;

    // Digital Payment Stats
    private Long digitalPaymentInitiatedCount;
    private Long digitalPaymentSuccessCount;
    private Long digitalPaymentFailedCount;
    private BigDecimal digitalPaymentTotalAmount;
}
