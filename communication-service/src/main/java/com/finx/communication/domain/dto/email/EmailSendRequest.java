package com.finx.communication.domain.dto.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailSendRequest {

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Invalid email format")
    private String to;

    private String cc;
    private String bcc;

    // From email (optional, will use default if not provided)
    private String fromEmail;
    private String fromName;

    @NotBlank(message = "Subject is required")
    private String subject;

    private String bodyHtml;
    private String bodyText;

    private String templateId; // SendGrid template ID or internal template ID

    /**
     * Dynamic variables for template
     * Example: {"name": "John", "amount": "1000"}
     */
    private Map<String, String> variables;

    private List<String> attachmentUrls;

    private Long campaignId;
    private Long caseId;
    private Long userId;
}
