package com.epic.cms.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for receiving encrypted payloads from frontend.
 * Used for secure transport layer encryption between frontend and backend.
 * 
 * The frontend encrypts sensitive data (like CreateCardDTO) with the transport key
 * and sends it in this wrapper format.
 */
public class EncryptedPayload {
    
    @NotBlank(message = "Encrypted data is required")
    private String encryptedData;
    
    private String timestamp;
    
    /**
     * Default constructor.
     */
    public EncryptedPayload() {
    }
    
    /**
     * Constructor with all fields.
     * 
     * @param encryptedData Base64 encoded encrypted data in format {iv}.{ciphertext}
     * @param timestamp ISO-8601 formatted timestamp from frontend
     */
    public EncryptedPayload(String encryptedData, String timestamp) {
        this.encryptedData = encryptedData;
        this.timestamp = timestamp;
    }
    
    /**
     * Get the encrypted data.
     * 
     * @return Base64 encoded encrypted data in format {iv}.{ciphertext}
     */
    public String getEncryptedData() {
        return encryptedData;
    }
    
    /**
     * Set the encrypted data.
     * 
     * @param encryptedData Base64 encoded encrypted data in format {iv}.{ciphertext}
     */
    public void setEncryptedData(String encryptedData) {
        this.encryptedData = encryptedData;
    }
    
    /**
     * Get the timestamp.
     * 
     * @return ISO-8601 formatted timestamp
     */
    public String getTimestamp() {
        return timestamp;
    }
    
    /**
     * Set the timestamp.
     * 
     * @param timestamp ISO-8601 formatted timestamp
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "EncryptedPayload{" +
                "timestamp='" + timestamp + '\'' +
                ", encryptedDataLength=" + (encryptedData != null ? encryptedData.length() : 0) +
                '}';
    }
}
