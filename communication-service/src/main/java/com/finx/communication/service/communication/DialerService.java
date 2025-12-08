package com.finx.communication.service.communication;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finx.communication.service.IntegrationCacheService;
import com.finx.communication.domain.dto.dialer.*;
import com.finx.communication.domain.entity.DialerCallLog;
import com.finx.communication.domain.model.ThirdPartyIntegrationMaster;
import com.finx.communication.exception.ApiCallException;
import com.finx.communication.exception.ConfigurationNotFoundException;
import com.finx.communication.repository.DialerCallLogRepository;
import com.finx.communication.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Telecalling/Dialer Service
 *
 * Vendor-agnostic service layer for telecalling operations.
 * Currently contains placeholder implementations - actual vendor integration
 * to be implemented once third-party provider is finalized.
 *
 * Potential vendors: Ozonetel, Knowlarity, Exotel, Ameyo, etc.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DialerService {

    private final WebClient webClient;
    private final IntegrationCacheService integrationCacheService;
    private final DialerCallLogRepository dialerCallLogRepository;
    private final ObjectMapper objectMapper;
    private final EncryptionUtil encryptionUtil;

    private static final String INTEGRATION_NAME = "DIALER_PROVIDER"; // To be configured once vendor is finalized
    private static final String DEFAULT_DIALER_NAME = "PLACEHOLDER";

    // ==================== Call Operations ====================

    public DialerResponse initiateCall(DialerCallRequest request) {
        log.info("Initiating dialer call to: {}", request.getCustomerMobile());

        // Generate internal call ID
        String callId = UUID.randomUUID().toString();

        // Save to database
        DialerCallLog callLog = DialerCallLog.builder()
                .callId(callId)
                .dialerName(DEFAULT_DIALER_NAME)
                .agentId(request.getAgentId())
                .caseId(request.getCaseId())
                .customerMobile(request.getCustomerMobile())
                .callType(request.getCallType() != null ? request.getCallType() : "CLICK_TO_CALL")
                .callStatus("INITIATED")
                .disposition(request.getDisposition())
                .notes(request.getNotes())
                .initiatedAt(LocalDateTime.now())
                .build();

        dialerCallLogRepository.save(callLog);

        // TODO: Implement actual vendor API call once provider is finalized
        // ThirdPartyIntegrationMaster config = getIntegrationConfig();
        // String response = callDialerApi(config.getApiEndpoint() + "/click-to-call", requestBody, config);

        return DialerResponse.builder()
                .callId(callId)
                .status("INITIATED")
                .message("Call initiated successfully (placeholder - vendor integration pending)")
                .build();
    }

    public DialerResponse getCallStatus(String callId) {
        log.info("Fetching call status for: {}", callId);

        DialerCallLog callLog = dialerCallLogRepository.findByCallId(callId)
                .orElseThrow(() -> new ApiCallException("Call log not found: " + callId));

        return DialerResponse.builder()
                .callId(callLog.getCallId())
                .dialerCallId(callLog.getDialerCallId())
                .status(callLog.getCallStatus())
                .message("Call status retrieved")
                .build();
    }

    public DialerResponse endCall(String callId) {
        log.info("Ending call: {}", callId);

        DialerCallLog callLog = dialerCallLogRepository.findByCallId(callId)
                .orElseThrow(() -> new ApiCallException("Call log not found: " + callId));

        callLog.setCallStatus("ENDED");
        callLog.setEndedAt(LocalDateTime.now());
        dialerCallLogRepository.save(callLog);

        // TODO: Implement actual vendor API call to end call

        return DialerResponse.builder()
                .callId(callId)
                .status("ENDED")
                .message("Call ended successfully (placeholder)")
                .build();
    }

    public DialerResponse transferCall(String callId, String targetNumber, String transferType) {
        log.info("Transferring call {} to {} (type: {})", callId, targetNumber, transferType);

        DialerCallLog callLog = dialerCallLogRepository.findByCallId(callId)
                .orElseThrow(() -> new ApiCallException("Call log not found: " + callId));

        callLog.setCallStatus("TRANSFERRED");
        dialerCallLogRepository.save(callLog);

        // TODO: Implement actual vendor API call for transfer

        return DialerResponse.builder()
                .callId(callId)
                .status("TRANSFERRED")
                .message("Call transfer initiated to " + targetNumber + " (placeholder)")
                .build();
    }

    public DialerResponse holdCall(String callId, Boolean hold) {
        log.info("{} call: {}", hold ? "Holding" : "Resuming", callId);

        DialerCallLog callLog = dialerCallLogRepository.findByCallId(callId)
                .orElseThrow(() -> new ApiCallException("Call log not found: " + callId));

        callLog.setCallStatus(hold ? "ON_HOLD" : "ACTIVE");
        dialerCallLogRepository.save(callLog);

        // TODO: Implement actual vendor API call for hold/unhold

        return DialerResponse.builder()
                .callId(callId)
                .status(callLog.getCallStatus())
                .message(hold ? "Call placed on hold (placeholder)" : "Call resumed (placeholder)")
                .build();
    }

    // ==================== Queue Operations ====================

    public DialerQueueResponse queueCalls(DialerQueueRequest request) {
        log.info("Queueing {} calls for campaign: {}", request.getCustomers().size(), request.getCampaignId());

        String batchId = UUID.randomUUID().toString();
        List<DialerQueueResponse.QueuedCallItem> queuedCalls = new ArrayList<>();
        int successCount = 0;

        for (DialerQueueRequest.CustomerQueueItem customer : request.getCustomers()) {
            String callId = UUID.randomUUID().toString();

            // Save each call to queue
            DialerCallLog callLog = DialerCallLog.builder()
                    .callId(callId)
                    .dialerName(DEFAULT_DIALER_NAME)
                    .agentId(request.getAgentId())
                    .caseId(customer.getCaseId())
                    .customerMobile(customer.getCustomerMobile())
                    .callType(request.getDialMode() != null ? request.getDialMode() : "AUTO_DIAL")
                    .callStatus("QUEUED")
                    .queuePriority(customer.getPriority() != null ? customer.getPriority() : request.getPriority())
                    .queuedAt(LocalDateTime.now())
                    .build();

            dialerCallLogRepository.save(callLog);
            successCount++;

            queuedCalls.add(DialerQueueResponse.QueuedCallItem.builder()
                    .caseId(customer.getCaseId())
                    .customerMobile(customer.getCustomerMobile())
                    .callId(callId)
                    .status("QUEUED")
                    .queuePosition(successCount)
                    .build());
        }

        // TODO: Implement actual vendor API call to queue calls

        return DialerQueueResponse.builder()
                .batchId(batchId)
                .totalQueued(request.getCustomers().size())
                .successCount(successCount)
                .failedCount(0)
                .status("QUEUED")
                .message("Calls queued successfully (placeholder)")
                .queuedAt(LocalDateTime.now())
                .queuedCalls(queuedCalls)
                .build();
    }

    public void removeFromQueue(String batchId) {
        log.info("Removing batch from queue: {}", batchId);
        // TODO: Implement removal of queued calls by batch ID
        // This would typically update all calls with matching batch ID to CANCELLED status
    }

    public void removeCallFromQueue(String callId) {
        log.info("Removing call from queue: {}", callId);

        DialerCallLog callLog = dialerCallLogRepository.findByCallId(callId)
                .orElseThrow(() -> new ApiCallException("Call log not found: " + callId));

        if ("QUEUED".equals(callLog.getCallStatus())) {
            callLog.setCallStatus("CANCELLED");
            dialerCallLogRepository.save(callLog);
        }
    }

    public DialerQueueResponse getQueueStatus(String batchId) {
        log.info("Getting queue status for batch: {}", batchId);

        // TODO: Implement actual queue status lookup

        return DialerQueueResponse.builder()
                .batchId(batchId)
                .status("QUEUED")
                .message("Queue status retrieved (placeholder)")
                .build();
    }

    // ==================== Disposition Operations ====================

    public DialerResponse updateDisposition(DialerDispositionRequest request) {
        log.info("Updating disposition for call: {} with code: {}", request.getCallId(), request.getDispositionCode());

        DialerCallLog callLog = dialerCallLogRepository.findByCallId(request.getCallId())
                .orElseThrow(() -> new ApiCallException("Call log not found: " + request.getCallId()));

        callLog.setDisposition(request.getDispositionCode());
        callLog.setNotes(request.getNotes());

        // Store callback info in notes if provided
        if (request.getCallbackDateTime() != null) {
            callLog.setNotes((callLog.getNotes() != null ? callLog.getNotes() + "\n" : "") +
                    "Callback scheduled: " + request.getCallbackDateTime());
        }

        dialerCallLogRepository.save(callLog);

        return DialerResponse.builder()
                .callId(request.getCallId())
                .status("UPDATED")
                .message("Disposition updated successfully")
                .build();
    }

    public List<String> getDispositionCodes() {
        // Return standard collection disposition codes
        return Arrays.asList(
                "PTP",              // Promise to Pay
                "RTP",              // Refused to Pay
                "CALLBACK",         // Customer requested callback
                "NOT_INTERESTED",   // Not interested
                "WRONG_NUMBER",     // Wrong number
                "NUMBER_BUSY",      // Number was busy
                "NO_ANSWER",        // No answer
                "RINGING_NO_ANSWER",// Rang but no answer
                "SWITCHED_OFF",     // Phone switched off
                "NOT_REACHABLE",    // Not reachable
                "DISPUTE",          // Customer has dispute
                "ALREADY_PAID",     // Already paid
                "PAYMENT_PARTIAL",  // Partial payment done
                "DECEASED",         // Customer deceased
                "BANKRUPTCY",       // Customer filed bankruptcy
                "LANGUAGE_BARRIER", // Language barrier
                "THIRD_PARTY",      // Spoke to third party
                "AGENT_HANG_UP",    // Agent hung up
                "CUSTOMER_HANG_UP", // Customer hung up
                "SETTLED",          // Settlement discussed
                "LEGAL_ACTION",     // Legal action intimated
                "OTHER"             // Other
        );
    }

    // ==================== Call History Operations ====================

    public DialerCallHistoryResponse getCallHistory(DialerCallHistoryRequest request) {
        log.info("Getting call history with filters");

        // TODO: Implement proper pagination and filtering using JPA Specification
        List<DialerCallLog> callLogs;

        if (request.getCaseId() != null) {
            callLogs = dialerCallLogRepository.findByCaseId(request.getCaseId());
        } else if (request.getAgentId() != null) {
            callLogs = dialerCallLogRepository.findByAgentId(request.getAgentId());
        } else {
            callLogs = dialerCallLogRepository.findAll();
        }

        List<DialerCallHistoryResponse.CallHistoryItem> items = callLogs.stream()
                .map(this::mapToCallHistoryItem)
                .collect(Collectors.toList());

        return DialerCallHistoryResponse.builder()
                .calls(items)
                .page(request.getPage() != null ? request.getPage() : 0)
                .size(request.getSize() != null ? request.getSize() : 20)
                .totalElements((long) items.size())
                .totalPages(1)
                .hasNext(false)
                .build();
    }

    public DialerCallHistoryResponse getCallHistoryByCase(Long caseId) {
        log.info("Getting call history for case: {}", caseId);

        List<DialerCallLog> callLogs = dialerCallLogRepository.findByCaseId(caseId);

        List<DialerCallHistoryResponse.CallHistoryItem> items = callLogs.stream()
                .map(this::mapToCallHistoryItem)
                .collect(Collectors.toList());

        return DialerCallHistoryResponse.builder()
                .calls(items)
                .totalElements((long) items.size())
                .build();
    }

    public String getCallRecordingUrl(String callId) {
        log.info("Getting recording URL for call: {}", callId);

        DialerCallLog callLog = dialerCallLogRepository.findByCallId(callId)
                .orElseThrow(() -> new ApiCallException("Call log not found: " + callId));

        return callLog.getRecordingUrl();
    }

    private DialerCallHistoryResponse.CallHistoryItem mapToCallHistoryItem(DialerCallLog callLog) {
        return DialerCallHistoryResponse.CallHistoryItem.builder()
                .callId(callLog.getCallId())
                .dialerCallId(callLog.getDialerCallId())
                .caseId(callLog.getCaseId())
                .agentId(callLog.getAgentId())
                .customerMobile(callLog.getCustomerMobile())
                .callType(callLog.getCallType())
                .callStatus(callLog.getCallStatus())
                .callDuration(callLog.getCallDuration())
                .recordingUrl(callLog.getRecordingUrl())
                .dispositionCode(callLog.getDisposition())
                .notes(callLog.getNotes())
                .initiatedAt(callLog.getInitiatedAt())
                .answeredAt(callLog.getAnsweredAt())
                .endedAt(callLog.getEndedAt())
                .build();
    }

    // ==================== Agent Operations ====================

    public DialerAgentStatusResponse updateAgentStatus(DialerAgentStatusRequest request) {
        log.info("Updating agent {} status to: {}", request.getAgentId(), request.getStatus());

        // TODO: Implement actual vendor API call for agent status update
        // This would typically update agent status in the dialer system

        return DialerAgentStatusResponse.builder()
                .agentId(request.getAgentId())
                .status(request.getStatus())
                .previousStatus("UNKNOWN")
                .statusChangedAt(LocalDateTime.now())
                .message("Agent status updated (placeholder)")
                .build();
    }

    public DialerAgentStatusResponse getAgentStatus(Long agentId) {
        log.info("Getting status for agent: {}", agentId);

        // TODO: Implement actual vendor API call to get agent status

        return DialerAgentStatusResponse.builder()
                .agentId(agentId)
                .status("AVAILABLE")
                .message("Agent status retrieved (placeholder)")
                .build();
    }

    public DialerAgentStatusResponse getAllAgentsStatus() {
        log.info("Getting all agents status");

        // TODO: Implement actual vendor API call to get all agents status

        return DialerAgentStatusResponse.builder()
                .agents(Collections.emptyList())
                .message("All agents status retrieved (placeholder)")
                .build();
    }

    // ==================== Analytics & Reporting ====================

    public DialerCampaignStatsResponse getCampaignStats(String campaignId, LocalDate date) {
        log.info("Getting stats for campaign: {} on date: {}", campaignId, date);

        // TODO: Implement actual statistics calculation from call logs

        return DialerCampaignStatsResponse.builder()
                .campaignId(campaignId)
                .date(date != null ? date : LocalDate.now())
                .totalCalls(0)
                .answeredCalls(0)
                .missedCalls(0)
                .contactRate(0.0)
                .build();
    }

    public DialerCampaignStatsResponse getDailyStats(LocalDate date) {
        log.info("Getting daily stats for: {}", date);

        // TODO: Implement actual daily statistics calculation

        return DialerCampaignStatsResponse.builder()
                .date(date != null ? date : LocalDate.now())
                .totalCalls(0)
                .answeredCalls(0)
                .build();
    }

    public DialerCampaignStatsResponse getAgentStats(Long agentId, LocalDate date) {
        log.info("Getting stats for agent: {} on date: {}", agentId, date);

        // TODO: Implement actual agent statistics calculation

        return DialerCampaignStatsResponse.builder()
                .date(date != null ? date : LocalDate.now())
                .totalCalls(0)
                .answeredCalls(0)
                .build();
    }

    // ==================== Webhooks ====================

    public void handleCallback(DialerCallbackRequest request) {
        log.info("Processing dialer callback for call: {} status: {}",
                request.getCallId(), request.getCallStatus());

        Optional<DialerCallLog> callLogOpt = dialerCallLogRepository.findByCallId(request.getCallId());

        if (callLogOpt.isEmpty() && request.getDialerCallId() != null) {
            callLogOpt = dialerCallLogRepository.findByDialerCallId(request.getDialerCallId());
        }

        if (callLogOpt.isPresent()) {
            DialerCallLog callLog = callLogOpt.get();
            callLog.setCallStatus(request.getCallStatus());
            callLog.setCallDuration(request.getCallDuration());
            callLog.setRecordingUrl(request.getRecordingUrl());
            callLog.setDisposition(request.getDisposition());
            callLog.setDialerResponse(request.getProviderData());

            if (request.getAnsweredAt() != null) {
                callLog.setAnsweredAt(LocalDateTime.parse(request.getAnsweredAt()));
            }
            if (request.getEndedAt() != null) {
                callLog.setEndedAt(LocalDateTime.parse(request.getEndedAt()));
            }

            dialerCallLogRepository.save(callLog);
            log.info("Call log updated from callback: {}", callLog.getCallId());
        } else {
            log.warn("Call log not found for callback: callId={}, dialerCallId={}",
                    request.getCallId(), request.getDialerCallId());
        }
    }

    // ==================== Helper Methods ====================

    private ThirdPartyIntegrationMaster getIntegrationConfig() {
        return integrationCacheService.getIntegration(INTEGRATION_NAME)
                .orElseThrow(() -> new ConfigurationNotFoundException(INTEGRATION_NAME));
    }

    @SuppressWarnings("null")
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
                    .onErrorResume(error -> Mono.error(new ApiCallException("Failed to call dialer API", error)))
                    .block();
        } catch (Exception e) {
            throw new ApiCallException("Failed to call Dialer API", e);
        }
    }
}
