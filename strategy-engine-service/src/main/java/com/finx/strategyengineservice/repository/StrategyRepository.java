package com.finx.strategyengineservice.repository;

import com.finx.strategyengineservice.domain.entity.Strategy;
import com.finx.strategyengineservice.domain.enums.StrategyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StrategyRepository extends JpaRepository<Strategy, Long> {

    List<Strategy> findByStatus(StrategyStatus status);

    List<Strategy> findByStatusOrderByPriorityDesc(StrategyStatus status);

    Optional<Strategy> findByIdAndStatus(Long id, StrategyStatus status);

    @Query("SELECT s FROM Strategy s LEFT JOIN FETCH s.rules LEFT JOIN FETCH s.actions WHERE s.id = :id")
    Optional<Strategy> findByIdWithRulesAndActions(Long id);

    @Query("SELECT COUNT(s) FROM Strategy s WHERE s.status = :status")
    long countByStatus(StrategyStatus status);

    boolean existsByStrategyName(String strategyName);

    Optional<Strategy> findByStrategyName(String strategyName);

    Optional<Strategy> findByStrategyCode(String strategyCode);
}
