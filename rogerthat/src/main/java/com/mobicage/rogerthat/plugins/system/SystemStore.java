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

package com.mobicage.rogerthat.plugins.system;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.util.db.DatabaseManager;
import com.mobicage.rogerthat.util.system.T;

public class SystemStore implements Closeable {

    private final SQLiteStatement mUpdateJSEmbeddedPacket;
    private final SQLiteStatement mDeleteJSEmbeddedPacket;

    private final MainService mMainService;
    private final SQLiteDatabase mDb;

    public SystemStore(final MainService mainService, final DatabaseManager dbManager) {
        T.UI();
        mMainService = mainService;
        mDb = dbManager.getDatabase();

        mUpdateJSEmbeddedPacket = mDb.compileStatement(mMainService.getString(R.string.sql_insert_js_embedding));
        mDeleteJSEmbeddedPacket = mDb.compileStatement(mMainService.getString(R.string.sql_delete_js_embedding));
    }

    @Override
    public void close() {
        T.UI();
        mUpdateJSEmbeddedPacket.close();
        mDeleteJSEmbeddedPacket.close();
    }

    public Map<String, JSEmbedding> getJSEmbeddedPackets() {
        T.dontCare();
        Map<String, JSEmbedding> packets = new HashMap<String, JSEmbedding>();
        final Cursor bc = mDb.rawQuery(mMainService.getString(R.string.sql_get_js_embedding), new String[] {});
        try {
            if (bc.moveToFirst()) {
                for (int i = 0; i < bc.getCount(); i++) {
                    JSEmbedding jse = new JSEmbedding(bc.getString(0), bc.getString(1), bc.getLong(2));
                    packets.put(jse.getName(), jse);
                    if (!bc.moveToNext())
                        break;
                }
            }
        } finally {
            bc.close();
        }

        return packets;
    }

    public void updateJSEmbeddedPacket(final String name, final String embeddingHash, final long status) {
        T.dontCare();
        mUpdateJSEmbeddedPacket.bindString(1, name);
        mUpdateJSEmbeddedPacket.bindString(2, embeddingHash);
        mUpdateJSEmbeddedPacket.bindLong(3, status);
        mUpdateJSEmbeddedPacket.execute();
    }

    public void deleteJSEmbeddedPacket(final String name) {
        mDeleteJSEmbeddedPacket.bindString(1, name);
        mDeleteJSEmbeddedPacket.execute();
    }
}
