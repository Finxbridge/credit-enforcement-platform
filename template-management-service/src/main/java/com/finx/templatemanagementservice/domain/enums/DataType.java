package com.finx.templatemanagementservice.domain.enums;

/**
 * Data types for template variables
 */
public enum DataType {
    TEXT("Text", "Plain text value"),
    NUMBER("Number", "Numeric value"),
    DATE("Date", "Date value (formatted)"),
    CURRENCY("Currency", "Currency value (formatted)"),
    IMAGE("Image", "Image URL or media ID"),
    VIDEO("Video", "Video URL or media ID"),
    DOCUMENT("Document", "Document URL or media ID");

    private final String displayName;
    private final String description;

    DataType(String displayName, String description) {
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
