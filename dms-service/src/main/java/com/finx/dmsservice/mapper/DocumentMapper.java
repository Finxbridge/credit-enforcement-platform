package com.finx.dmsservice.mapper;

import com.finx.dmsservice.domain.dto.DocumentAccessLogDTO;
import com.finx.dmsservice.domain.dto.DocumentDTO;
import com.finx.dmsservice.domain.entity.Document;
import com.finx.dmsservice.domain.entity.DocumentAccessLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for Document entity
 */
@Mapper(componentModel = "spring")
public interface DocumentMapper {

    // Document mappings
    DocumentDTO toDto(Document document);

    List<DocumentDTO> toDtoList(List<Document> documents);

    // DocumentAccessLog mappings
    @Mapping(target = "documentName", ignore = true)
    DocumentAccessLogDTO toAccessLogDto(DocumentAccessLog log);

    List<DocumentAccessLogDTO> toAccessLogDtoList(List<DocumentAccessLog> logs);
}
