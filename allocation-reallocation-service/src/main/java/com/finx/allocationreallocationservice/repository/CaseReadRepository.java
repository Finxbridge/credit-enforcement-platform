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
 */
@Repository
public interface CaseReadRepository extends JpaRepository<Case, Long> {

    Page<Case> findByCaseStatus(String caseStatus, Pageable pageable);

    Optional<Case> findByCaseNumber(String caseNumber);

    Optional<Case> findByExternalCaseId(String externalCaseId);

    Long countByCaseStatus(String caseStatus);

    /**
     * Find unallocated cases by geography codes
     */
    @Query("SELECT c FROM Case c WHERE c.caseStatus = 'UNALLOCATED' " +
           "AND c.geographyCode IN :geographyCodes")
    Page<Case> findUnallocatedCasesByGeography(@Param("geographyCodes") List<String> geographyCodes,
                                                Pageable pageable);

    /**
     * Find unallocated cases by geography codes and bucket
     */
    @Query("SELECT c FROM Case c JOIN FETCH c.loan l WHERE c.caseStatus = 'UNALLOCATED' " +
           "AND c.geographyCode IN :geographyCodes AND l.bucket IN :buckets")
    Page<Case> findUnallocatedCasesByGeographyAndBucket(@Param("geographyCodes") List<String> geographyCodes,
                                                         @Param("buckets") List<String> buckets,
                                                         Pageable pageable);

    /**
     * Find unallocated cases by bucket only
     */
    @Query("SELECT c FROM Case c JOIN FETCH c.loan l WHERE c.caseStatus = 'UNALLOCATED' " +
           "AND l.bucket IN :buckets")
    Page<Case> findUnallocatedCasesByBucket(@Param("buckets") List<String> buckets,
                                            Pageable pageable);

    /**
     * Count unallocated cases by geography codes
     */
    @Query("SELECT COUNT(c) FROM Case c WHERE c.caseStatus = 'UNALLOCATED' " +
           "AND c.geographyCode IN :geographyCodes")
    Long countUnallocatedCasesByGeography(@Param("geographyCodes") List<String> geographyCodes);

    /**
     * Count unallocated cases by geography codes and bucket
     */
    @Query("SELECT COUNT(c) FROM Case c JOIN c.loan l WHERE c.caseStatus = 'UNALLOCATED' " +
           "AND c.geographyCode IN :geographyCodes AND l.bucket IN :buckets")
    Long countUnallocatedCasesByGeographyAndBucket(@Param("geographyCodes") List<String> geographyCodes,
                                                   @Param("buckets") List<String> buckets);

    /**
     * Count unallocated cases by bucket only
     */
    @Query("SELECT COUNT(c) FROM Case c JOIN c.loan l WHERE c.caseStatus = 'UNALLOCATED' " +
           "AND l.bucket IN :buckets")
    Long countUnallocatedCasesByBucket(@Param("buckets") List<String> buckets);
}
