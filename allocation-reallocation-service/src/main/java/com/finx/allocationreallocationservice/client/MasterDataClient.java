package com.finx.allocationreallocationservice.client;

import com.finx.allocationreallocationservice.client.dto.ContactUpdateRequestDTO;
import com.finx.allocationreallocationservice.client.dto.ContactUpdateResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "master-data-service", path = "/api/v1/borrowers")
public interface MasterDataClient {

    @PutMapping("/{caseId}/contact-info")
    ContactUpdateResponseDTO updateBorrowerContactInfo(
            @PathVariable("caseId") Long caseId,
            @RequestBody ContactUpdateRequestDTO request);
}
