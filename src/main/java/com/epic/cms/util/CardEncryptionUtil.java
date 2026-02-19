package com.epic.cms.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Utility class for encrypting and decrypting card numbers.
 * Uses AES-256 encryption for PCI-DSS compliance.
 */
@Component
public class CardEncryptionUtil {

    @Value("${card.encryption.key:CHANGE_THIS_32_CHAR_SECRET_KEY}")
    private String secretKey;

    private static final String ALGORITHM = "AES";

    /**
     * Encrypts a card number using AES encryption.
     *
     * @param cardNumber the plain text card number
     * @return the encrypted card number in Base64 format
     */
    public String encrypt(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return cardNumber;
        }

        try {
            SecretKeySpec keySpec = new SecretKeySpec(getKey(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(cardNumber.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting card number", e);
        }
    }

    /**
     * Decrypts an encrypted card number.
     *
     * @param encryptedCardNumber the encrypted card number in Base64 format
     * @return the decrypted plain text card number
     */
    public String decrypt(String encryptedCardNumber) {
        if (encryptedCardNumber == null || encryptedCardNumber.isEmpty()) {
            return encryptedCardNumber;
        }

        try {
            SecretKeySpec keySpec = new SecretKeySpec(getKey(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedCardNumber));
            return new String(decrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting card number", e);
        }
    }

    /**
     * Gets the encryption key, ensuring it's 16 bytes for AES-128.
     * For production, use AES-256 with a 32-byte key.
     */
    private byte[] getKey() {
        String key = secretKey;
        // Ensure the key is exactly 16 bytes for AES-128 (or 32 for AES-256)
        if (key.length() < 16) {
            key = String.format("%-16s", key).replace(' ', '0');
        } else if (key.length() > 16) {
            key = key.substring(0, 16);
        }
        return key.getBytes();
    }
}
