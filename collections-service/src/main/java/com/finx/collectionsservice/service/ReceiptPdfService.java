package com.finx.collectionsservice.service;

import com.finx.collectionsservice.domain.entity.Repayment;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class ReceiptPdfService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");
    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(41, 128, 185);
    private static final DeviceRgb LIGHT_GRAY = new DeviceRgb(245, 245, 245);

    // Status Colors
    private static final DeviceRgb APPROVED_COLOR = new DeviceRgb(22, 163, 74);          // Green
    private static final DeviceRgb PENDING_COLOR = new DeviceRgb(245, 158, 11);          // Orange
    private static final DeviceRgb REJECTED_COLOR = new DeviceRgb(220, 38, 38);          // Red
    private static final DeviceRgb CORRECTION_REQUIRED_COLOR = new DeviceRgb(107, 114, 128);    // Gray

    // Background Colors (lighter shades)
    private static final DeviceRgb APPROVED_BG = new DeviceRgb(220, 252, 231);           // Light Green
    private static final DeviceRgb PENDING_BG = new DeviceRgb(254, 243, 199);            // Light Orange
    private static final DeviceRgb REJECTED_BG = new DeviceRgb(254, 226, 226);           // Light Red
    private static final DeviceRgb CORRECTION_REQUIRED_BG = new DeviceRgb(243, 244, 246);       // Light Gray

    public byte[] generateReceiptPdf(Repayment repayment) {
        log.info("Generating PDF receipt for repayment: {}", repayment.getId());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(40, 40, 40, 40);

            // Header
            addHeader(document, repayment);

            // Receipt Details
            addReceiptDetails(document, repayment);

            // Payment Information
            addPaymentInformation(document, repayment);

            // Approval Information (if approved)
            if (repayment.getApprovalStatus() != null) {
                addApprovalInformation(document, repayment);
            }

            // Footer
            addFooter(document);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating PDF receipt", e);
            throw new RuntimeException("Failed to generate PDF receipt", e);
        }
    }

    private void addHeader(Document document, Repayment repayment) {
        // Company Header
        Paragraph company = new Paragraph("FINX CREDIT ENFORCEMENT")
                .setFontSize(24)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        document.add(company);

        Paragraph subtitle = new Paragraph("Payment Receipt")
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        document.add(subtitle);

        // Get status color
        DeviceRgb bannerColor = getStatusColor(repayment.getApprovalStatus());

        // Receipt Number Banner with status color
        Table bannerTable = new Table(1);
        bannerTable.setWidth(UnitValue.createPercentValue(100));

        Cell bannerCell = new Cell()
                .add(new Paragraph("Receipt No: " + repayment.getRepaymentNumber())
                        .setFontSize(16)
                        .setBold()
                        .setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(bannerColor)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(10)
                .setBorder(Border.NO_BORDER);

        bannerTable.addCell(bannerCell);
        document.add(bannerTable);
        document.add(new Paragraph(" ").setMarginBottom(5));
    }

    private void addReceiptDetails(Document document, Repayment repayment) {
        Paragraph sectionTitle = new Paragraph("Receipt Details")
                .setFontSize(11)
                .setBold()
                .setMarginBottom(5);
        document.add(sectionTitle);

        Table table = new Table(new float[]{1, 2});
        table.setWidth(UnitValue.createPercentValue(100));

        addDetailRow(table, "Receipt Number", repayment.getRepaymentNumber());
        addDetailRow(table, "Case ID", String.valueOf(repayment.getCaseId()));

        if (repayment.getLoanAccountNumber() != null) {
            addDetailRow(table, "Loan Account", repayment.getLoanAccountNumber());
        }

        addDetailRow(table, "Receipt Date",
            repayment.getCreatedAt().format(DATE_FORMATTER));

        document.add(table);
        document.add(new Paragraph(" ").setMarginBottom(5));
    }

    private void addPaymentInformation(Document document, Repayment repayment) {
        Paragraph sectionTitle = new Paragraph("Payment Information")
                .setFontSize(11)
                .setBold()
                .setMarginBottom(5);
        document.add(sectionTitle);

        Table table = new Table(new float[]{1, 2});
        table.setWidth(UnitValue.createPercentValue(100));

        // Amount - Highlighted
        Cell labelCell = new Cell()
                .add(new Paragraph("Payment Amount").setBold())
                .setBackgroundColor(LIGHT_GRAY)
                .setPadding(6)
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));

        // Convert from paisa to rupees (divide by 100)
        BigDecimal amountInRupees = repayment.getPaymentAmount().divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        Cell valueCell = new Cell()
                .add(new Paragraph("₹ " + amountInRupees)
                        .setFontSize(14)
                        .setBold()
                        .setFontColor(PRIMARY_COLOR))
                .setBackgroundColor(LIGHT_GRAY)
                .setPadding(6)
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));

        table.addCell(labelCell);
        table.addCell(valueCell);

        addDetailRow(table, "Payment Date",
            repayment.getPaymentDate().format(DATE_FORMATTER));
        addDetailRow(table, "Payment Mode",
            repayment.getPaymentMode() != null ? repayment.getPaymentMode().name() : "N/A");

        if (repayment.getTransactionId() != null) {
            addDetailRow(table, "Transaction ID", String.valueOf(repayment.getTransactionId()));
        }

        if (repayment.getCollectedBy() != null) {
            addDetailRow(table, "Collected By (User ID)", String.valueOf(repayment.getCollectedBy()));
        }

        if (repayment.getCollectionLocation() != null) {
            addDetailRow(table, "Collection Location", repayment.getCollectionLocation());
        }

        if (repayment.getNotes() != null && !repayment.getNotes().isEmpty()) {
            addDetailRow(table, "Notes", repayment.getNotes());
        }

        document.add(table);
        document.add(new Paragraph(" ").setMarginBottom(5));
    }

    private void addApprovalInformation(Document document, Repayment repayment) {
        Paragraph sectionTitle = new Paragraph("Approval Information")
                .setFontSize(11)
                .setBold()
                .setMarginBottom(5);
        document.add(sectionTitle);

        Table table = new Table(new float[]{1, 2});
        table.setWidth(UnitValue.createPercentValue(100));

        String status = repayment.getApprovalStatus().name();
        DeviceRgb statusColor = getStatusColor(repayment.getApprovalStatus());
        DeviceRgb statusBgColor = getStatusBackgroundColor(repayment.getApprovalStatus());

        Cell labelCell = new Cell()
                .add(new Paragraph("Status").setBold())
                .setPadding(6)
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));

        Cell valueCell = new Cell()
                .add(new Paragraph(status)
                        .setBold()
                        .setFontSize(12)
                        .setFontColor(statusColor))
                .setBackgroundColor(statusBgColor)
                .setPadding(6)
                .setBorder(new SolidBorder(statusColor, 1.5f));

        table.addCell(labelCell);
        table.addCell(valueCell);

        if (repayment.getApprovedBy() != null) {
            addDetailRow(table, "Approved By (User ID)", String.valueOf(repayment.getApprovedBy()));
        }

        if (repayment.getApprovedAt() != null) {
            addDetailRow(table, "Approved At",
                repayment.getApprovedAt().format(DATETIME_FORMATTER));
        }

        if (repayment.getRejectionReason() != null) {
            addDetailRow(table, "Rejection Reason", repayment.getRejectionReason());
        }

        if (repayment.getCorrectionNotes() != null) {
            addDetailRow(table, "Correction Notes", repayment.getCorrectionNotes());
        }

        document.add(table);
        document.add(new Paragraph(" ").setMarginBottom(5));
    }

    private void addFooter(Document document) {
        // System generated note
        Paragraph footer = new Paragraph("This is a computer-generated receipt and does not require a signature.")
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY)
                .setMarginTop(15);
        document.add(footer);
    }

    private void addDetailRow(Table table, String label, String value) {
        Cell labelCell = new Cell()
                .add(new Paragraph(label).setBold().setFontSize(10))
                .setPadding(5)
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));

        Cell valueCell = new Cell()
                .add(new Paragraph(value != null ? value : "N/A").setFontSize(10))
                .setPadding(5)
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private DeviceRgb getStatusColor(com.finx.collectionsservice.domain.enums.RepaymentStatus status) {
        return switch (status) {
            case APPROVED -> APPROVED_COLOR;
            case PENDING -> PENDING_COLOR;
            case REJECTED -> REJECTED_COLOR;
            case CORRECTION_REQUIRED -> CORRECTION_REQUIRED_COLOR;
        };
    }

    private DeviceRgb getStatusBackgroundColor(com.finx.collectionsservice.domain.enums.RepaymentStatus status) {
        return switch (status) {
            case APPROVED -> APPROVED_BG;
            case PENDING -> PENDING_BG;
            case REJECTED -> REJECTED_BG;
            case CORRECTION_REQUIRED -> CORRECTION_REQUIRED_BG;
        };
    }
}
