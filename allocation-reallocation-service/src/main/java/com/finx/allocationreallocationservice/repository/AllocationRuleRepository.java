package com.finx.allocationreallocationservice.repository;

import com.finx.allocationreallocationservice.domain.entity.AllocationRule;
import com.finx.allocationreallocationservice.domain.enums.RuleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllocationRuleRepository extends JpaRepository<AllocationRule, Long> {

    List<AllocationRule> findByStatus(RuleStatus status);

    List<AllocationRule> findByStatusOrderByPriorityAsc(RuleStatus status);
}
