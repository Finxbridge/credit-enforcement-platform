package com.finx.communication.repository;

import com.finx.communication.domain.entity.PaymentGatewayTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentGatewayTransactionRepository extends JpaRepository<PaymentGatewayTransaction, Long> {

    Optional<PaymentGatewayTransaction> findByTransactionId(String transactionId);

    Optional<PaymentGatewayTransaction> findByGatewayOrderId(String gatewayOrderId);

    Optional<PaymentGatewayTransaction> findByGatewayPaymentId(String gatewayPaymentId);

    List<PaymentGatewayTransaction> findByCaseId(Long caseId);

    List<PaymentGatewayTransaction> findByStatus(String status);

    List<PaymentGatewayTransaction> findByReconciledFalse();
}
