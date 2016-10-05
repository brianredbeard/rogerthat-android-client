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
import java.util.UUID;

public class NewsActivity extends ServiceBoundCursorRecyclerActivity implements NewsChannelCallbackHandler {

    protected NewsPlugin newsPlugin;
    protected NewsStore newsStore;
    protected FriendsPlugin friendsPlugin;

    protected SwipeRefreshLayout swipeContainer;
    protected int existence;
    protected String expectedEmailHash;

    private boolean mIsConnectedToInternet = false;
    private String mCursor;
    private String mUUID;

    private long mNewUpdatedSinceTimestamp;
    private ProgressDialog mProgressDialog;
    private NewsChannel mNewsChannel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.loading));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestMoreNews(true);
            }
        });
        swipeContainer.setColorSchemeResources(R.color.mc_primary_color, R.color.mc_secondary_color);
    }

    protected void setupIntentFilter() {
        final IntentFilter filter = new IntentFilter(CachedDownloader.CACHED_DOWNLOAD_AVAILABLE_INTENT);
        filter.addAction(NewsPlugin.GET_NEWS_RECEIVED_INTENT);
        filter.addAction(NewsPlugin.GET_NEWS_ITEMS_RECEIVED_INTENT);
        filter.addAction(NewsPlugin.DISABLE_NEWS_ITEM_INTENT);
        filter.addAction(FriendsPlugin.FRIEND_INFO_RECEIVED_INTENT);
        filter.addAction(FriendsPlugin.SERVICE_DATA_UPDATED);
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
            } else {
                nla.handleIntent(context, intent);
            }

            return new String[]{intent.getAction()};
        }
    };

    private void processFriendInfoReceived(Intent intent) {
        if (expectedEmailHash != null && expectedEmailHash.equals(intent.getStringExtra(ProcessScanActivity.EMAILHASH))) {
            mProgressDialog.dismiss();

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

        if (swipeContainer.isRefreshing()) {
            swipeContainer.setRefreshing(false);

            if (nla.getItemCount() == 0) {
                for (NewsItemDetails item : newsStore.getNewsItemDetailsCache().values()) {
                    nla.addNewsItem(item.id, false);
                }
                nla.refreshView();
            } else {
                final Button updatesAvailable = (Button) findViewById(R.id.updates_available);
                if (ids.length < 50) {
                    updatesAvailable.setText(getString(R.string.x_new_items_available, ids.length));
                } else {
                    updatesAvailable.setText(R.string.new_items_available);
                }
                updatesAvailable.setVisibility(View.VISIBLE);

                updatesAvailable.setOnClickListener(new SafeViewOnClickListener() {
                    @Override
                    public void safeOnClick(View v) {
                        updatesAvailable.setVisibility(View.GONE);
                        for (int i = 0; i < ids.length; i++) {
                            nla.addNewsItem(ids[i], false);
                        }
                        nla.refreshView();
                    }
                });
            }

            requestMoreNews(false);
        } else {
            for (int i = 0; i < ids.length; i++) {
                nla.updateView(ids[i]);
            }
        }
    }

    private void processNewsReceived(Intent intent, final NewsListAdapter nla) {
        final String uuid = intent.getStringExtra("uuid");
        if (mUUID == null || !mUUID.equals(uuid)) {
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
                        newsStore.savePartialNewsItem(ids[i], versions[i], sortTimestamps[i], sortPriorities[i], false);
                    } else if (newsStore.getNewsItemDetails(ids[i]).version < versions[i]) {
                        idsToRequest.add(ids[i]);
                        newsStore.savePartialNewsItem(ids[i], versions[i], sortTimestamps[i], sortPriorities[i], true);
                    } else if (newsStore.getNewsItemDetails(ids[i]).isPartial) {
                        idsToRequest.add(ids[i]);
                    }
                }

                mService.postAtFrontOfUIHandler(new SafeRunnable() {
                    @Override
                    protected void safeRun() throws Exception {
                        if (swipeContainer.isRefreshing() && idsToRequest.size() > 0) {
                            long[] primitiveIdsToRequest = new long[idsToRequest.size()];
                            Long[] tmpArray1 = idsToRequest.toArray(new Long[idsToRequest.size()]);
                            for (int i = 0; i < tmpArray1.length; i++) {
                                primitiveIdsToRequest[i] = tmpArray1[i].longValue();
                            }

                            newsPlugin.getNewsItems(primitiveIdsToRequest);
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
                    if (mNewsChannel != null) {
                        mNewsChannel.internetConnected();
                    }

                }
            };
        } else {
            ll.setVisibility(View.VISIBLE);
            swipeContainer.setEnabled(false);
            swipeContainer.setRefreshing(false);
            mUUID = UUID.randomUUID().toString();
            newsRunnable = new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    if (mNewsChannel != null) {
                        mNewsChannel.internetDisconnected();
                    }

                }
            };
        }
        mService.postAtFrontOfBIZZHandler(newsRunnable);
    }

    @Override
    protected String[] getAllReceivingIntents() {
        Set<String> intents = new HashSet<>();
        intents.add(FriendsPlugin.SERVICE_DATA_UPDATED); // todo ruben should we do this?
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
        NewsListAdapter nla = new NewsListAdapter(this, mService, newsPlugin, newsStore, friendsPlugin);
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
                newsStore.fillNewsItemsCache();
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
        final ConfigurationProvider configurationProvider = mService.getConfigurationProvider();
        SafeRunnable runnable = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                mNewsChannel = new NewsChannel(NewsActivity.this, configurationProvider);
                if (!mNewsChannel.isConnected()) {
                    mNewsChannel.connect();
                }
            }
        };
        mService.postAtFrontOfBIZZHandler(runnable);

    }

    private void connectToChannel() {
        SafeRunnable runnable = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                if (mNewsChannel != null && !mNewsChannel.isConnected()) {
                    mNewsChannel.connect();
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
                if (mNewsChannel != null && mNewsChannel.isConnected()) {
                    mNewsChannel.disconnect();
                }
            }
        };
        if (mService != null) {
            mService.postAtFrontOfBIZZHandler(runnable);
        }
    }

    @Override
    protected void onServiceUnbound() {
        newsStore.clearCache();
        unregisterReceiver(mBroadcastReceiver);
        unregisterReceiver(getDefaultBroadcastReceiver());
        disconnectChannel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        connectToChannel();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disconnectChannel();
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
        mUUID = UUID.randomUUID().toString();
        newsPlugin.getNews(mCursor, mUUID);
    }

    @Override
    public void newsRogerUpdate(long newsId, String friendEmail) {
        // TODO: 04/10/16 implement
    }

    @Override
    public void newsPush(AppNewsItemTO newsItem) {
        // TODO: 04/10/16 implement
    }

    @Override
    public void newsReadUpdate(Map<Long, Long> statsMap) {
        // TODO: 04/10/16 implement

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
        mProgressDialog.show();

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
        menu.getItem(0).setIcon(new IconicsDrawable(this).icon(FontAwesome.Icon.faw_bookmark).color(Color.DKGRAY).sizeDp(18));
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