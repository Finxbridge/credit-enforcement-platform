package com.finx.templatemanagementservice.client;

import com.finx.templatemanagementservice.domain.dto.CommonResponse;
import com.finx.templatemanagementservice.domain.dto.comm.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Feign Client for Communication Service
 * Used to sync templates with external providers (MSG91)
 *
 * Note: smsType, senderId (SMS) and integrated_number (WhatsApp) are read from
 * database config_json in communication-service, not passed in request
 */
@FeignClient(name = "communication-service", url = "${COMMUNICATION_SERVICE_URL:http://localhost:8085}")
public interface CommunicationServiceClient {

    // ==================== SMS APIs ====================

    /**
     * Create SMS Template in MSG91
     * Note: smsType and senderId are read from database config_json
     */
    @PostMapping("/comm/sms/create-template")
    CommonResponse<Map<String, Object>> createSMSTemplate(@RequestBody SmsCreateTemplateRequest request);

    /**
     * Add SMS Template Version (Edit/Update SMS Template)
     * POST /api/v5/sms/addTemplateVersion
     * Note: senderId is read from database config_json
     */
    @PostMapping("/comm/sms/add-template-version")
    CommonResponse<Map<String, Object>> addSMSTemplateVersion(@RequestBody SmsAddTemplateVersionRequest request);

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
     * Edit WhatsApp Template in MSG91
     * Note: integrated_number is read from database config_json
     * @param templateId The MSG91 template ID (providerTemplateId)
     * @param request The edit request with updated template details
     */
    @PutMapping("/comm/whatsapp/templates/{templateId}")
    CommonResponse<Map<String, Object>> editWhatsAppTemplate(
            @PathVariable("templateId") String templateId,
            @RequestBody WhatsAppEditTemplateRequest request);

    /**
     * Delete WhatsApp Template from MSG91
     * @param templateName The template name to delete
     */
    @DeleteMapping("/comm/whatsapp/templates")
    CommonResponse<Map<String, Object>> deleteWhatsAppTemplate(@RequestParam("templateName") String templateName);

    /**
     * Send WhatsApp message using MSG91 API
     */
    @PostMapping("/comm/whatsapp/send")
    CommonResponse<WhatsAppResponse> sendWhatsApp(@RequestBody WhatsAppSendRequest request);

    /**
     * Upload media file (IMAGE, VIDEO, DOCUMENT) to MSG91 for WhatsApp template creation
     * Returns header_handle to use in template HEADER component
     *
     * Note: This requires multipart/form-data, typically called via RestTemplate/WebClient
     * instead of Feign. See WhatsAppMediaUploadClient for the actual implementation.
     *
     * @param mediaBytes The media file bytes
     * @param filename The original filename
     * @param contentType The content type (application/pdf, image/jpeg, etc.)
     * @return Response containing the header_handle URL
     */
    @PostMapping("/comm/whatsapp/media-upload")
    CommonResponse<WhatsAppMediaUploadResponse> uploadWhatsAppMedia(
            @RequestParam("mediaBytes") byte[] mediaBytes,
            @RequestParam("filename") String filename,
            @RequestParam("contentType") String contentType);

    // ==================== Email APIs ====================

    /**
     * Create Email Template in MSG91
     * Note: For "edit", create new template then delete old (MSG91 doesn't have edit API)
     */
    @PostMapping("/comm/email/msg91/templates")
    CommonResponse<Map<String, Object>> createEmailTemplate(@RequestBody EmailTemplateCreateRequest request);

    /**
     * Send Email via MSG91
     */
    @PostMapping("/comm/email/msg91/send")
    CommonResponse<Map<String, Object>> sendEmail(@RequestBody Map<String, Object> request);
}
