package com.finx.communication.controller;

import com.finx.communication.domain.dto.CommonResponse;
import com.finx.communication.util.ResponseWrapper;
import com.finx.communication.domain.dto.dialer.*;
import com.finx.communication.service.communication.DialerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Telecalling/Dialer Integration Controller
 *
 * Vendor-agnostic API design for telecalling operations.
 * Currently supports placeholder implementations - actual vendor integration
 * to be implemented once third-party provider is finalized.
 *
 * Potential vendors: Ozonetel, Knowlarity, Exotel, Ameyo, etc.
 */
@Slf4j
@RestController
@RequestMapping("/integrations/dialer")
@RequiredArgsConstructor
@Tag(name = "Telecalling/Dialer Integration",
     description = "Telecalling and dialer integration APIs for click-to-call, auto-dial, call queue management, " +
                   "dispositions, agent status, and campaign analytics")
public class DialerController {

    private final DialerService dialerService;

    // ==================== Call Operations ====================

    @PostMapping("/initiate-call")
    @Operation(summary = "Initiate Call (Click-to-Call)",
               description = "Initiate an outbound call via dialer. Connects agent to customer.")
    public ResponseEntity<CommonResponse<DialerResponse>> initiateCall(
            @Valid @RequestBody DialerCallRequest request) {
        log.info("Request to initiate call to: {}", request.getCustomerMobile());
        DialerResponse response = dialerService.initiateCall(request);
        return ResponseWrapper.ok("Call initiated successfully", response);
    }

    @GetMapping("/call-status/{callId}")
    @Operation(summary = "Get Call Status",
               description = "Get current status of a call by call ID")
    public ResponseEntity<CommonResponse<DialerResponse>> getCallStatus(
            @PathVariable String callId) {
        log.info("Request to get call status for: {}", callId);
        DialerResponse response = dialerService.getCallStatus(callId);
        return ResponseWrapper.ok("Call status retrieved", response);
    }

    @PostMapping("/end-call/{callId}")
    @Operation(summary = "End Call",
               description = "Terminate an active call by call ID")
    public ResponseEntity<CommonResponse<DialerResponse>> endCall(
            @PathVariable String callId) {
        log.info("Request to end call: {}", callId);
        DialerResponse response = dialerService.endCall(callId);
        return ResponseWrapper.ok("Call ended successfully", response);
    }

    @PostMapping("/transfer-call/{callId}")
    @Operation(summary = "Transfer Call",
               description = "Transfer an active call to another agent or number")
    public ResponseEntity<CommonResponse<DialerResponse>> transferCall(
            @PathVariable String callId,
            @RequestParam String targetNumber,
            @RequestParam(defaultValue = "BLIND") String transferType) {
        log.info("Request to transfer call {} to: {}", callId, targetNumber);
        DialerResponse response = dialerService.transferCall(callId, targetNumber, transferType);
        return ResponseWrapper.ok("Call transferred successfully", response);
    }

    @PostMapping("/hold-call/{callId}")
    @Operation(summary = "Hold/Unhold Call",
               description = "Put a call on hold or resume it")
    public ResponseEntity<CommonResponse<DialerResponse>> holdCall(
            @PathVariable String callId,
            @RequestParam(defaultValue = "true") Boolean hold) {
        log.info("Request to {} call: {}", hold ? "hold" : "unhold", callId);
        DialerResponse response = dialerService.holdCall(callId, hold);
        return ResponseWrapper.ok(hold ? "Call placed on hold" : "Call resumed", response);
    }

    // ==================== Queue Operations ====================

    @PostMapping("/queue")
    @Operation(summary = "Queue Bulk Calls",
               description = "Add multiple calls to the dialer queue for auto-dial campaigns")
    public ResponseEntity<CommonResponse<DialerQueueResponse>> queueCalls(
            @Valid @RequestBody DialerQueueRequest request) {
        log.info("Request to queue {} calls for campaign: {}",
                request.getCustomers().size(), request.getCampaignId());
        DialerQueueResponse response = dialerService.queueCalls(request);
        return ResponseWrapper.created("Calls queued successfully", response);
    }

    @DeleteMapping("/queue/{batchId}")
    @Operation(summary = "Remove Batch from Queue",
               description = "Remove all calls from a queued batch")
    public ResponseEntity<CommonResponse<Void>> removeFromQueue(
            @PathVariable String batchId) {
        log.info("Request to remove batch from queue: {}", batchId);
        dialerService.removeFromQueue(batchId);
        return ResponseWrapper.ok("Batch removed from queue", null);
    }

    @DeleteMapping("/queue/call/{callId}")
    @Operation(summary = "Remove Single Call from Queue",
               description = "Remove a specific call from the queue")
    public ResponseEntity<CommonResponse<Void>> removeCallFromQueue(
            @PathVariable String callId) {
        log.info("Request to remove call from queue: {}", callId);
        dialerService.removeCallFromQueue(callId);
        return ResponseWrapper.ok("Call removed from queue", null);
    }

    @GetMapping("/queue/status/{batchId}")
    @Operation(summary = "Get Queue Status",
               description = "Get status of a queued batch")
    public ResponseEntity<CommonResponse<DialerQueueResponse>> getQueueStatus(
            @PathVariable String batchId) {
        log.info("Request to get queue status for batch: {}", batchId);
        DialerQueueResponse response = dialerService.getQueueStatus(batchId);
        return ResponseWrapper.ok("Queue status retrieved", response);
    }

    // ==================== Disposition Operations ====================

    @PostMapping("/disposition")
    @Operation(summary = "Update Call Disposition",
               description = "Update disposition and notes for a completed call. " +
                             "Dispositions: PTP, RTP, CALLBACK, NOT_INTERESTED, DISPUTE, WRONG_NUMBER, etc.")
    public ResponseEntity<CommonResponse<DialerResponse>> updateDisposition(
            @Valid @RequestBody DialerDispositionRequest request) {
        log.info("Request to update disposition for call: {} with code: {}",
                request.getCallId(), request.getDispositionCode());
        DialerResponse response = dialerService.updateDisposition(request);
        return ResponseWrapper.ok("Disposition updated successfully", response);
    }

    @GetMapping("/dispositions")
    @Operation(summary = "Get Disposition Codes",
               description = "Get list of available disposition codes for call tagging")
    public ResponseEntity<CommonResponse<List<String>>> getDispositionCodes() {
        log.info("Request to get disposition codes");
        List<String> dispositions = dialerService.getDispositionCodes();
        return ResponseWrapper.ok("Disposition codes retrieved", dispositions);
    }

    // ==================== Call History Operations ====================

    @GetMapping("/history")
    @Operation(summary = "Get Call History",
               description = "Get paginated call history with filters")
    public ResponseEntity<CommonResponse<DialerCallHistoryResponse>> getCallHistory(
            @RequestParam(required = false) Long caseId,
            @RequestParam(required = false) Long agentId,
            @RequestParam(required = false) String customerMobile,
            @RequestParam(required = false) String callStatus,
            @RequestParam(required = false) String dispositionCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String campaignId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        log.info("Request to get call history with filters");

        DialerCallHistoryRequest request = DialerCallHistoryRequest.builder()
                .caseId(caseId)
                .agentId(agentId)
                .customerMobile(customerMobile)
                .callStatus(callStatus)
                .dispositionCode(dispositionCode)
                .startDate(startDate)
                .endDate(endDate)
                .campaignId(campaignId)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        DialerCallHistoryResponse response = dialerService.getCallHistory(request);
        return ResponseWrapper.ok("Call history retrieved", response);
    }

    @GetMapping("/history/case/{caseId}")
    @Operation(summary = "Get Call History by Case",
               description = "Get all calls for a specific case")
    public ResponseEntity<CommonResponse<DialerCallHistoryResponse>> getCallHistoryByCase(
            @PathVariable Long caseId) {
        log.info("Request to get call history for case: {}", caseId);
        DialerCallHistoryResponse response = dialerService.getCallHistoryByCase(caseId);
        return ResponseWrapper.ok("Call history retrieved", response);
    }

    @GetMapping("/recording/{callId}")
    @Operation(summary = "Get Call Recording URL",
               description = "Get the recording URL for a completed call")
    public ResponseEntity<CommonResponse<String>> getCallRecording(
            @PathVariable String callId) {
        log.info("Request to get recording for call: {}", callId);
        String recordingUrl = dialerService.getCallRecordingUrl(callId);
        return ResponseWrapper.ok("Recording URL retrieved", recordingUrl);
    }

    // ==================== Agent Operations ====================

    @PostMapping("/agent/status")
    @Operation(summary = "Update Agent Status",
               description = "Update agent status for dialer. Statuses: LOGIN, LOGOUT, AVAILABLE, BREAK, BUSY, WRAP_UP")
    public ResponseEntity<CommonResponse<DialerAgentStatusResponse>> updateAgentStatus(
            @Valid @RequestBody DialerAgentStatusRequest request) {
        log.info("Request to update agent {} status to: {}", request.getAgentId(), request.getStatus());
        DialerAgentStatusResponse response = dialerService.updateAgentStatus(request);
        return ResponseWrapper.ok("Agent status updated", response);
    }

    @GetMapping("/agent/status/{agentId}")
    @Operation(summary = "Get Agent Status",
               description = "Get current status of an agent")
    public ResponseEntity<CommonResponse<DialerAgentStatusResponse>> getAgentStatus(
            @PathVariable Long agentId) {
        log.info("Request to get status for agent: {}", agentId);
        DialerAgentStatusResponse response = dialerService.getAgentStatus(agentId);
        return ResponseWrapper.ok("Agent status retrieved", response);
    }

    @GetMapping("/agents/status")
    @Operation(summary = "Get All Agents Status",
               description = "Get status of all agents (for supervisor dashboard)")
    public ResponseEntity<CommonResponse<DialerAgentStatusResponse>> getAllAgentsStatus() {
        log.info("Request to get all agents status");
        DialerAgentStatusResponse response = dialerService.getAllAgentsStatus();
        return ResponseWrapper.ok("Agents status retrieved", response);
    }

    // ==================== Analytics & Reporting ====================

    @GetMapping("/stats/campaign/{campaignId}")
    @Operation(summary = "Get Campaign Statistics",
               description = "Get call statistics for a specific campaign")
    public ResponseEntity<CommonResponse<DialerCampaignStatsResponse>> getCampaignStats(
            @PathVariable String campaignId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Request to get stats for campaign: {}", campaignId);
        DialerCampaignStatsResponse response = dialerService.getCampaignStats(campaignId, date);
        return ResponseWrapper.ok("Campaign statistics retrieved", response);
    }

    @GetMapping("/stats/daily")
    @Operation(summary = "Get Daily Statistics",
               description = "Get overall daily call statistics")
    public ResponseEntity<CommonResponse<DialerCampaignStatsResponse>> getDailyStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Request to get daily stats for date: {}", date);
        DialerCampaignStatsResponse response = dialerService.getDailyStats(date);
        return ResponseWrapper.ok("Daily statistics retrieved", response);
    }

    @GetMapping("/stats/agent/{agentId}")
    @Operation(summary = "Get Agent Statistics",
               description = "Get call statistics for a specific agent")
    public ResponseEntity<CommonResponse<DialerCampaignStatsResponse>> getAgentStats(
            @PathVariable Long agentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Request to get stats for agent: {}", agentId);
        DialerCampaignStatsResponse response = dialerService.getAgentStats(agentId, date);
        return ResponseWrapper.ok("Agent statistics retrieved", response);
    }

    // ==================== Webhooks ====================

    @PostMapping("/webhook/callback")
    @Operation(summary = "Dialer Callback Webhook",
               description = "Webhook endpoint for receiving call status updates from dialer provider")
    public ResponseEntity<CommonResponse<Void>> handleDialerCallback(
            @RequestBody DialerCallbackRequest request) {
        log.info("Received dialer callback for call: {} status: {}",
                request.getCallId(), request.getCallStatus());
        dialerService.handleCallback(request);
        return ResponseWrapper.ok("Callback processed", null);
    }
}
