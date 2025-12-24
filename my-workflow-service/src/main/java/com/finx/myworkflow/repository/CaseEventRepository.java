package com.finx.myworkflow.repository;

import com.finx.myworkflow.domain.entity.CaseEvent;
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
public interface CaseEventRepository extends JpaRepository<CaseEvent, Long> {

    Optional<CaseEvent> findByEventId(String eventId);

    Page<CaseEvent> findByCaseIdOrderByEventTimestampDesc(Long caseId, Pageable pageable);

    List<CaseEvent> findByCaseIdOrderByEventTimestampDesc(Long caseId);

    Page<CaseEvent> findByCaseIdAndEventCategoryOrderByEventTimestampDesc(
            Long caseId, String eventCategory, Pageable pageable);

    Page<CaseEvent> findByCaseIdAndEventTypeOrderByEventTimestampDesc(
            Long caseId, String eventType, Pageable pageable);

    @Query("SELECT e FROM CaseEvent e WHERE e.caseId = :caseId AND e.eventTimestamp BETWEEN :startDate AND :endDate ORDER BY e.eventTimestamp DESC")
    Page<CaseEvent> findByCaseIdAndDateRange(
            @Param("caseId") Long caseId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT e FROM CaseEvent e WHERE e.caseId = :caseId AND e.eventCategory IN :categories ORDER BY e.eventTimestamp DESC")
    Page<CaseEvent> findByCaseIdAndCategories(
            @Param("caseId") Long caseId,
            @Param("categories") List<String> categories,
            Pageable pageable);

    @Query("SELECT e FROM CaseEvent e WHERE e.actorId = :actorId ORDER BY e.eventTimestamp DESC")
    Page<CaseEvent> findByActorId(@Param("actorId") Long actorId, Pageable pageable);

    @Query("SELECT e FROM CaseEvent e WHERE e.loanAccountNumber = :loanAccountNumber ORDER BY e.eventTimestamp DESC")
    List<CaseEvent> findByLoanAccountNumber(@Param("loanAccountNumber") String loanAccountNumber);

    @Query("SELECT COUNT(e) FROM CaseEvent e WHERE e.caseId = :caseId AND e.eventCategory = :category")
    Long countByCaseIdAndCategory(@Param("caseId") Long caseId, @Param("category") String category);

    @Query("SELECT e.eventCategory, COUNT(e) FROM CaseEvent e WHERE e.caseId = :caseId GROUP BY e.eventCategory")
    List<Object[]> getEventCategoryStats(@Param("caseId") Long caseId);
}
