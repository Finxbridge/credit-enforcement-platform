package com.finx.masterdataservice.mapper;

import com.finx.masterdataservice.domain.dto.MasterDataDTO;
import com.finx.masterdataservice.domain.entity.MasterData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MasterDataMapper {

    MasterDataDTO toDto(MasterData masterData);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    MasterData toEntity(MasterDataDTO masterDataDTO);
}
