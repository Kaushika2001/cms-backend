-- V3__update_card_number_for_encryption.sql
-- Migration: Update card_number column to support encrypted data

-- Increase column size to store encrypted data (AES-256-GCM with IV and ciphertext)
-- Format: {iv}.{ciphertext} where both are base64 encoded
-- Estimated size: 12 bytes IV + authentication tag + padding -> ~500 chars is safe
ALTER TABLE Card 
ALTER COLUMN CardNumber TYPE VARCHAR(500);

-- Remove the length check constraint as encrypted data will be longer
ALTER TABLE Card
DROP CONSTRAINT IF EXISTS chk_card_number;

-- Add comment for documentation
COMMENT ON COLUMN Card.CardNumber IS 
'AES-256-GCM encrypted card number. Format: {iv}.{ciphertext} where both parts are base64 encoded. Uses storage layer encryption key.';

-- Add index on CardNumber for faster lookups (if not already exists)
-- Note: Encrypted data won't benefit from pattern matching, but exact match will work
CREATE INDEX IF NOT EXISTS idx_card_number ON Card(CardNumber);

-- Note: Existing unencrypted card numbers will need to be migrated separately
-- This migration only updates the schema to support encrypted data
