package com.finx.collectionsservice.repository;

import com.finx.collectionsservice.domain.entity.ClosureRuleExecution;
import com.finx.collectionsservice.domain.enums.RuleExecutionStatus;
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
public interface ClosureRuleExecutionRepository extends JpaRepository<ClosureRuleExecution, Long> {

    Optional<ClosureRuleExecution> findByExecutionId(String executionId);

    List<ClosureRuleExecution> findByRuleIdOrderByCreatedAtDesc(Long ruleId);

    Page<ClosureRuleExecution> findByRuleIdOrderByCreatedAtDesc(Long ruleId, Pageable pageable);

    List<ClosureRuleExecution> findByStatus(RuleExecutionStatus status);

    @Query("SELECT e FROM ClosureRuleExecution e WHERE e.isSimulation = false ORDER BY e.createdAt DESC")
    List<ClosureRuleExecution> findRecentExecutions(Pageable pageable);

    @Query("SELECT e FROM ClosureRuleExecution e WHERE e.createdAt >= :since ORDER BY e.createdAt DESC")
    List<ClosureRuleExecution> findExecutionsSince(@Param("since") LocalDateTime since);

    @Query("SELECT SUM(e.totalSuccess) FROM ClosureRuleExecution e WHERE e.isSimulation = false AND e.status = 'COMPLETED'")
    Long sumTotalSuccessfulClosures();

    @Query("SELECT SUM(e.totalSuccess) FROM ClosureRuleExecution e WHERE e.isSimulation = false AND e.status = 'COMPLETED' AND e.createdAt >= :since")
    Long sumSuccessfulClosuresSince(@Param("since") LocalDateTime since);

    @Query("SELECT SUM(e.totalFailed) FROM ClosureRuleExecution e WHERE e.isSimulation = false")
    Long sumTotalFailedClosures();

    @Query("SELECT COUNT(e) FROM ClosureRuleExecution e WHERE e.status = 'PENDING' OR e.status = 'RUNNING'")
    Long countPendingExecutions();
}
