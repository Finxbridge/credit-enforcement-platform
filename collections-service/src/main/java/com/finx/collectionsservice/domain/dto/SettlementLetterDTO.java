package com.finx.collectionsservice.domain.dto;

import com.finx.collectionsservice.domain.enums.LetterStatus;
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
public class SettlementLetterDTO {
    private Long id;
    private String letterNumber;
    private Long otsId;
    private String otsNumber;
    private Long caseId;
    private String loanAccountNumber;
    private String customerName;
    private BigDecimal originalOutstanding;
    private BigDecimal settlementAmount;
    private BigDecimal waiverAmount;
    private BigDecimal discountPercentage;
    private LocalDateTime paymentDeadline;
    private Long templateId;
    private String letterContent; // JSON string
    private String pdfUrl;
    private LetterStatus status;
    private LocalDateTime generatedAt;
    private Long generatedBy;
    private LocalDateTime downloadedAt;
    private Long downloadedBy;
    private Integer downloadCount;
    private LocalDateTime sentAt;
    private String sentVia;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
