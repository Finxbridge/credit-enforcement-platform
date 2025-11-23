package com.finx.casesourcingservice.domain.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for capturing PTP commitment
 * FR-PTP-1: Capture Promise to Pay
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CapturePTPRequest {

    @NotNull(message = "Case ID is required")
    private Long caseId;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "PTP date is required")
    @Future(message = "PTP date must be in the future")
    private LocalDate ptpDate;

    @NotNull(message = "PTP amount is required")
    @DecimalMin(value = "1.0", message = "PTP amount must be at least 1")
    @Digits(integer = 15, fraction = 2, message = "Invalid amount format")
    private BigDecimal ptpAmount;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    @Size(max = 50, message = "Call disposition cannot exceed 50 characters")
    private String callDisposition;

    private LocalDate followUpDate;
}
