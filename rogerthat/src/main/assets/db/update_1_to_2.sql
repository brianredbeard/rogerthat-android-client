-- Add a priority indication to the Backlog table
ALTER TABLE Backlog
ADD COLUMN has_priority INTEGER DEFAULT +0;
CREATE INDEX ix_Backlog_has_priority ON Backlog (has_priority);

-- Add a last_resend_timestamp to the Backlog table
ALTER TABLE Backlog
ADD COLUMN last_resend_timestamp INTEGER DEFAULT +0;
CREATE INDEX ix_Backlog_last_resend_timestamp ON Backlog (last_resend_timestamp);

-- Add a retention_timeout to the Backlog table
ALTER TABLE Backlog
ADD COLUMN retention_timeout INTEGER DEFAULT +0;
CREATE INDEX ix_Backlog_retention_timeout ON Backlog (retention_timeout);