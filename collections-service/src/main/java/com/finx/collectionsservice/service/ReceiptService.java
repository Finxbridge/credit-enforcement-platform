package com.finx.collectionsservice.service;

import com.finx.collectionsservice.domain.dto.*;
import com.finx.collectionsservice.domain.enums.ReceiptStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface ReceiptService {

    // Receipt Generation
    ReceiptDTO generateReceipt(GenerateReceiptRequest request, Long userId);

    ReceiptDTO generateReceipt(Long repaymentId, Long generatedBy);

    BulkReceiptResponse generateBulkReceipts(BulkReceiptRequest request, Long userId);

    // Receipt Retrieval
    ReceiptDTO getReceiptById(Long id);

    ReceiptDTO getReceiptByNumber(String receiptNumber);

    ReceiptDTO getReceiptByRepaymentId(Long repaymentId);

    List<ReceiptDTO> getReceiptsByCaseId(Long caseId);

    List<ReceiptDTO> getReceiptsByLoanAccount(String loanAccountNumber);

    Page<ReceiptDTO> getReceiptsByStatus(ReceiptStatus status, Pageable pageable);

    Page<ReceiptDTO> getReceiptsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<ReceiptDTO> searchReceipts(ReceiptSearchCriteria criteria, Pageable pageable);

    Page<ReceiptDTO> getMyGeneratedReceipts(Long userId, Pageable pageable);

    // Receipt Actions
    ReceiptDTO downloadReceipt(Long id, Long downloadedBy);

    byte[] downloadReceiptPdf(Long id, Long downloadedBy);

    ReceiptDTO emailReceipt(Long id, EmailReceiptRequest request, Long sentBy);

    ReceiptDTO emailReceipt(Long id, String email, Long sentBy);

    ReceiptDTO sendReceiptSms(Long id, String phoneNumber, Long sentBy);

    // Receipt Verification
    ReceiptDTO verifyReceipt(Long id, Long verifiedBy);

    // Receipt Cancellation/Void
    ReceiptDTO cancelReceipt(Long id, CancelReceiptRequest request, Long userId);

    ReceiptDTO voidReceipt(Long id, VoidReceiptRequest request, Long userId);

    // Receipt History
    List<ReceiptHistoryDTO> getReceiptHistory(Long receiptId);

    Page<ReceiptHistoryDTO> getReceiptHistoryPaginated(Long receiptId, Pageable pageable);

    // Summary & Statistics
    ReceiptSummaryDTO getReceiptSummary();

    ReceiptSummaryDTO getReceiptSummaryByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    Long countReceiptsByCaseId(Long caseId);

    // Regenerate Receipt (if original is corrupted or template changed)
    ReceiptDTO regenerateReceipt(Long id, Long userId);
}
