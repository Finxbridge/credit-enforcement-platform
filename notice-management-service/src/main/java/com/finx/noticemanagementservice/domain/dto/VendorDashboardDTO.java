package com.finx.noticemanagementservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorDashboardDTO {

    private Long vendorId;
    private String vendorCode;
    private String vendorName;

    // Today's Stats
    private Long todayPendingCount;
    private Long todayDispatchedCount;
    private Long todayDeliveredCount;
    private Long todayRtoCount;

    // Overall Stats
    private Long totalPendingCount;
    private Long totalInTransitCount;
    private Long totalDeliveredCount;
    private Long totalRtoCount;
    private Long totalFailedCount;

    // SLA Stats
    private Long slaBreachedCount;
    private Long slaAtRiskCount; // About to breach within 24 hours
    private BigDecimal slaComplianceRate;

    // Performance
    private BigDecimal deliveryRate;
    private BigDecimal rtoRate;
    private Integer avgDeliveryDays;
}
