package com.finx.noticemanagementservice.repository;

import com.finx.noticemanagementservice.domain.entity.RoutingRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoutingRuleRepository extends JpaRepository<RoutingRule, Long> {

    Optional<RoutingRule> findByRuleCode(String ruleCode);

    boolean existsByRuleCode(String ruleCode);

    List<RoutingRule> findByIsActiveTrueOrderByRulePriorityDesc();

    @Query("SELECT r FROM RoutingRule r WHERE r.isActive = true " +
           "AND (r.validFrom IS NULL OR r.validFrom <= :date) " +
           "AND (r.validUntil IS NULL OR r.validUntil >= :date) " +
           "ORDER BY r.rulePriority DESC")
    List<RoutingRule> findValidRules(@Param("date") LocalDate date);

    Page<RoutingRule> findByIsActive(Boolean isActive, Pageable pageable);

    @Query("SELECT r FROM RoutingRule r WHERE " +
           "(:isActive IS NULL OR r.isActive = :isActive) AND " +
           "(:search IS NULL OR LOWER(r.ruleName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(r.ruleCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<RoutingRule> findWithFilters(
            @Param("isActive") Boolean isActive,
            @Param("search") String search,
            Pageable pageable);
}
