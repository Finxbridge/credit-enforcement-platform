package com.finx.communication.repository;

import com.finx.communication.domain.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * SystemConfigRepository
 * Purpose: Data access layer for SystemConfig entity
 */
@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {

    Optional<SystemConfig> findByConfigKey(String configKey);

    List<SystemConfig> findByIsActive(Boolean isActive);

    List<SystemConfig> findByDataType(String dataType);

    Boolean existsByConfigKey(String configKey);
}
