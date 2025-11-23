package com.finx.strategyengineservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * SMS Request DTO for communication-service
 * Aligned with communication-service SmsSendRequest format
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SMSRequest {
    private String templateId;
    private String shortUrl; // "1" or "0"
    private String shortUrlExpiry; // In seconds
    private String realTimeResponse; // "1" for real-time response
    private List<SmsRecipient> recipients;

    // Tracking fields
    private Long campaignId;
    private Long caseId;
    private Long userId;

    /**
     * SMS Recipient with dynamic variables
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SmsRecipient {
        private String mobile;
        private java.util.Map<String, Object> variables; // VAR1, VAR2, etc.
    }
}
