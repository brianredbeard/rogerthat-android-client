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

package com.mobicage.rogerthat.plugins.news;

import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.plugins.MobicagePlugin;

import com.mobicage.rogerthat.util.db.DatabaseManager;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.CallReceiver;

import com.mobicage.to.news.GetNewsItemsRequestTO;
import com.mobicage.to.news.GetNewsRequestTO;
import com.mobicage.to.system.SettingsTO;
import java.io.IOException;


public class NewsPlugin implements MobicagePlugin {

    public static final String GET_NEWS_RECEIVED_INTENT = "com.mobicage.rogerthat.plugins.news.GET_NEWS_RECEIVED_INTENT";
    public static final String GET_NEWS_ITEMS_RECEIVED_INTENT = "com.mobicage.rogerthat.plugins.news.GET_NEWS_ITEMS_RECEIVED_INTENT";

    private final MainService mMainService;
    private final NewsStore mStore;
    private NewsCallReceiver mNewsCallReceiver;

    public NewsPlugin(final MainService pMainService, final DatabaseManager pDatabaseManager) {
        T.UI();
        mMainService = pMainService;
        mStore = new NewsStore(pDatabaseManager, pMainService);
    }

    @Override
    public void destroy() {
        T.UI();
        try {
            mStore.close();
        } catch (IOException e) {
            L.bug(e);
        }
        CallReceiver.comMobicageCapiNewsIClientRpc = null;
    }

    @Override
    public void initialize() {
        T.UI();
        reconfigure();

        mNewsCallReceiver = new NewsCallReceiver(mMainService, this);
        CallReceiver.comMobicageCapiNewsIClientRpc = mNewsCallReceiver;
    }

    @Override
    public void reconfigure() {
        T.UI();
    }

    @Override
    public void processSettings(SettingsTO settings) {
        // not used
    }

    public NewsStore getStore() {
        return mStore;
    }

    public void getNews() {
        SafeRunnable runnable = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                final GetNewsResponseHandler responseHandler = new GetNewsResponseHandler();
                responseHandler.setUUID("todo ruben");

                GetNewsRequestTO request = new GetNewsRequestTO();
                request.cursor = null;

                com.mobicage.api.news.Rpc.getNews(responseHandler, request);
            }
        };

        if (com.mobicage.rogerthat.util.system.T.getThreadType() == com.mobicage.rogerthat.util.system.T.BIZZ) {
            runnable.run();
        } else {
            mMainService.postAtFrontOfBIZZHandler(runnable);
        }
    }

    public void getNewsItems(final long[] ids) {
        SafeRunnable runnable = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                final GetNewsItemsResponseHandler responseHandler = new GetNewsItemsResponseHandler();
                GetNewsItemsRequestTO request = new GetNewsItemsRequestTO();
                request.ids = ids;

                com.mobicage.api.news.Rpc.getNewsItems(responseHandler, request);
            }
        };

        if (com.mobicage.rogerthat.util.system.T.getThreadType() == com.mobicage.rogerthat.util.system.T.BIZZ) {
            runnable.run();
        } else {
            mMainService.postAtFrontOfBIZZHandler(runnable);
        }
    }
}