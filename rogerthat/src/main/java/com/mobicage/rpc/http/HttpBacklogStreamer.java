/*
 * Copyright 2017 GIG Technology NV
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
 * @@license_version:1.3@@
 */

package com.mobicage.rpc.http;

import java.io.Closeable;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.util.system.T;

public class HttpBacklogStreamer implements Closeable {

    private final static String BASE_SQL = "SELECT callid, calltype, callbody, timestamp FROM Backlog WHERE last_resend_timestamp < ? AND has_priority >= 0 AND calltype="
        + HttpBacklog.MESSAGETYPE_CALL;
    private final static String ORDER_BY = " ORDER BY has_priority DESC, last_resend_timestamp";
    private final static String WHERE_WIFI_ONLY = " AND wifi_only=0";

    public final static HttpBacklogItem SKIP = new HttpBacklogItem();

    private final Context mContext;
    private final SQLiteDatabase mDb;

    private Cursor mCursor;

    public HttpBacklogStreamer(Context context, SQLiteDatabase db, boolean filterOnWifiOnly) {
        // notWifiOnly means that we're looking for backlog items which may be send over edge/3G/...
        T.BIZZ();
        mContext = context;
        mDb = db;

        String sql = BASE_SQL;
        if (filterOnWifiOnly)
            sql += WHERE_WIFI_ONLY;
        sql += ORDER_BY;

        mCursor = db.rawQuery(sql,
            new String[] { String.valueOf(System.currentTimeMillis() - HttpProtocol.PACKET_GRACE_TIME) });
    }

    public HttpBacklogItem next() {
        T.BIZZ();
        if (mCursor == null) {
            mCursor = mDb.rawQuery(mContext.getString(R.string.sql_backlog_batch),
                new String[] { String.valueOf(HttpProtocol.PACKET_GRACE_TIME) });
            if (!mCursor.moveToFirst())
                return null;
        } else {
            if (!mCursor.moveToNext())
                return null;
        }
        HttpBacklogItem bi = new HttpBacklogItem();
        bi.callid = mCursor.getString(0);
        bi.calltype = mCursor.getInt(1);

        bi.body = mCursor.getString(2);
        if (bi.body == null) {
            // Do not wire incomplete responses
            // XXX - should do thorough analysis - when do we come here?
            return SKIP;
        }
        bi.timestamp = mCursor.getLong(3);
        return bi;
    }

    @Override
    public void close() {
        T.BIZZ();
        if (mCursor != null)
            mCursor.close();
    }
}
