package com.finx.communication.controller;

import com.finx.communication.domain.dto.CommonResponse;
import com.finx.communication.util.ResponseWrapper;
import com.finx.communication.domain.dto.whatsapp.*;
import com.finx.communication.service.communication.WhatsAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * WhatsApp Controller - Msg91 Integration
 */
@Slf4j
@RestController
@RequestMapping("/comm/whatsapp")
@RequiredArgsConstructor
@Tag(name = "WhatsApp Service", description = "WhatsApp messaging APIs (Msg91)")
public class WhatsAppController {

    private final WhatsAppService whatsAppService;

    @PostMapping("/send")
    @Operation(summary = "Send WhatsApp", description = "Send WhatsApp message with dynamic components")
    public ResponseEntity<CommonResponse<WhatsAppResponse>> sendWhatsApp(
            @Valid @RequestBody WhatsAppSendRequest request) {
        log.info("Request to send WhatsApp to {} recipients", request.getTo().size());
        WhatsAppResponse response = whatsAppService.sendWhatsApp(request);
        return ResponseWrapper.ok("WhatsApp sent successfully", response);
    }

    @PostMapping("/templates")
    @Operation(summary = "Create WhatsApp Template", description = "Create WhatsApp template in Msg91. Supports TEXT/MEDIA/LOCATION headers with various button types")
    public ResponseEntity<CommonResponse<Map<String, Object>>> createTemplate(
            @Valid @RequestBody WhatsAppCreateTemplateRequest request) {
        log.info("Request to create WhatsApp template: {}", request.getTemplateName());
        Map<String, Object> response = whatsAppService.createTemplate(request);
        return ResponseWrapper.ok("Template created successfully", response);
    }

    @PutMapping("/templates/{templateId}")
    @Operation(summary = "Edit WhatsApp Template", description = "Edit existing WhatsApp template in Msg91")
    public ResponseEntity<CommonResponse<Map<String, Object>>> editTemplate(
            @PathVariable String templateId,
            @Valid @RequestBody WhatsAppEditTemplateRequest request) {
        log.info("Request to edit WhatsApp template: {}", templateId);
        Map<String, Object> response = whatsAppService.editTemplate(templateId, request);
        return ResponseWrapper.ok("Template edited successfully", response);
    }

    @DeleteMapping("/templates")
    @Operation(summary = "Delete WhatsApp Template", description = "Delete WhatsApp template from Msg91. integrated_number is read from database config")
    public ResponseEntity<CommonResponse<Map<String, Object>>> deleteTemplate(
            @RequestParam String templateName) {
        log.info("Request to delete WhatsApp template: {}", templateName);
        Map<String, Object> response = whatsAppService.deleteTemplate(templateName);
        return ResponseWrapper.ok("Template deleted successfully", response);
    }

    @PostMapping("/payment-link")
    @Operation(summary = "Send WhatsApp Payment Link", description = "Send payment link via WhatsApp")
    public ResponseEntity<CommonResponse<WhatsAppResponse>> sendPaymentLink(
            @Valid @RequestBody WhatsAppPaymentLinkRequest request) {
        log.info("Request to send WhatsApp payment link to: {}", request.getRecipientNumber());
        WhatsAppResponse response = whatsAppService.sendPaymentLink(request);
        return ResponseWrapper.ok("Payment link sent successfully", response);
    }

    @PostMapping(value = "/media-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload Media for WhatsApp Template",
               description = "Upload media file (IMAGE, VIDEO, DOCUMENT) to MSG91 to get header_handle for template creation. " +
                           "Supported formats: PDF, DOC, DOCX for DOCUMENT; JPG, PNG for IMAGE; MP4 for VIDEO")
    public ResponseEntity<CommonResponse<WhatsAppMediaUploadResponse>> uploadMedia(
            @Parameter(description = "Media file to upload (PDF, DOC, DOCX, JPG, PNG, MP4)")
            @RequestParam("media") MultipartFile media) {
        log.info("Request to upload media for WhatsApp template: filename={}, size={}, contentType={}",
                media.getOriginalFilename(), media.getSize(), media.getContentType());
        WhatsAppMediaUploadResponse response = whatsAppService.uploadMedia(media);
        return ResponseWrapper.ok("Media uploaded successfully", response);
    }

    @PostMapping("/templates/with-document")
    @Operation(summary = "Create WhatsApp Template with Document",
               description = "Two-step process: 1) Upload document to get header_handle, 2) Create template with DOCUMENT header. " +
                           "This endpoint handles both steps automatically.")
    public ResponseEntity<CommonResponse<Map<String, Object>>> createTemplateWithDocument(
            @Parameter(description = "Document file (PDF, DOC, DOCX)")
            @RequestPart("document") MultipartFile document,
            @Parameter(description = "Template creation request")
            @RequestPart("template") @Valid WhatsAppCreateTemplateRequest request) {
        log.info("Request to create WhatsApp template with document: template={}, file={}",
                request.getTemplateName(), document.getOriginalFilename());
        Map<String, Object> response = whatsAppService.createTemplateWithDocument(document, request);
        return ResponseWrapper.ok("Template with document created successfully", response);
    }
}
