package com.finx.casesourcingservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentUploadDTO {
    private String batchId;
    private String source;
    private String uploadedBy;
    private LocalDateTime uploadedAt;
    private Integer totalCases;
    private String status;
}
