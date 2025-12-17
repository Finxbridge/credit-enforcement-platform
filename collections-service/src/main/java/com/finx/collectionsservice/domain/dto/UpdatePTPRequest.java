package com.finx.collectionsservice.domain.dto;

import com.finx.collectionsservice.domain.enums.PTPStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for updating PTP status
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePTPRequest {

    @NotNull(message = "PTP status is required")
    private PTPStatus ptpStatus;

    @DecimalMin(value = "0.0", message = "Payment amount cannot be negative")
    @Digits(integer = 15, fraction = 2, message = "Invalid amount format")
    private BigDecimal paymentReceivedAmount;

    private LocalDate paymentReceivedDate;

    @Size(max = 500, message = "Broken reason cannot exceed 500 characters")
    private String brokenReason;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    private LocalDate followUpDate;

    @NotNull(message = "Updated by user ID is required")
    private Long updatedBy;
}
