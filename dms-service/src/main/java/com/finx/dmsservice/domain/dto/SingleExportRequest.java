package com.finx.dmsservice.domain.dto;

import com.finx.dmsservice.domain.enums.ExportFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SingleExportRequest {

    @NotNull(message = "Document ID is required")
    private Long documentId;

    @NotNull(message = "Export format is required")
    private ExportFormat format;

    // Optional: Convert to different format
    private Boolean convertToPdf;

    // Optional: Add watermark
    private Boolean addWatermark;
    private String watermarkText;

    // Optional: Password protect
    private Boolean passwordProtect;
    private String password;

    // Optional: Resize/compress images
    private Boolean compressImages;
    private Integer quality; // 1-100
}
