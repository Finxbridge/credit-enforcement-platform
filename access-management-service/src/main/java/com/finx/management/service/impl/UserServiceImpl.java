package com.finx.management.service.impl;

import com.finx.common.constants.CacheConstants;
import com.finx.management.domain.dto.CreateUserRequest;
import com.finx.management.domain.dto.UpdateUserRequest;
import com.finx.management.domain.dto.UserDTO;
import com.finx.management.domain.dto.UserPermissionDTO;
import com.finx.management.domain.entity.Role;
import com.finx.management.domain.entity.User;
import com.finx.management.domain.entity.UserGroup;
import com.finx.management.exception.BusinessException;
import com.finx.management.exception.ConflictException;
import com.finx.management.exception.ResourceNotFoundException;
import com.finx.management.mapper.UserMapper;
import com.finx.management.repository.ManagementRoleRepository;
import com.finx.management.repository.UserGroupRepository;
import com.finx.management.repository.UserRepository;
import com.finx.management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserGroupRepository userGroupRepository;
    private final ManagementRoleRepository managementRoleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @SuppressWarnings("null")
    @Override
    public Page<UserDTO> getAllUsers(Pageable pageable, String search, String status) {
        Specification<User> spec = Specification.where(null);

        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("username")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("firstName")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("lastName")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("email")), "%" + search.toLowerCase() + "%")));
        }

        if (status != null && !status.trim().isEmpty()) {
            try {
                User.UserStatus userStatus = User.UserStatus.valueOf(status.toUpperCase());
                spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), userStatus));
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Invalid user status: " + status);
            }
        }

        return userRepository.findAll(spec, pageable).map(userMapper::toDto);
    }

    @SuppressWarnings("null")
    @Override
    @Cacheable(value = CacheConstants.USERS, key = "#id")
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.toDto(user);
    }

    @SuppressWarnings("null")
    @Override
    @Transactional
    public UserDTO createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("User with username '" + request.getUsername() + "' already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("User with email '" + request.getEmail() + "' already exists");
        }

        User user = userMapper.toEntity(request);
        user.setIsFirstLogin(true);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        try {
            user.setStatus(User.UserStatus.valueOf(request.getStatus().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid user status: " + request.getStatus());
        }

        if (request.getUserGroupId() != null) {
            UserGroup userGroup = userGroupRepository.findById(request.getUserGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("UserGroup", "id", request.getUserGroupId()));
            user.setUserGroup(userGroup);
        }

        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            Set<Role> roles = new HashSet<>(managementRoleRepository.findAllById(request.getRoleIds()));
            if (roles.size() != request.getRoleIds().size()) {
                throw new BusinessException("One or more role IDs are invalid");
            }
            user.setRoles(roles);
        }

        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @SuppressWarnings("null")
    @Override
    @Transactional
    @CacheEvict(value = { CacheConstants.USERS, CacheConstants.USER_PERMISSIONS }, key = "#id")
    public UserDTO updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Check for email conflict if email is being changed
        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ConflictException("User with email '" + request.getEmail() + "' already exists");
            }
        }

        if (request.getStatus() != null) {
            try {
                user.setStatus(User.UserStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Invalid user status: " + request.getStatus());
            }
        }

        userMapper.updateEntityFromDto(request, user);

        if (request.getUserGroupId() != null) {
            UserGroup userGroup = userGroupRepository.findById(request.getUserGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("UserGroup", "id", request.getUserGroupId()));
            user.setUserGroup(userGroup);
        } else if (request.getUserGroupId() == null) {
            user.setUserGroup(null);
        }

        if (request.getRoleIds() != null) {
            Set<Role> roles = new HashSet<>(managementRoleRepository.findAllById(request.getRoleIds()));
            if (roles.size() != request.getRoleIds().size()) {
                throw new BusinessException("One or more role IDs are invalid");
            }
            user.setRoles(roles);
        } else if (request.getRoleIds() != null && request.getRoleIds().isEmpty()) {
            user.setRoles(new HashSet<>());
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toDto(updatedUser);
    }

    @SuppressWarnings("null")
    @Override
    @CacheEvict(value = { CacheConstants.USERS, CacheConstants.USER_PERMISSIONS }, key = "#id")
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        userRepository.delete(user);
    }

    @SuppressWarnings("null")
    @Override
    @Cacheable(value = CacheConstants.USER_PERMISSIONS, key = "#userId")
    public List<UserPermissionDTO> getUserPermissions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> {
                    UserPermissionDTO dto = new UserPermissionDTO();
                    dto.setCode(permission.getPermissionCode());
                    dto.setName(permission.getPermissionName());
                    // Assuming category is part of permission entity or can be derived
                    // For now, let's leave category as null or add it to Permission entity/DTO if
                    // needed.
                    return dto;
                })
                .distinct()
                .collect(Collectors.toList());
    }
}
