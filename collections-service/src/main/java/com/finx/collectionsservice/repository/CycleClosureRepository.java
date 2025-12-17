package com.finx.collectionsservice.repository;

import com.finx.collectionsservice.domain.entity.CycleClosure;
import com.finx.collectionsservice.domain.enums.ClosureStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CycleClosureRepository extends JpaRepository<CycleClosure, Long> {

    List<CycleClosure> findByExecutionId(String executionId);

    Optional<CycleClosure> findByCaseId(Long caseId);

    Page<CycleClosure> findByClosureStatus(ClosureStatus status, Pageable pageable);

    @Query("SELECT c FROM CycleClosure c WHERE c.closureStatus = :status ORDER BY c.archivedAt DESC")
    List<CycleClosure> findRecentClosures(@Param("status") ClosureStatus status, Pageable pageable);

    @Query("SELECT COUNT(c) FROM CycleClosure c WHERE c.executionId = :executionId AND c.closureStatus = :status")
    Long countByExecutionIdAndStatus(@Param("executionId") String executionId, @Param("status") ClosureStatus status);

    @Query("SELECT c FROM CycleClosure c WHERE c.caseId IN :caseIds")
    List<CycleClosure> findByCaseIdIn(@Param("caseIds") List<Long> caseIds);
}
