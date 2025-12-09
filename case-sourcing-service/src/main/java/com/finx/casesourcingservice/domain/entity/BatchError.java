package com.finx.casesourcingservice.domain.entity;

import com.finx.casesourcingservice.domain.enums.ErrorType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "batch_errors", indexes = {
        @Index(name = "idx_batch_id", columnList = "batch_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id", nullable = false, length = 100)
    private String batchId;

    @Column(name = "row_number")
    private Integer rowNumber;

    @Column(name = "external_case_id", length = 100)
    private String externalCaseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "error_type", length = 50)
    private ErrorType errorType;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "field_name", length = 100)
    private String fieldName;

    /**
     * Stores the original CSV row data as JSON for error export
     * This allows exporting the exact data that was uploaded with STATUS and REMARKS
     */
    @Column(name = "original_row_data", columnDefinition = "TEXT")
    private String originalRowData;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
