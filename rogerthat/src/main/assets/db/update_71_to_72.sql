CREATE TABLE news (
    id INTEGER PRIMARY KEY,
    "timestamp" INTEGER,
    sender_email TEXT,
    sender_name TEXT,
    sender_avatar_id INTEGER,
    title TEXT,
    message TEXT,
    image_url TEXT,
    broadcast_type TEXT,
    reach INTEGER DEFAULT 0,
    qr_code_content TEXT,
    qr_code_caption TEXT,
    version INTEGER,
    flags INTEGER,
    type INTEGER,
    "read" INTEGER DEFAULT 0,
    pinned INTEGER DEFAULT 0,
    rogered INTEGER DEFAULT 0,
    disabled INTEGER DEFAULT 0,
    sort_timestamp INTEGER UNIQUE,
    sort_priority INTEGER,
    is_partial INTEGER,
    reindex INTEGER DEFAULT 0,
    sort_key INTEGER
);

CREATE INDEX ix_news_reindex ON news ("reindex");
CREATE INDEX ix_news_sort_order ON news ("sort_key", "id");

CREATE TABLE news_buttons (
    news_id INTEGER NOT NULL,
    id TEXT NOT NULL,
    caption TEXT NOT NULL,
    "action" TEXT,
    flow_params TEXT,
    "index" INTEGER,
    PRIMARY KEY (news_id, id),
    FOREIGN KEY (news_id) REFERENCES news ("id")
);

CREATE INDEX ix_news_button_order ON news_buttons (news_id, "index");

CREATE TABLE news_rogered_users (
    news_id INTEGER NOT NULL,
    friend TEXT NOT NULL,
    PRIMARY KEY (news_id, friend),
    FOREIGN KEY (news_id) REFERENCES news ("id")
);

ALTER TABLE friend ADD COLUMN actions TEXT;
ALTER TABLE service_menu_item ADD COLUMN "action" INTEGER NOT NULL DEFAULT 0;
ALTER TABLE service_menu_item ADD COLUMN "icon_name" TEXT;
ALTER TABLE service_menu_item ADD COLUMN "icon_color" TEXT;