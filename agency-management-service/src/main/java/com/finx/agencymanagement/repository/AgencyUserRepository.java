package com.finx.agencymanagement.repository;

import com.finx.agencymanagement.domain.entity.AgencyUser;
import com.finx.agencymanagement.domain.enums.AgencyUserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Agency User Repository
 *
 * @author Naveen Manyam
 * @version 1.0.0
 */
@Repository
public interface AgencyUserRepository extends JpaRepository<AgencyUser, Long> {

    Optional<AgencyUser> findByUserCode(String userCode);

    boolean existsByUserCode(String userCode);

    boolean existsByMobileNumber(String mobileNumber);

    boolean existsByEmail(String email);

    List<AgencyUser> findByAgencyId(Long agencyId);

    Page<AgencyUser> findByAgencyId(Long agencyId, Pageable pageable);

    List<AgencyUser> findByAgencyIdAndIsActiveTrue(Long agencyId);

    Page<AgencyUser> findByAgencyIdAndIsActiveTrue(Long agencyId, Pageable pageable);

    List<AgencyUser> findByAgencyIdAndRole(Long agencyId, AgencyUserRole role);

    @Query("SELECT COUNT(u) FROM AgencyUser u WHERE u.agencyId = :agencyId")
    Long countByAgencyId(@Param("agencyId") Long agencyId);

    @Query("SELECT COUNT(u) FROM AgencyUser u WHERE u.agencyId = :agencyId AND u.isActive = true")
    Long countActiveByAgencyId(@Param("agencyId") Long agencyId);

    @Query("SELECT u FROM AgencyUser u WHERE u.agencyId = :agencyId AND " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.mobileNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<AgencyUser> searchAgencyUsers(@Param("agencyId") Long agencyId,
                                        @Param("searchTerm") String searchTerm,
                                        Pageable pageable);

    @Query("SELECT u FROM AgencyUser u WHERE u.agencyId = :agencyId AND u.currentCaseCount < u.maxCaseCapacity AND u.isActive = true")
    List<AgencyUser> findAvailableAgentsForAllocation(@Param("agencyId") Long agencyId);
}
