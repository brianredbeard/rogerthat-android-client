CREATE TABLE recipients_group (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    avatar_hash TEXT,
    avatar BLOB
);

CREATE TABLE recipients_group_member (
    group_id TEXT NOT NULL,
    email TEXT NOT NULL,
    CONSTRAINT recipients_group_member_primary PRIMARY KEY(group_id,email)
);