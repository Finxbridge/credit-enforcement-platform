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

    /**
     * Stores the original CSV row data as JSON for error export
     * This allows exporting the exact data that was uploaded with STATUS and REMARKS
     */
    @Column(name = "original_row_data", columnDefinition = "TEXT")
    private String originalRowData;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
