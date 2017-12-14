CREATE TABLE user_data (
    email TEXT,
    d_type TEXT,
    d_key TEXT,
    d_value TEXT,
    PRIMARY KEY (email, d_type, d_key)
);