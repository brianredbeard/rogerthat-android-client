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
import android.support.design.widget.BottomSheetDialog;
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
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.friends.MenuItemPresser;
import com.mobicage.rogerthat.plugins.friends.Poker;
import com.mobicage.rogerthat.plugins.history.HistoryItem;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.news.NewsPlugin;
import com.mobicage.rogerthat.plugins.news.NewsStore;
import com.mobicage.rogerthat.plugins.scan.GetUserInfoResponseHandler;
import com.mobicage.rogerthat.plugins.scan.ProcessScanActivity;
import com.mobicage.rogerthat.util.CachedDownloader;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.net.NetworkConnectivityManager;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.TestUtils;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.IncompleteMessageException;
import com.mobicage.to.friends.GetUserInfoRequestTO;
import com.mobicage.to.friends.GetUserInfoResponseTO;
import com.mobicage.to.news.NewsInfoTO;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NewsActivity extends ServiceBoundCursorRecyclerActivity {

    protected NewsPlugin newsPlugin;
    protected NewsStore newsStore;
    protected FriendsPlugin friendsPlugin;

    protected SwipeRefreshLayout swipeContainer;
    protected int existence;
    protected String expectedEmailHash;
    protected String pinnedSearchQry;

    private boolean mIsConnectedToInternet = false;

    protected ProgressDialog progressDialog;

    private Set<Long> mNewNewsItems = new HashSet<>();
    private long mIdToShowAtTop = -1;

    protected CachedDownloader cachedDownloader;
    private MenuItemPresser<NewsActivity> mMenuItemPresser;
    private Poker<NewsActivity> mPoker;
    private BottomSheetDialog mBottomSheetDialog;
    private final List<BroadcastReceiver> mBroadcastReceivers = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!newsPlugin.getNews(true, false)) {
                    swipeContainer.setRefreshing(false);
                    resetUpdatesAvailable((NewsListAdapter) getAdapter(), (Button) findViewById(R.id.updates_available));
                }
            }
        });

        swipeContainer.setColorSchemeResources(R.color.mc_primary_color, R.color.mc_secondary_color);

        Intent i = getIntent();
        mIdToShowAtTop = i.getLongExtra("id", -1);
        mBottomSheetDialog = new BottomSheetDialog(this);
    }

    @Override
    protected void onPause() {
        if (swipeContainer.isRefreshing()) {
            swipeContainer.setRefreshing(false);
        }
        super.onPause();
    }

    protected void addBroadcastReceiver(BroadcastReceiver br, IntentFilter filter) {
        registerReceiver(br, filter);
        mBroadcastReceivers.add(br);
    }

    protected void setupIntentFilter() {
        final IntentFilter filter = new IntentFilter(NewsPlugin.GET_NEWS_RECEIVED_INTENT);
        filter.addAction(NewsPlugin.NEW_NEWS_ITEM_INTENT);
        filter.addAction(NewsPlugin.READ_NEWS_STATISTICS_INTENT);
        filter.addAction(NewsPlugin.ROGER_NEWS_STATISTICS_INTENT);
        filter.addAction(NewsPlugin.STATS_NEWS_STATISTICS_INTENT);
        filter.addAction(FriendsPlugin.FRIEND_INFO_RECEIVED_INTENT);
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
            } else if (NewsPlugin.READ_NEWS_STATISTICS_INTENT.equals(action)) {
                long[] ids = intent.getLongArrayExtra("ids");
                long[] reach = intent.getLongArrayExtra("reach");
                Map<Long, Long> statsMap = new HashMap<>();
                for (int i = 0; i < ids.length; i++) {
                    statsMap.put(ids[i], reach[i]);
                }
                nla.updateReachStatistics(statsMap);
            } else if (NewsPlugin.ROGER_NEWS_STATISTICS_INTENT.equals(action)) {
                long newsId = intent.getLongExtra("id", -1);
                String friendEmail = intent.getStringExtra("email");
                final Map<Long, String[]> rogeredMap = new HashMap<>();
                rogeredMap.put(newsId, new String[]{friendEmail});
                nla.updateRogerStatistics(rogeredMap);
            } else if (NewsPlugin.STATS_NEWS_STATISTICS_INTENT.equals(action)) {
                String stats = intent.getStringExtra("stats");
                JSONObject jsonObject = (JSONObject) JSONValue.parse(stats);
                final Map<Long, Long> readCountMap = new HashMap<>();
                final Map<Long, String[]> rogeredMap = new HashMap<>();
                for (Object o : jsonObject.keySet()) {
                    String key = (String) o;
                    try {
                        Long newsId = Long.parseLong(key);
                        //noinspection unchecked
                        Object v = jsonObject.get(key);
                        if (v != null) {
                            final NewsInfoTO newsInfo = new NewsInfoTO((Map<String, Object>) jsonObject.get(key));
                            readCountMap.put(newsId, newsInfo.reach);
                            rogeredMap.put(newsId, newsInfo.users_that_rogered);
                        }

                    } catch (IncompleteMessageException e) {
                        L.bug(e);
                    }
                }

                nla.updateReachStatistics(readCountMap);
                nla.updateRogerStatistics(rogeredMap);
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

    private void processNewsReceived(Intent intent, final NewsListAdapter nla) {

        if (swipeContainer.isRefreshing()) {
            swipeContainer.setRefreshing(false);
            resetUpdatesAvailable(nla, (Button) findViewById(R.id.updates_available));
        } else {
            final boolean isInitial = intent.getBooleanExtra("initial", false);
            if (!isInitial) {
                final long[] newIds = intent.getLongArrayExtra("new_ids");
                for (long newsId : newIds) {
                    mNewNewsItems.add(newsId);
                }
                if (newIds.length > 0) {
                    setupUpdatesAvailable();
                }

                final long[] updatedIds = intent.getLongArrayExtra("updated_ids");
                nla.updateNewsItems(updatedIds);
            }
        }
    }

    private void setupConnectedToInternet() {
        final LinearLayout ll = (LinearLayout) findViewById(R.id.internet_status_container);
        if (mIsConnectedToInternet) {
            ll.setVisibility(View.GONE);
            swipeContainer.setEnabled(true);
            swipeContainer.setRefreshing(true);
        } else {
            ll.setVisibility(View.VISIBLE);
            swipeContainer.setEnabled(false);
            swipeContainer.setRefreshing(false);
        }
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
        friendsPlugin = mService.getPlugin(FriendsPlugin.class);

        newsStore = newsPlugin.getStore();
        newsPlugin.resetBadgeCount();

        if (mIdToShowAtTop > 0) {
            newsPlugin.setNewsItemSortPriority(mIdToShowAtTop, NewsPlugin.SORT_PRIORITY_TOP);
        }

        cachedDownloader = CachedDownloader.getInstance(mService);

        setRecyclerView((RecyclerView) findViewById(R.id.news_list));
        loadCursorAndSetAdaptar();

        mIsConnectedToInternet = mService.getNetworkConnectivityManager().isConnected();
        if (mIsConnectedToInternet) {
            findViewById(R.id.internet_status_container).setVisibility(View.GONE);
        }

        swipeContainer.setEnabled(mIsConnectedToInternet);
        if (!TestUtils.isRunningTest() && mIsConnectedToInternet && newsPlugin.isLoadingInitial()) {
            swipeContainer.setRefreshing(true);
        }

        setupIntentFilter();
    }

    @Override
    protected void onServiceUnbound() {
        if (mMenuItemPresser != null) {
            mMenuItemPresser.stop();
        }
        if (mPoker != null) {
            mPoker.stop();
        }

        unregisterReceiver(mBroadcastReceiver);
        unregisterReceiver(getDefaultBroadcastReceiver());
        for (BroadcastReceiver br : mBroadcastReceivers) {
            unregisterReceiver(br);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (this instanceof NewsPinnedActivity) {
            // do not show no network connection in pinned news
        } else {
            if (!mIsConnectedToInternet && mService != null && mService.getNetworkConnectivityManager().isConnected()) {
                mIsConnectedToInternet = true;
                setupConnectedToInternet();
            }

            if (newsPlugin != null) {
                newsPlugin.resetBadgeCount();
            }
        }
    }

    @Override
    public void onToolbarClicked() {
        setSelection(0);
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
        mNewNewsItems = new HashSet<>();
        nla.refreshView();
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
                    item.setVisible(newsStore.countNewsPinnedItems() > 0);
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

    public void showBottomSheetDialog(View sheetView) {
        mBottomSheetDialog.setContentView(sheetView);
        mBottomSheetDialog.show();
    }

    public void dismissBottomSheetDialog() {
        mBottomSheetDialog.dismiss();
    }

    public void actionPressed(String serviceEmail, String buttonAction, String buttonUrl, String flowParams) {
        if (Message.MC_SMI_PREFIX.equals(buttonAction)) {
            if (mMenuItemPresser != null) {
                mMenuItemPresser.stop();
            }
            mMenuItemPresser = new MenuItemPresser<>(this, serviceEmail);
            mMenuItemPresser.itemPressed(buttonUrl, flowParams, null);

        } else if (Message.MC_POKE_PREFIX.equals(buttonAction)) {
            if (mPoker != null) {
                mPoker.stop();
            }
            mPoker = new Poker<>(this, serviceEmail);
            mPoker.poke(buttonUrl, null);
        }
    }

    public CachedDownloader getCachedDownloader() {
        return cachedDownloader;
    }
}
