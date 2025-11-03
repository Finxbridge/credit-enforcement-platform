package com.finx.common.repository;

import com.finx.common.model.ThirdPartyIntegrationMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ThirdPartyIntegrationMasterRepository extends JpaRepository<ThirdPartyIntegrationMaster, Long> {
    Optional<ThirdPartyIntegrationMaster> findByIntegrationNameAndIsActiveTrue(String integrationName);
    List<ThirdPartyIntegrationMaster> findAllByIsActiveTrue();
}