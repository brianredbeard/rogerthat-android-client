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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.friends.MenuItemPresser;
import com.mobicage.rogerthat.plugins.friends.ServiceActionMenuActivity;
import com.mobicage.rogerthat.plugins.history.HistoryItem;
import com.mobicage.rogerthat.plugins.messaging.MembersActivity;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
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
import com.mobicage.rogerthat.util.ui.TestUtils;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rogerthat.widget.Resizable16by6ImageView;
import com.mobicage.rpc.config.CloudConstants;
import com.mobicage.to.friends.GetUserInfoRequestTO;
import com.mobicage.to.friends.GetUserInfoResponseTO;
import com.mobicage.to.news.BaseNewsItemTO;
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

    private Map<Long, Long> mDBItems = new HashMap<>();
    private List<Long> mOrder = new ArrayList<>();
    private List<Long> mLiveOrder = new ArrayList<>();
    private Map<Long, BaseNewsItemTO> mItems = new HashMap<>();
    private Map<String, ArrayList<Resizable16by6ImageView>> mImageViews = new HashMap<>();

    private ProgressDialog mProgressDialog;

    private int mExistence;
    private String mExpectedEmailHash;


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
                boolean shouldUpdateLayout = false;

                if (mSwipeContainer.isRefreshing()) {
                    mOrder = new ArrayList<>();
                    mLiveOrder = new ArrayList<>();
                    mItems = new HashMap<>();
                }

                Set<Long> idsToRequest = new LinkedHashSet<>();
                long[] ids = intent.getLongArrayExtra("ids");
                long[] versions = intent.getLongArrayExtra("versions");

                for (int i= 0 ; i < ids.length; i++) {
                    mLiveOrder.add(ids[i]);
                    if (!mDBItems.containsKey(ids[i])) {
                        idsToRequest.add(ids[i]);
                    } else if (mDBItems.get(ids[i]) < versions[i]){
                        idsToRequest.add(ids[i]);
                    } else if (!mOrder.contains(ids[i])) {
                        mItems.put(ids[i], mNewsStore.getNewsItem(ids[i]));
                        mOrder.add(ids[i]);
                        shouldUpdateLayout = true;
                    }
                }

                if (idsToRequest.size() > 0) {
                    long[] primitiveLongArray = new long[idsToRequest.size()];
                    Long[] longArray = idsToRequest.toArray(new Long[idsToRequest.size()]);
                    for (int i =0; i < longArray.length; i++) {
                        primitiveLongArray[i] = longArray[i].longValue();
                    }
                    mNewsPlugin.getNewsItems(primitiveLongArray);
                } else {
                    mSwipeContainer.setRefreshing(false);
                }

                if (shouldUpdateLayout) {
                    mListAdapter.notifyDataSetChanged();
                }

            } else if (NewsPlugin.GET_NEWS_ITEMS_RECEIVED_INTENT.equals(action)) {
                long[] ids = intent.getLongArrayExtra("ids");
                long[] versions = intent.getLongArrayExtra("versions");

                for (int i= 0 ; i < ids.length; i++) {
                    mDBItems.put(ids[i], versions[i]);
                    mOrder.add(ids[i]);
                    mItems.put(ids[i], mNewsStore.getNewsItem(ids[i]));
                }
                Collections.sort(mOrder, comparator);
                mSwipeContainer.setRefreshing(false);
                mListAdapter.notifyDataSetChanged();
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

    protected void setListAdapater() {
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
                mNewsPlugin.getNews();
            }
        });
        mSwipeContainer.setColorSchemeResources(R.color.mc_primary_color, R.color.mc_secondary_color);
        if (!TestUtils.isRunningTest()) {
            mSwipeContainer.setRefreshing(true);
        }

        mListView = (ListView) findViewById(R.id.news_list);
        setListAdapater();

        mDBItems = mNewsStore.getNewsItemVersions();
        mNewsPlugin.getNews();

        final IntentFilter filter = new IntentFilter(CachedDownloader.CACHED_DOWNLOAD_AVAILABLE_INTENT);
        filter.addAction(NewsPlugin.GET_NEWS_RECEIVED_INTENT);
        filter.addAction(NewsPlugin.GET_NEWS_ITEMS_RECEIVED_INTENT);
        filter.addAction(FriendsPlugin.FRIEND_INFO_RECEIVED_INTENT);
        registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    protected void onServiceUnbound() {
    }

    public class NewsListAdapter extends BaseAdapter {

        protected LayoutInflater mLayoutInflater;
        private final Context mContext;

        public NewsListAdapter(Context context) {
            T.UI();
            mContext = context;
            mLayoutInflater = LayoutInflater.from(mContext);
        }

        protected BaseNewsItemTO getNewsItem(int position) {
            Long newsId = mOrder.get(position);
            return mItems.get(newsId);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            T.UI();
            final View view;

            if (convertView == null) {
                view = mLayoutInflater.inflate(R.layout.news_list_item, parent, false);
            } else {
                view = convertView;
            }

            final BaseNewsItemTO newsItem = getNewsItem(position);

            LinearLayout membersContainer = (LinearLayout) view.findViewById(R.id.members_container);
            TextView members = (TextView) view.findViewById(R.id.members);

            // todo ruben remove
//            newsItem.users_that_rogered = new String[] {"pin2@gsm.gsm", "pin2@gsm.gsm", "pin2@gsm.gsm"};

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
            if (!TextUtils.isEmptyOrWhitespace(newsItem.image_url)) {
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

            // todo ruben we need to make sure the save for later pin is not hiding anything
            // android:layout_marginRight="30dp"

            TextView serviceName = (TextView) view.findViewById(R.id.service_name);
            serviceName.setText(newsItem.sender.name);

            TextView date = (TextView) view.findViewById(R.id.date);
            date.setText(TimeUtils.getDayTimeStr(NewsActivity.this, newsItem.timestamp * 1000));

            TextView title = (TextView) view.findViewById(R.id.title);
            title.setText(newsItem.title);
            if (newsItem.users_that_rogered.length == 0 && TextUtils.isEmptyOrWhitespace(newsItem.image_url)) {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) title.getLayoutParams();
                lp.setMargins(0,0,70,0);
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
                Button btn = (Button) mLayoutInflater.inflate(R.layout.news_list_item_action, parent, false);
                btn.setText(getString(R.string.rogerthat));
                btn.setOnClickListener(new SafeViewOnClickListener() {
                    @Override
                    public void safeOnClick(View v) {
                        L.i("btn click rogerthat");
                    }
                });
                actions.addView(btn);
                if (SystemUtils.isFlagEnabled(newsItem.flags, FLAG_ACTION_FOLLOW) || newsItem.buttons.length > 0) {
                    View spacer = mLayoutInflater.inflate(R.layout.news_list_item_action_spacer, parent, false);
                    actions.addView(spacer);
                }
            }

            if (SystemUtils.isFlagEnabled(newsItem.flags, FLAG_ACTION_FOLLOW)) {
                Button btn = (Button) mLayoutInflater.inflate(R.layout.news_list_item_action, parent, false);
                btn.setBackgroundColor(getResources().getColor(R.color.mc_divider_gray));
                btn.setText(getString(R.string.follow));
                btn.setOnClickListener(new SafeViewOnClickListener() {
                    @Override
                    public void safeOnClick(View v) {
                        mFriendsPlugin.inviteFriend(newsItem.sender.email, null, null, false);
                    }
                });
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
                            });
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