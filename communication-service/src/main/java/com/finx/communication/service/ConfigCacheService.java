package com.finx.communication.service;

import com.finx.communication.domain.entity.SystemConfig;
import com.finx.communication.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

import com.finx.communication.constants.CacheConstants;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConfigCacheService {

    private final SystemConfigRepository systemConfigRepository;

    @Cacheable(value = CacheConstants.SYSTEM_CONFIG, key = "#key")
    public String getConfig(String key) {
        log.info("Fetching config from DB for key: {}", key);
        Optional<SystemConfig> config = systemConfigRepository.findByConfigKey(key);
        return config.map(SystemConfig::getConfigValue).orElse(null);
    }

    public String getConfigOrDefault(String key, String defaultValue) {
        String configValue = getConfig(key);
        return configValue != null ? configValue : defaultValue;
    }

    public int getIntConfig(String key, int defaultValue) {
        String configValue = getConfig(key);
        try {
            return configValue != null ? Integer.parseInt(configValue) : defaultValue;
        } catch (NumberFormatException e) {
            log.error("ConfigCacheService: Invalid integer format for key: {}. Returning default value.", key, e);
            return defaultValue;
        }
    }

    public boolean getBooleanConfig(String key, boolean defaultValue) {
        String configValue = getConfig(key);
        return configValue != null ? Boolean.parseBoolean(configValue) : defaultValue;
    }
}
