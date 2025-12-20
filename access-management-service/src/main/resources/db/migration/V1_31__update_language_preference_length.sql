-- Update language_preference column length to support full language names
-- Allows both codes (te, hi) and full names (Telugu, Hindi, etc.)

ALTER TABLE customers
ALTER COLUMN language_preference TYPE VARCHAR(50);

-- Add comment to clarify usage
COMMENT ON COLUMN customers.language_preference IS 'Language preference - can be ISO code (te, hi, en) or full name (Telugu, Hindi, English)';
