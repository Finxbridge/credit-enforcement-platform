package com.finx.noticemanagementservice.controller;

import com.finx.noticemanagementservice.domain.dto.CommonResponse;
import com.finx.noticemanagementservice.domain.dto.DispatchTrackingDTO;
import com.finx.noticemanagementservice.domain.dto.UpdateDispatchStatusRequest;
import com.finx.noticemanagementservice.domain.enums.DispatchStatus;
import com.finx.noticemanagementservice.service.DispatchTrackingService;
import com.finx.noticemanagementservice.util.ResponseWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notices/dispatch")
@RequiredArgsConstructor
@Slf4j
public class DispatchTrackingController {

    private final DispatchTrackingService dispatchTrackingService;

    @PostMapping
    public ResponseEntity<CommonResponse<DispatchTrackingDTO>> createDispatch(
            @RequestParam Long noticeId,
            @RequestParam(required = false) Long vendorId) {
        log.info("Creating dispatch for notice: {}", noticeId);
        DispatchTrackingDTO dispatch = dispatchTrackingService.createDispatch(noticeId, vendorId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseWrapper.success(dispatch, "Dispatch created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<DispatchTrackingDTO>> getDispatchById(@PathVariable Long id) {
        DispatchTrackingDTO dispatch = dispatchTrackingService.getDispatchById(id);
        return ResponseEntity.ok(ResponseWrapper.success(dispatch));
    }

    @GetMapping("/tracking/{trackingId}")
    public ResponseEntity<CommonResponse<DispatchTrackingDTO>> getDispatchByTrackingId(
            @PathVariable String trackingId) {
        DispatchTrackingDTO dispatch = dispatchTrackingService.getDispatchByTrackingId(trackingId);
        return ResponseEntity.ok(ResponseWrapper.success(dispatch));
    }

    @GetMapping("/tracking-number/{trackingNumber}")
    public ResponseEntity<CommonResponse<DispatchTrackingDTO>> getDispatchByTrackingNumber(
            @PathVariable String trackingNumber) {
        DispatchTrackingDTO dispatch = dispatchTrackingService.getDispatchByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(ResponseWrapper.success(dispatch));
    }

    @GetMapping("/notice/{noticeId}")
    public ResponseEntity<CommonResponse<List<DispatchTrackingDTO>>> getDispatchesByNoticeId(
            @PathVariable Long noticeId) {
        List<DispatchTrackingDTO> dispatches = dispatchTrackingService.getDispatchesByNoticeId(noticeId);
        return ResponseEntity.ok(ResponseWrapper.success(dispatches));
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<CommonResponse<List<DispatchTrackingDTO>>> getDispatchesByVendorId(
            @PathVariable Long vendorId) {
        List<DispatchTrackingDTO> dispatches = dispatchTrackingService.getDispatchesByVendorId(vendorId);
        return ResponseEntity.ok(ResponseWrapper.success(dispatches));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<CommonResponse<DispatchTrackingDTO>> updateDispatchStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDispatchStatusRequest request) {
        log.info("Updating dispatch {} status to {}", id, request.getStatus());
        DispatchTrackingDTO dispatch = dispatchTrackingService.updateDispatchStatus(id, request);
        return ResponseEntity.ok(ResponseWrapper.success(dispatch, "Dispatch status updated successfully"));
    }

    @GetMapping("/queue")
    public ResponseEntity<CommonResponse<Page<DispatchTrackingDTO>>> getDispatchQueue(
            @RequestParam(required = false) DispatchStatus status,
            @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false) Boolean slaBreached,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<DispatchTrackingDTO> dispatches = dispatchTrackingService.getDispatchQueue(status, vendorId, slaBreached, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(dispatches));
    }

    @GetMapping("/sla-breached")
    public ResponseEntity<CommonResponse<List<DispatchTrackingDTO>>> getSlaBreachedDispatches() {
        List<DispatchTrackingDTO> dispatches = dispatchTrackingService.getSlaBreachedDispatches();
        return ResponseEntity.ok(ResponseWrapper.success(dispatches));
    }

    @PostMapping("/check-sla-breaches")
    public ResponseEntity<CommonResponse<Void>> checkSlaBreaches() {
        log.info("Manually triggering SLA breach check");
        dispatchTrackingService.checkAndMarkSlaBreaches();
        return ResponseEntity.ok(ResponseWrapper.success(null, "SLA breach check completed"));
    }
}
