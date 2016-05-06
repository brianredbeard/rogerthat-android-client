ALTER TABLE service_menu_item ADD COLUMN requires_wifi INTEGER NOT NULL DEFAULT 0;
ALTER TABLE service_menu_item ADD COLUMN run_in_background INTEGER NOT NULL DEFAULT 1;