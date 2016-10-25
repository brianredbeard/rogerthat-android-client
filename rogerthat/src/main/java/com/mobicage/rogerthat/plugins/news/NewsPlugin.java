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

import android.app.Activity;
import android.content.Intent;

import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.NewsActivity;
import com.mobicage.rogerthat.NewsPinnedActivity;
import com.mobicage.rogerthat.config.Configuration;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.plugins.MobicagePlugin;
import com.mobicage.rogerthat.util.db.DatabaseManager;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.CallReceiver;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.news.GetNewsItemsRequestTO;
import com.mobicage.to.news.GetNewsRequestTO;
import com.mobicage.to.news.SaveNewsStatisticsRequestTO;
import com.mobicage.to.news.SaveNewsStatisticsResponseTO;
import com.mobicage.to.system.SettingsTO;

import java.io.IOException;


public class NewsPlugin implements MobicagePlugin {

    public static final String GET_NEWS_RECEIVED_INTENT = "com.mobicage.rogerthat.plugins.news.GET_NEWS_RECEIVED_INTENT";
    public static final String GET_NEWS_ITEMS_RECEIVED_INTENT = "com.mobicage.rogerthat.plugins.news.GET_NEWS_ITEMS_RECEIVED_INTENT";
    public static final String PINNED_NEWS_ITEM_INTENT = "com.mobicage.rogerthat.plugins.news.PINNED_NEWS_ITEM_INTENT";
    public static final String NEW_NEWS_ITEM_INTENT = "com.mobicage.rogerthat.plugins.news.NEW_NEWS_ITEM_INTENT";
    public static final String DISABLE_NEWS_ITEM_INTENT = "com.mobicage.rogerthat.plugins.news.DISABLE_NEWS_ITEM_INTENT";

    public static final String STATISTIC_REACH = "news.reached";
    public static final String STATISTIC_ROGERED = "news.rogered";
    public static final String STATISTIC_FOLLOWED = "news.followed";
    public static final String STATISTIC_ACTION = "news.action";

    private static final String CONFIGKEY = "com.mobicage.rogerthat.plugins.news";
    private static final String UPDATED_SINCE = "updated_since";
    private static final String BADGE_COUNT = "badge_count";

    private final MainService mMainService;
    private final NewsStore mStore;
    private NewsCallReceiver mNewsCallReceiver;
    private final ConfigurationProvider mConfigProvider;

    private long mUpdatedSince;
    private long mBadgeCount;

    public NewsPlugin(final MainService pMainService, ConfigurationProvider pConfigProvider, final DatabaseManager pDatabaseManager) {
        T.UI();
        mMainService = pMainService;
        mConfigProvider = pConfigProvider;
        mStore = new NewsStore(pDatabaseManager, pMainService);

        Configuration cfg = mConfigProvider.getConfiguration(CONFIGKEY);
        mUpdatedSince = cfg.get(UPDATED_SINCE, 0);
        mBadgeCount = cfg.get(BADGE_COUNT, 0);

        mMainService.addHighPriorityIntent(GET_NEWS_RECEIVED_INTENT);
        mMainService.addHighPriorityIntent(GET_NEWS_ITEMS_RECEIVED_INTENT);
        mMainService.addHighPriorityIntent(DISABLE_NEWS_ITEM_INTENT);
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

    public void getNews(final String cursor, final String uuid) {
        SafeRunnable runnable = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                final GetNewsResponseHandler responseHandler = new GetNewsResponseHandler();
                responseHandler.setUUID(uuid);

                GetNewsRequestTO request = new GetNewsRequestTO();
                request.cursor = cursor;
                request.updated_since = mUpdatedSince;

                com.mobicage.api.news.Rpc.getNews(responseHandler, request);
            }
        };

        if (com.mobicage.rogerthat.util.system.T.getThreadType() == com.mobicage.rogerthat.util.system.T.BIZZ) {
            runnable.run();
        } else {
            mMainService.postAtFrontOfBIZZHandler(runnable);
        }
    }

    public void getNewsItems(final long[] ids, final String uuid) {
        SafeRunnable runnable = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                final GetNewsItemsResponseHandler responseHandler = new GetNewsItemsResponseHandler();
                responseHandler.setUUID(uuid);

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

    public void saveNewsStatistics(final long[] newsIds, final String type) {
        SafeRunnable runnable = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                ResponseHandler<SaveNewsStatisticsResponseTO> responseHandler = new ResponseHandler<>();
                SaveNewsStatisticsRequestTO request = new SaveNewsStatisticsRequestTO();
                request.news_ids = newsIds;
                request.type = type;
                com.mobicage.api.news.Rpc.saveNewsStatistics(responseHandler, request);
            }
        };

        if (com.mobicage.rogerthat.util.system.T.getThreadType() == com.mobicage.rogerthat.util.system.T.BIZZ) {
            runnable.run();
        } else {
            mMainService.postAtFrontOfBIZZHandler(runnable);
        }
    }

    public void putUpdatedSinceTimestamp(long updatedSince) {
        if (updatedSince > mUpdatedSince) {
            mUpdatedSince = updatedSince;
            Configuration cfg = mConfigProvider.getConfiguration(CONFIGKEY);
            cfg.put(UPDATED_SINCE, updatedSince);
            mConfigProvider.updateConfigurationNow(CONFIGKEY, cfg);
        }
    }

    public void resetBadgeCount() {
        if (mBadgeCount > 0) {
            mBadgeCount = 0;
            storeBadgeCount();
            updateBadge();
        }
    }

    public void increaseBadgeCount() {
        Activity currentActivity = UIUtils.getTopActivity(mMainService);
        if (currentActivity instanceof NewsActivity || currentActivity instanceof NewsPinnedActivity) {
            return;
        }

        mBadgeCount += 1;
        storeBadgeCount();
        updateBadge();
    }

    private void storeBadgeCount() {
        Configuration cfg = mConfigProvider.getConfiguration(CONFIGKEY);
        cfg.put(BADGE_COUNT, mBadgeCount);
        mConfigProvider.updateConfigurationNow(CONFIGKEY, cfg);
    }

    private void updateBadge() {
        Intent intent = new Intent(MainService.UPDATE_BADGE_INTENT);
        intent.putExtra("key", "news");
        intent.putExtra("count", getBadgeCount());
        mMainService.sendBroadcast(intent);
    }

    public long getBadgeCount() {
        return mBadgeCount;
    }
}
