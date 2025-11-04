package com.finx.casesourcingservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for bucket-wise unallocated cases
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BucketWiseUnallocatedDTO {

    private String bucket;
    private Long count;
}
