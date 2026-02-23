package com.epic.cms.util;

import com.epic.cms.util.LoggerUtil;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM Encryption Utility
 * Thread-safe encryption/decryption for card data.
 * 
 * This utility provides secure encryption using AES-256 in GCM mode,
 * which provides both confidentiality and authenticity.
 * 
 * Encrypted Format: {iv}.{ciphertext}
 * - iv: Base64 encoded initialization vector (12 bytes)
 * - ciphertext: Base64 encoded encrypted data with authentication tag
 */
public class EncryptionUtil {
    
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128; // bits
    private static final int GCM_IV_LENGTH = 12;   // bytes (96 bits)
    private static final int AES_KEY_SIZE = 256;   // bits
    
    /**
     * Encrypts data using AES-256-GCM.
     * 
     * @param plaintext Plain text to encrypt
     * @param base64Key Base64 encoded encryption key (must be 32 bytes for AES-256)
     * @return Base64 encoded format: {iv}.{encryptedData}
     * @throws RuntimeException if encryption fails
     */
    public static String encrypt(String plaintext, String base64Key) {
        try {
            // Validate input
            if (plaintext == null || plaintext.isEmpty()) {
                LoggerUtil.warn(EncryptionUtil.class, "Attempted to encrypt null or empty plaintext");
                throw new IllegalArgumentException("Plaintext cannot be null or empty");
            }
            
            // Decode the key
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            if (keyBytes.length != (AES_KEY_SIZE / 8)) {
                LoggerUtil.error(EncryptionUtil.class, "Invalid key size: {} bytes, expected 32 bytes", keyBytes.length);
                throw new IllegalArgumentException("Key must be 32 bytes for AES-256");
            }
            
            SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
            
            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            
            // Encrypt
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            
            // Combine IV and ciphertext: {iv}.{ciphertext}
            String ivBase64 = Base64.getEncoder().encodeToString(iv);
            String ciphertextBase64 = Base64.getEncoder().encodeToString(ciphertext);
            
            LoggerUtil.debug(EncryptionUtil.class, "Successfully encrypted data");
            return ivBase64 + "." + ciphertextBase64;
            
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            LoggerUtil.error(EncryptionUtil.class, "Encryption failed", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }
    
    /**
     * Decrypts data using AES-256-GCM.
     * 
     * @param encryptedData Base64 encoded format: {iv}.{encryptedData}
     * @param base64Key Base64 encoded encryption key (must be 32 bytes for AES-256)
     * @return Decrypted plain text
     * @throws RuntimeException if decryption fails
     */
    public static String decrypt(String encryptedData, String base64Key) {
        try {
            // Validate input
            if (encryptedData == null || encryptedData.isEmpty()) {
                LoggerUtil.warn(EncryptionUtil.class, "Attempted to decrypt null or empty data");
                throw new IllegalArgumentException("Encrypted data cannot be null or empty");
            }
            
            // Split IV and ciphertext
            String[] parts = encryptedData.split("\\.");
            if (parts.length != 2) {
                LoggerUtil.error(EncryptionUtil.class, "Invalid encrypted data format, expected 2 parts but got {}", parts.length);
                throw new IllegalArgumentException(
                    "Invalid encrypted data format. Expected: {iv}.{ciphertext}"
                );
            }
            
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] ciphertext = Base64.getDecoder().decode(parts[1]);
            
            // Decode the key
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            if (keyBytes.length != (AES_KEY_SIZE / 8)) {
                LoggerUtil.error(EncryptionUtil.class, "Invalid key size: {} bytes, expected 32 bytes", keyBytes.length);
                throw new IllegalArgumentException("Key must be 32 bytes for AES-256");
            }
            
            SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            
            // Decrypt
            byte[] plaintext = cipher.doFinal(ciphertext);
            
            LoggerUtil.debug(EncryptionUtil.class, "Successfully decrypted data");
            return new String(plaintext, StandardCharsets.UTF_8);
            
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            LoggerUtil.error(EncryptionUtil.class, "Decryption failed", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }
    
    /**
     * Validates encryption key format and length.
     * 
     * @param base64Key Base64 encoded key to validate
     * @return true if key is valid (32 bytes for AES-256), false otherwise
     */
    public static boolean validateKey(String base64Key) {
        try {
            if (base64Key == null || base64Key.isEmpty()) {
                return false;
            }
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            return keyBytes.length == (AES_KEY_SIZE / 8); // 32 bytes for AES-256
        } catch (Exception e) {
            LoggerUtil.warn(EncryptionUtil.class, "Key validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Generates a new random AES-256 key.
     * 
     * @return Base64 encoded 32-byte key
     */
    public static String generateKey() {
        try {
            byte[] key = new byte[AES_KEY_SIZE / 8]; // 32 bytes for AES-256
            SecureRandom random = new SecureRandom();
            random.nextBytes(key);
            return Base64.getEncoder().encodeToString(key);
        } catch (Exception e) {
            LoggerUtil.error(EncryptionUtil.class, "Failed to generate key", e);
            throw new RuntimeException("Failed to generate key", e);
        }
    }
}
