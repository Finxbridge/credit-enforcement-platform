package com.finx.common.service;

import com.finx.common.constants.CacheConstants;
import com.finx.common.model.ThirdPartyIntegrationMaster;
import com.finx.common.repository.ThirdPartyIntegrationMasterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class IntegrationCacheService {

    private final ThirdPartyIntegrationMasterRepository repository;

    @Cacheable(value = CacheConstants.INTEGRATION_CONFIG_CACHE, key = "#integrationName")
    public Optional<ThirdPartyIntegrationMaster> getIntegrationConfig(String integrationName) {
        log.info("Fetching integration config from DB for: {}", integrationName);
        return repository.findByIntegrationNameAndIsActiveTrue(integrationName);
    }

    public Optional<ThirdPartyIntegrationMaster> getIntegration(String integrationName) {
        return getIntegrationConfig(integrationName);
    }

    /**
     * Loads all active third-party integrations into the cache.
     * This method is typically called during application startup by a CacheWarmerService.
     * @return The number of integrations loaded.
     */
    public int loadAllIntegrations() {
        log.info("Loading all active third-party integrations into cache...");
        List<ThirdPartyIntegrationMaster> integrations = repository.findAllByIsActiveTrue();
        integrations.forEach(integration -> {
            // Calling getIntegrationConfig to populate the cache for each integration
            getIntegrationConfig(integration.getIntegrationName());
        });
        log.info("Loaded {} active third-party integrations.", integrations.size());
        return integrations.size();
    }

    /**
     * Evicts all entries from the integrationConfig cache and reloads them.
     * This method is typically called when integration configurations are updated.
     */
    @CacheEvict(value = CacheConstants.INTEGRATION_CONFIG_CACHE, allEntries = true)
    public void refreshAllIntegrations() {
        log.info("Refreshing all third-party integrations cache...");
        loadAllIntegrations();
        log.info("Third-party integrations cache refreshed.");
    }
}
