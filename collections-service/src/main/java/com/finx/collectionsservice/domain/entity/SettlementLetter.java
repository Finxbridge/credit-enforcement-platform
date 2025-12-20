package com.finx.collectionsservice.domain.entity;

import com.finx.collectionsservice.domain.enums.LetterStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Settlement Letter Entity
 * Tracks settlement letter generation for OTS
 */
@Entity
@Table(name = "settlement_letters")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementLetter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "letter_number", nullable = false, unique = true, length = 50)
    private String letterNumber;

    @Column(name = "ots_id", nullable = false)
    private Long otsId;

    @Column(name = "ots_number", length = 50)
    private String otsNumber;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "loan_account_number", length = 50)
    private String loanAccountNumber;

    @Column(name = "customer_name", length = 255)
    private String customerName;

    @Column(name = "original_outstanding", precision = 15, scale = 2)
    private BigDecimal originalOutstanding;

    @Column(name = "settlement_amount", precision = 15, scale = 2)
    private BigDecimal settlementAmount;

    @Column(name = "waiver_amount", precision = 15, scale = 2)
    private BigDecimal waiverAmount;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(name = "payment_deadline")
    private LocalDateTime paymentDeadline;

    @Column(name = "template_id")
    private Long templateId;

    // Store letter content as JSON string
    @Column(name = "letter_content", columnDefinition = "TEXT")
    private String letterContent;

    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private LetterStatus status;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "generated_by")
    private Long generatedBy;

    @Column(name = "downloaded_at")
    private LocalDateTime downloadedAt;

    @Column(name = "downloaded_by")
    private Long downloadedBy;

    @Column(name = "download_count")
    private Integer downloadCount;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "sent_via", length = 20)
    private String sentVia;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = LetterStatus.DRAFT;
        }
        if (downloadCount == null) {
            downloadCount = 0;
        }
    }
}
