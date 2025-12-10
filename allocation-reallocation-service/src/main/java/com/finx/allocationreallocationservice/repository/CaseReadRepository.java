package com.finx.allocationreallocationservice.repository;

import com.finx.allocationreallocationservice.domain.entity.Case;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Read-only repository for querying Case table
 * Used to fetch unallocated cases from case-sourcing database
 * Status codes: 200 = ACTIVE, 400 = CLOSED
 * All queries filter by status = 200 (ACTIVE) to exclude closed cases
 */
@Repository
public interface CaseReadRepository extends JpaRepository<Case, Long> {

    /**
     * Find active cases by caseStatus
     */
    @Query("SELECT c FROM Case c WHERE c.caseStatus = :caseStatus AND c.status = 200")
    Page<Case> findByCaseStatusAndActive(@Param("caseStatus") String caseStatus, Pageable pageable);

    /**
     * Find active case by case number
     */
    @Query("SELECT c FROM Case c WHERE c.caseNumber = :caseNumber AND c.status = 200")
    Optional<Case> findByCaseNumberAndActive(@Param("caseNumber") String caseNumber);

    /**
     * Find active case by external case ID
     */
    @Query("SELECT c FROM Case c WHERE c.externalCaseId = :externalCaseId AND c.status = 200")
    Optional<Case> findByExternalCaseIdAndActive(@Param("externalCaseId") String externalCaseId);

    /**
     * Find active case by loan ID (loan account number)
     * Used for allocation CSV where users provide loan_id instead of database case_id
     */
    @Query("SELECT c FROM Case c JOIN c.loan l WHERE l.loanAccountNumber = :loanId AND c.status = 200")
    Optional<Case> findByLoanIdAndActive(@Param("loanId") String loanId);

    /**
     * Count active cases by caseStatus
     */
    @Query("SELECT COUNT(c) FROM Case c WHERE c.caseStatus = :caseStatus AND c.status = 200")
    Long countByCaseStatusAndActive(@Param("caseStatus") String caseStatus);

    /**
     * Find active unallocated cases by geography codes
     */
    @Query("SELECT c FROM Case c WHERE c.caseStatus = 'UNALLOCATED' " +
           "AND c.geographyCode IN :geographyCodes AND c.status = 200")
    Page<Case> findUnallocatedCasesByGeography(@Param("geographyCodes") List<String> geographyCodes,
                                                Pageable pageable);

    /**
     * Find active unallocated cases by geography codes and bucket
     */
    @Query("SELECT c FROM Case c JOIN FETCH c.loan l WHERE c.caseStatus = 'UNALLOCATED' " +
           "AND c.geographyCode IN :geographyCodes AND l.bucket IN :buckets AND c.status = 200")
    Page<Case> findUnallocatedCasesByGeographyAndBucket(@Param("geographyCodes") List<String> geographyCodes,
                                                         @Param("buckets") List<String> buckets,
                                                         Pageable pageable);

    /**
     * Find active unallocated cases by bucket only
     */
    @Query("SELECT c FROM Case c JOIN FETCH c.loan l WHERE c.caseStatus = 'UNALLOCATED' " +
           "AND l.bucket IN :buckets AND c.status = 200")
    Page<Case> findUnallocatedCasesByBucket(@Param("buckets") List<String> buckets,
                                            Pageable pageable);

    /**
     * Count active unallocated cases by geography codes
     */
    @Query("SELECT COUNT(c) FROM Case c WHERE c.caseStatus = 'UNALLOCATED' " +
           "AND c.geographyCode IN :geographyCodes AND c.status = 200")
    Long countUnallocatedCasesByGeography(@Param("geographyCodes") List<String> geographyCodes);

    /**
     * Count active unallocated cases by geography codes and bucket
     */
    @Query("SELECT COUNT(c) FROM Case c JOIN c.loan l WHERE c.caseStatus = 'UNALLOCATED' " +
           "AND c.geographyCode IN :geographyCodes AND l.bucket IN :buckets AND c.status = 200")
    Long countUnallocatedCasesByGeographyAndBucket(@Param("geographyCodes") List<String> geographyCodes,
                                                   @Param("buckets") List<String> buckets);

    /**
     * Count active unallocated cases by bucket only
     */
    @Query("SELECT COUNT(c) FROM Case c JOIN c.loan l WHERE c.caseStatus = 'UNALLOCATED' " +
           "AND l.bucket IN :buckets AND c.status = 200")
    Long countUnallocatedCasesByBucket(@Param("buckets") List<String> buckets);

    // ============================================
    // MULTI-FIELD GEOGRAPHY FILTERING
    // Filter by states, cities, and/or locations
    // ============================================

    /**
     * Find unallocated cases by states only
     */
    @Query("SELECT c FROM Case c WHERE c.caseStatus = 'UNALLOCATED' " +
           "AND c.stateCode IN :states AND c.status = 200")
    Page<Case> findUnallocatedCasesByStates(@Param("states") List<String> states, Pageable pageable);

    /**
     * Find unallocated cases by cities only
     */
    @Query("SELECT c FROM Case c WHERE c.caseStatus = 'UNALLOCATED' " +
           "AND c.cityCode IN :cities AND c.status = 200")
    Page<Case> findUnallocatedCasesByCities(@Param("cities") List<String> cities, Pageable pageable);

    /**
     * Find unallocated cases by locations only
     */
    @Query("SELECT c FROM Case c WHERE c.caseStatus = 'UNALLOCATED' " +
           "AND c.location IN :locations AND c.status = 200")
    Page<Case> findUnallocatedCasesByLocations(@Param("locations") List<String> locations, Pageable pageable);

    /**
     * Find unallocated cases by states and cities
     */
    @Query("SELECT c FROM Case c WHERE c.caseStatus = 'UNALLOCATED' " +
           "AND c.stateCode IN :states AND c.cityCode IN :cities AND c.status = 200")
    Page<Case> findUnallocatedCasesByStatesAndCities(@Param("states") List<String> states,
                                                      @Param("cities") List<String> cities,
                                                      Pageable pageable);

    /**
     * Find unallocated cases by states, cities, and locations
     */
    @Query("SELECT c FROM Case c WHERE c.caseStatus = 'UNALLOCATED' " +
           "AND c.stateCode IN :states AND c.cityCode IN :cities AND c.location IN :locations AND c.status = 200")
    Page<Case> findUnallocatedCasesByStatesAndCitiesAndLocations(@Param("states") List<String> states,
                                                                  @Param("cities") List<String> cities,
                                                                  @Param("locations") List<String> locations,
                                                                  Pageable pageable);

    /**
     * Find unallocated cases by states and locations (no city filter)
     */
    @Query("SELECT c FROM Case c WHERE c.caseStatus = 'UNALLOCATED' " +
           "AND c.stateCode IN :states AND c.location IN :locations AND c.status = 200")
    Page<Case> findUnallocatedCasesByStatesAndLocations(@Param("states") List<String> states,
                                                         @Param("locations") List<String> locations,
                                                         Pageable pageable);

    /**
     * Find unallocated cases by cities and locations (no state filter)
     */
    @Query("SELECT c FROM Case c WHERE c.caseStatus = 'UNALLOCATED' " +
           "AND c.cityCode IN :cities AND c.location IN :locations AND c.status = 200")
    Page<Case> findUnallocatedCasesByCitiesAndLocations(@Param("cities") List<String> cities,
                                                         @Param("locations") List<String> locations,
                                                         Pageable pageable);

    /**
     * Find unallocated cases by multi-field geography with buckets
     */
    @Query("SELECT c FROM Case c JOIN FETCH c.loan l WHERE c.caseStatus = 'UNALLOCATED' " +
           "AND (:states IS NULL OR c.stateCode IN :states) " +
           "AND (:cities IS NULL OR c.cityCode IN :cities) " +
           "AND (:locations IS NULL OR c.location IN :locations) " +
           "AND l.bucket IN :buckets AND c.status = 200")
    Page<Case> findUnallocatedCasesByMultiGeographyAndBucket(@Param("states") List<String> states,
                                                              @Param("cities") List<String> cities,
                                                              @Param("locations") List<String> locations,
                                                              @Param("buckets") List<String> buckets,
                                                              Pageable pageable);

    // ============================================
    // LEGACY METHODS (without status filter)
    // Keep for backward compatibility but prefer *AndActive methods
    // ============================================

    Page<Case> findByCaseStatus(String caseStatus, Pageable pageable);

    Optional<Case> findByCaseNumber(String caseNumber);

    Optional<Case> findByExternalCaseId(String externalCaseId);

    @Query("SELECT c FROM Case c JOIN c.loan l WHERE l.loanAccountNumber = :loanId")
    Optional<Case> findByLoanId(@Param("loanId") String loanId);

    /**
     * Find active case by loan account number (ACCOUNT NO in CSV)
     * Used for allocation CSV upload
     */
    @Query("SELECT c FROM Case c JOIN c.loan l WHERE l.loanAccountNumber = :accountNo AND c.status = 200")
    Optional<Case> findByLoanAccountNumber(@Param("accountNo") String accountNo);

    Long countByCaseStatus(String caseStatus);
}
