package com.epic.cms.controller;

import com.epic.cms.service.CardReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Card Reports", description = "API endpoints for generating card reports with audit information")
public class CardReportController {

    private final CardReportService cardReportService;
    
    private static final DateTimeFormatter FILENAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @GetMapping(value = "/cards/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(
        summary = "Generate PDF card report", 
        description = "Generate a comprehensive PDF report containing all card details including:\n" +
                     "- Masked card number\n" +
                     "- Expiry date\n" +
                     "- Credit limit\n" +
                     "- Card status (description from CardStatus table)\n" +
                     "- Cash limit\n" +
                     "- Available credit limit\n" +
                     "- Available cash limit\n" +
                     "- Last update time (Audit)\n" +
                     "- Last update user (Audit)\n\n" +
                     "The report is formatted with headers and alternating row colors for better readability."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "PDF report generated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error during report generation")
    })
    public ResponseEntity<byte[]> generatePdfReport() {
        log.info("Received request to generate PDF card report with audit information");
        
        try {
            ByteArrayOutputStream baos = cardReportService.generatePdfReport();
            
            String filename = "card_report_" + LocalDateTime.now().format(FILENAME_FORMATTER) + ".pdf";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            headers.add("X-Report-Type", "Card Report with Audit");
            headers.add("X-Generated-At", LocalDateTime.now().toString());
            
            log.info("PDF card report generated successfully: {}", filename);
            
            return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Error generating PDF card report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating PDF report: " + e.getMessage()).getBytes());
        }
    }

    @GetMapping(value = "/cards/csv", produces = "text/csv")
    @Operation(
        summary = "Generate CSV card report", 
        description = "Generate a comprehensive CSV report containing all card details including:\n" +
                     "- Masked card number\n" +
                     "- Expiry date\n" +
                     "- Credit limit\n" +
                     "- Card status (description from CardStatus table)\n" +
                     "- Cash limit\n" +
                     "- Available credit limit\n" +
                     "- Available cash limit\n" +
                     "- Last update time (Audit)\n" +
                     "- Last update user (Audit)\n\n" +
                     "The CSV file is compatible with Excel and other spreadsheet applications."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "CSV report generated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error during report generation")
    })
    public ResponseEntity<byte[]> generateCsvReport() {
        log.info("Received request to generate CSV card report with audit information");
        
        try {
            ByteArrayOutputStream baos = cardReportService.generateCsvReport();
            
            String filename = "card_report_" + LocalDateTime.now().format(FILENAME_FORMATTER) + ".csv";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            headers.add("X-Report-Type", "Card Report with Audit");
            headers.add("X-Generated-At", LocalDateTime.now().toString());
            
            log.info("CSV card report generated successfully: {}", filename);
            
            return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Error generating CSV card report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating CSV report: " + e.getMessage()).getBytes());
        }
    }
}
