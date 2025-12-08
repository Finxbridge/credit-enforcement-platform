package com.finx.strategyengineservice.client;

import com.finx.strategyengineservice.client.dto.EmailRequest;
import com.finx.strategyengineservice.client.dto.SMSRequest;
import com.finx.strategyengineservice.client.dto.WhatsAppRequest;
import com.finx.strategyengineservice.domain.dto.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign Client for Communication Service
 * Handles SMS, Email, WhatsApp communications via Msg91
 */
@FeignClient(
    name = "communication-service",
    path = "/api/v1"
)
public interface CommunicationServiceClient {

    /**
     * Send SMS via Msg91
     *
     * @param request SMS request with templateId, recipients, etc.
     * @return CommonResponse indicating success/failure
     */
    @PostMapping("/comm/sms/send")
    CommonResponse<Void> sendSMS(@RequestBody SMSRequest request);

    /**
     * Send Email via MSG91
     *
     * @param request Email request with toEmail, templateId, variables
     * @return CommonResponse indicating success/failure
     */
    @PostMapping("/comm/email/msg91/send")
    CommonResponse<Void> sendEmail(@RequestBody EmailRequest request);

    /**
     * Send WhatsApp message via Msg91
     *
     * @param request WhatsApp request with templateId, to, components, language
     * @return CommonResponse indicating success/failure
     */
    @PostMapping("/comm/whatsapp/send")
    CommonResponse<Void> sendWhatsApp(@RequestBody WhatsAppRequest request);
}
