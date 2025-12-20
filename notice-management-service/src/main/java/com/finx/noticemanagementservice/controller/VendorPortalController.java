package com.finx.noticemanagementservice.controller;

import com.finx.noticemanagementservice.domain.dto.*;
import com.finx.noticemanagementservice.domain.enums.DispatchStatus;
import com.finx.noticemanagementservice.service.VendorPortalService;
import com.finx.noticemanagementservice.util.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

@RestController
@RequestMapping("/api/v1/notices/vendor-portal")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Vendor Portal", description = "Vendor portal APIs for notice dispatch management")
public class VendorPortalController {

    private final VendorPortalService vendorPortalService;

    @GetMapping("/{vendorId}/dashboard")
    @Operation(summary = "Get vendor dashboard", description = "Get dashboard statistics for a vendor")
    public ResponseEntity<CommonResponse<VendorDashboardDTO>> getVendorDashboard(
            @PathVariable Long vendorId) {
        log.info("Getting dashboard for vendor: {}", vendorId);
        VendorDashboardDTO dashboard = vendorPortalService.getVendorDashboard(vendorId);
        return ResponseWrapper.ok("Dashboard retrieved successfully", dashboard);
    }

    @GetMapping("/{vendorId}/cases")
    @Operation(summary = "Get vendor cases", description = "Get all cases assigned to vendor with filters")
    public ResponseEntity<CommonResponse<Page<VendorCaseDTO>>> getVendorCases(
            @PathVariable Long vendorId,
            @Parameter(description = "Filter by dispatch status")
            @RequestParam(required = false) DispatchStatus status,
            @Parameter(description = "Filter by SLA breach status")
            @RequestParam(required = false) Boolean slaBreached,
            @Parameter(description = "Filter by pincode")
            @RequestParam(required = false) String pincode,
            @Parameter(description = "Search term for notice number, customer name, or tracking number")
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Getting cases for vendor: {} with status: {}", vendorId, status);
        Page<VendorCaseDTO> cases = vendorPortalService.getVendorCases(vendorId, status, slaBreached, pincode, search, pageable);
        return ResponseWrapper.ok("Cases retrieved successfully", cases);
    }

    @GetMapping("/{vendorId}/cases/{dispatchId}")
    @Operation(summary = "Get case details", description = "Get detailed case information for vendor")
    public ResponseEntity<CommonResponse<VendorCaseDTO>> getVendorCaseDetails(
            @PathVariable Long vendorId,
            @PathVariable Long dispatchId) {
        log.info("Getting case details for vendor: {}, dispatch: {}", vendorId, dispatchId);
        VendorCaseDTO caseDetails = vendorPortalService.getVendorCaseDetails(vendorId, dispatchId);
        return ResponseWrapper.ok("Case details retrieved successfully", caseDetails);
    }

    @PutMapping("/{vendorId}/dispatch/{dispatchId}")
    @Operation(summary = "Update dispatch status", description = "Update dispatch status from vendor portal")
    public ResponseEntity<CommonResponse<DispatchTrackingDTO>> updateDispatchStatus(
            @PathVariable Long vendorId,
            @PathVariable Long dispatchId,
            @Valid @RequestBody VendorDispatchUpdateRequest request) {
        log.info("Vendor {} updating dispatch {} to status {}", vendorId, dispatchId, request.getStatus());
        DispatchTrackingDTO updated = vendorPortalService.updateDispatchFromVendor(vendorId, dispatchId, request);
        return ResponseWrapper.ok("Dispatch status updated successfully", updated);
    }

    @PostMapping("/{vendorId}/download")
    @Operation(summary = "Initiate bulk download", description = "Initiate bulk download of notice documents")
    public ResponseEntity<CommonResponse<BulkDownloadResponse>> initiateBulkDownload(
            @PathVariable Long vendorId,
            @Valid @RequestBody BulkDownloadRequest request) {
        log.info("Vendor {} initiating bulk download", vendorId);
        BulkDownloadResponse response = vendorPortalService.initiateBulkDownload(vendorId, request);
        return ResponseWrapper.ok("Download initiated successfully", response);
    }

    @GetMapping("/{vendorId}/download/{downloadId}/status")
    @Operation(summary = "Get download status", description = "Get status of a bulk download job")
    public ResponseEntity<CommonResponse<BulkDownloadResponse>> getDownloadStatus(
            @PathVariable Long vendorId,
            @PathVariable String downloadId) {
        log.info("Getting download status for vendor: {}, downloadId: {}", vendorId, downloadId);
        BulkDownloadResponse response = vendorPortalService.getDownloadStatus(vendorId, downloadId);
        return ResponseWrapper.ok("Download status retrieved successfully", response);
    }

    @PostMapping("/{vendorId}/bulk-dispatch")
    @Operation(summary = "Bulk mark as dispatched", description = "Mark multiple cases as dispatched")
    public ResponseEntity<CommonResponse<Integer>> bulkMarkAsDispatched(
            @PathVariable Long vendorId,
            @RequestParam List<Long> dispatchIds,
            @RequestParam(required = false) String trackingNumber,
            @RequestParam(required = false) String carrierName) {
        log.info("Vendor {} bulk dispatching {} cases", vendorId, dispatchIds.size());
        int count = vendorPortalService.markCasesAsDispatched(vendorId, dispatchIds, trackingNumber, carrierName);
        return ResponseWrapper.ok("Successfully dispatched " + count + " cases", count);
    }

    @GetMapping("/{vendorId}/sla-at-risk")
    @Operation(summary = "Get SLA at-risk cases", description = "Get cases that are about to breach SLA")
    public ResponseEntity<CommonResponse<Page<VendorCaseDTO>>> getSlaAtRiskCases(
            @PathVariable Long vendorId,
            @Parameter(description = "Hours threshold to consider at-risk (default: 24)")
            @RequestParam(defaultValue = "24") Integer hoursThreshold,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Getting SLA at-risk cases for vendor: {} within {} hours", vendorId, hoursThreshold);
        Page<VendorCaseDTO> cases = vendorPortalService.getSlaAtRiskCases(vendorId, hoursThreshold, pageable);
        return ResponseWrapper.ok("SLA at-risk cases retrieved successfully", cases);
    }
}
