package com.finx.strategyengineservice.controller;

import com.finx.strategyengineservice.domain.dto.CommunicationHistoryDTO;
import com.finx.strategyengineservice.domain.dto.CommonResponse;
import com.finx.strategyengineservice.domain.enums.CommunicationChannel;
import com.finx.strategyengineservice.domain.enums.CommunicationStatus;
import com.finx.strategyengineservice.service.CommunicationHistoryService;
import com.finx.strategyengineservice.util.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controller for communication history - tracks all sent communications (SMS, WhatsApp, Email, Notice)
 */
@RestController
@RequestMapping("/communication-history")
@RequiredArgsConstructor
public class CommunicationHistoryController {

    private final CommunicationHistoryService communicationHistoryService;

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<CommunicationHistoryDTO>> getById(@PathVariable Long id) {
        CommunicationHistoryDTO dto = communicationHistoryService.getById(id);
        return ResponseWrapper.ok("Communication history retrieved successfully", dto);
    }

    @GetMapping("/communication-id/{communicationId}")
    public ResponseEntity<CommonResponse<CommunicationHistoryDTO>> getByCommunicationId(
            @PathVariable String communicationId) {
        CommunicationHistoryDTO dto = communicationHistoryService.getByCommunicationId(communicationId);
        return ResponseWrapper.ok("Communication history retrieved successfully", dto);
    }

    @GetMapping("/case/{caseId}")
    public ResponseEntity<CommonResponse<List<CommunicationHistoryDTO>>> getByCaseId(@PathVariable Long caseId) {
        List<CommunicationHistoryDTO> list = communicationHistoryService.getByCaseId(caseId);
        return ResponseWrapper.ok("Communication history retrieved successfully", list);
    }

    @GetMapping("/case/{caseId}/channel/{channel}")
    public ResponseEntity<CommonResponse<List<CommunicationHistoryDTO>>> getByCaseIdAndChannel(
            @PathVariable Long caseId,
            @PathVariable CommunicationChannel channel) {
        List<CommunicationHistoryDTO> list = communicationHistoryService.getByCaseIdAndChannel(caseId, channel);
        return ResponseWrapper.ok("Communication history retrieved successfully", list);
    }

    /**
     * Get notices sent for a case - useful for "Notices Sent" tab
     */
    @GetMapping("/case/{caseId}/notices")
    public ResponseEntity<CommonResponse<List<CommunicationHistoryDTO>>> getNoticesByCaseId(
            @PathVariable Long caseId) {
        List<CommunicationHistoryDTO> list = communicationHistoryService.getNoticesByCaseId(caseId);
        return ResponseWrapper.ok("Notices retrieved successfully", list);
    }

    @GetMapping("/channel/{channel}")
    public ResponseEntity<CommonResponse<Page<CommunicationHistoryDTO>>> getByChannel(
            @PathVariable CommunicationChannel channel,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CommunicationHistoryDTO> page = communicationHistoryService.getByChannel(channel, pageable);
        return ResponseWrapper.ok("Communication history retrieved successfully", page);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<CommonResponse<Page<CommunicationHistoryDTO>>> getByStatus(
            @PathVariable CommunicationStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CommunicationHistoryDTO> page = communicationHistoryService.getByStatus(status, pageable);
        return ResponseWrapper.ok("Communication history retrieved successfully", page);
    }

    @GetMapping("/execution/{executionId}")
    public ResponseEntity<CommonResponse<Page<CommunicationHistoryDTO>>> getByExecutionId(
            @PathVariable Long executionId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CommunicationHistoryDTO> page = communicationHistoryService.getByExecutionId(executionId, pageable);
        return ResponseWrapper.ok("Communication history retrieved successfully", page);
    }

    @GetMapping("/channel/{channel}/date-range")
    public ResponseEntity<CommonResponse<Page<CommunicationHistoryDTO>>> getByChannelAndDateRange(
            @PathVariable CommunicationChannel channel,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CommunicationHistoryDTO> page = communicationHistoryService.getByChannelAndDateRange(
                channel, startDate, endDate, pageable);
        return ResponseWrapper.ok("Communication history retrieved successfully", page);
    }

    @GetMapping("/channel/{channel}/with-documents")
    public ResponseEntity<CommonResponse<Page<CommunicationHistoryDTO>>> getWithDocumentsByChannel(
            @PathVariable CommunicationChannel channel,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CommunicationHistoryDTO> page = communicationHistoryService.getWithDocumentsByChannel(channel, pageable);
        return ResponseWrapper.ok("Communications with documents retrieved successfully", page);
    }

    @GetMapping("/case/{caseId}/stats")
    public ResponseEntity<CommonResponse<List<Object[]>>> getStatsByCaseId(@PathVariable Long caseId) {
        List<Object[]> stats = communicationHistoryService.getStatsByCaseId(caseId);
        return ResponseWrapper.ok("Communication stats retrieved successfully", stats);
    }
}
