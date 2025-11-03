package com.finx.communication.domain.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private String transactionId;
    private String gatewayOrderId;
    private String paymentLink;
    private String status;
    private String message;
    private String providerResponse;
}
