package com.finx.collectionsservice.service.impl;

import com.finx.collectionsservice.domain.dto.SettlementLetterDTO;
import com.finx.collectionsservice.domain.entity.OTSRequest;
import com.finx.collectionsservice.domain.entity.SettlementLetter;
import com.finx.collectionsservice.domain.enums.LetterStatus;
import com.finx.collectionsservice.domain.enums.OTSStatus;
import com.finx.collectionsservice.exception.BusinessException;
import com.finx.collectionsservice.exception.ResourceNotFoundException;
import com.finx.collectionsservice.mapper.CollectionsMapper;
import com.finx.collectionsservice.repository.OTSRequestRepository;
import com.finx.collectionsservice.repository.SettlementLetterRepository;
import com.finx.collectionsservice.service.SettlementLetterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SettlementLetterServiceImpl implements SettlementLetterService {

    private final SettlementLetterRepository letterRepository;
    private final OTSRequestRepository otsRepository;
    private final CollectionsMapper mapper;

    @Override
    @CacheEvict(value = "settlementLetters", allEntries = true)
    public SettlementLetterDTO generateLetter(Long otsId, Long templateId, Long generatedBy) {
        OTSRequest ots = otsRepository.findById(otsId)
                .orElseThrow(() -> new ResourceNotFoundException("OTS request not found with id: " + otsId));

        if (ots.getOtsStatus() != OTSStatus.APPROVED) {
            throw new BusinessException("Cannot generate letter for non-approved OTS request");
        }

        if (letterRepository.existsByOtsId(otsId)) {
            throw new BusinessException("Letter already exists for OTS: " + ots.getOtsNumber());
        }

        String letterNumber = generateLetterNumber();

        SettlementLetter letter = SettlementLetter.builder()
                .letterNumber(letterNumber)
                .otsId(otsId)
                .otsNumber(ots.getOtsNumber())
                .caseId(ots.getCaseId())
                .loanAccountNumber(ots.getLoanAccountNumber())
                .customerName(ots.getCustomerName())
                .originalOutstanding(ots.getOriginalOutstanding())
                .settlementAmount(ots.getProposedSettlement())
                .waiverAmount(ots.getDiscountAmount())
                .discountPercentage(ots.getDiscountPercentage())
                .paymentDeadline(ots.getPaymentDeadline() != null ? ots.getPaymentDeadline().atStartOfDay() : null)
                .templateId(templateId)
                .status(LetterStatus.GENERATED)
                .generatedAt(LocalDateTime.now())
                .generatedBy(generatedBy)
                .expiresAt(ots.getPaymentDeadline() != null ? ots.getPaymentDeadline().atStartOfDay() : null)
                .build();

        letter = letterRepository.save(letter);

        // Update OTS status
        ots.setOtsStatus(OTSStatus.LETTER_GENERATED);
        ots.setLetterGeneratedAt(LocalDateTime.now());
        ots.setLetterId(letter.getId());
        otsRepository.save(ots);

        log.info("Generated settlement letter {} for OTS {}", letterNumber, ots.getOtsNumber());
        return mapper.toSettlementLetterDTO(letter);
    }

    @Override
    @Cacheable(value = "settlementLetters", key = "#id")
    @Transactional(readOnly = true)
    public SettlementLetterDTO getLetterById(Long id) {
        SettlementLetter letter = letterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement letter not found with id: " + id));
        return mapper.toSettlementLetterDTO(letter);
    }

    @Override
    @Transactional(readOnly = true)
    public SettlementLetterDTO getLetterByNumber(String letterNumber) {
        SettlementLetter letter = letterRepository.findByLetterNumber(letterNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement letter not found with number: " + letterNumber));
        return mapper.toSettlementLetterDTO(letter);
    }

    @Override
    @Transactional(readOnly = true)
    public SettlementLetterDTO getLetterByOtsId(Long otsId) {
        SettlementLetter letter = letterRepository.findByOtsId(otsId)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement letter not found for OTS: " + otsId));
        return mapper.toSettlementLetterDTO(letter);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettlementLetterDTO> getLettersByCaseId(Long caseId) {
        return letterRepository.findByCaseId(caseId).stream()
                .map(mapper::toSettlementLetterDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SettlementLetterDTO> getAllLetters(Pageable pageable) {
        return letterRepository.findAll(pageable)
                .map(mapper::toSettlementLetterDTO);
    }

    @Override
    @CacheEvict(value = "settlementLetters", key = "#id")
    public SettlementLetterDTO downloadLetter(Long id, Long downloadedBy) {
        SettlementLetter letter = letterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement letter not found with id: " + id));

        letter.setDownloadedAt(LocalDateTime.now());
        letter.setDownloadedBy(downloadedBy);
        letter.setDownloadCount(letter.getDownloadCount() + 1);
        letter.setStatus(LetterStatus.DOWNLOADED);

        letter = letterRepository.save(letter);

        // Update OTS with download info
        otsRepository.findById(letter.getOtsId()).ifPresent(ots -> {
            ots.setLetterDownloadedAt(LocalDateTime.now());
            ots.setLetterDownloadedBy(downloadedBy);
            otsRepository.save(ots);
        });

        log.info("Letter {} downloaded by user {}", letter.getLetterNumber(), downloadedBy);
        return mapper.toSettlementLetterDTO(letter);
    }

    @Override
    @CacheEvict(value = "settlementLetters", key = "#id")
    public SettlementLetterDTO sendLetter(Long id, String sendVia, Long sentBy) {
        SettlementLetter letter = letterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement letter not found with id: " + id));

        letter.setSentAt(LocalDateTime.now());
        letter.setSentVia(sendVia);
        letter.setStatus(LetterStatus.SENT);

        letter = letterRepository.save(letter);
        log.info("Letter {} sent via {} by user {}", letter.getLetterNumber(), sendVia, sentBy);
        return mapper.toSettlementLetterDTO(letter);
    }

    @Override
    @Scheduled(cron = "0 0 0 * * *") // Daily at midnight
    public void markExpiredLetters() {
        log.info("Checking for expired settlement letters");
        List<SettlementLetter> expiredLetters = letterRepository.findExpiredLetters(LocalDateTime.now());

        for (SettlementLetter letter : expiredLetters) {
            letter.setStatus(LetterStatus.EXPIRED);
            letterRepository.save(letter);
            log.info("Marked letter {} as expired", letter.getLetterNumber());
        }
    }

    private String generateLetterNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uniquePart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "SL-" + datePart + "-" + uniquePart;
    }
}
