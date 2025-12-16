package com.finx.templatemanagementservice.domain.enums;

/**
 * Supported languages for templates
 * The shortCode is used when communicating with external providers (MSG91, etc.)
 */
public enum LanguageType {
    ENGLISH("English", "en"),
    HINDI("Hindi", "hi"),
    TAMIL("Tamil", "ta"),
    TELUGU("Telugu", "te"),
    KANNADA("Kannada", "kn"),
    MALAYALAM("Malayalam", "ml"),
    MARATHI("Marathi", "mr"),
    GUJARATI("Gujarati", "gu"),
    BENGALI("Bengali", "bn"),
    PUNJABI("Punjabi", "pa");

    private final String displayName;
    private final String shortCode;

    LanguageType(String displayName, String shortCode) {
        this.displayName = displayName;
        this.shortCode = shortCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getShortCode() {
        return shortCode;
    }

    public static LanguageType fromShortCode(String shortCode) {
        for (LanguageType lang : values()) {
            if (lang.shortCode.equalsIgnoreCase(shortCode)) {
                return lang;
            }
        }
        throw new IllegalArgumentException("Unknown language short code: " + shortCode);
    }
}
