/*
 * Copyright 2018 GIG Technology NV
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
 * @@license_version:1.4@@
 */

package com.mobicage.rogerthat.util.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;

import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.util.IOUtils;
import com.mobicage.rogerthat.util.db.updates.IDbUpdater;
import com.mobicage.rogerthat.util.db.updates.Updates;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.T;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class DatabaseManager implements Closeable {

    private final static String DB_NAME = "mc.db";
    private final static int DB_VERSION = 83;

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
        }

        @Override
        public void onCreate(final SQLiteDatabase db) {
            T.UI();
            initDB(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            T.UI();
            for (int i = oldVersion; i < newVersion; i++) {
                updateDB(db, i, i + 1);
            }
        }

        private List<String> getSql(final String filename) {
            T.UI();
            try {
                return IOUtils.readAllLines(new InputStreamReader(mMainService.getAssets().open(filename)));
            } catch (IOException e) {
                L.d("Asset " + filename + " not found!");
                throw new RuntimeException(e);
            }
        }

        private void initDB(final SQLiteDatabase db) {
            L.d("Initializing db");
            final List<String> sqlLines = getSql("db/init.sql");
            executeLines(db, sqlLines);

            final Updates updates = new Updates();
            for (int i = 0; i <= DB_VERSION; i++) {
                IDbUpdater updater = updates.getUpdater(i);
                updater.postUpdate(mMainService, db);
            }
        }

        private void updateDB(final SQLiteDatabase db, final int current, final int next) {
            T.UI();
            L.d("Upgrading db from " + current + " to " + next);
            final List<String> sqlLines = getSql("db/update_" + current + "_to_" + next + ".sql");

            final Updates updates = new Updates();
            IDbUpdater updater = updates.getUpdater(next);
            executeLines(db, sqlLines);
            updater.postUpdate(mMainService, db);
        }

        private void executeLines(final SQLiteDatabase db, List<String> sqlLines) {
            if (sqlLines != null) {
                StringBuilder sb = new StringBuilder();
                for (String sqlPart : sqlLines) {
                    final String line = sqlPart.trim();
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
        }
    }

}
