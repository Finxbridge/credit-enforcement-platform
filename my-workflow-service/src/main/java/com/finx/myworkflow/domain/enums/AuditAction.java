package com.finx.myworkflow.domain.enums;

public enum AuditAction {
    CREATE,
    UPDATE,
    DELETE,
    VIEW,
    STATUS_CHANGE,
    QUEUE_ADD,
    QUEUE_REMOVE,
    BOOKMARK_ADD,
    BOOKMARK_REMOVE,
    NOTE_PIN,
    NOTE_UNPIN,
    ACTION_START,
    ACTION_COMPLETE,
    EXPORT,
    IMPORT,
    // Quick Actions
    CALL_INITIATED,
    CALL_DISPOSITION_SAVED,
    SMS_SENT,
    EMAIL_SENT,
    NOTICE_GENERATED,
    NOTICE_DISPATCHED,
    PAYMENT_LINK_GENERATED
}
