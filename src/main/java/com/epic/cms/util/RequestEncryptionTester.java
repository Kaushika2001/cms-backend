package com.epic.cms.util;

import com.epic.cms.config.EncryptionConfig;
import com.epic.cms.dto.CreateCardRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Utility class to generate encrypted payloads for card request creation testing.
 * 
 * Usage:
 * 1. Uncomment @Component annotation to enable
 * 2. Run the application
 * 3. Copy the JSON output
 * 4. Use with: POST http://localhost:8080/api/v1/requests/encrypted
 * 5. Comment out @Component after testing
 */
//@Component
@RequiredArgsConstructor
@Slf4j
public class RequestEncryptionTester implements CommandLineRunner {

    private final EncryptionConfig encryptionConfig;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        log.info("\n\n" + "=".repeat(80));
        log.info("CARD REQUEST ENCRYPTION TESTER");
        log.info("=".repeat(80));

        // Test Case 1: Card Activation Request
        generateActivationRequest();

        // Test Case 2: Card Closure Request
        generateClosureRequest();

        log.info("=".repeat(80) + "\n");
    }

    private void generateActivationRequest() throws Exception {
        log.info("\n--- Test Case 1: Card Activation Request (ACTI) ---");

        CreateCardRequestDTO requestDTO = CreateCardRequestDTO.builder()
                .cardNumber("4532019482960366")
                .requestReasonCode("ACTI")
                .remark("Customer requested card activation")
                .build();

        String json = objectMapper.writeValueAsString(requestDTO);
        log.info("Original CreateCardRequestDTO JSON:\n{}", json);

        String transportKey = encryptionConfig.getTransportKey();
        String encryptedData = EncryptionUtil.encrypt(json, transportKey);

        // Create the EncryptedPayload JSON manually
        String encryptedJson = String.format("{\"encryptedData\":\"%s\"}", encryptedData);

        log.info("\nEncrypted Payload for Postman/cURL:");
        log.info("{}", encryptedJson);

        log.info("\ncURL Command:");
        log.info("curl -X POST http://localhost:8080/api/v1/requests/encrypted \\");
        log.info("  -H \"Content-Type: application/json\" \\");
        log.info("  -d '{}'", encryptedJson.replace("\"", "\\\""));
    }

    private void generateClosureRequest() throws Exception {
        log.info("\n--- Test Case 2: Card Closure Request (CDCL) ---");

        CreateCardRequestDTO requestDTO = CreateCardRequestDTO.builder()
                .cardNumber("4532019482960366")
                .requestReasonCode("CDCL")
                .remark("Customer requested card closure due to lost card")
                .build();

        String json = objectMapper.writeValueAsString(requestDTO);
        log.info("Original CreateCardRequestDTO JSON:\n{}", json);

        String transportKey = encryptionConfig.getTransportKey();
        String encryptedData = EncryptionUtil.encrypt(json, transportKey);

        // Create the EncryptedPayload JSON manually
        String encryptedJson = String.format("{\"encryptedData\":\"%s\"}", encryptedData);

        log.info("\nEncrypted Payload for Postman/cURL:");
        log.info("{}", encryptedJson);

        log.info("\ncURL Command:");
        log.info("curl -X POST http://localhost:8080/api/v1/requests/encrypted \\");
        log.info("  -H \"Content-Type: application/json\" \\");
        log.info("  -d '{}'", encryptedJson.replace("\"", "\\\""));
    }
}
