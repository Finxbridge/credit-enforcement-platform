package com.finx.collectionsservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for DMS Document response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DmsDocumentDTO {
    private Long id;
    private String documentId;
    private String documentName;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String s3Key;
    private String s3Url;
    private String documentCategory;
    private String channel;
    private Long caseId;
    private Long sourceTemplateId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
