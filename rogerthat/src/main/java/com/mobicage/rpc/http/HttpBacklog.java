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

package com.mobicage.rpc.http;

import java.io.Closeable;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.util.db.DatabaseManager;
import com.mobicage.rogerthat.util.db.TransactionHelper;
import com.mobicage.rogerthat.util.db.TransactionWithoutResult;
import com.mobicage.rogerthat.util.pickle.PickleException;
import com.mobicage.rogerthat.util.pickle.Pickler;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.IResponseHandler;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.rpc.RpcCall;
import com.mobicage.rpc.RpcResult;
import com.mobicage.rpc.singlecall.SingleCall;

public class HttpBacklog implements Closeable {

    public static final int MESSAGETYPE_CALL = 0;
    public static final int MESSAGETYPE_RESPONSE = 1;

    private final Context mContext;
    private final SQLiteDatabase mDb;

    private final SQLiteStatement mGetBodyStatement;
    private final SQLiteStatement mInsertStatement;
    private final SQLiteStatement mHasBodyStatement;
    private final SQLiteStatement mUpdateBodyStatement;
    private final SQLiteStatement mDeleteBacklogItemStatement;
    private final SQLiteStatement mUpdateItemRetentionStatement;
    private final SQLiteStatement mRunRetentionCleanupStatement;
    private final SQLiteStatement mRemovePreviousUnsentCallsStatement;

    public HttpBacklog(final Context context, final DatabaseManager databaseManager) {
        T.BIZZ();
        mContext = context;
        mDb = databaseManager.getDatabase();
        mGetBodyStatement = mDb.compileStatement(mContext.getString(R.string.sql_backlog_get_body));
        mInsertStatement = mDb.compileStatement(mContext.getString(R.string.sql_backlog_insert));
        mHasBodyStatement = mDb.compileStatement(mContext.getString(R.string.sql_backlog_has_body));
        mUpdateBodyStatement = mDb.compileStatement(mContext.getString(R.string.sql_backlog_update_body));
        mDeleteBacklogItemStatement = mDb.compileStatement(mContext.getString(R.string.sql_backlog_delete_item));
        mUpdateItemRetentionStatement = mDb.compileStatement(mContext
            .getString(R.string.sql_backlog_update_retention_timeout));
        mRunRetentionCleanupStatement = mDb.compileStatement(mContext.getString(R.string.sql_backlog_run_retention));
        mRemovePreviousUnsentCallsStatement = mDb.compileStatement(mContext
            .getString(R.string.sql_backlog_remove_previous_unsent_calls));

        // Following line of code is for development purpose
        // Should never be compiled into a production version
        // mDb.compileStatement("DELETE FROM Backlog").execute(); // DO NOT USE IN PRODUCTION !!
    }

    @Override
    public void close() {
        T.BIZZ();
        mGetBodyStatement.close();
        mInsertStatement.close();
        mHasBodyStatement.close();
        mUpdateBodyStatement.close();
        mDeleteBacklogItemStatement.close();
        mUpdateItemRetentionStatement.close();
        mRunRetentionCleanupStatement.close();
        mRemovePreviousUnsentCallsStatement.close();
    }

    public HttpBacklogStreamer getStreamer(boolean filterOnWifiOnly) {
        T.BIZZ();
        return new HttpBacklogStreamer(mContext, mDb, filterOnWifiOnly);
    }

    public String getBodyForCallId(final String callid) {
        T.BIZZ();
        mGetBodyStatement.bindString(1, callid);
        return mGetBodyStatement.simpleQueryForString();
    }

    public boolean hasBodyForCallId(final String callid) {
        T.BIZZ();
        mHasBodyStatement.bindString(1, callid);
        return mHasBodyStatement.simpleQueryForLong() != 0;
    }

    public void insertNewIncomingCallInDb(final RpcCall rpcc) {
        T.BIZZ();
        mInsertStatement.bindString(1, rpcc.callId);
        mInsertStatement.bindLong(2, MESSAGETYPE_RESPONSE);
        mInsertStatement.bindLong(3, rpcc.timestamp * 1000);
        mInsertStatement.bindNull(4);
        mInsertStatement.bindLong(5, 1); // Incoming calls are always priorirty
        mInsertStatement.bindLong(6, 0);
        mInsertStatement.bindLong(7, System.currentTimeMillis() + HttpProtocol.MESSAGE_RETENTION_INTERVAL);
        mInsertStatement.bindNull(8);
        mInsertStatement.bindString(9, rpcc.function);
        mInsertStatement.bindLong(10, RpcCall.WIFI_ONLY_FUNCTIONS.contains(rpcc.function) ? 1 : 0);
        mInsertStatement.execute();
    }

    public void updateBody(final RpcResult rpcResult) {
        T.BIZZ();
        mUpdateBodyStatement.bindString(1, rpcResult.toJSON());
        mUpdateBodyStatement.bindString(2, rpcResult.callId);
        mUpdateBodyStatement.execute();
    }

    public ResponseHandler<?> getResponseHandler(final String callid) throws PickleException {
        T.BIZZ();
        final Cursor qry = mDb.rawQuery(mContext.getString(R.string.sql_backlog_get_response_handler),
            new String[] { callid });
        try {
            if (!qry.moveToFirst())
                return null;
            byte[] blob = qry.getBlob(0);
            if (blob == null)
                throw new PickleException("Response handler field in db is null");
            return (ResponseHandler<?>) Pickler.createObjectFromPickle(blob);
        } finally {
            qry.close();
        }
    }

    public void deleteItem(String callId) {
        T.BIZZ();
        mDeleteBacklogItemStatement.bindString(1, callId);
        mDeleteBacklogItemStatement.execute();
    }

    public void freezeRetention(String callid) {
        T.BIZZ();
        mUpdateItemRetentionStatement.bindLong(1, System.currentTimeMillis()
            + HttpProtocol.DUPLICATE_AVOIDANCE_RETENTION_INTERVAL);
        mUpdateItemRetentionStatement.bindString(2, callid);
        mUpdateItemRetentionStatement.execute();
    }

    public void doRetentionCleanup() {
        T.BIZZ();
        mRunRetentionCleanupStatement.bindLong(1, System.currentTimeMillis());
        mRunRetentionCleanupStatement.execute();
    }

    public void addOutgoingCall(final HttpBacklogItem item, final boolean prior, final String function,
        final IResponseHandler<?> responseHandler, final boolean wifiOnly) throws PickleException {
        T.BIZZ();
        try {
            TransactionHelper.runInTransaction(mDb, "addOutgoingCall", new TransactionWithoutResult() {

                @Override
                protected void run() {
                    T.BIZZ();
                    if (RpcCall.isSingleCall(function)) {
                        mRemovePreviousUnsentCallsStatement.bindLong(1, MESSAGETYPE_CALL);
                        mRemovePreviousUnsentCallsStatement.bindString(2, function);
                        mRemovePreviousUnsentCallsStatement.execute();
                    } else if (RpcCall.isSpecialSingleCall(function)) {
                        SingleCall call = SingleCall.singleCallForFunction(function, item.body);
                        if (call != null) {
                            final Cursor cursor = mDb.rawQuery(
                                mContext.getString(R.string.sql_backlog_singlecall_body), new String[] { function });
                            try {
                                if (cursor.moveToFirst()) {
                                    while (true) {
                                        final String callId = cursor.getString(0);
                                        final String callBody = cursor.getString(1);

                                        if (call.isEqualToCallWithBody(callBody))
                                            deleteItem(callId);

                                        if (!cursor.moveToNext())
                                            break;
                                    }
                                }
                            } finally {
                                cursor.close();
                            }
                        }
                    }
                    mInsertStatement.bindString(1, item.callid);
                    mInsertStatement.bindLong(2, item.calltype);
                    mInsertStatement.bindLong(3, item.timestamp);
                    mInsertStatement.bindString(4, item.body);
                    mInsertStatement.bindLong(5, prior ? 1 : 0);
                    mInsertStatement.bindLong(6, 0);
                    mInsertStatement.bindLong(7, System.currentTimeMillis() + HttpProtocol.MESSAGE_RETENTION_INTERVAL);
                    try {
                        mInsertStatement.bindBlob(8, Pickler.getPickleFromObject(responseHandler));
                    } catch (PickleException e) {
                        throw new TransactionHelper.WrappedException(e);
                    }
                    mInsertStatement.bindString(9, function);
                    mInsertStatement.bindLong(10, wifiOnly ? 1 : 0);
                    mInsertStatement.execute();
                }
            });
        } catch (TransactionHelper.WrappedException e) {
            if (e.wrappedThrowable instanceof PickleException) {
                throw (PickleException) e.wrappedThrowable;
            }

            throw e;
        }
    }
}
