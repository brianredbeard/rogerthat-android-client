CREATE INDEX ix_message_by_index ON message (sortid DESC, "timestamp");
CREATE INDEX ix_needs_my_answer ON message (needs_my_answer);
