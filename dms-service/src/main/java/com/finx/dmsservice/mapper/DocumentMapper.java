package com.finx.dmsservice.mapper;

import com.finx.dmsservice.domain.dto.*;
import com.finx.dmsservice.domain.entity.Document;
import com.finx.dmsservice.domain.entity.DocumentAccessLog;
import com.finx.dmsservice.domain.entity.DocumentCategory;
import com.finx.dmsservice.domain.entity.DocumentExportJob;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    // Document mappings
    @Mapping(target = "categoryName", ignore = true)
    DocumentDTO toDto(Document document);

    List<DocumentDTO> toDtoList(List<Document> documents);

    // DocumentCategory mappings
    @Mapping(target = "parentCategoryName", ignore = true)
    DocumentCategoryDTO toCategoryDto(DocumentCategory category);

    List<DocumentCategoryDTO> toCategoryDtoList(List<DocumentCategory> categories);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    DocumentCategory toEntity(CreateCategoryRequest request);

    // DocumentAccessLog mappings
    @Mapping(target = "documentName", ignore = true)
    DocumentAccessLogDTO toAccessLogDto(DocumentAccessLog log);

    List<DocumentAccessLogDTO> toAccessLogDtoList(List<DocumentAccessLog> logs);

    // DocumentExportJob mappings
    DocumentExportJobDTO toDto(DocumentExportJob job);

    List<DocumentExportJobDTO> toExportJobDtoList(List<DocumentExportJob> jobs);
}
