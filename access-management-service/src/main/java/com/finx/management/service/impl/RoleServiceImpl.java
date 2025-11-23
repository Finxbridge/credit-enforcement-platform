package com.finx.management.service.impl;

import com.finx.common.constants.CacheConstants;
import com.finx.management.domain.dto.CreateRoleRequest;
import com.finx.management.domain.dto.RoleDTO;
import com.finx.management.domain.dto.UpdateRoleRequest;
import com.finx.management.domain.entity.Permission;
import com.finx.management.domain.entity.Role;
import com.finx.management.domain.entity.RoleGroup;
import com.finx.management.exception.BusinessException;
import com.finx.management.exception.ConflictException;
import com.finx.management.exception.ResourceNotFoundException;
import com.finx.management.mapper.RoleMapper;
import com.finx.management.repository.ManagementPermissionRepository;
import com.finx.management.repository.RoleGroupRepository;
import com.finx.management.repository.ManagementRoleRepository;
import com.finx.management.repository.UserRepository;
import com.finx.management.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final ManagementRoleRepository managementRoleRepository;
    private final RoleGroupRepository roleGroupRepository;
    private final ManagementPermissionRepository managementPermissionRepository;
    private final UserRepository userRepository;
    private final RoleMapper roleMapper;

    @Override
    @Cacheable(value = CacheConstants.ROLES)
    public List<RoleDTO> getAllRoles() {
        return getAllRoles(true);
    }

    @Override
    public List<RoleDTO> getAllRoles(boolean activeOnly) {
        if (activeOnly) {
            return managementRoleRepository.findAllByIsActive(true).stream()
                    .map(roleMapper::toDto)
                    .collect(Collectors.toList());
        } else {
            return managementRoleRepository.findAll().stream()
                    .map(roleMapper::toDto)
                    .collect(Collectors.toList());
        }
    }

    @SuppressWarnings("null")
    @Override
    @Cacheable(value = CacheConstants.ROLES, key = "#id")
    public RoleDTO getRoleById(Long id) {
        Role role = managementRoleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
        return roleMapper.toDto(role);
    }

    @SuppressWarnings("null")
    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.ROLES, allEntries = true)
    public RoleDTO createRole(CreateRoleRequest request) {
        if (managementRoleRepository.findByRoleCode(request.getName()).isPresent()) {
            throw new ConflictException("Role with name '" + request.getName() + "' already exists");
        }

        Role role = roleMapper.toEntity(request);
        role.setRoleCode(request.getName().toUpperCase().replace(" ", "_")); // Generate code from name

        if (request.getRoleGroupId() != null) {
            RoleGroup roleGroup = roleGroupRepository.findById(request.getRoleGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("RoleGroup", "id", request.getRoleGroupId()));
            role.setRoleGroup(roleGroup);
        }

        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            Set<Permission> permissions = new HashSet<>(
                    managementPermissionRepository.findAllById(request.getPermissionIds()));
            if (permissions.size() != request.getPermissionIds().size()) {
                throw new BusinessException("One or more permission IDs are invalid");
            }
            role.setPermissions(permissions);
        }

        Role savedRole = managementRoleRepository.save(role);
        return roleMapper.toDto(savedRole);
    }

    @SuppressWarnings("null")
    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.ROLES, allEntries = true)
    public RoleDTO updateRole(Long id, UpdateRoleRequest request) {
        Role role = managementRoleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        if (request.getDisplayName() != null) {
            role.setRoleName(request.getDisplayName());
        }
        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            role.setIsActive(request.getStatus());
        }

        if (request.getRoleGroupId() != null) {
            RoleGroup roleGroup = roleGroupRepository.findById(request.getRoleGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("RoleGroup", "id", request.getRoleGroupId()));
            role.setRoleGroup(roleGroup);
        } else if (request.getRoleGroupId() == null) {
            role.setRoleGroup(null);
        }

        if (request.getPermissionIds() != null) {
            Set<Permission> permissions = new HashSet<>(
                    managementPermissionRepository.findAllById(request.getPermissionIds()));
            if (permissions.size() != request.getPermissionIds().size()) {
                throw new BusinessException("One or more permission IDs are invalid");
            }
            role.setPermissions(permissions);
        } else if (request.getPermissionIds() != null && request.getPermissionIds().isEmpty()) {
            role.setPermissions(new HashSet<>());
        }

        Role updatedRole = managementRoleRepository.save(role);
        return roleMapper.toDto(updatedRole);
    }

    @SuppressWarnings("null")
    @Override
    @CacheEvict(value = CacheConstants.ROLES, allEntries = true)
    public void deleteRole(Long id) {
        Role role = managementRoleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        // Check if role is assigned to any active users
        if (userRepository.existsByRoleIdAndActiveUsers(id)) {
            long activeUserCount = userRepository.countActiveUsersByRoleId(id);
            throw new BusinessException(
                    String.format("Cannot delete role '%s'. It is currently assigned to %d active user(s). " +
                            "Please remove this role from all active users before deleting.",
                            role.getRoleName(), activeUserCount));
        }

        managementRoleRepository.delete(role);
    }
}
