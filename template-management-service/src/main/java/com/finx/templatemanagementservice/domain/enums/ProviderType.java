package com.finx.templatemanagementservice.domain.enums;

/**
 * External communication providers
 */
public enum ProviderType {
    MSG91("MSG91", "MSG91 Communication Platform"),
    TWILIO("Twilio", "Twilio Communication Platform"),
    SENDGRID("SendGrid", "SendGrid Email Service"),
    INTERNAL("Internal", "Internal Communication System");

    private final String displayName;
    private final String description;

    ProviderType(String displayName, String description) {
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
