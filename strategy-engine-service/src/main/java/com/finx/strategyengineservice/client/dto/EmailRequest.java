package com.finx.strategyengineservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Email Request DTO for communication-service
 * Matches InternalEmailRequest structure
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {
    private String toEmail;
    private String toName;
    private String templateId;
    private Map<String, String> variables;
    private List<String> replyTo;
    private List<Attachment> attachments;

    // Tracking fields
    private Long caseId;
    private Long userId;
    private String campaignId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Attachment {
        private String filename;
        private String content; // Base64 encoded
        private String contentType;
    }
}
