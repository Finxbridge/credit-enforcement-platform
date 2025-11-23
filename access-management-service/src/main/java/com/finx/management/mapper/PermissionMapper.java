package com.finx.management.mapper;

import com.finx.management.domain.dto.PermissionDTO;
import com.finx.management.domain.entity.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PermissionMapper {

    @Mapping(source = "permissionCode", target = "code")
    @Mapping(source = "permissionName", target = "name")
    PermissionDTO toDto(Permission permission);

    @Mapping(source = "code", target = "permissionCode")
    @Mapping(source = "name", target = "permissionName")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Permission toEntity(PermissionDTO permissionDTO);

    @Mapping(source = "code", target = "permissionCode")
    @Mapping(source = "name", target = "permissionName")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromDto(PermissionDTO permissionDTO, @MappingTarget Permission permission);
}
