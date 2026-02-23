-- V5__update_cardrequest_for_encryption.sql
-- Migration: Update CardRequest.CardNumber column to support encrypted card numbers

-- The CardRequest table stores card numbers that are encrypted in the Card table
-- We need to increase the column size to match the Card table (VARCHAR(500))
-- to handle encrypted card numbers in the format: {iv}.{ciphertext}

-- 1. Drop the foreign key constraint temporarily
ALTER TABLE CardRequest
DROP CONSTRAINT IF EXISTS fk_request_card;

-- 2. Increase CardNumber column size to support encrypted data
ALTER TABLE CardRequest 
ALTER COLUMN CardNumber TYPE VARCHAR(500);

-- 3. Recreate the foreign key constraint
ALTER TABLE CardRequest
ADD CONSTRAINT fk_request_card 
FOREIGN KEY (CardNumber) REFERENCES Card(CardNumber) ON DELETE CASCADE;

-- 4. Add comment for documentation
COMMENT ON COLUMN CardRequest.CardNumber IS 
'Encrypted card number reference matching Card.CardNumber. Format: {iv}.{ciphertext} where both parts are base64 encoded.';

-- 5. Drop and recreate the index for better performance with larger strings
DROP INDEX IF EXISTS idx_request_card;
CREATE INDEX idx_request_card ON CardRequest(CardNumber);

-- Note: This migration allows CardRequest to store encrypted card numbers
-- that match the encrypted format in the Card table
