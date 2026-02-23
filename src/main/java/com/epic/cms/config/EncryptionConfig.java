package com.epic.cms.config;

import com.epic.cms.util.LoggerUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for managing encryption keys.
 * Handles both transport layer and storage layer encryption keys.
 */
@Configuration
public class EncryptionConfig {
    
    @Value("${encryption.transport.key}")
    private String transportKey;
    
    @Value("${encryption.storage.key}")
    private String storageKey;
    
    /**
     * Get the transport encryption key (shared with frontend).
     * Used for encrypting data during API transmission.
     * 
     * @return Base64 encoded transport key
     * @throws IllegalStateException if key is not configured
     */
    public String getTransportKey() {
        if (transportKey == null || transportKey.trim().isEmpty()) {
            LoggerUtil.error(EncryptionConfig.class, "Transport encryption key is not configured");
            throw new IllegalStateException("Transport encryption key is not configured");
        }
        return transportKey;
    }
    
    /**
     * Get the storage encryption key (backend only).
     * Used for encrypting sensitive data at rest in the database.
     * 
     * @return Base64 encoded storage key
     * @throws IllegalStateException if key is not configured
     */
    public String getStorageKey() {
        if (storageKey == null || storageKey.trim().isEmpty()) {
            LoggerUtil.error(EncryptionConfig.class, "Storage encryption key is not configured");
            throw new IllegalStateException("Storage encryption key is not configured");
        }
        return storageKey;
    }
}
