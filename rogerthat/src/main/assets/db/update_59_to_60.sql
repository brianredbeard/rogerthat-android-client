CREATE TABLE beacon_region (
    uuid TEXT NOT NULL,
    major INTEGER,
    minor INTEGER,
    CONSTRAINT beacon_region_primary PRIMARY KEY(uuid,major,minor)
);