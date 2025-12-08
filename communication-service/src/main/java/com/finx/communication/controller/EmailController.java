package com.finx.communication.controller;

import com.finx.communication.domain.dto.CommonResponse;
import com.finx.communication.util.ResponseWrapper;
import com.finx.communication.domain.dto.InternalEmailRequest;
import com.finx.communication.domain.dto.email.*;
import com.finx.communication.service.communication.Msg91EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/comm/email")
@RequiredArgsConstructor
@Tag(name = "Email Service", description = "Email sending APIs (SendGrid & MSG91)")
public class EmailController {

    private final Msg91EmailService msg91EmailService;

    /**
     * Send Email via MSG91
     * Uses MSG91 Email API v5 with template support
     * Supports multiple recipients, variables per recipient, attachments
     */
    @PostMapping("/msg91/send")
    @Operation(summary = "Send Email via MSG91", description = "Send email via MSG91 with template support using the internal simplified request format.")
    public ResponseEntity<CommonResponse<Msg91EmailResponse>> sendEmailViaMsg91(
            @Valid @RequestBody InternalEmailRequest request) {
        log.info("Request to send email via MSG91 to: {}", request.getToEmail());
        Msg91EmailResponse response = msg91EmailService.sendEmail(request);
        return ResponseWrapper.ok("Email sent successfully via MSG91", response);
    }

    /**
     * Create Email Template in MSG91
     * POST /api/v5/email/templates
     */
    @PostMapping("/msg91/templates")
    @Operation(summary = "Create Email Template", description = "Create email template in MSG91")
    public ResponseEntity<CommonResponse<java.util.Map<String, Object>>> createEmailTemplate(
            @Valid @RequestBody com.finx.communication.domain.dto.email.EmailTemplateCreateRequest request) {
        log.info("Request to create email template: {}", request.getName());
        java.util.Map<String, Object> response = msg91EmailService.createTemplate(request);
        return ResponseWrapper.ok("Email template created successfully", response);
    }
}
