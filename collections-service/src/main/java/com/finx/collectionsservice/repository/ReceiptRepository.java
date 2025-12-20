package com.finx.collectionsservice.repository;

import com.finx.collectionsservice.domain.entity.Receipt;
import com.finx.collectionsservice.domain.enums.PaymentMode;
import com.finx.collectionsservice.domain.enums.ReceiptStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long>, JpaSpecificationExecutor<Receipt> {

    Optional<Receipt> findByReceiptNumber(String receiptNumber);

    Optional<Receipt> findByRepaymentId(Long repaymentId);

    List<Receipt> findByCaseId(Long caseId);

    Page<Receipt> findByCaseId(Long caseId, Pageable pageable);

    List<Receipt> findByLoanAccountNumber(String loanAccountNumber);

    Page<Receipt> findByStatus(ReceiptStatus status, Pageable pageable);

    Page<Receipt> findByPaymentMode(PaymentMode paymentMode, Pageable pageable);

    Page<Receipt> findByGeneratedBy(Long generatedBy, Pageable pageable);

    @Query("SELECT r FROM Receipt r WHERE r.generatedAt BETWEEN :startDate AND :endDate")
    Page<Receipt> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate,
                                   Pageable pageable);

    @Query("SELECT r FROM Receipt r WHERE r.status = :status " +
           "AND r.generatedAt BETWEEN :startDate AND :endDate")
    Page<Receipt> findByStatusAndDateRange(@Param("status") ReceiptStatus status,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate,
                                            Pageable pageable);

    @Query("SELECT COUNT(r) FROM Receipt r WHERE r.caseId = :caseId")
    Long countByCaseId(@Param("caseId") Long caseId);

    @Query("SELECT COUNT(r) FROM Receipt r WHERE r.status = :status")
    Long countByStatus(@Param("status") ReceiptStatus status);

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Receipt r WHERE r.status NOT IN ('CANCELLED', 'VOID')")
    BigDecimal sumTotalAmount();

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Receipt r " +
           "WHERE r.status NOT IN ('CANCELLED', 'VOID') " +
           "AND r.generatedAt >= :since")
    BigDecimal sumAmountSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(r) FROM Receipt r WHERE r.generatedAt >= :since")
    Long countSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(r) FROM Receipt r WHERE r.paymentMode = :paymentMode " +
           "AND r.status NOT IN ('CANCELLED', 'VOID')")
    Long countByPaymentMode(@Param("paymentMode") PaymentMode paymentMode);

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Receipt r " +
           "WHERE r.paymentMode = :paymentMode AND r.status NOT IN ('CANCELLED', 'VOID')")
    BigDecimal sumAmountByPaymentMode(@Param("paymentMode") PaymentMode paymentMode);

    @Query("SELECT r FROM Receipt r WHERE r.emailedAt IS NOT NULL ORDER BY r.emailedAt DESC")
    Page<Receipt> findEmailedReceipts(Pageable pageable);

    @Query("SELECT r FROM Receipt r WHERE r.downloadedAt IS NOT NULL ORDER BY r.downloadedAt DESC")
    Page<Receipt> findDownloadedReceipts(Pageable pageable);

    boolean existsByReceiptNumber(String receiptNumber);

    boolean existsByRepaymentId(Long repaymentId);

    List<Receipt> findByRepaymentIdIn(List<Long> repaymentIds);
}
