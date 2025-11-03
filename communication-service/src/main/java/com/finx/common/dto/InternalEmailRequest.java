package com.finx.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalEmailRequest {

    private String toName;

    @Email
    @NotBlank
    private String toEmail;

    @NotNull(message = "Variables cannot be null")
    private Map<String, String> variables;

    private String fromName;

    // fromEmail will be fetched from config, not from request body

    private List<String> replyTo;

    private List<Attachment> attachments;

    @NotBlank
    private String templateId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Attachment {
        private String filename;
        private String content; // Base64 encoded content
        private String contentType;
    }
}
