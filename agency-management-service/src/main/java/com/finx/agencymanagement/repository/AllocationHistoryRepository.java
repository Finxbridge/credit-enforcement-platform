package com.finx.agencymanagement.repository;

import com.finx.agencymanagement.domain.entity.AllocationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for writing allocation history entries directly to the shared database.
 */
@Repository
public interface AllocationHistoryRepository extends JpaRepository<AllocationHistory, Long> {

    /**
     * Find allocation history by case ID
     */
    List<AllocationHistory> findByCaseIdOrderByAllocatedAtDesc(Long caseId);

    /**
     * Find allocation history by agency ID
     */
    List<AllocationHistory> findByAgencyIdOrderByAllocatedAtDesc(Long agencyId);
}
