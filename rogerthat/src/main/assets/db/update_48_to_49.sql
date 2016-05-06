CREATE TABLE service_api_calls (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    service TEXT,
    item TEXT,
    method TEXT,
    tag TEXT,
    result TEXT,
    error TEXT,
    status INTEGER
);

CREATE INDEX ix_service_api_calls_by_item_and_status ON service_api_calls (service, item, status);
CREATE INDEX ix_service_api_calls_by_id ON service_api_calls (service, id);
