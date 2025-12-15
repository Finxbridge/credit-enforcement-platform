package com.finx.dmsservice.repository;

import com.finx.dmsservice.domain.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByEntityTypeAndEntityId(String entityType, String entityId, Pageable pageable);

    Page<AuditLog> findByActorId(Long actorId, Pageable pageable);

    Page<AuditLog> findByEventType(String eventType, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.eventTimestamp BETWEEN :startDate AND :endDate ORDER BY a.eventTimestamp DESC")
    Page<AuditLog> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    List<AuditLog> findByEntityTypeAndEntityIdOrderByEventTimestampDesc(String entityType, String entityId);
}
