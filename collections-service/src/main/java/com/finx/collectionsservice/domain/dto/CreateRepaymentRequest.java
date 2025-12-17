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
public class CreateRepaymentRequest {

    @NotNull(message = "Case ID is required")
    private Long caseId;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than 0")
    @Digits(integer = 15, fraction = 2, message = "Invalid amount format")
    private BigDecimal paymentAmount;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    @NotNull(message = "Payment mode is required")
    private PaymentMode paymentMode;

    private Long collectedBy;

    @Size(max = 255, message = "Collection location cannot exceed 255 characters")
    private String collectionLocation;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    private Long otsId;
}
