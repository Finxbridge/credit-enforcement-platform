package com.finx.collectionsservice.repository;

import com.finx.collectionsservice.domain.entity.Case;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for searching cases for OTS creation
 */
@Repository
public interface CaseRepository extends JpaRepository<Case, Long> {

    /**
     * Search cases by customer name (partial match, case-insensitive)
     */
    @Query("SELECT c FROM Case c " +
           "JOIN FETCH c.loan l " +
           "JOIN FETCH l.primaryCustomer cu " +
           "WHERE LOWER(cu.fullName) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "AND (c.isArchived = false OR c.isArchived IS NULL) " +
           "AND c.caseStatus != 'CLOSED' " +
           "ORDER BY c.createdAt DESC")
    Page<Case> searchByCustomerName(@Param("name") String name, Pageable pageable);

    /**
     * Search cases by loan account number (partial match)
     */
    @Query("SELECT c FROM Case c " +
           "JOIN FETCH c.loan l " +
           "JOIN FETCH l.primaryCustomer cu " +
           "WHERE LOWER(l.loanAccountNumber) LIKE LOWER(CONCAT('%', :accountNumber, '%')) " +
           "AND (c.isArchived = false OR c.isArchived IS NULL) " +
           "AND c.caseStatus != 'CLOSED' " +
           "ORDER BY c.createdAt DESC")
    Page<Case> searchByLoanAccountNumber(@Param("accountNumber") String accountNumber, Pageable pageable);

    /**
     * Search cases by case number (partial match)
     */
    @Query("SELECT c FROM Case c " +
           "JOIN FETCH c.loan l " +
           "JOIN FETCH l.primaryCustomer cu " +
           "WHERE LOWER(c.caseNumber) LIKE LOWER(CONCAT('%', :caseNumber, '%')) " +
           "AND (c.isArchived = false OR c.isArchived IS NULL) " +
           "AND c.caseStatus != 'CLOSED' " +
           "ORDER BY c.createdAt DESC")
    Page<Case> searchByCaseNumber(@Param("caseNumber") String caseNumber, Pageable pageable);

    /**
     * Search cases by customer mobile number
     */
    @Query("SELECT c FROM Case c " +
           "JOIN FETCH c.loan l " +
           "JOIN FETCH l.primaryCustomer cu " +
           "WHERE cu.mobileNumber LIKE CONCAT('%', :mobile, '%') " +
           "AND (c.isArchived = false OR c.isArchived IS NULL) " +
           "AND c.caseStatus != 'CLOSED' " +
           "ORDER BY c.createdAt DESC")
    Page<Case> searchByMobileNumber(@Param("mobile") String mobile, Pageable pageable);

    /**
     * Generic search - searches across customer name, loan account, case number, mobile
     */
    @Query("SELECT c FROM Case c " +
           "JOIN FETCH c.loan l " +
           "JOIN FETCH l.primaryCustomer cu " +
           "WHERE (LOWER(cu.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "   OR LOWER(l.loanAccountNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "   OR LOWER(c.caseNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "   OR cu.mobileNumber LIKE CONCAT('%', :searchTerm, '%')) " +
           "AND (c.isArchived = false OR c.isArchived IS NULL) " +
           "AND c.caseStatus != 'CLOSED' " +
           "ORDER BY c.createdAt DESC")
    Page<Case> searchCases(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Get case by ID with loan and customer details
     */
    @Query("SELECT c FROM Case c " +
           "JOIN FETCH c.loan l " +
           "JOIN FETCH l.primaryCustomer cu " +
           "WHERE c.id = :caseId")
    Optional<Case> findByIdWithDetails(@Param("caseId") Long caseId);
}
