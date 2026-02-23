package com.epic.cms.util;

import com.epic.cms.dto.CreateCardDTO;
import com.epic.cms.dto.EncryptedPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Quick test to generate an encrypted payload with a random card number
 */
public class QuickEncryptionTest {

    private static final String TRANSPORT_KEY = "bPRw3dR0xfnHIn3Vr0mFS4BLjEvzDpWPW/0k2+P+72c=";
    private static final DateTimeFormatter MM_YYYY_FORMATTER = DateTimeFormatter.ofPattern("MM-yyyy");
    
    public static void main(String[] args) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            
            // Generate a random valid card number
            Random random = new Random();
            String randomCardNumber = "45320151128" + String.format("%05d", random.nextInt(100000));
            
            // Generate expiry date in MM-YYYY format
            String expiryDate = LocalDate.now().plusYears(3).format(MM_YYYY_FORMATTER);
            
            CreateCardDTO card = CreateCardDTO.builder()
                    .cardNumber(randomCardNumber)
                    .expiryDate(expiryDate)
                    .cardStatus("IACT")
                    .creditLimit(new BigDecimal("50000.00"))
                    .cashLimit(new BigDecimal("10000.00"))
                    .availableCreditLimit(new BigDecimal("50000.00"))
                    .availableCashLimit(new BigDecimal("10000.00"))
                    .build();
            
            String json = objectMapper.writeValueAsString(card);
            String encrypted = EncryptionUtil.encrypt(json, TRANSPORT_KEY);
            
            EncryptedPayload payload = new EncryptedPayload(
                encrypted,
                LocalDateTime.now().toString()
            );
            
            String payloadJson = objectMapper.writeValueAsString(payload);
            
            System.out.println("Card Number: " + CardMaskingUtil.mask(randomCardNumber));
            System.out.println();
            System.out.println("Encrypted Payload:");
            System.out.println(payloadJson);
            System.out.println();
            System.out.println("curl -X POST http://localhost:8080/api/v1/cards/encrypted \\");
            System.out.println("  -H \"Content-Type: application/json\" \\");
            System.out.println("  -d '" + payloadJson + "'");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
