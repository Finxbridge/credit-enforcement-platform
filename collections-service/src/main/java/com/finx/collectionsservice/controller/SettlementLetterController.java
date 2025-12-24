package com.finx.collectionsservice.controller;

import com.finx.collectionsservice.domain.dto.CommonResponse;
import com.finx.collectionsservice.domain.dto.SettlementLetterDTO;
import com.finx.collectionsservice.service.SettlementLetterService;
import com.finx.collectionsservice.util.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/collections/settlement-letters")
@RequiredArgsConstructor
@Tag(name = "Settlement Letters", description = "APIs for OTS settlement letter management")
public class SettlementLetterController {

    private final SettlementLetterService letterService;

    @PostMapping("/generate")
    @Operation(summary = "Generate letter", description = "Generate a settlement letter for approved OTS")
    public ResponseEntity<CommonResponse<SettlementLetterDTO>> generateLetter(
            @RequestParam Long otsId,
            @RequestParam Long templateId,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("POST /settlement-letters/generate - Generating letter for OTS: {}", otsId);
        SettlementLetterDTO response = letterService.generateLetter(otsId, templateId, userId);
        return ResponseWrapper.created("Settlement letter generated successfully", response);
    }

    @GetMapping("/{letterId}")
    @Operation(summary = "Get letter by ID", description = "Get settlement letter details")
    public ResponseEntity<CommonResponse<SettlementLetterDTO>> getLetter(@PathVariable Long letterId) {
        log.info("GET /settlement-letters/{} - Fetching letter", letterId);
        SettlementLetterDTO response = letterService.getLetterById(letterId);
        return ResponseWrapper.ok("Settlement letter retrieved successfully", response);
    }

    @GetMapping("/number/{letterNumber}")
    @Operation(summary = "Get letter by number", description = "Get settlement letter by letter number")
    public ResponseEntity<CommonResponse<SettlementLetterDTO>> getLetterByNumber(
            @PathVariable String letterNumber) {
        log.info("GET /settlement-letters/number/{} - Fetching letter", letterNumber);
        SettlementLetterDTO response = letterService.getLetterByNumber(letterNumber);
        return ResponseWrapper.ok("Settlement letter retrieved successfully", response);
    }

    @GetMapping("/ots/{otsId}")
    @Operation(summary = "Get letter by OTS", description = "Get settlement letter for an OTS request")
    public ResponseEntity<CommonResponse<SettlementLetterDTO>> getLetterByOtsId(@PathVariable Long otsId) {
        log.info("GET /settlement-letters/ots/{} - Fetching letter", otsId);
        SettlementLetterDTO response = letterService.getLetterByOtsId(otsId);
        return ResponseWrapper.ok("Settlement letter retrieved successfully", response);
    }

    @GetMapping("/case/{caseId}")
    @Operation(summary = "Get case letters", description = "Get all settlement letters for a case")
    public ResponseEntity<CommonResponse<List<SettlementLetterDTO>>> getLettersByCaseId(
            @PathVariable Long caseId) {
        log.info("GET /settlement-letters/case/{} - Fetching case letters", caseId);
        List<SettlementLetterDTO> letters = letterService.getLettersByCaseId(caseId);
        return ResponseWrapper.ok("Settlement letters retrieved successfully", letters);
    }

    @GetMapping
    @Operation(summary = "Get all letters", description = "Get all settlement letters with pagination")
    public ResponseEntity<CommonResponse<Page<SettlementLetterDTO>>> getAllLetters(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /settlement-letters - Fetching all letters");
        Page<SettlementLetterDTO> letters = letterService.getAllLetters(pageable);
        return ResponseWrapper.ok("Settlement letters retrieved successfully", letters);
    }

    @PostMapping("/{letterId}/download")
    @Operation(summary = "Download letter metadata", description = "Mark letter as downloaded and return details")
    public ResponseEntity<CommonResponse<SettlementLetterDTO>> downloadLetter(
            @PathVariable Long letterId,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("POST /settlement-letters/{}/download - Downloading letter", letterId);
        SettlementLetterDTO response = letterService.downloadLetter(letterId, userId);
        return ResponseWrapper.ok("Settlement letter downloaded", response);
    }

    @GetMapping("/{letterId}/pdf")
    @Operation(summary = "Download letter PDF", description = "Download the actual PDF file")
    public ResponseEntity<byte[]> downloadLetterPdf(
            @PathVariable Long letterId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        log.info("GET /settlement-letters/{}/pdf - Downloading PDF", letterId);

        SettlementLetterDTO letter = letterService.getLetterById(letterId);
        byte[] pdfContent = letterService.getLetterPdfContent(letterId);

        // Mark as downloaded
        if (userId != null) {
            letterService.downloadLetter(letterId, userId);
        }

        String fileName = "Settlement_Letter_" + letter.getLetterNumber() + ".pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", fileName);
        headers.setContentLength(pdfContent.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfContent);
    }

    @PostMapping("/{letterId}/send")
    @Operation(summary = "Send letter", description = "Send settlement letter via specified channel")
    public ResponseEntity<CommonResponse<SettlementLetterDTO>> sendLetter(
            @PathVariable Long letterId,
            @RequestParam String sendVia,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("POST /settlement-letters/{}/send - Sending letter via {}", letterId, sendVia);
        SettlementLetterDTO response = letterService.sendLetter(letterId, sendVia, userId);
        return ResponseWrapper.ok("Settlement letter sent successfully", response);
    }
}
