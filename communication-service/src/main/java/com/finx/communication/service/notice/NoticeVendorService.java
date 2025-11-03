package com.finx.communication.service.notice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finx.common.service.IntegrationCacheService;
import com.finx.communication.domain.dto.notice.NoticeDispatchRequest;
import com.finx.communication.domain.dto.notice.NoticeDispatchResponse;
import com.finx.common.model.ThirdPartyIntegrationMaster;
import com.finx.common.exception.ApiCallException;
import com.finx.common.exception.ConfigurationNotFoundException;
import com.finx.common.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeVendorService {

    private final WebClient webClient;
    private final IntegrationCacheService integrationCacheService;
    private final ObjectMapper objectMapper;
    private final EncryptionUtil encryptionUtil;

    private static final String INTEGRATION_NAME = "NOTICE_VENDOR";

    public NoticeDispatchResponse dispatchNotice(NoticeDispatchRequest request) {
        log.info("Dispatching notice {} via {}", request.getNoticeId(), request.getDispatchMethod());

        // 1. Get configuration
        ThirdPartyIntegrationMaster config = getIntegrationConfig();

        // 2. Build request body
        Map<String, Object> requestBody = buildDispatchRequestBody(request);

        // 3. Call Vendor API
        String url = config.getApiEndpoint() + "/api/dispatch/create";
        String response = callVendorApi(url, requestBody, config);

        // 4. Parse response and extract tracking info
        Map<String, Object> responseMap = parseResponse(response);

        return NoticeDispatchResponse.builder()
                .noticeId(request.getNoticeId())
                .vendorJobId(extractValue(responseMap, "job_id"))
                .trackingNumber(extractValue(responseMap, "tracking_number"))
                .status("DISPATCHED")
                .message("Notice dispatched successfully")
                .providerResponse(response)
                .build();
    }

    public Map<String, Object> trackNotice(String trackingNumber) {
        log.info("Tracking notice: {}", trackingNumber);

        // 1. Get configuration
        ThirdPartyIntegrationMaster config = getIntegrationConfig();

        // 2. Build URL
        String url = config.getApiEndpoint() + "/api/dispatch/track?tracking_number=" + trackingNumber;

        // 3. Call Vendor API
        String response = webClient.get()
                .uri(url)
                .header("Authorization", "Bearer " + encryptionUtil.decrypt(config.getApiKeyEncrypted()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return parseResponse(response);
    }

    private ThirdPartyIntegrationMaster getIntegrationConfig() {
        return integrationCacheService.getIntegration(INTEGRATION_NAME)
                .orElseThrow(() -> new ConfigurationNotFoundException(INTEGRATION_NAME));
    }

    private Map<String, Object> buildDispatchRequestBody(NoticeDispatchRequest request) {
        Map<String, Object> body = new HashMap<>();

        body.put("notice_id", request.getNoticeId());
        body.put("case_id", request.getCaseId());
        body.put("recipient_name", request.getRecipientName());
        body.put("recipient_address", request.getRecipientAddress());
        body.put("recipient_mobile", request.getRecipientMobile());
        body.put("recipient_pincode", request.getRecipientPincode());
        body.put("dispatch_method", request.getDispatchMethod());
        body.put("pdf_url", request.getPdfUrl());

        // Add additional parameters if present
        if (request.getAdditionalParams() != null) {
            body.putAll(request.getAdditionalParams());
        }

        return body;
    }

    private String callVendorApi(String url, Map<String, Object> body, ThirdPartyIntegrationMaster config) {
        try {
            return webClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + encryptionUtil.decrypt(config.getApiKeyEncrypted()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnError(error -> log.error("Vendor API call failed", error))
                    .onErrorResume(error -> Mono.error(new ApiCallException("Failed to dispatch notice", error)))
                    .block();
        } catch (Exception e) {
            throw new ApiCallException("Failed to call Vendor API", e);
        }
    }

    private Map<String, Object> parseResponse(String response) {
        try {
            return objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse vendor response", e);
            return Map.of("raw_response", response);
        }
    }

    private String extractValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
}
