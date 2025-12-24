package com.finx.management.repository;

import com.finx.management.domain.entity.Agency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Read-only repository for agencies table.
 * Used only to fetch approved agencies for user creation dropdown.
 */
@Repository
public interface AgencyRepository extends JpaRepository<Agency, Long> {

    /**
     * Get all approved and active agencies for dropdown
     * Agencies with status APPROVED or ACTIVE can have agents assigned
     */
    @Query("SELECT a FROM Agency a WHERE a.status IN ('APPROVED', 'ACTIVE') ORDER BY a.agencyName")
    List<Agency> findAllApprovedAgencies();
}
