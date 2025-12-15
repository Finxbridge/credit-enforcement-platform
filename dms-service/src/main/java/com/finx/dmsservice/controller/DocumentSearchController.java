package com.finx.dmsservice.controller;

import com.finx.dmsservice.domain.dto.CommonResponse;
import com.finx.dmsservice.domain.dto.DocumentDTO;
import com.finx.dmsservice.domain.dto.DocumentSearchRequest;
import com.finx.dmsservice.service.DocumentSearchService;
import com.finx.dmsservice.util.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/documents/search")
@RequiredArgsConstructor
public class DocumentSearchController {

    private final DocumentSearchService searchService;

    @PostMapping
    public ResponseEntity<CommonResponse<Page<DocumentDTO>>> search(
            @RequestBody DocumentSearchRequest request,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("POST /documents/search - Searching documents");
        Page<DocumentDTO> documents = searchService.search(request, pageable);
        return ResponseWrapper.ok("Search completed successfully", documents);
    }

    @GetMapping("/text")
    public ResponseEntity<CommonResponse<Page<DocumentDTO>>> searchByText(
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /documents/search/text - Searching by text: {}", query);
        Page<DocumentDTO> documents = searchService.searchByText(query, pageable);
        return ResponseWrapper.ok("Search completed successfully", documents);
    }

    @GetMapping("/tags")
    public ResponseEntity<CommonResponse<List<DocumentDTO>>> searchByTags(
            @RequestParam List<String> tags) {
        log.info("GET /documents/search/tags - Searching by tags: {}", tags);
        List<DocumentDTO> documents = searchService.searchByTags(tags);
        return ResponseWrapper.ok("Search completed successfully", documents);
    }

    @PostMapping("/advanced")
    public ResponseEntity<CommonResponse<Page<DocumentDTO>>> advancedSearch(
            @RequestBody DocumentSearchRequest request,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("POST /documents/search/advanced - Advanced search");
        Page<DocumentDTO> documents = searchService.advancedSearch(request, pageable);
        return ResponseWrapper.ok("Advanced search completed successfully", documents);
    }

    @PostMapping("/count")
    public ResponseEntity<CommonResponse<Long>> countSearchResults(
            @RequestBody DocumentSearchRequest request) {
        log.info("POST /documents/search/count - Counting search results");
        long count = searchService.countSearchResults(request);
        return ResponseWrapper.ok("Count completed successfully", count);
    }
}
