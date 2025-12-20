package com.finx.collectionsservice.service;

import com.finx.collectionsservice.domain.dto.CreateOTSRequest;
import com.finx.collectionsservice.domain.dto.OTSRequestDTO;
import com.finx.collectionsservice.domain.enums.OTSStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for OTS (One-Time Settlement) management
 */
public interface OTSService {

    /**
     * Create an OTS request
     */
    OTSRequestDTO createOTSRequest(CreateOTSRequest request, Long userId);

    /**
     * Get OTS by ID
     */
    OTSRequestDTO getOTSById(Long otsId);

    /**
     * Get OTS by number
     */
    OTSRequestDTO getOTSByNumber(String otsNumber);

    /**
     * Get all OTS requests for a case
     */
    List<OTSRequestDTO> getOTSByCase(Long caseId);

    /**
     * Get OTS requests by status
     */
    Page<OTSRequestDTO> getOTSByStatus(OTSStatus status, Pageable pageable);

    /**
     * Approve OTS request
     */
    OTSRequestDTO approveOTS(Long otsId, Long approverId, String comments);

    /**
     * Reject OTS request
     */
    OTSRequestDTO rejectOTS(Long otsId, Long approverId, String reason);

    /**
     * Cancel OTS request
     */
    OTSRequestDTO cancelOTS(Long otsId, Long userId, String reason);

    /**
     * Get pending OTS approvals
     */
    Page<OTSRequestDTO> getPendingApprovals(Pageable pageable);

    /**
     * Process expired OTS requests
     */
    Integer processExpiredOTS();
}
