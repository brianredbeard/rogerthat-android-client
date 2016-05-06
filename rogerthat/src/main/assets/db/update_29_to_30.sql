CREATE TRIGGER tr_message_thread_dirtyness_after_update AFTER UPDATE OF needs_my_answer, dirty ON message FOR EACH ROW
BEGIN
    UPDATE message
    SET thread_dirty = 
    CASE WHEN (
        SELECT sum(m1.dirty) + sum(m1.needs_my_answer) 
        FROM message m1 INNER JOIN message m2 ON m1.sortid = m2.sortid 
        WHERE m2.key = NEW.key
    ) > 0 THEN 1 ELSE 0 END
    WHERE sortid = NEW.sortid; END;

CREATE TRIGGER tr_message_thread_dirtyness_after_insert AFTER INSERT ON message FOR EACH ROW
BEGIN
    UPDATE message
	SET thread_dirty = 
    CASE WHEN (
        SELECT sum(m1.dirty) + sum(m1.needs_my_answer) 
        FROM message m1 INNER JOIN message m2 ON m1.sortid = m2.sortid 
        WHERE m2.key = NEW.key
    ) > 0 THEN 1 ELSE 0 END
    WHERE sortid = NEW.sortid; END;
