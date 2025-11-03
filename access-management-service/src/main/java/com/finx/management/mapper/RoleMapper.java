package com.finx.management.mapper;

import com.finx.management.domain.dto.CreateRoleRequest;
import com.finx.management.domain.dto.RoleDTO;
import com.finx.management.domain.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = {
        PermissionMapper.class }, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RoleMapper {

    @Mapping(source = "roleName", target = "name")
    @Mapping(source = "roleCode", target = "code")
    @Mapping(source = "roleName", target = "displayName")
    @Mapping(source = "isActive", target = "status", qualifiedByName = "booleanToString")
    RoleDTO toDto(Role role);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "roleGroup", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    @Mapping(source = "name", target = "roleName")
    @Mapping(source = "name", target = "roleCode") // Use name for roleCode
    @Mapping(source = "status", target = "isActive")
    Role toEntity(CreateRoleRequest createRoleRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roleCode", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "roleGroup", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    @Mapping(source = "name", target = "roleName")
    @Mapping(source = "status", target = "isActive", qualifiedByName = "stringToBoolean")
    void updateEntityFromDto(RoleDTO roleDTO, @MappingTarget Role role);

    @Named("booleanToString")
    default String booleanToString(Boolean value) {
        return value != null && value ? "ACTIVE" : "INACTIVE";
    }

    @Named("stringToBoolean")
    default Boolean stringToBoolean(String value) {
        return "ACTIVE".equalsIgnoreCase(value);
    }
}
