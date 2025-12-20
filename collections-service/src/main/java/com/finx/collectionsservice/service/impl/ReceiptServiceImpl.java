package com.finx.collectionsservice.service.impl;

import com.finx.collectionsservice.domain.dto.*;
import com.finx.collectionsservice.domain.entity.Receipt;
import com.finx.collectionsservice.domain.entity.ReceiptHistory;
import com.finx.collectionsservice.domain.entity.Repayment;
import com.finx.collectionsservice.domain.enums.PaymentMode;
import com.finx.collectionsservice.domain.enums.ReceiptFormat;
import com.finx.collectionsservice.domain.enums.ReceiptStatus;
import com.finx.collectionsservice.domain.enums.RepaymentStatus;
import com.finx.collectionsservice.exception.BusinessException;
import com.finx.collectionsservice.exception.ResourceNotFoundException;
import com.finx.collectionsservice.mapper.CollectionsMapper;
import com.finx.collectionsservice.repository.ReceiptHistoryRepository;
import com.finx.collectionsservice.repository.ReceiptRepository;
import com.finx.collectionsservice.repository.RepaymentRepository;
import com.finx.collectionsservice.service.AuditLogService;
import com.finx.collectionsservice.service.ReceiptService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReceiptServiceImpl implements ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final ReceiptHistoryRepository receiptHistoryRepository;
    private final RepaymentRepository repaymentRepository;
    private final CollectionsMapper mapper;
    private final AuditLogService auditLogService;

    @Override
    @CacheEvict(value = "receipts", allEntries = true)
    public ReceiptDTO generateReceipt(GenerateReceiptRequest request, Long userId) {
        Repayment repayment = repaymentRepository.findById(request.getRepaymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Repayment not found with id: " + request.getRepaymentId()));

        if (repayment.getApprovalStatus() != RepaymentStatus.APPROVED) {
            throw new BusinessException("Cannot generate receipt for non-approved repayment");
        }

        if (receiptRepository.existsByRepaymentId(request.getRepaymentId())) {
            throw new BusinessException("Receipt already exists for repayment: " + repayment.getRepaymentNumber());
        }

        String receiptNumber = generateReceiptNumber();

        Receipt receipt = Receipt.builder()
                .receiptNumber(receiptNumber)
                .status(ReceiptStatus.GENERATED)
                .repaymentId(request.getRepaymentId())
                .repaymentNumber(repayment.getRepaymentNumber())
                .caseId(repayment.getCaseId())
                .loanAccountNumber(repayment.getLoanAccountNumber())
                .amount(repayment.getPaymentAmount())
                .paymentMode(repayment.getPaymentMode())
                .paymentDate(repayment.getPaymentDate() != null ? repayment.getPaymentDate().atStartOfDay() : null)
                .paymentReference(repayment.getTransactionId() != null ? repayment.getTransactionId().toString() : null)
                .format(request.getFormat() != null ? request.getFormat() : ReceiptFormat.PDF)
                .templateId(request.getTemplateId())
                .customerEmail(request.getCustomerEmail())
                .customerPhone(request.getCustomerPhone())
                .generatedBy(userId)
                .remarks(request.getRemarks())
                .build();

        receipt = receiptRepository.save(receipt);

        // Update repayment with receipt ID
        repayment.setReceiptId(receipt.getId());
        repaymentRepository.save(repayment);

        // Record history
        recordHistory(receipt, "GENERATED", null, ReceiptStatus.GENERATED.name(), userId, "Receipt generated");

        // Audit log
        auditLogService.logEvent("RECEIPT", "Receipt", receipt.getId(), receiptNumber,
                userId, null, "Receipt generated for repayment: " + repayment.getRepaymentNumber());

        log.info("Generated receipt {} for repayment {}", receiptNumber, repayment.getRepaymentNumber());

        // Auto-send if requested
        if (Boolean.TRUE.equals(request.getSendEmail()) && request.getCustomerEmail() != null) {
            emailReceipt(receipt.getId(), request.getCustomerEmail(), userId);
        }
        if (Boolean.TRUE.equals(request.getSendSms()) && request.getCustomerPhone() != null) {
            sendReceiptSms(receipt.getId(), request.getCustomerPhone(), userId);
        }

        return mapper.toReceiptDTO(receipt);
    }

    @Override
    @CacheEvict(value = "receipts", allEntries = true)
    public ReceiptDTO generateReceipt(Long repaymentId, Long generatedBy) {
        GenerateReceiptRequest request = GenerateReceiptRequest.builder()
                .repaymentId(repaymentId)
                .build();
        return generateReceipt(request, generatedBy);
    }

    @Override
    @CacheEvict(value = "receipts", allEntries = true)
    public BulkReceiptResponse generateBulkReceipts(BulkReceiptRequest request, Long userId) {
        List<ReceiptDTO> generatedReceipts = new ArrayList<>();
        List<BulkReceiptResponse.ReceiptGenerationError> errors = new ArrayList<>();

        for (Long repaymentId : request.getRepaymentIds()) {
            try {
                GenerateReceiptRequest generateRequest = GenerateReceiptRequest.builder()
                        .repaymentId(repaymentId)
                        .format(request.getFormat())
                        .templateId(request.getTemplateId())
                        .sendEmail(request.getSendEmail())
                        .sendSms(request.getSendSms())
                        .build();

                ReceiptDTO receipt = generateReceipt(generateRequest, userId);
                generatedReceipts.add(receipt);
            } catch (Exception e) {
                log.error("Failed to generate receipt for repayment {}: {}", repaymentId, e.getMessage());
                errors.add(BulkReceiptResponse.ReceiptGenerationError.builder()
                        .repaymentId(repaymentId)
                        .errorMessage(e.getMessage())
                        .build());
            }
        }

        return BulkReceiptResponse.builder()
                .totalRequested(request.getRepaymentIds().size())
                .successCount(generatedReceipts.size())
                .failedCount(errors.size())
                .generatedReceipts(generatedReceipts)
                .errors(errors)
                .build();
    }

    @Override
    @Cacheable(value = "receipts", key = "#id")
    @Transactional(readOnly = true)
    public ReceiptDTO getReceiptById(Long id) {
        Receipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found with id: " + id));
        return mapper.toReceiptDTO(receipt);
    }

    @Override
    @Transactional(readOnly = true)
    public ReceiptDTO getReceiptByNumber(String receiptNumber) {
        Receipt receipt = receiptRepository.findByReceiptNumber(receiptNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found with number: " + receiptNumber));
        return mapper.toReceiptDTO(receipt);
    }

    @Override
    @Transactional(readOnly = true)
    public ReceiptDTO getReceiptByRepaymentId(Long repaymentId) {
        Receipt receipt = receiptRepository.findByRepaymentId(repaymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found for repayment: " + repaymentId));
        return mapper.toReceiptDTO(receipt);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReceiptDTO> getReceiptsByCaseId(Long caseId) {
        return receiptRepository.findByCaseId(caseId).stream()
                .map(mapper::toReceiptDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReceiptDTO> getReceiptsByLoanAccount(String loanAccountNumber) {
        return receiptRepository.findByLoanAccountNumber(loanAccountNumber).stream()
                .map(mapper::toReceiptDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReceiptDTO> getReceiptsByStatus(ReceiptStatus status, Pageable pageable) {
        return receiptRepository.findByStatus(status, pageable)
                .map(mapper::toReceiptDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReceiptDTO> getReceiptsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return receiptRepository.findByDateRange(startDate, endDate, pageable)
                .map(mapper::toReceiptDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReceiptDTO> searchReceipts(ReceiptSearchCriteria criteria, Pageable pageable) {
        Specification<Receipt> spec = buildSpecification(criteria);
        return receiptRepository.findAll(spec, pageable).map(mapper::toReceiptDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReceiptDTO> getMyGeneratedReceipts(Long userId, Pageable pageable) {
        return receiptRepository.findByGeneratedBy(userId, pageable)
                .map(mapper::toReceiptDTO);
    }

    @Override
    @CacheEvict(value = "receipts", key = "#id")
    public ReceiptDTO downloadReceipt(Long id, Long downloadedBy) {
        Receipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found with id: " + id));

        validateReceiptActive(receipt);

        receipt.setDownloadedAt(LocalDateTime.now());
        receipt.setDownloadedBy(downloadedBy);
        receipt.setDownloadCount(receipt.getDownloadCount() + 1);
        if (receipt.getStatus() == ReceiptStatus.GENERATED || receipt.getStatus() == ReceiptStatus.VERIFIED) {
            receipt.setStatus(ReceiptStatus.DOWNLOADED);
        }

        receipt = receiptRepository.save(receipt);

        recordHistory(receipt, "DOWNLOADED", null, receipt.getStatus().name(), downloadedBy,
                "Receipt downloaded (count: " + receipt.getDownloadCount() + ")");

        auditLogService.logEvent("RECEIPT", "Receipt", receipt.getId(), receipt.getReceiptNumber(),
                downloadedBy, null, "Receipt downloaded");

        log.info("Receipt {} downloaded by user {}", receipt.getReceiptNumber(), downloadedBy);
        return mapper.toReceiptDTO(receipt);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadReceiptPdf(Long id, Long downloadedBy) {
        Receipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found with id: " + id));

        validateReceiptActive(receipt);

        // In a real implementation, this would fetch from storage or generate PDF
        // For now, return a placeholder
        log.info("Downloading PDF for receipt {}", receipt.getReceiptNumber());

        // Update download stats (in a separate transaction ideally)
        downloadReceipt(id, downloadedBy);

        // Placeholder - actual implementation would read from pdfStoragePath
        return new byte[0];
    }

    @Override
    @CacheEvict(value = "receipts", key = "#id")
    public ReceiptDTO emailReceipt(Long id, EmailReceiptRequest request, Long sentBy) {
        Receipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found with id: " + id));

        validateReceiptActive(receipt);

        receipt.setEmailedAt(LocalDateTime.now());
        receipt.setEmailedTo(request.getPrimaryEmail());
        receipt.setEmailCount(receipt.getEmailCount() != null ? receipt.getEmailCount() + 1 : 1);
        if (receipt.getStatus() == ReceiptStatus.GENERATED || receipt.getStatus() == ReceiptStatus.VERIFIED) {
            receipt.setStatus(ReceiptStatus.SENT);
        }

        receipt = receiptRepository.save(receipt);

        // In real implementation, send email via communication service
        log.info("Emailing receipt {} to {}", receipt.getReceiptNumber(), request.getPrimaryEmail());

        recordHistory(receipt, "EMAILED", null, receipt.getStatus().name(), sentBy,
                "Receipt emailed to: " + request.getPrimaryEmail());

        auditLogService.logEvent("RECEIPT", "Receipt", receipt.getId(), receipt.getReceiptNumber(),
                sentBy, null, "Receipt emailed to: " + request.getPrimaryEmail());

        return mapper.toReceiptDTO(receipt);
    }

    @Override
    @CacheEvict(value = "receipts", key = "#id")
    public ReceiptDTO emailReceipt(Long id, String email, Long sentBy) {
        EmailReceiptRequest request = EmailReceiptRequest.builder()
                .primaryEmail(email)
                .attachPdf(true)
                .build();
        return emailReceipt(id, request, sentBy);
    }

    @Override
    @CacheEvict(value = "receipts", key = "#id")
    public ReceiptDTO sendReceiptSms(Long id, String phoneNumber, Long sentBy) {
        Receipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found with id: " + id));

        validateReceiptActive(receipt);

        receipt.setSmsSentAt(LocalDateTime.now());
        receipt.setSmsSentTo(phoneNumber);
        if (receipt.getStatus() == ReceiptStatus.GENERATED || receipt.getStatus() == ReceiptStatus.VERIFIED) {
            receipt.setStatus(ReceiptStatus.SENT);
        }

        receipt = receiptRepository.save(receipt);

        // In real implementation, send SMS via communication service
        log.info("Sending SMS for receipt {} to {}", receipt.getReceiptNumber(), phoneNumber);

        recordHistory(receipt, "SMS_SENT", null, receipt.getStatus().name(), sentBy,
                "Receipt SMS sent to: " + phoneNumber);

        auditLogService.logEvent("RECEIPT", "Receipt", receipt.getId(), receipt.getReceiptNumber(),
                sentBy, null, "Receipt SMS sent to: " + phoneNumber);

        return mapper.toReceiptDTO(receipt);
    }

    @Override
    @CacheEvict(value = "receipts", key = "#id")
    public ReceiptDTO verifyReceipt(Long id, Long verifiedBy) {
        Receipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found with id: " + id));

        if (receipt.getStatus() != ReceiptStatus.GENERATED) {
            throw new BusinessException("Only generated receipts can be verified");
        }

        String oldStatus = receipt.getStatus().name();
        receipt.setStatus(ReceiptStatus.VERIFIED);
        receipt.setVerifiedAt(LocalDateTime.now());
        receipt.setVerifiedBy(verifiedBy);

        receipt = receiptRepository.save(receipt);

        recordHistory(receipt, "VERIFIED", oldStatus, ReceiptStatus.VERIFIED.name(), verifiedBy, "Receipt verified");

        auditLogService.logEvent("RECEIPT", "Receipt", receipt.getId(), receipt.getReceiptNumber(),
                verifiedBy, null, "Receipt verified");

        log.info("Receipt {} verified by user {}", receipt.getReceiptNumber(), verifiedBy);
        return mapper.toReceiptDTO(receipt);
    }

    @Override
    @CacheEvict(value = "receipts", key = "#id")
    public ReceiptDTO cancelReceipt(Long id, CancelReceiptRequest request, Long userId) {
        Receipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found with id: " + id));

        if (receipt.getStatus() == ReceiptStatus.CANCELLED || receipt.getStatus() == ReceiptStatus.VOID) {
            throw new BusinessException("Receipt is already cancelled or voided");
        }

        String oldStatus = receipt.getStatus().name();
        receipt.setStatus(ReceiptStatus.CANCELLED);
        receipt.setCancelledAt(LocalDateTime.now());
        receipt.setCancelledBy(userId);
        receipt.setCancellationReason(request.getReason());

        receipt = receiptRepository.save(receipt);

        recordHistory(receipt, "CANCELLED", oldStatus, ReceiptStatus.CANCELLED.name(), userId,
                "Receipt cancelled: " + request.getReason());

        auditLogService.logEvent("RECEIPT", "Receipt", receipt.getId(), receipt.getReceiptNumber(),
                userId, null, "Receipt cancelled: " + request.getReason());

        log.info("Receipt {} cancelled by user {}: {}", receipt.getReceiptNumber(), userId, request.getReason());
        return mapper.toReceiptDTO(receipt);
    }

    @Override
    @CacheEvict(value = "receipts", key = "#id")
    public ReceiptDTO voidReceipt(Long id, VoidReceiptRequest request, Long userId) {
        Receipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found with id: " + id));

        if (receipt.getStatus() == ReceiptStatus.VOID) {
            throw new BusinessException("Receipt is already voided");
        }

        if (receipt.getStatus() == ReceiptStatus.CANCELLED) {
            throw new BusinessException("Cannot void a cancelled receipt");
        }

        String oldStatus = receipt.getStatus().name();
        receipt.setStatus(ReceiptStatus.VOID);
        receipt.setVoidedAt(LocalDateTime.now());
        receipt.setVoidedBy(userId);
        receipt.setVoidReason(request.getReason());

        receipt = receiptRepository.save(receipt);

        recordHistory(receipt, "VOIDED", oldStatus, ReceiptStatus.VOID.name(), userId,
                "Receipt voided: " + request.getReason());

        auditLogService.logEvent("RECEIPT", "Receipt", receipt.getId(), receipt.getReceiptNumber(),
                userId, null, "Receipt voided: " + request.getReason());

        log.info("Receipt {} voided by user {}: {}", receipt.getReceiptNumber(), userId, request.getReason());
        return mapper.toReceiptDTO(receipt);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReceiptHistoryDTO> getReceiptHistory(Long receiptId) {
        List<ReceiptHistory> history = receiptHistoryRepository.findByReceiptIdOrderByActionTimestampDesc(receiptId);
        return history.stream()
                .map(this::toHistoryDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReceiptHistoryDTO> getReceiptHistoryPaginated(Long receiptId, Pageable pageable) {
        List<ReceiptHistory> history = receiptHistoryRepository.findByReceiptIdOrderByActionTimestampDesc(receiptId);
        List<ReceiptHistoryDTO> dtos = history.stream()
                .map(this::toHistoryDTO)
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), dtos.size());
        List<ReceiptHistoryDTO> pageContent = dtos.subList(start, end);

        return new PageImpl<>(pageContent, pageable, dtos.size());
    }

    @Override
    @Transactional(readOnly = true)
    public ReceiptSummaryDTO getReceiptSummary() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        return ReceiptSummaryDTO.builder()
                .totalReceipts(receiptRepository.count())
                .pendingReceipts(receiptRepository.countByStatus(ReceiptStatus.PENDING))
                .generatedReceipts(receiptRepository.countByStatus(ReceiptStatus.GENERATED))
                .sentReceipts(receiptRepository.countByStatus(ReceiptStatus.SENT))
                .downloadedReceipts(receiptRepository.countByStatus(ReceiptStatus.DOWNLOADED))
                .cancelledReceipts(receiptRepository.countByStatus(ReceiptStatus.CANCELLED))
                .voidReceipts(receiptRepository.countByStatus(ReceiptStatus.VOID))
                .totalAmount(receiptRepository.sumTotalAmount())
                .todayAmount(receiptRepository.sumAmountSince(todayStart))
                .todayCount(receiptRepository.countSince(todayStart))
                .cashReceipts(receiptRepository.countByPaymentMode(PaymentMode.CASH))
                .chequeReceipts(receiptRepository.countByPaymentMode(PaymentMode.CHEQUE))
                .onlineReceipts(receiptRepository.countByPaymentMode(PaymentMode.NEFT))
                .upiReceipts(receiptRepository.countByPaymentMode(PaymentMode.UPI))
                .cashAmount(receiptRepository.sumAmountByPaymentMode(PaymentMode.CASH))
                .chequeAmount(receiptRepository.sumAmountByPaymentMode(PaymentMode.CHEQUE))
                .onlineAmount(receiptRepository.sumAmountByPaymentMode(PaymentMode.NEFT))
                .upiAmount(receiptRepository.sumAmountByPaymentMode(PaymentMode.UPI))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ReceiptSummaryDTO getReceiptSummaryByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        // For date range summary, we need to filter by date
        // This is a simplified version - in production, you'd add date-filtered queries
        return getReceiptSummary();
    }

    @Override
    @Transactional(readOnly = true)
    public Long countReceiptsByCaseId(Long caseId) {
        return receiptRepository.countByCaseId(caseId);
    }

    @Override
    @CacheEvict(value = "receipts", key = "#id")
    public ReceiptDTO regenerateReceipt(Long id, Long userId) {
        Receipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found with id: " + id));

        if (receipt.getStatus() == ReceiptStatus.CANCELLED || receipt.getStatus() == ReceiptStatus.VOID) {
            throw new BusinessException("Cannot regenerate cancelled or voided receipt");
        }

        // Mark old receipt and generate new PDF
        String oldStatus = receipt.getStatus().name();
        receipt.setGeneratedAt(LocalDateTime.now());
        receipt.setGeneratedBy(userId);
        // In real implementation, regenerate PDF here

        receipt = receiptRepository.save(receipt);

        recordHistory(receipt, "REGENERATED", oldStatus, receipt.getStatus().name(), userId, "Receipt regenerated");

        auditLogService.logEvent("RECEIPT", "Receipt", receipt.getId(), receipt.getReceiptNumber(),
                userId, null, "Receipt regenerated");

        log.info("Receipt {} regenerated by user {}", receipt.getReceiptNumber(), userId);
        return mapper.toReceiptDTO(receipt);
    }

    // Helper methods

    private String generateReceiptNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uniquePart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "RCP-" + datePart + "-" + uniquePart;
    }

    private void validateReceiptActive(Receipt receipt) {
        if (receipt.getStatus() == ReceiptStatus.CANCELLED || receipt.getStatus() == ReceiptStatus.VOID) {
            throw new BusinessException("Receipt is " + receipt.getStatus().name().toLowerCase() + " and cannot be accessed");
        }
    }

    private void recordHistory(Receipt receipt, String action, String fromStatus, String toStatus, Long actorId, String remarks) {
        ReceiptHistory history = ReceiptHistory.builder()
                .receiptId(receipt.getId())
                .receiptNumber(receipt.getReceiptNumber())
                .action(action)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .actorId(actorId)
                .remarks(remarks)
                .actionTimestamp(LocalDateTime.now())
                .build();
        receiptHistoryRepository.save(history);
    }

    private ReceiptHistoryDTO toHistoryDTO(ReceiptHistory history) {
        return ReceiptHistoryDTO.builder()
                .id(history.getId())
                .receiptId(history.getReceiptId())
                .receiptNumber(history.getReceiptNumber())
                .action(history.getAction())
                .fromStatus(history.getFromStatus())
                .toStatus(history.getToStatus())
                .actorId(history.getActorId())
                .actorName(history.getActorName())
                .remarks(history.getRemarks())
                .actionTimestamp(history.getActionTimestamp())
                .build();
    }

    private Specification<Receipt> buildSpecification(ReceiptSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getReceiptNumber() != null && !criteria.getReceiptNumber().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("receiptNumber")),
                        "%" + criteria.getReceiptNumber().toLowerCase() + "%"));
            }

            if (criteria.getRepaymentNumber() != null && !criteria.getRepaymentNumber().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("repaymentNumber")),
                        "%" + criteria.getRepaymentNumber().toLowerCase() + "%"));
            }

            if (criteria.getLoanAccountNumber() != null && !criteria.getLoanAccountNumber().isEmpty()) {
                predicates.add(cb.equal(root.get("loanAccountNumber"), criteria.getLoanAccountNumber()));
            }

            if (criteria.getCustomerName() != null && !criteria.getCustomerName().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("customerName")),
                        "%" + criteria.getCustomerName().toLowerCase() + "%"));
            }

            if (criteria.getCaseId() != null) {
                predicates.add(cb.equal(root.get("caseId"), criteria.getCaseId()));
            }

            if (criteria.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
            }

            if (criteria.getFormat() != null) {
                predicates.add(cb.equal(root.get("format"), criteria.getFormat()));
            }

            if (criteria.getPaymentMode() != null) {
                predicates.add(cb.equal(root.get("paymentMode"), criteria.getPaymentMode()));
            }

            if (criteria.getGeneratedDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("generatedAt"),
                        criteria.getGeneratedDateFrom().atStartOfDay()));
            }

            if (criteria.getGeneratedDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("generatedAt"),
                        criteria.getGeneratedDateTo().atTime(LocalTime.MAX)));
            }

            if (criteria.getMinAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), criteria.getMinAmount()));
            }

            if (criteria.getMaxAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), criteria.getMaxAmount()));
            }

            if (criteria.getGeneratedBy() != null) {
                predicates.add(cb.equal(root.get("generatedBy"), criteria.getGeneratedBy()));
            }

            if (Boolean.TRUE.equals(criteria.getIsEmailed())) {
                predicates.add(cb.isNotNull(root.get("emailedAt")));
            } else if (Boolean.FALSE.equals(criteria.getIsEmailed())) {
                predicates.add(cb.isNull(root.get("emailedAt")));
            }

            if (Boolean.TRUE.equals(criteria.getIsDownloaded())) {
                predicates.add(cb.isNotNull(root.get("downloadedAt")));
            } else if (Boolean.FALSE.equals(criteria.getIsDownloaded())) {
                predicates.add(cb.isNull(root.get("downloadedAt")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
