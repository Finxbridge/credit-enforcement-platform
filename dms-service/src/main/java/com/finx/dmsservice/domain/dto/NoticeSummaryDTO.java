package com.finx.dmsservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeSummaryDTO {

    // Count by Status
    private Long totalNotices;
    private Long draftNotices;
    private Long pendingApprovalNotices;
    private Long approvedNotices;
    private Long generatedNotices;
    private Long dispatchedNotices;
    private Long deliveredNotices;
    private Long returnedNotices;
    private Long failedNotices;
    private Long cancelledNotices;

    // Count by Type
    private Map<String, Long> noticesByType;

    // Today's Stats
    private Long todayGenerated;
    private Long todayDispatched;
    private Long todayDelivered;

    // Amount Stats
    private BigDecimal totalDuesAmount;
    private BigDecimal averageDuesAmount;

    // DPD Distribution
    private Long dpdUnder30;
    private Long dpd30to60;
    private Long dpd60to90;
    private Long dpd90to180;
    private Long dpdOver180;

    // Region Distribution
    private Map<String, Long> noticesByRegion;

    // Delivery Stats
    private Double deliverySuccessRate;
    private Long pendingDelivery;
    private Long overdueResponses;
}
