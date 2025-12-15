package com.finx.dmsservice.domain.dto;

import com.finx.dmsservice.domain.enums.ExportFormat;
import com.finx.dmsservice.domain.enums.ExportType;
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
public class CreateExportJobRequest {

    @NotNull(message = "Export type is required")
    private ExportType exportType;

    @NotNull(message = "Export format is required")
    private ExportFormat exportFormat;

    private Map<String, Object> filterCriteria;

    private List<Long> documentIds;

    private Long createdBy;
}
