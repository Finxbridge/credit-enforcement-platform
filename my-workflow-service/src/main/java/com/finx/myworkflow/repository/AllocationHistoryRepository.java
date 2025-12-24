package com.finx.myworkflow.repository;

import com.finx.myworkflow.domain.entity.AllocationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AllocationHistoryRepository extends JpaRepository<AllocationHistory, Long> {

    List<AllocationHistory> findByCaseIdOrderByAllocatedAtDesc(Long caseId);

    Page<AllocationHistory> findByCaseIdOrderByAllocatedAtDesc(Long caseId, Pageable pageable);

    Page<AllocationHistory> findByCaseIdAndActionOrderByAllocatedAtDesc(
            Long caseId, String action, Pageable pageable);

    @Query("SELECT h FROM AllocationHistory h WHERE h.caseId = :caseId AND h.allocatedAt BETWEEN :startDate AND :endDate ORDER BY h.allocatedAt DESC")
    Page<AllocationHistory> findByCaseIdAndDateRange(
            @Param("caseId") Long caseId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT h FROM AllocationHistory h WHERE h.allocatedToUserId = :userId ORDER BY h.allocatedAt DESC")
    Page<AllocationHistory> findByAllocatedToUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT h FROM AllocationHistory h WHERE h.agencyId = :agencyId ORDER BY h.allocatedAt DESC")
    Page<AllocationHistory> findByAgencyId(@Param("agencyId") Long agencyId, Pageable pageable);

    @Query("SELECT h FROM AllocationHistory h WHERE h.action IN :actions AND h.caseId = :caseId ORDER BY h.allocatedAt DESC")
    List<AllocationHistory> findByCaseIdAndActions(
            @Param("caseId") Long caseId,
            @Param("actions") List<String> actions);

    @Query("SELECT COUNT(h) FROM AllocationHistory h WHERE h.caseId = :caseId")
    Long countByCaseId(@Param("caseId") Long caseId);
}
