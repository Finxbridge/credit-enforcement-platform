package com.finx.allocationreallocationservice.client;

import com.finx.allocationreallocationservice.client.dto.CustomPageImpl;
import com.finx.allocationreallocationservice.client.dto.UnallocatedCaseDTO;
import com.finx.allocationreallocationservice.domain.dto.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "case-sourcing-service", path = "/case")
public interface CaseSourcingClient {

    @GetMapping("/unallocated")
    CommonResponse<CustomPageImpl<UnallocatedCaseDTO>> getUnallocatedCases(@RequestParam("page") int page, @RequestParam("size") int size);

    @GetMapping("/unallocated")
    CommonResponse<CustomPageImpl<UnallocatedCaseDTO>> getUnallocatedCasesByGeography(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(value = "geography", required = false) List<String> geographies);
}
