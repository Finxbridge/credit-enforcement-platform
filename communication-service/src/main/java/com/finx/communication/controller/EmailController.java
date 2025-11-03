package com.finx.communication.controller;

import com.finx.common.dto.CommonResponse;
import com.finx.common.util.ResponseWrapper;
import com.finx.common.dto.InternalEmailRequest;
import com.finx.communication.domain.dto.email.*;
import com.finx.communication.service.communication.SendGridEmailService;
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

    private final SendGridEmailService sendGridEmailService;
    private final Msg91EmailService msg91EmailService;

    /**
     * Send Email via SendGrid
     * Supports dynamic variables, HTML/Text body, and attachments
     */
    @PostMapping("/sendgrid/send")
    @Operation(summary = "Send Email via SendGrid", description = "Send email via SendGrid with dynamic variables and template support")
    public ResponseEntity<CommonResponse<EmailResponse>> sendEmailViaSendGrid(
            @Valid @RequestBody EmailSendRequest request) {
        log.info("Request to send email via SendGrid to: {}", request.getTo());
        EmailResponse response = sendGridEmailService.sendEmail(request);
        return ResponseWrapper.ok("Email sent successfully via SendGrid", response);
    }

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
}
