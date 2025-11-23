package com.finx.communication.service.payment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finx.common.service.IntegrationCacheService;
import com.finx.communication.domain.dto.payment.*;
import com.finx.communication.domain.entity.PaymentGatewayTransaction;
import com.finx.common.model.ThirdPartyIntegrationMaster;
import com.finx.communication.exception.ApiCallException;
import com.finx.communication.exception.ConfigurationNotFoundException;
import com.finx.communication.repository.PaymentGatewayTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentGatewayService {

    private final WebClient webClient;
    private final IntegrationCacheService integrationCacheService;
    private final PaymentGatewayTransactionRepository paymentRepository;
    private final ObjectMapper objectMapper;

    private static final String INTEGRATION_NAME = "PHONEPE_PAYMENT";

    public PaymentResponse initiatePayment(PaymentInitiateRequest request) {
        log.info("Initiating payment for amount: {}", request.getAmount());

        // 1. Get configuration
        ThirdPartyIntegrationMaster config = getIntegrationConfig();

        // 2. Build request body
        Map<String, Object> requestBody = Map.of(
                "merchantId",
                request.getMerchantId() != null ? request.getMerchantId()
                        : config.getConfigValueAsString("merchant_id"),
                "amount", request.getAmount(),
                "storeId", request.getStoreId() != null ? request.getStoreId() : "default_store",
                "terminalId", request.getTerminalId() != null ? request.getTerminalId() : "default_terminal",
                "provider", request.getProvider() != null ? request.getProvider() : "PHONEPE");

        // 3. Call Payment API
        String url = config.getApiEndpoint() + "/api/phonepe/dqr/init";
        String response = callPaymentApi(url, requestBody, config);

        // 4. Save to database
        String transactionId = savePaymentTransaction(request, response);

        return PaymentResponse.builder()
                .transactionId(transactionId)
                .status("CREATED")
                .message("Payment initiated successfully")
                .providerResponse(response)
                .build();
    }

    public PaymentResponse getPaymentStatus(String transactionId) {
        log.info("Fetching payment status for: {}", transactionId);

        Optional<PaymentGatewayTransaction> transactionOpt = paymentRepository.findByTransactionId(transactionId);

        if (transactionOpt.isEmpty()) {
            throw new ApiCallException("Transaction not found");
        }

        PaymentGatewayTransaction transaction = transactionOpt.get();

        return PaymentResponse.builder()
                .transactionId(transaction.getTransactionId())
                .gatewayOrderId(transaction.getGatewayOrderId())
                .paymentLink(transaction.getPaymentLink())
                .status(transaction.getStatus())
                .message("Payment status retrieved")
                .build();
    }

    private ThirdPartyIntegrationMaster getIntegrationConfig() {
        return integrationCacheService.getIntegration(INTEGRATION_NAME)
                .orElseThrow(() -> new ConfigurationNotFoundException(INTEGRATION_NAME));
    }

    @SuppressWarnings("null")
    private String callPaymentApi(String url, Map<String, Object> body, ThirdPartyIntegrationMaster config) {
        try {
            return webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnError(error -> log.error("Payment API call failed", error))
                    .onErrorResume(error -> Mono.error(new ApiCallException("Failed to initiate payment", error)))
                    .block();
        } catch (Exception e) {
            throw new ApiCallException("Failed to call Payment API", e);
        }
    }

    @SuppressWarnings("null")
    private String savePaymentTransaction(PaymentInitiateRequest request, String response) {
        String transactionId = UUID.randomUUID().toString();

        PaymentGatewayTransaction transaction = PaymentGatewayTransaction.builder()
                .transactionId(transactionId)
                .gatewayName(request.getProvider() != null ? request.getProvider() : "PHONEPE")
                .caseId(request.getCaseId())
                .loanAccountNumber(request.getLoanAccountNumber())
                .amount(request.getAmount())
                .currency("INR")
                .status("CREATED")
                .customerMobile(request.getCustomerMobile())
                .customerEmail(request.getCustomerEmail())
                .build();

        try {
            Map<String, Object> responseMap = objectMapper.readValue(response,
                    new TypeReference<Map<String, Object>>() {
                    });
            transaction.setGatewayResponse(responseMap);
        } catch (Exception e) {
            log.warn("Failed to parse payment response", e);
        }

        paymentRepository.save(transaction);
        return transactionId;
    }
}
