package com.finx.noticemanagementservice.service.impl;

import com.finx.noticemanagementservice.config.CacheConstants;
import com.finx.noticemanagementservice.domain.dto.*;
import com.finx.noticemanagementservice.domain.entity.Notice;
import com.finx.noticemanagementservice.domain.entity.NoticeVendor;
import com.finx.noticemanagementservice.domain.enums.NoticeStatus;
import com.finx.noticemanagementservice.domain.enums.NoticeType;
import com.finx.noticemanagementservice.exception.BusinessException;
import com.finx.noticemanagementservice.exception.ResourceNotFoundException;
import com.finx.noticemanagementservice.mapper.NoticeMapper;
import com.finx.noticemanagementservice.repository.NoticeRepository;
import com.finx.noticemanagementservice.repository.NoticeVendorRepository;
import com.finx.noticemanagementservice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeVendorRepository vendorRepository;
    private final NoticeMapper noticeMapper;

    @Override
    @CacheEvict(value = {CacheConstants.NOTICE_CACHE, CacheConstants.NOTICE_BY_CASE_CACHE,
                         CacheConstants.NOTICE_STATS_CACHE}, allEntries = true)
    public NoticeDTO createNotice(CreateNoticeRequest request) {
        log.info("Creating notice for case ID: {}", request.getCaseId());

        Notice notice = noticeMapper.toEntity(request);
        notice.setNoticeNumber(generateNoticeNumber());
        notice.setNoticeStatus(NoticeStatus.DRAFT);
        notice.setCreatedBy(request.getCreatedBy());

        Notice savedNotice = noticeRepository.save(notice);
        log.info("Notice created with ID: {}", savedNotice.getId());

        return noticeMapper.toDto(savedNotice);
    }

    @Override
    @Cacheable(value = CacheConstants.NOTICE_CACHE, key = "#id")
    @Transactional(readOnly = true)
    public NoticeDTO getNoticeById(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notice", id));
        return enrichWithVendorName(noticeMapper.toDto(notice));
    }

    @Override
    @Transactional(readOnly = true)
    public NoticeDTO getNoticeByNumber(String noticeNumber) {
        Notice notice = noticeRepository.findByNoticeNumber(noticeNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Notice", noticeNumber));
        return enrichWithVendorName(noticeMapper.toDto(notice));
    }

    @Override
    @Cacheable(value = CacheConstants.NOTICE_BY_CASE_CACHE, key = "#caseId")
    @Transactional(readOnly = true)
    public List<NoticeDTO> getNoticesByCaseId(Long caseId) {
        List<Notice> notices = noticeRepository.findByCaseId(caseId);
        return notices.stream()
                .map(noticeMapper::toDto)
                .map(this::enrichWithVendorName)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoticeDTO> getNoticesByLoanAccountNumber(String loanAccountNumber) {
        List<Notice> notices = noticeRepository.findByLoanAccountNumber(loanAccountNumber);
        return notices.stream()
                .map(noticeMapper::toDto)
                .map(this::enrichWithVendorName)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoticeDTO> getNoticesByStatus(NoticeStatus status, Pageable pageable) {
        return noticeRepository.findByNoticeStatus(status, pageable)
                .map(noticeMapper::toDto)
                .map(this::enrichWithVendorName);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoticeDTO> getNoticesByType(NoticeType type, Pageable pageable) {
        return noticeRepository.findByNoticeType(type, pageable)
                .map(noticeMapper::toDto)
                .map(this::enrichWithVendorName);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoticeDTO> getNoticesByVendor(Long vendorId, Pageable pageable) {
        return noticeRepository.findByVendorId(vendorId, pageable)
                .map(noticeMapper::toDto)
                .map(this::enrichWithVendorName);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoticeDTO> getNoticesByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return noticeRepository.findByDateRange(startDate, endDate, pageable)
                .map(noticeMapper::toDto)
                .map(this::enrichWithVendorName);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoticeDTO> getAllNotices(Pageable pageable) {
        return noticeRepository.findAll(pageable)
                .map(noticeMapper::toDto)
                .map(this::enrichWithVendorName);
    }

    @Override
    @CacheEvict(value = {CacheConstants.NOTICE_CACHE, CacheConstants.NOTICE_STATS_CACHE}, allEntries = true)
    public NoticeDTO generateNotice(GenerateNoticeRequest request) {
        log.info("Generating notice for ID: {}", request.getNoticeId());

        Notice notice = noticeRepository.findById(request.getNoticeId())
                .orElseThrow(() -> new ResourceNotFoundException("Notice", request.getNoticeId()));

        if (notice.getNoticeStatus() != NoticeStatus.DRAFT) {
            throw new BusinessException("Notice can only be generated from DRAFT status");
        }

        // TODO: Integrate with template service to generate content
        notice.setTemplateId(request.getTemplateId());
        notice.setGeneratedContent("Generated content placeholder");
        notice.setNoticeStatus(NoticeStatus.GENERATED);
        notice.setGeneratedAt(LocalDateTime.now());
        notice.setGeneratedBy(request.getGeneratedBy());

        Notice savedNotice = noticeRepository.save(notice);
        log.info("Notice generated successfully: {}", savedNotice.getId());

        return noticeMapper.toDto(savedNotice);
    }

    @Override
    @CacheEvict(value = {CacheConstants.NOTICE_CACHE, CacheConstants.NOTICE_STATS_CACHE}, allEntries = true)
    public NoticeDTO dispatchNotice(DispatchNoticeRequest request) {
        log.info("Dispatching notice ID: {}", request.getNoticeId());

        Notice notice = noticeRepository.findById(request.getNoticeId())
                .orElseThrow(() -> new ResourceNotFoundException("Notice", request.getNoticeId()));

        if (notice.getNoticeStatus() != NoticeStatus.GENERATED) {
            throw new BusinessException("Notice can only be dispatched from GENERATED status");
        }

        NoticeVendor vendor = vendorRepository.findById(request.getVendorId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", request.getVendorId()));

        notice.setVendorId(request.getVendorId());
        notice.setTrackingNumber(request.getTrackingNumber());
        notice.setCarrierName(request.getCarrierName() != null ? request.getCarrierName() : vendor.getVendorName());
        notice.setExpectedDeliveryAt(request.getExpectedDeliveryAt() != null
                ? request.getExpectedDeliveryAt()
                : LocalDateTime.now().plusDays(vendor.getDefaultDeliverySlaDays()));
        notice.setNoticeStatus(NoticeStatus.DISPATCHED);
        notice.setDispatchedAt(LocalDateTime.now());
        notice.setDispatchedBy(request.getDispatchedBy());

        Notice savedNotice = noticeRepository.save(notice);
        log.info("Notice dispatched successfully: {}", savedNotice.getId());

        return enrichWithVendorName(noticeMapper.toDto(savedNotice));
    }

    @Override
    @CacheEvict(value = {CacheConstants.NOTICE_CACHE, CacheConstants.NOTICE_STATS_CACHE}, allEntries = true)
    public NoticeDTO updateDeliveryStatus(UpdateDeliveryStatusRequest request) {
        log.info("Updating delivery status for notice ID: {}", request.getNoticeId());

        Notice notice = noticeRepository.findById(request.getNoticeId())
                .orElseThrow(() -> new ResourceNotFoundException("Notice", request.getNoticeId()));

        notice.setNoticeStatus(request.getStatus());

        if (request.getDeliveredAt() != null) {
            notice.setDeliveredAt(request.getDeliveredAt());
        }
        if (request.getRtoAt() != null) {
            notice.setRtoAt(request.getRtoAt());
            notice.setRtoReason(request.getRtoReason());
        }
        notice.setUpdatedBy(request.getUpdatedBy());

        Notice savedNotice = noticeRepository.save(notice);
        return enrichWithVendorName(noticeMapper.toDto(savedNotice));
    }

    @Override
    @CacheEvict(value = {CacheConstants.NOTICE_CACHE, CacheConstants.NOTICE_STATS_CACHE}, allEntries = true)
    public NoticeDTO markAsDelivered(Long noticeId, Long podId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new ResourceNotFoundException("Notice", noticeId));

        notice.setNoticeStatus(NoticeStatus.DELIVERED);
        notice.setDeliveredAt(LocalDateTime.now());
        notice.setPodId(podId);

        Notice savedNotice = noticeRepository.save(notice);
        return enrichWithVendorName(noticeMapper.toDto(savedNotice));
    }

    @Override
    @CacheEvict(value = {CacheConstants.NOTICE_CACHE, CacheConstants.NOTICE_STATS_CACHE}, allEntries = true)
    public NoticeDTO markAsRto(Long noticeId, String rtoReason) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new ResourceNotFoundException("Notice", noticeId));

        notice.setNoticeStatus(NoticeStatus.RTO);
        notice.setRtoAt(LocalDateTime.now());
        notice.setRtoReason(rtoReason);

        Notice savedNotice = noticeRepository.save(notice);
        return enrichWithVendorName(noticeMapper.toDto(savedNotice));
    }

    @Override
    @Scheduled(cron = "0 0 * * * *") // Run every hour
    @CacheEvict(value = CacheConstants.NOTICE_STATS_CACHE, allEntries = true)
    public void checkAndUpdateSlaBreach() {
        log.info("Checking for SLA breaches...");

        LocalDateTime now = LocalDateTime.now();
        List<Notice> overdueDeliveries = noticeRepository.findOverdueDeliveries(now);

        for (Notice notice : overdueDeliveries) {
            notice.setDeliverySlaBreach(true);
            noticeRepository.save(notice);
        }

        log.info("Updated {} notices with delivery SLA breach", overdueDeliveries.size());
    }

    @Override
    @Cacheable(value = CacheConstants.NOTICE_STATS_CACHE)
    @Transactional(readOnly = true)
    public NoticeStatsDTO getNoticeStats() {
        long total = noticeRepository.count();
        long draft = noticeRepository.countByStatus(NoticeStatus.DRAFT);
        long generated = noticeRepository.countByStatus(NoticeStatus.GENERATED);
        long dispatched = noticeRepository.countByStatus(NoticeStatus.DISPATCHED);
        long inTransit = noticeRepository.countByStatus(NoticeStatus.IN_TRANSIT);
        long delivered = noticeRepository.countByStatus(NoticeStatus.DELIVERED);
        long rto = noticeRepository.countByStatus(NoticeStatus.RTO);
        long failed = noticeRepository.countByStatus(NoticeStatus.FAILED);
        long dispatchBreaches = noticeRepository.countDispatchSlaBreaches();
        long deliveryBreaches = noticeRepository.countDeliverySlaBreaches();

        double deliveryRate = total > 0 ? (double) delivered / total * 100 : 0;
        double rtoRate = total > 0 ? (double) rto / total * 100 : 0;

        return NoticeStatsDTO.builder()
                .totalNotices(total)
                .draftNotices(draft)
                .generatedNotices(generated)
                .dispatchedNotices(dispatched)
                .inTransitNotices(inTransit)
                .deliveredNotices(delivered)
                .rtoNotices(rto)
                .failedNotices(failed)
                .dispatchSlaBreaches(dispatchBreaches)
                .deliverySlaBreaches(deliveryBreaches)
                .deliveryRate(deliveryRate)
                .rtoRate(rtoRate)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoticeDTO> getDispatchSlaBreaches() {
        return noticeRepository.findDispatchSlaBreaches().stream()
                .map(noticeMapper::toDto)
                .map(this::enrichWithVendorName)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoticeDTO> getDeliverySlaBreaches() {
        return noticeRepository.findDeliverySlaBreaches().stream()
                .map(noticeMapper::toDto)
                .map(this::enrichWithVendorName)
                .toList();
    }

    @Override
    @CacheEvict(value = {CacheConstants.NOTICE_CACHE, CacheConstants.NOTICE_BY_CASE_CACHE,
                         CacheConstants.NOTICE_STATS_CACHE}, allEntries = true)
    public void deleteNotice(Long id) {
        if (!noticeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Notice", id);
        }
        noticeRepository.deleteById(id);
        log.info("Notice deleted: {}", id);
    }

    private String generateNoticeNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uniquePart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "NTC-" + datePart + "-" + uniquePart;
    }

    private NoticeDTO enrichWithVendorName(NoticeDTO dto) {
        if (dto.getVendorId() != null) {
            vendorRepository.findById(dto.getVendorId())
                    .ifPresent(vendor -> dto.setVendorName(vendor.getVendorName()));
        }
        return dto;
    }
}
