package com.finx.noticemanagementservice.service;

import com.finx.noticemanagementservice.domain.dto.*;
import com.finx.noticemanagementservice.domain.enums.NoticeStatus;
import com.finx.noticemanagementservice.domain.enums.NoticeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface NoticeService {

    NoticeDTO createNotice(CreateNoticeRequest request);

    NoticeDTO getNoticeById(Long id);

    NoticeDTO getNoticeByNumber(String noticeNumber);

    List<NoticeDTO> getNoticesByCaseId(Long caseId);

    List<NoticeDTO> getNoticesByLoanAccountNumber(String loanAccountNumber);

    Page<NoticeDTO> getNoticesByStatus(NoticeStatus status, Pageable pageable);

    Page<NoticeDTO> getNoticesByType(NoticeType type, Pageable pageable);

    Page<NoticeDTO> getNoticesByVendor(Long vendorId, Pageable pageable);

    Page<NoticeDTO> getNoticesByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<NoticeDTO> getAllNotices(Pageable pageable);

    NoticeDTO generateNotice(GenerateNoticeRequest request);

    NoticeDTO dispatchNotice(DispatchNoticeRequest request);

    NoticeDTO updateDeliveryStatus(UpdateDeliveryStatusRequest request);

    NoticeDTO markAsDelivered(Long noticeId, Long podId);

    NoticeDTO markAsRto(Long noticeId, String rtoReason);

    void checkAndUpdateSlaBreach();

    NoticeStatsDTO getNoticeStats();

    List<NoticeDTO> getDispatchSlaBreaches();

    List<NoticeDTO> getDeliverySlaBreaches();

    void deleteNotice(Long id);
}
