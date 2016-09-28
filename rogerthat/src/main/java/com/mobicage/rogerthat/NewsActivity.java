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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.google.zxing.client.android.Contents;
import com.google.zxing.client.android.Intents;
import com.google.zxing.client.android.encode.QRCodeEncoder;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.friends.MenuItemPresser;
import com.mobicage.rogerthat.plugins.friends.ServiceActionMenuActivity;
import com.mobicage.rogerthat.plugins.history.HistoryItem;
import com.mobicage.rogerthat.plugins.messaging.MembersActivity;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.plugins.news.NewsItem;
import com.mobicage.rogerthat.plugins.news.NewsItemDetails;
import com.mobicage.rogerthat.plugins.news.NewsPlugin;
import com.mobicage.rogerthat.plugins.news.NewsStore;
import com.mobicage.rogerthat.plugins.scan.GetUserInfoResponseHandler;
import com.mobicage.rogerthat.plugins.scan.ProcessScanActivity;
import com.mobicage.rogerthat.util.CachedDownloader;
import com.mobicage.rogerthat.util.DownloadImageTask;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeDialogInterfaceOnClickListener;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.time.TimeUtils;
import com.mobicage.rogerthat.util.ui.ScaleImageView;
import com.mobicage.rogerthat.util.ui.TestUtils;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rogerthat.widget.Resizable16by6ImageView;
import com.mobicage.rpc.config.CloudConstants;
import com.mobicage.to.friends.GetUserInfoRequestTO;
import com.mobicage.to.friends.GetUserInfoResponseTO;
import com.mobicage.to.news.NewsActionButtonTO;

import org.json.simple.JSONValue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class NewsActivity extends ServiceBoundActivity {

    protected NewsListAdapter mListAdapter;
    protected ListView mListView;
    protected final static int FLAG_ACTION_ROGERTHAT = 1;
    protected final static int FLAG_ACTION_FOLLOW = 2;

    private SwipeRefreshLayout mSwipeContainer;
    private NewsPlugin mNewsPlugin;
    private NewsStore mNewsStore;
    private MessagingPlugin mMessagingPlugin;
    private FriendsPlugin mFriendsPlugin;
    private CachedDownloader mCachedDownloader;

    private int mDisplayWidth;
    private String mMyEmail;
    private String mMyName;
    private Map<Long, NewsItemDetails> mDBItems = new HashMap<>();
    private List<Long> mOrder = new ArrayList<>();
    private List<Long> mLiveOrder = new ArrayList<>();
    private boolean mShowPinnedOnly = false;
    private List<Long> mPinnedItems = new ArrayList<>();
    private Map<Long, NewsItem> mItems = new HashMap<>();
    private Map<String, Bitmap> mQRCodes = new HashMap<>();
    private Map<String, ArrayList<Resizable16by6ImageView>> mImageViews = new HashMap<>();

    private ProgressDialog mProgressDialog;

    private int mExistence;
    private String mExpectedEmailHash;

    private boolean mShouldLoadMoreNews = false;
    private String mUUID;
    private String mCursor;

    private int mScrollPositionIndex = -1;
    private int mScrollPositionTop = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news);
        setActivityName("news");
        setTitle(R.string.news);
    }

    private final BroadcastReceiver mBroadcastReceiver = new SafeBroadcastReceiver() {
        @Override
        public String[] onSafeReceive(Context context, Intent intent) {
            T.UI();
            String action = intent.getAction();
            if (CachedDownloader.CACHED_DOWNLOAD_AVAILABLE_INTENT.equals(action)) {
                String url = intent.getStringExtra("url");

                File cachedFile = mCachedDownloader.getCachedFilePath(url);
                if (cachedFile != null) {
                    Bitmap bm = BitmapFactory.decodeFile(cachedFile.getAbsolutePath());

                    for (Resizable16by6ImageView image : mImageViews.get(url)) {
                        image.setImageBitmap(bm);
                        image.setVisibility(View.VISIBLE);
                    }
                }
            } else if (NewsPlugin.GET_NEWS_RECEIVED_INTENT.equals(action)) {
                String uuid = intent.getStringExtra("uuid");
                if (mUUID == null || !mUUID.equals(uuid)) {
                    L.i("Ignoring GET_NEWS_RECEIVED_INTENT uuid did not match");
                    return new String[] { action };
                }

                long[] ids = intent.getLongArrayExtra("ids");
                long[] versions = intent.getLongArrayExtra("versions");
                mCursor = intent.getStringExtra("cursor");

                boolean shouldUpdateLayout = false;

                if (mSwipeContainer.isRefreshing()) {
                    mOrder = new ArrayList<>();
                    mLiveOrder = new ArrayList<>();
                }

                Set<Long> idsToRequest = new LinkedHashSet<>();
                Set<Long> updatedIds = new LinkedHashSet<>();
                for (int i= 0 ; i < ids.length; i++) {
                    mLiveOrder.add(ids[i]);
                    if (!mDBItems.containsKey(ids[i])) {
                        idsToRequest.add(ids[i]);
                    } else if (mDBItems.get(ids[i]).version < versions[i]){
                        idsToRequest.add(ids[i]);
                        updatedIds.add(ids[i]);
                    } else if (mDBItems.get(ids[i]).deleted) {
                        // news item was removed
                    } else if (!mOrder.contains(ids[i])) {
                        mItems.put(ids[i], mNewsStore.getNewsItem(ids[i]));
                        mOrder.add(ids[i]);
                        shouldUpdateLayout = true;
                    }
                }

                if (idsToRequest.size() > 0) {
                    long[] primitiveIdsToRequest = new long[idsToRequest.size()];
                    Long[] tmpArray1 = idsToRequest.toArray(new Long[idsToRequest.size()]);
                    for (int i =0; i < tmpArray1.length; i++) {
                        primitiveIdsToRequest[i] = tmpArray1[i].longValue();
                    }

                    mNewsPlugin.getNewsItems(primitiveIdsToRequest, updatedIds);
                } else {
                    mSwipeContainer.setRefreshing(false);
                    mShouldLoadMoreNews = ids.length > 0;
                }

                if (shouldUpdateLayout) {
                    mListAdapter.notifyDataSetChanged();
                }

            } else if (NewsPlugin.GET_NEWS_ITEMS_RECEIVED_INTENT.equals(action)) {
                long[] ids = intent.getLongArrayExtra("ids");

                for (int i= 0 ; i < ids.length; i++) {
                    NewsItem item = mNewsStore.getNewsItem(ids[i]);

                    NewsItemDetails d = new NewsItemDetails();
                    d.id = item.id;
                    d.version = item.version;
                    d.dirty = item.dirty;
                    d.pinned = item.pinned;
                    d.rogered = item.rogered;
                    d.deleted = item.deleted;
                    mDBItems.put(item.id, d);

                    if (item.deleted) {
                        mOrder.remove(item.id);
                    } else {
                        if (!mOrder.contains(item.id)) {
                            mOrder.add(item.id);
                        }
                    }

                    mItems.put(item.id, item);
                }
                Collections.sort(mOrder, comparator);
                mSwipeContainer.setRefreshing(false);

                mShouldLoadMoreNews = true;
                mListAdapter.notifyDataSetChanged();

            } else if (NewsPlugin.DELETE_NEWS_ITEM_INTENT.equals(action)) {
                long id = intent.getLongExtra("id", -1);
                if (id > 0) {
                    if (mDBItems.containsKey(id)) {
                        mDBItems.get(id).deleted = true;
                    }
                    if (mItems.containsKey(id)) {
                        mItems.get(id).deleted = true;
                    }

                    if (mPinnedItems.contains(id)) {
                        mPinnedItems.remove(id);
                        if (mPinnedItems.size() == 0) {
                            invalidateOptionsMenu();
                        }
                    }

                    mLiveOrder.remove(id);
                    if (mOrder.remove(id)) {
                        mListAdapter.notifyDataSetChanged();
                    }
                }

            } else if (FriendsPlugin.FRIEND_INFO_RECEIVED_INTENT.equals(action)) {
                if (mExpectedEmailHash != null && mExpectedEmailHash.equals(intent.getStringExtra(ProcessScanActivity.EMAILHASH))) {
                    mProgressDialog.dismiss();

                    if (intent.getBooleanExtra(ProcessScanActivity.SUCCESS, true)) {
                        Intent launchIntent = new Intent(NewsActivity.this, ServiceDetailActivity.class);
                        if (mExistence == Friend.DELETED || mExistence == Friend.DELETION_PENDING) {
                            launchIntent.putExtra(ServiceDetailActivity.EXISTENCE, Friend.NOT_FOUND);
                        } else {
                            launchIntent.putExtra(ServiceDetailActivity.EXISTENCE, mExistence);
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
            } else {
                mListAdapter.notifyDataSetChanged();
            }
            return new String[] { action };
        }
    };

    protected void setListAdapter() {
        mListAdapter = new NewsListAdapter(this);
        mListView.setAdapter(mListAdapter);
    }

    @Override
    protected void onServiceBound() {
        mNewsPlugin = mService.getPlugin(NewsPlugin.class);
        mNewsStore = mNewsPlugin.getStore();
        mMessagingPlugin = mService.getPlugin(MessagingPlugin.class);
        mFriendsPlugin = mService.getPlugin(FriendsPlugin.class);
        mCachedDownloader = CachedDownloader.getInstance(getMainService());

        MyIdentity myIdentity = mService.getIdentityStore().getIdentity();
        mMyEmail = myIdentity.getEmail();
        mMyName = myIdentity.getDisplayName();

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(mService.getString(R.string.loading));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);


        mSwipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestMoreNews(true);
            }
        });
        mSwipeContainer.setColorSchemeResources(R.color.mc_primary_color, R.color.mc_secondary_color);
        if (!TestUtils.isRunningTest()) {
            mSwipeContainer.setRefreshing(true);
        }

        mDisplayWidth = UIUtils.getDisplayWidth(this);

        mListView = (ListView) findViewById(R.id.news_list);
        setListAdapter();

        mService.postAtFrontOfBIZZHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                final Map<Long, NewsItemDetails> dbItems = mNewsStore.getNewsItemVersions();
                final List<Long> pinnedItems = new ArrayList<>();
                for (NewsItemDetails d : dbItems.values()) {
                    if (d.pinned && !d.deleted) {
                        pinnedItems.add(d.id);
                    }
                }
                mService.postAtFrontOfUIHandler(new SafeRunnable() {
                    @Override
                    protected void safeRun() throws Exception {
                        mDBItems = dbItems;
                        mPinnedItems = pinnedItems;
                        invalidateOptionsMenu();
                    }
                });
            }
        });

        requestMoreNews(true);

        final IntentFilter filter = new IntentFilter(CachedDownloader.CACHED_DOWNLOAD_AVAILABLE_INTENT);
        filter.addAction(NewsPlugin.GET_NEWS_RECEIVED_INTENT);
        filter.addAction(NewsPlugin.GET_NEWS_ITEMS_RECEIVED_INTENT);
        filter.addAction(NewsPlugin.DELETE_NEWS_ITEM_INTENT);
        filter.addAction(FriendsPlugin.FRIEND_INFO_RECEIVED_INTENT);
        filter.addAction(FriendsPlugin.FRIEND_UPDATE_INTENT);
        filter.addAction(FriendsPlugin.FRIEND_AVATAR_CHANGED_INTENT);
        filter.addAction(FriendsPlugin.FRIEND_REMOVED_INTENT);
        filter.addAction(FriendsPlugin.FRIEND_MARKED_FOR_REMOVAL_INTENT);
        filter.addAction(FriendsPlugin.FRIEND_ADDED_INTENT);
        filter.addAction(FriendsPlugin.FRIENDS_LIST_REFRESHED);
        registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    protected void onServiceUnbound() {
    }

    private void requestMoreNews(boolean isRefresh) {
        mShouldLoadMoreNews = false;
        if (isRefresh) {
            mCursor = null;
        }
        mUUID = UUID.randomUUID().toString();
        mNewsPlugin.getNews(mCursor, mUUID);
    }

    public class NewsListAdapter extends BaseAdapter {

        protected LayoutInflater mLayoutInflater;
        private final Context mContext;

        public NewsListAdapter(Context context) {
            T.UI();
            mContext = context;
            mLayoutInflater = LayoutInflater.from(mContext);
        }

        protected NewsItem getNewsItem(int position) {
            if (mShowPinnedOnly) {
                return mItems.get(mPinnedItems.get(position));
            }
            return mItems.get(mOrder.get(position));
        }

        private void setRogeredUsers(final NewsItem newsItem, final View view) {
            LinearLayout membersContainer = (LinearLayout) view.findViewById(R.id.members_container);
            TextView members = (TextView) view.findViewById(R.id.members);

            if (newsItem.users_that_rogered.length > 0) {
                Map<String, String> namesMap = new HashMap<>();
                for (String email : newsItem.users_that_rogered) {
                    if (mMyEmail.equals(email)) {
                        namesMap.put(email, mMyName);

                    } else {
                        String name = mFriendsPlugin.getStore().getName(email);
                        if (name != null) {
                            namesMap.put(email, name);
                        }
                    }
                }

                List<String> names = new ArrayList<>();
                for (Map.Entry<String, String> entry : namesMap.entrySet()) {
                    if (!mMyEmail.equals(entry.getKey())) {
                        names.add(entry.getValue());
                    }
                }
                if (namesMap.containsKey(mMyEmail)) {
                    names.add(mMyName);
                }

                if (names.size() > 2) {
                    final SpannableString text = new SpannableString(getString(R.string.news_members_and_x_others, names.get(0), names.size() - 1));
                    text.setSpan(new StyleSpan(Typeface.BOLD), 0, names.get(0).length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    members.setText(text);

                } else {
                    String namesPart = android.text.TextUtils.join(" & ", names);
                    final SpannableString text = new SpannableString(getString(R.string.news_members, namesPart));
                    text.setSpan(new StyleSpan(Typeface.BOLD), 0, namesPart.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    members.setText(text);
                }

                membersContainer.setVisibility(View.VISIBLE);
                membersContainer.setOnClickListener(new SafeViewOnClickListener() {
                    @Override
                    public void safeOnClick(View v) {
                        Intent intent = new Intent(NewsActivity.this, MembersActivity.class);
                        intent.putExtra(MembersActivity.ME, mMyEmail);
                        intent.putExtra(MembersActivity.MEMBERS, newsItem.users_that_rogered);
                        startActivity(intent);
                    }
                });
            } else {
                membersContainer.setVisibility(View.GONE);
            }
        }

        private void togglePinned(final NewsItem newsItem) {
            final boolean isPinned = !newsItem.pinned;
            newsItem.pinned = isPinned;
            mDBItems.get(newsItem.id).pinned = isPinned;

            mService.postAtFrontOfBIZZHandler(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    mNewsStore.setNewsItemPinned(newsItem.id, isPinned);
                }
            });

            if (isPinned) {
                mPinnedItems.add(newsItem.id);
                if (mPinnedItems.size() == 1) {
                    invalidateOptionsMenu();
                }
            } else {
                mPinnedItems.remove(newsItem.id);
                if (mShowPinnedOnly && mPinnedItems.size() > 0) {
                    mListAdapter.notifyDataSetChanged();
                } else if (mShowPinnedOnly && mPinnedItems.size() == 0) {
                    toggleShowPinnedOnly();
                } else if (mPinnedItems.size() == 0) {
                    invalidateOptionsMenu();
                }
            }
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            T.UI();
            final View view;
            if (!mShowPinnedOnly && position >= mOrder.size() - 10) {
                if (mCursor != null && mShouldLoadMoreNews) {
                    requestMoreNews(false);
                }
            }

            if (convertView == null) {
                view = mLayoutInflater.inflate(R.layout.news_list_item, parent, false);
            } else {
                view = convertView;
            }

            final NewsItem newsItem = getNewsItem(position);
            if (!newsItem.dirty) {
                newsItem.dirty = true;
                mDBItems.get(newsItem.id).dirty = true;

                mService.postAtFrontOfBIZZHandler(new SafeRunnable() {
                    @Override
                    protected void safeRun() throws Exception {
                        mNewsStore.setNewsItemDirty(newsItem.id);
                    }
                });

                mNewsPlugin.newsRead(new long[] { newsItem.id });
            }

            final ImageButton pinned = (ImageButton) view.findViewById(R.id.pinned);
            pinned.setOnClickListener(new SafeViewOnClickListener() {
                @Override
                public void safeOnClick(View v) {
                    LayoutInflater layoutInflater = getLayoutInflater();
                    final LinearLayout dialog = (LinearLayout) layoutInflater.inflate(R.layout.news_actions, null);

                    final AlertDialog alertDialog = new AlertDialog.Builder(NewsActivity.this)
                            .setView(dialog)
                            .create();

                    if (newsItem.pinned) {
                        final View actionUnSave = layoutInflater.inflate(R.layout.news_actions_item, null);
                        ((ImageView) actionUnSave.findViewById(R.id.icon)).setImageDrawable(new IconicsDrawable(NewsActivity.this, FontAwesome.Icon.faw_bookmark).color(getResources().getColor(R.color.mc_default_text)).sizeDp(15).paddingDp(2));
                        ((TextView) actionUnSave.findViewById(R.id.title)).setText(R.string.unsave);
                        ((TextView) actionUnSave.findViewById(R.id.subtitle)).setText(R.string.remove_this_from_your_saved_items);
                        actionUnSave.setOnClickListener(new SafeViewOnClickListener() {
                            @Override
                            public void safeOnClick(View v) {
                                alertDialog.dismiss();
                                togglePinned(newsItem);
                            }
                        });
                        dialog.addView(actionUnSave);
                    } else {
                        final View actionSave = layoutInflater.inflate(R.layout.news_actions_item, null);
                        ((ImageView) actionSave.findViewById(R.id.icon)).setImageDrawable(new IconicsDrawable(NewsActivity.this, FontAwesome.Icon.faw_bookmark).color(getResources().getColor(R.color.mc_default_text)).sizeDp(15).paddingDp(2));
                        ((TextView) actionSave.findViewById(R.id.title)).setText(R.string.save);
                        ((TextView) actionSave.findViewById(R.id.subtitle)).setText(R.string.add_this_to_your_saved_items);
                        actionSave.setOnClickListener(new SafeViewOnClickListener() {
                            @Override
                            public void safeOnClick(View v) {
                                alertDialog.dismiss();
                                togglePinned(newsItem);
                            }
                        });
                        dialog.addView(actionSave);
                    }

                    final View actionHide = layoutInflater.inflate(R.layout.news_actions_item, null);
                    ((ImageView) actionHide.findViewById(R.id.icon)).setImageDrawable(new IconicsDrawable(NewsActivity.this, FontAwesome.Icon.faw_times_circle).color(getResources().getColor(R.color.mc_default_text)).sizeDp(15).paddingDp(2));
                    ((TextView) actionHide.findViewById(R.id.title)).setText(R.string.hide);
                    ((TextView) actionHide.findViewById(R.id.subtitle)).setText(R.string.see_fewer_posts_like_this);
                    actionHide.setOnClickListener(new SafeViewOnClickListener() {
                        @Override
                        public void safeOnClick(View v) {
                            alertDialog.dismiss();
                            L.i("todo ruben see fewer posts like this");
                        }
                    });
                    dialog.addView(actionHide);


                    alertDialog.setCanceledOnTouchOutside(true);
                    alertDialog.show();

                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                    Window window = alertDialog.getWindow();
                    lp.copyFrom(window.getAttributes());
                    lp.gravity = Gravity.BOTTOM;
                    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                    lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                    window.setAttributes(lp);
                }
            });

            setRogeredUsers(newsItem, view);

            Resizable16by6ImageView image = (Resizable16by6ImageView) view.findViewById(R.id.image);

            if (TextUtils.isEmptyOrWhitespace(newsItem.image_url)) {
                image.setVisibility(View.GONE);
            } else {
                if (mCachedDownloader.isStorageAvailable()) {
                    File cachedFile = mCachedDownloader.getCachedFilePath(newsItem.image_url);
                    if (cachedFile != null) {
                        Bitmap bm = BitmapFactory.decodeFile(cachedFile.getAbsolutePath());
                        image.setImageBitmap(bm);
                        image.setVisibility(View.VISIBLE);
                    } else {
                        if (!mImageViews.containsKey(newsItem.image_url)) {
                            mImageViews.put(newsItem.image_url, new ArrayList<Resizable16by6ImageView>());
                        }
                        mImageViews.get(newsItem.image_url).add(image);
                        // item started downloading intent when ready
                    }
                } else {
                    new DownloadImageTask(image).execute(newsItem.image_url);
                }
            }

            LinearLayout qrCodeContainer = (LinearLayout) view.findViewById(R.id.qr_code_container);

            if (newsItem.type == NewsItem.TYPE_QR_CODE) {
                ScaleImageView qrCode = (ScaleImageView) view.findViewById(R.id.qr_code);
                TextView qrCodeCaption = (TextView) view.findViewById(R.id.qr_code_caption);

                if (!mQRCodes.containsKey(newsItem.qr_code_content)) {
                    try {
                        Intent intent = new Intent();
                        intent.setAction(Intents.Encode.ACTION);
                        intent.putExtra(Intents.Encode.TYPE, Contents.Type.TEXT);
                        intent.putExtra(Intents.Encode.DATA, newsItem.qr_code_content);
                        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(NewsActivity.this, intent, mDisplayWidth / 2, false);
                        Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
                        mQRCodes.put(newsItem.qr_code_content, bitmap);

                    } catch (WriterException e) {
                        L.e(e);
                    }
                }

                if (mQRCodes.containsKey(newsItem.qr_code_content)) {
                    qrCode.setImageBitmap(mQRCodes.get(newsItem.qr_code_content));
                    qrCodeCaption.setText(newsItem.qr_code_caption);
                    qrCodeContainer.setVisibility(View.VISIBLE);

                    if (newsItem.users_that_rogered.length == 0 && TextUtils.isEmptyOrWhitespace(newsItem.image_url)) {
                        qrCodeCaption.setPadding(0, 0, UIUtils.convertDipToPixels(NewsActivity.this, 35), UIUtils.convertDipToPixels(NewsActivity.this, 15));
                    }
                } else {
                    qrCodeContainer.setVisibility(View.GONE);
                }

            } else {
                qrCodeContainer.setVisibility(View.GONE);
            }

            ImageView serviceAvatar = (ImageView) view.findViewById(R.id.service_avatar);
            Bitmap avatar = mFriendsPlugin.getStore().getAvatarBitmap(newsItem.sender.email);
            if (avatar == null) {
                new DownloadImageTask(serviceAvatar, true).execute(CloudConstants.CACHED_AVATAR_URL_PREFIX + newsItem.sender.avatar_id);
            } else {
                serviceAvatar.setImageBitmap(avatar);
            }

            final int existenceStatus = mFriendsPlugin.getStore().getExistence(newsItem.sender.email);

            serviceAvatar.setOnClickListener(new SafeViewOnClickListener() {
                @Override
                public void safeOnClick(View v) {
                    if (existenceStatus == Friend.ACTIVE) {
                        Intent intent = new Intent(NewsActivity.this, ServiceActionMenuActivity.class);
                        intent.putExtra(ServiceActionMenuActivity.SERVICE_EMAIL, newsItem.sender.email);
                        intent.putExtra(ServiceActionMenuActivity.MENU_PAGE, 0);
                        startActivity(intent);
                    } else if (existenceStatus == Friend.INVITE_PENDING) {
                        Intent intent = new Intent(NewsActivity.this, ServiceDetailActivity.class);
                        intent.putExtra(ServiceDetailActivity.EXISTENCE, existenceStatus);
                        intent.putExtra(ServiceDetailActivity.EMAIL, newsItem.sender.email);
                        startActivity(intent);

                    } else {
                        mExistence = existenceStatus;
                        mExpectedEmailHash = newsItem.sender.email;
                        requestFriendInfoByEmailHash(mExpectedEmailHash);
                    }
                }
            });

            TextView serviceName = (TextView) view.findViewById(R.id.service_name);
            serviceName.setText(newsItem.sender.name);

            TextView date = (TextView) view.findViewById(R.id.date);
            date.setText(TimeUtils.getDayTimeStr(NewsActivity.this, newsItem.timestamp * 1000));

            TextView title = (TextView) view.findViewById(R.id.title);
            title.setText(newsItem.title);
            if (newsItem.users_that_rogered.length == 0 &&
                    TextUtils.isEmptyOrWhitespace(newsItem.image_url) &&
                    qrCodeContainer.getVisibility() == View.GONE) {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) title.getLayoutParams();
                lp.setMargins(0, 0, UIUtils.convertDipToPixels(NewsActivity.this, 35), 0);
                title.setLayoutParams(lp);
            }

            TextView text = (TextView) view.findViewById(R.id.text);
            if (TextUtils.isEmptyOrWhitespace(newsItem.message)) {
                text.setVisibility(View.GONE);
            } else {
                text.setVisibility(View.VISIBLE);
                text.setText(newsItem.message);
            }
            TextView reach = (TextView) view.findViewById(R.id.reach);
            reach.setText(newsItem.reach + "");
            TextView label = (TextView) view.findViewById(R.id.label);
            label.setText("[" + newsItem.label + "]");

            LinearLayout actions = (LinearLayout) view.findViewById(R.id.actions);
            actions.removeAllViews();

            if (SystemUtils.isFlagEnabled(newsItem.flags, FLAG_ACTION_ROGERTHAT)) {
                final Button btn = (Button) mLayoutInflater.inflate(R.layout.news_list_item_action, parent, false);
                btn.setText(getString(R.string.rogerthat));

                if (newsItem.rogered) {
                    btn.setBackgroundColor(getResources().getColor(R.color.mc_divider_gray));
                } else {
                    btn.setBackgroundColor(getResources().getColor(R.color.mc_primary_color));
                    btn.setOnClickListener(new SafeViewOnClickListener() {
                        @Override
                        public void safeOnClick(View v) {
                            mDBItems.get(newsItem.id).rogered = true;
                            mNewsPlugin.newsRogered(newsItem.id);

                            mService.postAtFrontOfBIZZHandler(new SafeRunnable() {
                                @Override
                                protected void safeRun() throws Exception {
                                    mNewsStore.setNewsItemRogered(newsItem.id);
                                    mNewsStore.addUser(newsItem.id, mMyEmail);

                                    final NewsItem ni = mNewsStore.getNewsItem(newsItem.id);
                                    mService.postAtFrontOfUIHandler(new SafeRunnable() {
                                        @Override
                                        protected void safeRun() throws Exception {
                                            mItems.put(newsItem.id, ni);
                                            mListAdapter.getView(position, convertView, parent);
                                        }
                                    });
                                }
                            });
                        }
                    });
                }

                actions.addView(btn);
                if (SystemUtils.isFlagEnabled(newsItem.flags, FLAG_ACTION_FOLLOW) || newsItem.buttons.length > 0) {
                    View spacer = mLayoutInflater.inflate(R.layout.news_list_item_action_spacer, parent, false);
                    actions.addView(spacer);
                }
            }

            if (SystemUtils.isFlagEnabled(newsItem.flags, FLAG_ACTION_FOLLOW)) {
                final Button btn = (Button) mLayoutInflater.inflate(R.layout.news_list_item_action, parent, false);
                btn.setText(getString(R.string.follow));


                if (Friend.ACTIVE == existenceStatus) {
                    btn.setBackgroundColor(getResources().getColor(R.color.mc_divider_gray));
                } else {
                    btn.setBackgroundColor(getResources().getColor(R.color.mc_primary_color));
                    btn.setOnClickListener(new SafeViewOnClickListener() {
                        @Override
                        public void safeOnClick(View v) {
                            mFriendsPlugin.inviteFriend(newsItem.sender.email, null, null, false);
                            btn.setBackgroundColor(getResources().getColor(R.color.mc_divider_gray));
                            btn.setOnClickListener(null);
                        }
                    });
                }
                actions.addView(btn);

                if (newsItem.buttons.length > 0) {
                    View spacer = mLayoutInflater.inflate(R.layout.news_list_item_action_spacer, parent, false);
                    actions.addView(spacer);
                }
            }

            for (int i = 0; i < newsItem.buttons.length; i++) {
                final NewsActionButtonTO button = newsItem.buttons[i];

                Map<String, String> actionInfo = mMessagingPlugin.getButtonActionInfo(button);
                final String buttonAction = actionInfo.get("androidAction");
                final String buttonUrl = actionInfo.get("androidUrl");

                Button btn = (Button) mLayoutInflater.inflate(R.layout.news_list_item_action, parent, false);
                btn.setText(button.caption);

                if (Friend.ACTIVE == existenceStatus) {
                    btn.setOnClickListener(new SafeViewOnClickListener() {
                        @Override
                        public void safeOnClick(View v) {
                            if (Message.MC_CONFIRM_PREFIX.equals(buttonAction)) {
                                // ignore
                            } else if (Message.MC_SMI_PREFIX.equals(buttonAction)) {
                                MenuItemPresser menuItemPresser = new MenuItemPresser(NewsActivity.this, newsItem.sender.email);
                                menuItemPresser.itemPressed(buttonUrl, new MenuItemPresser.ResultHandler() {
                                    @Override
                                    public void onSuccess() {
                                        //overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_up);
                                    }

                                    @Override
                                    public void onError() {
                                        L.e("SMI with hash " + buttonUrl + " not found!"); // XXX: log error to message.sender
                                    }

                                    @Override
                                    public void onTimeout() {
                                    }
                                }, button.flow_params);
                            } else {
                                if (buttonAction != null) {
                                    final Intent intent = new Intent(buttonAction, Uri.parse(buttonUrl));
                                    startActivity(intent);
                                }
                            }
                        }
                    });
                } else {
                    btn.setOnClickListener(new SafeViewOnClickListener() {
                        @Override
                        public void safeOnClick(View v) {
                            new AlertDialog.Builder(NewsActivity.this)
                                    .setMessage(getString(R.string.invite_as_friend, new Object[]{newsItem.sender.name}))
                                    .setPositiveButton(R.string.yes, new SafeDialogInterfaceOnClickListener() {
                                        @Override
                                        public void safeOnClick(DialogInterface dialog, int which) {
                                            mFriendsPlugin.inviteFriend(newsItem.sender.email, null, null, true);
                                        }
                                    }).setNegativeButton(R.string.no, null).create().show();
                        }
                    });
                }

                actions.addView(btn);

                if ( newsItem.buttons.length > i + 1) {
                    View spacer = mLayoutInflater.inflate(R.layout.news_list_item_action_spacer, parent, false);
                    actions.addView(spacer);
                }
            }

            return view;
        }

        @Override
        public int getCount() {
            if (mShowPinnedOnly) {
                return mPinnedItems.size();
            }
            return mOrder.size();
        }

        @Override
        public Object getItem(int position) {
            if (mShowPinnedOnly) {
                return mPinnedItems.get(position);
            }
            return mOrder.get(position);
        }

        @Override
        public long getItemId(int position) {
            if (mShowPinnedOnly) {
                return mPinnedItems.get(position);
            }
            return mOrder.get(position);
        }
    }

    private final Comparator<Long> comparator = new Comparator<Long>() {
        @Override
        public int compare(Long item1, Long item2) {
            int position1 = mLiveOrder.indexOf(item1);
            int position2 = mLiveOrder.indexOf(item2);
            return position1 > position2 ? 1 : -1;
        }
    };

    private final Comparator<Long> comparatorPinned = new Comparator<Long>() {
        @Override
        public int compare(Long item1, Long item2) {
            long timestamp1 = mItems.get(item1).timestamp;
            long timestamp2 = mItems.get(item2).timestamp;
            return timestamp1 < timestamp2 ? 1 : -1;
        }
    };

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

    private void requestFriendInfoByEmailHash(String emailHash) {
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
                    item.setVisible(!mShowPinnedOnly && mPinnedItems.size() > 0);
                    break;
                case R.id.all:
                    item.setVisible(mShowPinnedOnly);
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
        menu.getItem(1).setIcon(new IconicsDrawable(this).icon(FontAwesome.Icon.faw_times).color(Color.DKGRAY).sizeDp(18));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        T.UI();
        switch (item.getItemId()) {
            case R.id.saved:
                toggleShowPinnedOnly();
                return true;
            case R.id.all:
                toggleShowPinnedOnly();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mShowPinnedOnly) {
            toggleShowPinnedOnly();
            return;
        }
        super.onBackPressed();
    }

    private void toggleShowPinnedOnly() {
        Collections.sort(mPinnedItems, comparatorPinned);

        if (mShowPinnedOnly) {
            mSwipeContainer.setEnabled(true);
            mShowPinnedOnly = false;
            setTitle(R.string.news);
            mListAdapter.notifyDataSetChanged();

            if (mScrollPositionIndex != -1)
                mListView.setSelectionFromTop(mScrollPositionIndex, mScrollPositionTop);
        } else {
            mScrollPositionIndex = mListView.getFirstVisiblePosition();
            View v = mListView.getChildAt(0);
            mScrollPositionTop = (v == null) ? 0 : v.getTop();

            mSwipeContainer.setRefreshing(false);
            mSwipeContainer.setEnabled(false);
            mShowPinnedOnly = true;
            setTitle(R.string.saved_items);
            mListAdapter.notifyDataSetChanged();

            mListView.setSelection(0);
        }

        invalidateOptionsMenu();
    }
}