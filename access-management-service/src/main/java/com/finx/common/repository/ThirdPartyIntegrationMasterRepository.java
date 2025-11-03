package com.finx.common.repository;

import com.finx.common.model.ThirdPartyIntegrationMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThirdPartyIntegrationMasterRepository extends JpaRepository<ThirdPartyIntegrationMaster, Long> {
    Optional<ThirdPartyIntegrationMaster> findByIntegrationNameAndIsActiveTrue(String integrationName);

    Optional<ThirdPartyIntegrationMaster> findByIntegrationNameAndIsActive(String integrationName, Boolean isActive);

    Optional<ThirdPartyIntegrationMaster> findByIntegrationTypeAndIsActive(String integrationType, Boolean isActive);

    List<ThirdPartyIntegrationMaster> findAllByIsActiveTrue();
}
