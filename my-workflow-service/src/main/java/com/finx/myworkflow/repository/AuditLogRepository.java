package com.finx.myworkflow.repository;

import com.finx.myworkflow.domain.entity.AuditLog;
import com.finx.myworkflow.domain.enums.AuditAction;
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
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Optional<AuditLog> findByAuditId(String auditId);

    Page<AuditLog> findByCaseIdOrderByCreatedAtDesc(Long caseId, Pageable pageable);

    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            String entityType, Long entityId, Pageable pageable);

    Page<AuditLog> findByActionOrderByCreatedAtDesc(AuditAction action, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    Page<AuditLog> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.caseId = :caseId AND a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    List<AuditLog> findCaseAuditTrail(
            @Param("caseId") Long caseId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM AuditLog a WHERE a.userId = :userId AND a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<AuditLog> findRecentUserActivity(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.userId = :userId AND a.action = :action AND a.createdAt >= :since")
    Long countUserActionsSince(@Param("userId") Long userId, @Param("action") AuditAction action, @Param("since") LocalDateTime since);

    @Query("SELECT a.action, COUNT(a) FROM AuditLog a WHERE a.createdAt >= :since GROUP BY a.action")
    List<Object[]> getActionStatistics(@Param("since") LocalDateTime since);
}
