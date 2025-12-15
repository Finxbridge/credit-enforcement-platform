package com.finx.dmsservice.service;

import com.finx.dmsservice.domain.dto.DocumentDTO;
import com.finx.dmsservice.domain.dto.DocumentSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DocumentSearchService {

    Page<DocumentDTO> search(DocumentSearchRequest request, Pageable pageable);

    Page<DocumentDTO> searchByText(String searchText, Pageable pageable);

    List<DocumentDTO> searchByTags(List<String> tags);

    Page<DocumentDTO> advancedSearch(DocumentSearchRequest request, Pageable pageable);

    long countSearchResults(DocumentSearchRequest request);
}
