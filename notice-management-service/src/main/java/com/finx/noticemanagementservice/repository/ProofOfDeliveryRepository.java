package com.finx.noticemanagementservice.repository;

import com.finx.noticemanagementservice.domain.entity.ProofOfDelivery;
import com.finx.noticemanagementservice.domain.enums.PodVerificationStatus;
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
public interface ProofOfDeliveryRepository extends JpaRepository<ProofOfDelivery, Long> {

    Optional<ProofOfDelivery> findByPodNumber(String podNumber);

    Optional<ProofOfDelivery> findByNoticeId(Long noticeId);

    List<ProofOfDelivery> findByVendorId(Long vendorId);

    Page<ProofOfDelivery> findByVerificationStatus(PodVerificationStatus status, Pageable pageable);

    @Query("SELECT p FROM ProofOfDelivery p WHERE p.verificationStatus = 'PENDING'")
    List<ProofOfDelivery> findPendingVerifications();

    @Query("SELECT p FROM ProofOfDelivery p WHERE p.uploadedAt BETWEEN :startDate AND :endDate")
    Page<ProofOfDelivery> findByUploadDateRange(@Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate,
                                                  Pageable pageable);

    @Query("SELECT p FROM ProofOfDelivery p WHERE p.deliveredAt BETWEEN :startDate AND :endDate")
    Page<ProofOfDelivery> findByDeliveryDateRange(@Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate,
                                                    Pageable pageable);

    @Query("SELECT COUNT(p) FROM ProofOfDelivery p WHERE p.verificationStatus = :status")
    Long countByVerificationStatus(@Param("status") PodVerificationStatus status);

    @Query("SELECT p FROM ProofOfDelivery p WHERE p.verifiedBy = :userId")
    List<ProofOfDelivery> findByVerifier(@Param("userId") Long userId);

    boolean existsByPodNumber(String podNumber);

    boolean existsByNoticeId(Long noticeId);
}
