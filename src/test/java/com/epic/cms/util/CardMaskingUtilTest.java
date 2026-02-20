package com.epic.cms.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for CardMaskingUtil to verify masking and pattern matching logic.
 */
public class CardMaskingUtilTest {

    @Test
    public void testMaskCardNumber() {
        String cardNumber = "5899250123450233";
        String masked = CardMaskingUtil.mask(cardNumber);
        
        System.out.println("Original: " + cardNumber);
        System.out.println("Masked: " + masked);
        
        assertEquals("589925******0233", masked);
    }

    @Test
    public void testExtractFirstPart() {
        String masked = "589925******0233";
        String firstPart = CardMaskingUtil.extractFirstPart(masked);
        
        System.out.println("Masked: " + masked);
        System.out.println("First Part: " + firstPart);
        
        assertEquals("589925", firstPart);
    }

    @Test
    public void testExtractLastPart() {
        String masked = "589925******0233";
        String lastPart = CardMaskingUtil.extractLastPart(masked);
        
        System.out.println("Masked: " + masked);
        System.out.println("Last Part: " + lastPart);
        
        assertEquals("0233", lastPart);
    }

    @Test
    public void testMatchesMaskedPattern() {
        String unmasked = "5899250123450233";
        String masked = "589925******0233";
        
        boolean matches = CardMaskingUtil.matchesMaskedPattern(unmasked, masked);
        
        System.out.println("Unmasked: " + unmasked);
        System.out.println("Masked: " + masked);
        System.out.println("Matches: " + matches);
        
        assertTrue(matches);
    }

    @Test
    public void testMatchesMaskedPatternNegative() {
        String unmasked = "1234567890123456";
        String masked = "589925******0233";
        
        boolean matches = CardMaskingUtil.matchesMaskedPattern(unmasked, masked);
        
        System.out.println("Unmasked: " + unmasked);
        System.out.println("Masked: " + masked);
        System.out.println("Matches: " + matches);
        
        assertFalse(matches);
    }

    @Test
    public void testGenerateMaskedCardId() {
        String cardNumber = "5899250123450233";
        String maskedId1 = CardMaskingUtil.generateMaskedCardId(cardNumber);
        String maskedId2 = CardMaskingUtil.generateMaskedCardId(cardNumber);
        
        System.out.println("Card Number: " + cardNumber);
        System.out.println("Masked ID 1: " + maskedId1);
        System.out.println("Masked ID 2: " + maskedId2);
        
        assertNotNull(maskedId1);
        assertTrue(maskedId1.startsWith("CRD-M-"));
        assertEquals(maskedId1, maskedId2); // Should be consistent
    }

    @Test
    public void testIsMasked() {
        assertTrue(CardMaskingUtil.isMasked("589925******0233"));
        assertFalse(CardMaskingUtil.isMasked("5899250123450233"));
        assertFalse(CardMaskingUtil.isMasked(null));
    }
}
