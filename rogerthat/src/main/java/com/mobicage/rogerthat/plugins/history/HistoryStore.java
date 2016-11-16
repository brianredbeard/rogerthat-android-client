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

package com.mobicage.rogerthat.plugins.history;

import java.io.Closeable;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.util.db.DatabaseManager;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.pickle.PickleException;
import com.mobicage.rogerthat.util.pickle.Pickler;
import com.mobicage.rogerthat.util.system.T;

public class HistoryStore implements Closeable {

    private final SQLiteStatement mCountItemsReadStatementUI;
    private final SQLiteStatement mCountItemsUnreadStatementUI;
    private final SQLiteStatement mGetLastReadStatementUI;
    private final SQLiteStatement mSetLastReadStatementUI;

    private final SQLiteStatement mAddItemStatementHTTP;
    private final SQLiteStatement mUpdateItemReferenceStatementHTTP;
    private final SQLiteStatement mDeleteItemStatementHTTP;

    private final Context mContext;
    private final SQLiteDatabase mDb;

    public HistoryStore(final Context context, final DatabaseManager dbManager) {
        T.UI();
        mContext = context;
        mDb = dbManager.getDatabase();

        mCountItemsReadStatementUI = mDb.compileStatement(mContext.getString(R.string.sql_activity_count_read));
        mCountItemsUnreadStatementUI = mDb.compileStatement(mContext.getString(R.string.sql_activity_count_unread));
        mGetLastReadStatementUI = mDb.compileStatement(mContext
            .getString(R.string.sql_activity_get_last_unread_activity));
        mSetLastReadStatementUI = mDb.compileStatement(mContext
            .getString(R.string.sql_activity_update_last_unread_activity));
        mAddItemStatementHTTP = mDb.compileStatement(mContext.getString(R.string.sql_activity_insert));
        mUpdateItemReferenceStatementHTTP = mDb.compileStatement(mContext
            .getString(R.string.sql_activity_update_reference_key));

        mDeleteItemStatementHTTP = mDb.compileStatement(mContext.getString(R.string.sql_activity_delete_for_message));

        // DO NOT PUT THE FOLLOWING LINE IN PRODUCTION !!!
        // mDb.execSQL("DELETE FROM activity");
    }

    Cursor getImportantOnlyCursor() {

        return mDb.rawQuery(mContext.getString(R.string.sql_activity_cursor_important_only_query), null);
    }

    Cursor getFullCursor() {
        return mDb.rawQuery(mContext.getString(R.string.sql_activity_cursor_full_query), null);
    }

    HistoryItem getCurrentHistoryItem(Cursor cursor) {
        try {
            HistoryItem historyItem = new HistoryItem();
            historyItem.timestampMillis = cursor.getLong(0);
            historyItem.type = cursor.getInt(1);
            historyItem.reference = cursor.getString(2);
            byte[] parameterBlob = cursor.getBlob(3);
            if (parameterBlob != null) {
                historyItem.parameters.putAll((HistoryItem.PickleableStringMap) Pickler
                    .createObjectFromPickle(parameterBlob));
            }
            historyItem.friendReference = cursor.getString(4);
            historyItem._id = cursor.getLong(5);
            return historyItem;
        } catch (PickleException e) {
            L.bug(e);
            return null;
        }
    }

    public void saveHistoryItem(HistoryItem item) {
        T.BIZZ();
        try {
            mAddItemStatementHTTP.bindLong(1, item.timestampMillis);
            mAddItemStatementHTTP.bindLong(2, item.type);
            if (item.reference == null)
                mAddItemStatementHTTP.bindNull(3);
            else
                mAddItemStatementHTTP.bindString(3, item.reference);
            if (item.parameters == null)
                mAddItemStatementHTTP.bindNull(4);
            else
                mAddItemStatementHTTP.bindBlob(4, Pickler.getPickleFromObject(item.parameters));
            if (item.friendReference == null)
                mAddItemStatementHTTP.bindNull(5);
            else
                mAddItemStatementHTTP.bindString(5, item.friendReference);
            mAddItemStatementHTTP.execute();
        } catch (PickleException e) {
            L.bug(e);
        }
    }

    @Override
    public void close() {
        T.UI();
        mCountItemsReadStatementUI.close();
        mCountItemsUnreadStatementUI.close();
        mGetLastReadStatementUI.close();
        mSetLastReadStatementUI.close();
        mAddItemStatementHTTP.close();
        mUpdateItemReferenceStatementHTTP.close();
    }

    public Cursor newQueryCursor(int maxItems) {
        T.UI();
        return mDb.rawQuery(mContext.getString(R.string.sql_activity_select_ids),
            new String[] { String.valueOf(maxItems) });
    }

    public void updateHistoryItemReference(String oldReference, String newReference) {
        T.BIZZ();
        mUpdateItemReferenceStatementHTTP.bindString(1, newReference);
        mUpdateItemReferenceStatementHTTP.bindString(2, oldReference);
        mUpdateItemReferenceStatementHTTP.execute();
    }

    public void updateLastReadItemID() {
        T.UI();
        // could be improved by returning MAX(ROWID) immediately in this function
        mSetLastReadStatementUI.execute();
    }

    public long getLastReadItemID() {
        T.UI();
        return mGetLastReadStatementUI.simpleQueryForLong();
    }

    public void deleteHistoryItem(String reference) {
        T.BIZZ();
        mDeleteItemStatementHTTP.bindString(1, reference);
        mDeleteItemStatementHTTP.bindString(2, reference);
        mDeleteItemStatementHTTP.execute();
    }
}
