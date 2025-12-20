package com.finx.noticemanagementservice.service;

import com.finx.noticemanagementservice.domain.dto.*;
import com.finx.noticemanagementservice.domain.enums.DispatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VendorPortalService {

    /**
     * Get vendor dashboard statistics
     */
    VendorDashboardDTO getVendorDashboard(Long vendorId);

    /**
     * Get all cases assigned to vendor with filters
     */
    Page<VendorCaseDTO> getVendorCases(Long vendorId, DispatchStatus status, Boolean slaBreached,
                                        String pincode, String searchTerm, Pageable pageable);

    /**
     * Get case details for vendor
     */
    VendorCaseDTO getVendorCaseDetails(Long vendorId, Long dispatchId);

    /**
     * Update dispatch status from vendor portal
     */
    DispatchTrackingDTO updateDispatchFromVendor(Long vendorId, Long dispatchId, VendorDispatchUpdateRequest request);

    /**
     * Initiate bulk download of notices
     */
    BulkDownloadResponse initiateBulkDownload(Long vendorId, BulkDownloadRequest request);

    /**
     * Get download status
     */
    BulkDownloadResponse getDownloadStatus(Long vendorId, String downloadId);

    /**
     * Mark cases as picked up (dispatched)
     */
    int markCasesAsDispatched(Long vendorId, java.util.List<Long> dispatchIds, String trackingNumber, String carrierName);

    /**
     * Get SLA at-risk cases for vendor (about to breach)
     */
    Page<VendorCaseDTO> getSlaAtRiskCases(Long vendorId, Integer hoursThreshold, Pageable pageable);
}
