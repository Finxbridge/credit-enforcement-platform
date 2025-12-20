package com.finx.templatemanagementservice.service.impl;

import com.finx.templatemanagementservice.exception.BusinessException;
import com.finx.templatemanagementservice.service.DocumentProcessingService;
import com.finx.templatemanagementservice.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;
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
 * while preserving exact document layout, formatting, and structure
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentProcessingServiceImpl implements DocumentProcessingService {

    private final FileStorageService fileStorageService;
    private final LocalFileStorageServiceImpl localFileStorage;
    private final com.finx.templatemanagementservice.client.DmsServiceClient dmsServiceClient;

    @Value("${file.storage.base-path:./uploads}")
    private String basePath;

    // Placeholder pattern: {{variable_name}} or {variable_name}
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{?([a-zA-Z_][a-zA-Z0-9_]*)\\}?\\}");

    // Image placeholder pattern for Phase 2: {{IMG:variable_name}}
    private static final Pattern IMAGE_PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{?IMG:([a-zA-Z_][a-zA-Z0-9_]*)\\}?\\}");

    @Override
    public String processDocument(String documentUrl, Map<String, Object> variables, Long caseId) {
        // Delegate to overloaded method without template metadata (for backward compatibility)
        return processDocument(documentUrl, variables, caseId, null, null);
    }

    @Override
    public String processDocument(String documentUrl, Map<String, Object> variables, Long caseId,
                                  Long sourceTemplateId, String channel) {
        log.info("Processing document: {} for case: {} with {} variables (template: {}, channel: {})",
                documentUrl, caseId, variables != null ? variables.size() : 0, sourceTemplateId, channel);

        if (documentUrl == null || documentUrl.isEmpty()) {
            throw new BusinessException("Document URL is required");
        }

        String documentType = getDocumentType(documentUrl);
        if (documentType == null) {
            throw new BusinessException("Unsupported document type: " + documentUrl);
        }

        try {
            // Download source document from DMS/S3
            byte[] sourceDocument = fileStorageService.downloadFile(documentUrl);
            log.info("Downloaded source document: {} bytes", sourceDocument.length);

            byte[] processedDocument;

            switch (documentType) {
                case "PDF":
                    processedDocument = processPdfDocumentPreservingLayout(sourceDocument, variables);
                    break;
                case "DOCX":
                    processedDocument = processDocxDocumentPreservingLayout(sourceDocument, variables);
                    break;
                case "DOC":
                    throw new BusinessException("Legacy DOC format not supported. Please convert to DOCX or PDF.");
                default:
                    throw new BusinessException("Unsupported document type: " + documentType);
            }

            // Upload processed document to DMS with GENERATED category
            String processedFilename = generateProcessedFilename(documentUrl, caseId);
            String processedUrl = uploadProcessedDocumentToDms(processedDocument, processedFilename, documentType,
                    caseId, sourceTemplateId, channel);

            log.info("Document processed and uploaded to DMS successfully: {}", processedUrl);
            return processedUrl;

        } catch (Exception e) {
            log.error("Error processing document: {}", e.getMessage(), e);
            throw new BusinessException("Failed to process document: " + e.getMessage());
        }
    }

    /**
     * Process PDF document while preserving exact layout, fonts, images, and formatting.
     * Uses PDFBox content stream editing to replace text in-place.
     */
    private byte[] processPdfDocumentPreservingLayout(byte[] sourceDocument, Map<String, Object> variables) throws IOException {
        log.info("Processing PDF document while preserving layout");

        try (PDDocument document = Loader.loadPDF(sourceDocument)) {
            boolean documentModified = false;

            // Process each page
            for (PDPage page : document.getPages()) {
                boolean pageModified = processPageContentStream(document, page, variables);
                if (pageModified) {
                    documentModified = true;
                }
            }

            if (documentModified) {
                log.info("PDF document modified with placeholder replacements");
            } else {
                log.info("No placeholders found or replaced in PDF");
            }

            // Save the modified document
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    /**
     * Process a single page's content stream to replace placeholders
     * This preserves the exact position, font, size, and styling of text
     */
    private boolean processPageContentStream(PDDocument document, PDPage page, Map<String, Object> variables) throws IOException {
        // Check if page has content
        if (page.getContents() == null) {
            return false;
        }

        PDFStreamParser parser = new PDFStreamParser(page);
        List<Object> tokens = parser.parse();
        boolean modified = false;

        // Process each token in the content stream
        for (int i = 0; i < tokens.size(); i++) {
            Object token = tokens.get(i);

            // Check if this is a text showing operator (Tj or TJ)
            if (token instanceof Operator) {
                Operator op = (Operator) token;
                String opName = op.getName();

                if ("Tj".equals(opName) && i > 0) {
                    // Simple text string - operand is the string before Tj
                    Object operand = tokens.get(i - 1);
                    if (operand instanceof String) {
                        String text = (String) operand;
                        String replaced = replacePlaceholders(text, variables);
                        if (!text.equals(replaced)) {
                            tokens.set(i - 1, replaced);
                            modified = true;
                            log.debug("Replaced placeholder in PDF: '{}' -> '{}'", text, replaced);
                        }
                    }
                } else if ("TJ".equals(opName) && i > 0) {
                    // Array of text strings and positioning - more complex
                    Object operand = tokens.get(i - 1);
                    if (operand instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Object> array = (List<Object>) operand;
                        modified |= processTextArray(array, variables);
                    }
                }
            }
        }

        if (modified) {
            // Write the modified content stream back to the page
            // Use the document reference passed in
            PDStream newStream = new PDStream(document);
            OutputStream out = newStream.createOutputStream(COSName.FLATE_DECODE);
            ContentStreamWriter writer = new ContentStreamWriter(out);
            writer.writeTokens(tokens);
            out.close();
            page.setContents(newStream);
        }

        return modified;
    }

    /**
     * Process a TJ text array, replacing placeholders while preserving positioning
     */
    private boolean processTextArray(List<Object> array, Map<String, Object> variables) {
        boolean modified = false;

        // First, concatenate all strings to find complete placeholders
        StringBuilder fullText = new StringBuilder();
        List<Integer> stringIndices = new ArrayList<>();

        for (int i = 0; i < array.size(); i++) {
            Object item = array.get(i);
            if (item instanceof String) {
                stringIndices.add(i);
                fullText.append((String) item);
            }
        }

        String originalText = fullText.toString();
        String replacedText = replacePlaceholders(originalText, variables);

        if (!originalText.equals(replacedText)) {
            // Distribute replaced text back across the string elements
            // Try to maintain original structure as much as possible
            if (stringIndices.size() == 1) {
                // Simple case: single string element
                array.set(stringIndices.get(0), replacedText);
                modified = true;
            } else {
                // Complex case: multiple string elements
                // Replace placeholders in the combined text and redistribute
                int charIndex = 0;
                for (int idx : stringIndices) {
                    String originalPart = (String) array.get(idx);
                    int partLength = originalPart.length();

                    if (charIndex + partLength <= replacedText.length()) {
                        String newPart = replacedText.substring(charIndex,
                                Math.min(charIndex + partLength, replacedText.length()));
                        if (!originalPart.equals(newPart)) {
                            array.set(idx, newPart);
                            modified = true;
                        }
                    }
                    charIndex += partLength;
                }

                // If replaced text is longer, append remainder to last string
                if (charIndex < replacedText.length() && !stringIndices.isEmpty()) {
                    int lastIdx = stringIndices.get(stringIndices.size() - 1);
                    String current = (String) array.get(lastIdx);
                    array.set(lastIdx, current + replacedText.substring(charIndex));
                    modified = true;
                }
            }

            log.debug("Replaced placeholder in PDF TJ array: '{}' -> '{}'", originalText, replacedText);
        }

        return modified;
    }

    /**
     * Process DOCX document while preserving exact layout, formatting, styles, and structure.
     * Handles placeholders that may span multiple runs.
     */
    private byte[] processDocxDocumentPreservingLayout(byte[] sourceDocument, Map<String, Object> variables) throws IOException {
        log.info("Processing DOCX document while preserving layout");

        try (ByteArrayInputStream bais = new ByteArrayInputStream(sourceDocument);
             XWPFDocument document = new XWPFDocument(bais)) {

            int replacementCount = 0;

            // Process main document body paragraphs
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                replacementCount += processDocxParagraphPreservingFormat(paragraph, variables);
            }

            // Process tables
            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph paragraph : cell.getParagraphs()) {
                            replacementCount += processDocxParagraphPreservingFormat(paragraph, variables);
                        }
                    }
                }
            }

            // Process headers
            for (XWPFHeader header : document.getHeaderList()) {
                for (XWPFParagraph paragraph : header.getParagraphs()) {
                    replacementCount += processDocxParagraphPreservingFormat(paragraph, variables);
                }
                for (XWPFTable table : header.getTables()) {
                    for (XWPFTableRow row : table.getRows()) {
                        for (XWPFTableCell cell : row.getTableCells()) {
                            for (XWPFParagraph paragraph : cell.getParagraphs()) {
                                replacementCount += processDocxParagraphPreservingFormat(paragraph, variables);
                            }
                        }
                    }
                }
            }

            // Process footers
            for (XWPFFooter footer : document.getFooterList()) {
                for (XWPFParagraph paragraph : footer.getParagraphs()) {
                    replacementCount += processDocxParagraphPreservingFormat(paragraph, variables);
                }
                for (XWPFTable table : footer.getTables()) {
                    for (XWPFTableRow row : table.getRows()) {
                        for (XWPFTableCell cell : row.getTableCells()) {
                            for (XWPFParagraph paragraph : cell.getParagraphs()) {
                                replacementCount += processDocxParagraphPreservingFormat(paragraph, variables);
                            }
                        }
                    }
                }
            }

            log.info("DOCX processing complete: {} placeholder replacements made", replacementCount);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.write(baos);
            return baos.toByteArray();
        }
    }

    /**
     * Process a DOCX paragraph, replacing placeholders while preserving exact formatting.
     * Handles the case where a placeholder like {{customer_name}} might be split across multiple runs.
     *
     * @return number of replacements made
     */
    private int processDocxParagraphPreservingFormat(XWPFParagraph paragraph, Map<String, Object> variables) {
        List<XWPFRun> runs = paragraph.getRuns();
        if (runs == null || runs.isEmpty()) {
            return 0;
        }

        // Step 1: Build complete text from all runs and track character positions
        StringBuilder fullText = new StringBuilder();
        List<RunCharacterMapping> characterMappings = new ArrayList<>();

        for (int runIndex = 0; runIndex < runs.size(); runIndex++) {
            XWPFRun run = runs.get(runIndex);
            String runText = run.getText(0);
            if (runText != null) {
                for (int charIndex = 0; charIndex < runText.length(); charIndex++) {
                    characterMappings.add(new RunCharacterMapping(runIndex, charIndex, runText.charAt(charIndex)));
                }
                fullText.append(runText);
            }
        }

        String originalText = fullText.toString();
        if (originalText.isEmpty()) {
            return 0;
        }

        // Step 2: Find all placeholders and their replacements
        int replacementCount = 0;
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(originalText);
        List<PlaceholderReplacement> replacements = new ArrayList<>();

        while (matcher.find()) {
            String placeholder = matcher.group(0); // Full match including braces
            String variableName = matcher.group(1); // Just the variable name
            Object value = variables.get(variableName);

            if (value != null) {
                String replacement = value.toString();
                replacements.add(new PlaceholderReplacement(
                        matcher.start(), matcher.end(), placeholder, replacement));
                replacementCount++;
                log.debug("Found placeholder '{}' at position {}-{}, replacing with '{}'",
                        placeholder, matcher.start(), matcher.end(), replacement);
            }
        }

        if (replacements.isEmpty()) {
            return 0;
        }

        // Step 3: Apply replacements in reverse order to maintain position accuracy
        Collections.reverse(replacements);

        for (PlaceholderReplacement replacement : replacements) {
            applyReplacementToRuns(runs, characterMappings, replacement);
        }

        return replacementCount;
    }

    /**
     * Apply a single replacement to the runs, handling cross-run placeholders
     */
    private void applyReplacementToRuns(List<XWPFRun> runs, List<RunCharacterMapping> mappings,
                                         PlaceholderReplacement replacement) {

        int startPos = replacement.start;
        int endPos = replacement.end;

        if (startPos >= mappings.size() || endPos > mappings.size()) {
            return;
        }

        // Find which runs are affected
        RunCharacterMapping startMapping = mappings.get(startPos);
        RunCharacterMapping endMapping = mappings.get(endPos - 1);

        int startRunIndex = startMapping.runIndex;
        int endRunIndex = endMapping.runIndex;

        if (startRunIndex == endRunIndex) {
            // Simple case: placeholder is within a single run
            XWPFRun run = runs.get(startRunIndex);
            String currentText = run.getText(0);
            if (currentText != null) {
                String newText = currentText.substring(0, startMapping.charIndex)
                        + replacement.replacement
                        + currentText.substring(endMapping.charIndex + 1);
                run.setText(newText, 0);
            }
        } else {
            // Complex case: placeholder spans multiple runs
            // Strategy: Put replacement in first run, clear the placeholder parts from subsequent runs

            // First run: keep text before placeholder, add replacement
            XWPFRun firstRun = runs.get(startRunIndex);
            String firstText = firstRun.getText(0);
            if (firstText != null) {
                String newFirstText = firstText.substring(0, startMapping.charIndex) + replacement.replacement;
                firstRun.setText(newFirstText, 0);
            }

            // Last run: keep text after placeholder
            XWPFRun lastRun = runs.get(endRunIndex);
            String lastText = lastRun.getText(0);
            if (lastText != null && endMapping.charIndex + 1 < lastText.length()) {
                lastRun.setText(lastText.substring(endMapping.charIndex + 1), 0);
            } else {
                lastRun.setText("", 0);
            }

            // Middle runs: clear completely (they only contained placeholder text)
            for (int i = startRunIndex + 1; i < endRunIndex; i++) {
                runs.get(i).setText("", 0);
            }
        }
    }

    /**
     * Helper class to map characters to their run positions
     */
    private static class RunCharacterMapping {
        final int runIndex;
        final int charIndex;
        final char character;

        RunCharacterMapping(int runIndex, int charIndex, char character) {
            this.runIndex = runIndex;
            this.charIndex = charIndex;
            this.character = character;
        }
    }

    /**
     * Helper class to store placeholder replacement info
     */
    private static class PlaceholderReplacement {
        final int start;
        final int end;
        final String placeholder;
        final String replacement;

        PlaceholderReplacement(int start, int end, String placeholder, String replacement) {
            this.start = start;
            this.end = end;
            this.placeholder = placeholder;
            this.replacement = replacement;
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

        // Replace both {{var}} and {var} patterns
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
     * Upload processed document to DMS (OVH S3) and return the URL
     * This method maintains backward compatibility
     */
    private String uploadProcessedDocumentToDms(byte[] document, String filename, String documentType, Long caseId) {
        return uploadProcessedDocumentToDms(document, filename, documentType, caseId, null, null);
    }

    /**
     * Upload processed document to DMS (OVH S3) with GENERATED category and return the URL
     */
    private String uploadProcessedDocumentToDms(byte[] document, String filename, String documentType,
                                                Long caseId, Long sourceTemplateId, String channel) {
        try {
            // Create MultipartFile from byte array for DMS upload
            org.springframework.web.multipart.MultipartFile multipartFile =
                new ByteArrayMultipartFile(document, filename, getContentType(documentType));

            // Generate document name: PROCESSED_{caseId}_{timestamp}_{filename}
            String documentName = String.format("PROCESSED_CASE_%d_%s_%s",
                    caseId,
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")),
                    filename);

            // Upload to DMS (OVH S3 storage) with GENERATED category
            var response = dmsServiceClient.uploadDocumentWithCategory(
                    multipartFile,
                    documentName,
                    "GENERATED",     // Category: generated document (placeholders replaced)
                    channel,         // Channel from template
                    caseId,          // Case ID
                    sourceTemplateId // Source template ID
            );

            if (response != null && response.getPayload() != null) {
                String dmsUrl = response.getPayload().getFileUrl();
                log.info("Processed document uploaded to DMS: {} -> {} (size: {} bytes, category: GENERATED, channel: {})",
                        filename, dmsUrl, document.length, channel);
                return dmsUrl;
            } else {
                throw new IOException("DMS upload returned empty response");
            }

        } catch (Exception e) {
            log.error("Failed to upload processed document to DMS: {}", e.getMessage());
            throw new BusinessException("Failed to upload processed document: " + e.getMessage());
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
        return String.format("case_%d_%s.%s", caseId, timestamp, extension);
    }

    /**
     * Get content type based on document type
     */
    private String getContentType(String documentType) {
        if (documentType == null) {
            return "application/octet-stream";
        }
        return switch (documentType.toUpperCase()) {
            case "PDF" -> "application/pdf";
            case "DOCX" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "DOC" -> "application/msword";
            default -> "application/octet-stream";
        };
    }

    /**
     * Get file extension from URL
     */
    private String getExtension(String url) {
        if (url == null || !url.contains(".")) {
            return "pdf";
        }
        String filename = url;
        // Handle URLs with query parameters
        if (filename.contains("?")) {
            filename = filename.substring(0, filename.indexOf("?"));
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    /**
     * Custom MultipartFile implementation for byte array content
     */
    private static class ByteArrayMultipartFile implements org.springframework.web.multipart.MultipartFile {
        private final byte[] content;
        private final String filename;
        private final String contentType;

        public ByteArrayMultipartFile(byte[] content, String filename, String contentType) {
            this.content = content;
            this.filename = filename;
            this.contentType = contentType;
        }

        @Override
        public String getName() {
            return "file";
        }

        @Override
        public String getOriginalFilename() {
            return filename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content == null || content.length == 0;
        }

        @Override
        public long getSize() {
            return content != null ? content.length : 0;
        }

        @Override
        public byte[] getBytes() throws IOException {
            return content;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
            Files.write(dest.toPath(), content);
        }
    }
}
