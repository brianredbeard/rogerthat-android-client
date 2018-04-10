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

package com.mobicage.rogerthat.plugins.system;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteStatement;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.util.db.DatabaseManager;
import com.mobicage.rogerthat.util.system.T;

public class SystemStore implements Closeable {

    private final SQLiteStatement mGetEmbeddedApp;
    private final SQLiteStatement mUpdateEmbeddedApp;
    private final SQLiteStatement mDeleteEmbeddedApp;

    private final SQLiteStatement mUpdateJSEmbeddedPacket;
    private final SQLiteStatement mDeleteJSEmbeddedPacket;

    private final SQLiteStatement mQRInsert;
    private final SQLiteStatement mQRDelete;

    private final SQLiteStatement mEmbeddedAppTranslationsInsert;
    private final SQLiteStatement mEmbeddedAppTranslationsSelect;

    private final MainService mMainService;
    private final SQLiteDatabase mDb;

    public SystemStore(final MainService mainService, final DatabaseManager dbManager) {
        T.UI();
        mMainService = mainService;
        mDb = dbManager.getDatabase();

        mGetEmbeddedApp = mDb.compileStatement(mMainService.getString(R.string.sql_get_embedded_app));
        mUpdateEmbeddedApp = mDb.compileStatement(mMainService.getString(R.string.sql_insert_embedded_app));
        mDeleteEmbeddedApp = mDb.compileStatement(mMainService.getString(R.string.sql_delete_embedded_app));

        mUpdateJSEmbeddedPacket = mDb.compileStatement(mMainService.getString(R.string.sql_insert_js_embedding));
        mDeleteJSEmbeddedPacket = mDb.compileStatement(mMainService.getString(R.string.sql_delete_js_embedding));

        mQRInsert = mDb.compileStatement(mMainService.getString(R.string.sql_qr_insert));
        mQRDelete = mDb.compileStatement(mMainService.getString(R.string.sql_qr_delete));

        mEmbeddedAppTranslationsInsert = mDb.compileStatement(mMainService.getString(R.string.sql_embedded_app_translations_insert));
        mEmbeddedAppTranslationsSelect = mDb.compileStatement(mMainService.getString(R.string.sql_embedded_app_translations_select));
    }

    @Override
    public void close() {
        T.UI();
        mGetEmbeddedApp.close();
        mUpdateEmbeddedApp.close();
        mDeleteEmbeddedApp.close();

        mUpdateJSEmbeddedPacket.close();
        mDeleteJSEmbeddedPacket.close();

        mQRInsert.close();
        mQRDelete.close();

        mEmbeddedAppTranslationsInsert.close();
        mEmbeddedAppTranslationsSelect.close();
    }

    public Map<String, Long> getEmbeddedApps() {
        T.dontCare();
        Map<String, Long> apps = new HashMap<>();
        final Cursor bc = mDb.rawQuery(mMainService.getString(R.string.sql_select_embedded_app), new String[] {});
        try {
            if (bc.moveToFirst()) {
                for (int i = 0; i < bc.getCount(); i++) {
                    String name = bc.getString(0);
                    long version = bc.getLong(1);
                    apps.put(name, version);
                    if (!bc.moveToNext())
                        break;
                }
            }
        } finally {
            bc.close();
        }

        return apps;
    }

    public long getEmbeddedAppVersion(final String name) {
        T.dontCare();
        mGetEmbeddedApp.bindString(1, name);
        try {
            return mGetEmbeddedApp.simpleQueryForLong();
        } catch (SQLiteDoneException e) {
            return -1;
        }
    }

    public void updateEmbeddedApp(final String name, final long version) {
        T.dontCare();
        mUpdateEmbeddedApp.bindString(1, name);
        mUpdateEmbeddedApp.bindLong(2, version);
        mUpdateEmbeddedApp.execute();
    }

    public void deleteEmbeddedApp(final String name) {
        T.dontCare();
        mDeleteEmbeddedApp.bindString(1, name);
        mDeleteEmbeddedApp.execute();
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

    void insertQR(final QRCode qrCode) {
        T.UI();
        mQRInsert.bindString(1, qrCode.content);
        mQRInsert.bindString(2, qrCode.name);
        mQRInsert.execute();
    }

    void deleteQR(final QRCode qrCode) {
        T.UI();
        mQRDelete.bindString(1,qrCode.content);
        mQRDelete.bindString(2,qrCode.name);
        mQRDelete.execute();
    }

    List<QRCode> listQRs() {
        T.UI();
        List<QRCode> qrCodes = new ArrayList<>();
        final Cursor c = mDb.rawQuery(mMainService.getString(R.string.sql_qr_select), new String[] {});
        try {
            if (c.moveToFirst()) {
                do {
                    final String content = c.getString(0);
                    final String name = c.getString(1);
                    qrCodes.add(new QRCode(name, content));
                } while (c.moveToNext());
            }
        } finally {
            c.close();
        }
        return qrCodes;
    }

    public void insertEmbeddedAppTranslations(String id, String content) {
        T.dontCare();
        mEmbeddedAppTranslationsInsert.bindString(1, id);
        mEmbeddedAppTranslationsInsert.bindString(2, content);
        mEmbeddedAppTranslationsInsert.execute();
    }

    public String getEmbeddedAppTranslations(String id) {
        mEmbeddedAppTranslationsSelect.bindString(1, id);
        try {
            return mEmbeddedAppTranslationsSelect.simpleQueryForString();
        } catch (SQLiteDoneException e) {
            return null;
        }
    }
}
