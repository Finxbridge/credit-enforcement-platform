package com.finx.noticemanagementservice.domain.dto;

import com.finx.noticemanagementservice.domain.enums.DispatchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkDownloadRequest {

    private List<Long> dispatchIds;

    // Filters (if dispatchIds not provided)
    private DispatchStatus status;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String pincode;
    private String city;
    private Boolean includePreviouslyDownloaded;

    // Download options
    private Boolean includeManifest;
    private Boolean separateFoldersByPincode;
    private String fileFormat; // PDF, ZIP
}
