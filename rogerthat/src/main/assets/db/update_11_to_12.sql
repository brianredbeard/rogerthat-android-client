-- Activity table
CREATE TABLE Activity(
	id INTEGER PRIMARY KEY AUTOINCREMENT, 
	timestamp INTEGER, 
	type INTEGER, 
	reference TEXT,
	parameters BLOB,
	friend_reference TEXT); 

-- Create table last_read_activity_id
CREATE TABLE last_read_activity_id (
   "id" INTEGER
);

INSERT INTO last_read_activity_id ("id") VALUES (-1);
