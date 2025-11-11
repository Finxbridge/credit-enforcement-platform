package com.finx.allocationreallocationservice.repository;

import com.finx.allocationreallocationservice.domain.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByEntityIdAndEntityType(Long entityId, String entityType);
}
