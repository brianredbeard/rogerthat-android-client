-- Create table friend
CREATE TABLE friend (
	email TEXT PRIMARY KEY,
	name TEXT,
	avatar_id TEXT,
	share_location INTEGER, 
	shares_location INTEGER,
	avatar BLOB
);
CREATE INDEX ix_friend_name ON friend (name);

-- Create table friend_generation
CREATE TABLE friend_generation (
	generation INTEGER
);
INSERT INTO friend_generation (generation) VALUES (-999);

