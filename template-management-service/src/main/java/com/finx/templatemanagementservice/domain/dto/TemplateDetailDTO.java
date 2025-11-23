package com.finx.templatemanagementservice.domain.dto;

import com.finx.templatemanagementservice.domain.enums.ChannelType;
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
    private ProviderType provider;
    private String providerTemplateId;
    private String description;
    private Boolean isActive;
    private List<TemplateVariableDTO> variables;
    private TemplateContentDTO content;
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
