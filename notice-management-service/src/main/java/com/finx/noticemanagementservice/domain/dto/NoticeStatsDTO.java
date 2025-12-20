package com.finx.noticemanagementservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeStatsDTO {
    private Long totalNotices;
    private Long draftNotices;
    private Long generatedNotices;
    private Long dispatchedNotices;
    private Long inTransitNotices;
    private Long deliveredNotices;
    private Long rtoNotices;
    private Long failedNotices;
    private Long dispatchSlaBreaches;
    private Long deliverySlaBreaches;
    private Double deliveryRate;
    private Double rtoRate;
}
