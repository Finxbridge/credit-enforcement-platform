package com.finx.dmsservice.service.impl;

import com.finx.dmsservice.domain.dto.NoticeDocumentDTO;
import com.finx.dmsservice.domain.dto.NoticeSearchCriteria;
import com.finx.dmsservice.domain.dto.NoticeSummaryDTO;
import com.finx.dmsservice.domain.entity.NoticeDocument;
import com.finx.dmsservice.domain.enums.NoticeStatus;
import com.finx.dmsservice.domain.enums.NoticeType;
import com.finx.dmsservice.exception.ResourceNotFoundException;
import com.finx.dmsservice.repository.NoticeDocumentRepository;
import com.finx.dmsservice.service.AuditLogService;
import com.finx.dmsservice.service.NoticeDocumentService;
import com.finx.dmsservice.service.StorageService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NoticeDocumentServiceImpl implements NoticeDocumentService {

    private final NoticeDocumentRepository noticeRepository;
    private final StorageService storageService;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(readOnly = true)
    public NoticeDocumentDTO getNoticeById(Long id) {
        NoticeDocument notice = noticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found with id: " + id));
        return toDTO(notice);
    }

    @Override
    @Transactional(readOnly = true)
    public NoticeDocumentDTO getNoticeByNumber(String noticeNumber) {
        NoticeDocument notice = noticeRepository.findByNoticeNumber(noticeNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found with number: " + noticeNumber));
        return toDTO(notice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoticeDocumentDTO> getNoticesByCaseId(Long caseId) {
        return noticeRepository.findByCaseId(caseId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoticeDocumentDTO> getNoticesByLoanAccount(String loanAccountNumber) {
        return noticeRepository.findByLoanAccountNumber(loanAccountNumber).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoticeDocumentDTO> getNoticesByCustomerId(Long customerId) {
        return noticeRepository.findByCustomerId(customerId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoticeDocumentDTO> getNoticesByType(NoticeType type, Pageable pageable) {
        return noticeRepository.findByNoticeType(type, pageable).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoticeDocumentDTO> getNoticesByStatus(NoticeStatus status, Pageable pageable) {
        return noticeRepository.findByNoticeStatus(status, pageable).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoticeDocumentDTO> getNoticesByTypeAndStatus(NoticeType type, NoticeStatus status, Pageable pageable) {
        return noticeRepository.findByNoticeTypeAndNoticeStatus(type, status, pageable).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoticeDocumentDTO> getNoticesByRegion(String region, Pageable pageable) {
        return noticeRepository.findByRegion(region, pageable).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoticeDocumentDTO> getNoticesByProductType(String productType, Pageable pageable) {
        return noticeRepository.findByProductType(productType, pageable).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoticeDocumentDTO> getNoticesByVendor(Long vendorId, Pageable pageable) {
        return noticeRepository.findByDispatchVendorId(vendorId, pageable).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoticeDocumentDTO> getNoticesByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return noticeRepository.findByGeneratedDateRange(startDate, endDate, pageable).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoticeDocumentDTO> getAllNotices(Pageable pageable) {
        return noticeRepository.findAll(pageable).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoticeDocumentDTO> searchNotices(NoticeSearchCriteria criteria, Pageable pageable) {
        Specification<NoticeDocument> spec = buildSpecification(criteria);
        return noticeRepository.findAll(spec, pageable).map(this::toDTO);
    }

    @Override
    public byte[] previewNotice(Long id, Long userId) {
        NoticeDocument notice = noticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found with id: " + id));

        auditLogService.logNoticeEvent(id, notice.getNoticeNumber(), "PREVIEW", userId,
                "Notice previewed by user: " + userId);

        log.info("Notice {} previewed by user {}", notice.getNoticeNumber(), userId);

        // Fetch content from storage
        if (notice.getStoragePath() != null) {
            return storageService.downloadFile(storageService.getDocumentsBucket(), notice.getStoragePath());
        }
        return new byte[0];
    }

    @Override
    public String getNoticePreviewUrl(Long id, Long userId) {
        NoticeDocument notice = noticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found with id: " + id));

        auditLogService.logNoticeEvent(id, notice.getNoticeNumber(), "PREVIEW_URL_GENERATED", userId,
                "Preview URL generated for user: " + userId);

        // Generate signed URL for preview (60 minutes)
        if (notice.getStoragePath() != null) {
            return storageService.getPresignedUrl(storageService.getDocumentsBucket(), notice.getStoragePath(), 60);
        }
        return notice.getFileUrl();
    }

    @Override
    public byte[] downloadNotice(Long id, Long userId) {
        NoticeDocument notice = noticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found with id: " + id));

        auditLogService.logNoticeEvent(id, notice.getNoticeNumber(), "DOWNLOAD", userId,
                "Notice downloaded by user: " + userId);

        log.info("Notice {} downloaded by user {}", notice.getNoticeNumber(), userId);

        // Fetch content from storage
        if (notice.getStoragePath() != null) {
            return storageService.downloadFile(storageService.getDocumentsBucket(), notice.getStoragePath());
        }
        return new byte[0];
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoticeDocumentDTO> getNoticeVersions(Long noticeId) {
        return noticeRepository.findVersionsByParentId(noticeId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public NoticeSummaryDTO getNoticeSummary() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        // Get counts by type
        Map<String, Long> noticesByType = new HashMap<>();
        for (NoticeType type : NoticeType.values()) {
            noticesByType.put(type.name(), noticeRepository.countByType(type));
        }

        // Calculate delivery success rate
        Long delivered = noticeRepository.countDelivered();
        Long totalDispatched = noticeRepository.countTotalDispatched();
        Double deliverySuccessRate = totalDispatched > 0 ?
                (delivered * 100.0) / totalDispatched : 0.0;

        return NoticeSummaryDTO.builder()
                .totalNotices(noticeRepository.count())
                .draftNotices(noticeRepository.countByStatus(NoticeStatus.DRAFT))
                .pendingApprovalNotices(noticeRepository.countByStatus(NoticeStatus.PENDING_APPROVAL))
                .approvedNotices(noticeRepository.countByStatus(NoticeStatus.APPROVED))
                .generatedNotices(noticeRepository.countByStatus(NoticeStatus.GENERATED))
                .dispatchedNotices(noticeRepository.countByStatus(NoticeStatus.DISPATCHED))
                .deliveredNotices(noticeRepository.countByStatus(NoticeStatus.DELIVERED))
                .returnedNotices(noticeRepository.countByStatus(NoticeStatus.RETURNED))
                .failedNotices(noticeRepository.countByStatus(NoticeStatus.FAILED))
                .cancelledNotices(noticeRepository.countByStatus(NoticeStatus.CANCELLED))
                .noticesByType(noticesByType)
                .todayGenerated(noticeRepository.countGeneratedSince(todayStart))
                .todayDispatched(noticeRepository.countDispatchedSince(todayStart))
                .todayDelivered(noticeRepository.countDeliveredSince(todayStart))
                .totalDuesAmount(noticeRepository.sumTotalDues())
                .averageDuesAmount(noticeRepository.avgTotalDues())
                .dpdUnder30(noticeRepository.countDpdUnder30())
                .dpd30to60(noticeRepository.countDpd30to60())
                .dpd60to90(noticeRepository.countDpd60to90())
                .dpd90to180(noticeRepository.countDpd90to180())
                .dpdOver180(noticeRepository.countDpdOver180())
                .deliverySuccessRate(deliverySuccessRate)
                .pendingDelivery(noticeRepository.countPendingDelivery())
                .overdueResponses(noticeRepository.countOverdueResponses(LocalDate.now()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public NoticeSummaryDTO getNoticeSummaryByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        // For date range summary, using the general summary for now
        // In production, add date-filtered queries
        return getNoticeSummary();
    }

    @Override
    @Transactional(readOnly = true)
    public Long countNoticesByCaseId(Long caseId) {
        return (long) noticeRepository.findByCaseId(caseId).size();
    }

    @Override
    @Transactional(readOnly = true)
    public Long countNoticesByStatus(NoticeStatus status) {
        return noticeRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countNoticesByType(NoticeType type) {
        return noticeRepository.countByType(type);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoticeDocumentDTO> getOverdueResponses(Pageable pageable) {
        return noticeRepository.findOverdueResponses(LocalDate.now(), pageable).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countOverdueResponses() {
        return noticeRepository.countOverdueResponses(LocalDate.now());
    }

    // Helper methods

    private NoticeDocumentDTO toDTO(NoticeDocument notice) {
        return NoticeDocumentDTO.builder()
                .id(notice.getId())
                .noticeNumber(notice.getNoticeNumber())
                .noticeType(notice.getNoticeType())
                .noticeStatus(notice.getNoticeStatus())
                .caseId(notice.getCaseId())
                .loanAccountNumber(notice.getLoanAccountNumber())
                .customerId(notice.getCustomerId())
                .customerName(notice.getCustomerName())
                .principalAmount(notice.getPrincipalAmount())
                .totalDues(notice.getTotalDues())
                .dpd(notice.getDpd())
                .bucket(notice.getBucket())
                .templateId(notice.getTemplateId())
                .templateName(notice.getTemplateName())
                .documentName(notice.getDocumentName())
                .fileUrl(notice.getFileUrl())
                .fileName(notice.getFileName())
                .fileType(notice.getFileType())
                .fileSizeBytes(notice.getFileSizeBytes())
                .storageProvider(notice.getStorageProvider())
                .storagePath(notice.getStoragePath())
                .deliveryAddress(notice.getDeliveryAddress())
                .city(notice.getCity())
                .state(notice.getState())
                .pincode(notice.getPincode())
                .region(notice.getRegion())
                .productType(notice.getProductType())
                .productName(notice.getProductName())
                .generatedAt(notice.getGeneratedAt())
                .generatedBy(notice.getGeneratedBy())
                .generatedByName(notice.getGeneratedByName())
                .approvedAt(notice.getApprovedAt())
                .approvedBy(notice.getApprovedBy())
                .approvedByName(notice.getApprovedByName())
                .dispatchedAt(notice.getDispatchedAt())
                .dispatchVendorId(notice.getDispatchVendorId())
                .dispatchVendorName(notice.getDispatchVendorName())
                .trackingNumber(notice.getTrackingNumber())
                .deliveredAt(notice.getDeliveredAt())
                .deliveryProofUrl(notice.getDeliveryProofUrl())
                .deliveryStatus(notice.getDeliveryStatus())
                .responseDueDate(notice.getResponseDueDate())
                .responseReceivedAt(notice.getResponseReceivedAt())
                .responseNotes(notice.getResponseNotes())
                .versionNumber(notice.getVersionNumber())
                .parentNoticeId(notice.getParentNoticeId())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .createdBy(notice.getCreatedBy())
                .build();
    }

    private Specification<NoticeDocument> buildSpecification(NoticeSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getNoticeNumber() != null && !criteria.getNoticeNumber().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("noticeNumber")),
                        "%" + criteria.getNoticeNumber().toLowerCase() + "%"));
            }

            if (criteria.getNoticeType() != null) {
                predicates.add(cb.equal(root.get("noticeType"), criteria.getNoticeType()));
            }

            if (criteria.getNoticeStatus() != null) {
                predicates.add(cb.equal(root.get("noticeStatus"), criteria.getNoticeStatus()));
            }

            if (criteria.getCaseId() != null) {
                predicates.add(cb.equal(root.get("caseId"), criteria.getCaseId()));
            }

            if (criteria.getLoanAccountNumber() != null && !criteria.getLoanAccountNumber().isEmpty()) {
                predicates.add(cb.equal(root.get("loanAccountNumber"), criteria.getLoanAccountNumber()));
            }

            if (criteria.getCustomerName() != null && !criteria.getCustomerName().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("customerName")),
                        "%" + criteria.getCustomerName().toLowerCase() + "%"));
            }

            if (criteria.getCustomerId() != null) {
                predicates.add(cb.equal(root.get("customerId"), criteria.getCustomerId()));
            }

            if (criteria.getMinDpd() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dpd"), criteria.getMinDpd()));
            }

            if (criteria.getMaxDpd() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dpd"), criteria.getMaxDpd()));
            }

            if (criteria.getBucket() != null && !criteria.getBucket().isEmpty()) {
                predicates.add(cb.equal(root.get("bucket"), criteria.getBucket()));
            }

            if (criteria.getMinAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("totalDues"), criteria.getMinAmount()));
            }

            if (criteria.getMaxAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("totalDues"), criteria.getMaxAmount()));
            }

            if (criteria.getGeneratedDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("generatedAt"),
                        criteria.getGeneratedDateFrom().atStartOfDay()));
            }

            if (criteria.getGeneratedDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("generatedAt"),
                        criteria.getGeneratedDateTo().atTime(LocalTime.MAX)));
            }

            if (criteria.getDispatchedDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dispatchedAt"),
                        criteria.getDispatchedDateFrom().atStartOfDay()));
            }

            if (criteria.getDispatchedDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dispatchedAt"),
                        criteria.getDispatchedDateTo().atTime(LocalTime.MAX)));
            }

            if (criteria.getRegion() != null && !criteria.getRegion().isEmpty()) {
                predicates.add(cb.equal(root.get("region"), criteria.getRegion()));
            }

            if (criteria.getState() != null && !criteria.getState().isEmpty()) {
                predicates.add(cb.equal(root.get("state"), criteria.getState()));
            }

            if (criteria.getCity() != null && !criteria.getCity().isEmpty()) {
                predicates.add(cb.equal(root.get("city"), criteria.getCity()));
            }

            if (criteria.getPincode() != null && !criteria.getPincode().isEmpty()) {
                predicates.add(cb.equal(root.get("pincode"), criteria.getPincode()));
            }

            if (criteria.getProductType() != null && !criteria.getProductType().isEmpty()) {
                predicates.add(cb.equal(root.get("productType"), criteria.getProductType()));
            }

            if (criteria.getTemplateId() != null) {
                predicates.add(cb.equal(root.get("templateId"), criteria.getTemplateId()));
            }

            if (criteria.getDispatchVendorId() != null) {
                predicates.add(cb.equal(root.get("dispatchVendorId"), criteria.getDispatchVendorId()));
            }

            if (Boolean.TRUE.equals(criteria.getHasResponse())) {
                predicates.add(cb.isNotNull(root.get("responseReceivedAt")));
            } else if (Boolean.FALSE.equals(criteria.getHasResponse())) {
                predicates.add(cb.isNull(root.get("responseReceivedAt")));
            }

            if (Boolean.TRUE.equals(criteria.getIsOverdue())) {
                predicates.add(cb.lessThan(root.get("responseDueDate"), LocalDate.now()));
                predicates.add(cb.isNull(root.get("responseReceivedAt")));
                predicates.add(cb.equal(root.get("noticeStatus"), NoticeStatus.DELIVERED));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
