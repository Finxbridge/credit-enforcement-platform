package com.finx.collectionsservice.repository;

import com.finx.collectionsservice.domain.entity.OTSRequest;
import com.finx.collectionsservice.domain.enums.OTSStatus;
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
public interface OTSRequestRepository extends JpaRepository<OTSRequest, Long> {

    Optional<OTSRequest> findByOtsNumber(String otsNumber);

    List<OTSRequest> findByCaseIdOrderByCreatedAtDesc(Long caseId);

    Optional<OTSRequest> findFirstByCaseIdOrderByCreatedAtDesc(Long caseId);

    Page<OTSRequest> findByOtsStatus(OTSStatus status, Pageable pageable);

    @Query("SELECT o FROM OTSRequest o WHERE o.otsStatus = 'PENDING_APPROVAL' ORDER BY o.createdAt ASC")
    Page<OTSRequest> findPendingApprovals(Pageable pageable);

    @Query("SELECT o FROM OTSRequest o WHERE o.paymentDeadline < :today AND o.otsStatus = 'APPROVED'")
    List<OTSRequest> findExpiredOTSRequests(@Param("today") LocalDate today);

    @Query("SELECT o FROM OTSRequest o WHERE o.intentCapturedBy = :userId ORDER BY o.createdAt DESC")
    Page<OTSRequest> findByIntentCapturedBy(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(o) FROM OTSRequest o WHERE o.caseId = :caseId AND o.otsStatus NOT IN ('EXPIRED', 'REJECTED')")
    Long countActiveOTSByCaseId(@Param("caseId") Long caseId);

    @Query("SELECT o FROM OTSRequest o WHERE o.otsStatus = :status AND o.currentApprovalLevel = :level ORDER BY o.createdAt ASC")
    List<OTSRequest> findByStatusAndApprovalLevel(@Param("status") OTSStatus status, @Param("level") Integer level);

    // Dashboard queries
    Long countByOtsStatus(OTSStatus status);

    Long countByOtsStatusAndCreatedAtBetween(OTSStatus status, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT SUM(o.proposedSettlement) FROM OTSRequest o WHERE o.otsStatus = 'APPROVED' AND o.updatedAt BETWEEN :startDate AND :endDate")
    BigDecimal sumSettledAmountByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(o.discountAmount) FROM OTSRequest o WHERE o.otsStatus = 'APPROVED' AND o.updatedAt BETWEEN :startDate AND :endDate")
    BigDecimal sumWaiverAmountByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT AVG(o.discountPercentage) FROM OTSRequest o WHERE o.otsStatus = 'APPROVED'")
    Double averageDiscountPercentage();
}
