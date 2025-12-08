package com.finx.communication.controller;

import com.finx.communication.domain.dto.CommonResponse;
import com.finx.communication.util.ResponseWrapper;
import com.finx.communication.domain.dto.notice.*;
import com.finx.communication.service.notice.NoticeVendorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/integrations/notice-vendor")
@RequiredArgsConstructor
@Tag(name = "Notice Vendor Integration", description = "Notice dispatch and tracking APIs")
public class NoticeVendorController {

    private final NoticeVendorService noticeVendorService;

    @PostMapping("/dispatch")
    @Operation(summary = "Dispatch Notice", description = "Dispatch notice to vendor for delivery")
    public ResponseEntity<CommonResponse<NoticeDispatchResponse>> dispatchNotice(
            @Valid @RequestBody NoticeDispatchRequest request) {
        log.info("Request to dispatch notice: {}", request.getNoticeId());
        NoticeDispatchResponse response = noticeVendorService.dispatchNotice(request);
        return ResponseWrapper.ok("Notice dispatched successfully", response);
    }

    @GetMapping("/track/{trackingNumber}")
    @Operation(summary = "Track Notice", description = "Track notice delivery status")
    public ResponseEntity<CommonResponse<Map<String, Object>>> trackNotice(
            @PathVariable String trackingNumber) {
        log.info("Request to track notice: {}", trackingNumber);
        Map<String, Object> trackingInfo = noticeVendorService.trackNotice(trackingNumber);
        return ResponseWrapper.ok("Tracking info retrieved", trackingInfo);
    }
}
