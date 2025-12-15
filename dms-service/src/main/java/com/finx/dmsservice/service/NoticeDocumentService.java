package com.finx.dmsservice.service;

import com.finx.dmsservice.domain.dto.NoticeDocumentDTO;
import com.finx.dmsservice.domain.dto.NoticeSearchCriteria;
import com.finx.dmsservice.domain.dto.NoticeSummaryDTO;
import com.finx.dmsservice.domain.enums.NoticeStatus;
import com.finx.dmsservice.domain.enums.NoticeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface NoticeDocumentService {

    // Retrieval
    NoticeDocumentDTO getNoticeById(Long id);

    NoticeDocumentDTO getNoticeByNumber(String noticeNumber);

    List<NoticeDocumentDTO> getNoticesByCaseId(Long caseId);

    List<NoticeDocumentDTO> getNoticesByLoanAccount(String loanAccountNumber);

    List<NoticeDocumentDTO> getNoticesByCustomerId(Long customerId);

    Page<NoticeDocumentDTO> getNoticesByType(NoticeType type, Pageable pageable);

    Page<NoticeDocumentDTO> getNoticesByStatus(NoticeStatus status, Pageable pageable);

    Page<NoticeDocumentDTO> getNoticesByTypeAndStatus(NoticeType type, NoticeStatus status, Pageable pageable);

    Page<NoticeDocumentDTO> getNoticesByRegion(String region, Pageable pageable);

    Page<NoticeDocumentDTO> getNoticesByProductType(String productType, Pageable pageable);

    Page<NoticeDocumentDTO> getNoticesByVendor(Long vendorId, Pageable pageable);

    Page<NoticeDocumentDTO> getNoticesByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<NoticeDocumentDTO> getAllNotices(Pageable pageable);

    // Search
    Page<NoticeDocumentDTO> searchNotices(NoticeSearchCriteria criteria, Pageable pageable);

    // Preview
    byte[] previewNotice(Long id, Long userId);

    String getNoticePreviewUrl(Long id, Long userId);

    // Download
    byte[] downloadNotice(Long id, Long userId);

    // Versions
    List<NoticeDocumentDTO> getNoticeVersions(Long noticeId);

    // Summary
    NoticeSummaryDTO getNoticeSummary();

    NoticeSummaryDTO getNoticeSummaryByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    // Counts
    Long countNoticesByCaseId(Long caseId);

    Long countNoticesByStatus(NoticeStatus status);

    Long countNoticesByType(NoticeType type);

    // Overdue
    Page<NoticeDocumentDTO> getOverdueResponses(Pageable pageable);

    Long countOverdueResponses();
}
