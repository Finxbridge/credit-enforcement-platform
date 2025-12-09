package com.finx.templatemanagementservice.service.impl;

import com.finx.templatemanagementservice.exception.BusinessException;
import com.finx.templatemanagementservice.service.DocumentProcessingService;
import com.finx.templatemanagementservice.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of DocumentProcessingService
 * Handles placeholder replacement in PDF and DOC/DOCX files
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentProcessingServiceImpl implements DocumentProcessingService {

    private final FileStorageService fileStorageService;
    private final LocalFileStorageServiceImpl localFileStorage;

    @Value("${file.storage.base-path:./uploads}")
    private String basePath;

    // Placeholder pattern: {variable_name} or {{variable_name}}
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{?([a-zA-Z_][a-zA-Z0-9_]*)\\}?\\}");

    // Image placeholder pattern: {IMG:variable_name} or {{IMG:variable_name}}
    private static final Pattern IMAGE_PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{?IMG:([a-zA-Z_][a-zA-Z0-9_]*)\\}?\\}");

    // Known image placeholders (can be extended)
    private static final Set<String> IMAGE_PLACEHOLDER_KEYS = Set.of(
            "companyLogo", "company_logo", "logo",
            "signature", "customer_signature", "authorized_signature",
            "stamp", "company_stamp", "seal",
            "qrCode", "qr_code", "barcode"
    );

    @Override
    public String processDocument(String documentUrl, Map<String, Object> variables, Long caseId) {
        log.info("Processing document: {} for case: {}", documentUrl, caseId);

        if (documentUrl == null || documentUrl.isEmpty()) {
            throw new BusinessException("Document URL is required");
        }

        String documentType = getDocumentType(documentUrl);
        if (documentType == null) {
            throw new BusinessException("Unsupported document type: " + documentUrl);
        }

        try {
            byte[] sourceDocument = fileStorageService.downloadFile(documentUrl);
            byte[] processedDocument;

            switch (documentType) {
                case "PDF":
                    processedDocument = processPdfDocument(sourceDocument, variables);
                    break;
                case "DOCX":
                    processedDocument = processDocxDocument(sourceDocument, variables);
                    break;
                case "DOC":
                    throw new BusinessException("Legacy DOC format not supported. Please use DOCX.");
                default:
                    throw new BusinessException("Unsupported document type: " + documentType);
            }

            // Save processed document
            String processedFilename = generateProcessedFilename(documentUrl, caseId);
            String processedUrl = saveProcessedDocument(processedDocument, processedFilename, documentType);

            log.info("Document processed successfully: {}", processedUrl);
            return processedUrl;

        } catch (Exception e) {
            log.error("Error processing document: {}", e.getMessage(), e);
            throw new BusinessException("Failed to process document: " + e.getMessage());
        }
    }

    @Override
    public List<String> extractPlaceholders(String documentUrl) {
        log.info("Extracting placeholders from document: {}", documentUrl);

        if (documentUrl == null || documentUrl.isEmpty()) {
            return Collections.emptyList();
        }

        String documentType = getDocumentType(documentUrl);
        if (documentType == null) {
            return Collections.emptyList();
        }

        try {
            byte[] document = fileStorageService.downloadFile(documentUrl);
            String textContent;

            switch (documentType) {
                case "PDF":
                    textContent = extractTextFromPdf(document);
                    break;
                case "DOCX":
                    textContent = extractTextFromDocx(document);
                    break;
                default:
                    return Collections.emptyList();
            }

            return extractPlaceholdersFromText(textContent);

        } catch (Exception e) {
            log.error("Error extracting placeholders: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public boolean hasPlaceholders(String documentUrl) {
        List<String> placeholders = extractPlaceholders(documentUrl);
        return placeholders != null && !placeholders.isEmpty();
    }

    /**
     * Process PDF document and replace placeholders
     * Note: PDF text replacement is complex due to PDF structure.
     * This implementation creates a new PDF with replaced text for simple cases.
     * For complex PDFs, consider using a commercial library or form-based approach.
     */
    private byte[] processPdfDocument(byte[] sourceDocument, Map<String, Object> variables) throws IOException {
        try (PDDocument document = Loader.loadPDF(sourceDocument)) {

            // Extract text and check for placeholders
            PDFTextStripper stripper = new PDFTextStripper();
            String originalText = stripper.getText(document);

            // Replace placeholders in text
            String processedText = replacePlaceholders(originalText, variables);

            // If text changed, we need to recreate the PDF
            // Note: This is a simplified approach - it creates a new text-only PDF
            // For maintaining original formatting, consider using PDFBox's content stream editing
            // or a commercial solution like iText

            if (!originalText.equals(processedText)) {
                log.debug("Placeholders found and replaced in PDF");

                // For complex PDFs, we'll use a workaround:
                // Create overlay or append replaced content
                // This is a simplified implementation
                return createPdfWithText(processedText, document.getNumberOfPages());
            }

            // No changes needed, return original
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    /**
     * Process DOCX document and replace placeholders (text and images)
     * This is more straightforward than PDF as DOCX is XML-based
     */
    private byte[] processDocxDocument(byte[] sourceDocument, Map<String, Object> variables) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(sourceDocument);
             XWPFDocument document = new XWPFDocument(bais)) {

            // Process paragraphs
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                processDocxParagraph(paragraph, variables, document);
            }

            // Process tables
            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph paragraph : cell.getParagraphs()) {
                            processDocxParagraph(paragraph, variables, document);
                        }
                    }
                }
            }

            // Process headers
            for (XWPFHeader header : document.getHeaderList()) {
                for (XWPFParagraph paragraph : header.getParagraphs()) {
                    processDocxParagraph(paragraph, variables, document);
                }
            }

            // Process footers
            for (XWPFFooter footer : document.getFooterList()) {
                for (XWPFParagraph paragraph : footer.getParagraphs()) {
                    processDocxParagraph(paragraph, variables, document);
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.write(baos);
            return baos.toByteArray();
        }
    }

    /**
     * Process a single DOCX paragraph and replace placeholders (text and images)
     */
    private void processDocxParagraph(XWPFParagraph paragraph, Map<String, Object> variables, XWPFDocument document) {
        List<XWPFRun> runs = paragraph.getRuns();
        if (runs == null || runs.isEmpty()) {
            return;
        }

        // Combine all runs to get full text (placeholders might span multiple runs)
        StringBuilder fullText = new StringBuilder();
        for (XWPFRun run : runs) {
            String text = run.getText(0);
            if (text != null) {
                fullText.append(text);
            }
        }

        String originalText = fullText.toString();

        // Check for image placeholders first
        Matcher imageMatcher = IMAGE_PLACEHOLDER_PATTERN.matcher(originalText);
        boolean hasImagePlaceholder = imageMatcher.find();

        // Also check for known image placeholder keys with regular syntax
        boolean hasKnownImagePlaceholder = false;
        String imageKey = null;
        for (String key : IMAGE_PLACEHOLDER_KEYS) {
            if (originalText.contains("{" + key + "}") || originalText.contains("{{" + key + "}}")) {
                hasKnownImagePlaceholder = true;
                imageKey = key;
                break;
            }
        }

        // Handle image replacement
        if (hasImagePlaceholder || hasKnownImagePlaceholder) {
            String placeholderKey = hasImagePlaceholder ? imageMatcher.group(1) : imageKey;
            Object imageValue = variables.get(placeholderKey);

            if (imageValue != null) {
                String imagePath = imageValue.toString();
                try {
                    // Clear existing runs
                    for (int i = runs.size() - 1; i >= 0; i--) {
                        paragraph.removeRun(i);
                    }

                    // Add new run with image
                    XWPFRun newRun = paragraph.createRun();
                    insertImage(newRun, imagePath, placeholderKey);
                    log.debug("Replaced image placeholder: {} with image: {}", placeholderKey, imagePath);
                    return;
                } catch (Exception e) {
                    log.warn("Failed to insert image for placeholder {}: {}", placeholderKey, e.getMessage());
                    // Fall back to text replacement
                }
            }
        }

        // Regular text placeholder replacement
        String replacedText = replacePlaceholders(originalText, variables);

        // If text changed, update the runs
        if (!originalText.equals(replacedText)) {
            // Clear all runs and set the replaced text in the first run
            for (int i = runs.size() - 1; i > 0; i--) {
                paragraph.removeRun(i);
            }

            if (!runs.isEmpty()) {
                runs.get(0).setText(replacedText, 0);
            }
        }
    }

    /**
     * Insert an image into a DOCX run
     * @param run the run to insert the image into
     * @param imagePath the path/URL of the image
     * @param placeholderKey the placeholder key (used to determine image size)
     */
    private void insertImage(XWPFRun run, String imagePath, String placeholderKey) throws Exception {
        byte[] imageBytes = loadImageBytes(imagePath);
        if (imageBytes == null || imageBytes.length == 0) {
            throw new BusinessException("Could not load image: " + imagePath);
        }

        // Determine image type
        int pictureType = getPictureType(imagePath);

        // Determine image dimensions based on placeholder type
        ImageDimensions dimensions = getImageDimensions(placeholderKey);

        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes)) {
            run.addPicture(bis, pictureType, imagePath,
                    Units.toEMU(dimensions.width), Units.toEMU(dimensions.height));
        }
    }

    /**
     * Load image bytes from path (local file or URL)
     */
    private byte[] loadImageBytes(String imagePath) {
        try {
            // Check if it's a URL
            if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
                URL url = new URL(imagePath);
                try (InputStream is = url.openStream();
                     ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesRead);
                    }
                    return baos.toByteArray();
                }
            }

            // Check if it's a relative path in our storage
            Path filePath = Path.of(basePath).resolve(imagePath).normalize();
            if (Files.exists(filePath)) {
                return Files.readAllBytes(filePath);
            }

            // Try as absolute path
            Path absolutePath = Path.of(imagePath);
            if (Files.exists(absolutePath)) {
                return Files.readAllBytes(absolutePath);
            }

            log.warn("Image not found at path: {}", imagePath);
            return null;

        } catch (Exception e) {
            log.error("Error loading image from {}: {}", imagePath, e.getMessage());
            return null;
        }
    }

    /**
     * Get POI picture type from file extension
     */
    private int getPictureType(String imagePath) {
        String lower = imagePath.toLowerCase();
        if (lower.endsWith(".png")) {
            return XWPFDocument.PICTURE_TYPE_PNG;
        } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return XWPFDocument.PICTURE_TYPE_JPEG;
        } else if (lower.endsWith(".gif")) {
            return XWPFDocument.PICTURE_TYPE_GIF;
        } else if (lower.endsWith(".bmp")) {
            return XWPFDocument.PICTURE_TYPE_BMP;
        }
        // Default to PNG
        return XWPFDocument.PICTURE_TYPE_PNG;
    }

    /**
     * Get image dimensions based on placeholder type
     */
    private ImageDimensions getImageDimensions(String placeholderKey) {
        // Define default dimensions for different image types (in pixels)
        String lowerKey = placeholderKey.toLowerCase();

        if (lowerKey.contains("logo")) {
            return new ImageDimensions(150, 50); // Company logo: wider
        } else if (lowerKey.contains("signature")) {
            return new ImageDimensions(120, 40); // Signature: medium width
        } else if (lowerKey.contains("stamp") || lowerKey.contains("seal")) {
            return new ImageDimensions(80, 80); // Stamp: square
        } else if (lowerKey.contains("qr") || lowerKey.contains("barcode")) {
            return new ImageDimensions(100, 100); // QR code: square
        }

        // Default dimensions
        return new ImageDimensions(100, 100);
    }

    /**
     * Helper class for image dimensions
     */
    private static class ImageDimensions {
        final int width;
        final int height;

        ImageDimensions(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

    /**
     * Replace placeholders in text with variable values
     */
    private String replacePlaceholders(String text, Map<String, Object> variables) {
        if (text == null || variables == null || variables.isEmpty()) {
            return text;
        }

        String result = text;

        // Replace both {var} and {{var}} patterns
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue() != null ? entry.getValue().toString() : "";

            // Replace {{key}}
            result = result.replace("{{" + key + "}}", value);
            // Replace {key}
            result = result.replace("{" + key + "}", value);
        }

        return result;
    }

    /**
     * Extract text from PDF for placeholder detection
     */
    private String extractTextFromPdf(byte[] document) throws IOException {
        try (PDDocument pdf = Loader.loadPDF(document)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(pdf);
        }
    }

    /**
     * Extract text from DOCX for placeholder detection
     */
    private String extractTextFromDocx(byte[] document) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(document);
             XWPFDocument doc = new XWPFDocument(bais)) {

            StringBuilder text = new StringBuilder();

            for (XWPFParagraph paragraph : doc.getParagraphs()) {
                text.append(paragraph.getText()).append("\n");
            }

            for (XWPFTable table : doc.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        text.append(cell.getText()).append("\t");
                    }
                    text.append("\n");
                }
            }

            return text.toString();
        }
    }

    /**
     * Extract placeholder names from text
     */
    private List<String> extractPlaceholdersFromText(String text) {
        Set<String> placeholders = new LinkedHashSet<>(); // Preserve order, avoid duplicates

        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        while (matcher.find()) {
            placeholders.add(matcher.group(1));
        }

        return new ArrayList<>(placeholders);
    }

    /**
     * Generate filename for processed document
     */
    private String generateProcessedFilename(String originalUrl, Long caseId) {
        String extension = getExtension(originalUrl);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return String.format("processed/case_%d_%s.%s", caseId, timestamp, extension);
    }

    /**
     * Save processed document to storage
     */
    private String saveProcessedDocument(byte[] document, String filename, String documentType) throws IOException {
        // Create processed folder path
        Path processedFolder = localFileStorage.getFilePath("processed");
        Files.createDirectories(processedFolder);

        // Save file
        Path filePath = localFileStorage.getFilePath(filename);
        Files.write(filePath, document);

        log.info("Processed document saved: {}", filename);
        return filename;
    }

    /**
     * Create a simple PDF with text content
     * This is a fallback for when we can't edit PDF in-place
     */
    private byte[] createPdfWithText(String text, int pageCount) throws IOException {
        try (PDDocument document = new PDDocument()) {
            String[] lines = text.split("\n");
            int linesPerPage = 50;
            int lineIndex = 0;

            while (lineIndex < lines.length) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    contentStream.beginText();
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                    contentStream.setLeading(14.5f);
                    contentStream.newLineAtOffset(50, 750);

                    int linesOnPage = 0;
                    while (lineIndex < lines.length && linesOnPage < linesPerPage) {
                        String line = lines[lineIndex];
                        // Escape special characters and truncate long lines
                        if (line.length() > 100) {
                            line = line.substring(0, 100) + "...";
                        }
                        contentStream.showText(line);
                        contentStream.newLine();
                        lineIndex++;
                        linesOnPage++;
                    }

                    contentStream.endText();
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    /**
     * Get file extension from URL
     */
    private String getExtension(String url) {
        if (url == null || !url.contains(".")) {
            return "pdf";
        }
        return url.substring(url.lastIndexOf('.') + 1).toLowerCase();
    }
}
