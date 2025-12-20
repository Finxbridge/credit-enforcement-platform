package com.finx.configurationsservice.domain.dto;

import com.finx.configurationsservice.domain.enums.AuthType;
import com.finx.configurationsservice.domain.enums.ProviderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProviderRequest {

    @NotBlank(message = "Provider code is required")
    @Size(max = 50, message = "Provider code must not exceed 50 characters")
    private String providerCode;

    @NotBlank(message = "Provider name is required")
    @Size(max = 200, message = "Provider name must not exceed 200 characters")
    private String providerName;

    @NotNull(message = "Provider type is required")
    private ProviderType providerType;

    private String description;

    private String endpointUrl;

    @NotNull(message = "Auth type is required")
    private AuthType authType;

    private String apiKey;
    private String apiSecret;
    private String username;
    private String password;
    private String oauthClientId;
    private String oauthClientSecret;
    private String oauthTokenUrl;

    // Provider Specific
    private String namespace;
    private String senderId;
    private String fromEmail;
    private String fromName;

    // Webhook
    private String webhookUrl;
    private String webhookSecret;

    // Rate Limits
    private Integer rateLimitPerSecond;
    private Integer rateLimitPerMinute;
    private Integer rateLimitPerDay;

    // Additional Config
    private Map<String, Object> additionalConfig;
    private Map<String, String> headersConfig;

    // Status
    private Boolean isActive;
    private Boolean isDefault;
    private Integer priorityOrder;
}
