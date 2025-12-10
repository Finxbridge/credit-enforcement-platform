package com.finx.templatemanagementservice.domain.enums;

/**
 * Communication channel types
 */
public enum ChannelType {
    SMS("SMS", "SMS messaging"),
    WHATSAPP("WhatsApp", "WhatsApp messaging"),
    EMAIL("Email", "Email communication"),
    IVR("IVR", "Interactive Voice Response"),
    NOTICE("Notice", "Legal/Physical Notice - Document only");

    private final String displayName;
    private final String description;

    ChannelType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
