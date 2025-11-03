package com.finx.communication.domain.dto.sms;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * SMS Send Request with support for multiple recipients and dynamic variables
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsSendRequest {

    @NotBlank(message = "Template ID is required")
    private String templateId;

    private String shortUrl; // "1" or "0"

    private String shortUrlExpiry; // In seconds

    private String realTimeResponse; // "1" for real-time response

    @NotEmpty(message = "At least one recipient is required")
    private List<SmsRecipient> recipients;

    private Long campaignId;
    private Long caseId;
    private Long userId;
}
