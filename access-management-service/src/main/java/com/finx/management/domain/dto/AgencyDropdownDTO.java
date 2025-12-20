package com.finx.management.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple DTO for agency dropdown in user creation form.
 * Used when creating a user with AGENT role.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgencyDropdownDTO {
    private Long id;
    private String agencyCode;
    private String agencyName;
}
