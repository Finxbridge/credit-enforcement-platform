package com.finx.templatemanagementservice.repository;

import com.finx.templatemanagementservice.domain.entity.Case;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Case entity (read-only)
 * Status codes: 200 = ACTIVE, 400 = CLOSED
 * All queries filter by status = 200 (ACTIVE) to exclude closed cases
 */
@Repository
public interface CaseRepository extends JpaRepository<Case, Long> {

    /**
     * Find active case with loan and customer details eagerly loaded
     */
    @Query("SELECT c FROM Case c " +
           "LEFT JOIN FETCH c.loan l " +
           "LEFT JOIN FETCH l.primaryCustomer " +
           "LEFT JOIN FETCH l.coBorrower " +
           "LEFT JOIN FETCH l.guarantor " +
           "WHERE c.id = :caseId AND c.status = 200")
    Optional<Case> findByIdWithLoanAndCustomers(@Param("caseId") Long caseId);

    /**
     * Find active case by case number with loan and customer details
     */
    @Query("SELECT c FROM Case c " +
           "LEFT JOIN FETCH c.loan l " +
           "LEFT JOIN FETCH l.primaryCustomer " +
           "LEFT JOIN FETCH l.coBorrower " +
           "LEFT JOIN FETCH l.guarantor " +
           "WHERE c.caseNumber = :caseNumber AND c.status = 200")
    Optional<Case> findByCaseNumberWithLoanAndCustomers(@Param("caseNumber") String caseNumber);
}
