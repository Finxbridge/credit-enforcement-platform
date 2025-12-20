package com.finx.agencymanagement.repository;

import com.finx.agencymanagement.domain.entity.Agency;
import com.finx.agencymanagement.domain.enums.AgencyStatus;
import com.finx.agencymanagement.domain.enums.AgencyType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Agency Repository
 *
 * @author Naveen Manyam
 * @version 1.0.0
 */
@Repository
public interface AgencyRepository extends JpaRepository<Agency, Long> {

    Optional<Agency> findByAgencyCode(String agencyCode);

    boolean existsByAgencyCode(String agencyCode);

    boolean existsByPanNumber(String panNumber);

    boolean existsByGstNumber(String gstNumber);

    List<Agency> findByStatus(AgencyStatus status);

    Page<Agency> findByStatus(AgencyStatus status, Pageable pageable);

    List<Agency> findByAgencyType(AgencyType agencyType);

    Page<Agency> findByAgencyType(AgencyType agencyType, Pageable pageable);

    List<Agency> findByStatusAndAgencyType(AgencyStatus status, AgencyType agencyType);

    Page<Agency> findByIsActiveTrue(Pageable pageable);

    List<Agency> findByIsActiveTrue();

    @Query("SELECT a FROM Agency a WHERE a.status = :status AND a.isActive = true")
    List<Agency> findActiveAgenciesByStatus(@Param("status") AgencyStatus status);

    @Query("SELECT a FROM Agency a WHERE a.status = 'PENDING_APPROVAL' ORDER BY a.submittedAt ASC")
    List<Agency> findAgenciesPendingApproval();

    @Query("SELECT COUNT(a) FROM Agency a WHERE a.status = :status")
    Long countByStatus(@Param("status") AgencyStatus status);

    @Query("SELECT a FROM Agency a WHERE " +
           "(LOWER(a.agencyName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.agencyCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.contactPerson) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Agency> searchAgencies(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT a FROM Agency a WHERE a.status IN :statuses")
    Page<Agency> findByStatusIn(@Param("statuses") List<AgencyStatus> statuses, Pageable pageable);
}
