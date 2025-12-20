package com.finx.collectionsservice.service.impl;

import com.finx.collectionsservice.domain.dto.*;
import com.finx.collectionsservice.domain.entity.Repayment;
import com.finx.collectionsservice.domain.enums.PaymentMode;
import com.finx.collectionsservice.domain.enums.RepaymentStatus;
import com.finx.collectionsservice.exception.BusinessException;
import com.finx.collectionsservice.exception.ResourceNotFoundException;
import com.finx.collectionsservice.mapper.CollectionsMapper;
import com.finx.collectionsservice.repository.RepaymentRepository;
import com.finx.collectionsservice.service.ReceiptPdfService;
import com.finx.collectionsservice.service.RepaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepaymentServiceImpl implements RepaymentService {

    private final RepaymentRepository repaymentRepository;
    private final CollectionsMapper mapper;
    private final ReceiptPdfService receiptPdfService;

    // ==================== Core Repayment APIs ====================

    @Override
    @Transactional
    public RepaymentDTO createRepayment(CreateRepaymentRequest request, Long userId) {
        log.info("Creating repayment for case {} with amount {}", request.getCaseId(), request.getPaymentAmount());

        Repayment repayment = mapper.toEntity(request);
        repayment.setRepaymentNumber("RPY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        repayment.setCreatedBy(userId);

        // Auto-approve online payments, keep CASH and CHEQUE as PENDING for manual approval
        PaymentMode paymentMode = request.getPaymentMode();
        if (paymentMode == PaymentMode.CASH || paymentMode == PaymentMode.CHEQUE) {
            // Manual payments need approval
            repayment.setApprovalStatus(RepaymentStatus.PENDING);
            repayment.setCurrentApprovalLevel(1);

            // Set deposit SLA (e.g., 24 hours for cash/cheque)
            repayment.setDepositRequiredBy(LocalDateTime.now().plusHours(24));
            repayment.setDepositSlaStatus("PENDING");

            log.info("Manual payment ({}), set to PENDING for approval", paymentMode);
        } else {
            // Online payments (UPI, CARD, NEFT, RTGS, DIGITAL, etc.) are auto-approved
            repayment.setApprovalStatus(RepaymentStatus.APPROVED);
            repayment.setCurrentApprovalLevel(0);
            repayment.setApprovedBy(userId);
            repayment.setApprovedAt(LocalDateTime.now());

            log.info("Online payment ({}), auto-approved", paymentMode);
        }

        if (request.getOtsId() != null) {
            repayment.setOtsId(request.getOtsId());
            repayment.setIsOtsPayment(true);
        }

        Repayment saved = repaymentRepository.save(repayment);
        log.info("Repayment created with number: {} - Status: {}",
            saved.getRepaymentNumber(), saved.getApprovalStatus());

        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RepaymentDTO getRepayment(Long repaymentId) {
        log.info("Fetching repayment by ID: {}", repaymentId);

        Repayment repayment = repaymentRepository.findById(repaymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Repayment", repaymentId));

        return mapper.toDto(repayment);
    }

    @Override
    @Transactional(readOnly = true)
    public RepaymentDTO getRepaymentByNumber(String repaymentNumber) {
        log.info("Fetching repayment by number: {}", repaymentNumber);

        Repayment repayment = repaymentRepository.findByRepaymentNumber(repaymentNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Repayment", repaymentNumber));

        return mapper.toDto(repayment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepaymentDTO> getCaseRepayments(Long caseId) {
        log.info("Fetching repayments for case: {}", caseId);

        return repaymentRepository.findByCaseIdOrderByPaymentDateDesc(caseId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RepaymentDTO> getRepaymentsByStatus(RepaymentStatus status, Pageable pageable) {
        log.info("Fetching repayments by status: {}", status);

        return repaymentRepository.findByApprovalStatus(status, pageable)
                .map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RepaymentDTO> searchRepayments(String searchTerm, RepaymentStatus status,
                                                LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        log.info("Searching repayments - term: {}, status: {}, from: {}, to: {}",
                searchTerm, status, fromDate, toDate);

        // Use repository search method
        return repaymentRepository.searchRepayments(searchTerm, status, fromDate, toDate, pageable)
                .map(mapper::toDto);
    }

    // ==================== Approval APIs (Maker-Checker) ====================

    @Override
    @Transactional
    public RepaymentDTO approveRepayment(Long repaymentId, Long approverId, String comments) {
        log.info("Approving repayment {} by user {}", repaymentId, approverId);

        Repayment repayment = repaymentRepository.findById(repaymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Repayment", repaymentId));

        if (repayment.getApprovalStatus() != RepaymentStatus.PENDING) {
            throw new BusinessException("Repayment is not in pending status");
        }

        repayment.setApprovalStatus(RepaymentStatus.APPROVED);
        repayment.setApprovedBy(approverId);
        repayment.setApprovedAt(LocalDateTime.now());
        repayment.setUpdatedBy(approverId);

        Repayment updated = repaymentRepository.save(repayment);
        log.info("Repayment {} approved successfully", repaymentId);

        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public RepaymentDTO rejectRepayment(Long repaymentId, Long approverId, String reason) {
        log.info("Rejecting repayment {} by user {}", repaymentId, approverId);

        Repayment repayment = repaymentRepository.findById(repaymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Repayment", repaymentId));

        if (repayment.getApprovalStatus() != RepaymentStatus.PENDING) {
            throw new BusinessException("Repayment is not in pending status");
        }

        repayment.setApprovalStatus(RepaymentStatus.REJECTED);
        repayment.setApprovedBy(approverId);
        repayment.setApprovedAt(LocalDateTime.now());
        repayment.setRejectionReason(reason);
        repayment.setUpdatedBy(approverId);

        Repayment updated = repaymentRepository.save(repayment);
        log.info("Repayment {} rejected", repaymentId);

        return mapper.toDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RepaymentDTO> getPendingApprovals(Pageable pageable) {
        log.info("Fetching pending repayment approvals");

        return repaymentRepository.findPendingApprovals(pageable)
                .map(mapper::toDto);
    }

    // ==================== Dashboard APIs ====================

    @Override
    @Transactional(readOnly = true)
    public RepaymentDashboardDTO getDashboardStats() {
        log.info("Fetching repayment dashboard statistics");

        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);

        // Get today's stats
        List<Repayment> todayRepayments = repaymentRepository.findByPaymentDate(today);
        long todayPending = todayRepayments.stream()
                .filter(r -> r.getApprovalStatus() == RepaymentStatus.PENDING).count();
        long todayApproved = todayRepayments.stream()
                .filter(r -> r.getApprovalStatus() == RepaymentStatus.APPROVED).count();
        long todayRejected = todayRepayments.stream()
                .filter(r -> r.getApprovalStatus() == RepaymentStatus.REJECTED).count();
        BigDecimal todayAmount = todayRepayments.stream()
                .map(Repayment::getPaymentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get month stats
        List<Repayment> monthRepayments = repaymentRepository.findByPaymentDateBetween(monthStart, today);
        BigDecimal monthAmount = monthRepayments.stream()
                .filter(r -> r.getApprovalStatus() == RepaymentStatus.APPROVED)
                .map(Repayment::getPaymentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Payment mode breakdown
        Map<String, Long> modeCount = todayRepayments.stream()
                .filter(r -> r.getPaymentMode() != null)
                .collect(Collectors.groupingBy(r -> r.getPaymentMode().name(), Collectors.counting()));

        Map<String, BigDecimal> modeAmount = todayRepayments.stream()
                .filter(r -> r.getPaymentMode() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getPaymentMode().name(),
                        Collectors.reducing(BigDecimal.ZERO, Repayment::getPaymentAmount, BigDecimal::add)
                ));

        // Pending counts
        long pendingApprovalCount = repaymentRepository.countByApprovalStatus(RepaymentStatus.PENDING);
        long slaBreachedCount = repaymentRepository.countSlaBreached();
        long pendingReconciliation = repaymentRepository.countByIsReconciledFalse();

        return RepaymentDashboardDTO.builder()
                .todayTotalCount((long) todayRepayments.size())
                .todayTotalAmount(todayAmount)
                .todayPendingCount(todayPending)
                .todayApprovedCount(todayApproved)
                .todayRejectedCount(todayRejected)
                .monthTotalCount((long) monthRepayments.size())
                .monthTotalAmount(monthAmount)
                .paymentModeCount(modeCount)
                .paymentModeAmount(modeAmount)
                .pendingApprovalCount(pendingApprovalCount)
                .slaBreachedCount(slaBreachedCount)
                .pendingReconciliationCount(pendingReconciliation)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SlaDashboardDTO getSlaDashboardStats() {
        log.info("Fetching SLA dashboard statistics");

        long totalRepayments = repaymentRepository.count();
        long breachedCount = repaymentRepository.countSlaBreached();
        long withinSlaCount = totalRepayments - breachedCount;

        double compliancePercentage = totalRepayments > 0
                ? ((double) withinSlaCount / totalRepayments) * 100
                : 100.0;

        // Get breach details
        List<Repayment> breachedRepayments = repaymentRepository.findSlaBreachedRepayments();

        // Categorize by severity
        long criticalBreaches = breachedRepayments.stream()
                .filter(r -> r.getDepositSlaBreachHours() != null && r.getDepositSlaBreachHours() > 48)
                .count();
        long majorBreaches = breachedRepayments.stream()
                .filter(r -> r.getDepositSlaBreachHours() != null &&
                        r.getDepositSlaBreachHours() > 24 && r.getDepositSlaBreachHours() <= 48)
                .count();
        long minorBreaches = breachedRepayments.stream()
                .filter(r -> r.getDepositSlaBreachHours() != null && r.getDepositSlaBreachHours() <= 24)
                .count();

        // Top breached cases
        List<SlaDashboardDTO.SlaBreachSummary> topBreaches = breachedRepayments.stream()
                .limit(10)
                .map(r -> SlaDashboardDTO.SlaBreachSummary.builder()
                        .repaymentId(r.getId())
                        .repaymentNumber(r.getRepaymentNumber())
                        .caseId(r.getCaseId())
                        .breachType(r.getDepositSlaStatus())
                        .breachHours(r.getDepositSlaBreachHours())
                        .status(r.getApprovalStatus().name())
                        .build())
                .collect(Collectors.toList());

        return SlaDashboardDTO.builder()
                .totalRepayments(totalRepayments)
                .withinSlaCount(withinSlaCount)
                .breachedCount(breachedCount)
                .slaCompliancePercentage(Math.round(compliancePercentage * 100.0) / 100.0)
                .criticalBreaches(criticalBreaches)
                .majorBreaches(majorBreaches)
                .minorBreaches(minorBreaches)
                .topBreachedCases(topBreaches)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepaymentDTO> getSlaBreachedRepayments() {
        log.info("Fetching SLA breached repayments");

        return repaymentRepository.findSlaBreachedRepayments()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    // ==================== Reconciliation APIs ====================

    @Override
    @Transactional(readOnly = true)
    public Page<ReconciliationDTO> getPendingReconciliation(Pageable pageable) {
        log.info("Fetching pending reconciliation list");

        Page<Repayment> pendingReconciliation = repaymentRepository.findByIsReconciledFalse(pageable);

        List<ReconciliationDTO> reconciliationList = pendingReconciliation.getContent().stream()
                .map(this::mapToReconciliationDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(reconciliationList, pageable, pendingReconciliation.getTotalElements());
    }

    @Override
    @Transactional
    public ReconciliationDTO updateReconciliationStatus(ReconciliationUpdateRequest request, Long userId) {
        log.info("Updating reconciliation status for repayment: {}", request.getRepaymentId());

        Repayment repayment = repaymentRepository.findById(request.getRepaymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Repayment", request.getRepaymentId()));

        if ("SUCCESS".equals(request.getReconciliationStatus())) {
            repayment.setIsReconciled(true);
            repayment.setReconciledAt(LocalDateTime.now());
            repayment.setReconciledBy(userId);
        }

        repayment.setUpdatedBy(userId);

        Repayment updated = repaymentRepository.save(repayment);
        log.info("Reconciliation status updated for repayment: {}", request.getRepaymentId());

        return mapToReconciliationDTO(updated);
    }

    @Override
    @Transactional
    public List<ReconciliationDTO> bulkReconcile(BulkReconciliationRequest request, Long userId) {
        log.info("Bulk reconciling {} repayments", request.getRepaymentIds().size());

        String batchId = request.getReconciliationBatchId() != null
                ? request.getReconciliationBatchId()
                : "BATCH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        List<Repayment> repayments = repaymentRepository.findAllById(request.getRepaymentIds());

        List<ReconciliationDTO> results = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Repayment repayment : repayments) {
            repayment.setIsReconciled(true);
            repayment.setReconciledAt(now);
            repayment.setReconciledBy(userId);
            repayment.setReconciliationBatchId(batchId);
            repayment.setUpdatedBy(userId);

            Repayment saved = repaymentRepository.save(repayment);
            results.add(mapToReconciliationDTO(saved));
        }

        log.info("Bulk reconciliation completed - batch: {}, count: {}", batchId, results.size());
        return results;
    }

    // ==================== Partial Payment APIs ====================

    @Override
    @Transactional
    public RepaymentDTO recordPartialPayment(PartialPaymentRequest request, Long userId) {
        log.info("Recording partial payment for case: {}, amount: {}", request.getCaseId(), request.getPartialAmount());

        Repayment repayment = new Repayment();
        repayment.setCaseId(request.getCaseId());
        repayment.setPaymentAmount(request.getPartialAmount());
        repayment.setPaymentDate(request.getPaymentDate());
        repayment.setRepaymentNumber("RPY-P-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        repayment.setApprovalStatus(RepaymentStatus.PENDING);
        repayment.setCurrentApprovalLevel(1);
        repayment.setCreatedBy(userId);
        repayment.setNotes("Partial Payment: " + (request.getNotes() != null ? request.getNotes() : ""));

        if (request.getPaymentMode() != null) {
            repayment.setPaymentMode(PaymentMode.valueOf(request.getPaymentMode()));
        }

        Repayment saved = repaymentRepository.save(repayment);
        log.info("Partial payment recorded with number: {}", saved.getRepaymentNumber());

        return mapper.toDto(saved);
    }

    // ==================== Receipt APIs ====================

    @Override
    @Transactional(readOnly = true)
    public byte[] generateReceipt(Long repaymentId) {
        log.info("Generating PDF receipt for repayment: {}", repaymentId);

        Repayment repayment = repaymentRepository.findById(repaymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Repayment", repaymentId));

        // Generate professional PDF receipt
        return receiptPdfService.generateReceiptPdf(repayment);
    }

    @Override
    @Transactional(readOnly = true)
    public RepaymentDTO getReceiptDetails(Long repaymentId) {
        log.info("Getting receipt details for repayment: {}", repaymentId);

        Repayment repayment = repaymentRepository.findById(repaymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Repayment", repaymentId));

        return mapper.toDto(repayment);
    }

    // ==================== Private Helper Methods ====================

    private ReconciliationDTO mapToReconciliationDTO(Repayment repayment) {
        return ReconciliationDTO.builder()
                .repaymentId(repayment.getId())
                .repaymentNumber(repayment.getRepaymentNumber())
                .caseId(repayment.getCaseId())
                .loanAccountNumber(repayment.getLoanAccountNumber())
                .paymentAmount(repayment.getPaymentAmount())
                .paymentMode(repayment.getPaymentMode() != null ? repayment.getPaymentMode().name() : null)
                .reconciliationStatus(repayment.getIsReconciled() != null && repayment.getIsReconciled()
                        ? "SUCCESS" : "PENDING")
                .isReconciled(repayment.getIsReconciled())
                .reconciledAt(repayment.getReconciledAt())
                .reconciledBy(repayment.getReconciledBy())
                .reconciliationBatchId(repayment.getReconciliationBatchId())
                .createdAt(repayment.getCreatedAt())
                .build();
    }

    private String generateReceiptHtml(Repayment repayment) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head><title>Payment Receipt</title></head>
            <body>
                <h1>Payment Receipt</h1>
                <hr/>
                <p><strong>Receipt Number:</strong> %s</p>
                <p><strong>Case ID:</strong> %d</p>
                <p><strong>Amount:</strong> ₹%s</p>
                <p><strong>Payment Date:</strong> %s</p>
                <p><strong>Payment Mode:</strong> %s</p>
                <p><strong>Status:</strong> %s</p>
                <p><strong>Approved On:</strong> %s</p>
                <hr/>
                <p><small>This is a system-generated receipt.</small></p>
            </body>
            </html>
            """,
                repayment.getRepaymentNumber(),
                repayment.getCaseId(),
                repayment.getPaymentAmount().setScale(2, RoundingMode.HALF_UP),
                repayment.getPaymentDate(),
                repayment.getPaymentMode() != null ? repayment.getPaymentMode().name() : "N/A",
                repayment.getApprovalStatus().name(),
                repayment.getApprovedAt() != null ? repayment.getApprovedAt().toString() : "N/A"
        );
    }
}
