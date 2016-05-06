/*
 * Copyright 2016 Mobicage NV
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @@license_version:1.1@@
 */

package com.mobicage.rogerthat.util.db;

import java.io.Closeable;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class SimpleKeyValueDatabase implements Closeable {

    private SQLiteDatabase mDb;
    private SQLiteStatement mInsertStmt;
    private final String mTableName;
    private final DatabaseManager mDatabaseManager;
    private DbBuilder mDbBuilder;

    public SimpleKeyValueDatabase(DatabaseManager pDatabaseManager, String pTableName) {
        mDatabaseManager = pDatabaseManager;
        mTableName = pTableName;
        initDb();
    }

    public long put(String key, String value) {
        mInsertStmt.bindString(1, key);
        mInsertStmt.bindString(2, value);
        return mInsertStmt.executeInsert();
    }

    // XXX try-finally for cursor closing
    public String get(String key, String defaultValue) {
        Cursor cursor = mDb.query(mTableName, new String[] { "value" }, "key = ?", new String[] { key }, null, null,
            null);
        if (cursor == null)
            return null;

        String value = null;
        if (cursor.moveToFirst()) {
            value = cursor.getString(0);
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    public void delete(String key) {
        mDb.delete(mTableName, "key = ?", new String[] { key });
    }

    private void initDb() {
        mDbBuilder = new DbBuilder(mTableName);
        mDbBuilder.addColumn("id", "INTEGER PRIMARY KEY AUTOINCREMENT");
        mDbBuilder.addIndexedColumn("key", "TEXT UNIQUE");
        mDbBuilder.addColumn("value", "TEXT");
        mDb = mDatabaseManager.getDatabase();

        String putSql = "insert or replace into " + mTableName + "(key, value) values (?, ?)";
        mInsertStmt = mDb.compileStatement(putSql);
    }

    @Override
    public void close() {
        if (mInsertStmt != null) {
            mInsertStmt.close();
        }
    }

}