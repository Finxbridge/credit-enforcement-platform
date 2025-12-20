package com.finx.communication.repository;

import com.finx.communication.domain.model.OtpRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OtpRequestRepository extends JpaRepository<OtpRequest, Long> {

    Optional<OtpRequest> findByRequestId(String requestId);

    Optional<OtpRequest> findTopByMobileAndStatusOrderByCreatedAtDesc(String mobile, String status);

    List<OtpRequest> findByMobileAndStatusAndExpiresAtAfter(String mobile, String status, LocalDateTime currentTime);

    Optional<OtpRequest> findTopByMobileAndPurposeOrderByCreatedAtDesc(String mobile, String purpose);
}
