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

package com.mobicage.rogerthat.plugins.history;

import android.content.Intent;

import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.plugins.MobicagePlugin;
import com.mobicage.rogerthat.util.db.DatabaseManager;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.to.system.SettingsTO;

public class HistoryPlugin implements MobicagePlugin {

    @SuppressWarnings("unused")
    private final static String CONFIGKEY = "com.mobicage.rogerthat.plugins.history";

    public final static String INTENT_HISTORY_ITEM_ADDED = "com.mobicage.rogerthat.plugins.history.INTENT_HISTORY_ITEM_ADDED";
    public final static String INTENT_HISTORY_ITEM_REFERENCE_MODIFIED = "com.mobicage.rogerthat.plugins.history.INTENT_HISTORY_ITEM_REFERENCE_MODIFIED";
    public final static String INTENT_HISTORY_ITEM_DELETED = "com.mobicage.rogerthat.plugins.history.INTENT_HISTORY_ITEM_DELETED";

    private final MainService mMainService;
    private final HistoryStore mStore;

    public HistoryPlugin(final MainService service, final DatabaseManager dbManager) {
        mMainService = service;
        mStore = new HistoryStore(service, dbManager);
    }

    @Override
    public void destroy() {
        mStore.close();
    }

    @Override
    public void initialize() {
    }

    @Override
    public void processSettings(SettingsTO settings) {
    }

    @Override
    public void reconfigure() {
    }

    public HistoryStore getStore() {
        return mStore;
    }

    public void addHistoryLog(String logText, int logLevel) {
        T.dontCare();
        if (logLevel > HistoryItem.MAX_HISTORY_LOG_VALUE) {
            L.bug("Not a valid history log level: " + logLevel);
            return;
        }
        HistoryItem item = new HistoryItem();
        item.timestampMillis = System.currentTimeMillis();
        item.type = logLevel;
        item.reference = null;
        item.friendReference = null;
        item.parameters.put(HistoryItem.PARAM_LOG_LINE, logText);
        addHistoryItem(item);
    }

    public void addHistoryItem(final HistoryItem item) {
        T.dontCare();
        mMainService.postOnBIZZHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                mStore.saveHistoryItem(item);
                Intent intent = new Intent(INTENT_HISTORY_ITEM_ADDED);
                mMainService.sendBroadcast(intent);
            }
        });
    }

    public void updateHistoryItemReference(final String oldReference, final String newReference) {
        T.dontCare();
        mMainService.postOnBIZZHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                mStore.updateHistoryItemReference(oldReference, newReference);
                Intent intent = new Intent(INTENT_HISTORY_ITEM_REFERENCE_MODIFIED);
                intent.putExtra("oldReference", oldReference);
                intent.putExtra("newReference", newReference);
                mMainService.sendBroadcast(intent);
            }
        });
    }

    public void deleteHistoryItem(final String reference) {
        T.dontCare();
        mMainService.postOnBIZZHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                mStore.deleteHistoryItem(reference);
                Intent intent = new Intent(INTENT_HISTORY_ITEM_DELETED);
                intent.putExtra("reference", reference);
                mMainService.sendBroadcast(intent);
            }
        });
    }
}
