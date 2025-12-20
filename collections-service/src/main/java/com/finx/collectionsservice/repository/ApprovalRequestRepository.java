package com.finx.collectionsservice.repository;

import com.finx.collectionsservice.domain.entity.ApprovalRequest;
import com.finx.collectionsservice.domain.enums.ApprovalStatus;
import com.finx.collectionsservice.domain.enums.ApprovalType;
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
public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, Long> {

    Optional<ApprovalRequest> findByRequestNumber(String requestNumber);

    List<ApprovalRequest> findByCaseId(Long caseId);

    List<ApprovalRequest> findByEntityTypeAndEntityId(String entityType, Long entityId);

    List<ApprovalRequest> findByApprovalStatus(ApprovalStatus status);

    @Query("SELECT r FROM ApprovalRequest r WHERE r.approvalStatus = 'PENDING' " +
           "AND r.currentApproverRoleId = :roleId ORDER BY r.requestedAt ASC")
    Page<ApprovalRequest> findPendingByApproverRole(@Param("roleId") Long roleId, Pageable pageable);

    @Query("SELECT r FROM ApprovalRequest r WHERE r.approvalStatus = 'PENDING' " +
           "AND r.currentApproverUserId = :userId ORDER BY r.requestedAt ASC")
    Page<ApprovalRequest> findPendingByApproverUser(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT r FROM ApprovalRequest r WHERE r.requestedBy = :userId ORDER BY r.requestedAt DESC")
    Page<ApprovalRequest> findByRequester(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT r FROM ApprovalRequest r WHERE " +
           "(:type IS NULL OR r.approvalType = :type) AND " +
           "(:status IS NULL OR r.approvalStatus = :status) AND " +
           "(:caseId IS NULL OR r.caseId = :caseId)")
    Page<ApprovalRequest> findWithFilters(
            @Param("type") ApprovalType type,
            @Param("status") ApprovalStatus status,
            @Param("caseId") Long caseId,
            Pageable pageable);

    @Query("SELECT r FROM ApprovalRequest r WHERE r.approvalStatus = 'PENDING' " +
           "AND r.expiresAt < :now")
    List<ApprovalRequest> findExpiredPendingRequests(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(r) FROM ApprovalRequest r WHERE r.approvalStatus = 'PENDING' " +
           "AND r.currentApproverRoleId = :roleId")
    Long countPendingByRole(@Param("roleId") Long roleId);

    @Query("SELECT COUNT(r) FROM ApprovalRequest r WHERE r.approvalStatus = 'PENDING' " +
           "AND r.currentApproverUserId = :userId")
    Long countPendingByUser(@Param("userId") Long userId);
}
