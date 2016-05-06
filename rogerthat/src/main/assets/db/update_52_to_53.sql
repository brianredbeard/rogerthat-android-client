CREATE TABLE message_attachment (
    message TEXT NOT NULL,
    content_type TEXT NOT NULL,
    download_url TEXT NOT NULL,
    size INTEGER NOT NULL,
    name TEXT NOT NULL,
    FOREIGN KEY (message) REFERENCES message(key)
);

CREATE INDEX ix_message_attachment_message ON message_attachment (message);
