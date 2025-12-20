package com.finx.noticemanagementservice.service.impl;

import com.finx.noticemanagementservice.config.CacheConstants;
import com.finx.noticemanagementservice.domain.dto.ProofOfDeliveryDTO;
import com.finx.noticemanagementservice.domain.dto.UploadPodRequest;
import com.finx.noticemanagementservice.domain.dto.VerifyPodRequest;
import com.finx.noticemanagementservice.domain.entity.Notice;
import com.finx.noticemanagementservice.domain.entity.NoticeVendor;
import com.finx.noticemanagementservice.domain.entity.ProofOfDelivery;
import com.finx.noticemanagementservice.domain.enums.NoticeStatus;
import com.finx.noticemanagementservice.domain.enums.PodVerificationStatus;
import com.finx.noticemanagementservice.exception.BusinessException;
import com.finx.noticemanagementservice.exception.ResourceNotFoundException;
import com.finx.noticemanagementservice.mapper.NoticeMapper;
import com.finx.noticemanagementservice.repository.NoticeRepository;
import com.finx.noticemanagementservice.repository.NoticeVendorRepository;
import com.finx.noticemanagementservice.repository.ProofOfDeliveryRepository;
import com.finx.noticemanagementservice.service.PodService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class PodServiceImpl implements PodService {

    private final ProofOfDeliveryRepository podRepository;
    private final NoticeRepository noticeRepository;
    private final NoticeVendorRepository vendorRepository;
    private final NoticeMapper noticeMapper;

    @Override
    @CacheEvict(value = {CacheConstants.POD_CACHE, CacheConstants.POD_BY_NOTICE_CACHE,
                         CacheConstants.NOTICE_CACHE, CacheConstants.NOTICE_STATS_CACHE}, allEntries = true)
    public ProofOfDeliveryDTO uploadPod(UploadPodRequest request) {
        log.info("Uploading POD for notice ID: {}", request.getNoticeId());

        Notice notice = noticeRepository.findById(request.getNoticeId())
                .orElseThrow(() -> new ResourceNotFoundException("Notice", request.getNoticeId()));

        if (podRepository.existsByNoticeId(request.getNoticeId())) {
            throw new BusinessException("POD already exists for this notice");
        }

        ProofOfDelivery pod = noticeMapper.toPodEntity(request);
        pod.setPodNumber(generatePodNumber());
        pod.setVerificationStatus(PodVerificationStatus.PENDING);
        pod.setUploadedAt(LocalDateTime.now());
        pod.setUploadedBy(request.getUploadedBy());

        ProofOfDelivery savedPod = podRepository.save(pod);

        // Update notice status
        notice.setNoticeStatus(NoticeStatus.DELIVERED);
        notice.setDeliveredAt(request.getDeliveredAt());
        notice.setPodId(savedPod.getId());
        noticeRepository.save(notice);

        log.info("POD uploaded with ID: {}", savedPod.getId());

        return enrichWithDetails(noticeMapper.toPodDto(savedPod), notice);
    }

    @Override
    @Cacheable(value = CacheConstants.POD_CACHE, key = "#id")
    @Transactional(readOnly = true)
    public ProofOfDeliveryDTO getPodById(Long id) {
        ProofOfDelivery pod = podRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProofOfDelivery", id));
        return enrichWithDetails(noticeMapper.toPodDto(pod), null);
    }

    @Override
    @Transactional(readOnly = true)
    public ProofOfDeliveryDTO getPodByNumber(String podNumber) {
        ProofOfDelivery pod = podRepository.findByPodNumber(podNumber)
                .orElseThrow(() -> new ResourceNotFoundException("ProofOfDelivery", podNumber));
        return enrichWithDetails(noticeMapper.toPodDto(pod), null);
    }

    @Override
    @Cacheable(value = CacheConstants.POD_BY_NOTICE_CACHE, key = "#noticeId")
    @Transactional(readOnly = true)
    public ProofOfDeliveryDTO getPodByNoticeId(Long noticeId) {
        ProofOfDelivery pod = podRepository.findByNoticeId(noticeId)
                .orElseThrow(() -> new ResourceNotFoundException("ProofOfDelivery for notice", noticeId));
        return enrichWithDetails(noticeMapper.toPodDto(pod), null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProofOfDeliveryDTO> getPodsByVendor(Long vendorId) {
        return podRepository.findByVendorId(vendorId).stream()
                .map(noticeMapper::toPodDto)
                .map(dto -> enrichWithDetails(dto, null))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProofOfDeliveryDTO> getPodsByVerificationStatus(PodVerificationStatus status, Pageable pageable) {
        return podRepository.findByVerificationStatus(status, pageable)
                .map(noticeMapper::toPodDto)
                .map(dto -> enrichWithDetails(dto, null));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProofOfDeliveryDTO> getPodsByUploadDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return podRepository.findByUploadDateRange(startDate, endDate, pageable)
                .map(noticeMapper::toPodDto)
                .map(dto -> enrichWithDetails(dto, null));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProofOfDeliveryDTO> getPodsByDeliveryDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return podRepository.findByDeliveryDateRange(startDate, endDate, pageable)
                .map(noticeMapper::toPodDto)
                .map(dto -> enrichWithDetails(dto, null));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProofOfDeliveryDTO> getAllPods(Pageable pageable) {
        return podRepository.findAll(pageable)
                .map(noticeMapper::toPodDto)
                .map(dto -> enrichWithDetails(dto, null));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProofOfDeliveryDTO> getPendingVerifications() {
        return podRepository.findPendingVerifications().stream()
                .map(noticeMapper::toPodDto)
                .map(dto -> enrichWithDetails(dto, null))
                .toList();
    }

    @Override
    @CacheEvict(value = {CacheConstants.POD_CACHE, CacheConstants.POD_BY_NOTICE_CACHE}, allEntries = true)
    public ProofOfDeliveryDTO verifyPod(VerifyPodRequest request) {
        log.info("Verifying POD ID: {}", request.getPodId());

        ProofOfDelivery pod = podRepository.findById(request.getPodId())
                .orElseThrow(() -> new ResourceNotFoundException("ProofOfDelivery", request.getPodId()));

        if (pod.getVerificationStatus() != PodVerificationStatus.PENDING) {
            throw new BusinessException("POD is already verified or rejected");
        }

        pod.setVerificationStatus(request.getVerificationStatus());
        pod.setVerificationRemarks(request.getVerificationRemarks());
        pod.setVerifiedBy(request.getVerifiedBy());
        pod.setVerifiedAt(LocalDateTime.now());

        if (request.getVerificationStatus() == PodVerificationStatus.REJECTED) {
            pod.setRejectionReason(request.getRejectionReason());
        }

        ProofOfDelivery savedPod = podRepository.save(pod);
        log.info("POD verified: {} with status: {}", savedPod.getId(), savedPod.getVerificationStatus());

        return enrichWithDetails(noticeMapper.toPodDto(savedPod), null);
    }

    @Override
    @CacheEvict(value = {CacheConstants.POD_CACHE, CacheConstants.POD_BY_NOTICE_CACHE}, allEntries = true)
    public ProofOfDeliveryDTO approvePod(Long podId, String remarks, Long verifiedBy) {
        VerifyPodRequest request = VerifyPodRequest.builder()
                .podId(podId)
                .verificationStatus(PodVerificationStatus.VERIFIED)
                .verificationRemarks(remarks)
                .verifiedBy(verifiedBy)
                .build();
        return verifyPod(request);
    }

    @Override
    @CacheEvict(value = {CacheConstants.POD_CACHE, CacheConstants.POD_BY_NOTICE_CACHE}, allEntries = true)
    public ProofOfDeliveryDTO rejectPod(Long podId, String rejectionReason, Long verifiedBy) {
        VerifyPodRequest request = VerifyPodRequest.builder()
                .podId(podId)
                .verificationStatus(PodVerificationStatus.REJECTED)
                .rejectionReason(rejectionReason)
                .verifiedBy(verifiedBy)
                .build();
        return verifyPod(request);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countByVerificationStatus(PodVerificationStatus status) {
        return podRepository.countByVerificationStatus(status);
    }

    @Override
    @CacheEvict(value = {CacheConstants.POD_CACHE, CacheConstants.POD_BY_NOTICE_CACHE}, allEntries = true)
    public void deletePod(Long id) {
        if (!podRepository.existsById(id)) {
            throw new ResourceNotFoundException("ProofOfDelivery", id);
        }
        podRepository.deleteById(id);
        log.info("POD deleted: {}", id);
    }

    private String generatePodNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uniquePart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "POD-" + datePart + "-" + uniquePart;
    }

    private ProofOfDeliveryDTO enrichWithDetails(ProofOfDeliveryDTO dto, Notice notice) {
        // Enrich with notice number
        if (dto.getNoticeId() != null) {
            Notice n = notice;
            if (n == null) {
                n = noticeRepository.findById(dto.getNoticeId()).orElse(null);
            }
            if (n != null) {
                dto.setNoticeNumber(n.getNoticeNumber());
            }
        }

        // Enrich with vendor name
        if (dto.getVendorId() != null) {
            vendorRepository.findById(dto.getVendorId())
                    .ifPresent(vendor -> dto.setVendorName(vendor.getVendorName()));
        }

        return dto;
    }
}
