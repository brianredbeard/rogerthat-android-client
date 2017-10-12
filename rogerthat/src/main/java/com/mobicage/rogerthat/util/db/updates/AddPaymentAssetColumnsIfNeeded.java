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

package com.mobicage.rogerthat.util.db.updates;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mobicage.rogerthat.MainService;

public class AddPaymentAssetColumnsIfNeeded implements IDbUpdater {

    @Override
    public void postUpdate(MainService mainService, SQLiteDatabase db) {
        if (!columnExists(db, "payment_asset", "available_balance")) {
            db.execSQL("ALTER TABLE payment_asset ADD COLUMN available_balance TEXT");
            db.execSQL("ALTER TABLE payment_asset ADD COLUMN total_balance TEXT");
        }
    }

    private boolean columnExists(SQLiteDatabase db, String table, String column) {
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + table + ")", null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex("name"));
                if (column.equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }

        return false;
    }
}
