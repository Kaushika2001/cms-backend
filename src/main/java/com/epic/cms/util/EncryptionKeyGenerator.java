package com.epic.cms.util;

/**
 * Utility to generate secure AES-256 encryption keys.
 * 
 * HOW TO USE:
 * 1. Run this class as a Java application
 * 2. Copy the generated keys to your environment variables or secrets manager
 * 3. NEVER commit these keys to version control
 * 
 * PRODUCTION DEPLOYMENT:
 * - Generate separate keys for each environment (dev, staging, production)
 * - Store keys in a secure secrets manager (AWS Secrets Manager, Azure Key Vault, HashiCorp Vault)
 * - Rotate keys every 90 days
 * - Use different keys for transport and storage layers
 * 
 * ENVIRONMENT VARIABLES:
 * Set these in your deployment environment:
 * - TRANSPORT_ENCRYPTION_KEY: Key for frontend-backend communication (share with frontend)
 * - STORAGE_ENCRYPTION_KEY: Key for database encryption (backend only - NEVER expose)
 */
public class EncryptionKeyGenerator {
    
    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("AES-256 ENCRYPTION KEY GENERATOR");
        System.out.println("=".repeat(80));
        System.out.println();
        
        // Generate keys
        String transportKey = EncryptionUtil.generateKey();
        String storageKey = EncryptionUtil.generateKey();
        
        System.out.println("GENERATED ENCRYPTION KEYS (AES-256)");
        System.out.println("-".repeat(80));
        System.out.println();
        
        // Transport Key
        System.out.println("1. TRANSPORT KEY (Frontend ↔ Backend)");
        System.out.println("   Purpose: Encrypt data during API transmission");
        System.out.println("   Share with: Frontend developers (add to .env file)");
        System.out.println();
        System.out.println("   Key: " + transportKey);
        System.out.println();
        System.out.println("   Frontend (.env file):");
        System.out.println("   VITE_ENCRYPTION_KEY=" + transportKey);
        System.out.println();
        System.out.println("   Backend (application.yaml or environment variable):");
        System.out.println("   TRANSPORT_ENCRYPTION_KEY=" + transportKey);
        System.out.println();
        System.out.println("-".repeat(80));
        System.out.println();
        
        // Storage Key
        System.out.println("2. STORAGE KEY (Backend ↔ Database)");
        System.out.println("   Purpose: Encrypt sensitive data at rest (PCI DSS compliance)");
        System.out.println("   Share with: NO ONE (backend only, store in secrets manager)");
        System.out.println();
        System.out.println("   Key: " + storageKey);
        System.out.println();
        System.out.println("   Backend (application.yaml or environment variable):");
        System.out.println("   STORAGE_ENCRYPTION_KEY=" + storageKey);
        System.out.println();
        System.out.println("-".repeat(80));
        System.out.println();
        
        // Validation
        System.out.println("KEY VALIDATION");
        System.out.println("-".repeat(80));
        System.out.println();
        System.out.println("✓ Transport Key Valid: " + EncryptionUtil.validateKey(transportKey));
        System.out.println("✓ Storage Key Valid: " + EncryptionUtil.validateKey(storageKey));
        System.out.println("✓ Keys are unique: " + !transportKey.equals(storageKey));
        System.out.println();
        
        // Test encryption
        try {
            String testData = "1234567890123456";
            String encrypted = EncryptionUtil.encrypt(testData, transportKey);
            String decrypted = EncryptionUtil.decrypt(encrypted, transportKey);
            System.out.println("✓ Encryption test passed: " + testData.equals(decrypted));
        } catch (Exception e) {
            System.out.println("✗ Encryption test failed: " + e.getMessage());
        }
        System.out.println();
        
        // Security reminders
        System.out.println("=".repeat(80));
        System.out.println("SECURITY REMINDERS");
        System.out.println("=".repeat(80));
        System.out.println();
        System.out.println("⚠ NEVER commit these keys to version control (Git, SVN, etc.)");
        System.out.println("⚠ Store production keys in a secrets manager:");
        System.out.println("  - AWS Secrets Manager");
        System.out.println("  - Azure Key Vault");
        System.out.println("  - HashiCorp Vault");
        System.out.println("  - Google Cloud Secret Manager");
        System.out.println();
        System.out.println("⚠ Generate DIFFERENT keys for each environment:");
        System.out.println("  - Development: Use generated keys for testing");
        System.out.println("  - Staging: Generate separate keys");
        System.out.println("  - Production: Generate separate keys (highest security)");
        System.out.println();
        System.out.println("⚠ Rotate keys regularly:");
        System.out.println("  - Every 90 days (recommended)");
        System.out.println("  - After suspected compromise");
        System.out.println("  - When employee with key access leaves");
        System.out.println();
        System.out.println("⚠ Transport key must match between frontend and backend");
        System.out.println("⚠ Storage key is BACKEND ONLY - never expose to frontend/client");
        System.out.println();
        System.out.println("=".repeat(80));
        System.out.println();
        
        // Alternative: OpenSSL command
        System.out.println("ALTERNATIVE: Generate keys using OpenSSL");
        System.out.println("-".repeat(80));
        System.out.println();
        System.out.println("Run these commands in your terminal:");
        System.out.println();
        System.out.println("# Generate transport key");
        System.out.println("openssl rand -base64 32");
        System.out.println();
        System.out.println("# Generate storage key");
        System.out.println("openssl rand -base64 32");
        System.out.println();
        System.out.println("=".repeat(80));
    }
}
