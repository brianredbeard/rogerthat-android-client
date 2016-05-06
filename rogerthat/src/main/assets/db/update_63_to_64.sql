CREATE TABLE thread_avatar (
    avatar_hash TEXT PRIMARY KEY,
    avatar BLOB
);

ALTER TABLE message ADD COLUMN thread_avatar_hash TEXT DEFAULT NULL;
ALTER TABLE message ADD COLUMN thread_background_color TEXT DEFAULT NULL;
ALTER TABLE message ADD COLUMN thread_text_color TEXT DEFAULT NULL;