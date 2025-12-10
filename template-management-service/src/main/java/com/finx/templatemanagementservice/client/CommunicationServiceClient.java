package com.finx.templatemanagementservice.client;

import com.finx.templatemanagementservice.domain.dto.CommonResponse;
import com.finx.templatemanagementservice.domain.dto.comm.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * Feign Client for Communication Service
 * Used to sync templates with external providers (MSG91)
 *
 * Note: smsType, senderId (SMS) and integrated_number (WhatsApp) are read from
 * database config_json in communication-service, not passed in request
 */
@FeignClient(name = "communication-service", url = "${communication-service.url:http://localhost:8084}")
public interface CommunicationServiceClient {

    // ==================== SMS APIs ====================

    /**
     * Create SMS Template in MSG91
     * Note: smsType and senderId are read from database config_json
     */
    @PostMapping("/comm/sms/create-template")
    CommonResponse<Map<String, Object>> createSMSTemplate(@RequestBody SmsCreateTemplateRequest request);

    /**
     * Send SMS using MSG91 Flow API
     */
    @PostMapping("/comm/sms/send")
    CommonResponse<SmsResponse> sendSMS(@RequestBody SmsSendRequest request);

    // ==================== WhatsApp APIs ====================

    /**
     * Create WhatsApp Template in MSG91
     * Note: integrated_number is read from database config_json
     */
    @PostMapping("/comm/whatsapp/templates")
    CommonResponse<Map<String, Object>> createWhatsAppTemplate(@RequestBody WhatsAppCreateTemplateRequest request);

    /**
     * Send WhatsApp message using MSG91 API
     */
    @PostMapping("/comm/whatsapp/send")
    CommonResponse<WhatsAppResponse> sendWhatsApp(@RequestBody WhatsAppSendRequest request);

    // ==================== Email APIs ====================

    /**
     * Create Email Template in MSG91
     */
    @PostMapping("/comm/email/msg91/templates")
    CommonResponse<Map<String, Object>> createEmailTemplate(@RequestBody Map<String, Object> request);

    /**
     * Send Email via MSG91
     */
    @PostMapping("/comm/email/msg91/send")
    CommonResponse<Map<String, Object>> sendEmail(@RequestBody Map<String, Object> request);
}
