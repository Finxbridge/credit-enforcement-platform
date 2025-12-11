package com.finx.templatemanagementservice.domain.enums;

/**
 * Supported languages for templates
 * The shortCode is used when communicating with external providers (MSG91, etc.)
 */
public enum LanguageType {
    TELUGU("Telugu", "Te"),
    HINDI("Hindi", "Hi"),
    ENGLISH("English", "En_US");

    private final String displayName;
    private final String shortCode;

    LanguageType(String displayName, String shortCode) {
        this.displayName = displayName;
        this.shortCode = shortCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Short code for communication service (MSG91, etc.)
     * TELUGU -> Te, HINDI -> Hi, ENGLISH -> En_US
     */
    public String getShortCode() {
        return shortCode;
    }

    /**
     * Get LanguageType from short code
     */
    public static LanguageType fromShortCode(String shortCode) {
        for (LanguageType lang : values()) {
            if (lang.shortCode.equalsIgnoreCase(shortCode)) {
                return lang;
            }
        }
        throw new IllegalArgumentException("Unknown language short code: " + shortCode);
    }
}
