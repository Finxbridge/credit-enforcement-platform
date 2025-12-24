package com.finx.collectionsservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for sending WhatsApp messages via Communication Service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppSendRequest {

    /**
     * WhatsApp template ID registered in MSG91
     */
    private String templateId;

    /**
     * List of recipient phone numbers (with country code)
     */
    private List<String> to;

    /**
     * Dynamic components for the template
     * Example: {"body_1": {"type": "text", "value": "John"}, "body_2": {"type": "text", "value": "5000"}}
     */
    private Map<String, Map<String, String>> components;

    /**
     * Language configuration
     */
    private WhatsAppLanguage language;

    /**
     * Campaign ID for tracking
     */
    private Long campaignId;

    /**
     * Case ID for tracking
     */
    private Long caseId;

    /**
     * User ID who initiated the message
     */
    private Long userId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WhatsAppLanguage {
        private String code;
        private String policy;
    }
}
