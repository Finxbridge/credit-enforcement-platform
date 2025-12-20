package com.finx.configurationsservice.domain.entity;

import com.finx.configurationsservice.domain.enums.AuthType;
import com.finx.configurationsservice.domain.enums.ProviderType;
import com.finx.configurationsservice.domain.enums.TestStatus;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "providers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Provider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_code", unique = true, nullable = false, length = 50)
    private String providerCode;

    @Column(name = "provider_name", nullable = false, length = 200)
    private String providerName;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false, length = 30)
    private ProviderType providerType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Connection Details
    @Column(name = "endpoint_url", length = 500)
    private String endpointUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_type", nullable = false, length = 30)
    private AuthType authType;

    @Column(name = "api_key", length = 500)
    private String apiKey;

    @Column(name = "api_secret", length = 500)
    private String apiSecret;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "password", length = 500)
    private String password;

    @Column(name = "oauth_client_id", length = 200)
    private String oauthClientId;

    @Column(name = "oauth_client_secret", length = 500)
    private String oauthClientSecret;

    @Column(name = "oauth_token_url", length = 500)
    private String oauthTokenUrl;

    // Provider Specific Config
    @Column(name = "namespace", length = 100)
    private String namespace;

    @Column(name = "sender_id", length = 50)
    private String senderId;

    @Column(name = "from_email", length = 100)
    private String fromEmail;

    @Column(name = "from_name", length = 100)
    private String fromName;

    // Webhook Configuration
    @Column(name = "webhook_url", length = 500)
    private String webhookUrl;

    @Column(name = "webhook_secret", length = 255)
    private String webhookSecret;

    // Rate Limits
    @Column(name = "rate_limit_per_second")
    private Integer rateLimitPerSecond;

    @Column(name = "rate_limit_per_minute")
    private Integer rateLimitPerMinute;

    @Column(name = "rate_limit_per_day")
    private Integer rateLimitPerDay;

    // Additional Configuration
    @Type(JsonType.class)
    @Column(name = "additional_config", columnDefinition = "jsonb")
    private Map<String, Object> additionalConfig;

    @Type(JsonType.class)
    @Column(name = "headers_config", columnDefinition = "jsonb")
    private Map<String, String> headersConfig;

    // Status & Health
    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "is_default")
    private Boolean isDefault;

    @Column(name = "priority_order")
    private Integer priorityOrder;

    @Column(name = "last_tested_at")
    private LocalDateTime lastTestedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_test_status", length = 20)
    private TestStatus lastTestStatus;

    @Column(name = "last_test_message", columnDefinition = "TEXT")
    private String lastTestMessage;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "success_count")
    private Long successCount;

    @Column(name = "failure_count")
    private Long failureCount;

    // Audit Fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (isDefault == null) {
            isDefault = false;
        }
        if (priorityOrder == null) {
            priorityOrder = 0;
        }
        if (successCount == null) {
            successCount = 0L;
        }
        if (failureCount == null) {
            failureCount = 0L;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
