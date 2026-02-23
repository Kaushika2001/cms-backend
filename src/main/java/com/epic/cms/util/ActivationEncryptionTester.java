package com.epic.cms.util;

import com.epic.cms.dto.EncryptedPayload;
import com.epic.cms.dto.UpdateCardStatusDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

/**
 * Utility to generate encrypted activation/status update test payloads
 */
public class ActivationEncryptionTester {

    private static final String TRANSPORT_KEY = "bPRw3dR0xfnHIn3Vr0mFS4BLjEvzDpWPW/0k2+P+72c=";
    
    public static void main(String[] args) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            
            System.out.println("=".repeat(80));
            System.out.println("Card Activation - Encrypted Payload Generator");
            System.out.println("=".repeat(80));
            System.out.println();
            
            // Example 1: Activate with FULL card number
            System.out.println("Example 1: Activation with FULL CARD NUMBER");
            System.out.println("-".repeat(80));
            
            UpdateCardStatusDTO statusUpdate1 = UpdateCardStatusDTO.builder()
                    .cardNumber("4532015112830366")  // Full card number
                    .newStatus("CACT")  // Activate card
                    .build();
            
            String json1 = objectMapper.writeValueAsString(statusUpdate1);
            System.out.println("Original Data (JSON):");
            System.out.println(json1);
            System.out.println();
            
            String encryptedData1 = EncryptionUtil.encrypt(json1, TRANSPORT_KEY);
            EncryptedPayload payload1 = new EncryptedPayload(
                encryptedData1,
                LocalDateTime.now().toString()
            );
            
            String payloadJson1 = objectMapper.writeValueAsString(payload1);
            System.out.println("Encrypted Payload:");
            System.out.println(payloadJson1);
            System.out.println();
            
            System.out.println("curl Command:");
            System.out.println("curl -X POST http://localhost:8080/api/v1/cards/activate/encrypted \\");
            System.out.println("  -H \"Content-Type: application/json\" \\");
            System.out.println("  -d '" + payloadJson1 + "'");
            System.out.println();
            System.out.println("=".repeat(80));
            System.out.println();
            
            // Example 2: Activate with MASKED card number
            System.out.println("Example 2: Activation with MASKED CARD NUMBER");
            System.out.println("-".repeat(80));
            
            UpdateCardStatusDTO statusUpdate2 = UpdateCardStatusDTO.builder()
                    .cardNumber("453201******0366")  // Masked card number (frontend gets this from lookup)
                    .newStatus("CACT")  // Activate card
                    .build();
            
            String json2 = objectMapper.writeValueAsString(statusUpdate2);
            System.out.println("Original Data (JSON):");
            System.out.println(json2);
            System.out.println();
            
            String encryptedData2 = EncryptionUtil.encrypt(json2, TRANSPORT_KEY);
            EncryptedPayload payload2 = new EncryptedPayload(
                encryptedData2,
                LocalDateTime.now().toString()
            );
            
            String payloadJson2 = objectMapper.writeValueAsString(payload2);
            System.out.println("Encrypted Payload:");
            System.out.println(payloadJson2);
            System.out.println();
            
            System.out.println("curl Command:");
            System.out.println("curl -X POST http://localhost:8080/api/v1/cards/activate/encrypted \\");
            System.out.println("  -H \"Content-Type: application/json\" \\");
            System.out.println("  -d '" + payloadJson2 + "'");
            System.out.println();
            System.out.println("=".repeat(80));
            System.out.println();
            
            // Example 3: Deactivate card
            System.out.println("Example 3: DEACTIVATE Card");
            System.out.println("-".repeat(80));
            
            UpdateCardStatusDTO statusUpdate3 = UpdateCardStatusDTO.builder()
                    .cardNumber("453201******0366")
                    .newStatus("DACT")  // Deactivate card
                    .build();
            
            String json3 = objectMapper.writeValueAsString(statusUpdate3);
            String encryptedData3 = EncryptionUtil.encrypt(json3, TRANSPORT_KEY);
            EncryptedPayload payload3 = new EncryptedPayload(
                encryptedData3,
                LocalDateTime.now().toString()
            );
            
            String payloadJson3 = objectMapper.writeValueAsString(payload3);
            System.out.println("Encrypted Payload (Deactivate):");
            System.out.println(payloadJson3);
            System.out.println();
            System.out.println("=".repeat(80));
            System.out.println();
            
            System.out.println("✓ Activation payload generation completed successfully!");
            System.out.println();
            System.out.println("Status Codes:");
            System.out.println("  IACT = Inactive");
            System.out.println("  CACT = Active");
            System.out.println("  DACT = Deactivated");
            System.out.println();
            System.out.println("Workflow:");
            System.out.println("1. Frontend gets masked card number from lookup: GET /api/v1/cards/masked-id?cardNumber=XXX");
            System.out.println("2. Frontend encrypts activation request with masked card number");
            System.out.println("3. Backend decrypts, looks up card by masked pattern, updates status");
            System.out.println("4. Returns updated card with masked card number");
            
        } catch (Exception e) {
            System.err.println("Error generating activation payload: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
