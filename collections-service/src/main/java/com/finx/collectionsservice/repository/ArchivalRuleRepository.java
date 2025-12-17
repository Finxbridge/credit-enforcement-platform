package com.finx.collectionsservice.repository;

import com.finx.collectionsservice.domain.entity.ArchivalRule;
import com.finx.collectionsservice.domain.enums.RuleStatus;
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
public interface ArchivalRuleRepository extends JpaRepository<ArchivalRule, Long> {

    Optional<ArchivalRule> findByRuleCode(String ruleCode);

    List<ArchivalRule> findByIsActiveTrue();

    List<ArchivalRule> findByStatus(RuleStatus status);

    Page<ArchivalRule> findByStatus(RuleStatus status, Pageable pageable);

    @Query("SELECT r FROM ArchivalRule r WHERE r.isActive = true AND r.status = 'ACTIVE' ORDER BY r.createdAt DESC")
    List<ArchivalRule> findActiveRules();

    @Query("SELECT r FROM ArchivalRule r WHERE r.nextExecutionAt <= :dateTime AND r.isActive = true AND r.status = 'ACTIVE'")
    List<ArchivalRule> findRulesDueForExecution(@Param("dateTime") LocalDateTime dateTime);

    boolean existsByRuleCode(String ruleCode);

    @Query("SELECT COUNT(r) FROM ArchivalRule r WHERE r.isActive = true")
    Long countActiveRules();

    @Query("SELECT SUM(r.totalCasesArchived) FROM ArchivalRule r")
    Long sumTotalCasesArchived();
}
