-- Add function field
ALTER TABLE Backlog
ADD COLUMN function TEXT NOT NULL DEFAULT "no function recorded";

-- Add index for single call lookups
CREATE INDEX ix_backlog_single_call ON Backlog (calltype, last_resend_timestamp, function);
