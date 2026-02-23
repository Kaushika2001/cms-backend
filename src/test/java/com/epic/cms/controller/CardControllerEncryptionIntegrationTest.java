package com.epic.cms.controller;

import com.epic.cms.dto.CardDTO;
import com.epic.cms.dto.CreateCardDTO;
import com.epic.cms.dto.EncryptedPayload;
import com.epic.cms.model.Card;
import com.epic.cms.repository.ICardRepository;
import com.epic.cms.util.EncryptionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Card encryption functionality.
 * Tests the complete flow from encrypted frontend payload to database storage.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CardControllerEncryptionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ICardRepository cardRepository;

    @Value("${encryption.transport.key}")
    private String transportKey;

    @Value("${encryption.storage.key}")
    private String storageKey;

    private String testCardNumber;
    private static final DateTimeFormatter MM_YYYY_FORMATTER = DateTimeFormatter.ofPattern("MM-yyyy");

    @BeforeEach
    void setUp() {
        // Use a unique card number for each test to avoid conflicts
        testCardNumber = "4532" + System.currentTimeMillis() % 1000000000000L;
    }
    
    /**
     * Helper method to get expiry date in MM-YYYY format (3 years from now)
     */
    private String getFutureExpiryDate() {
        return LocalDate.now().plusYears(3).format(MM_YYYY_FORMATTER);
    }

    @AfterEach
    void tearDown() {
        // Note: Test cleanup is manual. Run the following SQL after tests if needed:
        // DELETE FROM "Card" WHERE "CardNumber" LIKE '4532%';
        // Or for encrypted cards, you'll need to query and delete manually
    }

    @Test
    void testCreateCardWithEncryptedPayload() throws Exception {
        // Step 1: Create card DTO
        CreateCardDTO cardDTO = CreateCardDTO.builder()
                .cardNumber(testCardNumber)
                .expiryDate(getFutureExpiryDate())
                .cardStatus("IACT")
                .creditLimit(new BigDecimal("50000.00"))
                .cashLimit(new BigDecimal("10000.00"))
                .availableCreditLimit(new BigDecimal("50000.00"))
                .availableCashLimit(new BigDecimal("10000.00"))
                .build();

        // Step 2: Encrypt payload with transport key (simulate frontend)
        String json = objectMapper.writeValueAsString(cardDTO);
        String encrypted = EncryptionUtil.encrypt(json, transportKey);

        EncryptedPayload payload = new EncryptedPayload(
            encrypted, 
            LocalDateTime.now().toString()
        );

        // Step 3: Send encrypted request to backend
        MvcResult result = mockMvc.perform(post("/api/v1/cards/encrypted")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cardNumber").value(testCardNumber))
                .andExpect(jsonPath("$.cardStatus").value("IACT"))
                .andExpect(jsonPath("$.creditLimit").value(50000.00))
                .andReturn();

        // Step 4: Verify response
        String responseBody = result.getResponse().getContentAsString();
        CardDTO createdCard = objectMapper.readValue(responseBody, CardDTO.class);
        
        assertEquals(testCardNumber, createdCard.getCardNumber());
        assertEquals("IACT", createdCard.getCardStatus());
        assertEquals(new BigDecimal("50000.00"), createdCard.getCreditLimit());

        // Step 5: Verify card is encrypted in database
        Optional<Card> cardInDb = cardRepository.findByCardNumber(testCardNumber);
        assertFalse(cardInDb.isPresent(), 
            "Card should not be found by plaintext card number (it's encrypted in DB)");

        // Step 6: Verify we can find the card and decrypt it
        Card encryptedCard = cardRepository.findAll().stream()
            .filter(c -> {
                try {
                    String decrypted = EncryptionUtil.decrypt(c.getCardNumber(), storageKey);
                    return decrypted.equals(testCardNumber);
                } catch (Exception e) {
                    return false;
                }
            })
            .findFirst()
            .orElseThrow(() -> new AssertionError("Card not found in database"));

        // Card number should be encrypted in database
        assertTrue(encryptedCard.getCardNumber().contains("."), 
            "Card number should be encrypted in database (contains IV separator)");
        assertNotEquals(testCardNumber, encryptedCard.getCardNumber(), 
            "Card number should not be stored in plaintext");

        // Decrypt and verify
        String decryptedCardNumber = EncryptionUtil.decrypt(encryptedCard.getCardNumber(), storageKey);
        assertEquals(testCardNumber, decryptedCardNumber, 
            "Decrypted card number should match original");
    }

    @Test
    void testCreateCardWithInvalidEncryptedPayload() throws Exception {
        // Send invalid encrypted data
        EncryptedPayload invalidPayload = new EncryptedPayload(
            "invalid_encrypted_data", 
            LocalDateTime.now().toString()
        );

        mockMvc.perform(post("/api/v1/cards/encrypted")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPayload)))
                .andExpect(status().isBadRequest()); // Decryption failure returns 400
    }

    @Test
    void testCreateCardWithWrongEncryptionKey() throws Exception {
        // Create card DTO
        CreateCardDTO cardDTO = CreateCardDTO.builder()
                .cardNumber(testCardNumber)
                .expiryDate(getFutureExpiryDate())
                .cardStatus("IACT")
                .creditLimit(new BigDecimal("50000.00"))
                .cashLimit(new BigDecimal("10000.00"))
                .availableCreditLimit(new BigDecimal("50000.00"))
                .availableCashLimit(new BigDecimal("10000.00"))
                .build();

        // Encrypt with wrong key
        String wrongKey = EncryptionUtil.generateKey();
        String json = objectMapper.writeValueAsString(cardDTO);
        String encrypted = EncryptionUtil.encrypt(json, wrongKey);

        EncryptedPayload payload = new EncryptedPayload(
            encrypted, 
            LocalDateTime.now().toString()
        );

        // Should fail to decrypt
        mockMvc.perform(post("/api/v1/cards/encrypted")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest()); // Wrong key returns 400
    }

    @Test
    void testCreateCardWithMissingEncryptedData() throws Exception {
        EncryptedPayload emptyPayload = new EncryptedPayload(
            null, 
            LocalDateTime.now().toString()
        );

        mockMvc.perform(post("/api/v1/cards/encrypted")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyPayload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateCardWithInvalidCardData() throws Exception {
        // Create invalid card DTO (invalid card number format)
        CreateCardDTO invalidCardDTO = CreateCardDTO.builder()
                .cardNumber("invalid") // Invalid format
                .expiryDate(getFutureExpiryDate())
                .cardStatus("IACT")
                .creditLimit(new BigDecimal("50000.00"))
                .cashLimit(new BigDecimal("10000.00"))
                .availableCreditLimit(new BigDecimal("50000.00"))
                .availableCashLimit(new BigDecimal("10000.00"))
                .build();

        // Encrypt with transport key
        String json = objectMapper.writeValueAsString(invalidCardDTO);
        String encrypted = EncryptionUtil.encrypt(json, transportKey);

        EncryptedPayload payload = new EncryptedPayload(
            encrypted, 
            LocalDateTime.now().toString()
        );

        // Should fail validation
        mockMvc.perform(post("/api/v1/cards/encrypted")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testTwoLayerEncryptionFlow() throws Exception {
        // This test verifies the complete two-layer encryption flow:
        // 1. Frontend encrypts with transport key
        // 2. Backend decrypts with transport key
        // 3. Backend re-encrypts card number with storage key
        // 4. Database stores encrypted card number

        CreateCardDTO cardDTO = CreateCardDTO.builder()
                .cardNumber(testCardNumber)
                .expiryDate(getFutureExpiryDate())
                .cardStatus("IACT")
                .creditLimit(new BigDecimal("50000.00"))
                .cashLimit(new BigDecimal("10000.00"))
                .availableCreditLimit(new BigDecimal("50000.00"))
                .availableCashLimit(new BigDecimal("10000.00"))
                .build();

        // Layer 1: Transport encryption (frontend -> backend)
        String plainJson = objectMapper.writeValueAsString(cardDTO);
        String transportEncrypted = EncryptionUtil.encrypt(plainJson, transportKey);
        
        // Verify we cannot decrypt transport layer with storage key
        assertThrows(RuntimeException.class, () -> {
            EncryptionUtil.decrypt(transportEncrypted, storageKey);
        }, "Transport encrypted data should not be decryptable with storage key");

        // Send to backend
        EncryptedPayload payload = new EncryptedPayload(
            transportEncrypted, 
            LocalDateTime.now().toString()
        );

        mockMvc.perform(post("/api/v1/cards/encrypted")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated());

        // Layer 2: Storage encryption (backend -> database)
        // Find card in database
        Card storedCard = cardRepository.findAll().stream()
            .filter(c -> {
                try {
                    String decrypted = EncryptionUtil.decrypt(c.getCardNumber(), storageKey);
                    return decrypted.equals(testCardNumber);
                } catch (Exception e) {
                    return false;
                }
            })
            .findFirst()
            .orElseThrow(() -> new AssertionError("Card not found in database"));

        // Verify card number is encrypted with storage key
        assertTrue(storedCard.getCardNumber().contains("."), 
            "Card number should be encrypted in database");

        // Verify we cannot decrypt storage layer with transport key
        String storageEncrypted = storedCard.getCardNumber();
        assertThrows(RuntimeException.class, () -> {
            EncryptionUtil.decrypt(storageEncrypted, transportKey);
        }, "Storage encrypted data should not be decryptable with transport key");

        // Verify correct decryption with storage key
        String decryptedCardNumber = EncryptionUtil.decrypt(storageEncrypted, storageKey);
        assertEquals(testCardNumber, decryptedCardNumber, 
            "Card number should be correctly encrypted with storage key");
    }
}
