package com.finx.noticemanagementservice.repository;

import com.finx.noticemanagementservice.domain.entity.SlaBreach;
import com.finx.noticemanagementservice.domain.enums.BreachSeverity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SlaBreachRepository extends JpaRepository<SlaBreach, Long> {

    Optional<SlaBreach> findByBreachId(String breachId);

    List<SlaBreach> findByEntityTypeAndEntityId(String entityType, Long entityId);

    List<SlaBreach> findByVendorId(Long vendorId);

    List<SlaBreach> findByIsResolvedFalse();

    List<SlaBreach> findByIsEscalatedFalseAndIsResolvedFalse();

    Page<SlaBreach> findByBreachSeverity(BreachSeverity severity, Pageable pageable);

    @Query("SELECT s FROM SlaBreach s WHERE " +
           "(:breachType IS NULL OR s.breachType = :breachType) AND " +
           "(:isResolved IS NULL OR s.isResolved = :isResolved) AND " +
           "(:isEscalated IS NULL OR s.isEscalated = :isEscalated) AND " +
           "(:severity IS NULL OR s.breachSeverity = :severity) AND " +
           "(:vendorId IS NULL OR s.vendorId = :vendorId)")
    Page<SlaBreach> findWithFilters(
            @Param("breachType") String breachType,
            @Param("isResolved") Boolean isResolved,
            @Param("isEscalated") Boolean isEscalated,
            @Param("severity") BreachSeverity severity,
            @Param("vendorId") Long vendorId,
            Pageable pageable);

    @Query("SELECT COUNT(s) FROM SlaBreach s WHERE s.isResolved = false")
    Long countUnresolvedBreaches();

    @Query("SELECT COUNT(s) FROM SlaBreach s WHERE s.isResolved = false AND s.breachSeverity = :severity")
    Long countUnresolvedBreachesBySeverity(@Param("severity") BreachSeverity severity);
}
