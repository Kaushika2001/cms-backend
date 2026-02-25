-- Fix: Make LastUpdatedUser nullable if it exists, or add it with a default value
-- This migration handles the case where LastUpdatedUser was manually added to the database

-- Check if column exists and make it nullable
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'card' 
        AND column_name = 'lastupdateduser'
    ) THEN
        ALTER TABLE Card ALTER COLUMN LastUpdatedUser DROP NOT NULL;
        ALTER TABLE Card ALTER COLUMN LastUpdatedUser SET DEFAULT 'system';
        
        -- Update existing NULL values
        UPDATE Card SET LastUpdatedUser = 'system' WHERE LastUpdatedUser IS NULL;
    END IF;
END $$;
