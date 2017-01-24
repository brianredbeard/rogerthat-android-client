CREATE INDEX ix_news_read_since ON news ("read", "timestamp", "sender_email", "broadcast_type");
CREATE INDEX ix_news_sort_key ON news ("sort_key");
CREATE INDEX ix_news_pinned_sort_key ON news ("pinned", "sort_key");