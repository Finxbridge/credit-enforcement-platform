package com.finx.templatemanagementservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight DTO for template dropdown selection
 * Used by Strategy Engine frontend when selecting templates for actions
 *
 * In strategy creation, pass templateCode as the template identifier
 * templateName is for display purposes only
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateDropdownDTO {

    /**
     * Template ID (database primary key)
     */
    private Long id;

    /**
     * Display name for UI dropdown
     */
    private String templateName;

    /**
     * Unique template identifier - USE THIS for strategy creation
     * This is the value to pass in strategy.channel.templateName field
     */
    private String templateCode;

    /**
     * Channel type (SMS, WHATSAPP, EMAIL, IVR, NOTICE)
     */
    private String channel;

    /**
     * Language (TELUGU, HINDI, ENGLISH)
     */
    private String language;

    /**
     * Language short code for communication service (Te, Hi, En_US)
     */
    private String languageShortCode;
}
