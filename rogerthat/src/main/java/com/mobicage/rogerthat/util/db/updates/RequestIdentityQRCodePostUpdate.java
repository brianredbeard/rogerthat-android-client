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

import android.database.sqlite.SQLiteDatabase;

import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;

public class RequestIdentityQRCodePostUpdate implements IDbUpdater {

    @Override
    public void preUpdate(MainService mainService, SQLiteDatabase db) {
    }

    @Override
    public void postUpdate(MainService mainService, SQLiteDatabase db) {
        mainService.registerPluginDBUpdate(FriendsPlugin.class, FriendsPlugin.FRIENDS_PLUGIN_MUST_REFRESH_IDENTITY_QR_CODE);
    }

}
