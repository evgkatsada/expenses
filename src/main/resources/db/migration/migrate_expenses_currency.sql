-- ==============================================================================
-- Migration Script: Add Currency Column and Backfill Existing Records
-- Database: PostgreSQL
-- Table: expenses
-- Default Currency: EUR (Euro)
-- ==============================================================================

BEGIN;

-- Step 1: Add the currency column if it does not already exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'expenses' 
          AND column_name = 'currency'
    ) THEN
        ALTER TABLE expenses ADD COLUMN currency VARCHAR(10);
    END IF;
END $$;

-- Step 2: Backfill existing records that have NULL or blank currency with 'EUR'
UPDATE expenses
SET currency = 'EUR'
WHERE currency IS NULL OR TRIM(currency) = '';

-- Step 3: Set column default to 'EUR' for database-level safety
ALTER TABLE expenses 
ALTER COLUMN currency SET DEFAULT 'EUR';

-- Step 4: Enforce NOT NULL constraint now that all records are populated
ALTER TABLE expenses 
ALTER COLUMN currency SET NOT NULL;

COMMIT;
