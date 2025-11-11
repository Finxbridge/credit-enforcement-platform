package com.finx.allocationreallocationservice.domain.entity;

import com.finx.allocationreallocationservice.domain.enums.ErrorType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "batch_errors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "error_id", unique = true, nullable = false)
    private String errorId;

    @Column(name = "batch_id", nullable = false)
    private String batchId;

    @Column(name = "row_number")
    private Integer rowNumber;

    @Column(name = "case_id")
    private Long caseId;

    @Column(name = "external_case_id")
    private String externalCaseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "error_type", nullable = false)
    private ErrorType errorType;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "field_name")
    private String fieldName;

    @Column(name = "module")
    private String module;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
