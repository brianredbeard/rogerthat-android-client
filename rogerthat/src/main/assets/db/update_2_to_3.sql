-- Add requestHandler blob to backlog table
ALTER TABLE Backlog
ADD COLUMN response_handler BLOB;

-- Drop table RequestMetaPersisterImpl
DROP TABLE RequestMetaPersisterImpl;

