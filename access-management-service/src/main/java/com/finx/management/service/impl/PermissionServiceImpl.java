package com.finx.management.service.impl;

import com.finx.common.constants.CacheConstants;
import com.finx.common.exception.ValidationException;
import com.finx.management.domain.dto.PermissionDTO;
import com.finx.management.domain.entity.Permission;
import com.finx.management.exception.ResourceNotFoundException;
import com.finx.management.mapper.PermissionMapper;
import com.finx.management.repository.ManagementPermissionRepository;
import com.finx.management.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final ManagementPermissionRepository managementPermissionRepository;
    private final PermissionMapper permissionMapper;

    @Override
    public List<PermissionDTO> getAllPermissions() {
        return managementPermissionRepository.findAll().stream()
                .map(permissionMapper::toDto)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("null")
    @Override
    @Cacheable(value = CacheConstants.PERMISSIONS, key = "#id", unless = "#result == null")
    public PermissionDTO getPermissionById(Long id) {
        log.debug("Fetching permission from database for ID: {}", id);
        return managementPermissionRepository.findById(id)
                .map(permissionMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));
    }

    @SuppressWarnings("null")
    @Override
    @Transactional
    @CacheEvict(value = { CacheConstants.PERMISSIONS, "user_permissions" }, allEntries = true)
    public PermissionDTO createPermission(PermissionDTO permissionDTO) {
        log.info("Creating new permission with code: {}", permissionDTO.getCode());

        // Validate permission code uniqueness
        if (managementPermissionRepository.existsByPermissionCode(permissionDTO.getCode())) {
            throw new ValidationException("code", "Permission code already exists: " + permissionDTO.getCode());
        }

        // Validate permission name uniqueness
        if (managementPermissionRepository.existsByPermissionName(permissionDTO.getName())) {
            throw new ValidationException("name", "Permission name already exists: " + permissionDTO.getName());
        }

        // Map DTO to entity and save
        Permission permission = permissionMapper.toEntity(permissionDTO);
        Permission savedPermission = managementPermissionRepository.save(permission);

        log.info(
                "Permission created successfully with ID: {} - Cache evicted for both permissions and user_permissions",
                savedPermission.getId());
        return permissionMapper.toDto(savedPermission);
    }

    @SuppressWarnings("null")
    @Override
    @Transactional
    @CacheEvict(value = { CacheConstants.PERMISSIONS, "user_permissions" }, allEntries = true)
    public PermissionDTO updatePermission(Long id, PermissionDTO permissionDTO) {
        log.info("Updating permission with ID: {}", id);

        // Check if permission exists
        Permission existingPermission = managementPermissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));

        // Validate permission code uniqueness (if changed)
        if (!existingPermission.getPermissionCode().equals(permissionDTO.getCode())
                && managementPermissionRepository.existsByPermissionCode(permissionDTO.getCode())) {
            throw new ValidationException("code", "Permission code already exists: " + permissionDTO.getCode());
        }

        // Validate permission name uniqueness (if changed)
        if (!existingPermission.getPermissionName().equals(permissionDTO.getName())
                && managementPermissionRepository.existsByPermissionName(permissionDTO.getName())) {
            throw new ValidationException("name", "Permission name already exists: " + permissionDTO.getName());
        }

        // Update entity from DTO
        permissionMapper.updateEntityFromDto(permissionDTO, existingPermission);
        Permission updatedPermission = managementPermissionRepository.save(existingPermission);

        log.info(
                "Permission updated successfully with ID: {} - Cache evicted for both permissions and user_permissions",
                id);
        return permissionMapper.toDto(updatedPermission);
    }

    @SuppressWarnings("null")
    @Override
    @Transactional
    @CacheEvict(value = { CacheConstants.PERMISSIONS, "user_permissions" }, allEntries = true)
    public void deletePermission(Long id) {
        log.info("Deleting permission with ID: {}", id);

        // Check if permission exists
        if (!managementPermissionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Permission", "id", id);
        }

        managementPermissionRepository.deleteById(id);
        log.info(
                "Permission deleted successfully with ID: {} - Cache evicted for both permissions and user_permissions",
                id);
    }
}
