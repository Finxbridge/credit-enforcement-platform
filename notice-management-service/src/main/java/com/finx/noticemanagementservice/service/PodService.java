package com.finx.noticemanagementservice.service;

import com.finx.noticemanagementservice.domain.dto.ProofOfDeliveryDTO;
import com.finx.noticemanagementservice.domain.dto.UploadPodRequest;
import com.finx.noticemanagementservice.domain.dto.VerifyPodRequest;
import com.finx.noticemanagementservice.domain.enums.PodVerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface PodService {

    ProofOfDeliveryDTO uploadPod(UploadPodRequest request);

    ProofOfDeliveryDTO getPodById(Long id);

    ProofOfDeliveryDTO getPodByNumber(String podNumber);

    ProofOfDeliveryDTO getPodByNoticeId(Long noticeId);

    List<ProofOfDeliveryDTO> getPodsByVendor(Long vendorId);

    Page<ProofOfDeliveryDTO> getPodsByVerificationStatus(PodVerificationStatus status, Pageable pageable);

    Page<ProofOfDeliveryDTO> getPodsByUploadDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<ProofOfDeliveryDTO> getPodsByDeliveryDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<ProofOfDeliveryDTO> getAllPods(Pageable pageable);

    List<ProofOfDeliveryDTO> getPendingVerifications();

    ProofOfDeliveryDTO verifyPod(VerifyPodRequest request);

    ProofOfDeliveryDTO approvePod(Long podId, String remarks, Long verifiedBy);

    ProofOfDeliveryDTO rejectPod(Long podId, String rejectionReason, Long verifiedBy);

    Long countByVerificationStatus(PodVerificationStatus status);

    void deletePod(Long id);
}
