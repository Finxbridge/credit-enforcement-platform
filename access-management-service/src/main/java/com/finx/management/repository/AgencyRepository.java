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
     * Get all active (approved) agencies for dropdown
     */
    @Query("SELECT a FROM Agency a WHERE a.status = 'ACTIVE' ORDER BY a.agencyName")
    List<Agency> findAllApprovedAgencies();
}
