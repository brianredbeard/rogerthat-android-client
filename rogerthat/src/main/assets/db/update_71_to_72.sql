CREATE TABLE news (
    id INTEGER PRIMARY KEY,
    "timestamp" INTEGER,
    sender_email TEXT,
    sender_name TEXT,
    sender_avatar_id INTEGER,
    title TEXT,
    message TEXT,
    image_url TEXT,
    label TEXT,
    reach INTEGER,
    qr_code_content TEXT,
    qr_code_caption TEXT,
    version INTEGER
);

CREATE TABLE news_buttons (
    news_id INTEGER NOT NULL,
    id TEXT NOT NULL,
    caption TEXT NOT NULL,
    "action" TEXT,
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