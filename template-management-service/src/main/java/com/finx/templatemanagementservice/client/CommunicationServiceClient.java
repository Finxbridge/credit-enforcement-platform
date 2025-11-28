package com.finx.templatemanagementservice.client;

import com.finx.templatemanagementservice.domain.dto.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * Feign Client for Communication Service
 * Used to sync templates with external providers (MSG91, Twilio, etc.)
 */
@FeignClient(name = "communication-service", url = "${COMMUNICATION_SERVICE_URL:http://communication-service:8084}", path = "/api/v1")
public interface CommunicationServiceClient {

    /**
     * Send SMS using MSG91 Flow API
     */
    @PostMapping("/comm/sms/send")
    CommonResponse<Void> sendSMS(@RequestBody Map<String, Object> request);

    /**
     * Send WhatsApp message using MSG91 API
     */
    @PostMapping("/comm/whatsapp/send")
    CommonResponse<Void> sendWhatsApp(@RequestBody Map<String, Object> request);

    /**
     * Send Email
     */
    @PostMapping("/comm/email/send")
    CommonResponse<Void> sendEmail(@RequestBody Map<String, Object> request);
}
