package com.finx.management.mapper;

import com.finx.management.domain.dto.MasterDataDTO;
import com.finx.management.domain.entity.MasterData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MasterDataMapper {

    @Mapping(source = "dataType", target = "dataType")
    @Mapping(source = "code", target = "code")
    @Mapping(source = "value", target = "value")
    @Mapping(source = "parentCode", target = "parentCode")
    @Mapping(source = "displayOrder", target = "displayOrder")
    @Mapping(source = "isActive", target = "isActive")
    @Mapping(source = "metadata", target = "metadata")
    MasterDataDTO toDto(MasterData masterData);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    MasterData toEntity(MasterDataDTO masterDataDTO);
}
