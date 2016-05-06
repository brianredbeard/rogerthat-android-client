ALTER TABLE my_identity ADD COLUMN profile_data TEXT;
ALTER TABLE friend ADD COLUMN profile_data TEXT;

DROP TRIGGER tr_message_show_in_list_after_insert;

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


DROP TRIGGER tr_message_show_in_list_after_update;

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
