CREATE TABLE service_menu_item (
    friend TEXT NOT NULL,
    x INTEGER NOT NULL,
    y INTEGER NOT NULL,
    z INTEGER NOT NULL,
    label TEXT NOT NULL,
    icon_hash TEXT NOT NULL,
    screen_branding TEXT,
    PRIMARY KEY (friend, x, y, z),
    FOREIGN KEY (friend) REFERENCES friend(email) 
);

CREATE TABLE service_menu_icon (
	icon_hash TEXT PRIMARY KEY,
	icon BLOB NOT NULL
);

ALTER TABLE friend ADD COLUMN menu_branding TEXT;
ALTER TABLE friend ADD COLUMN main_phone_number TEXT;
ALTER TABLE friend ADD COLUMN share INTEGER NOT NULL DEFAULT 0;
ALTER TABLE friend ADD COLUMN generation INTEGER NOT NULL DEFAULT 0;