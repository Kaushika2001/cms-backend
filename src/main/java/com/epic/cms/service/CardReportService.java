package com.epic.cms.service;

import java.io.ByteArrayOutputStream;

/**
 * Service interface for generating card reports
 */
public interface CardReportService {
    
    /**
     * Generate a PDF report of all cards with their details and audit information
     * Fields include: masked card number, expiry date, credit limit, card status,
     * cash limit, available credit limit, available cash limit, last update time, last update user
     * @return ByteArrayOutputStream containing the PDF data
     */
    ByteArrayOutputStream generatePdfReport();
    
    /**
     * Generate a CSV report of all cards with their details and audit information
     * Fields include: masked card number, expiry date, credit limit, card status,
     * cash limit, available credit limit, available cash limit, last update time, last update user
     * @return ByteArrayOutputStream containing the CSV data
     */
    ByteArrayOutputStream generateCsvReport();
}
