package com.finx.templatemanagementservice.domain.dto;

import com.finx.templatemanagementservice.domain.enums.ChannelType;
import com.finx.templatemanagementservice.domain.enums.ProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Template DTO (summary view)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateDTO {

    private Long id;
    private String templateName;
    private String templateCode;
    private ChannelType channel;
    private ProviderType provider;
    private String providerTemplateId;
    private String description;
    private Boolean isActive;
    private Integer variableCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
