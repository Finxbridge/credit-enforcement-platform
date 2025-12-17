package com.finx.collectionsservice.service;

import com.finx.collectionsservice.domain.dto.payment.*;

/**
 * Unified payment integration service
 * Routes to appropriate communication-service API based on service type
 */
public interface PaymentIntegrationService {

    /**
     * Initiate payment based on service type (DYNAMIC_QR, PAYMENT_LINK, COLLECT_CALL)
     */
    PaymentResponse initiatePayment(PaymentInitRequest request);

    /**
     * Check payment status
     */
    PaymentResponse checkStatus(PaymentStatusRequest request);

    /**
     * Cancel a payment transaction
     */
    PaymentResponse cancelPayment(PaymentCancelRequest request);

    /**
     * Refund a completed payment
     */
    PaymentResponse refundPayment(PaymentRefundRequest request);

    /**
     * Get transaction details by ID
     */
    PaymentResponse getTransaction(String transactionId);

    /**
     * Get all transactions for a case
     */
    java.util.List<PaymentResponse> getTransactionsByCase(Long caseId);

    /**
     * Generate receipt for a successful payment transaction
     * Creates a repayment record and generates a receipt
     */
    com.finx.collectionsservice.domain.dto.RepaymentDTO generateReceiptForTransaction(String transactionId, Long userId);

    /**
     * Download receipt PDF for a payment transaction
     */
    byte[] downloadReceiptForTransaction(String transactionId, Long userId);
}
