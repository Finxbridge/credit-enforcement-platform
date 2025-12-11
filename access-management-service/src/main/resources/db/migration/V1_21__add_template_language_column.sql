-- Add language column to templates table for multilingual support
-- Language values: TELUGU, HINDI, ENGLISH
-- Short codes for communication service: Te, Hi, En_US

ALTER TABLE templates ADD COLUMN IF NOT EXISTS language VARCHAR(20);

-- Add comment for documentation
COMMENT ON COLUMN templates.language IS 'Template language: TELUGU (Te), HINDI (Hi), ENGLISH (En_US)';
