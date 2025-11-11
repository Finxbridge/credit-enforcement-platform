package com.finx.allocationreallocationservice.client.dto;

import com.finx.allocationreallocationservice.client.dto.CustomerDTO;
import com.finx.allocationreallocationservice.client.dto.LoanDetailsDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnallocatedCaseDTO {
    private Long id;
    private String caseNumber;
    private String externalCaseId;
    private CustomerDTO customer;
    private LoanDetailsDTO loanDetails;
    private String geography;
    private String bucket;
    private String status;
    private LocalDateTime createdAt;
}
