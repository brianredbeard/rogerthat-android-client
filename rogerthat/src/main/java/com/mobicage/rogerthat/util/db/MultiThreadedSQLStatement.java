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

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.mobicage.rogerthat.util.system.T;

@SuppressLint("UseSparseArrays")
public class MultiThreadedSQLStatement {

    private final Map<Integer, SQLiteStatement> mStatements;

    public MultiThreadedSQLStatement(final SQLiteDatabase db, final int[] threadIds, final String sqlString) {
        mStatements = new HashMap<Integer, SQLiteStatement>(threadIds.length);
        for (int threadId : threadIds) {
            mStatements.put(threadId, db.compileStatement(sqlString));
        }
    }

    public void close() {
        for (SQLiteStatement statement : mStatements.values())
            statement.close();
    }

    public SQLiteStatement getStatementForThisThread() {
        final int threadType = T.getThreadType();
        return mStatements.get(threadType);
    }

}
