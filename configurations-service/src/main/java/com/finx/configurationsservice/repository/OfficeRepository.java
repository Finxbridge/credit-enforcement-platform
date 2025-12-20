package com.finx.configurationsservice.repository;

import com.finx.configurationsservice.domain.entity.Office;
import com.finx.configurationsservice.domain.enums.OfficeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfficeRepository extends JpaRepository<Office, Long> {

    Optional<Office> findByOfficeCode(String officeCode);

    List<Office> findByIsActiveTrue();

    List<Office> findByOfficeType(OfficeType officeType);

    List<Office> findByParentOfficeId(Long parentOfficeId);

    List<Office> findByState(String state);

    List<Office> findByCity(String city);

    Page<Office> findByIsActive(Boolean isActive, Pageable pageable);

    @Query("SELECT o FROM Office o WHERE o.officeType = :type AND o.isActive = true")
    List<Office> findActiveOfficesByType(@Param("type") OfficeType type);

    @Query("SELECT o FROM Office o WHERE o.state = :state AND o.isActive = true")
    List<Office> findActiveOfficesByState(@Param("state") String state);

    boolean existsByOfficeCode(String officeCode);
}
