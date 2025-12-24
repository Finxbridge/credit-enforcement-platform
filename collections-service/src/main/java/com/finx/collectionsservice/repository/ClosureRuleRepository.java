package com.finx.collectionsservice.repository;

import com.finx.collectionsservice.domain.entity.ClosureRule;
import com.finx.collectionsservice.domain.enums.ClosureRuleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClosureRuleRepository extends JpaRepository<ClosureRule, Long> {

    Optional<ClosureRule> findByRuleCode(String ruleCode);

    List<ClosureRule> findByIsActiveTrue();

    List<ClosureRule> findByIsActiveTrueAndIsScheduledTrue();

    List<ClosureRule> findByRuleType(ClosureRuleType ruleType);

    Page<ClosureRule> findByIsActive(Boolean isActive, Pageable pageable);

    @Query("SELECT r FROM ClosureRule r WHERE r.isActive = true ORDER BY r.priority ASC")
    List<ClosureRule> findActiveRulesOrderByPriority();

    @Query("SELECT COUNT(r) FROM ClosureRule r WHERE r.isActive = true")
    Integer countActiveRules();

    boolean existsByRuleCode(String ruleCode);

    @Query("SELECT r FROM ClosureRule r WHERE " +
           "(:searchTerm IS NULL OR r.ruleName LIKE %:searchTerm% OR r.ruleCode LIKE %:searchTerm%) AND " +
           "(:ruleType IS NULL OR r.ruleType = :ruleType) AND " +
           "(:isActive IS NULL OR r.isActive = :isActive) " +
           "ORDER BY r.priority ASC")
    Page<ClosureRule> searchRules(@Param("searchTerm") String searchTerm,
                                  @Param("ruleType") ClosureRuleType ruleType,
                                  @Param("isActive") Boolean isActive,
                                  Pageable pageable);
}
