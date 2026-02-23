package com.epic.cms.config;

import com.epic.cms.util.EncryptionUtil;
import com.epic.cms.util.LoggerUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 * Validates encryption keys on application startup.
 * Ensures that both transport and storage encryption keys are properly configured
 * before the application starts accepting requests.
 */
@Component
public class EncryptionKeyValidator {
    
    private final EncryptionConfig encryptionConfig;
    
    public EncryptionKeyValidator(EncryptionConfig encryptionConfig) {
        this.encryptionConfig = encryptionConfig;
    }
    
    /**
     * Validates encryption keys immediately after bean construction.
     * This ensures the application fails fast if keys are invalid.
     * 
     * @throws IllegalStateException if any key is invalid
     */
    @PostConstruct
    public void validateKeys() {
        LoggerUtil.info(EncryptionKeyValidator.class, "Validating encryption keys on startup...");
        
        try {
            // Validate transport key
            String transportKey = encryptionConfig.getTransportKey();
            if (!EncryptionUtil.validateKey(transportKey)) {
                LoggerUtil.error(EncryptionKeyValidator.class, "Invalid transport encryption key");
                throw new IllegalStateException(
                    "Transport encryption key is invalid. Must be 32 bytes (256 bits) Base64 encoded."
                );
            }
            LoggerUtil.info(EncryptionKeyValidator.class, "✓ Transport encryption key is valid");
            
            // Validate storage key
            String storageKey = encryptionConfig.getStorageKey();
            if (!EncryptionUtil.validateKey(storageKey)) {
                LoggerUtil.error(EncryptionKeyValidator.class, "Invalid storage encryption key");
                throw new IllegalStateException(
                    "Storage encryption key is invalid. Must be 32 bytes (256 bits) Base64 encoded."
                );
            }
            LoggerUtil.info(EncryptionKeyValidator.class, "✓ Storage encryption key is valid");
            
            // Verify keys are different (security best practice)
            if (transportKey.equals(storageKey)) {
                LoggerUtil.warn(EncryptionKeyValidator.class, 
                    "WARNING: Transport and storage keys are identical. " +
                    "It's recommended to use different keys for each layer."
                );
            }
            
            // Test encryption/decryption with both keys
            testEncryptionRoundTrip(transportKey, "transport");
            testEncryptionRoundTrip(storageKey, "storage");
            
            LoggerUtil.info(EncryptionKeyValidator.class, 
                "✓ All encryption keys validated successfully. System is ready for encrypted operations.");
            
        } catch (IllegalStateException e) {
            LoggerUtil.error(EncryptionKeyValidator.class, 
                "CRITICAL: Encryption key validation failed. Application cannot start.", e);
            throw e;
        } catch (Exception e) {
            LoggerUtil.error(EncryptionKeyValidator.class, 
                "Unexpected error during encryption key validation", e);
            throw new IllegalStateException("Failed to validate encryption keys", e);
        }
    }
    
    /**
     * Test encryption and decryption with a given key.
     * 
     * @param key Base64 encoded key to test
     * @param keyName Name of the key (for logging)
     */
    private void testEncryptionRoundTrip(String key, String keyName) {
        try {
            String testData = "TEST_DATA_" + System.currentTimeMillis();
            
            // Encrypt
            String encrypted = EncryptionUtil.encrypt(testData, key);
            
            // Decrypt
            String decrypted = EncryptionUtil.decrypt(encrypted, key);
            
            // Verify
            if (!testData.equals(decrypted)) {
                throw new IllegalStateException(
                    "Encryption round-trip test failed for " + keyName + " key. " +
                    "Data mismatch after encryption/decryption."
                );
            }
            
            LoggerUtil.debug(EncryptionKeyValidator.class, 
                "✓ Encryption round-trip test passed for {} key", keyName);
            
        } catch (Exception e) {
            LoggerUtil.error(EncryptionKeyValidator.class, 
                "Encryption round-trip test failed for {} key", keyName, e);
            throw new IllegalStateException(
                "Failed encryption round-trip test for " + keyName + " key", e
            );
        }
    }
}
