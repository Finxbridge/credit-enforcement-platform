package com.finx.communication.domain.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entity for tracking Payment Gateway transactions
 */
@Entity
@Table(name = "payment_gateway_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentGatewayTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", unique = true, nullable = false, length = 100)
    private String transactionId;

    @Column(name = "gateway_order_id", length = 100)
    private String gatewayOrderId;

    @Column(name = "gateway_payment_id", length = 100)
    private String gatewayPaymentId;

    @Builder.Default
    @Column(name = "gateway_name", length = 50)
    private String gatewayName = "RAZORPAY";

    @Column(name = "case_id")
    private Long caseId;

    @Column(name = "loan_account_number", length = 50)
    private String loanAccountNumber;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Builder.Default
    @Column(name = "currency", length = 10)
    private String currency = "INR";

    @Column(name = "payment_method", length = 50)
    private String paymentMethod; // UPI, CARD, NETBANKING, WALLET

    @Column(name = "payment_link", columnDefinition = "TEXT")
    private String paymentLink;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 50)
    private String status = "CREATED"; // CREATED, PENDING, SUCCESS, FAILED, REFUNDED

    @Column(name = "customer_mobile", length = 15)
    private String customerMobile;

    @Column(name = "customer_email", length = 100)
    private String customerEmail;

    @Column(name = "payment_received_at")
    private LocalDateTime paymentReceivedAt;

    @Column(name = "refund_amount", precision = 15, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Type(JsonType.class)
    @Column(name = "gateway_response", columnDefinition = "jsonb")
    private Map<String, Object> gatewayResponse;

    @Builder.Default
    @Column(name = "webhook_received")
    private Boolean webhookReceived = false;

    @Column(name = "webhook_received_at")
    private LocalDateTime webhookReceivedAt;

    @Builder.Default
    @Column(name = "reconciled")
    private Boolean reconciled = false;

    @Column(name = "reconciled_at")
    private LocalDateTime reconciledAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
