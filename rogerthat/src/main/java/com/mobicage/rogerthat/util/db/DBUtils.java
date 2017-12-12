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
package com.mobicage.rogerthat.util.db;

import android.database.sqlite.SQLiteStatement;

import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.system.T;

public class DBUtils {

    public static void close(SQLiteStatement statement) {
        T.IO();
        if (statement != null)
            statement.close();
    }

    public static void close(SQLiteStatement... statements) {
        T.IO();
        for (SQLiteStatement statement : statements) {
            close(statement);
        }
    }

    public static void bindBoolean(SQLiteStatement statement, int position, boolean value) {
        statement.bindLong(position, value ? 1 : 0);
    }

    public static void bindString(SQLiteStatement statement, int position, String value) {
        bindString(statement, position, value, false);
    }

    public static void bindString(SQLiteStatement statement, int position, String value, boolean treatEmptyAsNull) {
        if ((treatEmptyAsNull && TextUtils.isEmptyOrWhitespace(value)) || value == null) {
            statement.bindNull(position);
        } else {
            statement.bindString(position, value);
        }
    }

}
