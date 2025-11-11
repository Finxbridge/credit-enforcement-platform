package com.finx.allocationreallocationservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactUpdateResponseDTO {
    private Long caseId;
    private Boolean success;
    private String message;
}
