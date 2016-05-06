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

package com.mobicage.rogerthat.util.db;

import java.util.ArrayList;
import java.util.List;

import android.database.sqlite.SQLiteDatabase;

import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rpc.config.CloudConstants;

@SuppressWarnings("serial")
public class TransactionHelper {

    public static class NotInTransactionException extends RuntimeException {
    }

    public static class WrappedException extends RuntimeException {
        public Throwable wrappedThrowable;

        public WrappedException(Throwable t) {
            wrappedThrowable = t;
        }
    }

    private static ThreadLocal<List<SafeRunnable>> onCommittedRunnables = new ThreadLocal<List<SafeRunnable>>() {
        @Override
        protected java.util.List<SafeRunnable> initialValue() {
            return null;
        }
    };

    private static ThreadLocal<List<SafeRunnable>> onRollbackedRunnables = new ThreadLocal<List<SafeRunnable>>() {
        @Override
        protected java.util.List<SafeRunnable> initialValue() {
            return null;
        }
    };

    public static void runInTransaction(final SQLiteDatabase db, final String name, final TransactionWithoutResult txn) {
        runInTransaction(db, name, new Transaction<Object>() {
            @Override
            protected Object run() {
                txn.run();
                return null;
            }
        });
    }

    public static <T> T runInTransaction(final SQLiteDatabase db, final String name, final Transaction<T> txn) {
        final long start = System.currentTimeMillis();

        final boolean isNestedTransaction = onCommittedRunnables.get() != null;
        if (!isNestedTransaction) {
            onCommittedRunnables.set(new ArrayList<SafeRunnable>());
            onRollbackedRunnables.set(new ArrayList<SafeRunnable>());
        }

        boolean success = false;
        if (!isNestedTransaction)
            db.beginTransaction();
        try {
            try {
                final T result = txn.run();
                if (!isNestedTransaction)
                    db.setTransactionSuccessful();
                success = true;
                return result;
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        } finally {
            if (!isNestedTransaction) {
                db.endTransaction();
                final List<SafeRunnable> runnables = (success ? onCommittedRunnables : onRollbackedRunnables).get();
                for (SafeRunnable safeRunnable : runnables) {
                    safeRunnable.run();
                }
                onCommittedRunnables.set(null);
                onRollbackedRunnables.set(null);

                final long duration = System.currentTimeMillis() - start;
                if (duration > 5000) {
                    L.bug("Transaction with name \"" + name + "\" took " + duration + " milliseconds!");
                }

                if (CloudConstants.DEBUG_LOGGING) {
                    L.d("Finished transaction \"" + name + "\" with success=" + success + " in " + duration
                        + " milliseconds.");
                }
            }
        }
    }

    public static void onTransactionCommitted(SafeRunnable safeRunnable) {
        final List<SafeRunnable> runnables = onCommittedRunnables.get();
        if (runnables == null) {
            safeRunnable.run();
        } else {
            runnables.add(safeRunnable);
        }
    }

    public static void onTransactionRollbacked(SafeRunnable safeRunnable) {
        final List<SafeRunnable> runnables = onRollbackedRunnables.get();
        if (runnables == null) {
            throw new NotInTransactionException();
        }
        runnables.add(safeRunnable);
    }

}