package com.finx.strategyengineservice.repository;

import com.finx.strategyengineservice.domain.entity.StrategyExecution;
import com.finx.strategyengineservice.domain.enums.ExecutionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StrategyExecutionRepository extends JpaRepository<StrategyExecution, Long> {

    Optional<StrategyExecution> findByExecutionId(String executionId);

    List<StrategyExecution> findByStrategyIdOrderByStartedAtDesc(Long strategyId);

    Page<StrategyExecution> findByStrategyId(Long strategyId, Pageable pageable);

    List<StrategyExecution> findByStatus(ExecutionStatus status);

    Page<StrategyExecution> findAllByOrderByStartedAtDesc(Pageable pageable);

    @Query("SELECT se FROM StrategyExecution se WHERE se.status = :status AND se.startedAt < :threshold")
    List<StrategyExecution> findStuckExecutions(ExecutionStatus status, LocalDateTime threshold);

    long countByStrategyIdAndStatus(Long strategyId, ExecutionStatus status);

    @Query("SELECT COUNT(se) FROM StrategyExecution se WHERE se.strategyId = :strategyId " +
           "AND se.startedAt >= :startDate AND se.startedAt <= :endDate")
    long countExecutionsInDateRange(Long strategyId, LocalDateTime startDate, LocalDateTime endDate);
}
