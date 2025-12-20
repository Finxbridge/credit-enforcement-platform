package com.finx.configurationsservice.repository;

import com.finx.configurationsservice.domain.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    Optional<Organization> findByOrgCode(String orgCode);

    List<Organization> findByIsActiveTrue();

    boolean existsByOrgCode(String orgCode);
}
