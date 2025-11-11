package com.finx.strategyengineservice.repository;

import com.finx.strategyengineservice.domain.entity.StrategyRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StrategyRuleRepository extends JpaRepository<StrategyRule, Long> {

    List<StrategyRule> findByStrategyIdOrderByRuleOrderAsc(Long strategyId);

    List<StrategyRule> findByStrategyIdAndIsActiveTrue(Long strategyId);

    void deleteByStrategyId(Long strategyId);

    long countByStrategyId(Long strategyId);
}
