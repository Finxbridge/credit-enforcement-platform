package com.finx.collectionsservice.service.impl;

import com.finx.collectionsservice.domain.dto.CreateOTSRequest;
import com.finx.collectionsservice.domain.dto.OTSCaseSearchDTO;
import com.finx.collectionsservice.domain.dto.OTSRequestDTO;
import com.finx.collectionsservice.domain.entity.OTSRequest;
import com.finx.collectionsservice.domain.enums.OTSStatus;
import com.finx.collectionsservice.exception.BusinessException;
import com.finx.collectionsservice.exception.ResourceNotFoundException;
import com.finx.collectionsservice.mapper.CollectionsMapper;
import com.finx.collectionsservice.repository.OTSRequestRepository;
import com.finx.collectionsservice.service.CaseEventService;
import com.finx.collectionsservice.service.CaseSearchService;
import com.finx.collectionsservice.service.OTSService;
import com.finx.collectionsservice.service.SettlementLetterService;
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
    private final CaseSearchService caseSearchService;
    private final SettlementLetterService settlementLetterService;

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

        // Fetch case details to populate OTS
        OTSCaseSearchDTO caseDetails = caseSearchService.getCaseDetails(request.getCaseId());

        OTSRequest ots = mapper.toEntity(request);
        ots.setOtsNumber("OTS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        ots.setOtsStatus(OTSStatus.PENDING_APPROVAL);
        ots.setCurrentApprovalLevel(0);
        ots.setMaxApprovalLevel(1); // Only 1 approval needed
        ots.setIntentCapturedAt(LocalDateTime.now());
        ots.setIntentCapturedBy(userId);
        ots.setIntentNotes(request.getIntentNotes());
        ots.setCreatedBy(userId);

        // Populate case details from CaseSearchService
        if (caseDetails != null) {
            ots.setLoanAccountNumber(caseDetails.getLoanAccountNumber());
            ots.setCustomerName(caseDetails.getCustomerName());
        }

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

        OTSRequestDTO dto = mapper.toDto(saved);
        // Also set caseNumber in the DTO
        if (caseDetails != null) {
            dto.setCaseNumber(caseDetails.getCaseNumber());
        }
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public OTSRequestDTO getOTSById(Long otsId) {
        log.info("Fetching OTS by ID: {}", otsId);

        OTSRequest ots = otsRepository.findById(otsId)
                .orElseThrow(() -> new ResourceNotFoundException("OTS", otsId));

        return enrichOTSWithCaseDetails(mapper.toDto(ots), ots.getCaseId());
    }

    @Override
    @Transactional(readOnly = true)
    public OTSRequestDTO getOTSByNumber(String otsNumber) {
        log.info("Fetching OTS by number: {}", otsNumber);

        OTSRequest ots = otsRepository.findByOtsNumber(otsNumber)
                .orElseThrow(() -> new ResourceNotFoundException("OTS", otsNumber));

        return enrichOTSWithCaseDetails(mapper.toDto(ots), ots.getCaseId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OTSRequestDTO> getOTSByCase(Long caseId) {
        log.info("Fetching OTS requests for case: {}", caseId);

        return otsRepository.findByCaseIdOrderByCreatedAtDesc(caseId)
                .stream()
                .map(ots -> enrichOTSWithCaseDetails(mapper.toDto(ots), ots.getCaseId()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OTSRequestDTO> getOTSByStatus(OTSStatus status, Pageable pageable) {
        log.info("Fetching OTS by status: {}", status);

        return otsRepository.findByOtsStatus(status, pageable)
                .map(ots -> enrichOTSWithCaseDetails(mapper.toDto(ots), ots.getCaseId()));
    }

    /**
     * Enriches OTS DTO with case details if not already present
     */
    private OTSRequestDTO enrichOTSWithCaseDetails(OTSRequestDTO dto, Long caseId) {
        if (dto.getCaseNumber() == null || dto.getLoanAccountNumber() == null || dto.getCustomerName() == null) {
            try {
                OTSCaseSearchDTO caseDetails = caseSearchService.getCaseDetails(caseId);
                if (caseDetails != null) {
                    if (dto.getCaseNumber() == null) {
                        dto.setCaseNumber(caseDetails.getCaseNumber());
                    }
                    if (dto.getLoanAccountNumber() == null) {
                        dto.setLoanAccountNumber(caseDetails.getLoanAccountNumber());
                    }
                    if (dto.getCustomerName() == null) {
                        dto.setCustomerName(caseDetails.getCustomerName());
                    }
                }
            } catch (Exception e) {
                log.warn("Could not fetch case details for case {}: {}", caseId, e.getMessage());
            }
        }
        return dto;
    }

    @Override
    @Transactional
    public OTSRequestDTO approveOTS(Long otsId, Long approverId, String comments) {
        log.info("Approving OTS {} by user {}", otsId, approverId);

        OTSRequest ots = otsRepository.findById(otsId)
                .orElseThrow(() -> new ResourceNotFoundException("OTS", otsId));

        if (ots.getOtsStatus() != OTSStatus.PENDING_APPROVAL) {
            throw new BusinessException("OTS is not in pending approval status");
        }

        ots.setCurrentApprovalLevel(ots.getCurrentApprovalLevel() + 1);
        ots.setOtsStatus(OTSStatus.APPROVED);
        ots.setUpdatedBy(approverId);

        OTSRequest updated = otsRepository.save(ots);
        log.info("OTS {} approved", otsId);

        // Log case event for OTS approval
        caseEventService.logOtsApproved(
                updated.getCaseId(),
                updated.getLoanAccountNumber(),
                updated.getId(),
                updated.getProposedSettlement(),
                approverId,
                null);

        // Auto-generate settlement letter and upload to DMS when approved
        try {
            log.info("Auto-generating settlement letter for approved OTS {}", otsId);
            settlementLetterService.generateLetter(otsId, 1L, approverId);
            log.info("Settlement letter generated successfully for OTS {}", otsId);
        } catch (Exception e) {
            log.error("Failed to auto-generate settlement letter for OTS {}: {}", otsId, e.getMessage());
            // Don't fail the approval if letter generation fails
        }

        return enrichOTSWithCaseDetails(mapper.toDto(updated), updated.getCaseId());
    }

    @Override
    @Transactional
    public OTSRequestDTO rejectOTS(Long otsId, Long approverId, String reason) {
        log.info("Rejecting OTS {} by user {}", otsId, approverId);

        OTSRequest ots = otsRepository.findById(otsId)
                .orElseThrow(() -> new ResourceNotFoundException("OTS", otsId));

        if (ots.getOtsStatus() != OTSStatus.PENDING_APPROVAL) {
            throw new BusinessException("Can only reject OTS in pending approval status");
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

        return enrichOTSWithCaseDetails(mapper.toDto(updated), updated.getCaseId());
    }

    @Override
    @Transactional
    public OTSRequestDTO cancelOTS(Long otsId, Long userId, String reason) {
        log.info("Cancelling OTS {} by user {}", otsId, userId);

        OTSRequest ots = otsRepository.findById(otsId)
                .orElseThrow(() -> new ResourceNotFoundException("OTS", otsId));

        // Can only cancel PENDING_APPROVAL or APPROVED OTS
        if (ots.getOtsStatus() != OTSStatus.PENDING_APPROVAL && ots.getOtsStatus() != OTSStatus.APPROVED) {
            throw new BusinessException("Can only cancel OTS in pending approval or approved status");
        }

        ots.setOtsStatus(OTSStatus.REJECTED);
        ots.setCancelledAt(LocalDateTime.now());
        ots.setCancelledBy(userId);
        ots.setCancellationReason(reason);
        ots.setUpdatedBy(userId);

        OTSRequest updated = otsRepository.save(ots);
        log.info("OTS {} cancelled", otsId);

        return enrichOTSWithCaseDetails(mapper.toDto(updated), updated.getCaseId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OTSRequestDTO> getPendingApprovals(Pageable pageable) {
        log.info("Fetching pending OTS approvals");

        return otsRepository.findPendingApprovals(pageable)
                .map(ots -> enrichOTSWithCaseDetails(mapper.toDto(ots), ots.getCaseId()));
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
            log.info("OTS {} marked as expired - payment deadline was {}", ots.getOtsNumber(), ots.getPaymentDeadline());
        }

        log.info("Processed {} expired OTS requests", expiredRequests.size());
        return expiredRequests.size();
    }
}
