-- Backlog table
CREATE TABLE Backlog(
	callid TEXT PRIMARY KEY, 
	calltype INTEGER, 
	timestamp INTEGER, 
	callbody TEXT); 
CREATE INDEX ix_Backlog_timestamp ON Backlog(timestamp);

-- ConfigurationProvider table
CREATE TABLE ConfigurationProvider(
	id INTEGER PRIMARY KEY AUTOINCREMENT, 
	category TEXT, 
	valuetype TEXT, 
	key TEXT UNIQUE, 
	value TEXT); 
CREATE INDEX ix_ConfigurationProvider_key ON ConfigurationProvider(key);

--  RequestMetaPersisterImpl table
CREATE TABLE RequestMetaPersisterImpl(
	id INTEGER PRIMARY KEY AUTOINCREMENT, 
	key TEXT UNIQUE, 
	value TEXT); 
CREATE INDEX ix_RequestMetaPersisterImpl_key ON RequestMetaPersisterImpl(key);

-- Friends table
CREATE TABLE Friends(
	id INTEGER PRIMARY KEY AUTOINCREMENT, 
	key TEXT UNIQUE, 
	value TEXT); 
CREATE INDEX ix_Friends_key ON Friends(key);

-- Unprocessed messages table
CREATE TABLE unprocessed_messages(
	id TEXT PRIMARY KEY, 
	timestamp INTEGER, 
	message BLOB); 
CREATE INDEX ix_unprocessed_messages_timestamp ON unprocessed_messages(timestamp);