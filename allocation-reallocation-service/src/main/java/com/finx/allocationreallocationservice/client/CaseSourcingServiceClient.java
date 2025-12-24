package com.finx.allocationreallocationservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Feign client for communicating with case-sourcing-service.
 * Used to notify case-sourcing-service about cache invalidation after allocation/reallocation.
 */
@FeignClient(
        name = "case-sourcing-service",
        url = "${CASE_SOURCING_SERVICE_URL:http://localhost:8082}"
)
public interface CaseSourcingServiceClient {

    /**
     * Evict unallocated cases cache in case-sourcing-service.
     * Should be called after case allocation or reallocation to ensure
     * the unallocated cases list is refreshed.
     */
    @PostMapping("/case/source/cache/evict/unallocated")
    void evictUnallocatedCasesCache();
}
