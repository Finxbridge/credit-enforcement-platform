package com.finx.collectionsservice.domain.dto;

import com.finx.collectionsservice.domain.enums.PaymentMode;
import com.finx.collectionsservice.domain.enums.ReceiptFormat;
import com.finx.collectionsservice.domain.enums.ReceiptStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptSearchCriteria {

    private String receiptNumber;
    private String repaymentNumber;
    private String loanAccountNumber;
    private String customerName;
    private Long caseId;
    private ReceiptStatus status;
    private ReceiptFormat format;
    private PaymentMode paymentMode;
    private LocalDate generatedDateFrom;
    private LocalDate generatedDateTo;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Long generatedBy;
    private Boolean isEmailed;
    private Boolean isDownloaded;
}
