package com.finx.collectionsservice.config;

public class CacheConstants {
    public static final String PTP_CACHE = "ptp_cache";
    public static final String PTP_STATS_CACHE = "ptp_stats_cache";
    public static final String REPAYMENT_CACHE = "repayment_cache";
    public static final String OTS_CACHE = "ots_cache";
    public static final String CLOSURE_CACHE = "closure_cache";

    public static final long DEFAULT_TTL_SECONDS = 3600;
    public static final long SHORT_TTL_SECONDS = 300;
    public static final long STATS_TTL_SECONDS = 1800;
}
