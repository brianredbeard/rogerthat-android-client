BEGIN TRANSACTION;
CREATE TABLE Activity(
	id INTEGER PRIMARY KEY AUTOINCREMENT, 
	timestamp INTEGER, 
	type INTEGER, 
	reference TEXT,
	parameters BLOB,
	friend_reference TEXT);
CREATE TABLE Backlog(
	callid TEXT PRIMARY KEY, 
	calltype INTEGER, 
	timestamp INTEGER, 
	callbody TEXT, has_priority INTEGER DEFAULT +0, last_resend_timestamp INTEGER DEFAULT +0, retention_timeout INTEGER DEFAULT +0, response_handler BLOB, function TEXT NOT NULL DEFAULT "no function recorded", wifi_only INTEGER NOT NULL DEFAULT +0);
CREATE TABLE ConfigurationProvider(
	id INTEGER PRIMARY KEY AUTOINCREMENT, 
	category TEXT, 
	valuetype TEXT, 
	key TEXT UNIQUE, 
	value TEXT);
CREATE TABLE beacon_discovery (
    uuid TEXT NOT NULL,
    name TEXT NOT NULL,
    timestamp INTEGER,
    friend_email TEXT, tag TEXT,
    CONSTRAINT beacon_discovery_primary PRIMARY KEY(uuid,name)
);
CREATE TABLE beacon_region (
    uuid TEXT NOT NULL,
    major INTEGER,
    minor INTEGER,
    CONSTRAINT beacon_region_primary PRIMARY KEY(uuid,major,minor)
);
CREATE TABLE button (
	message TEXT NOT NULL,
	id TEXT NOT NULL,
	caption TEXT NOT NULL,
	"action" TEXT,
	"index" INTEGER, ui_flags INTEGER NOT NULL DEFAULT 0, color TEXT,
	PRIMARY KEY (message, id),
	FOREIGN KEY (message) REFERENCES message ("key")
);
CREATE TABLE current_unprocessed_message_index (
	"index" INTEGER
, last_inbox_open_time INTEGER NOT NULL DEFAULT 0);
INSERT INTO "current_unprocessed_message_index" VALUES(-1,0);
CREATE TABLE embedded_app_translations (
     id TEXT PRIMARY KEY,
     content TEXT
  );
CREATE TABLE friend (
	email TEXT PRIMARY KEY,
	name TEXT,
	avatar_id TEXT,
	share_location INTEGER, 
	shares_location INTEGER,
	avatar BLOB
, existence INTEGER DEFAULT 0, "type" INTEGER NOT NULL DEFAULT 1, email_hash TEXT, description TEXT, description_branding TEXT, poke_description TEXT, menu_branding TEXT, main_phone_number TEXT, share INTEGER NOT NULL DEFAULT 0, generation INTEGER NOT NULL DEFAULT 0, share_image_url TEXT, share_description TEXT, share_caption TEXT, share_link_url TEXT, qualified_identifier TEXT, about_label TEXT, messages_label TEXT, call_label TEXT, share_label TEXT, call_confirmation TEXT, user_data TEXT, app_data TEXT, category_id TEXT, broadcast_flow_hash TEXT, organization_type INTEGER DEFAULT 0, callbacks INTEGER DEFAULT 0, versions TEXT NOT NULL DEFAULT "-1", flags INTEGER DEFAULT 0, profile_data TEXT, content_branding_hash TEXT, actions TEXT);
CREATE TABLE friend_category (
    id TEXT PRIMARY KEY,
    name TEXT,
    avatar BLOB
);
CREATE TABLE friend_invitation_secrets (
    secret TEXT PRIMARY KEY
);
CREATE TABLE friend_set (
	email TEXT NOT NULL
);
CREATE TABLE friend_set_version (
	version INTEGER
);
INSERT INTO "friend_set_version" VALUES(-999);
CREATE TABLE js_embedding (
    name TEXT PRIMARY KEY,
    hash TEXT NOT NULL,
    status INTEGER DEFAULT 0
);
CREATE TABLE last_read_activity_id (
   "id" INTEGER
);
INSERT INTO "last_read_activity_id" VALUES(-1);
CREATE TABLE member_status (
	message TEXT NOT NULL,
	member TEXT NOT NULL,
	received_timestamp INTEGER,
	acked_timestamp INTEGER,
	button TEXT,
	custom_reply TEXT,
	status INTEGER,
	PRIMARY KEY (message, member),
	FOREIGN KEY (message) REFERENCES message ("key"),
	FOREIGN KEY (button) REFERENCES button (id)
);
CREATE TABLE message (
	"key" TEXT PRIMARY KEY,
	parent_key TEXT,
	sender TEXT,
	message TEXT,
	timeout INTEGER,
	"timestamp" INTEGER,
	flags INTEGER,
	needs_my_answer INTEGER,
	branding TEXT,
	sortid INTEGER, dirty INTEGER, recipients TEXT, reply_count INTEGER DEFAULT 0, recipients_status INTEGER DEFAULT -1, alert_flags INTEGER NOT NULL DEFAULT 2, thread_dirty INTEGER NOT NULL DEFAULT 0, last_thread_message TEXT, show_in_message_list INTEGER NOT NULL DEFAULT 0, day INTEGER NOT NULL DEFAULT 0, form TEXT, dismiss_button_ui_flags INTEGER NOT NULL DEFAULT 0, thread_needs_my_answer INTEGER NOT NULL DEFAULT 0, last_updated_on INTEGER NOT NULL DEFAULT 0, existence INTEGER NOT NULL DEFAULT 1, thread_show_in_list INTEGER NOT NULL DEFAULT 0, broadcast_type TEXT, thread_avatar_hash TEXT DEFAULT NULL, thread_background_color TEXT DEFAULT NULL, thread_text_color TEXT DEFAULT NULL, priority INTEGER DEFAULT 1, default_priority INTEGER DEFAULT 1, default_sticky INTEGER DEFAULT 0,
	FOREIGN KEY (parent_key) REFERENCES message ("key")
);
CREATE TABLE message_attachment (
    message TEXT NOT NULL,
    content_type TEXT NOT NULL,
    download_url TEXT NOT NULL,
    size INTEGER NOT NULL,
    name TEXT NOT NULL,
    FOREIGN KEY (message) REFERENCES message(key)
);
CREATE TABLE message_flow_run (
    parent_message_key TEXT PRIMARY KEY,
    static_flow_hash TEXT,
    state TEXT
);
CREATE TABLE my_identity (
	email TEXT NOT NULL,
	name TEXT,
	avatar BLOB
, qr_code BLOB, short_link TEXT, qualified_identifier TEXT, avatar_id INTEGER NOT NULL DEFAULT -1, birthdate INTEGER DEFAULT NULL, gender INTEGER DEFAULT NULL, profile_data TEXT);
INSERT INTO "my_identity" VALUES('dummy',NULL,NULL,NULL,NULL,NULL,-1,NULL,NULL,NULL);
CREATE TABLE my_qr_codes (
    content TEXT,
    name TEXT,
    PRIMARY KEY (content, name)
);
CREATE TABLE news (
    id INTEGER PRIMARY KEY,
    "timestamp" INTEGER,
    sender_email TEXT,
    sender_name TEXT,
    sender_avatar_id INTEGER,
    title TEXT,
    message TEXT,
    image_url TEXT,
    broadcast_type TEXT,
    reach INTEGER DEFAULT 0,
    qr_code_content TEXT,
    qr_code_caption TEXT,
    version INTEGER,
    flags INTEGER,
    type INTEGER,
    "read" INTEGER DEFAULT 0,
    pinned INTEGER DEFAULT 0,
    rogered INTEGER DEFAULT 0,
    disabled INTEGER DEFAULT 0,
    sort_timestamp INTEGER UNIQUE,
    sort_priority INTEGER,
    is_partial INTEGER,
    reindex INTEGER DEFAULT 0,
    sort_key INTEGER
);
CREATE TABLE news_buttons (
    news_id INTEGER NOT NULL,
    id TEXT NOT NULL,
    caption TEXT NOT NULL,
    "action" TEXT,
    flow_params TEXT,
    "index" INTEGER,
    PRIMARY KEY (news_id, id),
    FOREIGN KEY (news_id) REFERENCES news ("id")
);
CREATE TABLE news_rogered_users (
    news_id INTEGER NOT NULL,
    friend TEXT NOT NULL,
    PRIMARY KEY (news_id, friend),
    FOREIGN KEY (news_id) REFERENCES news ("id")
);
CREATE TABLE payment_asset (
    provider_id TEXT,
    id TEXT,
    type TEXT,
    name TEXT,
    currency TEXT,
    available_balance TEXT,
    total_balance TEXT,
    verified INTEGER DEFAULT 0,
    enabled INTEGER DEFAULT 0,
    has_balance INTEGER DEFAULT 0,
    has_transactions INTEGER DEFAULT 0,
    required_action TEXT,
    PRIMARY KEY (provider_id, id)
 );
CREATE TABLE payment_provider (
    id TEXT PRIMARY KEY,
    name TEXT,
    logo_url TEXT,
    version INTEGER DEFAULT 0,
    description TEXT,
    oauth_authorize_url TEXT,
    black_white_logo TEXT,
    background_color TEXT,
    text_color TEXT,
    button_color TEXT,
    currencies TEXT,
    asset_types TEXT
);
CREATE TABLE pending_invitation (
	invitee TEXT PRIMARY KEY
);
CREATE TABLE recipients_group (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    avatar_hash TEXT,
    avatar BLOB
);
CREATE TABLE recipients_group_member (
    group_id TEXT NOT NULL,
    email TEXT NOT NULL,
    CONSTRAINT recipients_group_member_primary PRIMARY KEY(group_id,email)
);
CREATE TABLE requested_conversation (
	thread_key TEXT PRIMARY KEY NOT NULL
);
CREATE TABLE security_key (
    type TEXT,
    algorithm TEXT,
    name TEXT,
    indexx TEXT,
    data TEXT,
    PRIMARY KEY (type, algorithm, name, indexx)
 );
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
CREATE TABLE service_menu_icon (
	icon_hash TEXT PRIMARY KEY,
	icon BLOB NOT NULL
);
CREATE TABLE service_menu_item (
    friend TEXT NOT NULL,
    x INTEGER NOT NULL,
    y INTEGER NOT NULL,
    z INTEGER NOT NULL,
    label TEXT NOT NULL,
    icon_hash TEXT NOT NULL,
    screen_branding TEXT, static_flow_hash TEXT, hashed_tag TEXT, requires_wifi INTEGER NOT NULL DEFAULT 0, run_in_background INTEGER NOT NULL DEFAULT 1, "action" INTEGER NOT NULL DEFAULT 0, "icon_name" TEXT, "icon_color" TEXT, url TEXT, external INT, fall_through INTEGER DEFAULT 0,
    PRIMARY KEY (friend, x, y, z),
    FOREIGN KEY (friend) REFERENCES friend(email) 
);
CREATE TABLE service_static_flow (
	static_flow_hash TEXT PRIMARY KEY,
	static_flow BLOB NOT NULL
);
DELETE FROM "sqlite_sequence";
CREATE TABLE thread_avatar (
    avatar_hash TEXT PRIMARY KEY,
    avatar BLOB
);
CREATE TABLE unprocessed_messages(
	id TEXT PRIMARY KEY, 
	timestamp INTEGER, 
	message BLOB);
CREATE TABLE user_data (
    email TEXT,
    d_type TEXT,
    d_key TEXT,
    d_value TEXT,
    PRIMARY KEY (email, d_type, d_key)
);
CREATE INDEX ix_Backlog_timestamp ON Backlog(timestamp);
CREATE INDEX ix_ConfigurationProvider_key ON ConfigurationProvider(key);
CREATE INDEX ix_unprocessed_messages_timestamp ON unprocessed_messages(timestamp);
CREATE INDEX ix_Backlog_has_priority ON Backlog (has_priority);
CREATE INDEX ix_Backlog_last_resend_timestamp ON Backlog (last_resend_timestamp);
CREATE INDEX ix_Backlog_retention_timeout ON Backlog (retention_timeout);
CREATE INDEX ix_friend_name ON friend (name);
CREATE INDEX ix_button_message_order ON button (message, "index");
CREATE INDEX ix_message_by_index ON message (sortid DESC, "timestamp");
CREATE INDEX ix_needs_my_answer ON message (needs_my_answer);
CREATE INDEX ix_message_dirty ON message (dirty);
CREATE INDEX ix_backlog_single_call ON Backlog (calltype, last_resend_timestamp, function);
CREATE INDEX ix_message_parent_key ON message (parent_key);
CREATE INDEX ix_activity_reference ON activity (reference);
CREATE INDEX ix_friend_shares_location ON friend (shares_location);
CREATE INDEX ix_friend_type ON friend ("type");
CREATE INDEX ix_activity_timestamp ON activity (timestamp);
CREATE INDEX ix_friend_email_hash ON friend ("email_hash");
CREATE INDEX ix_message_show_in_list ON message ("show_in_message_list");
CREATE INDEX ix_message_day ON message ("day");
CREATE INDEX ix_friend_category_name ON friend_category (name);
CREATE INDEX ix_service_api_calls_by_item_and_status ON service_api_calls (service, item, status);
CREATE INDEX ix_service_api_calls_by_id ON service_api_calls (service, id);
CREATE INDEX ix_message_attachment_message ON message_attachment (message);
CREATE UNIQUE INDEX ix_friend_set_email ON friend_set(email);
CREATE TRIGGER tr_message_show_in_list_after_insert AFTER INSERT ON message FOR EACH ROW
BEGIN
    UPDATE message
    SET show_in_message_list =
        CASE
            WHEN NEW.existence = 0 THEN 0
            WHEN NEW.key != last_thread_message THEN 0
            WHEN NEW.sender = "dashboard@rogerth.at" THEN 1
            WHEN NEW.flags & 512 != 0 THEN 1
            WHEN (SELECT type FROM friend WHERE NEW.sender = email) = 1 THEN 1
            WHEN (SELECT type FROM friend WHERE NEW.sender = email) = 2 AND (NEW.DIRTY = 1 OR NEW.thread_show_in_list == 1) THEN 1
            WHEN (SELECT type FROM friend WHERE NEW.sender = email) = 2 THEN 2
            WHEN (SELECT email FROM my_identity) = NEW.sender THEN 1
            ELSE 0
        END
    WHERE key = NEW.key; END;
CREATE TRIGGER tr_message_show_in_list_after_update AFTER UPDATE OF needs_my_answer, dirty, last_thread_message, existence, thread_show_in_list ON message FOR EACH ROW
BEGIN
    UPDATE message
    SET show_in_message_list =
        CASE
            WHEN NEW.existence = 0 THEN 0
            WHEN NEW.key != last_thread_message THEN 0
            WHEN NEW.sender = "dashboard@rogerth.at" THEN 1
            WHEN NEW.flags & 512 != 0 THEN 1
            WHEN (SELECT type FROM friend WHERE NEW.sender = email) = 1 THEN 1
            WHEN (SELECT type FROM friend WHERE NEW.sender = email) = 2 AND (NEW.DIRTY = 1 OR NEW.thread_show_in_list == 1) THEN 1
            WHEN (SELECT type FROM friend WHERE NEW.sender = email) = 2 THEN 2
            WHEN (SELECT email FROM my_identity) = NEW.sender THEN 1
            ELSE 0
        END
    WHERE key = NEW.key; END;
CREATE TRIGGER tr_message_thread_dirtyness_after_insert AFTER INSERT ON message FOR EACH ROW 
BEGIN 
    UPDATE message 
    SET thread_dirty = CASE WHEN ( SELECT sum(m1.dirty)
                                   FROM message m1 INNER JOIN message m2 ON m1.sortid = m2.sortid 
                                   WHERE m2.key = NEW.key AND m2.existence = 1 ) > 0 THEN 1 ELSE 0 
                            END,
        thread_needs_my_answer = CASE WHEN ( SELECT sum(m1.needs_my_answer) 
                                             FROM message m1 INNER JOIN message m2 ON m1.sortid = m2.sortid 
                                             WHERE m2.key = NEW.key AND m2.existence = 1 ) > 0 THEN 1 ELSE 0 
                            END
    WHERE sortid = NEW.sortid; END;
CREATE TRIGGER tr_message_thread_dirtyness_after_update AFTER UPDATE OF needs_my_answer, dirty, existence ON message FOR EACH ROW 
BEGIN 
    UPDATE message 
    SET thread_dirty = CASE WHEN ( SELECT sum(m1.dirty)
                                   FROM message m1 INNER JOIN message m2 ON m1.sortid = m2.sortid 
                                   WHERE m2.key = NEW.key AND m2.existence = 1 ) > 0 THEN 1 ELSE 0 
                            END,
        thread_needs_my_answer = CASE WHEN ( SELECT sum(m1.needs_my_answer) 
                                             FROM message m1 INNER JOIN message m2 ON m1.sortid = m2.sortid 
                                             WHERE m2.key = NEW.key AND m2.existence = 1 ) > 0 THEN 1 ELSE 0 
                            END
    WHERE sortid = NEW.sortid; END;
CREATE INDEX ix_news_reindex ON news ("reindex");
CREATE INDEX ix_news_sort_order ON news ("sort_key", "id");
CREATE INDEX ix_news_button_order ON news_buttons (news_id, "index");
CREATE INDEX ix_news_read_since ON news ("read", "timestamp", "sender_email", "broadcast_type");
CREATE INDEX ix_news_sort_key ON news ("sort_key");
CREATE INDEX ix_news_pinned_sort_key ON news ("pinned", "sort_key");
CREATE INDEX ix_mq_qr_codes_name ON my_qr_codes ("name");
CREATE INDEX ix_payment_provider_name ON payment_provider ("name");
CREATE INDEX ix_payment_asset_name ON payment_asset ("name");
COMMIT;
