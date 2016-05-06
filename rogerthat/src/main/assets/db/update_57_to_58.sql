ALTER TABLE friend ADD COLUMN versions TEXT NOT NULL DEFAULT "-1";

CREATE TABLE friend_set (
	email TEXT NOT NULL
);
CREATE UNIQUE INDEX ix_friend_set_email ON friend_set(email);

DROP TABLE friend_generation;

CREATE TABLE friend_set_version (
	version INTEGER
);
INSERT INTO friend_set_version (version) VALUES (-999);
