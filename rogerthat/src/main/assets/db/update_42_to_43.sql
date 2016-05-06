CREATE TABLE message_flow_run (
    parent_message_key TEXT PRIMARY KEY,
    static_flow_hash TEXT,
    state TEXT
);

ALTER TABLE service_menu_item ADD COLUMN static_flow_hash TEXT;
ALTER TABLE service_menu_item ADD COLUMN hashed_tag TEXT;

CREATE TABLE service_static_flow (
	static_flow_hash TEXT PRIMARY KEY,
	static_flow BLOB NOT NULL
);
