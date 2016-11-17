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
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.NewsActivity;
import com.mobicage.rogerthat.NewsListAdapter;
import com.mobicage.rogerthat.NewsPinnedActivity;
import com.mobicage.rogerthat.config.Configuration;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.plugins.MobicagePlugin;
import com.mobicage.rogerthat.util.db.DatabaseManager;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.net.NetworkConnectivityManager;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.TestUtils;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.CallReceiver;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.news.AppNewsInfoTO;
import com.mobicage.to.news.AppNewsItemTO;
import com.mobicage.to.news.GetNewsItemsRequestTO;
import com.mobicage.to.news.GetNewsRequestTO;
import com.mobicage.to.news.SaveNewsStatisticsRequestTO;
import com.mobicage.to.news.SaveNewsStatisticsResponseTO;
import com.mobicage.to.system.SettingsTO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class NewsPlugin implements MobicagePlugin, NewsChannelCallbackHandler {

    public static final String GET_NEWS_RECEIVED_INTENT = "com.mobicage.rogerthat.plugins.news.GET_NEWS_RECEIVED_INTENT";
    public static final String GET_NEWS_ITEMS_RECEIVED_INTENT = "com.mobicage.rogerthat.plugins.news.GET_NEWS_ITEMS_RECEIVED_INTENT";
    public static final String PINNED_NEWS_ITEM_INTENT = "com.mobicage.rogerthat.plugins.news.PINNED_NEWS_ITEM_INTENT";
    public static final String NEW_NEWS_ITEM_INTENT = "com.mobicage.rogerthat.plugins.news.NEW_NEWS_ITEM_INTENT";
    public static final String DISABLE_NEWS_ITEM_INTENT = "com.mobicage.rogerthat.plugins.news.DISABLE_NEWS_ITEM_INTENT";
    public static final String READ_NEWS_STATISTICS_INTENT = "com.mobicage.rogerthat.plugins.news.READ_NEWS_STATISTICS_INTENT";
    public static final String ROGER_NEWS_STATISTICS_INTENT = "com.mobicage.rogerthat.plugins.news.ROGER_NEWS_STATISTICS_INTENT";
    public static final String STATS_NEWS_STATISTICS_INTENT = "com.mobicage.rogerthat.plugins.news.STATS_NEWS_STATISTICS_INTENT";

    public static final String STATISTIC_REACH = "news.reached";
    public static final String STATISTIC_ROGERED = "news.rogered";
    public static final String STATISTIC_FOLLOWED = "news.followed";
    public static final String STATISTIC_ACTION = "news.action";

    public static final long SORT_PRIORITY_TOP = 0;
    public static final long SORT_PRIORITY_FRIEND_ROGERED = 20;
    public static final long SORT_PRIORITY_READ = 50;
    public static final long SORT_PRIORITY_BOTTOM = 127;

    private static final String CONFIGKEY = "com.mobicage.rogerthat.plugins.news";
    private static final String UPDATED_SINCE = "updated_since";
    private static final String BADGE_COUNT = "badge_count";

    private final MainService mMainService;
    private final NewsStore mStore;
    private NewsCallReceiver mNewsCallReceiver;
    private final ConfigurationProvider mConfigProvider;
    private NewsChannel mNewsChannel;
    private Timer mChannelWatchTimer;
    private boolean mIsConnectedToInternet = false;
    private final SafeBroadcastReceiver mBroadcastReceiver;

    private boolean mIsLoadingInitial;
    private String mGetNewsCursor;
    private long mUpdatedSince;
    private long mBadgeCount;
    private long mNewUpdatedSinceTimestamp;

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

        mNewsChannel = new NewsChannel(NewsPlugin.this, mConfigProvider);
        if (TestUtils.isRunningTest()) {
            mBroadcastReceiver = null;
            return;
        }
        mMainService.runOnBIZZHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                if (!mNewsChannel.isConnected() && !mNewsChannel.isTryingToReconnect()) {
                    mNewsChannel.connect();
                }
            }
        });
        connectedToNewsChannelIfNotConnected();

        getNews(true, mUpdatedSince == 0);

        mBroadcastReceiver = new SafeBroadcastReceiver() {
            @Override
            public String[] onSafeReceive(Context context, Intent intent) {
                T.UI();

                final String action = intent.getAction();
                if (NetworkConnectivityManager.INTENT_NETWORK_UP.equals(action)) {
                    if (!mIsConnectedToInternet) {
                        mIsConnectedToInternet = true;
                        getNews(true, false);
                        mMainService.postOnBIZZHandler(new SafeRunnable() {
                            @Override
                            protected void safeRun() throws Exception {
                                mNewsChannel.internetConnected();
                            }
                        });
                    }
                } else if (NetworkConnectivityManager.INTENT_NETWORK_DOWN.equals(action)) {
                    if (mIsConnectedToInternet) {
                        mIsConnectedToInternet = false;

                        mMainService.postOnBIZZHandler(new SafeRunnable() {
                            @Override
                            protected void safeRun() throws Exception {
                                mNewsChannel.internetDisconnected();
                            }
                        });
                    }
                } else {
                    L.d("Error - received unexpected intent in NewsPlugin: action=" + action);
                }
                return null;
            }
        };

        final IntentFilter filter = new IntentFilter();
        filter.addAction(NetworkConnectivityManager.INTENT_NETWORK_UP);
        filter.addAction(NetworkConnectivityManager.INTENT_NETWORK_DOWN);
        mMainService.registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    public void destroy() {
        T.UI();
        try {
            mStore.close();
        } catch (IOException e) {
            L.bug(e);
        }
        disconnectChannel();
        if (mChannelWatchTimer != null) {
            mChannelWatchTimer.cancel();
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

    public boolean getNews(boolean isRefresh, boolean initial) {
        if (isRefresh) {
            if (mGetNewsCursor != null) {
                // News is still loading
                return false;
            }
            mNewUpdatedSinceTimestamp = System.currentTimeMillis() / 1000;
            mIsLoadingInitial = initial;
        }
        SafeRunnable runnable = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                final GetNewsResponseHandler responseHandler = new GetNewsResponseHandler();

                GetNewsRequestTO request = new GetNewsRequestTO();
                request.cursor = mGetNewsCursor;
                request.updated_since = mUpdatedSince;

                com.mobicage.api.news.Rpc.getNews(responseHandler, request);
            }
        };

        if (initial) {
            mMainService.runOnBIZZHandlerNow(runnable);
        } else if (mGetNewsCursor != null) {
            mMainService.postDelayedOnBIZZHandler(runnable, 100);
        } else {
            mMainService.runOnBIZZHandler(runnable);
        }
        return true;
    }

    public void processGetNews(final String newCursor, final AppNewsInfoTO[] partialNewsItems) {
        T.BIZZ();
        Map<String, List<Long>> result = mStore.savePartialNewsItems(partialNewsItems);
        List<Long> resultNewIds = result.get("new");
        List<Long> resultUpdatedIds = result.get("updated");

        long[] newIds = new long[resultNewIds.size()];
        for (int i = 0; i < resultNewIds.size(); i++) {
            newIds[i] = resultNewIds.get(i);
        }

        long[] updatedIds = new long[resultUpdatedIds.size()];
        for (int i = 0; i < resultUpdatedIds.size(); i++) {
            updatedIds[i] = resultUpdatedIds.get(i);
        }

        Intent intent = new Intent(NewsPlugin.GET_NEWS_RECEIVED_INTENT);
        intent.putExtra("new_ids", newIds);
        intent.putExtra("updated_ids", updatedIds);
        intent.putExtra("initial", mIsLoadingInitial);
        mMainService.sendBroadcast(intent);

        if (partialNewsItems.length > 0) {
            mGetNewsCursor = newCursor;
            getNews(false, false);
        } else {
            mIsLoadingInitial = false;
            mGetNewsCursor = null;
            putUpdatedSinceTimestamp(mNewUpdatedSinceTimestamp);
        }
    }

    public void getNewsItems(final long[] ids) {
        T.dontCare();
        if (ids.length == 0)
            return;

        SafeRunnable runnable = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                final GetNewsItemsResponseHandler responseHandler = new GetNewsItemsResponseHandler();
                GetNewsItemsRequestTO request = new GetNewsItemsRequestTO();
                request.ids = ids;
                com.mobicage.api.news.Rpc.getNewsItems(responseHandler, request);
            }
        };

        mMainService.runOnBIZZHandlerNow(runnable);
    }

    public void processGetNewsItems(final AppNewsItemTO[] newsItems) {
        T.BIZZ();

        long[] ids = new long[newsItems.length];
        for (int i = 0; i < newsItems.length; i++) {
            AppNewsItemTO newsItem = newsItems[i];
            mStore.updateNewsItem(newsItem);
            ids[i] = newsItem.id;
        }

        Intent intent = new Intent(NewsPlugin.GET_NEWS_ITEMS_RECEIVED_INTENT);
        intent.putExtra("ids", ids);
        mMainService.sendBroadcast(intent);
    }

    public void setNewsItemRead(final long id) {
        SafeRunnable runnable = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                if (mStore.setNewsItemRead(id)) {
                    mNewsChannel.readNews(id);
                    saveNewsStatistics(new long[]{id}, NewsPlugin.STATISTIC_REACH);
                } else {
                    setNewsItemSortPriority(id, SORT_PRIORITY_READ);
                }
            }
        };
        mMainService.runOnBIZZHandler(runnable);
    }

    public void setNewsItemSortPriority(final long id, final long sortPriority) {
        T.dontCare();
        mStore.setNewsItemSortPriority(id, sortPriority);
    }

    public void setNewsItemPinned(final long id, final boolean isPinned) {
        mStore.setNewsItemPinned(id, isPinned);
        Intent intent = new Intent(NewsPlugin.PINNED_NEWS_ITEM_INTENT);
        intent.putExtra("id", id);
        mMainService.sendBroadcast(intent);
    }

    public void setNewsItemRogered(final long id, final String myEmail) {
        SafeRunnable runnable = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                mStore.setNewsItemRogered(id);
                mStore.addUser(id, myEmail);
                mNewsChannel.rogerNews(id);
                saveNewsStatistics(new long[]{id}, NewsPlugin.STATISTIC_ROGERED);

                Intent intent = new Intent(NewsListAdapter.NEWS_ITEM_ROGER_UPDATE_INTENT);
                intent.putExtra("id", id);
                mMainService.sendBroadcast(intent);
            }
        };
        mMainService.runOnBIZZHandler(runnable);
    }

    public void addUser(long newsId, String email) {
        T.BIZZ();
        mStore.addUser(newsId, email);
        setNewsItemSortPriority(newsId, SORT_PRIORITY_FRIEND_ROGERED);
    }

    public void saveNewsStatistics(final long[] newsIds, final String type) {
        T.dontCare();
        mMainService.runOnBIZZHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.BIZZ();
                ResponseHandler<SaveNewsStatisticsResponseTO> responseHandler = new ResponseHandler<>();
                SaveNewsStatisticsRequestTO request = new SaveNewsStatisticsRequestTO();
                request.news_ids = newsIds;
                request.type = type;
                com.mobicage.api.news.Rpc.saveNewsStatistics(responseHandler, request);
            }
        });
    }

    public void reindexSortKeys() {
        T.UI();
        mStore.reindexSortKeys();
    }

    private void putUpdatedSinceTimestamp(long updatedSince) {
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

    public List<NewsItemIndex> getNewsBefore(long sortKey, long count, String qry) {
        T.UI();
        List<NewsItemIndex> newsItems = qry == null ? mStore.getNewsBefore(sortKey, count) : mStore.getNewsBefore(sortKey, count, qry);
        loadNewsItems(newsItems);
        return newsItems;
    }

    public List<NewsItemIndex> getNewsAfter(long sortKey, long count, String qry) {
        T.UI();
        List<NewsItemIndex> newsItems = qry == null ? mStore.getNewsAfter(sortKey, count) : mStore.getNewsAfter(sortKey, count, qry);
        loadNewsItems(newsItems);
        return newsItems;
    }

    private void loadNewsItems(List<NewsItemIndex> newsItems) {
        if (TestUtils.isRunningTest()) {
            return;
        }
        List<Long> statsToRequest = new ArrayList<>();
        List<Long> itemsToRequest = new ArrayList<>();

        for (NewsItemIndex ni : newsItems) {
            statsToRequest.add(ni.id);

            if (ni.isPartial) {
                itemsToRequest.add(ni.id);
            }
        }
        mNewsChannel.statsNews(statsToRequest);

        long[] ids = new long[itemsToRequest.size()];
        for (int i = 0; i < itemsToRequest.size(); i++) {
            ids[i] = itemsToRequest.get(i);
        }
        getNewsItems(ids);
    }

    public void removeNotification(long newsId) {
        UIUtils.cancelNotification(mMainService, newsId);
    }

    // NewsChannel

    protected void connectedToNewsChannelIfNotConnected() {
        if (mChannelWatchTimer != null) {
            mChannelWatchTimer.cancel();
        }
        mChannelWatchTimer = new Timer(true);
        mChannelWatchTimer.scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        if (mIsConnectedToInternet
                                && !mNewsChannel.isConnected()
                                && !mNewsChannel.isTryingToReconnect()
                                && mNewsChannel.hasValidConfiguration()) {
                            L.d("Reconnecting to channel since it is not connected and not retrying to reconnect");
                            connectToChannel();
                        }
                    }
                },
                0,
                15000
        );
    }

    private void connectToChannel() {
        SafeRunnable runnable = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                if (!mNewsChannel.isConnected() && !mNewsChannel.isTryingToReconnect()) {
                    mNewsChannel.connect();
                }
            }
        };
        if (mMainService != null) {
            mMainService.postOnBIZZHandler(runnable);
        }
    }

    private void disconnectChannel() {
        SafeRunnable runnable = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                if (mNewsChannel.isConnected()) {
                    mNewsChannel.disconnect();
                }
            }
        };
        if (mMainService != null) {
            mMainService.postOnBIZZHandler(runnable);
        }
    }

    @Override
    public MainService getMainService() {
        return mMainService;
    }

    @Override
    public void newsPush(final AppNewsItemTO newsItem) {
        T.BIZZ();
        if (mStore.insertNewsItem(newsItem)) {
            Intent intent = new Intent(NewsPlugin.NEW_NEWS_ITEM_INTENT);
            intent.putExtra("id", newsItem.id);
            mMainService.sendBroadcast(intent);
        }
    }

    @Override
    public void newsReadUpdate(final Map<Long, Long> statsMap) {
        T.dontCare();

        long[] ids = new long[statsMap.size()];
        long[] reach = new long[statsMap.size()];

        int i = 0;
        for (Map.Entry<Long, Long> entry : statsMap.entrySet()) {
            ids[i] = entry.getKey().longValue();
            reach[i] = entry.getValue().longValue();
            i++;
        }

        Intent intent = new Intent(NewsPlugin.READ_NEWS_STATISTICS_INTENT);
        intent.putExtra("ids", ids);
        intent.putExtra("reach", reach);
        mMainService.sendBroadcast(intent);
    }

    @Override
    public void newsRogerUpdate(long newsId, String friendEmail) {
        T.dontCare();
        Intent intent = new Intent(NewsPlugin.ROGER_NEWS_STATISTICS_INTENT);
        intent.putExtra("id", newsId);
        intent.putExtra("email", friendEmail);
        mMainService.sendBroadcast(intent);
    }

    @Override
    public void newsStatsReceived(String data) {
        T.dontCare();
        Intent intent = new Intent(NewsPlugin.STATS_NEWS_STATISTICS_INTENT);
        intent.putExtra("stats", data);
        mMainService.sendBroadcast(intent);
    }
}
