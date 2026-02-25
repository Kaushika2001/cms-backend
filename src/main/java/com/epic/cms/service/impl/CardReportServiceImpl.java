package com.epic.cms.service.impl;

import com.epic.cms.dto.CardReportDTO;
import com.epic.cms.repository.ICardRepository;
import com.epic.cms.service.CardReportService;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardReportServiceImpl implements CardReportService {

    private final ICardRepository cardRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DeviceRgb HEADER_COLOR = new DeviceRgb(52, 73, 94); // Dark blue-gray
    private static final DeviceRgb ALTERNATE_ROW_COLOR = new DeviceRgb(236, 240, 241); // Light gray

    @Override
    public ByteArrayOutputStream generatePdfReport() {
        log.info("Generating PDF card report with audit information");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            // Fetch card data
            List<CardReportDTO> cards = cardRepository.findAllForReport();
            log.info("Retrieved {} cards for PDF report", cards.size());
            
            // Initialize PDF document
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            
            // Add title
            Paragraph title = new Paragraph("Card Report with Audit Information")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5);
            document.add(title);
            
            // Add generation timestamp
            Paragraph timestamp = new Paragraph("Generated on: " + LocalDateTime.now().format(DATETIME_FORMATTER))
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(15);
            document.add(timestamp);
            
            // Add summary
            Paragraph summary = new Paragraph("Total Cards: " + cards.size())
                    .setFontSize(10)
                    .setBold()
                    .setMarginBottom(10);
            document.add(summary);
            
            // Create table with 9 columns
            float[] columnWidths = {2.5f, 1.8f, 1.5f, 2.2f, 1.5f, 1.8f, 1.5f, 2.5f, 1.8f};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));
            table.setFontSize(8);
            
            // Add table headers
            addHeaderCell(table, "Masked Card Number");
            addHeaderCell(table, "Expiry Date");
            addHeaderCell(table, "Credit Limit");
            addHeaderCell(table, "Card Status");
            addHeaderCell(table, "Cash Limit");
            addHeaderCell(table, "Available Credit");
            addHeaderCell(table, "Available Cash");
            addHeaderCell(table, "Last Update Time\n(Audit)");
            addHeaderCell(table, "Last Update User\n(Audit)");
            
            // Add data rows with alternating colors
            int rowIndex = 0;
            for (CardReportDTO card : cards) {
                boolean isAlternateRow = rowIndex % 2 == 1;
                
                addDataCell(table, card.getMaskedCardNumber() != null ? card.getMaskedCardNumber() : "N/A", isAlternateRow);
                addDataCell(table, card.getExpiryDate() != null ? card.getExpiryDate().format(DATE_FORMATTER) : "N/A", isAlternateRow);
                addDataCell(table, card.getCreditLimit() != null ? String.format("%.2f", card.getCreditLimit()) : "0.00", isAlternateRow);
                addDataCell(table, card.getCardStatusDescription() != null ? card.getCardStatusDescription() : "N/A", isAlternateRow);
                addDataCell(table, card.getCashLimit() != null ? String.format("%.2f", card.getCashLimit()) : "0.00", isAlternateRow);
                addDataCell(table, card.getAvailableCreditLimit() != null ? String.format("%.2f", card.getAvailableCreditLimit()) : "0.00", isAlternateRow);
                addDataCell(table, card.getAvailableCashLimit() != null ? String.format("%.2f", card.getAvailableCashLimit()) : "0.00", isAlternateRow);
                addDataCell(table, card.getLastUpdateTime() != null ? card.getLastUpdateTime().format(DATETIME_FORMATTER) : "N/A", isAlternateRow);
                addDataCell(table, card.getLastUpdateUser() != null ? card.getLastUpdateUser() : "System", isAlternateRow);
                
                rowIndex++;
            }
            
            document.add(table);
            
            // Add footer
            Paragraph footer = new Paragraph("\nNote: Audit information includes the last update time and user who made the changes.")
                    .setFontSize(8)
                    .setItalic()
                    .setMarginTop(10);
            document.add(footer);
            
            document.close();
            
            log.info("PDF report generated successfully with {} cards", cards.size());
            
        } catch (Exception e) {
            log.error("Error generating PDF report", e);
            throw new RuntimeException("Failed to generate PDF report: " + e.getMessage(), e);
        }
        
        return baos;
    }

    @Override
    public ByteArrayOutputStream generateCsvReport() {
        log.info("Generating CSV card report with audit information");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            // Fetch card data
            List<CardReportDTO> cards = cardRepository.findAllForReport();
            log.info("Retrieved {} cards for CSV report", cards.size());
            
            // Initialize CSV writer
            OutputStreamWriter osw = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
            CSVWriter csvWriter = new CSVWriter(osw);
            
            // Write header with audit fields
            String[] header = {
                "Masked Card Number",
                "Expiry Date",
                "Credit Limit",
                "Card Status",
                "Cash Limit",
                "Available Credit Limit",
                "Available Cash Limit",
                "Last Update Time (Audit)",
                "Last Update User (Audit)"
            };
            csvWriter.writeNext(header);
            
            // Write data rows
            for (CardReportDTO card : cards) {
                String[] data = {
                    card.getMaskedCardNumber() != null ? card.getMaskedCardNumber() : "N/A",
                    card.getExpiryDate() != null ? card.getExpiryDate().format(DATE_FORMATTER) : "N/A",
                    card.getCreditLimit() != null ? String.format("%.2f", card.getCreditLimit()) : "0.00",
                    card.getCardStatusDescription() != null ? card.getCardStatusDescription() : "N/A",
                    card.getCashLimit() != null ? String.format("%.2f", card.getCashLimit()) : "0.00",
                    card.getAvailableCreditLimit() != null ? String.format("%.2f", card.getAvailableCreditLimit()) : "0.00",
                    card.getAvailableCashLimit() != null ? String.format("%.2f", card.getAvailableCashLimit()) : "0.00",
                    card.getLastUpdateTime() != null ? card.getLastUpdateTime().format(DATETIME_FORMATTER) : "N/A",
                    card.getLastUpdateUser() != null ? card.getLastUpdateUser() : "System"
                };
                csvWriter.writeNext(data);
            }
            
            // Add metadata comment at the end
            String[] metadata = {"", "", "", "", "", "", "", "", ""};
            csvWriter.writeNext(metadata);
            String[] note = {"Note:", "Audit information includes the last update time and user who made the changes", "", "", "", "", "", "", ""};
            csvWriter.writeNext(note);
            String[] generated = {"Generated on:", LocalDateTime.now().format(DATETIME_FORMATTER), "", "", "", "", "", "", ""};
            csvWriter.writeNext(generated);
            
            csvWriter.close();
            
            log.info("CSV report generated successfully with {} cards", cards.size());
            
        } catch (Exception e) {
            log.error("Error generating CSV report", e);
            throw new RuntimeException("Failed to generate CSV report: " + e.getMessage(), e);
        }
        
        return baos;
    }
    
    private void addHeaderCell(Table table, String text) {
        Cell cell = new Cell()
                .add(new Paragraph(text)
                        .setBold()
                        .setFontColor(ColorConstants.WHITE)
                        .setFontSize(8))
                .setBackgroundColor(HEADER_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(5);
        table.addHeaderCell(cell);
    }
    
    private void addDataCell(Table table, String text, boolean isAlternateRow) {
        Cell cell = new Cell()
                .add(new Paragraph(text).setFontSize(7))
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(4);
        
        if (isAlternateRow) {
            cell.setBackgroundColor(ALTERNATE_ROW_COLOR);
        }
        
        table.addCell(cell);
    }
}
