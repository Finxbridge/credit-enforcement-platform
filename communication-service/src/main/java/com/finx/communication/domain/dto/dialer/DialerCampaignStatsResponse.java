package com.finx.communication.domain.dto.dialer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

/**
 * DTO for campaign/daily dialer statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialerCampaignStatsResponse {

    private String campaignId;
    private LocalDate date;

    // Call Metrics
    private Integer totalCalls;
    private Integer answeredCalls;
    private Integer missedCalls;
    private Integer busyCalls;
    private Integer failedCalls;
    private Integer noAnswerCalls;

    // Duration Metrics
    private Integer totalTalkTimeSeconds;
    private Integer avgTalkTimeSeconds;
    private Integer avgWaitTimeSeconds;
    private Integer avgHandleTimeSeconds;

    // Conversion Metrics
    private Integer ptpCount;           // Promise to pay count
    private Double ptpAmount;           // Total PTP amount
    private Integer callbacksScheduled;
    private Integer paymentsCollected;
    private Double amountCollected;

    // Agent Metrics
    private Integer activeAgents;
    private Integer totalAgents;

    // Disposition Breakdown
    private Map<String, Integer> dispositionCounts;

    // Contact Rate
    private Double contactRate;         // Answered/Total calls %
    private Double rightPartyContactRate;
}
