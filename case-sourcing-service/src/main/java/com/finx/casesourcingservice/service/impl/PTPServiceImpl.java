package com.finx.casesourcingservice.service.impl;

import com.finx.casesourcingservice.domain.dto.*;
import com.finx.casesourcingservice.domain.entity.Case;
import com.finx.casesourcingservice.domain.entity.PTPCommitment;
import com.finx.casesourcingservice.domain.enums.PTPStatus;
import com.finx.casesourcingservice.exception.BusinessException;
import com.finx.casesourcingservice.repository.CaseRepository;
import com.finx.casesourcingservice.repository.PTPCommitmentRepository;
import com.finx.casesourcingservice.service.PTPService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PTPServiceImpl implements PTPService {

    private final PTPCommitmentRepository ptpRepository;
    private final CaseRepository caseRepository;

    @Override
    @Transactional
    public PTPResponse capturePTP(CapturePTPRequest request) {
        log.info("Capturing PTP for case: {}, amount: {}, date: {}",
                request.getCaseId(), request.getPtpAmount(), request.getPtpDate());

        // Validate case exists
        Case caseEntity = caseRepository.findById(request.getCaseId())
                .orElseThrow(() -> new BusinessException("Case not found: " + request.getCaseId()));

        // Create PTP commitment
        PTPCommitment ptp = PTPCommitment.builder()
                .caseId(request.getCaseId())
                .userId(request.getUserId())
                .ptpDate(request.getPtpDate())
                .ptpAmount(request.getPtpAmount())
                .notes(request.getNotes())
                .callDisposition(request.getCallDisposition())
                .followUpDate(request.getFollowUpDate())
                .ptpStatus(PTPStatus.PENDING)
                .createdBy(request.getUserId())
                .build();

        PTPCommitment saved = ptpRepository.save(ptp);

        // Update case with PTP information
        caseEntity.setPtpDate(request.getPtpDate());
        caseEntity.setPtpAmount(request.getPtpAmount());
        caseEntity.setPtpStatus("PENDING");
        caseEntity.setCaseStatus("PTP");
        caseRepository.save(caseEntity);

        log.info("PTP captured successfully with ID: {}", saved.getId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PTPCaseDTO> getPTPsDue(LocalDate dueDate, Long userId) {
        log.info("Fetching PTPs due on: {} for user: {}", dueDate, userId);

        List<PTPCommitment> ptps;
        if (userId != null) {
            ptps = ptpRepository.findPTPsDueForUser(dueDate, userId);
        } else {
            ptps = ptpRepository.findPTPsDueOnDate(dueDate);
        }

        return ptps.stream()
                .map(this::mapToPTPCaseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PTPCaseDTO> getBrokenPTPs(Long userId) {
        log.info("Fetching broken PTPs for user: {}", userId);

        LocalDate today = LocalDate.now();
        List<PTPCommitment> ptps;

        if (userId != null) {
            ptps = ptpRepository.findBrokenPTPsForUser(today, userId);
        } else {
            ptps = ptpRepository.findBrokenPTPs(today);
        }

        return ptps.stream()
                .map(this::mapToPTPCaseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PTPResponse getPTPById(Long ptpId) {
        log.info("Fetching PTP by ID: {}", ptpId);

        PTPCommitment ptp = ptpRepository.findById(ptpId)
                .orElseThrow(() -> new BusinessException("PTP not found: " + ptpId));

        return mapToResponse(ptp);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PTPResponse> getPTPsByCase(Long caseId) {
        log.info("Fetching all PTPs for case: {}", caseId);

        List<PTPCommitment> ptps = ptpRepository.findByCaseIdOrderByCreatedAtDesc(caseId);

        return ptps.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PTPResponse updatePTPStatus(Long ptpId, UpdatePTPRequest request) {
        log.info("Updating PTP status: {} to {}", ptpId, request.getPtpStatus());

        PTPCommitment ptp = ptpRepository.findById(ptpId)
                .orElseThrow(() -> new BusinessException("PTP not found: " + ptpId));

        // Update PTP fields
        ptp.setPtpStatus(request.getPtpStatus());
        ptp.setPaymentReceivedAmount(request.getPaymentReceivedAmount());
        ptp.setPaymentReceivedDate(request.getPaymentReceivedDate());
        ptp.setBrokenReason(request.getBrokenReason());
        ptp.setFollowUpDate(request.getFollowUpDate());

        if (request.getNotes() != null) {
            ptp.setNotes(ptp.getNotes() != null ?
                    ptp.getNotes() + "\n" + request.getNotes() : request.getNotes());
        }

        PTPCommitment updated = ptpRepository.save(ptp);

        // Update case status
        Case caseEntity = caseRepository.findById(ptp.getCaseId())
                .orElseThrow(() -> new BusinessException("Case not found: " + ptp.getCaseId()));

        if (request.getPtpStatus() == PTPStatus.KEPT || request.getPtpStatus() == PTPStatus.PARTIAL) {
            caseEntity.setPtpStatus("KEPT");
        } else if (request.getPtpStatus() == PTPStatus.BROKEN) {
            caseEntity.setPtpStatus("BROKEN");
        }

        caseRepository.save(caseEntity);

        log.info("PTP status updated successfully");

        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public PTPStatsDTO getPTPStats(Long userId) {
        log.info("Fetching PTP statistics for user: {}", userId);

        // Get all PTPs for user
        List<PTPCommitment> allPTPs = ptpRepository.findAll().stream()
                .filter(p -> userId == null || p.getUserId().equals(userId))
                .collect(Collectors.toList());

        Long pending = allPTPs.stream().filter(p -> p.getPtpStatus() == PTPStatus.PENDING).count();
        Long kept = allPTPs.stream().filter(p -> p.getPtpStatus() == PTPStatus.KEPT).count();
        Long broken = allPTPs.stream().filter(p -> p.getPtpStatus() == PTPStatus.BROKEN).count();
        Long renewed = allPTPs.stream().filter(p -> p.getPtpStatus() == PTPStatus.RENEWED).count();
        Long partial = allPTPs.stream().filter(p -> p.getPtpStatus() == PTPStatus.PARTIAL).count();

        BigDecimal totalAmount = allPTPs.stream()
                .map(PTPCommitment::getPtpAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal collectedAmount = allPTPs.stream()
                .filter(p -> p.getPaymentReceivedAmount() != null)
                .map(PTPCommitment::getPaymentReceivedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendingAmount = allPTPs.stream()
                .filter(p -> p.getPtpStatus() == PTPStatus.PENDING)
                .map(PTPCommitment::getPtpAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate performance metrics
        Double keepRate = 0.0;
        Double brokenRate = 0.0;
        long totalResolved = kept + broken + partial;

        if (totalResolved > 0) {
            keepRate = ((kept + partial) * 100.0) / totalResolved;
            brokenRate = (broken * 100.0) / totalResolved;
        }

        // Today's summary
        LocalDate today = LocalDate.now();
        Long ptpsDueToday = allPTPs.stream()
                .filter(p -> p.getPtpDate().equals(today) && p.getPtpStatus() == PTPStatus.PENDING)
                .count();

        Long ptpsOverdueToday = allPTPs.stream()
                .filter(p -> p.getPtpDate().isBefore(today) && p.getPtpStatus() == PTPStatus.PENDING)
                .count();

        BigDecimal amountDueToday = allPTPs.stream()
                .filter(p -> p.getPtpDate().equals(today) && p.getPtpStatus() == PTPStatus.PENDING)
                .map(PTPCommitment::getPtpAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return PTPStatsDTO.builder()
                .userId(userId)
                .totalPTPs((long) allPTPs.size())
                .pendingPTPs(pending)
                .keptPTPs(kept)
                .brokenPTPs(broken)
                .renewedPTPs(renewed)
                .partialPTPs(partial)
                .totalPTPAmount(totalAmount)
                .collectedAmount(collectedAmount)
                .pendingAmount(pendingAmount)
                .keepRate(Math.round(keepRate * 100.0) / 100.0)
                .brokenRate(Math.round(brokenRate * 100.0) / 100.0)
                .ptpsDueToday(ptpsDueToday)
                .ptpsOverdueToday(ptpsOverdueToday)
                .amountDueToday(amountDueToday)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PTPResponse> getPTPsByStatus(String status, Pageable pageable) {
        log.info("Fetching PTPs by status: {}", status);

        PTPStatus ptpStatus = PTPStatus.valueOf(status.toUpperCase());
        Page<PTPCommitment> ptps = ptpRepository.findByPtpStatus(ptpStatus, pageable);

        return ptps.map(this::mapToResponse);
    }

    @Override
    @Transactional
    public Integer processOverduePTPs() {
        log.info("Processing overdue PTPs");

        LocalDate today = LocalDate.now();
        List<PTPCommitment> brokenPTPs = ptpRepository.findBrokenPTPs(today);

        for (PTPCommitment ptp : brokenPTPs) {
            ptp.setPtpStatus(PTPStatus.BROKEN);
            ptp.setBrokenReason("Auto-marked as broken: Payment not received by due date");
            ptpRepository.save(ptp);

            // Update case status
            caseRepository.findById(ptp.getCaseId()).ifPresent(caseEntity -> {
                caseEntity.setPtpStatus("BROKEN");
                caseRepository.save(caseEntity);
            });
        }

        log.info("Processed {} overdue PTPs", brokenPTPs.size());
        return brokenPTPs.size();
    }

    @Override
    @Transactional
    public Integer sendPTPReminders() {
        log.info("Sending PTP reminders");

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<PTPCommitment> ptpsNeedingReminder = ptpRepository.findPTPsRequiringReminder(tomorrow);

        int remindersSent = 0;
        for (PTPCommitment ptp : ptpsNeedingReminder) {
            // TODO: Integrate with communication service to send SMS/WhatsApp reminder
            // For now, just mark as reminder sent
            ptp.setReminderSent(true);
            ptp.setReminderSentAt(LocalDateTime.now());
            ptpRepository.save(ptp);
            remindersSent++;
        }

        log.info("Sent {} PTP reminders", remindersSent);
        return remindersSent;
    }

    private PTPResponse mapToResponse(PTPCommitment ptp) {
        LocalDate today = LocalDate.now();
        long daysSince = ChronoUnit.DAYS.between(ptp.getCommitmentDate().toLocalDate(), today);
        long daysUntil = ChronoUnit.DAYS.between(today, ptp.getPtpDate());
        boolean isOverdue = ptp.getPtpDate().isBefore(today) && ptp.getPtpStatus() == PTPStatus.PENDING;

        return PTPResponse.builder()
                .id(ptp.getId())
                .caseId(ptp.getCaseId())
                .userId(ptp.getUserId())
                .ptpDate(ptp.getPtpDate())
                .ptpAmount(ptp.getPtpAmount())
                .commitmentDate(ptp.getCommitmentDate())
                .ptpStatus(ptp.getPtpStatus())
                .paymentReceivedAmount(ptp.getPaymentReceivedAmount())
                .paymentReceivedDate(ptp.getPaymentReceivedDate())
                .brokenReason(ptp.getBrokenReason())
                .notes(ptp.getNotes())
                .reminderSent(ptp.getReminderSent())
                .reminderSentAt(ptp.getReminderSentAt())
                .followUpDate(ptp.getFollowUpDate())
                .followUpCompleted(ptp.getFollowUpCompleted())
                .callDisposition(ptp.getCallDisposition())
                .createdAt(ptp.getCreatedAt())
                .daysSinceCommitment(daysSince)
                .daysUntilDue(daysUntil)
                .isOverdue(isOverdue)
                .build();
    }

    private PTPCaseDTO mapToPTPCaseDTO(PTPCommitment ptp) {
        Case caseEntity = caseRepository.findById(ptp.getCaseId())
                .orElseThrow(() -> new BusinessException("Case not found: " + ptp.getCaseId()));

        LocalDate today = LocalDate.now();
        long daysOverdue = ptp.getPtpDate().isBefore(today) ?
                ChronoUnit.DAYS.between(ptp.getPtpDate(), today) : 0;

        return PTPCaseDTO.builder()
                .caseId(caseEntity.getId())
                .caseNumber(caseEntity.getCaseNumber())
                .loanAccountNumber(caseEntity.getLoan() != null ? caseEntity.getLoan().getLoanAccountNumber() : null)
                .externalCaseId(caseEntity.getExternalCaseId())
                .caseStatus(caseEntity.getCaseStatus())
                .customerName(caseEntity.getLoan() != null && caseEntity.getLoan().getPrimaryCustomer() != null ?
                        caseEntity.getLoan().getPrimaryCustomer().getFullName() : null)
                .mobileNumber(caseEntity.getLoan() != null && caseEntity.getLoan().getPrimaryCustomer() != null ?
                        caseEntity.getLoan().getPrimaryCustomer().getMobileNumber() : null)
                .totalOutstanding(caseEntity.getLoan() != null ? caseEntity.getLoan().getTotalOutstanding() : null)
                .dpd(caseEntity.getLoan() != null ? caseEntity.getLoan().getDpd() : null)
                .bucket(caseEntity.getLoan() != null ? caseEntity.getLoan().getBucket() : null)
                .productType(caseEntity.getLoan() != null ? caseEntity.getLoan().getProductType() : null)
                .ptpId(ptp.getId())
                .ptpDate(ptp.getPtpDate())
                .ptpAmount(ptp.getPtpAmount())
                .ptpStatus(ptp.getPtpStatus().name())
                .commitmentDate(ptp.getCommitmentDate().toLocalDate())
                .ptpNotes(ptp.getNotes())
                .daysOverdue(daysOverdue)
                .reminderSent(ptp.getReminderSent())
                .userId(ptp.getUserId())
                .build();
    }
}
