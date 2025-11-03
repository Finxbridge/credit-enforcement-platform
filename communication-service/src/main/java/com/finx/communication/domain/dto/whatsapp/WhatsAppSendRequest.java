package com.finx.communication.domain.dto.whatsapp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * WhatsApp Send Request with dynamic components support
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppSendRequest {

    @NotBlank(message = "Integrated number is required")
    private String integratedNumber;

    @NotBlank(message = "Template name is required")
    private String templateName;

    private String languageCode; // Default: "en"

    @NotEmpty(message = "At least one recipient is required")
    private List<WhatsAppRecipient> recipients;

    private Long campaignId;
    private Long caseId;
    private Long userId;
}
