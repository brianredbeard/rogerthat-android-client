ALTER TABLE message ADD COLUMN existence INTEGER NOT NULL DEFAULT 1;

DROP TRIGGER tr_message_show_in_list_after_insert;
CREATE TRIGGER tr_message_show_in_list_after_insert AFTER INSERT ON message FOR EACH ROW
BEGIN
    UPDATE message
    SET show_in_message_list =
        CASE
            WHEN NEW.existence = 0 THEN 0
            WHEN NEW.sender = "dashboard@rogerth.at" AND key = last_thread_message THEN 1
            WHEN (SELECT type FROM friend WHERE NEW.sender = email) = 2 AND (NEW.dirty == 1) AND key = last_thread_message THEN 1
            WHEN (SELECT type FROM friend WHERE NEW.sender = email) = 2 AND key = last_thread_message THEN 2
            WHEN (SELECT type FROM friend WHERE NEW.sender = email) = 1 AND key = last_thread_message THEN 1
            WHEN (SELECT email FROM my_identity) = NEW.sender AND key = last_thread_message THEN 1
            ELSE 0
        END
    WHERE key = NEW.key; END;


DROP TRIGGER tr_message_show_in_list_after_update;

CREATE TRIGGER tr_message_show_in_list_after_update AFTER UPDATE OF needs_my_answer, dirty, last_thread_message, existence ON message FOR EACH ROW
BEGIN
    UPDATE message
    SET show_in_message_list =
        CASE
            WHEN NEW.existence = 0 THEN 0
            WHEN NEW.sender = "dashboard@rogerth.at" AND key = last_thread_message THEN 1
            WHEN (SELECT type FROM friend WHERE NEW.sender = email) = 2 AND (NEW.dirty == 1) AND key = last_thread_message THEN 1
            WHEN (SELECT type FROM friend WHERE NEW.sender = email) = 2 AND key = last_thread_message THEN 2
            WHEN (SELECT type FROM friend WHERE NEW.sender = email) = 1 AND key = last_thread_message THEN 1
            WHEN (SELECT email FROM my_identity) = NEW.sender AND key = last_thread_message THEN 1
            ELSE 0
        END
    WHERE key = NEW.key; END;
