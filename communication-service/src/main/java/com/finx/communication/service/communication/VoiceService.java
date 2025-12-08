package com.finx.communication.service.communication;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finx.communication.service.IntegrationCacheService;
import com.finx.communication.domain.model.ThirdPartyIntegrationMaster;
import com.finx.communication.domain.dto.voice.*;
import com.finx.communication.domain.entity.VoiceCallLog;
import com.finx.communication.exception.ApiCallException;
import com.finx.communication.exception.ConfigurationNotFoundException;
import com.finx.communication.repository.VoiceCallLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * MSG91 Voice Service
 * Handles voice calls via MSG91 Voice API
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceService {

    private final WebClient webClient;
    private final IntegrationCacheService integrationCacheService;
    private final VoiceCallLogRepository voiceCallLogRepository;
    private final ObjectMapper objectMapper;

    private static final String INTEGRATION_NAME = "MSG91_VOICE";
    private static final String AUTH_KEY = "authkey";
    private static final String ACCEPT = "accept";
    private static final String ACCEPT_TYPE = "application/json";

    /**
     * Click to Call - Connects two numbers
     * POST /api/v5/voice/call/ctc
     */
    @SuppressWarnings("null")
    public VoiceResponse clickToCall(VoiceClickToCallRequest request) {
        log.info("Initiating click to call from {} to {}", request.getCallerId(), request.getDestination());

        // 1. Get configuration from cache
        ThirdPartyIntegrationMaster config = getIntegrationConfig();

        // 2. Get URL from config_json
        String url = config.getConfigValueAsString("click_to_call_url");

        // 3. Call MSG91 API (use API key directly - no encryption)
        String response = webClient.post()
                .uri(url)
                .header(AUTH_KEY, config.getApiKeyEncrypted())
                .header(ACCEPT, ACCEPT_TYPE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(error -> log.error("Click to call failed", error))
                .onErrorResume(error -> Mono.error(new ApiCallException("Failed to initiate click to call", error)))
                .block();

        log.info("Click to call response: {}", response);

        // 4. Save to database
        String callId = saveVoiceCallLog(request, response, "CLICK_TO_CALL");

        // 5. Return response
        return VoiceResponse.builder()
                .status("INITIATED")
                .message("Call initiated successfully")
                .callId(callId)
                .providerResponse(response)
                .build();
    }

    /**
     * Send Voice SMS
     * POST /api/v5/voice/call/
     */
    @SuppressWarnings("null")
    public VoiceResponse sendVoiceSms(VoiceSmsRequest request) {
        log.info("Sending voice SMS to: {}", request.getClientNumber());

        // 1. Get configuration from cache
        ThirdPartyIntegrationMaster config = getIntegrationConfig();

        // 2. Get URL from config_json
        String url = config.getConfigValueAsString("voice_sms_url");

        // 3. Call MSG91 API (use API key directly - no encryption)
        String response = webClient.post()
                .uri(url)
                .header(AUTH_KEY, config.getApiKeyEncrypted())
                .header(ACCEPT, ACCEPT_TYPE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(error -> log.error("Send voice SMS failed", error))
                .onErrorResume(error -> Mono.error(new ApiCallException("Failed to send voice SMS", error)))
                .block();

        log.info("Send voice SMS response: {}", response);

        // 4. Save to database
        String callId = saveVoiceSmsLog(request, response);

        // 5. Return response
        return VoiceResponse.builder()
                .status("SENT")
                .message("Voice SMS sent successfully")
                .callId(callId)
                .providerResponse(response)
                .build();
    }

    /**
     * Get Voice Call Logs
     * GET /api/v5/voice/call-logs/
     */
    @SuppressWarnings("null")
    public Map<String, Object> getVoiceLogs() {
        log.info("Fetching voice call logs");

        // 1. Get configuration from cache
        ThirdPartyIntegrationMaster config = getIntegrationConfig();

        // 2. Get URL from config_json
        String url = config.getConfigValueAsString("logs_url");

        // 3. Call MSG91 API (use API key directly - no encryption)
        String response = webClient.get()
                .uri(url)
                .header(AUTH_KEY, config.getApiKeyEncrypted())
                .header("content-type", ACCEPT_TYPE)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(error -> log.error("Get voice logs failed", error))
                .onErrorResume(error -> Mono.error(new ApiCallException("Failed to get voice logs", error)))
                .block();

        log.info("Voice logs response: {}", response);

        try {
            return objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            return Map.of("raw_response", response);
        }
    }

    // ==================== Helper Methods ====================

    private ThirdPartyIntegrationMaster getIntegrationConfig() {
        return integrationCacheService.getIntegration(INTEGRATION_NAME)
                .orElseThrow(() -> new ConfigurationNotFoundException(INTEGRATION_NAME));
    }

    @SuppressWarnings("null")
    private String saveVoiceCallLog(VoiceClickToCallRequest request, String response, String callType) {
        String callId = UUID.randomUUID().toString();

        VoiceCallLog voiceCallLog = VoiceCallLog.builder()
                .callId(callId)
                .provider("MSG91")
                .customerMobile(request.getDestination())
                .callerId(request.getCallerId())
                .callType(callType)
                .callStatus("INITIATED")
                .initiatedAt(LocalDateTime.now())
                .build();

        try {
            Map<String, Object> responseMap = objectMapper.readValue(
                    response,
                    new TypeReference<Map<String, Object>>() {
                    });
            voiceCallLog.setProviderResponse(responseMap);
        } catch (Exception e) {
            log.warn("Failed to parse voice call response", e);
        }

        voiceCallLogRepository.save(voiceCallLog);
        return callId;
    }

    @SuppressWarnings("null")
    private String saveVoiceSmsLog(VoiceSmsRequest request, String response) {
        String callId = UUID.randomUUID().toString();

        VoiceCallLog voiceCallLog = VoiceCallLog.builder()
                .callId(callId)
                .provider("MSG91")
                .customerMobile(request.getClientNumber())
                .callerId(request.getCallerId())
                .callType("VOICE_SMS")
                .template(request.getTemplate())
                .callStatus("SENT")
                .initiatedAt(LocalDateTime.now())
                .build();

        try {
            Map<String, Object> responseMap = objectMapper.readValue(
                    response,
                    new TypeReference<Map<String, Object>>() {
                    });
            voiceCallLog.setProviderResponse(responseMap);
        } catch (Exception e) {
            log.warn("Failed to parse voice SMS response", e);
        }

        voiceCallLogRepository.save(voiceCallLog);
        return callId;
    }
}
