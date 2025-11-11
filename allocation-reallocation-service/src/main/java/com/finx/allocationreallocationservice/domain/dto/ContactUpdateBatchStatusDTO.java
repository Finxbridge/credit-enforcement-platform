package com.finx.allocationreallocationservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactUpdateBatchStatusDTO {
    private String batchId;
    private Integer totalRecords;
    private Integer successfulUpdates;
    private Integer failedUpdates;
    private String status;
}
