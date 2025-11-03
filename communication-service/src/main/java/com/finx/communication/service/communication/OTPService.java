package com.finx.communication.service.communication;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finx.common.service.ConfigCacheService;
import com.finx.common.service.IntegrationCacheService;
import com.finx.communication.domain.dto.otp.*;
import com.finx.common.model.OtpRequest;
import com.finx.common.model.ThirdPartyIntegrationMaster;
import com.finx.communication.exception.ConfigurationNotFoundException;
import com.finx.communication.repository.OtpRequestRepository;
import com.finx.common.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.*;

/**
 * OTP Service - Msg91 Integration
 * Handles OTP generation, verification, and resend
 *
 * MSG91 OTP API Documentation:
 * - Send OTP: POST https://control.msg91.com/api/v5/otp
 * - Verify OTP: GET https://control.msg91.com/api/v5/otp/verify
 * - Resend OTP: GET https://control.msg91.com/api/v5/otp/retry
 * - Analytics: GET https://control.msg91.com/api/v5/report/analytics/p/otp
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OTPService {

    private final WebClient webClient;
    private final IntegrationCacheService integrationCacheService;
    private final ConfigCacheService configCacheService;
    private final OtpRequestRepository otpRequestRepository;
    private final ObjectMapper objectMapper;
    private final EncryptionUtil encryptionUtil;

    private static final String INTEGRATION_NAME = "MSG91_OTP";

    /**
     * Send OTP via Msg91
     * API: POST https://control.msg91.com/api/v5/otp
     *
     * Request format:
     * URL params: mobile, authkey, otp_expiry, template_id, realTimeResponse=1
     * Body: JSON with dynamic params like {"Param1": "value"}
     *
     * Response format:
     * Success: {"type": "success", "request_id": "..."}
     * Error: {"type": "error", "message": "..."}
     */
    public Map<String, Object> sendOtp(OtpSendRequest request) {
        try {
            log.info("Sending OTP to mobile: {}", request.getMobile());

            // 1. Get integration config from cache (URL + API Key)
            ThirdPartyIntegrationMaster config = getIntegrationConfig();

            // 2. Get OTP expiry from system_config or use from request
            Integer otpExpiry = request.getOtpExpiry() != null ? request.getOtpExpiry() : getOtpExpirySeconds();

            // 3. Build MSG91 API URL with query parameters (template_id from request)
            String url = buildSendOtpUrl(config, request.getMobile(), otpExpiry, request.getTemplateId());

            // 4. Build request body (dynamic parameters)
            Map<String, Object> requestBody = buildOtpRequestBody(request);

            // 5. Call MSG91 API
            String rawResponse = webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Content-Type", "application/JSON")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnError(error -> log.error("MSG91 OTP send failed", error))
                    .block();

            log.info("MSG91 send OTP response: {}", rawResponse);

            // 6. Parse response
            Map<String, Object> response = objectMapper.readValue(rawResponse, new TypeReference<>() {});

            // 7. Save to database (only if successful)
            if ("success".equals(response.get("type"))) {
                Object requestIdObj = response.get("request_id");
                if (requestIdObj != null) {
                    String requestId = requestIdObj.toString();
                    saveOtpRequest(request, requestId, rawResponse, otpExpiry);
                } else {
                    log.warn("MSG91 success response missing request_id: {}", rawResponse);
                }
            }

            // 8. Return raw MSG91 response
            return response;

        } catch (Exception e) {
            log.error("Error sending OTP", e);
            return Map.of(
                    "type", "error",
                    "message", e.getMessage() != null ? e.getMessage() : "Failed to send OTP"
            );
        }
    }

    /**
     * Verify OTP
     * API: GET https://control.msg91.com/api/v5/otp/verify
     *
     * Request format:
     * URL params: otp, mobile
     * Header: authkey
     *
     * Response format:
     * Success: {"type": "success", "message": "OTP verified success"}
     * Error: {"code": "201", "type": "error", "message": "Invalid authkey"}
     */
    public Map<String, Object> verifyOtp(OtpVerifyRequest request) {
        try {
            log.info("Verifying OTP for mobile: {}", request.getMobile());

            // 1. Get integration config from cache
            ThirdPartyIntegrationMaster config = getIntegrationConfig();

            // 2. Build verify URL with query parameters
            String url = config.getApiEndpoint() + "/api/v5/otp/verify" +
                    "?otp=" + request.getOtp() +
                    "&mobile=" + request.getMobile();

            // 3. Call MSG91 API with authkey header
            String rawResponse = webClient.get()
                    .uri(url)
                    .header("authkey", encryptionUtil.decrypt(config.getApiKeyEncrypted()))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnError(error -> log.error("MSG91 OTP verify failed", error))
                    .block();

            log.info("MSG91 verify OTP response: {}", rawResponse);

            // 4. Parse response
            Map<String, Object> response = objectMapper.readValue(rawResponse, new TypeReference<>() {});

            // 5. Update database asynchronously (only if successful)
            if ("success".equals(response.get("type"))) {
                updateOtpVerification(request.getMobile(), rawResponse);
            }

            // 6. Return raw MSG91 response
            return response;

        } catch (Exception e) {
            log.error("Error verifying OTP", e);
            return Map.of(
                    "code", "500",
                    "type", "error",
                    "message", e.getMessage() != null ? e.getMessage() : "Failed to verify OTP"
            );
        }
    }

    /**
     * Resend OTP
     * API: GET https://control.msg91.com/api/v5/otp/retry
     *
     * Request format:
     * URL params: mobile, authkey, retrytype=text
     *
     * Response format:
     * Success: {"type": "success", "message": "retry send successfully"}
     * Error: {"code": "201", "type": "error", "message": "Invalid authkey"}
     */
    public Map<String, Object> resendOtp(String mobile) {
        try {
            log.info("Resending OTP to mobile: {}", mobile);

            // 1. Get integration config from cache
            ThirdPartyIntegrationMaster config = getIntegrationConfig();

            // 2. Build resend URL with query parameters
            String url = config.getApiEndpoint() + "/api/v5/otp/retry" +
                    "?mobile=" + mobile +
                    "&authkey=" + encryptionUtil.decrypt(config.getApiKeyEncrypted()) +
                    "&retrytype=text";

            // 3. Call MSG91 API
            String rawResponse = webClient.get()
                    .uri(url)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnError(error -> log.error("MSG91 OTP resend failed", error))
                    .block();

            log.info("MSG91 resend OTP response: {}", rawResponse);

            // 4. Parse response
            Map<String, Object> response = objectMapper.readValue(rawResponse, new TypeReference<>() {});

            // 5. Return raw MSG91 response
            return response;

        } catch (Exception e) {
            log.error("Error resending OTP", e);
            return Map.of(
                    "code", "500",
                    "type", "error",
                    "message", e.getMessage() != null ? e.getMessage() : "Failed to resend OTP"
            );
        }
    }

    /**
     * Get OTP Analytics
     * API: GET https://control.msg91.com/api/v5/report/analytics/p/otp
     *
     * Request format:
     * URL params: authkey, endDate, startDate
     * Header: content-type: text/plain
     *
     * Response format:
     * Success: {"data": [...], "total": {...}}
     * Error: {"code": "401", "errors": "Unauthorized", "status": "fail", "apiError": null, "hasError": true}
     */
    public Map<String, Object> getAnalytics(String startDate, String endDate) {
        try {
            log.info("Fetching OTP analytics from {} to {}", startDate, endDate);

            // 1. Get integration config from cache
            ThirdPartyIntegrationMaster config = getIntegrationConfig();

            // 2. Build analytics URL with query parameters
            String url = config.getApiEndpoint() + "/api/v5/report/analytics/p/otp" +
                    "?authkey=" + encryptionUtil.decrypt(config.getApiKeyEncrypted()) +
                    "&endDate=" + endDate +
                    "&startDate=" + startDate;

            // 3. Call MSG91 API
            String rawResponse = webClient.get()
                    .uri(url)
                    .header("content-type", "text/plain")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnError(error -> log.error("MSG91 OTP analytics failed", error))
                    .block();

            log.info("MSG91 analytics response: {}", rawResponse);

            // 4. Parse and return raw MSG91 response
            return objectMapper.readValue(rawResponse, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Error fetching OTP analytics", e);
            return Map.of(
                    "code", "500",
                    "errors", e.getMessage() != null ? e.getMessage() : "Failed to fetch analytics",
                    "status", "fail",
                    "apiError", null,
                    "hasError", true
            );
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Get MSG91_OTP integration config from cache
     */
    private ThirdPartyIntegrationMaster getIntegrationConfig() {
        return integrationCacheService.getIntegration(INTEGRATION_NAME)
                .orElseThrow(() -> new ConfigurationNotFoundException(INTEGRATION_NAME));
    }

    /**
     * Get OTP expiry in seconds from system_config (cached)
     */
    private Integer getOtpExpirySeconds() {
        return configCacheService.getIntConfig("OTP_EXPIRY_SECONDS", 300); // Default 5 minutes
    }

    /**
     * Build MSG91 send OTP URL with query parameters
     * Format: https://control.msg91.com/api/v5/otp?mobile=...&authkey=...&otp_expiry=...&template_id=...&realTimeResponse=1
     */
    private String buildSendOtpUrl(ThirdPartyIntegrationMaster config, String mobile, Integer otpExpiry, String templateId) {
        StringBuilder url = new StringBuilder(config.getApiEndpoint());
        url.append("/api/v5/otp");
        url.append("?mobile=").append(mobile);
        url.append("&authkey=").append(encryptionUtil.decrypt(config.getApiKeyEncrypted()));
        url.append("&otp_expiry=").append(otpExpiry);

        if (templateId != null) {
            url.append("&template_id=").append(templateId);
        }

        url.append("&realTimeResponse=1");

        return url.toString();
    }

    /**
     * Build request body with dynamic parameters
     * Example: {"Param1": "Naveen"}
     */
    private Map<String, Object> buildOtpRequestBody(OtpSendRequest request) {
        Map<String, Object> body = new HashMap<>();

        // Add dynamic parameters if present
        if (request.getParams() != null && !request.getParams().isEmpty()) {
            body.putAll(request.getParams());
        }

        return body;
    }

    /**
     * Save OTP request to database (Async)
     * Uses @Async to not block the API response
     * Executes in background thread pool configured in AsyncConfig
     */
    @Async("taskExecutor")
    private void saveOtpRequest(OtpSendRequest request, String requestId, String providerResponse, Integer otpExpiry) {
        try {
            log.debug("Async saving OTP request with ID: {}", requestId);

            OtpRequest otpRequest = OtpRequest.builder()
                    .requestId(requestId)
                    .mobile(request.getMobile())
                    .email(request.getEmail())
                    .otpCode("") // MSG91 generates OTP, we don't know it
                    .otpHash("") // MSG91 manages OTP internally
                    .channel(request.getChannel() != null ? request.getChannel() : "SMS")
                    .purpose(request.getPurpose() != null ? request.getPurpose() : "LOGIN")
                    .status("SENT")
                    .provider("MSG91")
                    .providerRequestId(requestId)
                    .providerResponse(providerResponse)
                    .sentAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusSeconds(otpExpiry))
                    .build();

            otpRequestRepository.save(otpRequest);
            log.info("Successfully saved OTP request with ID: {} (async)", requestId);
        } catch (Exception e) {
            log.error("Failed to save OTP request to database (async): {}", requestId, e);
            // Don't throw - async method, already logged
        }
    }

    /**
     * Update OTP verification status (Async)
     * Uses @Async to not block the API response
     */
    @Async("taskExecutor")
    private void updateOtpVerification(String mobile, String rawResponse) {
        try {
            log.debug("Async updating OTP verification for mobile: {}", mobile);

            Optional<OtpRequest> otpRequestOpt = otpRequestRepository
                    .findTopByMobileAndStatusOrderByCreatedAtDesc(mobile, "SENT");

            if (otpRequestOpt.isPresent()) {
                OtpRequest otpRequest = otpRequestOpt.get();
                otpRequest.setStatus("VERIFIED");
                otpRequest.setVerifiedAt(LocalDateTime.now());
                otpRequest.setProviderResponse(rawResponse);
                otpRequestRepository.save(otpRequest);
                log.info("Successfully updated OTP verification for mobile: {} (async)", mobile);
            } else {
                log.warn("No pending OTP found for mobile: {}", mobile);
            }
        } catch (Exception e) {
            log.error("Failed to update OTP verification (async): {}", mobile, e);
            // Don't throw - async method, already logged
        }
    }
}
