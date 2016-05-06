-- Add alert_flags field
ALTER TABLE message
ADD COLUMN alert_flags INTEGER NOT NULL DEFAULT 2;
