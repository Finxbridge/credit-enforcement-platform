package com.finx.dmsservice.domain.dto;

import com.finx.dmsservice.domain.enums.DocumentType;
import com.finx.dmsservice.domain.enums.EntityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadDocumentRequest {

    @NotNull(message = "Document type is required")
    private DocumentType documentType;

    private String documentSubtype;

    private Long categoryId;

    @NotNull(message = "Entity type is required")
    private EntityType entityType;

    @NotNull(message = "Entity ID is required")
    private Long entityId;

    @NotBlank(message = "Document name is required")
    private String documentName;

    private String description;

    private Map<String, Object> metadata;

    private List<String> tags;

    private Integer retentionDays;

    private Long createdBy;
}
