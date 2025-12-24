package com.finx.collectionsservice.repository;

import com.finx.collectionsservice.domain.entity.Repayment;
import com.finx.collectionsservice.domain.enums.RepaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepaymentRepository extends JpaRepository<Repayment, Long> {

    Optional<Repayment> findByRepaymentNumber(String repaymentNumber);

    List<Repayment> findByCaseIdOrderByPaymentDateDesc(Long caseId);

    Page<Repayment> findByCaseIdOrderByPaymentDateDesc(Long caseId, Pageable pageable);

    Page<Repayment> findByApprovalStatus(RepaymentStatus status, Pageable pageable);

    @Query("SELECT r FROM Repayment r WHERE r.approvalStatus = 'PENDING' ORDER BY r.createdAt ASC")
    Page<Repayment> findPendingApprovals(Pageable pageable);

    @Query("SELECT r FROM Repayment r WHERE r.collectedBy = :userId ORDER BY r.paymentDate DESC")
    Page<Repayment> findByCollector(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT SUM(r.paymentAmount) FROM Repayment r WHERE r.caseId = :caseId AND r.approvalStatus = 'APPROVED'")
    BigDecimal sumApprovedPaymentsByCaseId(@Param("caseId") Long caseId);

    @Query("SELECT r FROM Repayment r WHERE r.paymentDate BETWEEN :startDate AND :endDate ORDER BY r.paymentDate DESC")
    List<Repayment> findByPaymentDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // SLA breached: either marked as BREACHED or deposit overdue (depositRequiredBy < now and still PENDING)
    @Query("SELECT r FROM Repayment r WHERE " +
           "(r.depositSlaStatus = 'BREACHED' OR (r.depositRequiredBy < CURRENT_TIMESTAMP AND r.approvalStatus = 'PENDING' AND r.depositedAt IS NULL)) " +
           "ORDER BY r.depositRequiredBy ASC")
    List<Repayment> findSlaBreachedRepayments();

    @Query("SELECT r FROM Repayment r WHERE r.otsId = :otsId ORDER BY r.paymentDate")
    List<Repayment> findByOtsId(@Param("otsId") Long otsId);

    @Query("SELECT COUNT(r) FROM Repayment r WHERE r.caseId = :caseId")
    Long countByCaseId(@Param("caseId") Long caseId);

    // Dashboard queries
    @Query("SELECT SUM(r.paymentAmount) FROM Repayment r WHERE r.createdAt BETWEEN :startDate AND :endDate AND r.approvalStatus = 'APPROVED'")
    BigDecimal sumPaymentAmountByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(r) FROM Repayment r WHERE r.createdAt BETWEEN :startDate AND :endDate")
    Long countByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    Long countByApprovalStatus(RepaymentStatus status);

    @Query("SELECT r.paymentMode, SUM(r.paymentAmount) FROM Repayment r WHERE r.createdAt BETWEEN :startDate AND :endDate AND r.approvalStatus = 'APPROVED' GROUP BY r.paymentMode")
    List<Object[]> sumByPaymentMode(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Dashboard and Reconciliation queries
    List<Repayment> findByPaymentDate(LocalDate paymentDate);

    List<Repayment> findByPaymentDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT COUNT(r) FROM Repayment r WHERE r.depositSlaStatus = 'BREACHED'")
    long countSlaBreached();

    // Count SLA breached or overdue (deposit time passed but not yet deposited)
    @Query("SELECT COUNT(r) FROM Repayment r WHERE " +
           "r.depositSlaStatus = 'BREACHED' OR " +
           "(r.depositRequiredBy < CURRENT_TIMESTAMP AND r.approvalStatus = 'PENDING' AND r.depositedAt IS NULL)")
    long countSlaBreachedOrOverdue();

    long countByIsReconciledFalse();

    // Count pending reconciliation (approved but not reconciled)
    @Query("SELECT COUNT(r) FROM Repayment r WHERE r.approvalStatus = 'APPROVED' AND (r.isReconciled = false OR r.isReconciled IS NULL)")
    long countPendingReconciliation();

    Page<Repayment> findByIsReconciledFalse(Pageable pageable);

    // Search with filters
    @Query("SELECT r FROM Repayment r WHERE " +
           "(:searchTerm IS NULL OR r.repaymentNumber LIKE %:searchTerm% OR r.loanAccountNumber LIKE %:searchTerm%) AND " +
           "(:status IS NULL OR r.approvalStatus = :status) AND " +
           "(:fromDate IS NULL OR r.paymentDate >= :fromDate) AND " +
           "(:toDate IS NULL OR r.paymentDate <= :toDate) " +
           "ORDER BY r.createdAt DESC")
    Page<Repayment> searchRepayments(@Param("searchTerm") String searchTerm,
                                     @Param("status") RepaymentStatus status,
                                     @Param("fromDate") LocalDate fromDate,
                                     @Param("toDate") LocalDate toDate,
                                     Pageable pageable);
}
