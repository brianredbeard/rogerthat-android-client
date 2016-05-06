-- Create table message
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
	sortid INTEGER,
	FOREIGN KEY (parent_key) REFERENCES message ("key")
);

-- Create table button
CREATE TABLE button (
	message TEXT NOT NULL,
	id TEXT NOT NULL,
	caption TEXT NOT NULL,
	"action" TEXT,
	"index" INTEGER,
	PRIMARY KEY (message, id),
	FOREIGN KEY (message) REFERENCES message ("key")
);
CREATE INDEX ix_button_message_order ON button (message, "index");

-- Create table member_status
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

-- Create table current_unprocessed_message_index
CREATE TABLE current_unprocessed_message_index (
	"index" INTEGER
);

INSERT INTO current_unprocessed_message_index ("index") VALUES (-1);

-- Create table my_identity
CREATE TABLE my_identity (
	email TEXT NOT NULL,
	name TEXT,
	avatar BLOB
);

INSERT INTO my_identity (email, name, avatar) VALUES ('dummy', NULL, NULL);