package com.finx.noticemanagementservice.service.impl;

import com.finx.noticemanagementservice.domain.dto.DispatchTrackingDTO;
import com.finx.noticemanagementservice.domain.dto.UpdateDispatchStatusRequest;
import com.finx.noticemanagementservice.domain.entity.*;
import com.finx.noticemanagementservice.domain.enums.BreachSeverity;
import com.finx.noticemanagementservice.domain.enums.DispatchStatus;
import com.finx.noticemanagementservice.exception.ResourceNotFoundException;
import com.finx.noticemanagementservice.repository.*;
import com.finx.noticemanagementservice.service.AuditLogService;
import com.finx.noticemanagementservice.service.DispatchTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DispatchTrackingServiceImpl implements DispatchTrackingService {

    private final DispatchTrackingRepository dispatchTrackingRepository;
    private final DispatchStatusHistoryRepository statusHistoryRepository;
    private final NoticeRepository noticeRepository;
    private final NoticeVendorRepository vendorRepository;
    private final SlaBreachRepository slaBreachRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public DispatchTrackingDTO createDispatch(Long noticeId, Long vendorId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found with id: " + noticeId));

        NoticeVendor vendor = null;
        if (vendorId != null) {
            vendor = vendorRepository.findById(vendorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + vendorId));
        }

        LocalDateTime now = LocalDateTime.now();
        Integer dispatchSlaHours = vendor != null ? vendor.getDefaultDispatchSlaHours() : 24;
        Integer deliverySlaDays = vendor != null ? vendor.getDefaultDeliverySlaDays() : 7;

        DispatchTracking dispatch = DispatchTracking.builder()
                .trackingId(UUID.randomUUID().toString())
                .notice(notice)
                .noticeNumber(notice.getNoticeNumber())
                .vendor(vendor)
                .vendorName(vendor != null ? vendor.getVendorName() : null)
                .dispatchStatus(DispatchStatus.PENDING)
                .expectedDispatchBy(now.plusHours(dispatchSlaHours))
                .expectedDeliveryBy(now.plusDays(deliverySlaDays))
                .build();

        DispatchTracking saved = dispatchTrackingRepository.save(dispatch);

        // Create initial status history
        createStatusHistory(saved, DispatchStatus.PENDING.name(), null, "Dispatch created", "SYSTEM", null);

        // Audit log
        auditLogService.logCreate("DispatchTracking", saved.getId().toString(),
                saved.getTrackingId(), Map.of("noticeId", noticeId, "vendorId", vendorId));

        log.info("Created dispatch tracking: {} for notice: {}", saved.getTrackingId(), notice.getNoticeNumber());
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DispatchTrackingDTO getDispatchById(Long id) {
        DispatchTracking dispatch = dispatchTrackingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispatch not found with id: " + id));
        return mapToDTO(dispatch);
    }

    @Override
    @Transactional(readOnly = true)
    public DispatchTrackingDTO getDispatchByTrackingId(String trackingId) {
        DispatchTracking dispatch = dispatchTrackingRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispatch not found with tracking id: " + trackingId));
        return mapToDTO(dispatch);
    }

    @Override
    @Transactional(readOnly = true)
    public DispatchTrackingDTO getDispatchByTrackingNumber(String trackingNumber) {
        DispatchTracking dispatch = dispatchTrackingRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Dispatch not found with tracking number: " + trackingNumber));
        return mapToDTO(dispatch);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DispatchTrackingDTO> getDispatchesByNoticeId(Long noticeId) {
        return dispatchTrackingRepository.findByNoticeId(noticeId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DispatchTrackingDTO> getDispatchesByVendorId(Long vendorId) {
        return dispatchTrackingRepository.findByVendorId(vendorId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DispatchTrackingDTO updateDispatchStatus(Long id, UpdateDispatchStatusRequest request) {
        DispatchTracking dispatch = dispatchTrackingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispatch not found with id: " + id));

        String oldStatus = dispatch.getDispatchStatus().name();
        DispatchStatus newStatus = request.getStatus();
        LocalDateTime eventTime = request.getEventTimestamp() != null ? request.getEventTimestamp() : LocalDateTime.now();

        // Update status-specific timestamps
        dispatch.setDispatchStatus(newStatus);
        dispatch.setCurrentLocation(request.getLocation());
        dispatch.setCurrentStatusRemarks(request.getRemarks());

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
                dispatch.setRtoReason(request.getRemarks());
            }
            case FAILED -> {
                dispatch.setLastAttemptAt(eventTime);
                dispatch.setLastAttemptStatus("FAILED");
                dispatch.setLastAttemptRemarks(request.getRemarks());
            }
            default -> {}
        }

        DispatchTracking saved = dispatchTrackingRepository.save(dispatch);

        // Create status history
        createStatusHistory(saved, newStatus.name(), request.getLocation(), request.getRemarks(),
                request.getSource() != null ? request.getSource() : "MANUAL", request.getRawData());

        // Audit log
        auditLogService.logStatusChange("DispatchTracking", saved.getId().toString(),
                saved.getTrackingId(), oldStatus, newStatus.name());

        log.info("Updated dispatch {} status from {} to {}", saved.getTrackingId(), oldStatus, newStatus);
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DispatchTrackingDTO> getDispatchQueue(DispatchStatus status, Long vendorId, Boolean slaBreached, Pageable pageable) {
        return dispatchTrackingRepository.findWithFilters(status, vendorId, slaBreached, pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DispatchTrackingDTO> getSlaBreachedDispatches() {
        return dispatchTrackingRepository.findSlaBreached().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void checkAndMarkSlaBreaches() {
        LocalDateTime now = LocalDateTime.now();

        // Check dispatch SLA breaches
        List<DispatchTracking> dispatchBreaches = dispatchTrackingRepository.findPendingDispatchSlaBreaches(now);
        for (DispatchTracking dispatch : dispatchBreaches) {
            dispatch.setDispatchSlaBreached(true);
            dispatchTrackingRepository.save(dispatch);
            createSlaBreach(dispatch, "DISPATCH_SLA");
            log.warn("Dispatch SLA breached for: {}", dispatch.getTrackingId());
        }

        // Check delivery SLA breaches
        List<DispatchTracking> deliveryBreaches = dispatchTrackingRepository.findPendingDeliverySlaBreaches(now);
        for (DispatchTracking dispatch : deliveryBreaches) {
            dispatch.setDeliverySlaBreached(true);
            dispatchTrackingRepository.save(dispatch);
            createSlaBreach(dispatch, "DELIVERY_SLA");
            log.warn("Delivery SLA breached for: {}", dispatch.getTrackingId());
        }

        log.info("SLA breach check completed. Dispatch breaches: {}, Delivery breaches: {}",
                dispatchBreaches.size(), deliveryBreaches.size());
    }

    private void createStatusHistory(DispatchTracking dispatch, String status, String location,
                                      String remarks, String source, Map<String, Object> rawData) {
        DispatchStatusHistory history = DispatchStatusHistory.builder()
                .dispatchTracking(dispatch)
                .status(status)
                .location(location)
                .remarks(remarks)
                .eventTimestamp(LocalDateTime.now())
                .source(source)
                .rawData(rawData)
                .build();
        statusHistoryRepository.save(history);
    }

    private void createSlaBreach(DispatchTracking dispatch, String breachType) {
        LocalDateTime expectedBy = "DISPATCH_SLA".equals(breachType) ?
                dispatch.getExpectedDispatchBy() : dispatch.getExpectedDeliveryBy();

        int breachHours = (int) ChronoUnit.HOURS.between(expectedBy, LocalDateTime.now());
        BreachSeverity severity = breachHours > 48 ? BreachSeverity.HIGH :
                breachHours > 24 ? BreachSeverity.MEDIUM : BreachSeverity.LOW;

        SlaBreach breach = SlaBreach.builder()
                .breachId(UUID.randomUUID().toString())
                .breachType(breachType)
                .entityType("DispatchTracking")
                .entityId(dispatch.getId())
                .entityReference(dispatch.getTrackingId())
                .expectedBy(expectedBy)
                .breachedAt(LocalDateTime.now())
                .breachDurationHours(breachHours)
                .vendorId(dispatch.getVendor() != null ? dispatch.getVendor().getId() : null)
                .vendorName(dispatch.getVendorName())
                .breachSeverity(severity)
                .build();

        slaBreachRepository.save(breach);

        auditLogService.log("SLA_BREACH_CREATED", "SlaBreach", breach.getId().toString(),
                breach.getBreachId(), "CREATE", null,
                Map.of("breachType", breachType, "severity", severity.name()), null);
    }

    private DispatchTrackingDTO mapToDTO(DispatchTracking dispatch) {
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
