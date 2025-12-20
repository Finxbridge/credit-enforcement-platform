package com.finx.myworkflow.repository;

import com.finx.myworkflow.domain.entity.Case;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for workflow case queries
 * Fetches only allocated cases with required fields for workflow display
 */
@Repository
public interface WorkflowCaseRepository extends JpaRepository<Case, Long> {

    /**
     * Get all allocated cases for admin view
     * Cases where allocatedToUserId is not null (allocated cases only)
     */
    @Query("SELECT c FROM Case c " +
           "JOIN FETCH c.loan l " +
           "JOIN FETCH l.primaryCustomer pc " +
           "WHERE c.allocatedToUserId IS NOT NULL " +
           "ORDER BY c.updatedAt DESC")
    Page<Case> findAllAllocatedCases(Pageable pageable);

    /**
     * Get allocated cases for a specific agent/collector
     */
    @Query("SELECT c FROM Case c " +
           "JOIN FETCH c.loan l " +
           "JOIN FETCH l.primaryCustomer pc " +
           "WHERE c.allocatedToUserId = :userId " +
           "ORDER BY c.updatedAt DESC")
    Page<Case> findAllocatedCasesByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Count all allocated cases
     */
    @Query("SELECT COUNT(c) FROM Case c WHERE c.allocatedToUserId IS NOT NULL")
    long countAllAllocatedCases();

    /**
     * Count allocated cases for a specific user
     */
    @Query("SELECT COUNT(c) FROM Case c WHERE c.allocatedToUserId = :userId")
    long countAllocatedCasesByUserId(@Param("userId") Long userId);

    /**
     * Find case by ID with eager fetch of loan and customer
     */
    @Query("SELECT c FROM Case c " +
           "LEFT JOIN FETCH c.loan l " +
           "LEFT JOIN FETCH l.primaryCustomer " +
           "WHERE c.id = :caseId")
    java.util.Optional<Case> findByIdWithDetails(@Param("caseId") Long caseId);
}
