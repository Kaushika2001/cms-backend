package com.epic.cms.util;

/**
 * Simple manual test utility for CardMaskingUtil.
 * Run this as a main method to test the masking logic.
 */
public class CardMaskingUtilManualTest {

    public static void main(String[] args) {
        System.out.println("=== Card Masking Utility Test ===\n");
        
        // Test 1: Basic masking
        System.out.println("Test 1: Basic Masking");
        String cardNumber = "5899250123450233";
        String masked = CardMaskingUtil.mask(cardNumber);
        System.out.println("  Original: " + cardNumber);
        System.out.println("  Masked:   " + masked);
        System.out.println("  Expected: 589925******0233");
        System.out.println("  Result:   " + (masked.equals("589925******0233") ? "✓ PASS" : "✗ FAIL"));
        System.out.println();
        
        // Test 2: Extract first part
        System.out.println("Test 2: Extract First Part");
        String firstPart = CardMaskingUtil.extractFirstPart(masked);
        System.out.println("  Masked:     " + masked);
        System.out.println("  First Part: " + firstPart);
        System.out.println("  Expected:   589925");
        System.out.println("  Result:     " + (firstPart.equals("589925") ? "✓ PASS" : "✗ FAIL"));
        System.out.println();
        
        // Test 3: Extract last part
        System.out.println("Test 3: Extract Last Part");
        String lastPart = CardMaskingUtil.extractLastPart(masked);
        System.out.println("  Masked:    " + masked);
        System.out.println("  Last Part: " + lastPart);
        System.out.println("  Expected:  0233");
        System.out.println("  Result:    " + (lastPart.equals("0233") ? "✓ PASS" : "✗ FAIL"));
        System.out.println();
        
        // Test 4: Pattern matching - positive case
        System.out.println("Test 4: Pattern Matching (Positive)");
        boolean matches1 = CardMaskingUtil.matchesMaskedPattern(cardNumber, masked);
        System.out.println("  Unmasked: " + cardNumber);
        System.out.println("  Masked:   " + masked);
        System.out.println("  Matches:  " + matches1);
        System.out.println("  Result:   " + (matches1 ? "✓ PASS" : "✗ FAIL"));
        System.out.println();
        
        // Test 5: Pattern matching - negative case
        System.out.println("Test 5: Pattern Matching (Negative)");
        String wrongCard = "1234567890123456";
        boolean matches2 = CardMaskingUtil.matchesMaskedPattern(wrongCard, masked);
        System.out.println("  Unmasked: " + wrongCard);
        System.out.println("  Masked:   " + masked);
        System.out.println("  Matches:  " + matches2);
        System.out.println("  Result:   " + (!matches2 ? "✓ PASS" : "✗ FAIL"));
        System.out.println();
        
        // Test 6: Generate masked card ID (consistency)
        System.out.println("Test 6: Generate Masked Card ID");
        String maskedId1 = CardMaskingUtil.generateMaskedCardId(cardNumber);
        String maskedId2 = CardMaskingUtil.generateMaskedCardId(cardNumber);
        System.out.println("  Card Number: " + cardNumber);
        System.out.println("  Masked ID 1: " + maskedId1);
        System.out.println("  Masked ID 2: " + maskedId2);
        System.out.println("  Starts with CRD-M-: " + (maskedId1.startsWith("CRD-M-") ? "✓" : "✗"));
        System.out.println("  Consistent:        " + (maskedId1.equals(maskedId2) ? "✓ PASS" : "✗ FAIL"));
        System.out.println();
        
        // Test 7: Is masked check
        System.out.println("Test 7: Is Masked Check");
        boolean isMasked1 = CardMaskingUtil.isMasked(masked);
        boolean isMasked2 = CardMaskingUtil.isMasked(cardNumber);
        System.out.println("  \"" + masked + "\" is masked: " + isMasked1 + " " + (isMasked1 ? "✓" : "✗"));
        System.out.println("  \"" + cardNumber + "\" is masked: " + isMasked2 + " " + (!isMasked2 ? "✓" : "✗"));
        System.out.println("  Result: " + (isMasked1 && !isMasked2 ? "✓ PASS" : "✗ FAIL"));
        System.out.println();
        
        // Test 8: Different card numbers
        System.out.println("Test 8: Different Card Numbers");
        testCardNumber("1234567890123456");
        testCardNumber("4111111111111111");
        testCardNumber("378282246310005");
        
        System.out.println("\n=== All Tests Complete ===");
    }
    
    private static void testCardNumber(String cardNumber) {
        String masked = CardMaskingUtil.mask(cardNumber);
        String maskedId = CardMaskingUtil.generateMaskedCardId(cardNumber);
        boolean matches = CardMaskingUtil.matchesMaskedPattern(cardNumber, masked);
        
        System.out.println("  Card: " + cardNumber);
        System.out.println("    Masked:      " + masked);
        System.out.println("    Masked ID:   " + maskedId);
        System.out.println("    Matches:     " + (matches ? "✓" : "✗"));
        System.out.println();
    }
}
