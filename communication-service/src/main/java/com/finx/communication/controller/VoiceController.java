package com.finx.communication.controller;

import com.finx.communication.domain.dto.CommonResponse;
import com.finx.communication.util.ResponseWrapper;
import com.finx.communication.domain.dto.voice.*;
import com.finx.communication.service.communication.VoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Voice Controller - MSG91 Voice Integration
 */
@Slf4j
@RestController
@RequestMapping("/comm/voice")
@RequiredArgsConstructor
@Tag(name = "Voice Service", description = "Voice call APIs (MSG91)")
public class VoiceController {

    private final VoiceService voiceService;

    @PostMapping("/click-to-call")
    @Operation(summary = "Click to Call", description = "Initiate click to call connecting two numbers")
    public ResponseEntity<CommonResponse<VoiceResponse>> clickToCall(
            @Valid @RequestBody VoiceClickToCallRequest request) {
        log.info("Request for click to call from {} to {}", request.getCallerId(), request.getDestination());
        VoiceResponse response = voiceService.clickToCall(request);
        return ResponseWrapper.ok("Click to call initiated successfully", response);
    }

    @PostMapping("/send-voice-sms")
    @Operation(summary = "Send Voice SMS", description = "Send voice message to a number")
    public ResponseEntity<CommonResponse<VoiceResponse>> sendVoiceSms(
            @Valid @RequestBody VoiceSmsRequest request) {
        log.info("Request to send voice SMS to: {}", request.getClientNumber());
        VoiceResponse response = voiceService.sendVoiceSms(request);
        return ResponseWrapper.ok("Voice SMS sent successfully", response);
    }

    @GetMapping("/logs")
    @Operation(summary = "Get Voice Logs", description = "Get voice call logs from MSG91")
    public ResponseEntity<CommonResponse<Map<String, Object>>> getVoiceLogs() {
        log.info("Request for voice call logs");
        Map<String, Object> logs = voiceService.getVoiceLogs();
        return ResponseWrapper.ok("Voice logs fetched successfully", logs);
    }
}
