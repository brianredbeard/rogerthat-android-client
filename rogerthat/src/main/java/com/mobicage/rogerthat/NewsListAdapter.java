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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
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
import com.mobicage.rogerthat.plugins.friends.MenuItemPresser;
import com.mobicage.rogerthat.plugins.friends.ServiceActionMenuActivity;
import com.mobicage.rogerthat.plugins.messaging.MembersActivity;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.plugins.news.NewsItem;
import com.mobicage.rogerthat.plugins.news.NewsItemDetails;
import com.mobicage.rogerthat.plugins.news.NewsPlugin;
import com.mobicage.rogerthat.util.CachedDownloader;
import com.mobicage.rogerthat.util.DownloadImageTask;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeDialogInterfaceOnClickListener;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.time.TimeUtils;
import com.mobicage.rogerthat.util.ui.ImageHelper;
import com.mobicage.rogerthat.util.ui.ScaleImageView;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rogerthat.widget.Resizable16by6ImageView;
import com.mobicage.rpc.config.CloudConstants;
import com.mobicage.to.news.NewsActionButtonTO;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class NewsListAdapter extends RecyclerView.Adapter<NewsListAdapter.ViewHolder> {

    protected final static int FLAG_ACTION_ROGERTHAT = 1;
    protected final static int FLAG_ACTION_FOLLOW = 2;

    private final int _1_DP;
    private final int _15_DP;
    private final int _27_DP;

    private NewsActivity mActivity;
    private final MainService mMainService;
    private final LayoutInflater mLayoutInflater;
    private final MessagingPlugin mMessagingPlugin;

    private CachedDownloader mCachedDownloader;

    private Map<String, Bitmap> mQRCodes = new HashMap<>();
    private Map<String, ArrayList<Resizable16by6ImageView>> mImageViews = new HashMap<>();
    private Set<Long> mReadMoreItems = new HashSet<>();

    private Map<String, Set<Long>> mServiceItems = new HashMap<>();
    private List<Long> mItems = new ArrayList<>();
    private List<Long> mVisibleItems = new ArrayList<>();
    private List<Long> mRequestedPartialItems = new ArrayList<>();

    private final String mMyEmail;
    private final String mMyName;
    private final int mDisplayWidth;

    private int mRequestMoreNewsPosition = 0;
    private Button mCurrentActionBtn;

    private final BottomSheetDialog mBottomSheetDialog;

    public NewsListAdapter(NewsActivity activity, MainService mainService) {
        mActivity = activity;
        mMainService = mainService;
        _1_DP = UIUtils.convertDipToPixels(mActivity, 1);
        _15_DP = UIUtils.convertDipToPixels(mActivity, 15);
        _27_DP = UIUtils.convertDipToPixels(mActivity, 27);
        mLayoutInflater = LayoutInflater.from(mActivity);
        mMessagingPlugin = mMainService.getPlugin(MessagingPlugin.class);

        mCachedDownloader = CachedDownloader.getInstance(mMainService);

        MyIdentity myIdentity = mainService.getIdentityStore().getIdentity();
        mMyEmail = myIdentity.getEmail();
        mMyName = myIdentity.getDisplayName();
        mDisplayWidth = UIUtils.getDisplayWidth(mActivity);
        mBottomSheetDialog = new BottomSheetDialog(mActivity);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View partialItem;
        public final View fullItem;
        public final ImageButton pinButton;
        public final ImageButton dropdownButton;
        public final LinearLayout membersContainer;
        public final TextView members;
        public final Resizable16by6ImageView image;
        public final View qrCodeContainer;
        public final ScaleImageView qrCode;
        public final TextView qrCodeCaption;
        public final ImageView serviceAvatar;
        public final TextView serviceName;
        public final TextView date;
        public final TextView title;
        public final LinearLayout details;
        public final TextView text;
        public final TextView readmore;
        public final TextView reach;
        public final ProgressBar reachSpinner;
        public final TextView broadcastType;
        public final LinearLayout actions;

        public ViewHolder(View itemView) {
            super(itemView);

            partialItem = itemView.findViewById(R.id.partial_item);
            fullItem = itemView.findViewById(R.id.full_item);
            pinButton = (ImageButton) itemView.findViewById(R.id.pin_button);
            dropdownButton = (ImageButton) itemView.findViewById(R.id.dropdown_button);
            membersContainer = (LinearLayout) itemView.findViewById(R.id.members_container);
            members = (TextView) itemView.findViewById(R.id.members);
            image = (Resizable16by6ImageView) itemView.findViewById(R.id.image);
            qrCodeContainer = itemView.findViewById(R.id.qr_code_container);
            qrCode = (ScaleImageView) itemView.findViewById(R.id.qr_code);
            qrCodeCaption = (TextView) itemView.findViewById(R.id.qr_code_caption);
            serviceAvatar = (ImageView) itemView.findViewById(R.id.service_avatar);
            serviceName = (TextView) itemView.findViewById(R.id.service_name);
            date = (TextView) itemView.findViewById(R.id.date);
            title = (TextView) itemView.findViewById(R.id.title);
            details = (LinearLayout) itemView.findViewById(R.id.details);
            text = (TextView) itemView.findViewById(R.id.text);
            readmore = (TextView) itemView.findViewById(R.id.readmore);
            reach = (TextView) itemView.findViewById(R.id.reach);
            reachSpinner = (ProgressBar) itemView.findViewById(R.id.reach_spinner);
            broadcastType = (TextView) itemView.findViewById(R.id.broadcast_type);
            actions = (LinearLayout) itemView.findViewById(R.id.actions);
        }
    }

    public void addNewsItem(long newsId, boolean notify) {
        if (!mItems.contains(newsId)) {
            mItems.add(newsId);

            if (!mActivity.newsStore.getNewsItemDetails(newsId).isPartial) {
                NewsItem newsItem = mActivity.newsStore.getNewsItem(newsId);
                if (mActivity.friendsPlugin.isBroadcastTypeDisabled(newsItem.sender.email, newsItem.broadcast_type)) {
                    return;
                }
            }

            mVisibleItems.add(newsId);
            if (notify) {
                notifyItemInserted(mVisibleItems.size());
            }
        }
    }

    public void setNewsItems(List<Long> items) {
        mItems = items;
    }

    public void refreshView() {
        mVisibleItems = new ArrayList<>();
        for (Long newsId : mItems) {
            if (!mActivity.newsStore.getNewsItemDetails(newsId).isPartial) {
                NewsItem newsItem = mActivity.newsStore.getNewsItem(newsId);
                if (mActivity.friendsPlugin.isBroadcastTypeDisabled(newsItem.sender.email, newsItem.broadcast_type)) {
                    continue;
                }
            }

            mVisibleItems.add(newsId);
        }
        if (mVisibleItems.size() > 0) {
            Collections.sort(mVisibleItems, comparator);
        }

        getNewsItems(0);
        notifyDataSetChanged();
    }

    private void refreshItemsOfService(String email) {
        if (mServiceItems.containsKey(email)) {
            for (Long newsId : mServiceItems.get(email)) {
                updateView(newsId);
            }
        }
    }

    private final Comparator<Long> comparator = new Comparator<Long>() {
        @Override
        public int compare(Long item1, Long item2) {
            if (mActivity.idToShowAtTop > 0) {
                if (item1 == mActivity.idToShowAtTop) {
                    return -1;
                } else if (item2 == mActivity.idToShowAtTop) {
                    return 1;
                }
            }
            NewsItemDetails details1 = mActivity.newsStore.getNewsItemDetails(item1);
            NewsItemDetails details2 = mActivity.newsStore.getNewsItemDetails(item2);

            if (!details1.read && details2.read) {
                return -1;
            } else if (details1.read && !details2.read) {
                return 1;
            } else if (details1.sortPriority > details2.sortPriority) {
                return 1;
            } else if (details1.sortPriority < details2.sortPriority) {
                return -1;
            } else if (details1.sortTimestamp < details2.sortTimestamp) {
                return 1;
            } else if (details1.sortTimestamp > details2.sortTimestamp) {
                return -1;
            }
            return 0;
        }
    };

    @Override
    public int getItemCount() {
        return mVisibleItems.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mLayoutInflater.inflate(R.layout.news_list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mVisibleItems.get(position);
        populateView(holder, position);
    }

    public void updateView(long newsId) {
        int position = mVisibleItems.indexOf(newsId);
        if (position >= 0) {
            notifyItemChanged(position);

            if (!mActivity.newsStore.getNewsItemDetails(newsId).isPartial) {
                mRequestedPartialItems.remove(newsId);
            }
        }
    }

    private void getNewsItems(final int position) {
        List<Long> statsToRequest = new ArrayList<>();
        List<Long> itemsToRequest = new ArrayList<>();

        mRequestMoreNewsPosition = position + 50;

        for (int i = position; i < mRequestMoreNewsPosition; i++) {
            if (i >= mVisibleItems.size()) {
                break;
            }
            long newsId = mVisibleItems.get(i);
            statsToRequest.add(newsId);
            if (!mActivity.newsStore.getNewsItemDetails(newsId).isPartial)
                continue;
            ;
            if (mRequestedPartialItems.contains(newsId))
                continue;

            itemsToRequest.add(newsId);
        }

        if (statsToRequest.size() > 0) {
            if (mActivity.newsChannel != null) {
                mActivity.newsChannel.statsNews(statsToRequest);
            }
        }

        if (itemsToRequest.size() == 0)
            return;

        L.d("Requesting " + itemsToRequest.size() + " news items");
        long[] ids = new long[itemsToRequest.size()];
        for (int i = 0; i < itemsToRequest.size(); i++) {
            ids[i] = itemsToRequest.get(i);
        }
        mActivity.newsPlugin.getNewsItems(ids, UUID.randomUUID().toString());
    }

    private void populateView(final ViewHolder viewHolder, final int position) {
        final NewsItem newsItem = mActivity.newsStore.getNewsItem(mVisibleItems.get(position));

        if (!mServiceItems.containsKey(newsItem.sender.email)) {
            mServiceItems.put(newsItem.sender.email, new HashSet<Long>());
        }
        mServiceItems.get(newsItem.sender.email).add(newsItem.id);

        if (position >= (mRequestMoreNewsPosition - 10)) {
            getNewsItems(position);
        }

        if (newsItem.isPartial) {
            viewHolder.partialItem.setVisibility(View.VISIBLE);
            viewHolder.fullItem.setVisibility(View.GONE);
            return;
        }

        viewHolder.partialItem.setVisibility(View.GONE);
        viewHolder.fullItem.setVisibility(View.VISIBLE);

        final int existenceStatus = mActivity.friendsPlugin.getStore().getExistence(newsItem.sender.email);
        if (!newsItem.read) {
            newsItem.read = true;

            mMainService.postAtFrontOfBIZZHandler(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    mActivity.newsStore.setNewsItemRead(newsItem.id);
                    mActivity.newsChannel.readNews(newsItem.id);
                }
            });

            mActivity.newsPlugin.newsRead(new long[]{newsItem.id});
        }

        setupPinned(viewHolder, newsItem);
        setupOptions(viewHolder, newsItem);
        setupRogeredUsers(viewHolder, newsItem);
        boolean isImageVisible = setupImage(viewHolder, newsItem);
        boolean isQrCodeVisible = setupQRCode(viewHolder, newsItem);

        setupAvatar(viewHolder, newsItem);

        viewHolder.serviceName.setText(newsItem.sender.name);
        viewHolder.date.setText(TimeUtils.getDayTimeStr(mActivity, newsItem.timestamp * 1000));

        if (TextUtils.isEmptyOrWhitespace(newsItem.title)) {
            viewHolder.title.setVisibility(View.GONE);
        } else {
            viewHolder.title.setText(newsItem.title);
            viewHolder.title.setVisibility(View.VISIBLE);

        }

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) viewHolder.details.getLayoutParams();
        if (newsItem.users_that_rogered.length == 0 && !isImageVisible && !isQrCodeVisible) {
            lp.setMargins(0, _27_DP, 0, 0);
        } else {
            lp.setMargins(0, 0, 0, 0);
        }

        if (TextUtils.isEmptyOrWhitespace(newsItem.message)) {
            viewHolder.text.setVisibility(View.GONE);
            viewHolder.readmore.setVisibility(View.GONE);
        } else {
            viewHolder.text.setVisibility(View.VISIBLE);
            viewHolder.text.setText(newsItem.message);
            mMainService.postOnUIHandler(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    int lineCount = viewHolder.text.getLineCount();
                    if (lineCount > 5) {
                        viewHolder.readmore.setVisibility(View.VISIBLE);
                        if (mReadMoreItems.contains(newsItem.id)) {
                            viewHolder.text.setMaxLines(lineCount);
                            viewHolder.readmore.setText(R.string.read_less);
                        } else {
                            viewHolder.text.setMaxLines(5);
                            viewHolder.readmore.setText(R.string.read_more);
                        }

                        viewHolder.readmore.setOnClickListener(new SafeViewOnClickListener() {
                            @Override
                            public void safeOnClick(View v) {
                                if (mReadMoreItems.contains(newsItem.id)) {
                                    mReadMoreItems.remove(newsItem.id);
                                } else {
                                    mReadMoreItems.add(newsItem.id);
                                }
                                notifyItemChanged(position);
                                mActivity.setSelection(position);
                            }
                        });
                    } else {
                        viewHolder.readmore.setVisibility(View.GONE);
                    }
                }
            });
        }

        if (newsItem.reach >= 0) {
            viewHolder.reach.setVisibility(View.VISIBLE);
            viewHolder.reachSpinner.setVisibility(View.GONE);
            viewHolder.reach.setText(String.valueOf(newsItem.reach));
        } else {
            viewHolder.reach.setVisibility(View.GONE);
            viewHolder.reachSpinner.setVisibility(View.VISIBLE);
        }

        viewHolder.broadcastType.setText(String.format("[%s]", newsItem.broadcast_type));

        setupButtons(viewHolder, position, newsItem, existenceStatus);

        if (newsItem.disabled) {
            viewHolder.image.setAlpha(0.4f);
            viewHolder.qrCodeContainer.setAlpha(0.4f);
            viewHolder.details.setAlpha(0.4f);
        } else {
            viewHolder.image.setAlpha(1f);
            viewHolder.qrCodeContainer.setAlpha(1f);
            viewHolder.details.setAlpha(1f);
        }
    }

    private void setupButtons(final ViewHolder viewHolder, final int position, final NewsItem newsItem, int existenceStatus) {
        viewHolder.actions.removeAllViews();
        int totalButtonCount = 0;
        int currentButton = 0;
        boolean rogerthatButtonEnabled = SystemUtils.isFlagEnabled(newsItem.flags, FLAG_ACTION_ROGERTHAT);
        boolean followButtonEnabled = SystemUtils.isFlagEnabled(newsItem.flags, FLAG_ACTION_FOLLOW);

        if (rogerthatButtonEnabled) {
            totalButtonCount++;
        }
        if (followButtonEnabled) {
            totalButtonCount++;
        }
        totalButtonCount += newsItem.buttons.length;
        if (rogerthatButtonEnabled) {
            currentButton++;
            final Button rogerthatButton = (Button) mLayoutInflater.inflate(R.layout.news_list_item_action, viewHolder.actions, false);
            viewHolder.actions.addView(rogerthatButton);
            rogerthatButton.setText(mActivity.getString(R.string.rogerthat));

            GradientDrawable background = new GradientDrawable();
            int backgroundColor;
            if (newsItem.rogered) {
                backgroundColor = R.color.mc_divider_gray;
            } else {
                backgroundColor = R.color.mc_default_text;
                rogerthatButton.setOnClickListener(new SafeViewOnClickListener() {
                    @Override
                    public void safeOnClick(View v) {
                        rogerthatButton.setOnClickListener(null);
                        mMainService.postAtFrontOfBIZZHandler(new SafeRunnable() {
                            @Override
                            protected void safeRun() throws Exception {
                                mActivity.newsPlugin.newsRogered(newsItem.id);
                                mActivity.newsStore.setNewsItemRogered(newsItem.id);
                                mActivity.newsStore.addUser(newsItem.id, mMyEmail);
                                mActivity.newsChannel.rogerNews(newsItem.id);
                                mMainService.postAtFrontOfUIHandler(new SafeRunnable() {
                                    @Override
                                    protected void safeRun() throws Exception {
                                        notifyItemChanged(position);
                                    }
                                });
                            }
                        });
                    }
                });
            }
            background.setCornerRadii(getCorners(totalButtonCount, currentButton));
            background.setColor(ContextCompat.getColor(mActivity, backgroundColor));
            rogerthatButton.setBackground(background);

            if (SystemUtils.isFlagEnabled(newsItem.flags, FLAG_ACTION_FOLLOW) || newsItem.buttons.length > 0) {
                ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) rogerthatButton.getLayoutParams();
                marginParams.setMargins(0, 0, getMarginLeft(totalButtonCount, currentButton), 0);
            }
        }

        if (followButtonEnabled) {
            currentButton++;
            final Button followButton = (Button) mLayoutInflater.inflate(R.layout.news_list_item_action, viewHolder.actions, false);
            viewHolder.actions.addView(followButton);
            followButton.setText(mActivity.getString(R.string.follow));


            int backgroundColor = Friend.ACTIVE == existenceStatus ? R.color.mc_divider_gray : R.color.mc_default_text;
            GradientDrawable background = new GradientDrawable();
            followButton.setOnClickListener(new SafeViewOnClickListener() {
                @Override
                public void safeOnClick(View v) {
                    followButton.setOnClickListener(null);
                    final int currentExistenceStatus = mActivity.friendsPlugin.getStore().getExistence(newsItem.sender.email);
                    if (currentExistenceStatus != Friend.ACTIVE) {
                        mActivity.friendsPlugin.inviteFriend(newsItem.sender.email, null, null, false);
                    }
                    followButton.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.mc_divider_gray));
                }
            });

            if (newsItem.buttons.length > 0) {
                ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) followButton.getLayoutParams();
                marginParams.setMargins(0, 0, getMarginLeft(totalButtonCount, currentButton), 0);
            }
            background.setColor(ContextCompat.getColor(mActivity, backgroundColor));
            background.setCornerRadii(getCorners(totalButtonCount, currentButton));
            followButton.setBackground(background);
        }

        for (int i = 0; i < newsItem.buttons.length; i++) {
            currentButton++;
            final NewsActionButtonTO button = newsItem.buttons[i];

            Map<String, String> actionInfo = mMessagingPlugin.getButtonActionInfo(button);
            final String buttonAction = actionInfo.get("androidAction");
            final String buttonUrl = actionInfo.get("androidUrl");

            final Button btn = (Button) mLayoutInflater.inflate(R.layout.news_list_item_action, viewHolder.actions, false);
            viewHolder.actions.addView(btn);
            btn.setText(button.caption);
            btn.setOnClickListener(new SafeViewOnClickListener() {
                @Override
                public void safeOnClick(View v) {
                    final int currentExistenceStatus = mActivity.friendsPlugin.getStore().getExistence(newsItem.sender.email);
                    if (Friend.ACTIVE == currentExistenceStatus) {
                        if (Message.MC_CONFIRM_PREFIX.equals(buttonAction)) {
                            // ignore
                        } else if (Message.MC_SMI_PREFIX.equals(buttonAction)) {
                            MenuItemPresser menuItemPresser = new MenuItemPresser(mActivity, newsItem.sender.email);
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
                                mActivity.startActivity(intent);
                            }
                        }
                    } else {
                        new AlertDialog.Builder(mActivity)
                                .setMessage(mActivity.getString(R.string.invite_as_friend, new Object[]{newsItem.sender.name}))
                                .setPositiveButton(R.string.yes, new SafeDialogInterfaceOnClickListener() {
                                    @Override
                                    public void safeOnClick(DialogInterface dialog, int which) {
                                        mActivity.progressDialog.show();
                                        mActivity.expectedEmailHash = newsItem.sender.email;
                                        mCurrentActionBtn = btn;
                                        mActivity.friendsPlugin.inviteFriend(newsItem.sender.email, null, null, true);
                                    }
                                }).setNegativeButton(R.string.no, null).create().show();
                    }

                }
            });
            GradientDrawable background = new GradientDrawable();
            background.setColor(ContextCompat.getColor(mActivity, R.color.mc_primary_color));
            background.setCornerRadii(getCorners(totalButtonCount, currentButton));
            btn.setBackground(background);
            ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) btn.getLayoutParams();
            marginParams.setMargins(0, 0, getMarginLeft(totalButtonCount, currentButton), 0);
        }
    }

    private float[] getCorners(int totalButtonCount, int currentButton) {
        // top-left, top-right, bottom-right, bottom-left.
        if (totalButtonCount == 1) {
            return new float[]{0, 0, 0, 0, _15_DP, _15_DP, _15_DP, _15_DP};
        }
        if (currentButton == 1) {
            return new float[]{0, 0, 0, 0, 0, 0, _15_DP, _15_DP};
        }
        if (currentButton == totalButtonCount) {
            return new float[]{0, 0, 0, 0, _15_DP, _15_DP, 0, 0};
        }
        return new float[]{0, 0, 0, 0, 0, 0, 0, 0};
    }

    private int getMarginLeft(int totalButtonCount, int currentButton) {
        return currentButton < totalButtonCount ? _1_DP : 0;
    }


    private void setupAvatar(final ViewHolder viewHolder, final NewsItem newsItem) {
        Bitmap avatar = mActivity.friendsPlugin.getStore().getAvatarBitmap(newsItem.sender.email);
        if (avatar == null) {
            new DownloadImageTask(viewHolder.serviceAvatar, true).execute(CloudConstants.CACHED_AVATAR_URL_PREFIX + newsItem.sender.avatar_id);
        } else {
            viewHolder.serviceAvatar.setImageBitmap(avatar);
        }

        viewHolder.serviceAvatar.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                final int existenceStatus = mActivity.friendsPlugin.getStore().getExistence(newsItem.sender.email);
                if (existenceStatus == Friend.ACTIVE) {
                    Intent intent = new Intent(mActivity, ServiceActionMenuActivity.class);
                    intent.putExtra(ServiceActionMenuActivity.SERVICE_EMAIL, newsItem.sender.email);
                    intent.putExtra(ServiceActionMenuActivity.MENU_PAGE, 0);
                    mActivity.startActivity(intent);
                } else if (existenceStatus == Friend.INVITE_PENDING) {
                    Intent intent = new Intent(mActivity, ServiceDetailActivity.class);
                    intent.putExtra(ServiceDetailActivity.EXISTENCE, existenceStatus);
                    intent.putExtra(ServiceDetailActivity.EMAIL, newsItem.sender.email);
                    mActivity.startActivity(intent);

                } else {
                    mActivity.existence = existenceStatus;
                    mActivity.expectedEmailHash = newsItem.sender.email;
                    mActivity.requestFriendInfoByEmailHash(mActivity.expectedEmailHash);
                }
            }
        });
    }

    private boolean setupQRCode(final ViewHolder viewHolder, final NewsItem newsItem) {
        if (newsItem.type == NewsItem.TYPE_QR_CODE) {
            if (mQRCodes.containsKey(newsItem.qr_code_content)) {
                setQRCode(viewHolder, newsItem);
            } else {
                viewHolder.qrCodeContainer.setVisibility(View.GONE);
                mMainService.postAtFrontOfBIZZHandler(new SafeRunnable() {
                    @Override
                    protected void safeRun() throws Exception {
                        Intent intent = new Intent();
                        intent.setAction(Intents.Encode.ACTION);
                        intent.putExtra(Intents.Encode.TYPE, Contents.Type.TEXT);
                        intent.putExtra(Intents.Encode.DATA, newsItem.qr_code_content);
                        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(mActivity, intent, mDisplayWidth / 2, false);
                        final Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();

                        mMainService.postAtFrontOfUIHandler(new SafeRunnable() {
                            @Override
                            protected void safeRun() throws Exception {
                                mQRCodes.put(newsItem.qr_code_content, bitmap);
                                setQRCode(viewHolder, newsItem);
                            }
                        });


                    }
                });
            }
            return true;
        } else {
            viewHolder.qrCodeContainer.setVisibility(View.GONE);
            return false;
        }
    }

    private boolean setupImage(final ViewHolder viewHolder, NewsItem newsItem) {
        if (TextUtils.isEmptyOrWhitespace(newsItem.image_url)) {
            viewHolder.image.setVisibility(View.GONE);
            return false;
        } else {
            int corderRadius = UIUtils.convertDipToPixels(mActivity, 20);
            if (mCachedDownloader.isStorageAvailable()) {
                File cachedFile = mCachedDownloader.getCachedFilePath(newsItem.image_url);
                if (cachedFile != null) {
                    Bitmap bm = BitmapFactory.decodeFile(cachedFile.getAbsolutePath());
                    if (newsItem.users_that_rogered.length == 0) {
                        viewHolder.image.setImageBitmap(ImageHelper.getRoundTopCornerBitmap(bm, corderRadius));
                    }
                    viewHolder.image.setVisibility(View.VISIBLE);
                } else {
                    if (!mImageViews.containsKey(newsItem.image_url)) {
                        mImageViews.put(newsItem.image_url, new ArrayList<Resizable16by6ImageView>());
                    }
                    mImageViews.get(newsItem.image_url).add(viewHolder.image);
                    // item started downloading intent when ready
                }
            } else if (newsItem.users_that_rogered.length == 0) {
                new DownloadImageTask(viewHolder.image, true, corderRadius).execute(newsItem.image_url);
            } else {
                new DownloadImageTask(viewHolder.image).execute(newsItem.image_url);
            }
        }
        return true;
    }

    private void setupOptions(final ViewHolder viewHolder, final NewsItem newsItem) {

        viewHolder.dropdownButton.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                final int existenceStatus = mActivity.friendsPlugin.getStore().getExistence(newsItem.sender.email);
                LinearLayout sheetView = (LinearLayout) mLayoutInflater.inflate(R.layout.news_options, null);

                if (newsItem.pinned) {
                    final View actionUnSave = mLayoutInflater.inflate(R.layout.news_options_item, null);
                    ((ImageView) actionUnSave.findViewById(R.id.icon)).setImageDrawable(new IconicsDrawable(mActivity, FontAwesome.Icon.faw_thumb_tack).color(ContextCompat.getColor(mActivity, R.color.mc_default_text)).sizeDp(20).paddingDp(2));
                    ((TextView) actionUnSave.findViewById(R.id.title)).setText(R.string.unsave);
                    ((TextView) actionUnSave.findViewById(R.id.subtitle)).setText(R.string.remove_this_from_your_saved_items);
                    actionUnSave.setOnClickListener(new SafeViewOnClickListener() {
                        @Override
                        public void safeOnClick(View v) {
                            mBottomSheetDialog.dismiss();
                            togglePinned(viewHolder, newsItem);
                        }
                    });
                    sheetView.addView(actionUnSave);
                } else {
                    final View actionSave = mLayoutInflater.inflate(R.layout.news_options_item, null);
                    ((ImageView) actionSave.findViewById(R.id.icon)).setImageDrawable(new IconicsDrawable(mActivity, FontAwesome.Icon.faw_thumb_tack).color(ContextCompat.getColor(mActivity, R.color.mc_default_text)).sizeDp(20).paddingDp(2));
                    ((TextView) actionSave.findViewById(R.id.title)).setText(R.string.save);
                    ((TextView) actionSave.findViewById(R.id.subtitle)).setText(R.string.add_this_to_your_saved_items);
                    actionSave.setOnClickListener(new SafeViewOnClickListener() {
                        @Override
                        public void safeOnClick(View v) {
                            mBottomSheetDialog.dismiss();
                            togglePinned(viewHolder, newsItem);
                        }
                    });
                    sheetView.addView(actionSave);
                }

                if (existenceStatus == Friend.ACTIVE) {
                    final View actionHide = mLayoutInflater.inflate(R.layout.news_options_item, null);
                    ((ImageView) actionHide.findViewById(R.id.icon)).setImageDrawable(new IconicsDrawable(mActivity, FontAwesome.Icon.faw_times_circle).color(ContextCompat.getColor(mActivity, R.color.mc_default_text)).sizeDp(20).paddingDp(2));
                    ((TextView) actionHide.findViewById(R.id.title)).setText(R.string.hide);
                    ((TextView) actionHide.findViewById(R.id.subtitle)).setText(mActivity.getString(R.string.hide_detail, newsItem.broadcast_type, newsItem.sender.name));
                    actionHide.setOnClickListener(new SafeViewOnClickListener() {
                        @Override
                        public void safeOnClick(View v) {
                            mBottomSheetDialog.dismiss();
                            mActivity.friendsPlugin.disableBroadcastType(newsItem.sender.email, newsItem.broadcast_type);
                            refreshView();
                        }
                    });
                    sheetView.addView(actionHide);
                }

                mBottomSheetDialog.setContentView(sheetView);
                mBottomSheetDialog.show();
            }
        });
    }

    private void setupPinned(final ViewHolder viewHolder, final NewsItem newsItem) {
        viewHolder.pinButton.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                togglePinned(viewHolder, newsItem);
            }
        });
        setupPinButton(viewHolder, newsItem.pinned);
    }

    private void setupRogeredUsers(final ViewHolder viewHolder, final NewsItem newsItem) {
        if (newsItem.users_that_rogered.length > 0) {
            Map<String, String> namesMap = new HashMap<>();
            for (String email : newsItem.users_that_rogered) {
                if (mMyEmail.equals(email)) {
                    namesMap.put(email, mMyName);

                } else {
                    String name = mActivity.friendsPlugin.getStore().getName(email);
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
                    viewHolder.members.setText(text);

                } else if (names.size() > 1) {
                    String namesPart = android.text.TextUtils.join(" & ", names);
                    final SpannableString text = new SpannableString(mActivity.getString(R.string.news_members_x, namesPart));
                    int splitIndex = namesPart.indexOf(" & ");
                    text.setSpan(new StyleSpan(Typeface.BOLD), 0, splitIndex,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    text.setSpan(new StyleSpan(Typeface.BOLD), splitIndex + 3, namesPart.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    viewHolder.members.setText(text);
                } else {
                    String namesPart = names.get(0);
                    final SpannableString text = new SpannableString(mActivity.getString(R.string.news_members_1, namesPart));
                    text.setSpan(new StyleSpan(Typeface.BOLD), 0, namesPart.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    viewHolder.members.setText(text);
                }

                viewHolder.membersContainer.setVisibility(View.VISIBLE);
                viewHolder.membersContainer.setOnClickListener(new SafeViewOnClickListener() {
                    @Override
                    public void safeOnClick(View v) {
                        Intent intent = new Intent(mActivity, MembersActivity.class);
                        intent.putExtra(MembersActivity.ME, mMyEmail);
                        intent.putExtra(MembersActivity.MEMBERS, newsItem.users_that_rogered);
                        mActivity.startActivity(intent);
                    }
                });
            } else {
                viewHolder.membersContainer.setVisibility(View.GONE);
            }
        } else {
            viewHolder.membersContainer.setVisibility(View.GONE);
        }
    }

    private void togglePinned(final ViewHolder viewHolder, final NewsItem newsItem) {
        mActivity.newsStore.setNewsItemPinned(newsItem.id, !newsItem.pinned);
        Intent intent = new Intent(NewsPlugin.PINNED_NEWS_ITEM_INTENT);
        intent.putExtra("id", newsItem.id);
        mMainService.sendBroadcast(intent);
        setupPinButton(viewHolder, newsItem.pinned);
    }

    private void setupPinButton(final ViewHolder viewHolder, boolean pinned) {
        int buttonColor = ContextCompat.getColor(mActivity, pinned ? R.color.mc_white : R.color.mc_primary_color);
        int backgroundColor = ContextCompat.getColor(mActivity, pinned ? R.color.mc_primary_color : R.color.mc_white);
        viewHolder.pinButton.setImageDrawable(new IconicsDrawable(mActivity).icon(FontAwesome.Icon.faw_thumb_tack).color(buttonColor).sizeDp(18));
        GradientDrawable background = (GradientDrawable) viewHolder.pinButton.getBackground();
        background.setColor(backgroundColor);
    }

    private void setQRCode(final ViewHolder viewHolder, final NewsItem newsItem) {
        viewHolder.qrCode.setImageBitmap(mQRCodes.get(newsItem.qr_code_content));
        viewHolder.qrCodeCaption.setText(newsItem.qr_code_caption);
        viewHolder.qrCodeContainer.setVisibility(View.VISIBLE);

        if (newsItem.users_that_rogered.length == 0 && TextUtils.isEmptyOrWhitespace(newsItem.image_url)) {
            viewHolder.qrCodeContainer.setPadding(0, _27_DP, 0, 0);
        }
    }

    public void handleIntent(Context context, Intent intent) {
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
        } else if (NewsPlugin.PINNED_NEWS_ITEM_INTENT.equals(action)) {
            if (mActivity instanceof NewsPinnedActivity) {
                if (mActivity.newsStore.countPinnedItems() > 0) {
                    long newsId = intent.getLongExtra("id", -1);
                    mItems.remove(newsId);
                    int index = mVisibleItems.indexOf(newsId);
                    mVisibleItems.remove(newsId);
                    notifyItemRemoved(index);
                } else {
                    mActivity.finish();
                }
            } else {
                mActivity.invalidateOptionsMenu();
            }
        } else if (NewsPlugin.DISABLE_NEWS_ITEM_INTENT.equals(action)) {
            updateView(intent.getLongExtra("id", -1));
        } else if (FriendsPlugin.SERVICE_DATA_UPDATED.equals(action)) {
            refreshView();
        } else if (FriendsPlugin.FRIEND_REMOVED_INTENT.equals(action) || FriendsPlugin.FRIEND_MARKED_FOR_REMOVAL_INTENT.equals(action)) {
            refreshItemsOfService(intent.getStringExtra("email"));
        } else if (FriendsPlugin.FRIEND_ADDED_INTENT.equals(action)) {
            String email = intent.getStringExtra("email");

            if (mActivity.expectedEmailHash != null && mActivity.expectedEmailHash.equals(email)) {
                final int existence = mActivity.friendsPlugin.getStore().getExistence(email);
                if (Friend.ACTIVE == existence) {
                    mActivity.progressDialog.dismiss();
                    mCurrentActionBtn.callOnClick();
                }
            }

            refreshItemsOfService(email);

        } else if (FriendsPlugin.FRIENDS_LIST_REFRESHED.equals(action)) {
            notifyDataSetChanged();
        }
    }
}