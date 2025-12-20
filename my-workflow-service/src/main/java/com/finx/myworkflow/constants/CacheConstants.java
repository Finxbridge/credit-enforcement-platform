package com.finx.myworkflow.constants;

public final class CacheConstants {

    private CacheConstants() {}

    public static final String WORKFLOW_QUEUE_CACHE = "workflow-queue-cache";
    public static final String WORKFLOW_ACTION_CACHE = "workflow-action-cache";
    public static final String CASE_BOOKMARK_CACHE = "case-bookmark-cache";
    public static final String CASE_NOTE_CACHE = "case-note-cache";
    public static final String USER_QUEUE_CACHE = "user-queue-cache";

    public static final long CACHE_TTL_SECONDS = 21600; // 6 hours
}
