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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.mobicage.rogerthat.util.system.T;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;

//
// Database with 4 columns
//   category
//   valuetype (string / long / boolean)
//   key
//   value
//
public class CategorizedKeyValueDatabase implements Closeable {

    public static final String VALUETYPE_STRING = "S";
    public static final String VALUETYPE_LONG = "L";
    public static final String VALUETYPE_BOOLEAN = "B";

    public final static String DB_FALSE_VALUE = "F";
    public final static String DB_TRUE_VALUE = "T";

    private final String mTableName;
    private final DatabaseManager mDatabaseManager;

    private SQLiteDatabase mDb;
    private SQLiteStatement mInsertStmt;
    private DbBuilder mDbBuilder;

    public CategorizedKeyValueDatabase(DatabaseManager pDatabaseManager, String pTableName) {
        T.UI();
        mDatabaseManager = pDatabaseManager;
        mTableName = pTableName;
        initDb();
    }

    // not threadsafe
    public long putString(String category, String key, String value) {
        T.dontCare();
        return put(category, VALUETYPE_STRING, key, value);
    }

    // not threadsafe
    public long putLong(String category, String key, long value) {
        T.dontCare();
        return put(category, VALUETYPE_LONG, key, String.valueOf(value));
    }

    // not threadsafe
    public long putBoolean(String category, String key, boolean value) {
        T.dontCare();
        return put(category, VALUETYPE_BOOLEAN, key, value ? DB_TRUE_VALUE : DB_FALSE_VALUE);
    }

    // not threadsafe
    private long put(String category, String valuetype, String key, String value) {
        T.dontCare();
        mInsertStmt.bindString(1, category);
        mInsertStmt.bindString(2, valuetype);
        mInsertStmt.bindString(3, key);
        mInsertStmt.bindString(4, value);
        return mInsertStmt.executeInsert();
    }

    public String getString(String category, String key, String defaultValue) {
        T.dontCare();
        String result = get(category, VALUETYPE_STRING, key);
        if (result == null) {
            return defaultValue;
        }
        return result;
    }

    public long getLong(String category, String key, long defaultValue) {
        T.dontCare();
        String result = get(category, VALUETYPE_LONG, key);
        if (result == null) {
            return defaultValue;
        }
        return Long.valueOf(result);
    }

    public boolean getBoolean(String category, String key, boolean defaultValue) {
        T.dontCare();
        String result = get(category, VALUETYPE_BOOLEAN, key);
        if (result == null) {
            return defaultValue;
        }
        return result.equals(DB_TRUE_VALUE);
    }

    private String get(String category, String valuetype, String key) {
        T.dontCare();
        final Cursor cursor = mDb.query(mTableName, new String[] { "value" },
            "category = ? and valuetype = ? and key = ?", new String[] { category, valuetype, key }, null, null, null);
        if (cursor == null)
            return null;

        String value = null;
        if (cursor.moveToFirst()) {
            value = cursor.getString(0);
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return value;
    }

    // XXX: try-finally for cursor closing
    public Map<String, String> getStringEntries(String category) {
        T.dontCare();
        Cursor cursor = mDb.query(mTableName, new String[] { "key", "value" }, "category = ? and valuetype = ?",
            new String[] { category, VALUETYPE_STRING }, null, null, null);
        HashMap<String, String> result = new HashMap<String, String>();

        if (cursor == null)
            return result;

        int keyColumnIndex = cursor.getColumnIndex("key");
        int valueColumnIndex = cursor.getColumnIndex("value");
        if (cursor.moveToFirst()) {
            do {
                result.put(cursor.getString(keyColumnIndex), cursor.getString(valueColumnIndex));
            } while (cursor.moveToNext());
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return result;
    }

    // XXX: try-finally for cursor closing
    public Map<String, Long> getLongEntries(String category) {
        T.dontCare();
        Cursor cursor = mDb.query(mTableName, new String[] { "key", "value" }, "category = ? and valuetype = ?",
            new String[] { category, VALUETYPE_LONG }, null, null, null);
        HashMap<String, Long> result = new HashMap<String, Long>();
        if (cursor == null)
            return result;

        int keyColumnIndex = cursor.getColumnIndex("key");
        int valueColumnIndex = cursor.getColumnIndex("value");
        if (cursor.moveToFirst()) {
            do {
                result.put(cursor.getString(keyColumnIndex), Long.valueOf(cursor.getString(valueColumnIndex)));
            } while (cursor.moveToNext());
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return result;
    }

    // XXX: try-finally for cursor closing
    public Map<String, Boolean> getBooleanEntries(String category) {
        T.dontCare();
        Cursor cursor = mDb.query(mTableName, new String[] { "key", "value" }, "category = ? and valuetype = ?",
            new String[] { category, VALUETYPE_BOOLEAN }, null, null, null);
        HashMap<String, Boolean> result = new HashMap<String, Boolean>();
        if (cursor == null)
            return result;

        int keyColumnIndex = cursor.getColumnIndex("key");
        int valueColumnIndex = cursor.getColumnIndex("value");
        if (cursor.moveToFirst()) {
            do {
                result.put(cursor.getString(keyColumnIndex), cursor.getString(valueColumnIndex).equals(DB_TRUE_VALUE));
            } while (cursor.moveToNext());
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return result;
    }

    private void initDb() {
        T.UI();
        mDbBuilder = new DbBuilder(mTableName);
        mDbBuilder.addColumn("id", "INTEGER PRIMARY KEY AUTOINCREMENT");
        mDbBuilder.addColumn("category", "TEXT");
        mDbBuilder.addColumn("valuetype", "TEXT");
        mDbBuilder.addIndexedColumn("key", "TEXT UNIQUE");
        mDbBuilder.addColumn("value", "TEXT");
        mDb = mDatabaseManager.getDatabase();

        String putSql = "insert or replace into " + mTableName
            + "(category, valuetype, key, value) values (?, ?, ?, ?)";
        mInsertStmt = mDb.compileStatement(putSql);
    }

    @Override
    public void close() {
        T.UI();
        mInsertStmt.close();
    }

}
