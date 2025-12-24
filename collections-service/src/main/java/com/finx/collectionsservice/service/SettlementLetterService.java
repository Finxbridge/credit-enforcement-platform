package com.finx.collectionsservice.service;

import com.finx.collectionsservice.domain.dto.SettlementLetterDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SettlementLetterService {

    SettlementLetterDTO generateLetter(Long otsId, Long templateId, Long generatedBy);

    SettlementLetterDTO getLetterById(Long id);

    SettlementLetterDTO getLetterByNumber(String letterNumber);

    SettlementLetterDTO getLetterByOtsId(Long otsId);

    List<SettlementLetterDTO> getLettersByCaseId(Long caseId);

    Page<SettlementLetterDTO> getAllLetters(Pageable pageable);

    SettlementLetterDTO downloadLetter(Long id, Long downloadedBy);

    SettlementLetterDTO sendLetter(Long id, String sendVia, Long sentBy);

    /**
     * Get the PDF content for a settlement letter
     * If stored in DMS, fetches from DMS; otherwise generates on-the-fly
     */
    byte[] getLetterPdfContent(Long id);

    void markExpiredLetters();
}
