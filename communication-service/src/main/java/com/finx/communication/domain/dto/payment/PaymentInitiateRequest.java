package com.finx.communication.domain.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitiateRequest {

    private String merchantId;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    private String storeId;
    private String terminalId;
    private String provider; // PHONEPE, RAZORPAY

    private String customerMobile;
    private String customerEmail;

    private Long caseId;
    private String loanAccountNumber;
}
