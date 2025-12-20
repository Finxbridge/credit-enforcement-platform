package com.finx.communication.domain.dto.dialer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO for call history response with pagination
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialerCallHistoryResponse {

    private List<CallHistoryItem> calls;
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;
    private Boolean hasNext;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CallHistoryItem {
        private String callId;
        private String dialerCallId;
        private Long caseId;
        private Long agentId;
        private String agentName;
        private String customerMobile;
        private String customerName;
        private String callType;          // CLICK_TO_CALL, AUTO_DIAL, INBOUND
        private String callStatus;
        private Integer callDuration;     // in seconds
        private String recordingUrl;
        private String dispositionCode;
        private String dispositionName;
        private String notes;
        private LocalDateTime initiatedAt;
        private LocalDateTime answeredAt;
        private LocalDateTime endedAt;
        private Map<String, Object> customFields;
    }
}
