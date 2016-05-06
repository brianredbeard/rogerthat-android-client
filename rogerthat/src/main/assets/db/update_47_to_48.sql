CREATE TABLE friend_category (
    id TEXT PRIMARY KEY,
    name TEXT,
    avatar BLOB
);

CREATE INDEX ix_friend_category_name ON friend_category (name);

ALTER TABLE friend ADD COLUMN category_id TEXT;
