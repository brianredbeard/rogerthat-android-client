/*
 * Copyright 2017 Mobicage NV
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
 * @@license_version:1.2@@
 */

package com.mobicage.rogerthat.util.db.updates;

import android.database.sqlite.SQLiteDatabase;

import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.plugins.messaging.BrandingMgr;

public class Update69 implements IDbUpdater {

    @Override
    public void preUpdate(MainService mainService, SQLiteDatabase db) {
    }

    @Override
    public void postUpdate(MainService mainService, SQLiteDatabase db) {
        mainService.registerPluginDBUpdate(BrandingMgr.class, BrandingMgr.MUST_DELETE_ATTACHMENTS_INTENT);
    }
}
