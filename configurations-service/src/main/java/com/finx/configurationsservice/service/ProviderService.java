package com.finx.configurationsservice.service;

import com.finx.configurationsservice.domain.dto.*;
import com.finx.configurationsservice.domain.enums.ProviderType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProviderService {

    ProviderDTO createProvider(CreateProviderRequest request);

    ProviderDTO getProviderById(Long id);

    ProviderDTO getProviderByCode(String code);

    List<ProviderDTO> getProvidersByType(ProviderType type);

    List<ProviderDTO> getActiveProvidersByType(ProviderType type);

    List<ProviderDTO> getActiveProviders();

    ProviderDTO getDefaultProvider(ProviderType type);

    ProviderDTO updateProvider(Long id, UpdateProviderRequest request);

    void deactivateProvider(Long id);

    void activateProvider(Long id);

    ProviderTestResult testProvider(Long id);

    Page<ProviderDTO> getAllProviders(ProviderType type, Boolean isActive, String search, Pageable pageable);

    List<ProviderTestResult> getTestHistory(Long providerId);
}
