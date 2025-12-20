package com.finx.noticemanagementservice.service.impl;

import com.finx.noticemanagementservice.domain.dto.*;
import com.finx.noticemanagementservice.domain.entity.*;
import com.finx.noticemanagementservice.domain.enums.DispatchStatus;
import com.finx.noticemanagementservice.exception.ResourceNotFoundException;
import com.finx.noticemanagementservice.repository.*;
import com.finx.noticemanagementservice.service.AuditLogService;
import com.finx.noticemanagementservice.service.VendorPortalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VendorPortalServiceImpl implements VendorPortalService {

    private final DispatchTrackingRepository dispatchTrackingRepository;
    private final DispatchStatusHistoryRepository statusHistoryRepository;
    private final NoticeVendorRepository vendorRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(readOnly = true)
    public VendorDashboardDTO getVendorDashboard(Long vendorId) {
        NoticeVendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + vendorId));

        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime slaRiskThreshold = LocalDateTime.now().plusHours(24);

        // Today's counts
        Long todayPending = dispatchTrackingRepository.countByVendorAndStatusToday(vendorId, DispatchStatus.PENDING, startOfDay);
        Long todayDispatched = dispatchTrackingRepository.countByVendorAndStatusToday(vendorId, DispatchStatus.DISPATCHED, startOfDay);
        Long todayDelivered = dispatchTrackingRepository.countByVendorAndStatusToday(vendorId, DispatchStatus.DELIVERED, startOfDay);
        Long todayRto = dispatchTrackingRepository.countByVendorAndStatusToday(vendorId, DispatchStatus.RTO, startOfDay);

        // Overall counts
        Long totalPending = dispatchTrackingRepository.countByVendorAndStatus(vendorId, DispatchStatus.PENDING);
        Long totalInTransit = dispatchTrackingRepository.countByVendorAndStatus(vendorId, DispatchStatus.IN_TRANSIT);
        Long totalDelivered = dispatchTrackingRepository.countDeliveredByVendor(vendorId);
        Long totalRto = dispatchTrackingRepository.countByVendorAndStatus(vendorId, DispatchStatus.RTO);
        Long totalFailed = dispatchTrackingRepository.countByVendorAndStatus(vendorId, DispatchStatus.FAILED);
        Long totalCount = dispatchTrackingRepository.countTotalByVendor(vendorId);

        // SLA stats
        Long slaBreached = dispatchTrackingRepository.countSlaBreachedByVendor(vendorId);
        Long slaAtRisk = dispatchTrackingRepository.countSlaAtRiskByVendor(vendorId, slaRiskThreshold);

        // Calculate rates
        BigDecimal deliveryRate = BigDecimal.ZERO;
        BigDecimal rtoRate = BigDecimal.ZERO;
        BigDecimal slaComplianceRate = BigDecimal.ZERO;

        if (totalCount > 0) {
            deliveryRate = BigDecimal.valueOf(totalDelivered)
                    .divide(BigDecimal.valueOf(totalCount), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            rtoRate = BigDecimal.valueOf(totalRto)
                    .divide(BigDecimal.valueOf(totalCount), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            slaComplianceRate = BigDecimal.valueOf(totalCount - slaBreached)
                    .divide(BigDecimal.valueOf(totalCount), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        // Audit log for dashboard view
        auditLogService.log("VENDOR_DASHBOARD_VIEWED", "Vendor", vendorId.toString(),
                vendor.getVendorName(), "VIEW", null, null, null);

        return VendorDashboardDTO.builder()
                .vendorId(vendorId)
                .vendorCode(vendor.getVendorCode())
                .vendorName(vendor.getVendorName())
                .todayPendingCount(todayPending != null ? todayPending : 0L)
                .todayDispatchedCount(todayDispatched != null ? todayDispatched : 0L)
                .todayDeliveredCount(todayDelivered != null ? todayDelivered : 0L)
                .todayRtoCount(todayRto != null ? todayRto : 0L)
                .totalPendingCount(totalPending != null ? totalPending : 0L)
                .totalInTransitCount(totalInTransit != null ? totalInTransit : 0L)
                .totalDeliveredCount(totalDelivered != null ? totalDelivered : 0L)
                .totalRtoCount(totalRto != null ? totalRto : 0L)
                .totalFailedCount(totalFailed != null ? totalFailed : 0L)
                .slaBreachedCount(slaBreached != null ? slaBreached : 0L)
                .slaAtRiskCount(slaAtRisk != null ? slaAtRisk : 0L)
                .slaComplianceRate(slaComplianceRate.setScale(2, RoundingMode.HALF_UP))
                .deliveryRate(deliveryRate.setScale(2, RoundingMode.HALF_UP))
                .rtoRate(rtoRate.setScale(2, RoundingMode.HALF_UP))
                .avgDeliveryDays(vendor.getDefaultDeliverySlaDays())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VendorCaseDTO> getVendorCases(Long vendorId, DispatchStatus status, Boolean slaBreached,
                                               String pincode, String searchTerm, Pageable pageable) {
        validateVendor(vendorId);

        Page<DispatchTracking> dispatches;
        if (searchTerm != null && !searchTerm.isBlank()) {
            dispatches = dispatchTrackingRepository.searchByVendor(vendorId, searchTerm, pageable);
        } else {
            dispatches = dispatchTrackingRepository.findByVendorWithFilters(vendorId, status, slaBreached, pincode, pageable);
        }

        return dispatches.map(this::mapToVendorCaseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public VendorCaseDTO getVendorCaseDetails(Long vendorId, Long dispatchId) {
        validateVendor(vendorId);

        DispatchTracking dispatch = dispatchTrackingRepository.findByIdAndVendorId(dispatchId, vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispatch not found with id: " + dispatchId + " for vendor: " + vendorId));

        // Audit log for case view
        auditLogService.log("VENDOR_CASE_VIEWED", "DispatchTracking", dispatchId.toString(),
                dispatch.getTrackingId(), "VIEW", null,
                Map.of("vendorId", vendorId, "noticeNumber", dispatch.getNoticeNumber()), null);

        return mapToVendorCaseDTO(dispatch);
    }

    @Override
    @Transactional
    public DispatchTrackingDTO updateDispatchFromVendor(Long vendorId, Long dispatchId, VendorDispatchUpdateRequest request) {
        validateVendor(vendorId);

        DispatchTracking dispatch = dispatchTrackingRepository.findByIdAndVendorId(dispatchId, vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispatch not found with id: " + dispatchId + " for vendor: " + vendorId));

        String oldStatus = dispatch.getDispatchStatus().name();
        DispatchStatus newStatus = request.getStatus();
        LocalDateTime eventTime = request.getEventTimestamp() != null ? request.getEventTimestamp() : LocalDateTime.now();

        // Update dispatch
        dispatch.setDispatchStatus(newStatus);
        dispatch.setCurrentLocation(request.getLocation());
        dispatch.setCurrentStatusRemarks(request.getRemarks());

        if (request.getTrackingNumber() != null) {
            dispatch.setTrackingNumber(request.getTrackingNumber());
        }
        if (request.getCarrierName() != null) {
            dispatch.setCarrierName(request.getCarrierName());
        }
        if (request.getServiceType() != null) {
            dispatch.setServiceType(request.getServiceType());
        }

        // Update status-specific timestamps
        switch (newStatus) {
            case DISPATCHED -> dispatch.setDispatchedAt(eventTime);
            case IN_TRANSIT -> dispatch.setInTransitAt(eventTime);
            case OUT_FOR_DELIVERY -> {
                dispatch.setOutForDeliveryAt(eventTime);
                dispatch.setDeliveryAttemptCount(dispatch.getDeliveryAttemptCount() + 1);
                dispatch.setLastAttemptAt(eventTime);
            }
            case DELIVERED -> dispatch.setDeliveredAt(eventTime);
            case RTO -> {
                dispatch.setRtoInitiatedAt(eventTime);
                dispatch.setRtoReason(request.getRemarks() != null ? request.getRemarks() : request.getFailureReason());
            }
            case FAILED -> {
                dispatch.setLastAttemptAt(eventTime);
                dispatch.setLastAttemptStatus("FAILED");
                dispatch.setLastAttemptRemarks(request.getFailureReason() != null ? request.getFailureReason() : request.getRemarks());
            }
            default -> {}
        }

        DispatchTracking saved = dispatchTrackingRepository.save(dispatch);

        // Create status history
        DispatchStatusHistory history = DispatchStatusHistory.builder()
                .dispatchTracking(saved)
                .status(newStatus.name())
                .location(request.getLocation())
                .remarks(request.getRemarks())
                .eventTimestamp(eventTime)
                .source("VENDOR_PORTAL")
                .rawData(request.getAdditionalData())
                .build();
        statusHistoryRepository.save(history);

        // Audit log
        auditLogService.logStatusChange("DispatchTracking", saved.getId().toString(),
                saved.getTrackingId(), oldStatus, newStatus.name());

        log.info("Vendor {} updated dispatch {} status from {} to {}",
                vendorId, saved.getTrackingId(), oldStatus, newStatus);

        return mapToDispatchDTO(saved);
    }

    @Override
    @Transactional
    public BulkDownloadResponse initiateBulkDownload(Long vendorId, BulkDownloadRequest request) {
        validateVendor(vendorId);

        String downloadId = UUID.randomUUID().toString();

        // In a real implementation, this would queue a background job
        // For now, we'll create a placeholder response
        BulkDownloadResponse response = BulkDownloadResponse.builder()
                .downloadId(downloadId)
                .status("PROCESSING")
                .totalDocuments(request.getDispatchIds() != null ? request.getDispatchIds().size() : 0)
                .processedDocuments(0)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        // Audit log
        auditLogService.log("BULK_DOWNLOAD_INITIATED", "BulkDownload", downloadId,
                "Bulk Download", "CREATE", null,
                Map.of("vendorId", vendorId, "totalDocuments", response.getTotalDocuments()), null);

        log.info("Vendor {} initiated bulk download with id: {}", vendorId, downloadId);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public BulkDownloadResponse getDownloadStatus(Long vendorId, String downloadId) {
        validateVendor(vendorId);

        // In a real implementation, this would fetch from a job tracking table
        return BulkDownloadResponse.builder()
                .downloadId(downloadId)
                .status("READY")
                .downloadUrl("/api/v1/notices/vendor-portal/downloads/" + downloadId + "/file")
                .totalDocuments(10)
                .processedDocuments(10)
                .fileSizeBytes(1024000L)
                .createdAt(LocalDateTime.now().minusMinutes(5))
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
    }

    @Override
    @Transactional
    public int markCasesAsDispatched(Long vendorId, List<Long> dispatchIds, String trackingNumber, String carrierName) {
        validateVendor(vendorId);

        List<DispatchTracking> dispatches = dispatchTrackingRepository.findByVendorIdAndIdIn(vendorId, dispatchIds);

        LocalDateTime now = LocalDateTime.now();
        int count = 0;

        for (DispatchTracking dispatch : dispatches) {
            if (dispatch.getDispatchStatus() == DispatchStatus.PENDING) {
                String oldStatus = dispatch.getDispatchStatus().name();

                dispatch.setDispatchStatus(DispatchStatus.DISPATCHED);
                dispatch.setDispatchedAt(now);
                dispatch.setTrackingNumber(trackingNumber);
                dispatch.setCarrierName(carrierName);
                dispatchTrackingRepository.save(dispatch);

                // Create status history
                DispatchStatusHistory history = DispatchStatusHistory.builder()
                        .dispatchTracking(dispatch)
                        .status(DispatchStatus.DISPATCHED.name())
                        .eventTimestamp(now)
                        .source("VENDOR_PORTAL_BULK")
                        .remarks("Bulk dispatch by vendor")
                        .build();
                statusHistoryRepository.save(history);

                // Audit log
                auditLogService.logStatusChange("DispatchTracking", dispatch.getId().toString(),
                        dispatch.getTrackingId(), oldStatus, DispatchStatus.DISPATCHED.name());

                count++;
            }
        }

        log.info("Vendor {} bulk dispatched {} cases", vendorId, count);
        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VendorCaseDTO> getSlaAtRiskCases(Long vendorId, Integer hoursThreshold, Pageable pageable) {
        validateVendor(vendorId);

        LocalDateTime threshold = LocalDateTime.now().plusHours(hoursThreshold != null ? hoursThreshold : 24);
        Page<DispatchTracking> dispatches = dispatchTrackingRepository.findSlaAtRiskByVendor(vendorId, threshold, pageable);

        return dispatches.map(this::mapToVendorCaseDTO);
    }

    private void validateVendor(Long vendorId) {
        if (!vendorRepository.existsById(vendorId)) {
            throw new ResourceNotFoundException("Vendor not found with id: " + vendorId);
        }
    }

    private VendorCaseDTO mapToVendorCaseDTO(DispatchTracking dispatch) {
        Notice notice = dispatch.getNotice();
        LocalDateTime now = LocalDateTime.now();

        Long hoursToDispatchSla = null;
        Long daysToDeliverySla = null;

        if (dispatch.getExpectedDispatchBy() != null && dispatch.getDispatchStatus() == DispatchStatus.PENDING) {
            hoursToDispatchSla = ChronoUnit.HOURS.between(now, dispatch.getExpectedDispatchBy());
        }
        if (dispatch.getExpectedDeliveryBy() != null &&
                dispatch.getDispatchStatus() != DispatchStatus.DELIVERED &&
                dispatch.getDispatchStatus() != DispatchStatus.RTO &&
                dispatch.getDispatchStatus() != DispatchStatus.FAILED) {
            daysToDeliverySla = ChronoUnit.DAYS.between(now, dispatch.getExpectedDeliveryBy());
        }

        return VendorCaseDTO.builder()
                .dispatchId(dispatch.getId())
                .trackingId(dispatch.getTrackingId())
                .noticeId(notice.getId())
                .noticeNumber(notice.getNoticeNumber())
                .noticeType(notice.getNoticeType())
                .noticeSubtype(notice.getNoticeSubtype())
                .caseId(notice.getCaseId())
                .loanAccountNumber(notice.getLoanAccountNumber())
                .customerName(notice.getCustomerName())
                .recipientName(notice.getRecipientName())
                .recipientAddress(notice.getRecipientAddress())
                .recipientCity(notice.getRecipientCity())
                .recipientState(notice.getRecipientState())
                .recipientPincode(notice.getRecipientPincode())
                .dispatchStatus(dispatch.getDispatchStatus())
                .trackingNumber(dispatch.getTrackingNumber())
                .carrierName(dispatch.getCarrierName())
                .serviceType(dispatch.getServiceType())
                .createdAt(dispatch.getCreatedAt())
                .dispatchedAt(dispatch.getDispatchedAt())
                .expectedDispatchBy(dispatch.getExpectedDispatchBy())
                .expectedDeliveryBy(dispatch.getExpectedDeliveryBy())
                .deliveredAt(dispatch.getDeliveredAt())
                .dispatchSlaBreached(dispatch.getDispatchSlaBreached())
                .deliverySlaBreached(dispatch.getDeliverySlaBreached())
                .hoursToDispatchSla(hoursToDispatchSla)
                .daysToDeliverySla(daysToDeliverySla)
                .documentUrl(notice.getPdfUrl())
                .pageCount(notice.getPageCount())
                .build();
    }

    private DispatchTrackingDTO mapToDispatchDTO(DispatchTracking dispatch) {
        return DispatchTrackingDTO.builder()
                .id(dispatch.getId())
                .trackingId(dispatch.getTrackingId())
                .noticeId(dispatch.getNotice().getId())
                .noticeNumber(dispatch.getNoticeNumber())
                .vendorId(dispatch.getVendor() != null ? dispatch.getVendor().getId() : null)
                .vendorName(dispatch.getVendorName())
                .trackingNumber(dispatch.getTrackingNumber())
                .carrierName(dispatch.getCarrierName())
                .serviceType(dispatch.getServiceType())
                .dispatchStatus(dispatch.getDispatchStatus())
                .currentLocation(dispatch.getCurrentLocation())
                .currentStatusRemarks(dispatch.getCurrentStatusRemarks())
                .createdAt(dispatch.getCreatedAt())
                .dispatchedAt(dispatch.getDispatchedAt())
                .pickedUpAt(dispatch.getPickedUpAt())
                .inTransitAt(dispatch.getInTransitAt())
                .outForDeliveryAt(dispatch.getOutForDeliveryAt())
                .deliveredAt(dispatch.getDeliveredAt())
                .rtoInitiatedAt(dispatch.getRtoInitiatedAt())
                .rtoReceivedAt(dispatch.getRtoReceivedAt())
                .deliveryAttemptCount(dispatch.getDeliveryAttemptCount())
                .lastAttemptAt(dispatch.getLastAttemptAt())
                .lastAttemptStatus(dispatch.getLastAttemptStatus())
                .lastAttemptRemarks(dispatch.getLastAttemptRemarks())
                .expectedDispatchBy(dispatch.getExpectedDispatchBy())
                .expectedDeliveryBy(dispatch.getExpectedDeliveryBy())
                .dispatchSlaBreached(dispatch.getDispatchSlaBreached())
                .deliverySlaBreached(dispatch.getDeliverySlaBreached())
                .slaBreachNotified(dispatch.getSlaBreachNotified())
                .rtoReason(dispatch.getRtoReason())
                .rtoAction(dispatch.getRtoAction())
                .rtoActionTakenAt(dispatch.getRtoActionTakenAt())
                .rtoActionTakenBy(dispatch.getRtoActionTakenBy())
                .podId(dispatch.getPodId())
                .podUploadedAt(dispatch.getPodUploadedAt())
                .dispatchCost(dispatch.getDispatchCost())
                .updatedAt(dispatch.getUpdatedAt())
                .updatedBy(dispatch.getUpdatedBy())
                .build();
    }
}
