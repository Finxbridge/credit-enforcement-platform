package com.finx.dmsservice.domain.enums;

/**
 * Document category for categorizing documents in DMS
 */
public enum DocumentCategory {
    TEMPLATE,      // Non-generated documents (with placeholders)
    GENERATED,     // Generated documents (placeholders replaced with real values)
    USER_UPLOAD    // General user uploads (default)
}
