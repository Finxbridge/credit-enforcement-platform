package com.finx.communication.constants;

public class CacheConstants {
    // Cache namespaces
    public static final String AUTH_NAMESPACE = "auth:";
    public static final String USER_NAMESPACE = "user:";
    public static final String CONFIG_NAMESPACE = "config:";
    public static final String MASTERDATA_NAMESPACE = "masterdata:";
    public static final String CASE_NAMESPACE = "case:";
    public static final String TEMPLATE_NAMESPACE = "template:";
    public static final String DASHBOARD_NAMESPACE = "dashboard:";
    public static final String INTEGRATION_NAMESPACE = "integration:";

    // TTL values in seconds
    public static final long DEFAULT_TTL = 3600; // 1 hour
    public static final long SESSION_TTL = 1800; // 30 minutes
    public static final long CONFIG_TTL = 7200; // 2 hours
    public static final long OTP_TTL = 600; // 10 minutes

    // ConfigCacheService keys
    public static final String MSG91_OTP_TEMPLATE_ID = "MSG91_OTP_TEMPLATE_ID";
    public static final String MSG91_FROM_NAME = "MSG91_FROM_NAME";
    public static final String EMAIL_FROM_ADDRESS = "EMAIL_FROM_ADDRESS";
    public static final String EMAIL_FROM_NAME = "EMAIL_FROM_NAME";
    public static final String WEBCLIENT_RESPONSE_TIMEOUT = "WEBCLIENT_RESPONSE_TIMEOUT";

    // IntegrationCacheService keys
    public static final String MSG91_SMS = "MSG91_SMS";
    public static final String MSG91_WHATSAPP = "MSG91_WHATSAPP";
    public static final String SENDGRID_EMAIL = "SENDGRID_EMAIL";
    public static final String MSG91_EMAIL = "MSG91_EMAIL";
    public static final String MSG91_OTP = "MSG91_OTP";

    // ConfigCacheService keys
    public static final String SYSTEM_CONFIG = "systemConfig";

    // IntegrationCacheService keys
    public static final String INTEGRATION_CONFIG_CACHE = "integrationConfig";
}
