package com.finx.dmsservice.domain.dto;

import com.finx.dmsservice.domain.enums.DocumentStatus;
import com.finx.dmsservice.domain.enums.DocumentType;
import com.finx.dmsservice.domain.enums.EntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSearchRequest {
    private String searchText;
    private List<DocumentType> documentTypes;
    private List<DocumentStatus> statuses;
    private List<EntityType> entityTypes;
    private Long entityId;
    private Long categoryId;
    private List<String> tags;
    private Long uploadedBy;
    private LocalDateTime uploadedFrom;
    private LocalDateTime uploadedTo;
    private Long minSize;
    private Long maxSize;
    private String fileExtension;
}
