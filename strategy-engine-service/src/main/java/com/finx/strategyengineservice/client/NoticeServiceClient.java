package com.finx.strategyengineservice.client;

import com.finx.strategyengineservice.client.dto.CreateNoticeRequest;
import com.finx.strategyengineservice.client.dto.NoticeDTO;
import com.finx.strategyengineservice.domain.dto.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Feign Client for Notice Management Service
 * Used to create and track notices with document attachments
 */
@FeignClient(name = "notice-management-service", url = "${NOTICE_SERVICE_URL:http://localhost:8091}")
public interface NoticeServiceClient {

    /**
     * Create a new notice with optional document attachment
     */
    @PostMapping("/notices")
    CommonResponse<NoticeDTO> createNotice(@RequestBody CreateNoticeRequest request);

    /**
     * Get notice by ID
     */
    @GetMapping("/notices/{id}")
    CommonResponse<NoticeDTO> getNoticeById(@PathVariable("id") Long id);

    /**
     * Get notice by notice number
     */
    @GetMapping("/notices/number/{noticeNumber}")
    CommonResponse<NoticeDTO> getNoticeByNumber(@PathVariable("noticeNumber") String noticeNumber);

    /**
     * Get all notices for a case
     */
    @GetMapping("/notices/case/{caseId}")
    CommonResponse<List<NoticeDTO>> getNoticesByCaseId(@PathVariable("caseId") Long caseId);

    /**
     * Get notices by loan account number
     */
    @GetMapping("/notices/loan/{loanAccountNumber}")
    CommonResponse<List<NoticeDTO>> getNoticesByLoanAccountNumber(@PathVariable("loanAccountNumber") String loanAccountNumber);
}
