package com.finx.agencymanagement.repository;

import com.finx.agencymanagement.domain.entity.AgencyAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Agency Audit Log Repository
 *
 * @author Naveen Manyam
 * @version 1.0.0
 */
@Repository
public interface AgencyAuditLogRepository extends JpaRepository<AgencyAuditLog, Long> {

    List<AgencyAuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);

    Page<AgencyAuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId, Pageable pageable);

    List<AgencyAuditLog> findByEventTypeOrderByCreatedAtDesc(String eventType);

    Page<AgencyAuditLog> findByEventTypeOrderByCreatedAtDesc(String eventType, Pageable pageable);

    List<AgencyAuditLog> findByActorIdOrderByCreatedAtDesc(Long actorId);

    Page<AgencyAuditLog> findByActorIdOrderByCreatedAtDesc(Long actorId, Pageable pageable);

    List<AgencyAuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);

    Page<AgencyAuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
