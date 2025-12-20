package com.finx.templatemanagementservice.domain.dto;

import com.finx.templatemanagementservice.domain.enums.ChannelType;
import com.finx.templatemanagementservice.domain.enums.LanguageType;
import com.finx.templatemanagementservice.domain.enums.ProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Template Detail DTO (full view with variables)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateDetailDTO {

    private Long id;
    private String templateName;
    private String templateCode;
    private ChannelType channel;
    private LanguageType language;
    private String languageShortCode;  // Short code for communication service (Te, Hi, En_US)
    private ProviderType provider;
    private String providerTemplateId;
    private String description;
    private Boolean isActive;
    private List<TemplateVariableDTO> variables;
    private TemplateContentDTO content;

    // Document attachment fields (stored in DMS - OVH S3)
    private String dmsDocumentId;        // Reference ID to DMS service
    private String documentUrl;          // Public URL to access document
    private String documentStoragePath;  // S3 storage path for direct access
    private String documentStorageBucket; // S3 bucket name
    private String documentOriginalName; // Original filename
    private String documentType;         // PDF, DOC, DOCX
    private String documentContentType;  // MIME type
    private Long documentSizeBytes;
    private Boolean hasDocumentVariables;
    private List<String> documentPlaceholders; // Extracted placeholders from document

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateContentDTO {
        private String languageCode;
        private String subject;
        private String content;
    }
}
