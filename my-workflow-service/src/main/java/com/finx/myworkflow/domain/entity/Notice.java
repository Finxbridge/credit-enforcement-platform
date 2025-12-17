package com.finx.myworkflow.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Read-only Notice entity for workflow tabs
 */
@Entity
@Table(name = "notices")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "notice_number", length = 50)
    private String noticeNumber;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "notice_type", length = 50)
    private String noticeType;

    @Column(name = "notice_subtype", length = 50)
    private String noticeSubtype;

    @Column(name = "status", length = 30)
    private String status;

    @Column(name = "recipient_name", length = 255)
    private String recipientName;

    @Column(name = "recipient_address", columnDefinition = "TEXT")
    private String recipientAddress;

    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
