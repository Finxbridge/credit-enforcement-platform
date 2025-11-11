package com.finx.allocationreallocationservice.service;

import com.finx.allocationreallocationservice.domain.dto.AllocationBatchStatusDTO;
import com.finx.allocationreallocationservice.domain.dto.AllocationBatchUploadResponseDTO;
import com.finx.allocationreallocationservice.domain.dto.ReallocationByAgentRequestDTO;
import com.finx.allocationreallocationservice.domain.dto.ReallocationByFilterRequestDTO;
import com.finx.allocationreallocationservice.domain.dto.ReallocationResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface ReallocationService {

    AllocationBatchUploadResponseDTO uploadReallocationBatch(MultipartFile file);

    AllocationBatchStatusDTO getReallocationBatchStatus(String batchId);

    byte[] exportFailedReallocationRows(String batchId);

    ReallocationResponseDTO reallocateByAgent(ReallocationByAgentRequestDTO request);

    ReallocationResponseDTO reallocateByFilter(ReallocationByFilterRequestDTO request);
}
