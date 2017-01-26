CREATE TABLE my_qr_codes (
    content TEXT,
    name TEXT,
    PRIMARY KEY (content, name)
);

CREATE INDEX ix_mq_qr_codes_name ON my_qr_codes ("name");
