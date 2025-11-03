package com.finx.management.mapper;

import com.finx.management.domain.dto.PermissionDTO;
import com.finx.management.domain.entity.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PermissionMapper {

    @Mapping(source = "permissionCode", target = "code")
    @Mapping(source = "permissionName", target = "name")
    @Mapping(target = "category", ignore = true) // Ignore unmapped target property
    @Mapping(target = "status", ignore = true) // Ignore unmapped target property
    PermissionDTO toDto(Permission permission);
}
