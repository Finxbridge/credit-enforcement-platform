package com.finx.communication.domain.dto.notice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeDispatchResponse {

    private Long noticeId;
    private String vendorJobId;
    private String trackingNumber;
    private String status;
    private String message;
    private String providerResponse;
}
