package com.finx.communication.controller;

import com.finx.common.dto.CommonResponse;
import com.finx.common.util.ResponseWrapper;
import com.finx.communication.domain.dto.dialer.*;
import com.finx.communication.service.communication.DialerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/integrations/dialer")
@RequiredArgsConstructor
@Tag(name = "Dialer Integration", description = "Dialer/telecalling integration APIs (Ozonetel)")
public class DialerController {

    private final DialerService dialerService;

    @PostMapping("/initiate-call")
    @Operation(summary = "Initiate Call", description = "Initiate outbound call via dialer")
    public ResponseEntity<CommonResponse<DialerResponse>> initiateCall(
            @Valid @RequestBody DialerCallRequest request) {
        log.info("Request to initiate call to: {}", request.getCustomerMobile());
        DialerResponse response = dialerService.initiateCall(request);
        return ResponseWrapper.ok("Call initiated successfully", response);
    }

    @GetMapping("/call-status/{callId}")
    @Operation(summary = "Get Call Status", description = "Get call status by call ID")
    public ResponseEntity<CommonResponse<DialerResponse>> getCallStatus(@PathVariable String callId) {
        log.info("Request to get call status for: {}", callId);
        DialerResponse response = dialerService.getCallStatus(callId);
        return ResponseWrapper.ok("Call status retrieved", response);
    }
}
