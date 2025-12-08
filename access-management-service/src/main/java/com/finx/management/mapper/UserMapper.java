package com.finx.management.mapper;

import com.finx.management.domain.dto.UpdateUserRequest;
import com.finx.management.domain.dto.CreateUserRequest;
import com.finx.management.domain.dto.UserDTO;
import com.finx.management.domain.dto.UserListDTO;
import com.finx.management.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = { RoleMapper.class,
        PermissionMapper.class }, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    @Mapping(source = "userGroup.id", target = "userGroupId")
    @Mapping(source = "userGroup.groupName", target = "userGroupName")
    @Mapping(target = "roles", source = "roles") // Explicitly map roles
    @Mapping(target = "permissions", expression = "java(mapRolesToPermissions(user.getRoles()))")
    UserDTO toDto(User user);

    /**
     * Simplified mapping for list endpoint - no roles/permissions to avoid N+1 queries
     */
    @Mapping(source = "userGroup.groupName", target = "userGroupName")
    UserListDTO toListDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "userGroup", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "accountLockedUntil", ignore = true)
    @Mapping(target = "currentCaseCount", ignore = true)
    @Mapping(target = "failedLoginAttempts", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "otpExpiresAt", ignore = true)
    @Mapping(target = "otpSecret", ignore = true)
    @Mapping(target = "sessionExpiresAt", ignore = true)
    @Mapping(target = "sessionId", ignore = true)
    @Mapping(target = "passwordHash", source = "password")
    @Mapping(target = "isFirstLogin", ignore = true)
    User toEntity(CreateUserRequest createUserRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "isFirstLogin", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "userGroup", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "accountLockedUntil", ignore = true)
    @Mapping(target = "currentCaseCount", ignore = true)
    @Mapping(target = "failedLoginAttempts", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "otpExpiresAt", ignore = true)
    @Mapping(target = "otpSecret", ignore = true)
    @Mapping(target = "sessionExpiresAt", ignore = true)
    @Mapping(target = "sessionId", ignore = true)
    void updateEntityFromDto(UpdateUserRequest updateUserRequest, @MappingTarget User user);

    default Set<String> mapRolesToPermissions(Set<com.finx.management.domain.entity.Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(com.finx.management.domain.entity.Permission::getPermissionCode)
                .collect(Collectors.toSet());
    }
}
