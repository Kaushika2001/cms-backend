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

    /**
     * Generates a unique masked card ID from a card number.
     * Format: CRD-M-XXXXXXXX where XXXXXXXX is a hash of the card number.
     * This provides a stable, unique identifier without exposing the actual card number.
     *
     * @param cardNumber the card number to generate an ID from
     * @return the masked card ID (e.g., CRD-M-943E49CC)
     */
    public static String generateMaskedCardId(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return null;
        }

        // Generate a stable hash from the card number
        int hash = cardNumber.hashCode();
        // Convert to hex and take first 8 characters, uppercase
        String hexHash = Integer.toHexString(Math.abs(hash)).toUpperCase();
        
        // Pad with zeros if necessary to ensure 8 characters
        while (hexHash.length() < 8) {
            hexHash = "0" + hexHash;
        }
        
        // Take first 8 characters
        hexHash = hexHash.substring(0, Math.min(8, hexHash.length()));
        
        return "CRD-M-" + hexHash;
    }

    /**
     * Extracts the visible first part from a masked card number.
     * Example: "123456******3456" returns "123456"
     *
     * @param maskedCardNumber the masked card number
     * @return the first visible part, or null if cannot extract
     */
    public static String extractFirstPart(String maskedCardNumber) {
        if (maskedCardNumber == null || maskedCardNumber.isEmpty()) {
            return null;
        }
        
        String clean = maskedCardNumber.replaceAll("[\\s-]", "");
        int firstAsterisk = clean.indexOf(MASK_CHAR);
        
        if (firstAsterisk <= 0) {
            return null;
        }
        
        return clean.substring(0, firstAsterisk);
    }

    /**
     * Extracts the visible last part from a masked card number.
     * Example: "123456******3456" returns "3456"
     *
     * @param maskedCardNumber the masked card number
     * @return the last visible part, or null if cannot extract
     */
    public static String extractLastPart(String maskedCardNumber) {
        if (maskedCardNumber == null || maskedCardNumber.isEmpty()) {
            return null;
        }
        
        String clean = maskedCardNumber.replaceAll("[\\s-]", "");
        int lastAsterisk = clean.lastIndexOf(MASK_CHAR);
        
        if (lastAsterisk < 0 || lastAsterisk >= clean.length() - 1) {
            return null;
        }
        
        return clean.substring(lastAsterisk + 1);
    }

    /**
     * Checks if a given unmasked card number matches a masked card number pattern.
     * Example: "1234567890123456" matches "123456******3456"
     *
     * @param unmaskedCardNumber the full card number
     * @param maskedCardNumber the masked card number to match against
     * @return true if they match
     */
    public static boolean matchesMaskedPattern(String unmaskedCardNumber, String maskedCardNumber) {
        if (unmaskedCardNumber == null || maskedCardNumber == null) {
            return false;
        }
        
        String firstPart = extractFirstPart(maskedCardNumber);
        String lastPart = extractLastPart(maskedCardNumber);
        
        if (firstPart == null || lastPart == null) {
            return false;
        }
        
        String cleanUnmasked = unmaskedCardNumber.replaceAll("[\\s-]", "");
        
        return cleanUnmasked.startsWith(firstPart) && 
               cleanUnmasked.endsWith(lastPart) &&
               cleanUnmasked.length() >= firstPart.length() + lastPart.length();
    }
}
