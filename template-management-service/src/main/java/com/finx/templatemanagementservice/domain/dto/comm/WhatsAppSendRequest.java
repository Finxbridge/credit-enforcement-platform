package com.finx.templatemanagementservice.domain.dto.comm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * WhatsApp Send Request - Aligned with user's input format
 * This will be transformed to Msg91 API format internally by communication-service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppSendRequest {

    private String templateId;

    private List<String> to;

    /**
     * Dynamic components for the template
     * Example: {"header_1": {"type": "image", "value": "url"}, "body_1": {"type": "text", "value": "John"}}
     */
    private Map<String, Map<String, String>> components;

    private WhatsAppLanguage language;

    // Optional tracking fields
    private Long campaignId;
    private Long caseId;
    private Long userId;
}
