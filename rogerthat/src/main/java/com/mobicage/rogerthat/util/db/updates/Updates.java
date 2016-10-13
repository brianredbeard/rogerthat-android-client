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
package com.mobicage.rogerthat.util.db.updates;

import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;

import com.mobicage.rogerthat.MainService;

public class Updates {

    protected SparseArray<IDbUpdater> mUpdates;

    public Updates() {
        mUpdates = new SparseArray<IDbUpdater>();
        initUpdates();
    }

    public void initUpdates() {
        mUpdates.put(19, new RequestFriendSetPostUpdate());
        mUpdates.put(21, new RequestFriendSetPostUpdate());
        mUpdates.put(23, new RequestFriendSetPostUpdate());
        mUpdates.put(26, new Update26());
        mUpdates.put(28, new Update28());
        mUpdates.put(32, new Update32());
        mUpdates.put(33, new RequestFriendSetPostUpdate());
        mUpdates.put(36, new RequestFriendSetPostUpdate());
        mUpdates.put(38, new Update38());
        mUpdates.put(43, new RequestFriendSetPostUpdate());
        mUpdates.put(45, new RequestIdentityPostUpdate());
        mUpdates.put(48, new RequestFriendSetPostUpdate());
        mUpdates.put(49, new RequestFriendSetPostUpdate());
        mUpdates.put(50, new RequestFriendSetPostUpdate());
        mUpdates.put(51, new RequestIdentityPostUpdate());
        mUpdates.put(55, new RequestJsEmbeddingsPostUpdate());
        mUpdates.put(57, new RequestFriendSetPostUpdate());
        mUpdates.put(58, new RequestFriendSetPostUpdate());
        mUpdates.put(59, new Update59());
        mUpdates.put(60, new Update60());
        mUpdates.put(62, new RequestJsEmbeddingsPostUpdate());
        mUpdates.put(65, new Update65());
        mUpdates.put(69, new Update69());
        mUpdates.put(71, new RequestIdentityQRCodePostUpdate());
        mUpdates.put(72, new RequestFriendSetPostUpdate());
    }

    public IDbUpdater getUpdater(int version) {
        return mUpdates.get(version, new IDbUpdater() {
            @Override
            public void preUpdate(MainService mainService, SQLiteDatabase db) {
            }

            @Override
            public void postUpdate(MainService mainService, SQLiteDatabase db) {
            }
        });
    }

}
