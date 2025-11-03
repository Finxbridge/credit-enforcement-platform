package com.finx.common.constants;

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
    public static final String OTP_LENGTH = "OTP_LENGTH";
    public static final String OTP_EXPIRY_MINUTES = "OTP_EXPIRY_MINUTES";
    public static final String OTP_MAX_ATTEMPTS = "OTP_MAX_ATTEMPTS";
    public static final String SESSION_INACTIVITY_TIMEOUT_MINUTES = "SESSION_INACTIVITY_TIMEOUT_MINUTES";
    public static final String SECURITY_MAX_FAILED_LOGIN_ATTEMPTS = "SECURITY_MAX_FAILED_LOGIN_ATTEMPTS";
    public static final String SECURITY_ACCOUNT_LOCKOUT_DURATION_MINUTES = "SECURITY_ACCOUNT_LOCKOUT_DURATION_MINUTES";
    public static final String JWT_ACCESS_TOKEN_EXPIRATION_MINUTES = "JWT_ACCESS_TOKEN_EXPIRATION_MINUTES";
    public static final String JWT_REFRESH_TOKEN_EXPIRATION_DAYS = "JWT_REFRESH_TOKEN_EXPIRATION_DAYS";
    public static final String JWT_RESET_TOKEN_EXPIRATION_MINUTES = "JWT_RESET_TOKEN_EXPIRATION_MINUTES";
    public static final String JWT_SECRET = "JWT_SECRET";
    public static final String SESSION_SINGLE_ENFORCEMENT = "SESSION_SINGLE_ENFORCEMENT";
    public static final String MSG91_OTP_TEMPLATE_ID = "MSG91_OTP_TEMPLATE_ID";
    public static final String MSG91_FROM_NAME = "MSG91_FROM_NAME";

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
