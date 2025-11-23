package com.finx.communication.domain.dto.whatsapp;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * WhatsApp Send Request - Aligned with user's input format
 * This will be transformed to Msg91 API format internally
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppSendRequest {

    @NotBlank(message = "Template ID is required")
    private String templateId;

    @NotEmpty(message = "At least one recipient is required")
    private List<String> to;

    /**
     * Dynamic components for the template
     * Example: {"header_1": {"type": "image", "value": "url"}, "body_1": {"type": "text", "value": "John"}}
     */
    private Map<String, Map<String, String>> components;

    @Valid
    private WhatsAppLanguage language;

    // Optional tracking fields
    private Long campaignId;
    private Long caseId;
    private Long userId;
}
