package com.finx.noticemanagementservice.repository;

import com.finx.noticemanagementservice.domain.entity.Notice;
import com.finx.noticemanagementservice.domain.enums.NoticeStatus;
import com.finx.noticemanagementservice.domain.enums.NoticeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    Optional<Notice> findByNoticeNumber(String noticeNumber);

    List<Notice> findByCaseId(Long caseId);

    List<Notice> findByLoanAccountNumber(String loanAccountNumber);

    Page<Notice> findByNoticeStatus(NoticeStatus status, Pageable pageable);

    Page<Notice> findByNoticeType(NoticeType type, Pageable pageable);

    Page<Notice> findByVendorId(Long vendorId, Pageable pageable);

    @Query("SELECT n FROM Notice n WHERE n.noticeStatus = :status AND n.vendorId = :vendorId")
    Page<Notice> findByStatusAndVendor(@Param("status") NoticeStatus status,
                                        @Param("vendorId") Long vendorId,
                                        Pageable pageable);

    @Query("SELECT n FROM Notice n WHERE n.dispatchSlaBreach = true")
    List<Notice> findDispatchSlaBreaches();

    @Query("SELECT n FROM Notice n WHERE n.deliverySlaBreach = true")
    List<Notice> findDeliverySlaBreaches();

    @Query("SELECT n FROM Notice n WHERE n.noticeStatus = 'IN_TRANSIT' AND n.expectedDeliveryAt < :now")
    List<Notice> findOverdueDeliveries(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(n) FROM Notice n WHERE n.noticeStatus = :status")
    Long countByStatus(@Param("status") NoticeStatus status);

    @Query("SELECT COUNT(n) FROM Notice n WHERE n.noticeType = :type")
    Long countByType(@Param("type") NoticeType type);

    @Query("SELECT COUNT(n) FROM Notice n WHERE n.dispatchSlaBreach = true")
    Long countDispatchSlaBreaches();

    @Query("SELECT COUNT(n) FROM Notice n WHERE n.deliverySlaBreach = true")
    Long countDeliverySlaBreaches();

    @Query("SELECT n FROM Notice n WHERE n.createdAt BETWEEN :startDate AND :endDate")
    Page<Notice> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate,
                                  Pageable pageable);

    @Query("SELECT n FROM Notice n WHERE n.noticeStatus = 'DISPATCHED' AND n.dispatchedAt IS NOT NULL " +
           "AND n.dispatchedAt < :thresholdTime")
    List<Notice> findPendingTransitUpdate(@Param("thresholdTime") LocalDateTime thresholdTime);

    boolean existsByNoticeNumber(String noticeNumber);
}
