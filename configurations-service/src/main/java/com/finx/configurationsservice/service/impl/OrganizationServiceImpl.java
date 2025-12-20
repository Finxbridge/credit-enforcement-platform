package com.finx.configurationsservice.service.impl;

import com.finx.configurationsservice.domain.dto.CreateOrganizationRequest;
import com.finx.configurationsservice.domain.dto.OrganizationDTO;
import com.finx.configurationsservice.domain.entity.Organization;
import com.finx.configurationsservice.exception.BusinessException;
import com.finx.configurationsservice.exception.ResourceNotFoundException;
import com.finx.configurationsservice.mapper.ConfigurationsMapper;
import com.finx.configurationsservice.repository.OrganizationRepository;
import com.finx.configurationsservice.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final ConfigurationsMapper mapper;

    @Override
    @CacheEvict(value = "organizations", allEntries = true)
    public OrganizationDTO createOrganization(CreateOrganizationRequest request) {
        if (organizationRepository.existsByOrgCode(request.getOrgCode())) {
            throw new BusinessException("Organization with code " + request.getOrgCode() + " already exists");
        }

        Organization organization = Organization.builder()
                .orgCode(request.getOrgCode())
                .orgName(request.getOrgName())
                .legalName(request.getLegalName())
                .logoUrl(request.getLogoUrl())
                .primaryColor(request.getPrimaryColor())
                .secondaryColor(request.getSecondaryColor())
                .email(request.getEmail())
                .phone(request.getPhone())
                .website(request.getWebsite())
                .address(request.getAddress())
                .defaultCurrency(request.getDefaultCurrency())
                .defaultLanguage(request.getDefaultLanguage())
                .defaultTimezone(request.getDefaultTimezone())
                .dateFormat(request.getDateFormat())
                .licenseType(request.getLicenseType())
                .licenseValidUntil(request.getLicenseValidUntil())
                .maxUsers(request.getMaxUsers())
                .maxCases(request.getMaxCases())
                .enabledFeatures(request.getEnabledFeatures())
                .isActive(true)
                .build();

        organization = organizationRepository.save(organization);
        log.info("Created organization: {}", organization.getOrgCode());
        return mapper.toDto(organization);
    }

    @Override
    @Cacheable(value = "organizations", key = "#id")
    @Transactional(readOnly = true)
    public OrganizationDTO getOrganizationById(Long id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + id));
        return mapper.toDto(organization);
    }

    @Override
    @Cacheable(value = "organizations", key = "'code-' + #orgCode")
    @Transactional(readOnly = true)
    public OrganizationDTO getOrganizationByCode(String orgCode) {
        Organization organization = organizationRepository.findByOrgCode(orgCode)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with code: " + orgCode));
        return mapper.toDto(organization);
    }

    @Override
    @Cacheable(value = "organizations", key = "'active'")
    @Transactional(readOnly = true)
    public List<OrganizationDTO> getActiveOrganizations() {
        return mapper.toOrganizationDtoList(organizationRepository.findByIsActiveTrue());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrganizationDTO> getAllOrganizations(Pageable pageable) {
        return organizationRepository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    @CacheEvict(value = "organizations", allEntries = true)
    public OrganizationDTO updateOrganization(Long id, CreateOrganizationRequest request) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + id));

        if (!organization.getOrgCode().equals(request.getOrgCode()) &&
                organizationRepository.existsByOrgCode(request.getOrgCode())) {
            throw new BusinessException("Organization with code " + request.getOrgCode() + " already exists");
        }

        organization.setOrgCode(request.getOrgCode());
        organization.setOrgName(request.getOrgName());
        if (request.getLegalName() != null) organization.setLegalName(request.getLegalName());
        if (request.getLogoUrl() != null) organization.setLogoUrl(request.getLogoUrl());
        if (request.getPrimaryColor() != null) organization.setPrimaryColor(request.getPrimaryColor());
        if (request.getSecondaryColor() != null) organization.setSecondaryColor(request.getSecondaryColor());
        if (request.getEmail() != null) organization.setEmail(request.getEmail());
        if (request.getPhone() != null) organization.setPhone(request.getPhone());
        if (request.getWebsite() != null) organization.setWebsite(request.getWebsite());
        if (request.getAddress() != null) organization.setAddress(request.getAddress());
        if (request.getDefaultCurrency() != null) organization.setDefaultCurrency(request.getDefaultCurrency());
        if (request.getDefaultLanguage() != null) organization.setDefaultLanguage(request.getDefaultLanguage());
        if (request.getDefaultTimezone() != null) organization.setDefaultTimezone(request.getDefaultTimezone());
        if (request.getDateFormat() != null) organization.setDateFormat(request.getDateFormat());
        if (request.getLicenseType() != null) organization.setLicenseType(request.getLicenseType());
        if (request.getLicenseValidUntil() != null) organization.setLicenseValidUntil(request.getLicenseValidUntil());
        if (request.getMaxUsers() != null) organization.setMaxUsers(request.getMaxUsers());
        if (request.getMaxCases() != null) organization.setMaxCases(request.getMaxCases());
        if (request.getEnabledFeatures() != null) organization.setEnabledFeatures(request.getEnabledFeatures());

        organization = organizationRepository.save(organization);
        log.info("Updated organization: {}", organization.getOrgCode());
        return mapper.toDto(organization);
    }

    @Override
    @CacheEvict(value = "organizations", allEntries = true)
    public OrganizationDTO updateFeatures(Long id, Map<String, Boolean> features) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + id));

        Map<String, Boolean> existingFeatures = organization.getEnabledFeatures();
        if (existingFeatures == null) {
            existingFeatures = new HashMap<>();
        }
        existingFeatures.putAll(features);
        organization.setEnabledFeatures(existingFeatures);

        organization = organizationRepository.save(organization);
        log.info("Updated features for organization: {}", organization.getOrgCode());
        return mapper.toDto(organization);
    }

    @Override
    @CacheEvict(value = "organizations", allEntries = true)
    public OrganizationDTO activateOrganization(Long id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + id));

        organization.setIsActive(true);
        organization = organizationRepository.save(organization);
        log.info("Activated organization: {}", organization.getOrgCode());
        return mapper.toDto(organization);
    }

    @Override
    @CacheEvict(value = "organizations", allEntries = true)
    public OrganizationDTO deactivateOrganization(Long id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + id));

        organization.setIsActive(false);
        organization = organizationRepository.save(organization);
        log.info("Deactivated organization: {}", organization.getOrgCode());
        return mapper.toDto(organization);
    }

    @Override
    @CacheEvict(value = "organizations", allEntries = true)
    public void deleteOrganization(Long id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + id));

        organizationRepository.delete(organization);
        log.info("Deleted organization: {}", organization.getOrgCode());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isLicenseValid(Long id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + id));

        if (organization.getLicenseValidUntil() == null) {
            return true;
        }
        return !LocalDate.now().isAfter(organization.getLicenseValidUntil());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFeatureEnabled(Long id, String featureName) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + id));

        if (organization.getEnabledFeatures() == null) {
            return false;
        }
        return Boolean.TRUE.equals(organization.getEnabledFeatures().get(featureName));
    }
}
