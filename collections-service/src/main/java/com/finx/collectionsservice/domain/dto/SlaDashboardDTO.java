package com.finx.collectionsservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * SLA Monitoring Dashboard statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlaDashboardDTO {

    // Overall SLA Stats
    private Long totalRepayments;
    private Long withinSlaCount;
    private Long breachedCount;
    private Double slaCompliancePercentage;

    // Breach by Type
    private Long depositSlaBreaches;
    private Long reconciliationSlaBreaches;
    private Long approvalSlaBreaches;

    // Breach by Severity
    private Long criticalBreaches;    // > 48 hours
    private Long majorBreaches;        // 24-48 hours
    private Long minorBreaches;        // < 24 hours

    // Breach Trend (last 7 days)
    private Map<String, Long> dailyBreachTrend;

    // Top Breached Cases
    private List<SlaBreachSummary> topBreachedCases;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SlaBreachSummary {
        private Long repaymentId;
        private String repaymentNumber;
        private Long caseId;
        private String customerName;
        private String breachType;
        private Integer breachHours;
        private String status;
    }
}
