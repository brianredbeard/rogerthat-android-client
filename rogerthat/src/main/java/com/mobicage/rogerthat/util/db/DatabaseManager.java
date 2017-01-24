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
import java.io.IOException;
import java.io.InputStreamReader;

import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;

import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.util.db.updates.IDbUpdater;
import com.mobicage.rogerthat.util.db.updates.Updates;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.T;

public class DatabaseManager implements Closeable {

    private final static String DB_NAME = "mc.db";
    private final static int DB_VERSION = 73;

    private final MainService mMainService;
    private final SQLiteDatabase mDb;

    public DatabaseManager(final MainService context) {
        T.UI();
        mMainService = context;
        final OpenHelper openHelper = new OpenHelper(mMainService, DB_NAME, DB_VERSION);
        mDb = openHelper.getWritableDatabase();
    }

    @Override
    public void close() {
        T.UI();
        if (mDb != null) {
            mDb.close();
            L.d("********* CLOSED MC DATABASE *********");
        }
    }

    public void wipeAndClose() {
        T.UI();
        close();
        mMainService.deleteDatabase(DB_NAME);
    }

    public SQLiteDatabase getDatabase() {
        T.dontCare();
        return mDb;
    }

    private class OpenHelper extends SQLiteOpenHelper {

        private final MainService mMainService;
        private final int mVersion;

        OpenHelper(final MainService mainService, final String dbName, final int dbVersion) {
            super(mainService, dbName, new SQLiteDatabase.CursorFactory() {
                @Override
                public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver masterQuery, String editTable,
                    SQLiteQuery query) {
                    // L.d("SQL: " + query.toString());
                    return new SQLiteCursor(db, masterQuery, editTable, query);
                }
            }, dbVersion);
            T.UI();
            mMainService = mainService;
            mVersion = dbVersion;
        }

        @Override
        public void onCreate(final SQLiteDatabase db) {
            T.UI();
            for (int i = 1; i <= mVersion; i++) {
                final int current = i - 1;
                final int next = i;
                updateDB(db, current, next);
            }
        }

        private void updateDB(final SQLiteDatabase db, final int current, final int next) {
            T.UI();
            L.d("Upgrading db from " + current + " to " + next);
            Updates updates = new Updates();
            IDbUpdater updater = updates.getUpdater(next);
            updater.preUpdate(mMainService, db);
            final String sql = getSql("db/update_" + current + "_to_" + next + ".sql");
            if (sql != null) {
                String[] sql_parts = sql.split("\n");
                StringBuilder sb = new StringBuilder();
                for (String sql_part : sql_parts) {
                    final String line = sql_part.trim();
                    if (line.startsWith("--"))
                        continue;
                    sb.append(line);
                    sb.append(" ");
                    if (line.endsWith(";")) {
                        String sql_statement = sb.toString();
                        L.d("Executing sql:\n" + sql_statement);
                        db.execSQL(sql_statement);
                        sb = new StringBuilder();
                    }
                }
            }
            updater.postUpdate(mMainService, db);
        }

        private String getSql(final String filename) {
            T.UI();
            final InputStreamReader reader;
            try {
                reader = new InputStreamReader(mMainService.getAssets().open(filename));
                try {
                    final StringBuilder sb = new StringBuilder();
                    final char[] buf = new char[1024];
                    int bytes_read = reader.read(buf);
                    while (bytes_read != -1) {
                        sb.append(buf, 0, bytes_read);
                        bytes_read = reader.read(buf);
                    }
                    return sb.toString();
                } finally {
                    reader.close();
                }
            } catch (IOException e) {
                L.d("Asset " + filename + " not found!");
                return null;
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            T.UI();
            for (int i = oldVersion; i < newVersion; i++) {
                updateDB(db, i, i + 1);
            }
        }
    }

}
