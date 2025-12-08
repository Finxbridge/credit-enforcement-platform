package com.finx.strategyengineservice.repository;

import com.finx.strategyengineservice.domain.entity.StrategyAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StrategyActionRepository extends JpaRepository<StrategyAction, Long> {

    List<StrategyAction> findByStrategyIdOrderByActionOrderAsc(Long strategyId);

    List<StrategyAction> findByStrategyIdAndIsActiveTrue(Long strategyId);

    void deleteByStrategyId(Long strategyId);

    long countByStrategyId(Long strategyId);

    List<StrategyAction> findByTemplateId(String templateId);
}
