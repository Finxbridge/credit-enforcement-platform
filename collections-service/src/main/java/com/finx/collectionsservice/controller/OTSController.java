package com.finx.collectionsservice.controller;

import com.finx.collectionsservice.domain.dto.*;
import com.finx.collectionsservice.domain.enums.OTSStatus;
import com.finx.collectionsservice.service.OTSService;
import com.finx.collectionsservice.util.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/collections/ots")
@RequiredArgsConstructor
@Tag(name = "OTS Management", description = "APIs for One-Time Settlement management")
public class OTSController {

    private final OTSService otsService;

    @PostMapping
    @Operation(summary = "Create OTS request", description = "Create a new OTS/settlement request")
    public ResponseEntity<CommonResponse<OTSRequestDTO>> createOTSRequest(
            @Valid @RequestBody CreateOTSRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("POST /ots - Creating OTS for case: {}", request.getCaseId());
        OTSRequestDTO response = otsService.createOTSRequest(request, userId);
        return ResponseWrapper.created("OTS request created successfully", response);
    }

    @GetMapping("/{otsId}")
    @Operation(summary = "Get OTS by ID", description = "Get OTS request details")
    public ResponseEntity<CommonResponse<OTSRequestDTO>> getOTSById(@PathVariable Long otsId) {
        log.info("GET /ots/{} - Fetching OTS", otsId);
        OTSRequestDTO response = otsService.getOTSById(otsId);
        return ResponseWrapper.ok("OTS retrieved successfully", response);
    }

    @GetMapping("/number/{otsNumber}")
    @Operation(summary = "Get OTS by number", description = "Get OTS request by its number")
    public ResponseEntity<CommonResponse<OTSRequestDTO>> getOTSByNumber(@PathVariable String otsNumber) {
        log.info("GET /ots/number/{} - Fetching OTS", otsNumber);
        OTSRequestDTO response = otsService.getOTSByNumber(otsNumber);
        return ResponseWrapper.ok("OTS retrieved successfully", response);
    }

    @GetMapping("/case/{caseId}")
    @Operation(summary = "Get case OTS requests", description = "Get all OTS requests for a case")
    public ResponseEntity<CommonResponse<List<OTSRequestDTO>>> getOTSByCase(@PathVariable Long caseId) {
        log.info("GET /ots/case/{} - Fetching case OTS requests", caseId);
        List<OTSRequestDTO> otsRequests = otsService.getOTSByCase(caseId);
        return ResponseWrapper.ok("Case OTS requests retrieved successfully", otsRequests);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get OTS by status", description = "Get OTS requests filtered by status")
    public ResponseEntity<CommonResponse<Page<OTSRequestDTO>>> getOTSByStatus(
            @PathVariable OTSStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /ots/status/{} - Fetching OTS requests", status);
        Page<OTSRequestDTO> otsRequests = otsService.getOTSByStatus(status, pageable);
        return ResponseWrapper.ok("OTS requests retrieved successfully", otsRequests);
    }

    @PostMapping("/{otsId}/approve")
    @Operation(summary = "Approve OTS", description = "Approve an OTS request")
    public ResponseEntity<CommonResponse<OTSRequestDTO>> approveOTS(
            @PathVariable Long otsId,
            @RequestHeader("X-User-Id") Long approverId,
            @RequestParam(required = false) String comments) {
        log.info("POST /ots/{}/approve - Approving OTS", otsId);
        OTSRequestDTO response = otsService.approveOTS(otsId, approverId, comments);
        return ResponseWrapper.ok("OTS approved successfully", response);
    }

    @PostMapping("/{otsId}/reject")
    @Operation(summary = "Reject OTS", description = "Reject an OTS request")
    public ResponseEntity<CommonResponse<OTSRequestDTO>> rejectOTS(
            @PathVariable Long otsId,
            @RequestHeader("X-User-Id") Long approverId,
            @RequestParam String reason) {
        log.info("POST /ots/{}/reject - Rejecting OTS", otsId);
        OTSRequestDTO response = otsService.rejectOTS(otsId, approverId, reason);
        return ResponseWrapper.ok("OTS rejected", response);
    }

    @PostMapping("/{otsId}/cancel")
    @Operation(summary = "Cancel OTS", description = "Cancel an OTS request")
    public ResponseEntity<CommonResponse<OTSRequestDTO>> cancelOTS(
            @PathVariable Long otsId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam String reason) {
        log.info("POST /ots/{}/cancel - Cancelling OTS", otsId);
        OTSRequestDTO response = otsService.cancelOTS(otsId, userId, reason);
        return ResponseWrapper.ok("OTS cancelled", response);
    }

    @GetMapping("/pending-approvals")
    @Operation(summary = "Get pending approvals", description = "Get OTS requests pending approval")
    public ResponseEntity<CommonResponse<Page<OTSRequestDTO>>> getPendingApprovals(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /ots/pending-approvals - Fetching pending approvals");
        Page<OTSRequestDTO> otsRequests = otsService.getPendingApprovals(pageable);
        return ResponseWrapper.ok("Pending approvals retrieved successfully", otsRequests);
    }

    @PostMapping("/process-expired")
    @Operation(summary = "Process expired OTS", description = "Mark expired OTS requests")
    public ResponseEntity<CommonResponse<Integer>> processExpiredOTS() {
        log.info("POST /ots/process-expired - Processing expired OTS");
        Integer count = otsService.processExpiredOTS();
        return ResponseWrapper.ok("Processed " + count + " expired OTS requests", count);
    }
}
