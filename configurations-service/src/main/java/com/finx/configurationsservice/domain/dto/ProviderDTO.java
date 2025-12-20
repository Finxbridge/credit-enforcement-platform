package com.finx.configurationsservice.domain.dto;

import com.finx.configurationsservice.domain.enums.AuthType;
import com.finx.configurationsservice.domain.enums.ProviderType;
import com.finx.configurationsservice.domain.enums.TestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderDTO {
    private Long id;
    private String providerCode;
    private String providerName;
    private ProviderType providerType;
    private String description;
    private String endpointUrl;
    private AuthType authType;
    private String namespace;
    private String senderId;
    private String fromEmail;
    private String fromName;
    private String webhookUrl;
    private Integer rateLimitPerSecond;
    private Integer rateLimitPerMinute;
    private Integer rateLimitPerDay;
    private Map<String, Object> additionalConfig;
    private Map<String, String> headersConfig;
    private Boolean isActive;
    private Boolean isDefault;
    private Integer priorityOrder;
    private LocalDateTime lastTestedAt;
    private TestStatus lastTestStatus;
    private String lastTestMessage;
    private LocalDateTime lastUsedAt;
    private Long successCount;
    private Long failureCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
}
