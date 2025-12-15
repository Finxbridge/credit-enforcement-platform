package com.finx.dmsservice.repository;

import com.finx.dmsservice.domain.entity.NoticeDocument;
import com.finx.dmsservice.domain.enums.NoticeStatus;
import com.finx.dmsservice.domain.enums.NoticeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NoticeDocumentRepository extends JpaRepository<NoticeDocument, Long>, JpaSpecificationExecutor<NoticeDocument> {

    Optional<NoticeDocument> findByNoticeNumber(String noticeNumber);

    List<NoticeDocument> findByCaseId(Long caseId);

    List<NoticeDocument> findByLoanAccountNumber(String loanAccountNumber);

    List<NoticeDocument> findByCustomerId(Long customerId);

    Page<NoticeDocument> findByNoticeType(NoticeType noticeType, Pageable pageable);

    Page<NoticeDocument> findByNoticeStatus(NoticeStatus noticeStatus, Pageable pageable);

    Page<NoticeDocument> findByNoticeTypeAndNoticeStatus(NoticeType noticeType, NoticeStatus status, Pageable pageable);

    Page<NoticeDocument> findByRegion(String region, Pageable pageable);

    Page<NoticeDocument> findByProductType(String productType, Pageable pageable);

    Page<NoticeDocument> findByDispatchVendorId(Long vendorId, Pageable pageable);

    // Date range queries
    @Query("SELECT n FROM NoticeDocument n WHERE n.generatedAt BETWEEN :startDate AND :endDate")
    Page<NoticeDocument> findByGeneratedDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT n FROM NoticeDocument n WHERE n.dispatchedAt BETWEEN :startDate AND :endDate")
    Page<NoticeDocument> findByDispatchedDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT n FROM NoticeDocument n WHERE n.deliveredAt BETWEEN :startDate AND :endDate")
    Page<NoticeDocument> findByDeliveredDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // DPD range queries
    @Query("SELECT n FROM NoticeDocument n WHERE n.dpd BETWEEN :minDpd AND :maxDpd")
    Page<NoticeDocument> findByDpdRange(
            @Param("minDpd") Integer minDpd,
            @Param("maxDpd") Integer maxDpd,
            Pageable pageable);

    // Amount range queries
    @Query("SELECT n FROM NoticeDocument n WHERE n.totalDues BETWEEN :minAmount AND :maxAmount")
    Page<NoticeDocument> findByAmountRange(
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            Pageable pageable);

    // Count queries
    @Query("SELECT COUNT(n) FROM NoticeDocument n WHERE n.noticeStatus = :status")
    Long countByStatus(@Param("status") NoticeStatus status);

    @Query("SELECT COUNT(n) FROM NoticeDocument n WHERE n.noticeType = :type")
    Long countByType(@Param("type") NoticeType type);

    @Query("SELECT COUNT(n) FROM NoticeDocument n WHERE n.generatedAt >= :since")
    Long countGeneratedSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(n) FROM NoticeDocument n WHERE n.dispatchedAt >= :since")
    Long countDispatchedSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(n) FROM NoticeDocument n WHERE n.deliveredAt >= :since")
    Long countDeliveredSince(@Param("since") LocalDateTime since);

    // DPD distribution counts
    @Query("SELECT COUNT(n) FROM NoticeDocument n WHERE n.dpd < 30")
    Long countDpdUnder30();

    @Query("SELECT COUNT(n) FROM NoticeDocument n WHERE n.dpd >= 30 AND n.dpd < 60")
    Long countDpd30to60();

    @Query("SELECT COUNT(n) FROM NoticeDocument n WHERE n.dpd >= 60 AND n.dpd < 90")
    Long countDpd60to90();

    @Query("SELECT COUNT(n) FROM NoticeDocument n WHERE n.dpd >= 90 AND n.dpd < 180")
    Long countDpd90to180();

    @Query("SELECT COUNT(n) FROM NoticeDocument n WHERE n.dpd >= 180")
    Long countDpdOver180();

    // Amount aggregations
    @Query("SELECT COALESCE(SUM(n.totalDues), 0) FROM NoticeDocument n WHERE n.noticeStatus NOT IN ('CANCELLED', 'FAILED')")
    BigDecimal sumTotalDues();

    @Query("SELECT COALESCE(AVG(n.totalDues), 0) FROM NoticeDocument n WHERE n.noticeStatus NOT IN ('CANCELLED', 'FAILED')")
    BigDecimal avgTotalDues();

    // Region counts
    @Query("SELECT COUNT(n) FROM NoticeDocument n WHERE n.region = :region")
    Long countByRegion(@Param("region") String region);

    // Overdue response queries
    @Query("SELECT n FROM NoticeDocument n WHERE n.responseDueDate < :today AND n.responseReceivedAt IS NULL AND n.noticeStatus = 'DELIVERED'")
    Page<NoticeDocument> findOverdueResponses(@Param("today") LocalDate today, Pageable pageable);

    @Query("SELECT COUNT(n) FROM NoticeDocument n WHERE n.responseDueDate < :today AND n.responseReceivedAt IS NULL AND n.noticeStatus = 'DELIVERED'")
    Long countOverdueResponses(@Param("today") LocalDate today);

    // Pending delivery
    @Query("SELECT COUNT(n) FROM NoticeDocument n WHERE n.noticeStatus IN ('GENERATED', 'DISPATCHED')")
    Long countPendingDelivery();

    // Version queries
    @Query("SELECT n FROM NoticeDocument n WHERE n.parentNoticeId = :parentId ORDER BY n.versionNumber DESC")
    List<NoticeDocument> findVersionsByParentId(@Param("parentId") Long parentId);

    // Delivery success rate calculation
    @Query("SELECT COUNT(n) FROM NoticeDocument n WHERE n.noticeStatus = 'DELIVERED'")
    Long countDelivered();

    @Query("SELECT COUNT(n) FROM NoticeDocument n WHERE n.noticeStatus IN ('DISPATCHED', 'DELIVERED', 'RETURNED', 'FAILED')")
    Long countTotalDispatched();

    boolean existsByNoticeNumber(String noticeNumber);
}
