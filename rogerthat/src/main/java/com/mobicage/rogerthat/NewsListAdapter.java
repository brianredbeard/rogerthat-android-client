/*
 * Copyright 2018 GIG Technology NV
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
 * @@license_version:1.4@@
 */
package com.mobicage.rogerthat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.StyleSpan;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.zxing.client.android.Contents;
import com.google.zxing.client.android.Intents;
import com.google.zxing.client.android.encode.QRCodeEncoder;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.friends.ServiceActionMenuActivity;
import com.mobicage.rogerthat.plugins.messaging.MembersActivity;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.plugins.news.NewsItem;
import com.mobicage.rogerthat.plugins.news.NewsItemIndex;
import com.mobicage.rogerthat.plugins.news.NewsPlugin;
import com.mobicage.rogerthat.util.CachedDownloader;
import com.mobicage.rogerthat.util.DebugUtils;
import com.mobicage.rogerthat.util.DownloadImageTask;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeAsyncTask;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.time.TimeUtils;
import com.mobicage.rogerthat.util.ui.ScaleImageView;
import com.mobicage.rogerthat.util.ui.TestUtils;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.config.LookAndFeelConstants;
import com.mobicage.to.news.NewsActionButtonTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NewsListAdapter extends RecyclerView.Adapter<NewsListAdapter.ViewHolder> {

    public final static String NEWS_ITEM_ROGER_UPDATE_INTENT = "com.mobicage.rogerthat.NewsListAdapter.NEWS_ITEM_ROGER_UPDATE_INTENT";

    private final static String UPDATE_CAUSE_FULL = "full";
    private final static String UPDATE_CAUSE_FOLLOW = "follow";
    private final static String UPDATE_CAUSE_REACH = "reach";
    private final static String UPDATE_CAUSE_ROGER = "roger";

    private final static long CACHE_SIZE = 50;
    private final static long BATCH_SIZE = 15;
    private final static long BACKGROUND_FETCH_THRESHOLD = 10;

    private NewsActivity mActivity;
    private final MainService mMainService;
    private final MessagingPlugin mMessagingPlugin;

    private final String mMyEmail;
    private final String mMyName;

    private List<NewsListAdapter.ViewHolder> mViewHolders = new ArrayList<>();
    private SparseArray<NewsItemIndex> mNewsItemsByPosition = new SparseArray<>();
    private LongSparseArray<NewsItemIndex> mNewsItemsById = new LongSparseArray<>();

    private int mMinPosition;
    private int mMaxPosition;
    private final int TOP_RADIUS;

    public NewsListAdapter(NewsActivity activity, MainService mainService) {
        mActivity = activity;
        mMainService = mainService;
        mMessagingPlugin = mMainService.getPlugin(MessagingPlugin.class);

        MyIdentity myIdentity = mainService.getIdentityStore().getIdentity();
        mMyEmail = myIdentity.getEmail();
        mMyName = myIdentity.getDisplayName();
        TOP_RADIUS = UIUtils.convertDipToPixels(mActivity, 30);

        refreshView();
    }

    public void refreshView() {
        // When in pinned news never reindex
        if (!(mActivity instanceof NewsPinnedActivity)) {
            mActivity.newsPlugin.reindexSortKeys();
        }
        mNewsItemsByPosition = new SparseArray<>();
        mNewsItemsById = new LongSparseArray<>();
        mMinPosition = -1;
        mMaxPosition = -1;
        notifyDataSetChanged();
    }

    public void updateNewsItems(final long[] ids) {
        Set<Long> idsToUpdate = new HashSet<>();
        Set<Long> idsToRequest = new HashSet<>();
        for (int i = 0; i < ids.length; i++) {
            idsToUpdate.add(ids[i]);
            if (mNewsItemsById.indexOfKey(ids[i]) >= 0) {
                idsToRequest.add(ids[i]);
            }
        }

        for (ViewHolder vh : mViewHolders) {
            if (vh.mNewsItem != null && idsToUpdate.contains(vh.mNewsItem.id)) {
                vh.updateView(UPDATE_CAUSE_FULL);
            }
        }

        long[] primitiveLongArray = new long[idsToRequest.size()];
        Long[] longArray = idsToRequest.toArray(new Long[idsToRequest.size()]);
        for (int i = 0; i < longArray.length; i++) {
            primitiveLongArray[i] = longArray[i].longValue();
        }

        mActivity.newsPlugin.getNewsItems(primitiveLongArray);
    }

    public void updateReachStatistics(Map<Long, Long> statsMap) {

        for (Map.Entry<Long, Long> entry : statsMap.entrySet()) {
            if (entry.getValue() > 0) {
                NewsItemIndex ni = mNewsItemsById.get(entry.getKey());
                if (ni != null) {
                    if (ni.reach < entry.getValue()) {
                        ni.reach = entry.getValue();
                    }
                }
            }
        }

        for (ViewHolder vh : mViewHolders) {
            if (vh.mNewsItem != null) {
                vh.updateView(UPDATE_CAUSE_REACH);
            }
        }
    }

    public void updateRogerStatistics(Map<Long, String[]> statsMap) {
        Set<Long> idsToUpdate = new HashSet<>();
        for (Map.Entry<Long, String[]> entry : statsMap.entrySet()) {
            NewsItemIndex ni = mNewsItemsById.get(entry.getKey());
            if (ni != null) {
                for (String friendEmail : entry.getValue()) {
                    if (!ni.usersThatRogered.contains(friendEmail)) {
                        idsToUpdate.add(ni.id);
                        mActivity.newsPlugin.addUser(ni.id, friendEmail);
                        ni.usersThatRogered.add(friendEmail);
                    }
                }
            }
        }

        for (ViewHolder vh : mViewHolders) {
            if (vh.mNewsItem != null && idsToUpdate.contains(vh.mNewsItem.id)) {
                vh.updateView(UPDATE_CAUSE_ROGER);
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final View mPartialItem;
        private final View mFullItem;
        private final ImageButton mPinButton;
        private final ImageButton mDropdownButton;
        private final LinearLayout mMembersContainer;
        private final TextView mMembers;
        private final ImageView mImage;
        private final View mQRCodeContainer;
        private final ScaleImageView mQRCode;
        private final TextView mQRCodeCaption;
        private final ImageView mServiceAvatar;
        private final TextView mServiceName;
        private final TextView mDate;
        private final TextView mTitle;
        private final LinearLayout mDetails;
        private final TextView mText;
        private final TextView mReadmore;
        private final TextView mReach;
        private final ProgressBar mReachSpinner;
        private final TextView mBroadcastType;
        private final LinearLayout mActions;

        private final MainService mMainService;
        private final NewsActivity mActivity;
        private final NewsListAdapter mNewsListAdapter;
        private final LayoutInflater mLayoutInflater;
        private final NewsPlugin mNewsPlugin;
        private final FriendsPlugin mFriendsPlugin;
        private final MessagingPlugin mMessagingPlugin;
        private int mPosition;
        private NewsItem mNewsItem;
        private NewsItemIndex mNewsItemIndex;
        private final String mMyEmail;
        private final String mMyName;
        private final int TOP_RADIUS;

        private SafeRunnable mExecuteAfterBecameFriends;

        protected final BroadcastReceiver mBroadcastReceiver = new SafeBroadcastReceiver() {
            @Override
            public String[] onSafeReceive(Context context, Intent intent) {
                T.UI();
                if (mNewsItem == null) {
                    return null;
                }
                String action = intent.getAction();
                if (CachedDownloader.CACHED_DOWNLOAD_AVAILABLE_INTENT.equals(action)) {
                    String url = intent.getStringExtra("url");

                    if (url.equals(mNewsItem.image_url)) {
                        new DownloadImageTask(mActivity.getCachedDownloader(), mImage, mNewsItemIndex.usersThatRogered.size() == 0, mActivity, TOP_RADIUS).execute(url);
                        return null;
                    }
                    if (url.equals(mNewsItem.getAvatarUrl())) {
                        new DownloadImageTask(mActivity.getCachedDownloader(), mServiceAvatar, true, mActivity, 0).execute(mNewsItem.getAvatarUrl());
                    }

                    return null;
                } else if (NewsPlugin.PINNED_NEWS_ITEM_INTENT.equals(action)) {
                    if (mActivity instanceof NewsPinnedActivity) {
                        if (mActivity.newsStore.countNewsPinnedItems() > 0) {
                            mNewsListAdapter.refreshView();
                        } else {
                            mActivity.finish();
                        }
                    } else {
                        long newsId = intent.getLongExtra("id", -1);
                        if (mNewsItem.id == newsId) {
                            updateView();
                        }
                        mActivity.supportInvalidateOptionsMenu();
                    }
                } else if (NewsPlugin.DISABLE_NEWS_ITEM_INTENT.equals(action)) {
                    long newsId = intent.getLongExtra("id", -1);
                    if (mNewsItem.id == newsId) {
                        updateView();
                    }
                } else if (FriendsPlugin.SERVICE_DATA_UPDATED.equals(action)) {
                    if (mNewsItem.isPartial)
                        return null;

                    String email = intent.getStringExtra("email");
                    if (mNewsItem.sender.email.equals(email)) {
                        updateView();
                    }

                } else if (FriendsPlugin.FRIEND_REMOVED_INTENT.equals(action) || FriendsPlugin.FRIEND_MARKED_FOR_REMOVAL_INTENT.equals(action)) {
                    if (mNewsItem.isPartial)
                        return null;

                    String email = intent.getStringExtra("email");
                    if (mNewsItem.sender.email.equals(email)) {
                        updateView();
                    }
                } else if (FriendsPlugin.FRIEND_ADDED_INTENT.equals(action)) {
                    if (mNewsItem.isPartial)
                        return null;

                    String email = intent.getStringExtra("email");

                    if (mActivity.expectedEmailHash != null && mActivity.expectedEmailHash.equals(email)) {
                        final int existence = mActivity.friendsPlugin.getStore().getExistence(email);
                        if (Friend.ACTIVE == existence) {
                            mActivity.mProgressDialog.dismiss();
                            if (mExecuteAfterBecameFriends != null) {
                                mExecuteAfterBecameFriends.run();
                            }
                        }
                    }

                    if (mNewsItem.sender.email.equals(email)) {
                        updateView();
                    }

                } else if (FriendsPlugin.FRIENDS_LIST_REFRESHED.equals(action)) {
                    updateView(UPDATE_CAUSE_FOLLOW);
                } else if (NewsPlugin.GET_NEWS_ITEMS_RECEIVED_INTENT.equals(action)) {
                    // TODO: check id
                    updateView();
                } else if (NEWS_ITEM_ROGER_UPDATE_INTENT.equals(action)) {
                    long newsId = intent.getLongExtra("id", -1);
                    if (mNewsItem.id == newsId) {
                        updateView(UPDATE_CAUSE_ROGER);
                    }
                }

                return new String[]{intent.getAction()};
            }
        };

        public ViewHolder(MainService mainService, NewsActivity context, NewsListAdapter nla,
                          View itemView, NewsPlugin newsPlugin, FriendsPlugin friendsPlugin,
                          MessagingPlugin messagingPlugin, String myEmail, String myName, int topRadius) {
            super(itemView);

            mPartialItem = itemView.findViewById(R.id.partial_item);
            mFullItem = itemView.findViewById(R.id.full_item);
            mPinButton = (ImageButton) itemView.findViewById(R.id.pin_button);
            mDropdownButton = (ImageButton) itemView.findViewById(R.id.dropdown_button);
            mMembersContainer = (LinearLayout) itemView.findViewById(R.id.members_container);
            mMembers = (TextView) itemView.findViewById(R.id.members);
            mImage = (ImageView) itemView.findViewById(R.id.image);
            mQRCodeContainer = itemView.findViewById(R.id.qr_code_container);
            mQRCode = (ScaleImageView) itemView.findViewById(R.id.qr_code);
            mQRCodeCaption = (TextView) itemView.findViewById(R.id.qr_code_caption);
            mServiceAvatar = (ImageView) itemView.findViewById(R.id.service_avatar);
            mServiceName = (TextView) itemView.findViewById(R.id.service_name);
            mDate = (TextView) itemView.findViewById(R.id.date);
            mTitle = (TextView) itemView.findViewById(R.id.title);
            mDetails = (LinearLayout) itemView.findViewById(R.id.details);
            mText = (TextView) itemView.findViewById(R.id.text);
            mReadmore = (TextView) itemView.findViewById(R.id.readmore);
            mReach = (TextView) itemView.findViewById(R.id.reach);
            mReachSpinner = (ProgressBar) itemView.findViewById(R.id.reach_spinner);
            mBroadcastType = (TextView) itemView.findViewById(R.id.broadcast_type);
            mActions = (LinearLayout) itemView.findViewById(R.id.actions);

            mMainService = mainService;
            mActivity = context;
            mNewsListAdapter = nla;
            mLayoutInflater = mActivity.getLayoutInflater();
            mNewsPlugin = newsPlugin;
            mFriendsPlugin = friendsPlugin;
            mMessagingPlugin = messagingPlugin;
            mMyEmail = myEmail;
            mMyName = myName;

            mPinButton.setOnClickListener(new SafeViewOnClickListener() {
                @Override
                public void safeOnClick(View v) {
                    togglePinned();
                }
            });

            mReadmore.setOnClickListener(new SafeViewOnClickListener() {
                @Override
                public void safeOnClick(View v) {
                    int lineCount = mText.getLineCount();
                    mText.setMaxLines(lineCount);
                    mReadmore.setVisibility(View.GONE);
                    mActivity.setSelection(mPosition);
                }
            });

            TOP_RADIUS = topRadius;

            final IntentFilter filter = new IntentFilter(CachedDownloader.CACHED_DOWNLOAD_AVAILABLE_INTENT);
            filter.addAction(NewsPlugin.PINNED_NEWS_ITEM_INTENT);
            filter.addAction(NewsPlugin.DISABLE_NEWS_ITEM_INTENT);
            filter.addAction(FriendsPlugin.SERVICE_DATA_UPDATED);
            filter.addAction(FriendsPlugin.FRIEND_REMOVED_INTENT);
            filter.addAction(FriendsPlugin.FRIEND_MARKED_FOR_REMOVAL_INTENT);
            filter.addAction(FriendsPlugin.FRIEND_ADDED_INTENT);
            filter.addAction(FriendsPlugin.FRIENDS_LIST_REFRESHED);
            filter.addAction(NewsPlugin.GET_NEWS_ITEMS_RECEIVED_INTENT);
            filter.addAction(NEWS_ITEM_ROGER_UPDATE_INTENT);
            mActivity.addBroadcastReceiver(mBroadcastReceiver, filter);

            setVisibility(false);

            // Colours
            int primaryColor = LookAndFeelConstants.getPrimaryColor(mMainService);
            mTitle.setTextColor(primaryColor);
            mPinButton.setImageDrawable(new IconicsDrawable(mMainService, FontAwesome.Icon.faw_thumb_tack).color
                    (primaryColor)
                    .sizeDp(16));
            mDropdownButton.setImageDrawable(new IconicsDrawable(mMainService, FontAwesome.Icon.faw_angle_down).color
                    (primaryColor)
                    .sizeDp(16));
            UIUtils.setColors(mMainService, mReachSpinner);
        }

        public void setNewsItem(final int position, final NewsItem newsItem, final NewsItemIndex ni) {
            mPosition = position;
            mNewsItem = newsItem;
            mNewsItemIndex = ni;

            populateView();
        }

        private void updateView() {
            updateView(UPDATE_CAUSE_FULL);
        }

        private void updateView(String updateType) {
            if (mNewsItem == null) {
                setVisibility(false);
                return;
            }

            if (UPDATE_CAUSE_FULL.equals(updateType)) {
                updateNewsItem();
                populateView();
            } else if (UPDATE_CAUSE_FOLLOW.equals(updateType)) {
                if (mNewsItem.isPartial)
                    return;
                final int existenceStatus = mFriendsPlugin.getStore().getExistence(mNewsItem.sender.email);
                setupButtons(existenceStatus);
            } else if (UPDATE_CAUSE_REACH.equals(updateType)) {
                if (mNewsItem.isPartial)
                    return;
                updateReach();
            } else if (UPDATE_CAUSE_ROGER.equals(updateType)) {
                if (mNewsItem.isPartial)
                    return;
                updateNewsItem();
                setupRogeredUsers();

                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mDetails.getLayoutParams();
                if (mNewsItemIndex.usersThatRogered.size() > 0) {
                    lp.setMargins(0, 0, 0, 0);
                }
                setupImage();
            } else {
                L.bug("Unknown updateView cause: " + updateType);
            }
        }

        public void setVisibility(boolean isVisible) {
            RecyclerView.LayoutParams param = (RecyclerView.LayoutParams) itemView.getLayoutParams();
            if (isVisible) {
                param.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                param.width = LinearLayout.LayoutParams.MATCH_PARENT;
            } else {
                param.height = 1;
                param.width = 0;
            }
            itemView.setLayoutParams(param);
        }

        private void populateView() {
            if (mNewsItem.isPartial) {
                setVisibility(true);
                mPartialItem.setVisibility(View.VISIBLE);
                mFullItem.setVisibility(View.GONE);
                return;
            }

            if (mFriendsPlugin.isBroadcastTypeDisabled(mNewsItem.sender.email, mNewsItem.broadcast_type)) {
                setVisibility(false);
                return;
            }

            setVisibility(true);

            mPartialItem.setVisibility(View.GONE);
            mFullItem.setVisibility(View.VISIBLE);

            final int existenceStatus = mFriendsPlugin.getStore().getExistence(mNewsItem.sender.email);

            mNewsPlugin.removeNotification(mNewsItem.id);
            if (!mNewsItem.read) {
                mNewsItem.read = true;
                mNewsPlugin.setNewsItemRead(mNewsItem.id);
            } else {
                mNewsPlugin.setNewsItemSortPriority(mNewsItem.id, NewsPlugin.SORT_PRIORITY_READ);
            }

            setupPinButton(mNewsItem.pinned);

            setupOptions();
            setupRogeredUsers();
            boolean isImageVisible = setupImage();
            boolean isQrCodeVisible = setupQRCode();

            setupAvatar();

            mServiceName.setText(mNewsItem.sender.name);
            mDate.setText(TimeUtils.getDayTimeStr(mActivity, mNewsItem.timestamp * 1000));

            if (TextUtils.isEmptyOrWhitespace(mNewsItem.title)) {
                mTitle.setVisibility(View.GONE);
            } else {
                mTitle.setText(mNewsItem.title);
                mTitle.setVisibility(View.VISIBLE);
            }

            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mDetails.getLayoutParams();
            if (mNewsItemIndex.usersThatRogered.size() == 0 && !isImageVisible && !isQrCodeVisible) {
                lp.setMargins(0, UIUtils.convertDipToPixels(mActivity, 30), 0, 0);
            } else {
                lp.setMargins(0, 0, 0, 0);
            }

            if (TextUtils.isEmptyOrWhitespace(mNewsItem.message)) {
                mText.setVisibility(View.GONE);
                mReadmore.setVisibility(View.GONE);
            } else {
                mText.setVisibility(View.VISIBLE);
                mText.setText(TextUtils.toMarkDown(mMainService, mNewsItem.message));
                mText.setMovementMethod(LinkMovementMethod.getInstance());
                mMainService.postOnUIHandler(new SafeRunnable() {
                    @Override
                    protected void safeRun() throws Exception {
                        int lineCount = mText.getLineCount();
                        if (lineCount > 5) {
                            mReadmore.setVisibility(View.VISIBLE);
                            mText.setMaxLines(5);
                            mReadmore.setText(R.string.read_more);
                        } else {
                            mReadmore.setVisibility(View.GONE);
                        }
                    }
                });
            }

            updateReach();

            mBroadcastType.setText(String.format("[%s]", mNewsItem.broadcast_type));

            setupButtons(existenceStatus);

            if (mNewsItem.disabled) {
                mImage.setAlpha(0.4f);
                mQRCodeContainer.setAlpha(0.4f);
                mDetails.setAlpha(0.4f);
            } else {
                mImage.setAlpha(1f);
                mQRCodeContainer.setAlpha(1f);
                mDetails.setAlpha(1f);
            }

            SafeViewOnClickListener gotoServiceClickListener = new SafeViewOnClickListener() {
                @Override
                public void safeOnClick(View v) {
                    final int existenceStatus = mActivity.friendsPlugin.getStore().getExistence(mNewsItem.sender.email);
                    if (existenceStatus == Friend.ACTIVE) {
                        Intent intent = new Intent(mActivity, ServiceActionMenuActivity.class);
                        intent.putExtra(ServiceActionMenuActivity.SERVICE_EMAIL, mNewsItem.sender.email);
                        intent.putExtra(ServiceActionMenuActivity.MENU_PAGE, 0);
                        mActivity.startActivity(intent);
                    } else if (existenceStatus == Friend.INVITE_PENDING) {
                        Intent intent = new Intent(mActivity, ServiceDetailActivity.class);
                        intent.putExtra(ServiceDetailActivity.EXISTENCE, existenceStatus);
                        intent.putExtra(ServiceDetailActivity.EMAIL, mNewsItem.sender.email);
                        mActivity.startActivity(intent);

                    } else {
                        mActivity.existence = existenceStatus;
                        mActivity.expectedEmailHash = mNewsItem.sender.email;
                        mActivity.requestFriendInfoByEmailHash(mActivity.expectedEmailHash);
                    }
                }
            };

            mImage.setOnClickListener(gotoServiceClickListener);
            mServiceAvatar.setOnClickListener(gotoServiceClickListener);
            mServiceName.setOnClickListener(gotoServiceClickListener);
            mDate.setOnClickListener(gotoServiceClickListener);
            mTitle.setOnClickListener(gotoServiceClickListener);
            mText.setOnClickListener(gotoServiceClickListener);
        }

        private void togglePinned() {
            mNewsItem.pinned = !mNewsItem.pinned;
            mNewsPlugin.setNewsItemPinned(mNewsItem.id, mNewsItem.pinned);
            setupPinButton(mNewsItem.pinned);
        }

        private void setupPinButton(boolean pinned) {
            int primaryColor = LookAndFeelConstants.getPrimaryColor(mActivity);
            int buttonColor = pinned ? ContextCompat.getColor(mActivity, R.color.mc_white) : primaryColor;
            int backgroundColor = pinned ? primaryColor : ContextCompat.getColor(mActivity, R.color.mc_white);
            mPinButton.setImageDrawable(new IconicsDrawable(mActivity).icon(FontAwesome.Icon.faw_thumb_tack).color(buttonColor).sizeDp(18));
            GradientDrawable background = (GradientDrawable) mPinButton.getBackground();
            background.setColor(backgroundColor);
        }

        private void setupOptions() {
            mDropdownButton.setOnClickListener(new SafeViewOnClickListener() {
                @Override
                public void safeOnClick(View v) {
                    final int existenceStatus = mFriendsPlugin.getStore().getExistence(mNewsItem.sender.email);
                    LinearLayout sheetView = (LinearLayout) mLayoutInflater.inflate(R.layout.news_options, null);
                    final View optionItem = mLayoutInflater.inflate(R.layout.news_options_item, null);
                    final ImageView iconView = (ImageView) optionItem.findViewById(R.id.icon);
                    final TextView titleView = (TextView) optionItem.findViewById(R.id.title);
                    final TextView subTitleView = (TextView) optionItem.findViewById(R.id.subtitle);
                    iconView.setImageDrawable(new IconicsDrawable(mActivity, FontAwesome.Icon.faw_thumb_tack).color(ContextCompat.getColor(mActivity, R.color.mc_default_text)).sizeDp(20).paddingDp(2));
                    optionItem.setOnClickListener(new SafeViewOnClickListener() {
                        @Override
                        public void safeOnClick(View v) {
                            mActivity.dismissBottomSheetDialog();
                            togglePinned();
                        }
                    });

                    if (mNewsItem.pinned) {
                        titleView.setText(R.string.unsave);
                        subTitleView.setText(R.string.remove_this_from_your_saved_items);
                    } else {
                        titleView.setText(R.string.save);
                        subTitleView.setText(R.string.add_this_to_your_saved_items);
                    }
                    sheetView.addView(optionItem);

                    if (!(mActivity instanceof NewsPinnedActivity)) {
                        // You cannot hide news items from pinned item list
                        final View actionHide = mLayoutInflater.inflate(R.layout.news_options_item, null);
                        ((ImageView) actionHide.findViewById(R.id.icon)).setImageDrawable(new IconicsDrawable(mActivity, FontAwesome.Icon.faw_times_circle).color(ContextCompat.getColor(mActivity, R.color.mc_default_text)).sizeDp(20).paddingDp(2));
                        ((TextView) actionHide.findViewById(R.id.title)).setText(R.string.unsubscribe);
                        ((TextView) actionHide.findViewById(R.id.subtitle)).setText(mActivity.getString(R.string.hide_detail, mNewsItem.broadcast_type, mNewsItem.sender.name));
                        actionHide.setOnClickListener(new SafeViewOnClickListener() {
                            @Override
                            public void safeOnClick(View v) {
                                mActivity.dismissBottomSheetDialog();
                                mFriendsPlugin.disableBroadcastType(mNewsItem.sender.email, mNewsItem.broadcast_type);
                                mNewsListAdapter.refreshView();
                            }
                        });
                        sheetView.addView(actionHide);
                    }

                    mActivity.showBottomSheetDialog(sheetView);
                }
            });
        }

        private void setupRogeredUsers() {
            if (mNewsItemIndex.usersThatRogered.size() > 0) {
                Map<String, String> namesMap = new HashMap<>();
                for (String email : mNewsItemIndex.usersThatRogered) {
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

                if (names.size() > 0) {
                    if (names.size() > 2) {
                        final SpannableString text = new SpannableString(mActivity.getString(R.string.news_members_and_x_others, names.get(0), (names.size() - 1) + ""));
                        text.setSpan(new StyleSpan(Typeface.BOLD), 0, names.get(0).length(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        mMembers.setText(text);

                    } else if (names.size() > 1) {
                        String namesPart = android.text.TextUtils.join(" & ", names);
                        final SpannableString text = new SpannableString(mActivity.getString(R.string.news_members_x, namesPart));
                        int splitIndex = namesPart.indexOf(" & ");
                        text.setSpan(new StyleSpan(Typeface.BOLD), 0, splitIndex,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        text.setSpan(new StyleSpan(Typeface.BOLD), splitIndex + 3, namesPart.length(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        mMembers.setText(text);
                    } else {
                        String namesPart = names.get(0);
                        final SpannableString text = new SpannableString(mActivity.getString(R.string.news_members_1, namesPart));
                        text.setSpan(new StyleSpan(Typeface.BOLD), 0, namesPart.length(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        mMembers.setText(text);
                    }

                    mMembersContainer.setVisibility(View.VISIBLE);
                    mMembersContainer.setOnClickListener(new SafeViewOnClickListener() {
                        @Override
                        public void safeOnClick(View v) {
                            Intent intent = new Intent(mActivity, MembersActivity.class);
                            intent.putExtra(MembersActivity.ME, mMyEmail);
                            intent.putExtra(MembersActivity.MEMBERS, mNewsItemIndex.usersThatRogered.toArray(new String[mNewsItemIndex.usersThatRogered.size()]));
                            mActivity.startActivity(intent);
                        }
                    });
                } else {
                    mMembersContainer.setVisibility(View.GONE);
                }
            } else {
                mMembersContainer.setVisibility(View.GONE);
            }
        }

        private void setupButtons(int existenceStatus) {
            mActions.removeAllViews();
            int totalButtonCount = 0;
            int currentButton = 0;
            boolean rogerthatButtonEnabled = SystemUtils.isFlagEnabled(mNewsItem.flags, NewsItem.FLAG_ACTION_ROGERTHAT);
            boolean followButtonEnabled = SystemUtils.isFlagEnabled(mNewsItem.flags, NewsItem.FLAG_ACTION_FOLLOW);

            if (rogerthatButtonEnabled) {
                totalButtonCount++;
            }
            if (followButtonEnabled) {
                totalButtonCount++;
            }
            totalButtonCount += mNewsItem.buttons.length;
            if (rogerthatButtonEnabled) {
                currentButton++;
                final Button rogerthatButton = (Button) mLayoutInflater.inflate(R.layout.news_list_item_action, mActions, false);
                mActions.addView(rogerthatButton);
                rogerthatButton.setText(mActivity.getString(R.string.like));

                final GradientDrawable background = new GradientDrawable();
                background.setCornerRadii(getCorners(totalButtonCount, currentButton));

                int backgroundColor;
                if (mNewsItem.rogered) {
                    backgroundColor = R.color.mc_divider_gray;
                } else {
                    backgroundColor = R.color.mc_default_text;
                    rogerthatButton.setOnClickListener(new SafeViewOnClickListener() {
                        @Override
                        public void safeOnClick(View v) {
                            rogerthatButton.setOnClickListener(null);
                            mNewsPlugin.setNewsItemRogered(mNewsItem.id, mMyEmail);
                            background.setColor(ContextCompat.getColor(mActivity, R.color.mc_divider_gray));
                            rogerthatButton.setBackground(background);
                        }
                    });
                }

                background.setColor(ContextCompat.getColor(mActivity, backgroundColor));
                rogerthatButton.setBackground(background);

                if (followButtonEnabled || mNewsItem.buttons.length > 0) {
                    ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) rogerthatButton.getLayoutParams();
                    marginParams.setMargins(0, 0, getMarginLeft(totalButtonCount, currentButton), 0);
                }
            }

            if (followButtonEnabled) {
                currentButton++;
                final Button followButton = (Button) mLayoutInflater.inflate(R.layout.news_list_item_action, mActions, false);
                mActions.addView(followButton);
                followButton.setText(mActivity.getString(R.string.follow));


                int backgroundColor = Friend.ACTIVE == existenceStatus ? R.color.mc_divider_gray : R.color.mc_default_text;
                GradientDrawable background = new GradientDrawable();
                followButton.setOnClickListener(new SafeViewOnClickListener() {
                    @Override
                    public void safeOnClick(View v) {
                        followButton.setOnClickListener(null);
                        final int currentExistenceStatus = mFriendsPlugin.getStore().getExistence(mNewsItem.sender.email);
                        if (currentExistenceStatus != Friend.ACTIVE) {
                            mFriendsPlugin.inviteFriend(mNewsItem.sender.email, null, null, false, true);

                            SafeRunnable runnable = new SafeRunnable() {
                                @Override
                                protected void safeRun() throws Exception {
                                    mNewsPlugin.saveNewsStatistics(new long[]{mNewsItem.id}, NewsPlugin.STATISTIC_FOLLOWED);
                                }
                            };
                            mMainService.runOnBIZZHandler(runnable);

                        }
                        followButton.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.mc_divider_gray));
                    }
                });

                if (mNewsItem.buttons.length > 0) {
                    ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) followButton.getLayoutParams();
                    marginParams.setMargins(0, 0, getMarginLeft(totalButtonCount, currentButton), 0);
                }
                background.setColor(ContextCompat.getColor(mActivity, backgroundColor));
                background.setCornerRadii(getCorners(totalButtonCount, currentButton));
                followButton.setBackground(background);
            }

            for (int i = 0; i < mNewsItem.buttons.length; i++) {
                currentButton++;
                final NewsActionButtonTO button = mNewsItem.buttons[i];

                Map<String, String> actionInfo = mMessagingPlugin.getButtonActionInfo(button.action);
                final String buttonAction = actionInfo.get("androidAction");
                final String buttonUrl = actionInfo.get("androidUrl");

                final Button btn = (Button) mLayoutInflater.inflate(R.layout.news_list_item_action, mActions, false);
                mActions.addView(btn);
                btn.setText(button.caption);
                btn.setOnClickListener(new SafeViewOnClickListener() {
                    @Override
                    public void safeOnClick(View v) {
                        if (Message.MC_CONFIRM_PREFIX.equals(buttonAction)) {
                            return;
                        }

                        if (Message.MC_SMI_PREFIX.equals(buttonAction)) {
                            final SafeRunnable smiClickRunnable = new SafeRunnable() {
                                @Override
                                protected void safeRun() throws Exception {
                                    mNewsPlugin.saveNewsStatistics(new long[]{mNewsItem.id}, NewsPlugin.STATISTIC_ACTION);
                                    mActivity.actionPressed(mNewsItem.sender.email, buttonAction, buttonUrl, button.flow_params);
                                    mExecuteAfterBecameFriends = null;
                                }
                            };

                            final int currentExistenceStatus = mFriendsPlugin.getStore().getExistence(mNewsItem.sender.email);
                            if (Friend.ACTIVE == currentExistenceStatus) {
                                smiClickRunnable.run();
                            } else {
                                if (!mMainService.getNetworkConnectivityManager().isConnected()) {
                                    UIUtils.showDialog(mActivity, null, R.string.no_internet_connection_try_again);
                                    return;
                                }
                                mActivity.mProgressDialog.show();
                                mActivity.expectedEmailHash = mNewsItem.sender.email;

                                mExecuteAfterBecameFriends = smiClickRunnable;
                                mFriendsPlugin.inviteFriend(mNewsItem.sender.email, null, null, false, true);
                            }
                        } else if (Message.MC_POKE_PREFIX.equals(buttonAction)) {
                            mNewsPlugin.saveNewsStatistics(new long[]{mNewsItem.id}, NewsPlugin.STATISTIC_ACTION);
                            mActivity.actionPressed(mNewsItem.sender.email, buttonAction, buttonUrl, null);
                        } else if (buttonAction != null) {
                            mNewsPlugin.saveNewsStatistics(new long[]{mNewsItem.id}, NewsPlugin.STATISTIC_ACTION);
                            Uri uri = Uri.parse(buttonUrl);
                            if (buttonUrl.startsWith("http")) {
                                CustomTabsIntent.Builder customTabsBuilder = new CustomTabsIntent.Builder();
                                CustomTabsIntent customTabsIntent = customTabsBuilder.build();
                                customTabsIntent.launchUrl(mActivity, uri);
                            } else {
                                final Intent intent = new Intent(buttonAction, uri);
                                mActivity.startActivity(intent);
                            }
                        }
                    }
                });
                GradientDrawable background = new GradientDrawable();
                background.setColor(LookAndFeelConstants.getPrimaryColor(mActivity));
                background.setCornerRadii(getCorners(totalButtonCount, currentButton));
                btn.setBackground(background);
                ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) btn.getLayoutParams();
                marginParams.setMargins(0, 0, getMarginLeft(totalButtonCount, currentButton), 0);
            }
        }

        private boolean setupImage() {
            if (TextUtils.isEmptyOrWhitespace(mNewsItem.image_url)) {
                mImage.setVisibility(View.GONE);
                return false;
            } else {
                new DownloadImageTask(mActivity.getCachedDownloader(), mImage,
                        mNewsItemIndex.usersThatRogered.size() == 0, mActivity, TOP_RADIUS).execute(mNewsItem.image_url);
            }
            return true;
        }

        private void setupAvatar() {
            Bitmap avatar = mActivity.friendsPlugin.getStore().getAvatarBitmap(mNewsItem.sender.email);
            if (avatar == null) {
                new DownloadImageTask(mActivity.getCachedDownloader(), mServiceAvatar, true, mActivity, 0).execute(mNewsItem.getAvatarUrl());
            } else {
                mServiceAvatar.setImageBitmap(avatar);
            }
        }

        private boolean setupQRCode() {
            if (mNewsItem.type == NewsItem.TYPE_QR_CODE) {
                mQRCodeContainer.setVisibility(View.VISIBLE);
                mQRCodeCaption.setText(mNewsItem.qr_code_caption);

                if (mNewsItemIndex.usersThatRogered.size() == 0 && TextUtils.isEmptyOrWhitespace(mNewsItem.image_url)) {
                    mQRCodeContainer.setPadding(0, UIUtils.convertDipToPixels(mActivity, 30), 0, 0);
                }

                mQRCode.setImageResource(R.drawable.qr_gray_preview); // todo not working

                mMainService.postOnBIZZHandler(new SafeRunnable() {
                    @Override
                    protected void safeRun() throws Exception {
                        Intent intent = new Intent();
                        intent.setAction(Intents.Encode.ACTION);
                        intent.putExtra(Intents.Encode.TYPE, Contents.Type.TEXT);
                        intent.putExtra(Intents.Encode.DATA, mNewsItem.qr_code_content);
                        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(mActivity, intent, UIUtils.getDisplayWidth(mActivity) / 2, false);
                        final Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();

                        mMainService.postAtFrontOfUIHandler(new SafeRunnable() {
                            @Override
                            protected void safeRun() throws Exception {
                                setQRCode(bitmap);
                            }
                        });
                    }
                });
                return true;
            } else {
                mQRCodeContainer.setVisibility(View.GONE);
                return false;
            }
        }

        private void setQRCode(Bitmap bitmap) {
            mQRCode.setImageBitmap(bitmap);
        }

        private float[] getCorners(int totalButtonCount, int currentButton) {
            // top-left, top-right, bottom-right, bottom-left.
            int dp15 = UIUtils.convertDipToPixels(mActivity, 15);
            if (totalButtonCount == 1) {
                return new float[]{0, 0, 0, 0, dp15, dp15, dp15, dp15};
            }
            if (currentButton == 1) {
                return new float[]{0, 0, 0, 0, 0, 0, dp15, dp15};
            }
            if (currentButton == totalButtonCount) {
                return new float[]{0, 0, 0, 0, dp15, dp15, 0, 0};
            }
            return new float[]{0, 0, 0, 0, 0, 0, 0, 0};
        }

        private int getMarginLeft(int totalButtonCount, int currentButton) {
            return currentButton < totalButtonCount ? UIUtils.convertDipToPixels(mActivity, 1) : 0;
        }

        private void updateNewsItem() {
            DebugUtils.profile("NewsStore.getNewsItem()", new Runnable() {
                @Override
                public void run() {
                    mNewsItem = mNewsPlugin.getStore().getNewsItem(mNewsItem.id);

                    for (String friendEmail : mNewsItem.users_that_rogered) {
                        if (!mNewsItemIndex.usersThatRogered.contains(friendEmail)) {
                            mNewsItemIndex.usersThatRogered.add(friendEmail);
                        }
                    }
                }
            });
        }

        private void updateReach() {
            if (TestUtils.isRunningTest()) {
                mNewsItemIndex.reach = mNewsItem.reach + 1;
            }
            if (mNewsItemIndex.reach > 0) {
                mReach.setVisibility(View.VISIBLE);
                mReachSpinner.setVisibility(View.GONE);
                mReach.setText(String.valueOf(mNewsItemIndex.reach));
            } else {
                mReach.setVisibility(View.GONE);
                mReachSpinner.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mActivity instanceof NewsPinnedActivity) {
            return (int) mActivity.newsStore.countNewsPinnedItemsSearch(mActivity.pinnedSearchQry);
        }
        return (int) mActivity.newsStore.countNewsItems();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mActivity.getLayoutInflater().inflate(R.layout.news_list_item, parent, false);
        ViewHolder vh = new ViewHolder(mMainService, mActivity, this, v, mActivity.newsPlugin,
                mActivity.friendsPlugin, mMessagingPlugin, mMyEmail, mMyName, TOP_RADIUS);

        mViewHolders.add(vh);
        return vh;
    }

    private class FillCacheParams {
        public final long sortKey;
        public final String pinnedSearchQry;

        public FillCacheParams(long sortKey, String pinnedSearchQry) {
            this.sortKey = sortKey;
            this.pinnedSearchQry = pinnedSearchQry;
        }
    }

    private class FillCacheInFrontTask extends SafeAsyncTask<FillCacheParams, Void, List<NewsItemIndex>> {
        @Override
        protected List<NewsItemIndex> safeDoInBackground(FillCacheParams... params) {
            T.dontCare();
            return mActivity.newsPlugin.getNewsBefore(params[0].sortKey, BATCH_SIZE, params[0].pinnedSearchQry);
        }

        @Override
        protected void onPostExecute(List<NewsItemIndex> result) {
            T.UI();
            addNewsItemsToFront(result);
        }
    }
    private FillCacheInFrontTask mFillCacheInFrontTask;

    private class FillCacheInEndTask extends SafeAsyncTask<FillCacheParams, Void, List<NewsItemIndex>> {
        @Override
        protected List<NewsItemIndex> safeDoInBackground(FillCacheParams... params) {
            T.dontCare();
            return mActivity.newsPlugin.getNewsAfter(params[0].sortKey, BATCH_SIZE, params[0].pinnedSearchQry);
        }

        @Override
        protected void onPostExecute(List<NewsItemIndex> result) {
            T.UI();
            addNewsItemsToEnd(result);
        }
    }
    private FillCacheInEndTask mFillCacheInEndTask;

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position < (mMinPosition - BATCH_SIZE)) {
            mNewsItemsByPosition = new SparseArray<>();
            mNewsItemsById = new LongSparseArray<>();
            mMinPosition = -1;
            mMaxPosition = -1;
        }

        if (position < mMinPosition) {
            if (mFillCacheInFrontTask != null && mFillCacheInFrontTask.getStatus() != AsyncTask.Status.FINISHED) {
                mFillCacheInFrontTask.cancel(true);
                mFillCacheInFrontTask = null;
            }

            do {
                List<NewsItemIndex> newsItems = mActivity.newsPlugin.getNewsBefore(mNewsItemsByPosition.get(mMinPosition).sortKey, BATCH_SIZE, mActivity.pinnedSearchQry);
                addNewsItemsToFront(newsItems);
            } while (position < mMinPosition);

        } else if (position > mMaxPosition) {
            if (mFillCacheInEndTask != null && mFillCacheInEndTask.getStatus() != AsyncTask.Status.FINISHED) {
                mFillCacheInEndTask.cancel(true);
                mFillCacheInEndTask = null;
            }

            if (mMinPosition < 0)
                mMinPosition = 0;

            do {
                NewsItemIndex newsItemIndex = mNewsItemsByPosition.get(mMaxPosition);
                long sortKey = newsItemIndex == null ? Long.MAX_VALUE : newsItemIndex.sortKey;
                List<NewsItemIndex> newsItems = mActivity.newsPlugin.getNewsAfter(sortKey, BATCH_SIZE, mActivity.pinnedSearchQry);
                addNewsItemsToEnd(newsItems);
            } while (position > mMaxPosition);
        }

        if ((position - BACKGROUND_FETCH_THRESHOLD) <= mMinPosition && mMinPosition > 0
                && (mFillCacheInFrontTask == null || mFillCacheInFrontTask.getStatus() == AsyncTask.Status.FINISHED)) {
            mFillCacheInFrontTask = new FillCacheInFrontTask();
            mFillCacheInFrontTask.execute(new FillCacheParams(mNewsItemsByPosition.get(mMinPosition).sortKey, mActivity.pinnedSearchQry));
        }
        if ((position + BACKGROUND_FETCH_THRESHOLD) >= mMaxPosition
                && (mFillCacheInEndTask == null || mFillCacheInEndTask.getStatus() == AsyncTask.Status.FINISHED )) {
            NewsItemIndex newsItemIndex = mNewsItemsByPosition.get(mMaxPosition);
            long sortKey = newsItemIndex == null ? Long.MAX_VALUE : newsItemIndex.sortKey;
            mFillCacheInEndTask = new FillCacheInEndTask();
            mFillCacheInEndTask.execute(new FillCacheParams(sortKey, mActivity.pinnedSearchQry));
        }

        String logMessage = "onBindViewHolder for:\n- position: " + position + "\n- mMinPosition: " + mMinPosition + "\n- mMaxPosition: " + mMaxPosition + "\n- newsitem size: " + mNewsItemsByPosition.size() + "\n- adapter size: " + getItemCount();
        NewsItemIndex ni = mNewsItemsByPosition.get(position);
        if (ni == null) {
            L.bug(logMessage);
        } else {
            L.d(logMessage);
            holder.setNewsItem(position, mActivity.newsStore.getNewsItem(ni.id), ni);
        }
    }

    private void addNewsItemsToEnd(List<NewsItemIndex> newsItems) {
        for (NewsItemIndex ni : newsItems) {
            mNewsItemsByPosition.put(++mMaxPosition, ni);
            mNewsItemsById.put(ni.id, ni);
        }
        while (mNewsItemsByPosition.size() > CACHE_SIZE) {
            NewsItemIndex ni = mNewsItemsByPosition.get(mMinPosition);
            mNewsItemsByPosition.remove(mMinPosition++);
            mNewsItemsById.remove(ni.id);
        }
    }

    private void addNewsItemsToFront(List<NewsItemIndex> newsItems) {
        for (NewsItemIndex ni : newsItems) {
            mNewsItemsByPosition.put(--mMinPosition, ni);
            mNewsItemsById.put(ni.id, ni);
        }
        while (mNewsItemsByPosition.size() > CACHE_SIZE) {
            NewsItemIndex ni = mNewsItemsByPosition.get(mMaxPosition);
            mNewsItemsByPosition.remove(mMaxPosition--);
            mNewsItemsById.remove(ni.id);
        }
    }
}
