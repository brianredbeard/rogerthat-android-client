CREATE TABLE beacon_discovery (
    uuid TEXT NOT NULL,
    name TEXT NOT NULL,
    timestamp INTEGER,
    friend_email TEXT,
    CONSTRAINT beacon_discovery_primary PRIMARY KEY(uuid,name)
);