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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private final static int FLAG_ACTION_ROGERTHAT = 1;
    private final static int FLAG_ACTION_FOLLOW = 2;

    private SwipeRefreshLayout mSwipeContainer;
    private NewsPlugin mNewsPlugin;
    private NewsStore mNewsStore;
    private MessagingPlugin mMessagingPlugin;
    private FriendsPlugin mFriendsPlugin;
    private CachedDownloader mCachedDownloader;

    private int mDisplayWidth;
    private Map<Long, NewsItemDetails> mDBItems = new HashMap<>();
    private List<Long> mOrder = new ArrayList<>();
    private List<Long> mLiveOrder = new ArrayList<>();
    private Map<Long, NewsItem> mItems = new HashMap<>();
    private Map<String, ArrayList<Resizable16by6ImageView>> mImageViews = new HashMap<>();

    private ProgressDialog mProgressDialog;

    private int mExistence;
    private String mExpectedEmailHash;

    private boolean mIsLoadingMoreNews = false;
    private boolean mShouldLoadMoreNews = false;
    private String mUUID;
    private String mCursor;


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

                    for (NewsItemDetails d : mDBItems.values()) {
                        if (d.pinned && !d.deleted) {
                            shouldUpdateLayout = true;
                            mLiveOrder.add(d.id);
                            mOrder.add(d.id);
                        }
                    }
                }

                Set<Long> idsToRequest = new LinkedHashSet<>();
                Set<Long> updatedIds = new LinkedHashSet<>();
                for (int i= 0 ; i < ids.length; i++) {
                    if (!mLiveOrder.contains(ids[i])) {
                        mLiveOrder.add(ids[i]);
                    }
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
                    mIsLoadingMoreNews = false;
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

                mIsLoadingMoreNews = false;
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

        mDBItems = mNewsStore.getNewsItemVersions();
        boolean shouldUpdateLayout = false;
        for (NewsItemDetails d : mDBItems.values()) {
            if (d.pinned && !d.deleted) {
                shouldUpdateLayout = true;
                mLiveOrder.add(d.id);
                mOrder.add(d.id);
                mItems.put(d.id, mNewsStore.getNewsItem(d.id));
            }
        }

        if (shouldUpdateLayout) {
            mListAdapter.notifyDataSetChanged();
        }
        requestMoreNews(true);

        final IntentFilter filter = new IntentFilter(CachedDownloader.CACHED_DOWNLOAD_AVAILABLE_INTENT);
        filter.addAction(NewsPlugin.GET_NEWS_RECEIVED_INTENT);
        filter.addAction(NewsPlugin.GET_NEWS_ITEMS_RECEIVED_INTENT);
        filter.addAction(NewsPlugin.DELETE_NEWS_ITEM_INTENT);
        filter.addAction(FriendsPlugin.FRIEND_INFO_RECEIVED_INTENT);
        registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    protected void onServiceUnbound() {
    }

    private void requestMoreNews(boolean isRefresh) {
        if (mIsLoadingMoreNews) {
            L.e("requestMoreNews called when already loading news");
        }
        mIsLoadingMoreNews = true;
        mShouldLoadMoreNews = false;
        if (isRefresh) {
            mCursor = null;
        }
        mUUID = UUID.randomUUID().toString();
        mNewsPlugin.getNews(mCursor, mUUID);
    }

    private void updatedPinnedLayout(ImageButton pinned, boolean isPinned) {
        if (isPinned) {
            pinned.setImageDrawable(new IconicsDrawable(this, FontAwesome.Icon.faw_thumb_tack).color(getResources().getColor(R.color.mc_primary_color)).sizeDp(16));
            pinned.setBackgroundResource(R.drawable.news_pin_background_pinned);
        } else {
            pinned.setImageDrawable(new IconicsDrawable(this, FontAwesome.Icon.faw_thumb_tack).color(Color.WHITE).sizeDp(16));
            pinned.setBackgroundResource(R.drawable.news_pin_background_normal);
        }
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
            Long newsId = mOrder.get(position);
            return mItems.get(newsId);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            T.UI();
            final View view;

            if (position >= mOrder.size() - 10) {
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

                mNewsStore.setNewsItemDirty(newsItem.id);
                mNewsPlugin.newsRead(new long[] { newsItem.id });
            }

            final ImageButton pinned = (ImageButton) view.findViewById(R.id.pinned);
            updatedPinnedLayout(pinned, newsItem.pinned);
            pinned.setOnClickListener(new SafeViewOnClickListener() {
                @Override
                public void safeOnClick(View v) {
                    boolean isPinned = !newsItem.pinned;
                    newsItem.pinned = isPinned;
                    mDBItems.get(newsItem.id).pinned = isPinned;
                    mNewsStore.setNewsItemPinned(newsItem.id, isPinned);

                    updatedPinnedLayout(pinned, isPinned);
                }
            });

            LinearLayout membersContainer = (LinearLayout) view.findViewById(R.id.members_container);
            TextView members = (TextView) view.findViewById(R.id.members);

            if (newsItem.users_that_rogered.length > 0) {
                List<String> names = new ArrayList<>();
                for (String email : newsItem.users_that_rogered) {
                    String name = mFriendsPlugin.getStore().getName(email);
                    if (name != null) {
                        names.add(name);
                    }
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
                        intent.putExtra(MembersActivity.ME, mService.getIdentityStore().getIdentity().getEmail());
                        intent.putExtra(MembersActivity.MEMBERS, newsItem.users_that_rogered);
                        startActivity(intent);
                    }
                });
            } else {
                membersContainer.setVisibility(View.GONE);
            }

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

                try {
                    Intent intent = new Intent();
                    intent.setAction(Intents.Encode.ACTION);
                    intent.putExtra(Intents.Encode.TYPE, Contents.Type.TEXT);
                    intent.putExtra(Intents.Encode.DATA, newsItem.qr_code_content);
                    QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(NewsActivity.this, intent, mDisplayWidth / 2, false);
                    Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
                    qrCode.setImageBitmap(bitmap);
                    qrCodeCaption.setText(newsItem.qr_code_caption);
                    qrCodeContainer.setVisibility(View.VISIBLE);

                    if (newsItem.users_that_rogered.length == 0 && TextUtils.isEmptyOrWhitespace(newsItem.image_url)) {
                        qrCodeCaption.setPadding(0, 0, UIUtils.convertDipToPixels(NewsActivity.this, 35), UIUtils.convertDipToPixels(NewsActivity.this, 15));
                    }

                } catch (WriterException e) {
                    qrCodeContainer.setVisibility(View.GONE);
                    L.e(e);
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

            serviceAvatar.setOnClickListener(new SafeViewOnClickListener() {
                @Override
                public void safeOnClick(View v) {
                    int existenceStatus = mFriendsPlugin.getStore().getExistence(newsItem.sender.email);
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
                            newsItem.rogered = true;
                            mDBItems.get(newsItem.id).rogered = true;
                            mNewsStore.setNewsItemRogered(newsItem.id);
                            mNewsPlugin.newsRogered(newsItem.id);

                            btn.setBackgroundColor(getResources().getColor(R.color.mc_divider_gray));
                            btn.setOnClickListener(null);
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

                int existenceStatus = mFriendsPlugin.getStore().getExistence(newsItem.sender.email);
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
            return mOrder.size();
        }

        @Override
        public Object getItem(int position) {
            return mOrder.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mOrder.get(position);
        }
    }

    private final Comparator<Long> comparator = new Comparator<Long>() {
        @Override
        public int compare(Long item1, Long item2) {
            NewsItemDetails d1 = mDBItems.get(item1);
            NewsItemDetails d2 = mDBItems.get(item2);

            if (d1.pinned && !d2.pinned) {
                return -1;
            } else if (!d1.pinned && d2.pinned) {
                return 1;
            }

            int position1 = mLiveOrder.indexOf(item1);
            int position2 = mLiveOrder.indexOf(item2);
            return position1 > position2 ? 1 : -1;
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
}