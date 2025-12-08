package com.finx.communication.domain.dto.dialer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for webhook/callback from dialer provider
 * Vendor-agnostic structure for receiving call status updates
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialerCallbackRequest {

    private String callId;           // Our internal call ID
    private String dialerCallId;     // Provider's call ID
    private String callStatus;       // RINGING, ANSWERED, BUSY, FAILED, NO_ANSWER, COMPLETED
    private Integer callDuration;    // Duration in seconds
    private String recordingUrl;     // URL to call recording
    private String disposition;      // Call disposition code
    private String hangupCause;      // Reason for call ending
    private String answeredAt;       // ISO timestamp when call was answered
    private String endedAt;          // ISO timestamp when call ended
    private Map<String, Object> providerData; // Raw data from provider
}
