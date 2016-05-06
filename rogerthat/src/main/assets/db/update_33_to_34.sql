DROP TRIGGER tr_message_show_in_list_after_insert;
DROP TRIGGER tr_message_show_in_list_after_update;

ALTER TABLE message ADD COLUMN thread_needs_my_answer INTEGER NOT NULL DEFAULT 0;
ALTER TABLE message ADD COLUMN last_updated_on INTEGER NOT NULL DEFAULT 0;
ALTER TABLE current_unprocessed_message_index ADD COLUMN last_inbox_open_time INTEGER NOT NULL DEFAULT 0;

CREATE TRIGGER tr_message_show_in_list_after_insert AFTER INSERT ON message FOR EACH ROW 
BEGIN 
	UPDATE message 
	SET show_in_message_list = 
		CASE 
			WHEN NEW.sender = "dashboard@rogerth.at" AND key = last_thread_message THEN 1 
			WHEN (SELECT type FROM friend WHERE NEW.sender = email) = 2 AND (NEW.needs_my_answer == 1 OR NEW.dirty == 1) AND key = last_thread_message THEN 1 
			WHEN (SELECT type FROM friend WHERE NEW.sender = email) = 2 AND key = last_thread_message THEN 2 
			WHEN (SELECT type FROM friend WHERE NEW.sender = email) = 1 AND key = last_thread_message THEN 1 
			WHEN (SELECT email FROM my_identity) = NEW.sender AND key = last_thread_message THEN 1 
			ELSE 0 
		END 
	WHERE key = NEW.key; END;

CREATE TRIGGER tr_message_show_in_list_after_update AFTER UPDATE OF needs_my_answer, dirty, last_thread_message ON message FOR EACH ROW 
BEGIN 
	UPDATE message 
	SET show_in_message_list = 
		CASE 
			WHEN NEW.sender = "dashboard@rogerth.at" AND key = last_thread_message THEN 1 
			WHEN (SELECT type FROM friend WHERE NEW.sender = email) = 2 AND (NEW.needs_my_answer == 1 OR NEW.dirty == 1) AND key = last_thread_message THEN 1 
			WHEN (SELECT type FROM friend WHERE NEW.sender = email) = 2 AND key = last_thread_message THEN 2 
			WHEN (SELECT type FROM friend WHERE NEW.sender = email) = 1 AND key = last_thread_message THEN 1 
			WHEN (SELECT email FROM my_identity) = NEW.sender AND key = last_thread_message THEN 1 
			ELSE 0 
		END 
	WHERE key = NEW.key; END;

UPDATE message
    SET last_thread_message = (SELECT mm.key FROM message mm WHERE mm.parent_key = key GROUP BY mm.parent_key HAVING max(mm.timestamp) = mm.timestamp)
    WHERE last_thread_message IS NULL;
    
UPDATE message
    SET last_thread_message = key
    WHERE last_thread_message IS NULL;

UPDATE message 
	SET show_in_message_list = 
		CASE 
			WHEN sender = "dashboard@rogerth.at" AND key = last_thread_message THEN 1 
			WHEN (SELECT type FROM friend f WHERE sender = f.email) = 2 AND (needs_my_answer == 1 OR dirty == 1) AND key = last_thread_message THEN 1 
			WHEN (SELECT type FROM friend f WHERE sender = f.email) = 2 AND key = last_thread_message THEN 2 
			WHEN (SELECT type FROM friend f WHERE sender = f.email) = 1 AND key = last_thread_message THEN 1 
			WHEN (SELECT email FROM my_identity) = sender AND key = last_thread_message THEN 1 
			ELSE 0 
		END; 

UPDATE message 
SET thread_needs_my_answer = CASE WHEN ( SELECT sum(m1.needs_my_answer) 
									     FROM message m1 INNER JOIN message m2 ON m1.sortid = m2.sortid 
									     WHERE m2.key = message.key ) > 0 THEN 1 ELSE 0 
						END;


DROP TRIGGER tr_message_thread_dirtyness_after_insert;
CREATE TRIGGER tr_message_thread_dirtyness_after_insert AFTER INSERT ON message FOR EACH ROW 
BEGIN 
	UPDATE message 
	SET thread_dirty = CASE WHEN ( SELECT sum(m1.dirty)
								   FROM message m1 INNER JOIN message m2 ON m1.sortid = m2.sortid 
								   WHERE m2.key = NEW.key ) > 0 THEN 1 ELSE 0 
							END,
		thread_needs_my_answer = CASE WHEN ( SELECT sum(m1.needs_my_answer) 
										     FROM message m1 INNER JOIN message m2 ON m1.sortid = m2.sortid 
										     WHERE m2.key = NEW.key ) > 0 THEN 1 ELSE 0 
							END
	WHERE sortid = NEW.sortid; END;

DROP TRIGGER tr_message_thread_dirtyness_after_update;
CREATE TRIGGER tr_message_thread_dirtyness_after_update AFTER UPDATE OF needs_my_answer, dirty ON message FOR EACH ROW 
BEGIN 
	UPDATE message 
	SET thread_dirty = CASE WHEN ( SELECT sum(m1.dirty)
								   FROM message m1 INNER JOIN message m2 ON m1.sortid = m2.sortid 
								   WHERE m2.key = NEW.key ) > 0 THEN 1 ELSE 0 
					        END,
		thread_needs_my_answer = CASE WHEN ( SELECT sum(m1.needs_my_answer) 
										     FROM message m1 INNER JOIN message m2 ON m1.sortid = m2.sortid 
										     WHERE m2.key = NEW.key ) > 0 THEN 1 ELSE 0 
							END
	WHERE sortid = NEW.sortid; END;