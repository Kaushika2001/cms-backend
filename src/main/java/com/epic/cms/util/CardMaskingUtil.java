package com.epic.cms.util;

/**
 * Utility class for masking card numbers.
 * Masks the middle digits while preserving the first 6 and last 4 digits.
 */
public class CardMaskingUtil {

    private static final char MASK_CHAR = '*';
    private static final int FIRST_DIGITS = 6;
    private static final int LAST_DIGITS = 4;

    /**
     * Masks a card number, showing only the first 6 and last 4 digits.
     * Example: 1234567890123456 becomes 123456******3456
     *
     * @param cardNumber the card number to mask
     * @return the masked card number
     */
    public static String mask(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return cardNumber;
        }

        // Remove any spaces or dashes
        String cleanCardNumber = cardNumber.replaceAll("[\\s-]", "");

        // If card number is too short to mask properly, mask everything except last 4
        if (cleanCardNumber.length() <= FIRST_DIGITS + LAST_DIGITS) {
            int visibleDigits = Math.min(4, cleanCardNumber.length());
            int maskedLength = cleanCardNumber.length() - visibleDigits;
            return MASK_CHAR + "".repeat(Math.max(0, maskedLength - 1)) + 
                   cleanCardNumber.substring(Math.max(0, cleanCardNumber.length() - visibleDigits));
        }

        // Mask the middle portion
        String firstPart = cleanCardNumber.substring(0, FIRST_DIGITS);
        String lastPart = cleanCardNumber.substring(cleanCardNumber.length() - LAST_DIGITS);
        int middleLength = cleanCardNumber.length() - FIRST_DIGITS - LAST_DIGITS;
        String maskedMiddle = String.valueOf(MASK_CHAR).repeat(middleLength);

        return firstPart + maskedMiddle + lastPart;
    }

    /**
     * Masks a card number with formatting (groups of 4 digits).
     * Example: 1234567890123456 becomes 1234 56** **** 3456
     *
     * @param cardNumber the card number to mask
     * @return the masked and formatted card number
     */
    public static String maskWithFormat(String cardNumber) {
        String masked = mask(cardNumber);
        if (masked == null || masked.isEmpty()) {
            return masked;
        }

        // Add spaces every 4 characters
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < masked.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                formatted.append(" ");
            }
            formatted.append(masked.charAt(i));
        }
        return formatted.toString();
    }

    /**
     * Checks if a card number appears to be already masked.
     *
     * @param cardNumber the card number to check
     * @return true if the card number contains mask characters
     */
    public static boolean isMasked(String cardNumber) {
        return cardNumber != null && cardNumber.indexOf(MASK_CHAR) >= 0;
    }
}
