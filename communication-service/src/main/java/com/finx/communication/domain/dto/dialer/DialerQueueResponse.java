package com.finx.communication.domain.dto.dialer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for bulk queue response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialerQueueResponse {

    private String batchId;
    private Integer totalQueued;
    private Integer successCount;
    private Integer failedCount;
    private String status;
    private String message;
    private LocalDateTime queuedAt;
    private List<QueuedCallItem> queuedCalls;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueuedCallItem {
        private Long caseId;
        private String customerMobile;
        private String callId;
        private String status;    // QUEUED, FAILED
        private String errorMessage;
        private Integer queuePosition;
    }
}
