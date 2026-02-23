package com.epic.cms.util;

import com.epic.cms.dto.CreateCardDTO;
import com.epic.cms.dto.EncryptedPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class to manually test encryption and decryption of card payloads.
 * This simulates what the frontend does when encrypting card data.
 * 
 * Usage:
 * 1. Run this class to generate an encrypted payload
 * 2. Copy the encrypted payload JSON
 * 3. Send it to POST /api/v1/cards/encrypted using curl or Postman
 * 
 * Example:
 * ./mvnw exec:java -Dexec.mainClass="com.epic.cms.util.ManualEncryptionTester"
 */
public class ManualEncryptionTester {

    // Transport key from application.yaml (same as frontend VITE_ENCRYPTION_KEY)
    private static final String TRANSPORT_KEY = "bPRw3dR0xfnHIn3Vr0mFS4BLjEvzDpWPW/0k2+P+72c=";
    private static final DateTimeFormatter MM_YYYY_FORMATTER = DateTimeFormatter.ofPattern("MM-yyyy");
    
    /**
     * Helper method to get expiry date in MM-YYYY format (3 years from now)
     */
    private static String getFutureExpiryDate() {
        return LocalDate.now().plusYears(3).format(MM_YYYY_FORMATTER);
    }
    
    public static void main(String[] args) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            
            System.out.println("=".repeat(80));
            System.out.println("Manual Encryption Tester for Card Management System");
            System.out.println("=".repeat(80));
            System.out.println();
            
            // Example 1: Create and encrypt a valid card payload
            System.out.println("Example 1: Creating and encrypting a VALID card payload");
            System.out.println("-".repeat(80));
            
            CreateCardDTO validCard = CreateCardDTO.builder()
                    .cardNumber("4532015112830366")
                    .expiryDate(getFutureExpiryDate())
                    .cardStatus("IACT")
                    .creditLimit(new BigDecimal("50000.00"))
                    .cashLimit(new BigDecimal("10000.00"))
                    .availableCreditLimit(new BigDecimal("50000.00"))
                    .availableCashLimit(new BigDecimal("10000.00"))
                    .build();
            
            String validJson = objectMapper.writeValueAsString(validCard);
            System.out.println("Original Card Data (JSON):");
            System.out.println(validJson);
            System.out.println();
            
            // Encrypt the payload
            String encryptedData = EncryptionUtil.encrypt(validJson, TRANSPORT_KEY);
            
            EncryptedPayload validPayload = new EncryptedPayload(
                encryptedData,
                LocalDateTime.now().toString()
            );
            
            String encryptedPayloadJson = objectMapper.writeValueAsString(validPayload);
            System.out.println("Encrypted Payload (send this to backend):");
            System.out.println(encryptedPayloadJson);
            System.out.println();
            
            System.out.println("Test this payload with curl:");
            System.out.println("curl -X POST http://localhost:8080/api/v1/cards/encrypted \\");
            System.out.println("  -H \"Content-Type: application/json\" \\");
            System.out.println("  -d '" + encryptedPayloadJson + "'");
            System.out.println();
            System.out.println("=".repeat(80));
            System.out.println();
            
            // Example 2: Test decryption (what backend does)
            System.out.println("Example 2: Testing DECRYPTION (what backend does)");
            System.out.println("-".repeat(80));
            
            String decryptedJson = EncryptionUtil.decrypt(encryptedData, TRANSPORT_KEY);
            System.out.println("Decrypted Data:");
            System.out.println(decryptedJson);
            System.out.println();
            
            CreateCardDTO decryptedCard = objectMapper.readValue(decryptedJson, CreateCardDTO.class);
            System.out.println("Parsed Card Object:");
            System.out.println("  Card Number: " + CardMaskingUtil.mask(decryptedCard.getCardNumber()));
            System.out.println("  Expiry Date: " + decryptedCard.getExpiryDate());
            System.out.println("  Status: " + decryptedCard.getCardStatus());
            System.out.println("  Credit Limit: $" + decryptedCard.getCreditLimit());
            System.out.println();
            System.out.println("=".repeat(80));
            System.out.println();
            
            // Example 3: Invalid card (should fail validation)
            System.out.println("Example 3: Creating payload with INVALID card data");
            System.out.println("-".repeat(80));
            
            CreateCardDTO invalidCard = CreateCardDTO.builder()
                    .cardNumber("123") // Invalid - too short
                    .expiryDate(getFutureExpiryDate())
                    .cardStatus("IACT")
                    .creditLimit(new BigDecimal("50000.00"))
                    .cashLimit(new BigDecimal("10000.00"))
                    .availableCreditLimit(new BigDecimal("50000.00"))
                    .availableCashLimit(new BigDecimal("10000.00"))
                    .build();
            
            String invalidJson = objectMapper.writeValueAsString(invalidCard);
            String invalidEncrypted = EncryptionUtil.encrypt(invalidJson, TRANSPORT_KEY);
            
            EncryptedPayload invalidPayload = new EncryptedPayload(
                invalidEncrypted,
                LocalDateTime.now().toString()
            );
            
            String invalidPayloadJson = objectMapper.writeValueAsString(invalidPayload);
            System.out.println("Encrypted Payload with Invalid Card (should return 400):");
            System.out.println(invalidPayloadJson);
            System.out.println();
            
            System.out.println("Expected Response: 400 Bad Request");
            System.out.println("Expected Error: 'Card number must be 13-16 digits'");
            System.out.println();
            System.out.println("=".repeat(80));
            System.out.println();
            
            System.out.println("✓ Manual encryption test completed successfully!");
            System.out.println();
            System.out.println("Next Steps:");
            System.out.println("1. Start the application: ./mvnw spring-boot:run");
            System.out.println("2. Copy one of the curl commands above");
            System.out.println("3. Run it in a new terminal to test the encrypted endpoint");
            System.out.println("4. Check logs/system.log for audit trail");
            
        } catch (Exception e) {
            System.err.println("Error during encryption test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
