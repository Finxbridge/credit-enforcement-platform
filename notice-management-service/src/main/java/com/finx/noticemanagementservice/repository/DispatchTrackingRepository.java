package com.finx.noticemanagementservice.repository;

import com.finx.noticemanagementservice.domain.entity.DispatchTracking;
import com.finx.noticemanagementservice.domain.enums.DispatchStatus;
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
public interface DispatchTrackingRepository extends JpaRepository<DispatchTracking, Long> {

    Optional<DispatchTracking> findByTrackingId(String trackingId);

    Optional<DispatchTracking> findByTrackingNumber(String trackingNumber);

    List<DispatchTracking> findByNoticeId(Long noticeId);

    List<DispatchTracking> findByVendorId(Long vendorId);

    List<DispatchTracking> findByDispatchStatus(DispatchStatus status);

    @Query("SELECT d FROM DispatchTracking d WHERE d.dispatchSlaBreached = true OR d.deliverySlaBreached = true")
    List<DispatchTracking> findSlaBreached();

    @Query("SELECT d FROM DispatchTracking d WHERE " +
           "(:status IS NULL OR d.dispatchStatus = :status) AND " +
           "(:vendorId IS NULL OR d.vendor.id = :vendorId) AND " +
           "(:slaBreached IS NULL OR d.dispatchSlaBreached = :slaBreached OR d.deliverySlaBreached = :slaBreached)")
    Page<DispatchTracking> findWithFilters(
            @Param("status") DispatchStatus status,
            @Param("vendorId") Long vendorId,
            @Param("slaBreached") Boolean slaBreached,
            Pageable pageable);

    @Query("SELECT d FROM DispatchTracking d WHERE " +
           "d.dispatchStatus = 'PENDING' AND " +
           "d.expectedDispatchBy < :now AND " +
           "d.dispatchSlaBreached = false")
    List<DispatchTracking> findPendingDispatchSlaBreaches(@Param("now") LocalDateTime now);

    @Query("SELECT d FROM DispatchTracking d WHERE " +
           "d.dispatchStatus NOT IN ('DELIVERED', 'RTO', 'FAILED') AND " +
           "d.expectedDeliveryBy < :now AND " +
           "d.deliverySlaBreached = false")
    List<DispatchTracking> findPendingDeliverySlaBreaches(@Param("now") LocalDateTime now);

    // Vendor Portal Queries
    @Query("SELECT d FROM DispatchTracking d WHERE d.vendor.id = :vendorId AND " +
           "(:status IS NULL OR d.dispatchStatus = :status) AND " +
           "(:slaBreached IS NULL OR d.dispatchSlaBreached = :slaBreached OR d.deliverySlaBreached = :slaBreached) AND " +
           "(:pincode IS NULL OR d.notice.recipientPincode = :pincode)")
    Page<DispatchTracking> findByVendorWithFilters(
            @Param("vendorId") Long vendorId,
            @Param("status") DispatchStatus status,
            @Param("slaBreached") Boolean slaBreached,
            @Param("pincode") String pincode,
            Pageable pageable);

    @Query("SELECT d FROM DispatchTracking d WHERE d.vendor.id = :vendorId AND " +
           "(d.notice.noticeNumber LIKE %:searchTerm% OR " +
           "d.notice.customerName LIKE %:searchTerm% OR " +
           "d.notice.loanAccountNumber LIKE %:searchTerm% OR " +
           "d.trackingNumber LIKE %:searchTerm%)")
    Page<DispatchTracking> searchByVendor(
            @Param("vendorId") Long vendorId,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

    // Count queries for dashboard
    @Query("SELECT COUNT(d) FROM DispatchTracking d WHERE d.vendor.id = :vendorId AND d.dispatchStatus = :status")
    Long countByVendorAndStatus(@Param("vendorId") Long vendorId, @Param("status") DispatchStatus status);

    @Query("SELECT COUNT(d) FROM DispatchTracking d WHERE d.vendor.id = :vendorId AND d.dispatchStatus = :status " +
           "AND d.createdAt >= :startOfDay")
    Long countByVendorAndStatusToday(
            @Param("vendorId") Long vendorId,
            @Param("status") DispatchStatus status,
            @Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT COUNT(d) FROM DispatchTracking d WHERE d.vendor.id = :vendorId AND " +
           "(d.dispatchSlaBreached = true OR d.deliverySlaBreached = true)")
    Long countSlaBreachedByVendor(@Param("vendorId") Long vendorId);

    @Query("SELECT d FROM DispatchTracking d WHERE d.vendor.id = :vendorId AND " +
           "d.dispatchStatus NOT IN ('DELIVERED', 'RTO', 'FAILED') AND " +
           "d.dispatchSlaBreached = false AND d.deliverySlaBreached = false AND " +
           "(d.expectedDispatchBy <= :threshold OR d.expectedDeliveryBy <= :threshold)")
    Page<DispatchTracking> findSlaAtRiskByVendor(
            @Param("vendorId") Long vendorId,
            @Param("threshold") LocalDateTime threshold,
            Pageable pageable);

    @Query("SELECT COUNT(d) FROM DispatchTracking d WHERE d.vendor.id = :vendorId AND " +
           "d.dispatchStatus NOT IN ('DELIVERED', 'RTO', 'FAILED') AND " +
           "d.dispatchSlaBreached = false AND d.deliverySlaBreached = false AND " +
           "(d.expectedDispatchBy <= :threshold OR d.expectedDeliveryBy <= :threshold)")
    Long countSlaAtRiskByVendor(@Param("vendorId") Long vendorId, @Param("threshold") LocalDateTime threshold);

    @Query("SELECT COUNT(d) FROM DispatchTracking d WHERE d.vendor.id = :vendorId AND d.dispatchStatus = 'DELIVERED'")
    Long countDeliveredByVendor(@Param("vendorId") Long vendorId);

    @Query("SELECT COUNT(d) FROM DispatchTracking d WHERE d.vendor.id = :vendorId")
    Long countTotalByVendor(@Param("vendorId") Long vendorId);

    List<DispatchTracking> findByVendorIdAndIdIn(Long vendorId, List<Long> ids);

    Optional<DispatchTracking> findByIdAndVendorId(Long id, Long vendorId);
}
