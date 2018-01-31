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
        mUpdates.put(19, new RequestFriendSet());
        mUpdates.put(21, new RequestFriendSet());
        mUpdates.put(23, new RequestFriendSet());
        mUpdates.put(26, new UpdateMessageTimestamp());
        mUpdates.put(28, new UpdateMessageTimestamp());
        mUpdates.put(32, new RequestInvitationSecrets());
        mUpdates.put(33, new RequestFriendSet());
        mUpdates.put(36, new RequestFriendSet());
        mUpdates.put(38, new CheckIdentityShortUrl());
        mUpdates.put(43, new RequestFriendSet());
        mUpdates.put(45, new RequestIdentity());
        mUpdates.put(48, new RequestFriendSet());
        mUpdates.put(49, new RequestFriendSet());
        mUpdates.put(50, new RequestFriendSet());
        mUpdates.put(51, new RequestIdentity());
        mUpdates.put(55, new RequestJsEmbeddings());
        mUpdates.put(57, new RequestFriendSet());
        mUpdates.put(58, new RequestFriendSet());
        mUpdates.put(59, new RequestGroups());
        mUpdates.put(60, new RequestBeaconRegions());
        mUpdates.put(62, new RequestJsEmbeddings());
        mUpdates.put(65, new RequestUpdateEmailHashes());
        mUpdates.put(69, new DeleteAttachments());
        mUpdates.put(71, new RequestIdentityQRCode());
        mUpdates.put(72, new RequestFriendsAndAssets());
        mUpdates.put(79, new AddPaymentAssetColumnsIfNeeded());
        mUpdates.put(80, new MigrateFriendData());
    }

    public IDbUpdater getUpdater(int version) {
        return mUpdates.get(version, new IDbUpdater() {
            @Override
            public void postUpdate(MainService mainService, SQLiteDatabase db) {
            }
        });
    }

}
