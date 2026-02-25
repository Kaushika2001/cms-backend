-- V7__fix_lastupdateduser_foreign_key.sql
-- Migration: Fix LastUpdatedUser foreign key constraint issue

-- Check if LastUpdatedUser column exists and has a foreign key constraint
-- Drop the foreign key constraint if it exists
ALTER TABLE Card 
DROP CONSTRAINT IF EXISTS fk_card_last_updated_user;

-- Make LastUpdatedUser nullable to allow cards without user tracking
ALTER TABLE Card 
ALTER COLUMN LastUpdatedUser DROP NOT NULL;

-- Set default value to NULL instead of 'system'
ALTER TABLE Card 
ALTER COLUMN LastUpdatedUser SET DEFAULT NULL;

-- Update existing rows that have 'system' as LastUpdatedUser to NULL
UPDATE Card 
SET LastUpdatedUser = NULL 
WHERE LastUpdatedUser = 'system';

-- Add comment for documentation
COMMENT ON COLUMN Card.LastUpdatedUser IS 
'Username of the user who last updated this card. Nullable field, defaults to NULL for system operations.';

-- If you want to add the foreign key back later after creating a users table:
-- ALTER TABLE Card 
-- ADD CONSTRAINT fk_card_last_updated_user 
-- FOREIGN KEY (LastUpdatedUser) REFERENCES users(username) ON DELETE SET NULL;
