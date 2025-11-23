package com.finx.strategyengineservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * WhatsApp Request DTO for communication-service
 * Aligned with communication-service WhatsAppSendRequest format
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppRequest {
    private String templateId;
    private List<String> to; // List of phone numbers
    private Map<String, Map<String, String>> components; // Dynamic components (header_1, body_1, etc.)
    private WhatsAppLanguage language;

    // Tracking fields
    private Long campaignId;
    private Long caseId;
    private Long userId;

    /**
     * WhatsApp Language configuration
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WhatsAppLanguage {
        private String code; // e.g., "en_US", "en"

        @Builder.Default
        private String policy = "deterministic";
    }
}
