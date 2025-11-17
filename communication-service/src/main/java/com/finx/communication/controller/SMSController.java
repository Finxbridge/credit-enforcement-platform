package com.finx.communication.controller;

import com.finx.common.dto.CommonResponse;
import com.finx.common.util.ResponseWrapper;
import com.finx.communication.domain.dto.sms.*;
import com.finx.communication.service.communication.SMSService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * SMS Controller - Msg91 Integration
 */
@Slf4j
@RestController
@RequestMapping("/comm/sms")
@RequiredArgsConstructor
@Tag(name = "SMS Service", description = "SMS sending and management APIs (Msg91)")
public class SMSController {

    private final SMSService smsService;

    @PostMapping("/send")
    @Operation(summary = "Send SMS", description = "Send SMS to multiple recipients with dynamic variables")
    public ResponseEntity<CommonResponse<SmsResponse>> sendSms(@Valid @RequestBody SmsSendRequest request) {
        log.info("Request to send SMS to {} recipients", request.getRecipients().size());
        SmsResponse response = smsService.sendSms(request);
        return ResponseWrapper.ok("SMS sent successfully", response);
    }

    @PostMapping("/create-template")
    @Operation(summary = "Create SMS Template", description = "Create SMS template in Msg91")
    public ResponseEntity<CommonResponse<Map<String, Object>>> createTemplate(
            @Valid @RequestBody SmsCreateTemplateRequest request) {
        log.info("Request to create SMS template: {}", request.getTemplateName());
        Map<String, Object> response = smsService.createTemplate(request);
        return ResponseWrapper.ok("Template created successfully", response);
    }

    @PostMapping("/get-template-details")
    @Operation(summary = "Get Template Details", description = "Get SMS template details by template ID")
    public ResponseEntity<CommonResponse<Map<String, Object>>> getTemplateDetails(
            @Valid @RequestBody SmsGetTemplateVersionsRequest request) {
        log.info("Request to get template details for: {}", request.getTemplateId());
        Map<String, Object> response = smsService.getTemplateDetails(request);
        return ResponseWrapper.ok("Template details fetched successfully", response);
    }

    @PostMapping("/logs")
    @Operation(summary = "Get SMS Logs", description = "Get SMS logs by date range")
    public ResponseEntity<CommonResponse<Map<String, Object>>> getLogs(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        log.info("Request for SMS logs from {} to {}", startDate, endDate);
        Map<String, Object> logs = smsService.getLogs(startDate, endDate);
        return ResponseWrapper.ok("SMS logs fetched successfully", logs);
    }

    @PostMapping("/add-template-version")
    @Operation(summary = "Add SMS Template Version", description = "Add a new version to an existing SMS template")
    public ResponseEntity<CommonResponse<Map<String, Object>>> addTemplateVersion(
            @Valid @RequestBody SmsAddTemplateVersionRequest request) {
        log.info("Request to add template version for template: {}", request.getTemplateId());
        Map<String, Object> response = smsService.addTemplateVersion(request);
        return ResponseWrapper.ok("Template version added successfully", response);
    }

    @GetMapping("/analytics")
    @Operation(summary = "Get SMS Analytics", description = "Get SMS analytics by date range")
    public ResponseEntity<CommonResponse<Map<String, Object>>> getAnalytics(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        log.info("Request for SMS analytics from {} to {}", startDate, endDate);
        Map<String, Object> analytics = smsService.getAnalytics(startDate, endDate);
        return ResponseWrapper.ok("SMS analytics fetched successfully", analytics);
    }

    @GetMapping("/mark-active")
    @Operation(summary = "Mark Template Version Active", description = "Mark a specific template version as active")
    public ResponseEntity<CommonResponse<Map<String, Object>>> markTemplateActive(
            @RequestParam String id,
            @RequestParam String templateId) {
        log.info("Request to mark template version {} as active for template {}", id, templateId);
        Map<String, Object> response = smsService.markTemplateActive(id, templateId);
        return ResponseWrapper.ok("Template version marked as active", response);
    }
}
