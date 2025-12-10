package com.finx.templatemanagementservice.domain.dto;

import com.finx.templatemanagementservice.domain.enums.ChannelType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight DTO for template dropdown selection
 * Used by Strategy Engine frontend when selecting templates for actions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateDropdownDTO {

    private Long id;
    private String templateName;
    private String templateCode;
    private ChannelType channel;
}
