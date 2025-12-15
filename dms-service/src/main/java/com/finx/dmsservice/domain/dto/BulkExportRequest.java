package com.finx.dmsservice.domain.dto;

import com.finx.dmsservice.domain.enums.ExportFormat;
import jakarta.validation.constraints.NotEmpty;
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
public class BulkExportRequest {

    @NotEmpty(message = "At least one document ID is required")
    private List<Long> documentIds;

    @NotNull(message = "Export format is required")
    private ExportFormat format;

    // ZIP packaging options
    private Boolean createZip;
    private String zipFileName;

    // Include metadata file (CSV/JSON summary)
    private Boolean includeMetadata;
    private String metadataFormat; // CSV or JSON

    // Folder structure in ZIP
    private Boolean organizeByType;
    private Boolean organizeByDate;

    // Processing options
    private Boolean convertToPdf;
    private Boolean addWatermark;
    private String watermarkText;

    // Search criteria (alternative to documentIds)
    private Map<String, Object> searchCriteria;

    // Notification
    private String notifyEmail;
    private Boolean notifyOnComplete;
}
