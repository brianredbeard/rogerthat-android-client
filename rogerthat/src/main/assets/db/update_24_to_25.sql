ALTER TABLE message ADD COLUMN thread_dirty INTEGER NOT NULL DEFAULT 0;
UPDATE message 
SET thread_dirty = 1 
WHERE ( 
	SELECT sum(m2.dirty) + sum(m2.needs_my_answer) 
	FROM message m2 
	WHERE m2.sortid = message.sortid 
	) > 0;

ALTER TABLE message ADD COLUMN last_thread_message TEXT;
UPDATE message 
SET last_thread_message = ( 
	SELECT m2.key 
	FROM message m2 
	WHERE m2.sortid = message.sortid 
	AND m2.timestamp = (
		SELECT max(m3.timestamp) 
		FROM message m3 
		WHERE m3.sortid = message.sortid
		) 
	);
	
UPDATE message
SET reply_count = (SELECT count(*) FROM message m1 WHERE m1.sortid = message.sortid);