package com.epic.cms.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EncryptionUtil.
 * Tests AES-256-GCM encryption and decryption functionality.
 */
class EncryptionUtilTest {
    
    // Test key (32 bytes for AES-256, base64 encoded)
    private static final String TEST_KEY = "bPRw3dR0xfnHIn3Vr0mFS4BLjEvzDpWPW/0k2+P+72c=";
    
    @Test
    void testEncryptDecrypt() {
        String plaintext = "1234567890123456";
        
        // Encrypt
        String encrypted = EncryptionUtil.encrypt(plaintext, TEST_KEY);
        assertNotNull(encrypted, "Encrypted data should not be null");
        assertTrue(encrypted.contains("."), "Encrypted data should contain IV separator");
        assertNotEquals(plaintext, encrypted, "Encrypted data should differ from plaintext");
        
        // Decrypt
        String decrypted = EncryptionUtil.decrypt(encrypted, TEST_KEY);
        assertEquals(plaintext, decrypted, "Decrypted data should match original plaintext");
    }
    
    @Test
    void testEncryptionProducesDifferentResults() {
        String plaintext = "1234567890123456";
        
        // Encrypt the same plaintext twice
        String encrypted1 = EncryptionUtil.encrypt(plaintext, TEST_KEY);
        String encrypted2 = EncryptionUtil.encrypt(plaintext, TEST_KEY);
        
        // Results should be different due to random IV
        assertNotEquals(encrypted1, encrypted2, 
            "Two encryptions of same plaintext should produce different ciphertexts (different IVs)");
        
        // But both should decrypt to the same plaintext
        assertEquals(plaintext, EncryptionUtil.decrypt(encrypted1, TEST_KEY));
        assertEquals(plaintext, EncryptionUtil.decrypt(encrypted2, TEST_KEY));
    }
    
    @Test
    void testInvalidKeyThrowsException() {
        String plaintext = "test";
        String invalidKey = "invalid_key";
        
        assertThrows(RuntimeException.class, () -> {
            EncryptionUtil.encrypt(plaintext, invalidKey);
        }, "Encryption with invalid key should throw RuntimeException");
    }
    
    @Test
    void testInvalidEncryptedDataFormatThrowsException() {
        String invalidData = "invalid_encrypted_data_without_separator";
        
        assertThrows(IllegalArgumentException.class, () -> {
            EncryptionUtil.decrypt(invalidData, TEST_KEY);
        }, "Decryption with invalid format should throw IllegalArgumentException");
    }
    
    @Test
    void testNullPlaintextThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            EncryptionUtil.encrypt(null, TEST_KEY);
        }, "Encryption with null plaintext should throw IllegalArgumentException");
    }
    
    @Test
    void testEmptyPlaintextThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            EncryptionUtil.encrypt("", TEST_KEY);
        }, "Encryption with empty plaintext should throw IllegalArgumentException");
    }
    
    @Test
    void testNullEncryptedDataThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            EncryptionUtil.decrypt(null, TEST_KEY);
        }, "Decryption with null data should throw IllegalArgumentException");
    }
    
    @Test
    void testEmptyEncryptedDataThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            EncryptionUtil.decrypt("", TEST_KEY);
        }, "Decryption with empty data should throw IllegalArgumentException");
    }
    
    @Test
    void testValidateKey() {
        // Valid 32-byte key
        assertTrue(EncryptionUtil.validateKey(TEST_KEY), "Valid 32-byte key should pass validation");
        
        // Invalid keys
        assertFalse(EncryptionUtil.validateKey("short_key"), "Short key should fail validation");
        assertFalse(EncryptionUtil.validateKey(null), "Null key should fail validation");
        assertFalse(EncryptionUtil.validateKey(""), "Empty key should fail validation");
    }
    
    @Test
    void testGenerateKey() {
        String key1 = EncryptionUtil.generateKey();
        String key2 = EncryptionUtil.generateKey();
        
        // Keys should be valid
        assertTrue(EncryptionUtil.validateKey(key1), "Generated key 1 should be valid");
        assertTrue(EncryptionUtil.validateKey(key2), "Generated key 2 should be valid");
        
        // Keys should be different
        assertNotEquals(key1, key2, "Generated keys should be unique");
        
        // Generated key should work for encryption/decryption
        String plaintext = "test_message";
        String encrypted = EncryptionUtil.encrypt(plaintext, key1);
        String decrypted = EncryptionUtil.decrypt(encrypted, key1);
        assertEquals(plaintext, decrypted, "Generated key should work for encryption/decryption");
    }
    
    @Test
    void testEncryptLongText() {
        String longText = "This is a much longer text that contains multiple sentences. " +
                         "It includes special characters like !@#$%^&*() and numbers 1234567890. " +
                         "Testing encryption with larger payloads is important for real-world scenarios.";
        
        String encrypted = EncryptionUtil.encrypt(longText, TEST_KEY);
        String decrypted = EncryptionUtil.decrypt(encrypted, TEST_KEY);
        
        assertEquals(longText, decrypted, "Long text should encrypt and decrypt correctly");
    }
    
    @Test
    void testEncryptSpecialCharacters() {
        String specialText = "Card#1234-5678-9012-3456!@#$%^&*()_+-=[]{}|;':\",./<>?";
        
        String encrypted = EncryptionUtil.encrypt(specialText, TEST_KEY);
        String decrypted = EncryptionUtil.decrypt(encrypted, TEST_KEY);
        
        assertEquals(specialText, decrypted, "Special characters should encrypt and decrypt correctly");
    }
    
    @Test
    void testEncryptUnicodeCharacters() {
        String unicodeText = "Card 카드 😀 € £ ¥";
        
        String encrypted = EncryptionUtil.encrypt(unicodeText, TEST_KEY);
        String decrypted = EncryptionUtil.decrypt(encrypted, TEST_KEY);
        
        assertEquals(unicodeText, decrypted, "Unicode characters should encrypt and decrypt correctly");
    }
    
    @Test
    void testWrongKeyCannotDecrypt() {
        String plaintext = "secret_data";
        String key1 = EncryptionUtil.generateKey();
        String key2 = EncryptionUtil.generateKey();
        
        String encrypted = EncryptionUtil.encrypt(plaintext, key1);
        
        assertThrows(RuntimeException.class, () -> {
            EncryptionUtil.decrypt(encrypted, key2);
        }, "Decryption with wrong key should fail");
    }
}
