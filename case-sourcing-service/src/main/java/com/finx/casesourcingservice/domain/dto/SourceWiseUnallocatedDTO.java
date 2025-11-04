package com.finx.casesourcingservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for source-wise unallocated cases
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SourceWiseUnallocatedDTO {

    private String source;
    private Long count;
}
