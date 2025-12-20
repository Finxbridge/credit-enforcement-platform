package com.finx.noticemanagementservice.service;

import com.finx.noticemanagementservice.domain.dto.DispatchTrackingDTO;
import com.finx.noticemanagementservice.domain.dto.UpdateDispatchStatusRequest;
import com.finx.noticemanagementservice.domain.enums.DispatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DispatchTrackingService {

    DispatchTrackingDTO createDispatch(Long noticeId, Long vendorId);

    DispatchTrackingDTO getDispatchById(Long id);

    DispatchTrackingDTO getDispatchByTrackingId(String trackingId);

    DispatchTrackingDTO getDispatchByTrackingNumber(String trackingNumber);

    List<DispatchTrackingDTO> getDispatchesByNoticeId(Long noticeId);

    List<DispatchTrackingDTO> getDispatchesByVendorId(Long vendorId);

    DispatchTrackingDTO updateDispatchStatus(Long id, UpdateDispatchStatusRequest request);

    Page<DispatchTrackingDTO> getDispatchQueue(DispatchStatus status, Long vendorId, Boolean slaBreached, Pageable pageable);

    List<DispatchTrackingDTO> getSlaBreachedDispatches();

    void checkAndMarkSlaBreaches();
}
