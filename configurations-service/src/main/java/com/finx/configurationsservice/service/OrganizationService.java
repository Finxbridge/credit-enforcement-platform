package com.finx.configurationsservice.service;

import com.finx.configurationsservice.domain.dto.CreateOrganizationRequest;
import com.finx.configurationsservice.domain.dto.OrganizationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface OrganizationService {

    OrganizationDTO createOrganization(CreateOrganizationRequest request);

    OrganizationDTO getOrganizationById(Long id);

    OrganizationDTO getOrganizationByCode(String orgCode);

    List<OrganizationDTO> getActiveOrganizations();

    Page<OrganizationDTO> getAllOrganizations(Pageable pageable);

    OrganizationDTO updateOrganization(Long id, CreateOrganizationRequest request);

    OrganizationDTO updateFeatures(Long id, Map<String, Boolean> features);

    OrganizationDTO activateOrganization(Long id);

    OrganizationDTO deactivateOrganization(Long id);

    void deleteOrganization(Long id);

    boolean isLicenseValid(Long id);

    boolean isFeatureEnabled(Long id, String featureName);
}
