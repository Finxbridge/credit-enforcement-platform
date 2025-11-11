package com.finx.allocationreallocationservice.service.async;

public interface AllocationBatchProcessingService {
    void processAllocationBatchAsync(String batchId, String filePath);
    void processReallocationBatchAsync(String batchId, String filePath);
    void processContactUpdateBatchAsync(String batchId, String filePath);
}
