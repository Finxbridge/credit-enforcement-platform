package com.finx.collectionsservice.service.impl;

import com.finx.collectionsservice.client.DmsServiceClient;
import com.finx.collectionsservice.client.dto.DmsDocumentDTO;
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
import com.finx.collectionsservice.util.ByteArrayMultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.io.font.constants.StandardFonts;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
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
    private final DmsServiceClient dmsServiceClient;

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

        // Update OTS - letter generated (OTS stays APPROVED)
        ots.setLetterGeneratedAt(LocalDateTime.now());
        ots.setLetterId(letter.getId());
        otsRepository.save(ots);

        // Upload settlement letter to DMS
        uploadLetterToDms(letter, ots);

        log.info("Generated settlement letter {} for OTS {}", letterNumber, ots.getOtsNumber());
        return mapper.toSettlementLetterDTO(letter);
    }

    /**
     * Upload settlement letter PDF to DMS service
     */
    private void uploadLetterToDms(SettlementLetter letter, OTSRequest ots) {
        try {
            // Generate PDF content (placeholder - in real implementation, use a PDF generator)
            byte[] pdfContent = generateLetterPdfContent(letter, ots);

            String fileName = "Settlement_Letter_" + letter.getLetterNumber() + ".pdf";
            ByteArrayMultipartFile file = new ByteArrayMultipartFile(
                    pdfContent,
                    "file",
                    fileName,
                    "application/pdf"
            );

            // Upload to DMS with SETTLEMENT_LETTER channel
            var response = dmsServiceClient.uploadDocument(
                    file,
                    fileName,
                    "GENERATED",
                    "SETTLEMENT_LETTER",
                    ots.getCaseId(),
                    letter.getTemplateId()
            );

            if (response != null && response.getPayload() != null) {
                DmsDocumentDTO dmsDoc = response.getPayload();
                letter.setDmsDocumentId(dmsDoc.getDocumentId());
                letter.setPdfUrl(dmsDoc.getS3Url());
                letterRepository.save(letter);
                log.info("Settlement letter {} uploaded to DMS with documentId: {}",
                        letter.getLetterNumber(), dmsDoc.getDocumentId());
            }
        } catch (Exception e) {
            log.error("Failed to upload settlement letter {} to DMS: {}",
                    letter.getLetterNumber(), e.getMessage());
            // Don't fail the entire operation if DMS upload fails
        }
    }

    /**
     * Generate PDF content for settlement letter using iText 7
     */
    private byte[] generateLetterPdfContent(SettlementLetter letter, OTSRequest ots) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);
            document.setMargins(50, 50, 50, 50);

            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // Header
            DeviceRgb headerColor = new DeviceRgb(0, 51, 102);
            Paragraph header = new Paragraph("SETTLEMENT LETTER")
                    .setFont(boldFont)
                    .setFontSize(20)
                    .setFontColor(headerColor)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(30);
            document.add(header);

            // Letter details
            Paragraph letterInfo = new Paragraph()
                    .setFont(regularFont)
                    .setFontSize(11)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .add("Letter No: " + letter.getLetterNumber() + "\n")
                    .add("Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
            document.add(letterInfo);

            // Customer details section
            Paragraph customerSection = new Paragraph("CUSTOMER DETAILS")
                    .setFont(boldFont)
                    .setFontSize(12)
                    .setFontColor(headerColor)
                    .setMarginTop(20)
                    .setMarginBottom(10);
            document.add(customerSection);

            // Details table
            Table detailsTable = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                    .useAllAvailableWidth()
                    .setMarginBottom(20);

            addTableRow(detailsTable, "Customer Name:", ots.getCustomerName() != null ? ots.getCustomerName() : "N/A", regularFont, boldFont);
            addTableRow(detailsTable, "Loan Account Number:", ots.getLoanAccountNumber() != null ? ots.getLoanAccountNumber() : "N/A", regularFont, boldFont);
            addTableRow(detailsTable, "OTS Reference:", ots.getOtsNumber(), regularFont, boldFont);
            document.add(detailsTable);

            // Settlement details section
            Paragraph settlementSection = new Paragraph("SETTLEMENT DETAILS")
                    .setFont(boldFont)
                    .setFontSize(12)
                    .setFontColor(headerColor)
                    .setMarginTop(10)
                    .setMarginBottom(10);
            document.add(settlementSection);

            Table settlementTable = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                    .useAllAvailableWidth()
                    .setMarginBottom(20);

            BigDecimal originalOutstanding = letter.getOriginalOutstanding() != null ? letter.getOriginalOutstanding() : BigDecimal.ZERO;
            BigDecimal settlementAmount = letter.getSettlementAmount() != null ? letter.getSettlementAmount() : BigDecimal.ZERO;
            BigDecimal waiverAmount = letter.getWaiverAmount() != null ? letter.getWaiverAmount() : BigDecimal.ZERO;
            BigDecimal discountPercentage = letter.getDiscountPercentage() != null ? letter.getDiscountPercentage() : BigDecimal.ZERO;

            addTableRow(settlementTable, "Original Outstanding:", String.format("Rs. %,.2f", originalOutstanding), regularFont, boldFont);
            addTableRow(settlementTable, "Settlement Amount:", String.format("Rs. %,.2f", settlementAmount), regularFont, boldFont);
            addTableRow(settlementTable, "Waiver Amount:", String.format("Rs. %,.2f", waiverAmount), regularFont, boldFont);
            addTableRow(settlementTable, "Discount Percentage:", String.format("%.2f%%", discountPercentage), regularFont, boldFont);

            if (letter.getPaymentDeadline() != null) {
                addTableRow(settlementTable, "Payment Deadline:",
                        letter.getPaymentDeadline().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")), regularFont, boldFont);
            }
            document.add(settlementTable);

            // Terms and conditions
            Paragraph termsHeader = new Paragraph("TERMS AND CONDITIONS")
                    .setFont(boldFont)
                    .setFontSize(12)
                    .setFontColor(headerColor)
                    .setMarginTop(20)
                    .setMarginBottom(10);
            document.add(termsHeader);

            String[] terms = {
                    "This settlement offer is valid only if full payment is received by the payment deadline mentioned above.",
                    "Upon receipt of full settlement amount, the loan account will be marked as settled.",
                    "This letter is system generated and does not require signature.",
                    "For any queries, please contact our customer support."
            };

            for (int i = 0; i < terms.length; i++) {
                Paragraph term = new Paragraph((i + 1) + ". " + terms[i])
                        .setFont(regularFont)
                        .setFontSize(10)
                        .setMarginBottom(5);
                document.add(term);
            }

            // Footer
            Paragraph footer = new Paragraph()
                    .setFont(regularFont)
                    .setFontSize(9)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(40)
                    .add("This is a system generated document.\n")
                    .add("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
            document.add(footer);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating PDF for letter {}: {}", letter.getLetterNumber(), e.getMessage());
            throw new BusinessException("Failed to generate PDF: " + e.getMessage());
        }
    }

    /**
     * Helper method to add a row to the table
     */
    private void addTableRow(Table table, String label, String value, PdfFont regularFont, PdfFont boldFont) {
        Cell labelCell = new Cell()
                .add(new Paragraph(label).setFont(boldFont).setFontSize(10))
                .setBorder(Border.NO_BORDER)
                .setPadding(5);
        Cell valueCell = new Cell()
                .add(new Paragraph(value).setFont(regularFont).setFontSize(10))
                .setBorder(Border.NO_BORDER)
                .setPadding(5);
        table.addCell(labelCell);
        table.addCell(valueCell);
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
    @Transactional(readOnly = true)
    public byte[] getLetterPdfContent(Long id) {
        SettlementLetter letter = letterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement letter not found with id: " + id));

        // If DMS document exists, try to fetch from DMS first
        if (letter.getDmsDocumentId() != null) {
            try {
                // Try to get the document ID (numeric) from the repository or DMS
                var dmsResponse = dmsServiceClient.getDocumentByDocumentId(letter.getDmsDocumentId());
                if (dmsResponse != null && dmsResponse.getPayload() != null) {
                    Long dmsId = dmsResponse.getPayload().getId();
                    byte[] content = dmsServiceClient.getDocumentContent(dmsId, null);
                    if (content != null && content.length > 0) {
                        return content;
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to fetch PDF from DMS for letter {}: {}", letter.getLetterNumber(), e.getMessage());
            }
        }

        // Fallback: Generate PDF on-the-fly
        OTSRequest ots = otsRepository.findById(letter.getOtsId())
                .orElseThrow(() -> new ResourceNotFoundException("OTS request not found for letter: " + letter.getLetterNumber()));

        return generateLetterPdfContent(letter, ots);
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
