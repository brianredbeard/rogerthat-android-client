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

package com.mobicage.rogerthat.plugins.security;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.util.db.DatabaseManager;
import com.mobicage.rogerthat.util.system.T;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

import static com.mobicage.rogerthat.util.db.DBUtils.bindString;


public class SecurityStore implements Closeable {

    private final MainService mMainService;
    private final SQLiteDatabase mDb;

    private final SQLiteStatement mInsertSecurityKey;
    private final SQLiteStatement mDeleteSecurityGroup;

    public SecurityStore(final MainService mainService, final DatabaseManager dbManager) {
        T.UI();
        mMainService = mainService;
        mDb = dbManager.getDatabase();

        mInsertSecurityKey = mDb.compileStatement(mMainService.getString(R.string.sql_security_insert));
        mDeleteSecurityGroup = mDb.compileStatement(mMainService.getString(R.string.sql_security_delete_group));
    }

    @Override
    public void close() {
        T.UI();
        mInsertSecurityKey.close();
        mDeleteSecurityGroup.close();
    }

    public SQLiteDatabase getDatabase() {
        return mDb;
    }


    public void saveSecurityKey(final String type, final String algorithm, final String name, final String index,
                                final String data) {
        T.dontCare();
        bindString(mInsertSecurityKey, 1, type);
        bindString(mInsertSecurityKey, 2, algorithm);
        bindString(mInsertSecurityKey, 3, name);
        bindString(mInsertSecurityKey, 4, index);
        bindString(mInsertSecurityKey, 5, data);
        mInsertSecurityKey.execute();
    }

    public void deleteSecurityGroup(final String algorithm, final String name) {
        mDeleteSecurityGroup.bindString(1, algorithm);
        mDeleteSecurityGroup.bindString(2, name);
        mDeleteSecurityGroup.execute();
    }

    public String getSecurityKey(final String type, final String algorithm, final String name, final String index) {
        T.dontCare();
        final Cursor c = mDb.rawQuery(mMainService.getString(R.string.sql_security_select_by_key), new String[]{type,
                algorithm, name, index});
        try {
            if (!c.moveToFirst()) {
                return null;
            }
            return c.getString(0);
        } finally {
            c.close();
        }
    }

    public List<PublicKeyInfo> listPublicKeys() {
        T.UI();
        final List<PublicKeyInfo> publicKeys = new ArrayList<>();

        final Cursor c = mDb.rawQuery(mMainService.getString(R.string.sql_security_list_public_keys), new String[]{});
        try {
            if (c.moveToFirst()) {
                do {
                    PublicKeyInfo publicKey = new PublicKeyInfo();
                    publicKey.name = c.getString(0);
                    publicKey.algorithm = c.getString(1);
                    publicKeys.add(publicKey);
                } while (c.moveToNext());
            }
        } finally {
            c.close();
        }

        return publicKeys;
    }

}
