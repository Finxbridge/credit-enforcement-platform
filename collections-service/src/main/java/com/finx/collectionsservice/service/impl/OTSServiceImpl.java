package com.finx.collectionsservice.service.impl;

import com.finx.collectionsservice.domain.dto.CreateOTSRequest;
import com.finx.collectionsservice.domain.dto.OTSRequestDTO;
import com.finx.collectionsservice.domain.entity.OTSRequest;
import com.finx.collectionsservice.domain.enums.OTSStatus;
import com.finx.collectionsservice.exception.BusinessException;
import com.finx.collectionsservice.exception.ResourceNotFoundException;
import com.finx.collectionsservice.mapper.CollectionsMapper;
import com.finx.collectionsservice.repository.OTSRequestRepository;
import com.finx.collectionsservice.service.CaseEventService;
import com.finx.collectionsservice.service.OTSService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OTSServiceImpl implements OTSService {

    private final OTSRequestRepository otsRepository;
    private final CollectionsMapper mapper;
    private final CaseEventService caseEventService;

    @Override
    @Transactional
    public OTSRequestDTO createOTSRequest(CreateOTSRequest request, Long userId) {
        log.info("Creating OTS request for case {} with settlement amount {}",
                request.getCaseId(), request.getProposedSettlement());

        // Check for existing active OTS
        Long activeCount = otsRepository.countActiveOTSByCaseId(request.getCaseId());
        if (activeCount > 0) {
            throw new BusinessException("An active OTS request already exists for this case");
        }

        OTSRequest ots = mapper.toEntity(request);
        ots.setOtsNumber("OTS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        ots.setOtsStatus(OTSStatus.INTENT_CAPTURED);
        ots.setIntentCapturedAt(LocalDateTime.now());
        ots.setIntentCapturedBy(userId);
        ots.setIntentNotes(request.getIntentNotes());
        ots.setCreatedBy(userId);

        OTSRequest saved = otsRepository.save(ots);
        log.info("OTS created with number: {}", saved.getOtsNumber());

        // Log case event for OTS creation
        caseEventService.logOtsCreated(
                saved.getCaseId(),
                saved.getLoanAccountNumber(),
                saved.getId(),
                saved.getProposedSettlement(),
                saved.getOriginalOutstanding(),
                userId,
                null);

        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OTSRequestDTO getOTSById(Long otsId) {
        log.info("Fetching OTS by ID: {}", otsId);

        OTSRequest ots = otsRepository.findById(otsId)
                .orElseThrow(() -> new ResourceNotFoundException("OTS", otsId));

        return mapper.toDto(ots);
    }

    @Override
    @Transactional(readOnly = true)
    public OTSRequestDTO getOTSByNumber(String otsNumber) {
        log.info("Fetching OTS by number: {}", otsNumber);

        OTSRequest ots = otsRepository.findByOtsNumber(otsNumber)
                .orElseThrow(() -> new ResourceNotFoundException("OTS", otsNumber));

        return mapper.toDto(ots);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OTSRequestDTO> getOTSByCase(Long caseId) {
        log.info("Fetching OTS requests for case: {}", caseId);

        return otsRepository.findByCaseIdOrderByCreatedAtDesc(caseId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OTSRequestDTO> getOTSByStatus(OTSStatus status, Pageable pageable) {
        log.info("Fetching OTS by status: {}", status);

        return otsRepository.findByOtsStatus(status, pageable)
                .map(mapper::toDto);
    }

    @Override
    @Transactional
    public OTSRequestDTO approveOTS(Long otsId, Long approverId, String comments) {
        log.info("Approving OTS {} by user {}", otsId, approverId);

        OTSRequest ots = otsRepository.findById(otsId)
                .orElseThrow(() -> new ResourceNotFoundException("OTS", otsId));

        if (ots.getOtsStatus() != OTSStatus.PENDING_APPROVAL && ots.getOtsStatus() != OTSStatus.INTENT_CAPTURED) {
            throw new BusinessException("OTS is not in pending approval status");
        }

        ots.setCurrentApprovalLevel(ots.getCurrentApprovalLevel() + 1);

        if (ots.getCurrentApprovalLevel() >= ots.getMaxApprovalLevel()) {
            ots.setOtsStatus(OTSStatus.APPROVED);
        } else {
            ots.setOtsStatus(OTSStatus.PENDING_APPROVAL);
        }

        ots.setUpdatedBy(approverId);

        OTSRequest updated = otsRepository.save(ots);
        log.info("OTS {} approval level updated to {}", otsId, updated.getCurrentApprovalLevel());

        // Log case event for OTS approval if fully approved
        if (updated.getOtsStatus() == OTSStatus.APPROVED) {
            caseEventService.logOtsApproved(
                    updated.getCaseId(),
                    updated.getLoanAccountNumber(),
                    updated.getId(),
                    updated.getProposedSettlement(),
                    approverId,
                    null);
        }

        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public OTSRequestDTO rejectOTS(Long otsId, Long approverId, String reason) {
        log.info("Rejecting OTS {} by user {}", otsId, approverId);

        OTSRequest ots = otsRepository.findById(otsId)
                .orElseThrow(() -> new ResourceNotFoundException("OTS", otsId));

        if (ots.getOtsStatus() == OTSStatus.SETTLED || ots.getOtsStatus() == OTSStatus.CANCELLED) {
            throw new BusinessException("Cannot reject OTS in current status");
        }

        ots.setOtsStatus(OTSStatus.REJECTED);
        ots.setUpdatedBy(approverId);

        OTSRequest updated = otsRepository.save(ots);
        log.info("OTS {} rejected", otsId);

        // Log case event for OTS rejection
        caseEventService.logOtsRejected(
                updated.getCaseId(),
                updated.getLoanAccountNumber(),
                updated.getId(),
                updated.getProposedSettlement(),
                reason,
                approverId,
                null);

        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public OTSRequestDTO cancelOTS(Long otsId, Long userId, String reason) {
        log.info("Cancelling OTS {} by user {}", otsId, userId);

        OTSRequest ots = otsRepository.findById(otsId)
                .orElseThrow(() -> new ResourceNotFoundException("OTS", otsId));

        if (ots.getOtsStatus() == OTSStatus.SETTLED) {
            throw new BusinessException("Cannot cancel a settled OTS");
        }

        ots.setOtsStatus(OTSStatus.CANCELLED);
        ots.setCancelledAt(LocalDateTime.now());
        ots.setCancelledBy(userId);
        ots.setCancellationReason(reason);
        ots.setUpdatedBy(userId);

        OTSRequest updated = otsRepository.save(ots);
        log.info("OTS {} cancelled", otsId);

        return mapper.toDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OTSRequestDTO> getPendingApprovals(Pageable pageable) {
        log.info("Fetching pending OTS approvals");

        return otsRepository.findPendingApprovals(pageable)
                .map(mapper::toDto);
    }

    @Override
    @Transactional
    public Integer processExpiredOTS() {
        log.info("Processing expired OTS requests");

        LocalDate today = LocalDate.now();
        List<OTSRequest> expiredRequests = otsRepository.findExpiredOTSRequests(today);

        for (OTSRequest ots : expiredRequests) {
            ots.setOtsStatus(OTSStatus.EXPIRED);
            ots.setExpiredAt(LocalDateTime.now());
            otsRepository.save(ots);
        }

        log.info("Processed {} expired OTS requests", expiredRequests.size());
        return expiredRequests.size();
    }
}
