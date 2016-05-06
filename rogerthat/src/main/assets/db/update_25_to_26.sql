ALTER TABLE message ADD COLUMN show_in_message_list INTEGER NOT NULL DEFAULT 0;
ALTER TABLE message ADD COLUMN day INTEGER NOT NULL DEFAULT 0;

UPDATE message SET show_in_message_list = CASE WHEN parent_key IS NULL OR needs_my_answer == 1 OR dirty == 1 OR key = last_thread_message THEN 1 ELSE 0 END;
		
CREATE INDEX ix_message_show_in_list ON message ("show_in_message_list");

CREATE TRIGGER tr_message_show_in_list_after_insert AFTER INSERT ON message FOR EACH ROW 
BEGIN 
	UPDATE message 
	SET show_in_message_list = 
		CASE 
			WHEN parent_key IS NULL OR needs_my_answer == 1 OR dirty == 1 OR key = last_thread_message THEN 1 
			ELSE 0 
		END 
	WHERE key = NEW.key; END;

CREATE TRIGGER tr_message_show_in_list_after_update AFTER UPDATE OF needs_my_answer, dirty, last_thread_message ON message FOR EACH ROW 
BEGIN 
	UPDATE message
	SET show_in_message_list = 
		CASE
			WHEN NEW.parent_key IS NULL OR NEW.needs_my_answer == 1 OR NEW.dirty == 1 OR NEW.key = NEW.last_thread_message THEN 1
			ELSE 0
		END
	WHERE key = NEW.key; END;