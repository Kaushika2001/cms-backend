package com.epic.cms.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ExpiryDateUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MM-yyyy");

    /**
     * Converts expiry date from MM-YYYY format to LocalDate (last day of the month).
     * Credit cards expire at the end of the specified month.
     * 
     * @param expiryDate String in MM-YYYY format (e.g., "02-2026")
     * @return LocalDate representing the last day of the expiry month
     * @throws IllegalArgumentException if the format is invalid
     */
    public static LocalDate convertToLocalDate(String expiryDate) {
        if (expiryDate == null || expiryDate.trim().isEmpty()) {
            throw new IllegalArgumentException("Expiry date cannot be null or empty");
        }

        try {
            YearMonth yearMonth = YearMonth.parse(expiryDate, FORMATTER);
            // Return the last day of the month since cards expire at end of month
            return yearMonth.atEndOfMonth();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid expiry date format. Expected MM-YYYY (e.g., 02-2026)", e);
        }
    }

    /**
     * Validates that the expiry date is in the future.
     * 
     * @param expiryDate String in MM-YYYY format
     * @return true if the expiry date is in the future
     */
    public static boolean isFutureDate(String expiryDate) {
        try {
            LocalDate expiryLocalDate = convertToLocalDate(expiryDate);
            return expiryLocalDate.isAfter(LocalDate.now());
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Formats a LocalDate to MM-YYYY format.
     * 
     * @param date LocalDate to format
     * @return String in MM-YYYY format
     */
    public static String formatToMMYYYY(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return date.format(FORMATTER);
    }
}
