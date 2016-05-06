ALTER TABLE friend ADD COLUMN "type" INTEGER NOT NULL DEFAULT 1;

CREATE INDEX ix_friend_type ON friend ("type");