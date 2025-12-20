package com.finx.configurationsservice.service;

import com.finx.configurationsservice.domain.dto.*;
import com.finx.configurationsservice.domain.enums.LmsType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LmsConfigurationService {

    LmsConfigurationDTO createLmsConfig(CreateLmsConfigRequest request);

    LmsConfigurationDTO getLmsConfigById(Long id);

    LmsConfigurationDTO getLmsConfigByCode(String code);

    List<LmsConfigurationDTO> getLmsConfigsByType(LmsType type);

    List<LmsConfigurationDTO> getActiveLmsConfigs();

    List<LmsConfigurationDTO> getActiveLmsConfigsByType(LmsType type);

    LmsConfigurationDTO updateLmsConfig(Long id, UpdateLmsConfigRequest request);

    void deactivateLmsConfig(Long id);

    void activateLmsConfig(Long id);

    LmsConnectionTestResult testLmsConnection(Long id);

    LmsSyncHistoryDTO triggerManualSync(Long id);

    Page<LmsConfigurationDTO> getAllLmsConfigs(LmsType type, Boolean isActive, String search, Pageable pageable);

    Page<LmsSyncHistoryDTO> getSyncHistory(Long lmsId, Pageable pageable);
}
