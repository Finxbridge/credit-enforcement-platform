package com.finx.collectionsservice.domain.dto;

import com.finx.collectionsservice.domain.enums.PaymentMode;
import jakarta.validation.constraints.*;
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
public class CreateOTSRequest {

    @NotNull(message = "Case ID is required")
    private Long caseId;

    @NotNull(message = "Proposed settlement amount is required")
    @DecimalMin(value = "1.0", message = "Proposed settlement must be at least 1")
    @Digits(integer = 15, fraction = 2, message = "Invalid amount format")
    private BigDecimal proposedSettlement;

    private PaymentMode paymentMode;

    private Integer installmentCount;

    private LocalDate paymentDeadline;

    @Size(max = 1000, message = "Intent notes cannot exceed 1000 characters")
    private String intentNotes;

    private Boolean borrowerConsent;
}
