package com.finx.configurationsservice.service.impl;

import com.finx.configurationsservice.domain.dto.*;
import com.finx.configurationsservice.domain.entity.Provider;
import com.finx.configurationsservice.domain.entity.ProviderTestHistory;
import com.finx.configurationsservice.domain.enums.ProviderType;
import com.finx.configurationsservice.domain.enums.TestStatus;
import com.finx.configurationsservice.exception.BusinessException;
import com.finx.configurationsservice.exception.ResourceNotFoundException;
import com.finx.configurationsservice.repository.ProviderRepository;
import com.finx.configurationsservice.repository.ProviderTestHistoryRepository;
import com.finx.configurationsservice.service.AuditLogService;
import com.finx.configurationsservice.service.ProviderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProviderServiceImpl implements ProviderService {

    private final ProviderRepository providerRepository;
    private final ProviderTestHistoryRepository testHistoryRepository;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ProviderDTO createProvider(CreateProviderRequest request) {
        if (providerRepository.existsByProviderCode(request.getProviderCode())) {
            throw new BusinessException("Provider with code '" + request.getProviderCode() + "' already exists");
        }

        Provider provider = Provider.builder()
                .providerCode(request.getProviderCode())
                .providerName(request.getProviderName())
                .providerType(request.getProviderType())
                .description(request.getDescription())
                .endpointUrl(request.getEndpointUrl())
                .authType(request.getAuthType())
                .apiKey(request.getApiKey())
                .apiSecret(request.getApiSecret())
                .username(request.getUsername())
                .password(request.getPassword())
                .oauthClientId(request.getOauthClientId())
                .oauthClientSecret(request.getOauthClientSecret())
                .oauthTokenUrl(request.getOauthTokenUrl())
                .namespace(request.getNamespace())
                .senderId(request.getSenderId())
                .fromEmail(request.getFromEmail())
                .fromName(request.getFromName())
                .webhookUrl(request.getWebhookUrl())
                .webhookSecret(request.getWebhookSecret())
                .rateLimitPerSecond(request.getRateLimitPerSecond())
                .rateLimitPerMinute(request.getRateLimitPerMinute())
                .rateLimitPerDay(request.getRateLimitPerDay())
                .additionalConfig(request.getAdditionalConfig())
                .headersConfig(request.getHeadersConfig())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .priorityOrder(request.getPriorityOrder() != null ? request.getPriorityOrder() : 0)
                .build();

        Provider saved = providerRepository.save(provider);

        // Audit log
        auditLogService.logCreate("Provider", saved.getId().toString(), saved.getProviderName(),
                convertToMap(saved));

        log.info("Created provider: {} ({})", saved.getProviderName(), saved.getProviderCode());
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProviderDTO getProviderById(Long id) {
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with id: " + id));
        return mapToDTO(provider);
    }

    @Override
    @Transactional(readOnly = true)
    public ProviderDTO getProviderByCode(String code) {
        Provider provider = providerRepository.findByProviderCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with code: " + code));
        return mapToDTO(provider);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProviderDTO> getProvidersByType(ProviderType type) {
        return providerRepository.findByProviderType(type).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProviderDTO> getActiveProvidersByType(ProviderType type) {
        return providerRepository.findActiveByTypeOrderByPriority(type).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProviderDTO> getActiveProviders() {
        return providerRepository.findAllActiveOrderByPriority().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProviderDTO getDefaultProvider(ProviderType type) {
        Provider provider = providerRepository.findByProviderTypeAndIsDefaultTrue(type)
                .orElseThrow(() -> new ResourceNotFoundException("No default provider found for type: " + type));
        return mapToDTO(provider);
    }

    @Override
    @Transactional
    public ProviderDTO updateProvider(Long id, UpdateProviderRequest request) {
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with id: " + id));

        Map<String, Object> oldValue = convertToMap(provider);
        List<String> changedFields = new ArrayList<>();

        if (request.getProviderName() != null && !request.getProviderName().equals(provider.getProviderName())) {
            provider.setProviderName(request.getProviderName());
            changedFields.add("providerName");
        }
        if (request.getProviderType() != null) {
            provider.setProviderType(request.getProviderType());
            changedFields.add("providerType");
        }
        if (request.getDescription() != null) {
            provider.setDescription(request.getDescription());
            changedFields.add("description");
        }
        if (request.getEndpointUrl() != null) {
            provider.setEndpointUrl(request.getEndpointUrl());
            changedFields.add("endpointUrl");
        }
        if (request.getAuthType() != null) {
            provider.setAuthType(request.getAuthType());
            changedFields.add("authType");
        }
        if (request.getApiKey() != null) {
            provider.setApiKey(request.getApiKey());
            changedFields.add("apiKey");
        }
        if (request.getApiSecret() != null) {
            provider.setApiSecret(request.getApiSecret());
            changedFields.add("apiSecret");
        }
        if (request.getUsername() != null) {
            provider.setUsername(request.getUsername());
            changedFields.add("username");
        }
        if (request.getPassword() != null) {
            provider.setPassword(request.getPassword());
            changedFields.add("password");
        }
        if (request.getOauthClientId() != null) {
            provider.setOauthClientId(request.getOauthClientId());
            changedFields.add("oauthClientId");
        }
        if (request.getOauthClientSecret() != null) {
            provider.setOauthClientSecret(request.getOauthClientSecret());
            changedFields.add("oauthClientSecret");
        }
        if (request.getOauthTokenUrl() != null) {
            provider.setOauthTokenUrl(request.getOauthTokenUrl());
            changedFields.add("oauthTokenUrl");
        }
        if (request.getNamespace() != null) {
            provider.setNamespace(request.getNamespace());
            changedFields.add("namespace");
        }
        if (request.getSenderId() != null) {
            provider.setSenderId(request.getSenderId());
            changedFields.add("senderId");
        }
        if (request.getFromEmail() != null) {
            provider.setFromEmail(request.getFromEmail());
            changedFields.add("fromEmail");
        }
        if (request.getFromName() != null) {
            provider.setFromName(request.getFromName());
            changedFields.add("fromName");
        }
        if (request.getWebhookUrl() != null) {
            provider.setWebhookUrl(request.getWebhookUrl());
            changedFields.add("webhookUrl");
        }
        if (request.getWebhookSecret() != null) {
            provider.setWebhookSecret(request.getWebhookSecret());
            changedFields.add("webhookSecret");
        }
        if (request.getRateLimitPerSecond() != null) {
            provider.setRateLimitPerSecond(request.getRateLimitPerSecond());
            changedFields.add("rateLimitPerSecond");
        }
        if (request.getRateLimitPerMinute() != null) {
            provider.setRateLimitPerMinute(request.getRateLimitPerMinute());
            changedFields.add("rateLimitPerMinute");
        }
        if (request.getRateLimitPerDay() != null) {
            provider.setRateLimitPerDay(request.getRateLimitPerDay());
            changedFields.add("rateLimitPerDay");
        }
        if (request.getAdditionalConfig() != null) {
            provider.setAdditionalConfig(request.getAdditionalConfig());
            changedFields.add("additionalConfig");
        }
        if (request.getHeadersConfig() != null) {
            provider.setHeadersConfig(request.getHeadersConfig());
            changedFields.add("headersConfig");
        }
        if (request.getIsActive() != null) {
            provider.setIsActive(request.getIsActive());
            changedFields.add("isActive");
        }
        if (request.getIsDefault() != null) {
            provider.setIsDefault(request.getIsDefault());
            changedFields.add("isDefault");
        }
        if (request.getPriorityOrder() != null) {
            provider.setPriorityOrder(request.getPriorityOrder());
            changedFields.add("priorityOrder");
        }

        Provider saved = providerRepository.save(provider);

        // Audit log
        auditLogService.logUpdate("Provider", saved.getId().toString(), saved.getProviderName(),
                oldValue, convertToMap(saved), changedFields);

        log.info("Updated provider: {} ({})", saved.getProviderName(), saved.getProviderCode());
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public void deactivateProvider(Long id) {
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with id: " + id));

        Map<String, Object> oldValue = convertToMap(provider);
        provider.setIsActive(false);
        providerRepository.save(provider);

        auditLogService.logUpdate("Provider", provider.getId().toString(), provider.getProviderName(),
                oldValue, convertToMap(provider), List.of("isActive"));

        log.info("Deactivated provider: {} ({})", provider.getProviderName(), provider.getProviderCode());
    }

    @Override
    @Transactional
    public void activateProvider(Long id) {
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with id: " + id));

        Map<String, Object> oldValue = convertToMap(provider);
        provider.setIsActive(true);
        providerRepository.save(provider);

        auditLogService.logUpdate("Provider", provider.getId().toString(), provider.getProviderName(),
                oldValue, convertToMap(provider), List.of("isActive"));

        log.info("Activated provider: {} ({})", provider.getProviderName(), provider.getProviderCode());
    }

    @Override
    @Transactional
    public ProviderTestResult testProvider(Long id) {
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with id: " + id));

        long startTime = System.currentTimeMillis();
        TestStatus testStatus;
        Integer responseCode = null;
        String responseMessage = null;
        String errorDetails = null;

        try {
            if (provider.getEndpointUrl() != null && !provider.getEndpointUrl().isEmpty()) {
                URL url = new URL(provider.getEndpointUrl());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                responseCode = connection.getResponseCode();
                if (responseCode >= 200 && responseCode < 300) {
                    testStatus = TestStatus.SUCCESS;
                    responseMessage = "Connection successful";
                } else {
                    testStatus = TestStatus.FAILED;
                    responseMessage = "HTTP " + responseCode;
                }
                connection.disconnect();
            } else {
                testStatus = TestStatus.FAILED;
                responseMessage = "No endpoint URL configured";
            }
        } catch (Exception e) {
            testStatus = TestStatus.FAILED;
            responseMessage = "Connection failed";
            errorDetails = e.getMessage();
            log.error("Provider test failed for {}: {}", provider.getProviderCode(), e.getMessage());
        }

        int responseTimeMs = (int) (System.currentTimeMillis() - startTime);

        // Update provider test status
        provider.setLastTestedAt(LocalDateTime.now());
        provider.setLastTestStatus(testStatus);
        provider.setLastTestMessage(responseMessage);
        providerRepository.save(provider);

        // Save test history
        ProviderTestHistory testHistory = ProviderTestHistory.builder()
                .provider(provider)
                .testType("CONNECTIVITY")
                .testStatus(testStatus)
                .responseTimeMs(responseTimeMs)
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .errorDetails(errorDetails)
                .testedAt(LocalDateTime.now())
                .build();
        testHistoryRepository.save(testHistory);

        // Audit log
        auditLogService.log("PROVIDER_TESTED", "Provider", provider.getId().toString(),
                provider.getProviderName(), "TEST", null,
                Map.of("testStatus", testStatus.name(), "responseTimeMs", responseTimeMs), null);

        return ProviderTestResult.builder()
                .providerId(provider.getId())
                .providerCode(provider.getProviderCode())
                .providerName(provider.getProviderName())
                .testType("CONNECTIVITY")
                .testStatus(testStatus)
                .responseTimeMs(responseTimeMs)
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .errorDetails(errorDetails)
                .testedAt(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProviderDTO> getAllProviders(ProviderType type, Boolean isActive, String search, Pageable pageable) {
        return providerRepository.findWithFilters(type, isActive, search, pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProviderTestResult> getTestHistory(Long providerId) {
        return testHistoryRepository.findTop10ByProviderIdOrderByTestedAtDesc(providerId).stream()
                .map(this::mapTestHistoryToResult)
                .collect(Collectors.toList());
    }

    private ProviderDTO mapToDTO(Provider provider) {
        return ProviderDTO.builder()
                .id(provider.getId())
                .providerCode(provider.getProviderCode())
                .providerName(provider.getProviderName())
                .providerType(provider.getProviderType())
                .description(provider.getDescription())
                .endpointUrl(provider.getEndpointUrl())
                .authType(provider.getAuthType())
                .namespace(provider.getNamespace())
                .senderId(provider.getSenderId())
                .fromEmail(provider.getFromEmail())
                .fromName(provider.getFromName())
                .webhookUrl(provider.getWebhookUrl())
                .rateLimitPerSecond(provider.getRateLimitPerSecond())
                .rateLimitPerMinute(provider.getRateLimitPerMinute())
                .rateLimitPerDay(provider.getRateLimitPerDay())
                .additionalConfig(provider.getAdditionalConfig())
                .headersConfig(provider.getHeadersConfig())
                .isActive(provider.getIsActive())
                .isDefault(provider.getIsDefault())
                .priorityOrder(provider.getPriorityOrder())
                .lastTestedAt(provider.getLastTestedAt())
                .lastTestStatus(provider.getLastTestStatus())
                .lastTestMessage(provider.getLastTestMessage())
                .lastUsedAt(provider.getLastUsedAt())
                .successCount(provider.getSuccessCount())
                .failureCount(provider.getFailureCount())
                .createdAt(provider.getCreatedAt())
                .updatedAt(provider.getUpdatedAt())
                .createdBy(provider.getCreatedBy())
                .updatedBy(provider.getUpdatedBy())
                .build();
    }

    private ProviderTestResult mapTestHistoryToResult(ProviderTestHistory history) {
        return ProviderTestResult.builder()
                .providerId(history.getProvider().getId())
                .providerCode(history.getProvider().getProviderCode())
                .providerName(history.getProvider().getProviderName())
                .testType(history.getTestType())
                .testStatus(history.getTestStatus())
                .responseTimeMs(history.getResponseTimeMs())
                .responseCode(history.getResponseCode())
                .responseMessage(history.getResponseMessage())
                .errorDetails(history.getErrorDetails())
                .testedAt(history.getTestedAt())
                .testedBy(history.getTestedBy())
                .build();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> convertToMap(Provider provider) {
        try {
            return objectMapper.convertValue(provider, Map.class);
        } catch (Exception e) {
            log.warn("Failed to convert provider to map: {}", e.getMessage());
            return Map.of("id", provider.getId(), "providerCode", provider.getProviderCode());
        }
    }
}
