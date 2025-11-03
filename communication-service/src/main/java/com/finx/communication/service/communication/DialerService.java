package com.finx.communication.service.communication;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finx.common.service.IntegrationCacheService;
import com.finx.communication.domain.dto.dialer.*;
import com.finx.communication.domain.entity.DialerCallLog;
import com.finx.common.model.ThirdPartyIntegrationMaster;
import com.finx.communication.exception.ApiCallException;
import com.finx.communication.exception.ConfigurationNotFoundException;
import com.finx.communication.repository.DialerCallLogRepository;
import com.finx.common.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DialerService {

    private final WebClient webClient;
    private final IntegrationCacheService integrationCacheService;
    private final DialerCallLogRepository dialerCallLogRepository;
    private final ObjectMapper objectMapper;
    private final EncryptionUtil encryptionUtil;

    private static final String INTEGRATION_NAME = "OZONETEL_DIALER";

    public DialerResponse initiateCall(DialerCallRequest request) {
        log.info("Initiating dialer call to: {}", request.getCustomerMobile());

        // 1. Get configuration
        ThirdPartyIntegrationMaster config = getIntegrationConfig();

        // 2. Build request body
        Map<String, Object> requestBody = Map.of(
                "customer_mobile", request.getCustomerMobile(),
                "agent_number", request.getAgentNumber() != null ? request.getAgentNumber() : "",
                "call_type", request.getCallType() != null ? request.getCallType() : "CLICK_TO_CALL"
        );

        // 3. Call Dialer API
        String url = config.getApiEndpoint() + "/api/click-to-call";
        String response = callDialerApi(url, requestBody, config);

        // 4. Save to database
        String callId = saveDialerCallLog(request, response);

        return DialerResponse.builder()
                .callId(callId)
                .status("INITIATED")
                .message("Call initiated successfully")
                .providerResponse(response)
                .build();
    }

    public DialerResponse getCallStatus(String callId) {
        log.info("Fetching call status for: {}", callId);

        Optional<DialerCallLog> callLogOpt = dialerCallLogRepository.findByCallId(callId);

        if (callLogOpt.isEmpty()) {
            throw new ApiCallException("Call log not found");
        }

        DialerCallLog callLog = callLogOpt.get();

        return DialerResponse.builder()
                .callId(callLog.getCallId())
                .dialerCallId(callLog.getDialerCallId())
                .status(callLog.getCallStatus())
                .message("Call status retrieved")
                .build();
    }

    private ThirdPartyIntegrationMaster getIntegrationConfig() {
        return integrationCacheService.getIntegration(INTEGRATION_NAME)
                .orElseThrow(() -> new ConfigurationNotFoundException(INTEGRATION_NAME));
    }

    private String callDialerApi(String url, Map<String, Object> body, ThirdPartyIntegrationMaster config) {
        try {
            return webClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + encryptionUtil.decrypt(config.getApiKeyEncrypted()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnError(error -> log.error("Dialer API call failed", error))
                    .onErrorResume(error -> Mono.error(new ApiCallException("Failed to initiate call", error)))
                    .block();
        } catch (Exception e) {
            throw new ApiCallException("Failed to call Dialer API", e);
        }
    }

    private String saveDialerCallLog(DialerCallRequest request, String response) {
        String callId = UUID.randomUUID().toString();

        DialerCallLog callLog = DialerCallLog.builder()
                .callId(callId)
                .dialerName("OZONETEL")
                .agentId(request.getAgentId())
                .caseId(request.getCaseId())
                .customerMobile(request.getCustomerMobile())
                .callType(request.getCallType() != null ? request.getCallType() : "CLICK_TO_CALL")
                .callStatus("INITIATED")
                .disposition(request.getDisposition())
                .notes(request.getNotes())
                .initiatedAt(LocalDateTime.now())
                .build();

        try {
            Map<String, Object> responseMap = objectMapper.readValue(
                response,
                new TypeReference<Map<String, Object>>() {}
            );
            callLog.setDialerResponse(responseMap);
        } catch (Exception e) {
            log.warn("Failed to parse dialer response", e);
        }

        dialerCallLogRepository.save(callLog);
        return callId;
    }
}
