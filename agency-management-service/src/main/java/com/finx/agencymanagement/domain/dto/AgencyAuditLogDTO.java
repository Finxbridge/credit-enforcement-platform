package com.finx.agencymanagement.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Agency Audit Log DTO
 * Matches frontend agency.types.ts AgencyAuditLog interface
 *
 * @author Naveen Manyam
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgencyAuditLogDTO {

    private Long id;

    @JsonProperty("agencyId")
    private Long agencyId;

    @JsonProperty("eventType")
    private String eventType;

    @JsonProperty("description")
    private String description;

    @JsonProperty("performedBy")
    private Long performedBy;

    @JsonProperty("performedAt")
    private LocalDateTime performedAt;

    @JsonProperty("oldValue")
    private String oldValue;

    @JsonProperty("newValue")
    private String newValue;
}
