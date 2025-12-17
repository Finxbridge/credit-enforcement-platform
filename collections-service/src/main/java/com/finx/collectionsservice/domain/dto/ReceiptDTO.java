package com.finx.collectionsservice.domain.dto;

import com.finx.collectionsservice.domain.enums.PaymentMode;
import com.finx.collectionsservice.domain.enums.ReceiptFormat;
import com.finx.collectionsservice.domain.enums.ReceiptStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptDTO {
    private Long id;
    private String receiptNumber;
    private ReceiptStatus status;
    private Long repaymentId;
    private String repaymentNumber;
    private Long caseId;
    private String loanAccountNumber;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private BigDecimal amount;
    private PaymentMode paymentMode;
    private LocalDateTime paymentDate;
    private String paymentReference;
    private ReceiptFormat format;
    private Long templateId;
    private String pdfUrl;
    private String pdfStoragePath;
    private LocalDateTime generatedAt;
    private Long generatedBy;
    private String generatedByName;
    private LocalDateTime verifiedAt;
    private Long verifiedBy;
    private String verifiedByName;
    private LocalDateTime downloadedAt;
    private Long downloadedBy;
    private Integer downloadCount;
    private LocalDateTime emailedAt;
    private String emailedTo;
    private Integer emailCount;
    private LocalDateTime smsSentAt;
    private String smsSentTo;
    private LocalDateTime cancelledAt;
    private Long cancelledBy;
    private String cancelledByName;
    private String cancellationReason;
    private LocalDateTime voidedAt;
    private Long voidedBy;
    private String voidedByName;
    private String voidReason;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
