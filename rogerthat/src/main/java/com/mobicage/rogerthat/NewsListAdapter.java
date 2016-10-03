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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.mobicage.rogerthat.plugins.news.NewsPlugin;
import com.mobicage.rogerthat.plugins.news.NewsStore;
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
import com.mobicage.rogerthat.util.ui.ScaleImageView;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rogerthat.widget.Resizable16by6ImageView;
import com.mobicage.rpc.config.CloudConstants;
import com.mobicage.to.news.NewsActionButtonTO;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Adapter lives on UI thread
// It can be temporarily out of date with respect to the
// underlying database content:
//   * we cache the retrieved item count, in order to avoid ANR
//   * if a non-existing entry is retrieved from the db (e.g. item 5 of 4), we return an empty (non-null) view
// When database is changed, code must call notifyDataSetChanged() to refresh the adapter

class NewsListAdapter extends CursorAdapter {

    protected final static int FLAG_ACTION_ROGERTHAT = 1;
    protected final static int FLAG_ACTION_FOLLOW = 2;

    private final NewsActivity mActivity;
    private final MainService mMainService;
    private final LayoutInflater mLayoutInflater;
    private final NewsPlugin mNewsPlugin;
    private final NewsStore mStore;
    private final FriendsPlugin mFriendsPlugin;
    private final MessagingPlugin mMessagingPlugin;

    private CachedDownloader mCachedDownloader;

    private Map<String, Bitmap> mQRCodes = new HashMap<>();
    private Map<String, ArrayList<Resizable16by6ImageView>> mImageViews = new HashMap<>();
    private Set<Long> mReadMoreItems = new HashSet<>();

    private final String mMyEmail;
    private final String mMyName;
    private final int mDisplayWidth;

    public NewsListAdapter(NewsActivity activity, MainService mainService, Cursor cursor, NewsPlugin newsPlugin, NewsStore store, FriendsPlugin friendsPlugin) {
        super(activity, cursor, false);
        T.UI();
        mActivity = activity;
        mMainService = mainService;
        mLayoutInflater = LayoutInflater.from(mActivity);
        mNewsPlugin = newsPlugin;
        mStore = store;
        mFriendsPlugin = friendsPlugin;
        mMessagingPlugin = mMainService.getPlugin(MessagingPlugin.class);

        mCachedDownloader = CachedDownloader.getInstance(mMainService);

        MyIdentity myIdentity = mMainService.getIdentityStore().getIdentity();
        mMyEmail = myIdentity.getEmail();
        mMyName = myIdentity.getDisplayName();
        mDisplayWidth = UIUtils.getDisplayWidth(mActivity);
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        T.UI();
        final View view = mLayoutInflater.inflate(R.layout.news_list_item, parent, false);

        Cursor cursor = getCursor();
        if (!cursor.moveToPosition(position)) {
            L.e("couldn't move cursor to position " + position);
            return mLayoutInflater.inflate(R.layout.news_list_item_hidden, parent, false);
        }

        final ListView lv = mActivity.getListView();
        final NewsItem newsItem = mStore.readNewsItemFromCursor(cursor);

        if (mFriendsPlugin.isBroadcastTypeDisabled(newsItem.sender.email, newsItem.broadcast_type)) {
            return mLayoutInflater.inflate(R.layout.news_list_item_hidden, parent, false);
        }

        final int existenceStatus = mFriendsPlugin.getStore().getExistence(newsItem.sender.email);
        if (!newsItem.read) {
            newsItem.read = true;

            mMainService.postAtFrontOfBIZZHandler(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    mStore.setNewsItemRead(newsItem.id);
                }
            });

            mNewsPlugin.newsRead(new long[]{newsItem.id});
        }

        setupPinned(view, newsItem, existenceStatus);
        setupRogeredUsers(newsItem, view);
        setupImage(view, newsItem);

        final LinearLayout qrCodeContainer = (LinearLayout) view.findViewById(R.id.qr_code_container);
        setupQRCode(view, newsItem, qrCodeContainer);

        setupAvatar(view, newsItem, existenceStatus);

        TextView serviceName = (TextView) view.findViewById(R.id.service_name);
        serviceName.setText(newsItem.sender.name);

        TextView date = (TextView) view.findViewById(R.id.date);
        date.setText(TimeUtils.getDayTimeStr(mActivity, newsItem.timestamp * 1000));

        TextView title = (TextView) view.findViewById(R.id.title);
        if (TextUtils.isEmptyOrWhitespace(newsItem.title)) {
            title.setVisibility(View.GONE);
        } else {
            title.setText(newsItem.title);
            title.setVisibility(View.VISIBLE);

            if (newsItem.users_that_rogered.length == 0 &&
                    TextUtils.isEmptyOrWhitespace(newsItem.image_url) &&
                    qrCodeContainer.getVisibility() == View.GONE) {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) title.getLayoutParams();
                lp.setMargins(0, 0, UIUtils.convertDipToPixels(mActivity, 35), 0);
                title.setLayoutParams(lp);
            }
        }

        final TextView text = (TextView) view.findViewById(R.id.text);
        final TextView readmore = (TextView) view.findViewById(R.id.readmore);
        if (TextUtils.isEmptyOrWhitespace(newsItem.message)) {
            text.setVisibility(View.GONE);
            readmore.setVisibility(View.GONE);
        } else {
            text.setVisibility(View.VISIBLE);
            text.setText(newsItem.message);
            mMainService.postOnUIHandler(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    int lineCount = text.getLineCount();
                    if (lineCount > 5) {
                        readmore.setVisibility(View.VISIBLE);
                        if (mReadMoreItems.contains(newsItem.id)) {
                            text.setMaxLines(lineCount);
                            readmore.setText(R.string.read_less);
                        } else {
                            text.setMaxLines(5);
                            readmore.setText(R.string.read_more);
                        }

                        readmore.setOnClickListener(new SafeViewOnClickListener() {
                            @Override
                            public void safeOnClick(View v) {
                                if (mReadMoreItems.contains(newsItem.id)) {
                                    mReadMoreItems.remove(newsItem.id);
                                } else {
                                    mReadMoreItems.add(newsItem.id);
                                }
                                mActivity.refreshCursor();
                                if (!mReadMoreItems.contains(newsItem.id)) {
                                    lv.setSelection(position);
                                }
                            }
                        });
                    } else {
                        readmore.setVisibility(View.GONE);
                    }
                }
            });
        }
        TextView reach = (TextView) view.findViewById(R.id.reach);
        reach.setText(newsItem.reach + "");
        TextView broadcastType = (TextView) view.findViewById(R.id.broadcast_type);
        broadcastType.setText("[" + newsItem.broadcast_type + "]");

        setupButtons(position, convertView, parent, view, newsItem, existenceStatus);

        if (newsItem.disabled) {
            view.findViewById(R.id.image).setAlpha(0.4f);
            view.findViewById(R.id.qr_code_container).setAlpha(0.4f);
            view.findViewById(R.id.details).setAlpha(0.4f);
        } else {
            view.findViewById(R.id.image).setAlpha(1f);
            view.findViewById(R.id.qr_code_container).setAlpha(1f);
            view.findViewById(R.id.details).setAlpha(1f);
        }

        return view;
    }

    private void setupButtons(final int position, final View convertView, final ViewGroup parent, View view, final NewsItem newsItem, int existenceStatus) {
        LinearLayout actions = (LinearLayout) view.findViewById(R.id.actions);
        actions.removeAllViews();

        if (SystemUtils.isFlagEnabled(newsItem.flags, FLAG_ACTION_ROGERTHAT)) {
            final Button btn = (Button) mLayoutInflater.inflate(R.layout.news_list_item_action, parent, false);
            btn.setText(mActivity.getString(R.string.rogerthat));

            if (newsItem.rogered) {
                btn.setBackgroundColor(mActivity.getResources().getColor(R.color.mc_divider_gray));
            } else {
                btn.setBackgroundColor(mActivity.getResources().getColor(R.color.mc_primary_color));
                btn.setOnClickListener(new SafeViewOnClickListener() {
                    @Override
                    public void safeOnClick(View v) {
                        mNewsPlugin.newsRogered(newsItem.id);

                        mMainService.postAtFrontOfBIZZHandler(new SafeRunnable() {
                            @Override
                            protected void safeRun() throws Exception {
                                mStore.setNewsItemRogered(newsItem.id);
                                mStore.addUser(newsItem.id, mMyEmail);

                                final NewsItem ni = mStore.getNewsItem(newsItem.id);
                                mMainService.postAtFrontOfUIHandler(new SafeRunnable() {
                                    @Override
                                    protected void safeRun() throws Exception {
                                        getView(position, convertView, parent);
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
            btn.setText(mActivity.getString(R.string.follow));


            if (Friend.ACTIVE == existenceStatus) {
                btn.setBackgroundColor(mActivity.getResources().getColor(R.color.mc_divider_gray));
            } else {
                btn.setBackgroundColor(mActivity.getResources().getColor(R.color.mc_primary_color));
                btn.setOnClickListener(new SafeViewOnClickListener() {
                    @Override
                    public void safeOnClick(View v) {
                        mFriendsPlugin.inviteFriend(newsItem.sender.email, null, null, false);
                        btn.setBackgroundColor(mActivity.getResources().getColor(R.color.mc_divider_gray));
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
                    }
                });
            } else {
                btn.setOnClickListener(new SafeViewOnClickListener() {
                    @Override
                    public void safeOnClick(View v) {
                        new AlertDialog.Builder(mActivity)
                                .setMessage(mActivity.getString(R.string.invite_as_friend, new Object[]{newsItem.sender.name}))
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

            if (newsItem.buttons.length > i + 1) {
                View spacer = mLayoutInflater.inflate(R.layout.news_list_item_action_spacer, parent, false);
                actions.addView(spacer);
            }
        }
    }

    private void setupAvatar(View view, final NewsItem newsItem, final int existenceStatus) {
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

    private void setupQRCode(final View view, final NewsItem newsItem, final LinearLayout qrCodeContainer) {
        if (newsItem.type == NewsItem.TYPE_QR_CODE) {
            if (mQRCodes.containsKey(newsItem.qr_code_content)) {
                setQRCode(newsItem, view, qrCodeContainer);
            } else {
                qrCodeContainer.setVisibility(View.GONE);
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
                                setQRCode(newsItem, view, qrCodeContainer);
                            }
                        });


                    }
                });
            }
        } else {
            qrCodeContainer.setVisibility(View.GONE);
        }
    }

    private void setupImage(View view, NewsItem newsItem) {
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
    }

    private void setupPinned(View view, final NewsItem newsItem, final int existenceStatus) {
        final ImageButton pinned = (ImageButton) view.findViewById(R.id.pinned);
        pinned.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                final LinearLayout dialog = (LinearLayout) mLayoutInflater.inflate(R.layout.news_actions, null);

                final AlertDialog alertDialog = new AlertDialog.Builder(mActivity)
                        .setView(dialog)
                        .create();

                if (newsItem.pinned) {
                    final View actionUnSave = mLayoutInflater.inflate(R.layout.news_actions_item, null);
                    ((ImageView) actionUnSave.findViewById(R.id.icon)).setImageDrawable(new IconicsDrawable(mActivity, FontAwesome.Icon.faw_bookmark).color(mActivity.getResources().getColor(R.color.mc_default_text)).sizeDp(15).paddingDp(2));
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
                    final View actionSave = mLayoutInflater.inflate(R.layout.news_actions_item, null);
                    ((ImageView) actionSave.findViewById(R.id.icon)).setImageDrawable(new IconicsDrawable(mActivity, FontAwesome.Icon.faw_bookmark).color(mActivity.getResources().getColor(R.color.mc_default_text)).sizeDp(15).paddingDp(2));
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

                if (existenceStatus == Friend.ACTIVE) {
                    final View actionHide = mLayoutInflater.inflate(R.layout.news_actions_item, null);
                    ((ImageView) actionHide.findViewById(R.id.icon)).setImageDrawable(new IconicsDrawable(mActivity, FontAwesome.Icon.faw_times_circle).color(mActivity.getResources().getColor(R.color.mc_default_text)).sizeDp(15).paddingDp(2));
                    ((TextView) actionHide.findViewById(R.id.title)).setText(R.string.hide);
                    ((TextView) actionHide.findViewById(R.id.subtitle)).setText(R.string.see_fewer_posts_like_this);
                    actionHide.setOnClickListener(new SafeViewOnClickListener() {
                        @Override
                        public void safeOnClick(View v) {
                            alertDialog.dismiss();
                            mFriendsPlugin.disableBroadcastType(newsItem.sender.email, newsItem.broadcast_type);
                            mActivity.refreshCursor();
                        }
                    });
                    dialog.addView(actionHide);
                }


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
    }

    private void setupRogeredUsers(final NewsItem newsItem, final View view) {
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

            if (names.size() > 0) {
                if (names.size() > 2) {
                    final SpannableString text = new SpannableString(mActivity.getString(R.string.news_members_and_x_others, names.get(0), names.size() - 1));
                    text.setSpan(new StyleSpan(Typeface.BOLD), 0, names.get(0).length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    members.setText(text);

                } else if (names.size() > 1) {
                    String namesPart = android.text.TextUtils.join(" & ", names);
                    final SpannableString text = new SpannableString(mActivity.getString(R.string.news_members_x, namesPart));
                    int splitIndex = namesPart.indexOf(" & ");
                    text.setSpan(new StyleSpan(Typeface.BOLD), 0, splitIndex,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    text.setSpan(new StyleSpan(Typeface.BOLD), splitIndex + 3, namesPart.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    members.setText(text);
                } else {
                    String namesPart = names.get(0);
                    final SpannableString text = new SpannableString(mActivity.getString(R.string.news_members_1, namesPart));
                    text.setSpan(new StyleSpan(Typeface.BOLD), 0, namesPart.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    members.setText(text);
                }

                membersContainer.setVisibility(View.VISIBLE);
                membersContainer.setOnClickListener(new SafeViewOnClickListener() {
                    @Override
                    public void safeOnClick(View v) {
                        Intent intent = new Intent(mActivity, MembersActivity.class);
                        intent.putExtra(MembersActivity.ME, mMyEmail);
                        intent.putExtra(MembersActivity.MEMBERS, newsItem.users_that_rogered);
                        mActivity.startActivity(intent);
                    }
                });
            } else {
                membersContainer.setVisibility(View.GONE);
            }
        } else {
            membersContainer.setVisibility(View.GONE);
        }
    }

    private void togglePinned(final NewsItem newsItem) {
        final boolean isPinned = !newsItem.pinned;
        newsItem.pinned = isPinned;

        mStore.setNewsItemPinned(newsItem.id, isPinned);

        Intent intent = new Intent(NewsPlugin.PINNED_NEWS_ITEM_INTENT);
        intent.putExtra("id", newsItem.id);
        intent.putExtra("pinned", newsItem.pinned);
        mMainService.sendBroadcast(intent);
    }

    private void setQRCode(final NewsItem newsItem, final View view, final LinearLayout qrCodeContainer) {
        ScaleImageView qrCode = (ScaleImageView) view.findViewById(R.id.qr_code);
        TextView qrCodeCaption = (TextView) view.findViewById(R.id.qr_code_caption);

        qrCode.setImageBitmap(mQRCodes.get(newsItem.qr_code_content));
        qrCodeCaption.setText(newsItem.qr_code_caption);
        qrCodeContainer.setVisibility(View.VISIBLE);

        if (newsItem.users_that_rogered.length == 0 && TextUtils.isEmptyOrWhitespace(newsItem.image_url)) {
            qrCodeCaption.setPadding(0, 0, UIUtils.convertDipToPixels(mActivity, 35), UIUtils.convertDipToPixels(mActivity, 15));
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
                L.d("url: " + url);
                L.d("mImageViews.containsKey(url): " + mImageViews.containsKey(url));
                L.d("mImageViews.get(url).size(): " + mImageViews.get(url).size());
                for (Resizable16by6ImageView image : mImageViews.get(url)) {
                    image.setImageBitmap(bm);
                    image.setVisibility(View.VISIBLE);
                }
            }
        } else if (NewsPlugin.PINNED_NEWS_ITEM_INTENT.equals(action)) {
            if (mActivity instanceof NewsPinnedActivity) {
                if (mStore.countPinnedItems() > 0) {
                    mActivity.refreshCursor();
                } else {
                    mActivity.finish();
                }
            } else {
                mActivity.invalidateOptionsMenu();
            }
        }
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        L.bug("Should not come here");
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        L.bug("Should not come here");
        return null;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }
}
