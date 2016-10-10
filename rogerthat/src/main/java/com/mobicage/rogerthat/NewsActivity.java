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
package com.mobicage.rogerthat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.history.HistoryItem;
import com.mobicage.rogerthat.plugins.news.NewsChannel;
import com.mobicage.rogerthat.plugins.news.NewsChannelCallbackHandler;
import com.mobicage.rogerthat.plugins.news.NewsItemDetails;
import com.mobicage.rogerthat.plugins.news.NewsPlugin;
import com.mobicage.rogerthat.plugins.news.NewsStore;
import com.mobicage.rogerthat.plugins.scan.GetUserInfoResponseHandler;
import com.mobicage.rogerthat.plugins.scan.ProcessScanActivity;
import com.mobicage.rogerthat.util.CachedDownloader;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.net.NetworkConnectivityManager;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.TestUtils;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.to.friends.GetUserInfoRequestTO;
import com.mobicage.to.friends.GetUserInfoResponseTO;
import com.mobicage.to.news.AppNewsItemTO;

import org.json.simple.JSONValue;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class NewsActivity extends ServiceBoundCursorRecyclerActivity implements NewsChannelCallbackHandler {

    protected NewsPlugin newsPlugin;
    protected NewsStore newsStore;
    protected FriendsPlugin friendsPlugin;
    protected NewsChannel newsChannel;

    protected SwipeRefreshLayout swipeContainer;
    protected int existence;
    protected String expectedEmailHash;

    private boolean mIsConnectedToInternet = false;
    private String mCursor;
    private String mRequestNewsUUID;
    private String mRequestNewsItemsUUID;

    private long mNewUpdatedSinceTimestamp;
    protected ProgressDialog progressDialog;

    private Set<Long> mNewNewsItems = new HashSet<>();
    private Timer mChannelWatchTimer;

    protected long idToShowAtTop = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestMoreNews(true);
            }
        });
        swipeContainer.setColorSchemeResources(R.color.mc_primary_color, R.color.mc_secondary_color);

        Intent i = getIntent();
        idToShowAtTop = i.getLongExtra("id", -1);
    }

    protected void setupIntentFilter() {
        final IntentFilter filter = new IntentFilter(CachedDownloader.CACHED_DOWNLOAD_AVAILABLE_INTENT);
        filter.addAction(NewsPlugin.GET_NEWS_RECEIVED_INTENT);
        filter.addAction(NewsPlugin.GET_NEWS_ITEMS_RECEIVED_INTENT);
        filter.addAction(NewsPlugin.PINNED_NEWS_ITEM_INTENT);
        filter.addAction(NewsPlugin.NEW_NEWS_ITEM_INTENT);
        filter.addAction(NewsPlugin.DISABLE_NEWS_ITEM_INTENT);
        filter.addAction(FriendsPlugin.FRIEND_INFO_RECEIVED_INTENT);
        filter.addAction(FriendsPlugin.SERVICE_DATA_UPDATED);
        filter.addAction(FriendsPlugin.FRIEND_REMOVED_INTENT);
        filter.addAction(FriendsPlugin.FRIEND_MARKED_FOR_REMOVAL_INTENT);
        filter.addAction(FriendsPlugin.FRIEND_ADDED_INTENT);
        filter.addAction(FriendsPlugin.FRIENDS_LIST_REFRESHED);
        filter.addAction(NetworkConnectivityManager.INTENT_NETWORK_UP);
        filter.addAction(NetworkConnectivityManager.INTENT_NETWORK_DOWN);
        registerReceiver(mBroadcastReceiver, filter);

        IntentFilter filterCursorList = new IntentFilter();
        for (String action : getAllReceivingIntents())
            filterCursorList.addAction(action);
        registerReceiver(getDefaultBroadcastReceiver(), filterCursorList);
    }

    protected final BroadcastReceiver mBroadcastReceiver = new SafeBroadcastReceiver() {
        @Override
        public String[] onSafeReceive(Context context, Intent intent) {
            T.UI();
            final NewsListAdapter nla = ((NewsListAdapter) getAdapter());

            String action = intent.getAction();
            if (NewsPlugin.GET_NEWS_RECEIVED_INTENT.equals(action)) {
                processNewsReceived(intent, nla);

            } else if (NewsPlugin.GET_NEWS_ITEMS_RECEIVED_INTENT.equals(action)) {
                processNewsItemsReceived(intent, nla);

            } else if (FriendsPlugin.FRIEND_INFO_RECEIVED_INTENT.equals(action)) {
                processFriendInfoReceived(intent);
            } else if (NetworkConnectivityManager.INTENT_NETWORK_UP.equals(action)) {
                if (!mIsConnectedToInternet) {
                    mIsConnectedToInternet = true;
                    setupConnectedToInternet();
                }
            } else if (NetworkConnectivityManager.INTENT_NETWORK_DOWN.equals(action)) {
                if (mIsConnectedToInternet) {
                    mIsConnectedToInternet = false;
                    setupConnectedToInternet();
                }
            } else if (NewsPlugin.NEW_NEWS_ITEM_INTENT.equals(action)) {
                mNewNewsItems.add(intent.getLongExtra("id", -1));
                setupUpdatesAvailable();
            } else {
                nla.handleIntent(context, intent);
            }

            return new String[]{intent.getAction()};
        }
    };

    private void processFriendInfoReceived(Intent intent) {
        if (expectedEmailHash != null && expectedEmailHash.equals(intent.getStringExtra(ProcessScanActivity.EMAILHASH))) {
            progressDialog.dismiss();

            if (intent.getBooleanExtra(ProcessScanActivity.SUCCESS, true)) {
                Intent launchIntent = new Intent(NewsActivity.this, ServiceDetailActivity.class);
                if (existence == Friend.DELETED || existence == Friend.DELETION_PENDING) {
                    launchIntent.putExtra(ServiceDetailActivity.EXISTENCE, Friend.NOT_FOUND);
                } else {
                    launchIntent.putExtra(ServiceDetailActivity.EXISTENCE, existence);
                }

                GetUserInfoResponseTO item = new GetUserInfoResponseTO();
                item.avatar = intent.getStringExtra(ProcessScanActivity.AVATAR);
                item.avatar_id = -1;
                item.description = intent.getStringExtra(ProcessScanActivity.DESCRIPTION);
                item.descriptionBranding = intent.getStringExtra(ProcessScanActivity.DESCRIPTION_BRANDING);
                item.email = intent.getStringExtra(ProcessScanActivity.EMAIL);
                item.name = intent.getStringExtra(ProcessScanActivity.NAME);
                item.qualifiedIdentifier = intent.getStringExtra(ProcessScanActivity.QUALIFIED_IDENTIFIER);
                item.type = intent.getLongExtra(ProcessScanActivity.TYPE, FriendsPlugin.FRIEND_TYPE_SERVICE);
                launchIntent.putExtra(ServiceDetailActivity.GET_USER_INFO_RESULT, JSONValue.toJSONString(item.toJSONMap()));
                startActivity(launchIntent);
            } else {
                showError(intent);
            }
        }
    }

    private void processNewsItemsReceived(Intent intent, final NewsListAdapter nla) {
        final long[] ids = intent.getLongArrayExtra("ids");

        final String uuid = intent.getStringExtra("uuid");
        if (mRequestNewsItemsUUID != null && mRequestNewsItemsUUID.equals(uuid)) {
            mRequestNewsItemsUUID = null;
            swipeContainer.setRefreshing(false);

            if (nla.getItemCount() == 0) {
                for (NewsItemDetails item : newsStore.getNewsItemDetailsCache().values()) {
                    nla.addNewsItem(item.id, false);
                }
            }
            resetUpdatesAvailable(nla, (Button) findViewById(R.id.updates_available));
            requestMoreNews(false);
        } else {
            for (int i = 0; i < ids.length; i++) {
                nla.updateView(ids[i]);
            }
        }
    }

    private void processNewsReceived(Intent intent, final NewsListAdapter nla) {
        final String uuid = intent.getStringExtra("uuid");
        if (mRequestNewsUUID == null || !mRequestNewsUUID.equals(uuid)) {
            return;
        }

        final long[] ids = intent.getLongArrayExtra("ids");
        final long[] versions = intent.getLongArrayExtra("versions");
        final long[] sortTimestamps = intent.getLongArrayExtra("sort_timestamps");
        final long[] sortPriorities = intent.getLongArrayExtra("sort_priorities");
        mCursor = intent.getStringExtra("cursor");

        mService.postAtFrontOfBIZZHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                final Set<Long> idsToRequest = new LinkedHashSet<>();
                for (int i = 0; i < ids.length; i++) {
                    if (newsStore.getNewsItemDetails(ids[i]) == null) {
                        idsToRequest.add(ids[i]);
                        mNewNewsItems.add(ids[i]);
                        newsStore.savePartialNewsItem(ids[i], versions[i], sortTimestamps[i], sortPriorities[i]);
                    } else if (newsStore.getNewsItemDetails(ids[i]).version < versions[i]) {
                        idsToRequest.add(ids[i]);
                        newsStore.savePartialNewsItem(ids[i], versions[i], sortTimestamps[i], sortPriorities[i]);
                    } else if (newsStore.getNewsItemDetails(ids[i]).isPartial) {
                        idsToRequest.add(ids[i]);
                    }
                }

                mService.postAtFrontOfUIHandler(new SafeRunnable() {
                    @Override
                    protected void safeRun() throws Exception {
                        if (swipeContainer.isRefreshing()) {
                            if (idsToRequest.size() > 0) {
                                long[] primitiveIdsToRequest = new long[idsToRequest.size()];
                                Long[] tmpArray1 = idsToRequest.toArray(new Long[idsToRequest.size()]);
                                for (int i = 0; i < tmpArray1.length; i++) {
                                    primitiveIdsToRequest[i] = tmpArray1[i].longValue();
                                }
                                mRequestNewsItemsUUID = UUID.randomUUID().toString();
                                newsPlugin.getNewsItems(primitiveIdsToRequest, mRequestNewsItemsUUID);
                            } else {
                                swipeContainer.setRefreshing(false);
                                resetUpdatesAvailable(nla, (Button) findViewById(R.id.updates_available));

                                if (ids.length > 0) {
                                    requestMoreNews(false);
                                }
                            }
                        } else {
                            swipeContainer.setRefreshing(false);
                            if (idsToRequest.size() > 0) {
                                for (int i = 0; i < ids.length; i++) {
                                    nla.addNewsItem(ids[i], true);
                                }
                            }

                            if (ids.length > 0) {
                                requestMoreNews(false);
                            } else {
                                newsPlugin.putUpdatedSinceTimestamp(mNewUpdatedSinceTimestamp);
                            }
                        }
                    }
                });
            }
        });
    }

    private void setupConnectedToInternet() {
        final LinearLayout ll = (LinearLayout) findViewById(R.id.internet_status_container);
        SafeRunnable newsRunnable;
        if (mIsConnectedToInternet) {
            ll.setVisibility(View.GONE);
            swipeContainer.setEnabled(true);
            swipeContainer.setRefreshing(true);
            requestMoreNews(true);
            newsRunnable = new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    if (newsChannel != null) {
                        newsChannel.internetConnected();
                    }
                }
            };
        } else {
            ll.setVisibility(View.VISIBLE);
            swipeContainer.setEnabled(false);
            swipeContainer.setRefreshing(false);
            mRequestNewsUUID = UUID.randomUUID().toString();
            newsRunnable = new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    if (newsChannel != null) {
                        newsChannel.internetDisconnected();
                    }
                }
            };
        }
        mService.postAtFrontOfBIZZHandler(newsRunnable);
    }

    @Override
    protected String[] getAllReceivingIntents() {
        Set<String> intents = new HashSet<>();
        return intents.toArray(new String[intents.size()]);
    }


    @Override
    protected void changeCursor() {
        if (mServiceIsBound) {
            NewsListAdapter nla = ((NewsListAdapter) getAdapter());
            nla.refreshView();
        }
    }

    protected void loadCursorAndSetAdaptar() {
        NewsListAdapter nla = new NewsListAdapter(this, mService);
        setAdapter(nla);
    }

    @Override
    protected void onServiceBound() {
        setActivityName("news");
        setTitle(R.string.news);

        newsPlugin = mService.getPlugin(NewsPlugin.class);
        newsStore = newsPlugin.getStore();
        friendsPlugin = mService.getPlugin(FriendsPlugin.class);

        setRecyclerView((RecyclerView) findViewById(R.id.news_list));
        loadCursorAndSetAdaptar();

        mService.postAtFrontOfBIZZHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                newsStore.fillNewsItemDetailsCache();
                mService.postAtFrontOfUIHandler(new SafeRunnable() {
                    @Override
                    protected void safeRun() throws Exception {
                        if (newsStore.countNewsItems() > 0) {
                            NewsListAdapter nla = ((NewsListAdapter) getAdapter());
                            for (NewsItemDetails item : newsStore.getNewsItemDetailsCache().values()) {
                                nla.addNewsItem(item.id, false);
                            }
                            nla.refreshView();
                        }
                    }
                });
            }
        });

        mIsConnectedToInternet = mService.getNetworkConnectivityManager().isConnected();
        if (mIsConnectedToInternet) {
            findViewById(R.id.internet_status_container).setVisibility(View.GONE);
        }

        swipeContainer.setEnabled(mIsConnectedToInternet);
        if (!TestUtils.isRunningTest() && mIsConnectedToInternet) {
            swipeContainer.setRefreshing(true);
            requestMoreNews(true);
        }

        setupIntentFilter();

        if (this instanceof NewsPinnedActivity) {
            L.d("not subscribing to news when in NewsPinnedActivity");
        } else {
            final ConfigurationProvider configurationProvider = mService.getConfigurationProvider();
            SafeRunnable runnable = new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    newsChannel = new NewsChannel(NewsActivity.this, configurationProvider);
                    if (!newsChannel.isConnected() && !newsChannel.isTryingToReconnect()) {
                        newsChannel.connect();
                    }
                }
            };
            mService.postAtFrontOfBIZZHandler(runnable);

            connectedToNewsChannelIfNotConnected();
        }
    }

    private void connectedToNewsChannelIfNotConnected(){
        if(mChannelWatchTimer != null){
            mChannelWatchTimer.cancel();
        }
        mChannelWatchTimer = new Timer(true);
        mChannelWatchTimer.scheduleAtFixedRate(
            new TimerTask() {
                @Override
                public void run() {
                    if (mIsConnectedToInternet && newsChannel != null && !newsChannel.isConnected() && !newsChannel.isTryingToReconnect()) {
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
                if (newsChannel != null && !newsChannel.isConnected() && !newsChannel.isTryingToReconnect()) {
                    newsChannel.connect();
                }
            }
        };
        if (mService != null) {
            mService.postAtFrontOfBIZZHandler(runnable);
        }
    }

    private void disconnectChannel() {
        SafeRunnable runnable = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                if (newsChannel != null && newsChannel.isConnected()) {
                    newsChannel.disconnect();
                }
            }
        };
        if (mService != null) {
            mService.postAtFrontOfBIZZHandler(runnable);
        }
    }

    @Override
    protected void onServiceUnbound() {
        if (this instanceof NewsPinnedActivity) {
            L.d("not clearing cache when in NewsPinnedActivity");
        } else {
            newsStore.clearCache();
        }
        unregisterReceiver(mBroadcastReceiver);
        unregisterReceiver(getDefaultBroadcastReceiver());
        disconnectChannel();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!mIsConnectedToInternet && mService != null && mService.getNetworkConnectivityManager().isConnected()) {
            mIsConnectedToInternet = true;
            setupConnectedToInternet();
        }
        connectToChannel();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disconnectChannel();
        if(mChannelWatchTimer != null){
            mChannelWatchTimer.cancel();
        }
    }

    @Override
    public void onToolbarClicked() {
        setSelection(0);
    }

    protected void requestMoreNews(boolean isRefresh) {
        if (isRefresh) {
            mCursor = null;
            mNewUpdatedSinceTimestamp = System.currentTimeMillis() / 1000;
        }
        mRequestNewsUUID = UUID.randomUUID().toString();
        newsPlugin.getNews(mCursor, mRequestNewsUUID);
    }

    private void setupUpdatesAvailable() {
        final Button updatesAvailable = (Button) findViewById(R.id.updates_available);
        if (mNewNewsItems.size() == 1) {
            updatesAvailable.setText(getString(R.string.new_item_available));
        } else if (mNewNewsItems.size() > 0 && mNewNewsItems.size() < 50) {
            updatesAvailable.setText(getString(R.string.x_new_items_available, mNewNewsItems.size()));
        } else {
            updatesAvailable.setText(R.string.new_items_available);
        }

        if (updatesAvailable.getVisibility() == View.GONE) {
            updatesAvailable.setVisibility(View.VISIBLE);

            updatesAvailable.setOnClickListener(new SafeViewOnClickListener() {
                @Override
                public void safeOnClick(View v) {
                    resetUpdatesAvailable((NewsListAdapter) getAdapter(), updatesAvailable);
                }
            });
        }
    }

    private void resetUpdatesAvailable(NewsListAdapter nla, Button updatesAvailable) {
        updatesAvailable.setVisibility(View.GONE);
        for (Long id : mNewNewsItems) {
            nla.addNewsItem(id, false);
        }
        mNewNewsItems = new HashSet<>();
        nla.refreshView();
    }

    @Override
    public void newsRogerUpdate(final long newsId, String[] friendEmails) {
        T.BIZZ();
        for(String friendEmail: friendEmails) {
            newsStore.addUser(newsId, friendEmail);
        }
        mService.postAtFrontOfUIHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                final NewsListAdapter nla = ((NewsListAdapter) getAdapter());
                nla.updateView(newsId);
            }
        });
    }

    @Override
    public void newsPush(final AppNewsItemTO newsItem) {
        T.BIZZ();
        if (newsStore.insertNewsItem(newsItem)) {
            mService.postAtFrontOfUIHandler(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    mNewNewsItems.add(newsItem.id);
                    setupUpdatesAvailable();
                }
            });
        }
    }

    @Override
    public void newsReadUpdate(final Map<Long, Long> statsMap) {
        T.BIZZ();
        mService.postAtFrontOfUIHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                final NewsListAdapter nla = ((NewsListAdapter) getAdapter());
                for (Map.Entry<Long, Long> entry : statsMap.entrySet()) {
                    if (entry.getValue() > 0) {
                        newsStore.setNewsItemReach(entry.getKey(), entry.getValue());
                        nla.updateView(entry.getKey());
                    }
                }
            }
        });
    }

    private void showErrorToast() {
        UIUtils.showLongToast(NewsActivity.this, getString(R.string.scanner_communication_failure));
    }

    private void showError(Intent intent) {
        final String errorMessage = intent.getStringExtra(ProcessScanActivity.ERROR_MESSAGE);
        if (TextUtils.isEmptyOrWhitespace(errorMessage)) {
            showErrorToast();
        } else {
            final String errorCaption = intent.getStringExtra(ProcessScanActivity.ERROR_CAPTION);
            final String errorAction = intent.getStringExtra(ProcessScanActivity.ERROR_ACTION);
            final String errorTitle = intent.getStringExtra(ProcessScanActivity.ERROR_TITLE);

            final AlertDialog.Builder builder = new AlertDialog.Builder(NewsActivity.this);
            builder.setTitle(errorTitle);
            builder.setMessage(errorMessage);
            builder.setNegativeButton(R.string.rogerthat, new AlertDialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            if (!TextUtils.isEmptyOrWhitespace(errorCaption) && !TextUtils.isEmptyOrWhitespace(errorAction)) {
                builder.setPositiveButton(errorCaption, new AlertDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(errorAction));
                        startActivity(intent);
                        dialog.dismiss();
                    }
                });
            }

            builder.show();
        }
    }

    protected void requestFriendInfoByEmailHash(String emailHash) {
        progressDialog.show();

        final GetUserInfoRequestTO request = new GetUserInfoRequestTO();
        request.code = emailHash;
        request.allow_cross_app = true;

        final GetUserInfoResponseHandler handler = new GetUserInfoResponseHandler();
        handler.setCode(emailHash);

        try {
            com.mobicage.api.friends.Rpc.getUserInfo(handler, request);
        } catch (Exception e) {
            mService.putInHistoryLog(getString(R.string.getuserinfo_failure), HistoryItem.ERROR);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            switch (item.getItemId()) {
                case R.id.saved:
                    item.setVisible(newsStore.countPinnedItems() > 0);
                    break;
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        T.UI();
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.news_menu, menu);
        menu.getItem(0).setIcon(new IconicsDrawable(this).icon(FontAwesome.Icon.faw_thumb_tack).color(Color.DKGRAY).sizeDp(18));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        T.UI();
        switch (item.getItemId()) {
            case R.id.saved:
                Intent i = new Intent(this, NewsPinnedActivity.class);
                this.startActivity(i);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}