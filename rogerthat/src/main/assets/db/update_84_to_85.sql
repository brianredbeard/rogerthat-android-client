CREATE TABLE embedded_app_url_regex (
    name TEXT,
    url_regex TEXT,
    PRIMARY KEY (name, url_regex)
);

 CREATE INDEX embedded_app_url_regex_name ON embedded_app_url_regex ("name");