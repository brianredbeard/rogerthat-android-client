-- Add index on dirty field
CREATE INDEX ix_message_dirty ON message (dirty);

-- Add recipients field
ALTER TABLE message
ADD COLUMN recipients TEXT;
