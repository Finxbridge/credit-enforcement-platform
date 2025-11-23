package com.finx.communication.controller;

import com.finx.common.dto.CommonResponse;
import com.finx.common.util.ResponseWrapper;
import com.finx.communication.domain.dto.whatsapp.*;
import com.finx.communication.service.communication.WhatsAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @Operation(summary = "Create WhatsApp Template", description = "Create WhatsApp template in Msg91")
    public ResponseEntity<CommonResponse<Map<String, Object>>> createTemplate(
            @RequestBody Map<String, Object> templateRequest) {
        log.info("Request to create WhatsApp template");
        Map<String, Object> response = whatsAppService.createTemplate(templateRequest);
        return ResponseWrapper.ok("Template created successfully", response);
    }
}
