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
package com.mobicage.rogerthat.plugins.messaging;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.FriendDetailOrInviteActivity;
import com.mobicage.rogerthat.IdentityStore;
import com.mobicage.rogerthat.ServiceBoundCursorListActivity;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.scan.ProfileActivity;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeDialogInterfaceOnClickListener;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.time.TimeUtils;
import com.mobicage.rogerthat.util.ui.ImageHelper;
import com.mobicage.rogerthat.util.ui.SendMessageView;
import com.mobicage.rogerthat.util.ui.Slider;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.IncompleteMessageException;
import com.mobicage.rpc.config.AppConstants;
import com.mobicage.to.messaging.ButtonTO;
import com.mobicage.to.messaging.MemberStatusTO;
import com.mobicage.to.messaging.MessageTO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import thirdparty.nishantnair.FlowLayout;
import thirdparty.nishantnair.FlowLayoutRTL;

public class FriendsThreadActivity extends ServiceBoundCursorListActivity {

    public final static String ANDROID_TEL_PREFIX = "tel:";
    public final static String ANDROID_HTTP_PREFIX = "http://";
    public final static String ANDROID_HTTPS_PREFIX = "https://";
    public final static String ANDROID_GEO_PREFIX = "geo:";

    public static final String PARENT_MESSAGE_KEY = "parent_message_key";
    public static final String MESSAGE_FLAGS = "message_flags";

    public final static String BUTTON_INFO = "buttonInfo";

    private final static String HINT_SWIPE = "com.mobicage.rogerthat.plugins.messaging.FriendsThreadActivity.HINT_SWIPE";

    private boolean mScrollToBottomOnUpdate = false;
    private String mParentMessageKey;
    private MessagingPlugin mMessagingPlugin;
    private MessageStore mMessageStore;
    private FriendsPlugin mFriendsPlugin;
    private String mMyEmail;
    private Scroller mScroller;
    private int mMessageCount;
    private GestureDetector mGestureScanner;
    private long mFlags;
    private Message mParentMessage;
    private Set<String> mRenderedMessages;

    private SendMessageView mSendMessageView;

    private int _1_DP_IN_PX;
    private int _3_DP_IN_PX;
    private int _4_DP_IN_PX;
    private int _42_DP_IN_PX;
    private int _48_DP_IN_PX;

    public static Intent createIntent(Context context, String threadKey, long messageFlags, String memberFilter) {
        Intent intent = new Intent(context, FriendsThreadActivity.class);
        intent.putExtra(FriendsThreadActivity.PARENT_MESSAGE_KEY, threadKey);
        intent.putExtra(FriendsThreadActivity.MESSAGE_FLAGS, messageFlags);
        intent.putExtra(MessagingPlugin.MEMBER_FILTER, memberFilter);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.message_thread);
        setNavigationBarBurgerVisible(false, true);

        _1_DP_IN_PX = UIUtils.convertDipToPixels(this, 1);
        _3_DP_IN_PX = UIUtils.convertDipToPixels(this, 3);
        _4_DP_IN_PX = UIUtils.convertDipToPixels(this, 4);
        _42_DP_IN_PX = UIUtils.convertDipToPixels(this, 42);
        _48_DP_IN_PX = UIUtils.convertDipToPixels(this, 48);

        mRenderedMessages = new HashSet<String>();
        final Intent intent = getIntent();
        mParentMessageKey = intent.getStringExtra(PARENT_MESSAGE_KEY);
        mFlags = intent.getLongExtra(MESSAGE_FLAGS, 0);
        setListView((ListView) findViewById(R.id.thread_messages));
        mScroller = Scroller.getInstance();
        ListView listView = getListView();
        listView.setDivider(null);
        listView.setVerticalScrollBarEnabled(false);
        mScroller.setListView(listView);

        final IntentFilter filter1 = new IntentFilter();
        for (String action : getAllReceivingIntents()) {
            filter1.addAction(action);
        }
        registerReceiver(getDefaultBroadcastReceiver(), filter1);

        IntentFilter filter2 = new IntentFilter(MessagingPlugin.NEW_MESSAGE_QUEUED_TO_BACKLOG_INTENT);
        filter2.addAction(MessagingPlugin.THREAD_DELETED_INTENT);
        filter2.addAction(BrandingMgr.ATTACHMENT_AVAILABLE_INTENT);
        filter2.addAction(MessagingPlugin.THREAD_MODIFIED_INTENT);
        filter2.addAction(FriendsPlugin.FRIEND_INFO_RECEIVED_INTENT);
        registerReceiver(mReceiver, filter2);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        unregisterReceiver(getDefaultBroadcastReceiver());
        if (mMessagingPlugin != null) {
            mMessagingPlugin.cleanThreadDirtyFlags(mParentMessageKey);
            if (!SystemUtils.isFlagEnabled(mFlags, MessagingPlugin.FLAG_DYNAMIC_CHAT) && !mParentMessage.threadDirty) {
                List<String> dirties = new ArrayList<String>(mRenderedMessages.size());
                for (String key : mRenderedMessages)
                    dirties.add(key);
                mMessagingPlugin.markMessagesAsRead(mParentMessageKey, dirties.toArray(new String[dirties.size()]));
            }
        }
        super.onDestroy();
    }

    @Override
    protected void onServiceBound() {
        mMessagingPlugin = mService.getPlugin(MessagingPlugin.class);
        mFriendsPlugin = mService.getPlugin(FriendsPlugin.class);
        mMessageStore = mMessagingPlugin.getStore();
        mMyEmail = mService.getIdentityStore().getIdentity().getEmail();
        reloadMessage();
        createCursor();
        mMessageCount = getCursor().getCount();
        startManagingCursor(getCursor());
        setListAdapter(new MessageThreadAdapter(this, getCursor()));
        scrollToBeAckedPosition(getCursor());

        String memberFilter = getIntent().getStringExtra(MessagingPlugin.MEMBER_FILTER);
        Slider instance = new Slider(this, this,
            new LeftSwiper(this, mMessagingPlugin, mParentMessageKey, memberFilter), new RightSwiper(this,
                mMessagingPlugin, mParentMessageKey, memberFilter));
        mGestureScanner = new GestureDetector(instance);

        UIUtils.showHint(this, mService, HINT_SWIPE, R.string.hint_swipe);

        mSendMessageView = (SendMessageView) findViewById(R.id.chat_container);
        if (SystemUtils.isFlagEnabled(mFlags, MessagingPlugin.FLAG_ALLOW_REPLY)) {
            mSendMessageView.setActive(this, mService, null, null, mParentMessageKey, mFlags, mParentMessageKey, mParentMessage.default_priority, mParentMessage.default_sticky);
        } else {
            mSendMessageView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onServiceUnbound() {
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (!mServiceIsBound) {
            addOnServiceBoundRunnable(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    onActivityResult(requestCode, resultCode, data);
                }
            });
            return;
        }

        mSendMessageView.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    }

    @Override
    protected boolean onListItemLongClick(ListView l, View v, int position, long id) {
        return false;
    }

    private void createCursor() {
        setCursor(mMessageStore.getFullThreadCursor(mParentMessageKey));
    }

    @Override
    protected void changeCursor() {
        final int lastPosition = getListView().getLastVisiblePosition();
        final int oldCount = getCursor().getCount();

        if (mServiceIsBound) {
            createCursor();
            MessageThreadAdapter mta = (MessageThreadAdapter) getListAdapter();
            mta.cursor = getCursor();
            mta.changeCursor(getCursor());
        }

        final int count = getCursor().getCount();
        reloadMessage();
        L.d("changeCursor: count = " + count);
        L.d("changeCursor: mMessageCount = " + mMessageCount);
        L.d("changeCursor: mScrollToBottomOnUpdate = " + mScrollToBottomOnUpdate);
        if ((mScrollToBottomOnUpdate && count > mMessageCount) || lastPosition == oldCount - 1 && count > oldCount) {
            mScrollToBottomOnUpdate = false;
            mMessageCount = count;
            mService.postDelayedOnUIHandler(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    scrollToPosition(count - 1);
                }
            }, 500);
        }
    }

    private void reloadMessage() {
        mParentMessage = mMessageStore.getFullMessageByKey(mParentMessageKey);
        mFlags = mParentMessage.flags;
        displayMembers(mParentMessage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        T.UI();
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.message_thread_menu, menu);
        inflater.inflate(R.menu.thread_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            switch (item.getItemId()) {
                case R.id.members:
                    item.setVisible(!SystemUtils.isFlagEnabled(mFlags, MessagingPlugin.FLAG_DYNAMIC_CHAT));
                    break;
                case R.id.delete_conversation:
                    item.setVisible(!SystemUtils.isFlagEnabled(mFlags, MessagingPlugin.FLAG_NOT_REMOVABLE));
                    break;
                case R.id.info:
                    item.setVisible(SystemUtils.isFlagEnabled(mFlags, MessagingPlugin.FLAG_DYNAMIC_CHAT));
                    break;
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        T.UI();
        switch (item.getItemId()) {
            case R.id.members:
                Intent intent = new Intent(this, MembersActivity.class);
                String[] members = new String[mParentMessage.members.length];
                for (int i = 0; i < mParentMessage.members.length; i++) {
                    members[i] = mParentMessage.members[i].member;
                }
                intent.putExtra(MembersActivity.ME, mMyEmail);
                intent.putExtra(MembersActivity.MEMBERS, members);
                startActivity(intent);
                return true;
            case R.id.help:
                new AlertDialog.Builder(FriendsThreadActivity.this).setTitle(R.string.help)
                    .setMessage(getString(R.string.message_thread_help)).setPositiveButton(getString(R.string.ok), null)
                    .create().show();
                return true;
            case R.id.delete_conversation:
                mMessagingPlugin.removeConversationFromList(this, mParentMessageKey);
                return true;
            case R.id.info:
                startActivity(ChatInfoActivity.createIntent(this, mParentMessageKey));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private final BroadcastReceiver mReceiver = new SafeBroadcastReceiver() {

        @Override
        public String[] onSafeReceive(Context context, Intent intent) {
            if (MessagingPlugin.NEW_MESSAGE_QUEUED_TO_BACKLOG_INTENT.equals(intent.getAction())) {
                mScrollToBottomOnUpdate = true;
                L.d("Will scroll down on next update!");
                return new String[] { intent.getAction() };
            }
            if (MessagingPlugin.THREAD_DELETED_INTENT.equals(intent.getAction())) {
                if (mParentMessageKey.equals(intent.getStringExtra("key"))) {
                    finish();
                    return new String[] { intent.getAction() };
                }
            }
            if (BrandingMgr.ATTACHMENT_AVAILABLE_INTENT.equals(intent.getAction())) {
                if (mParentMessageKey.equals(intent.getStringExtra(BrandingMgr.THREAD_KEY))) {
                    refreshCursor();
                    return new String[] { intent.getAction() };
                }
            }
            if (MessagingPlugin.THREAD_MODIFIED_INTENT.equals(intent.getAction())) {
                if (mParentMessageKey.equals(intent.getStringExtra("thread_key"))) {
                    refreshCursor();
                    return new String[] { intent.getAction() };
                }
            }
            if (FriendsPlugin.FRIEND_INFO_RECEIVED_INTENT.equals(intent.getAction())) {
                if (intent.getBooleanExtra("success", false)) {
                    refreshCursor();
                    return new String[] { intent.getAction() };
                }
            }
            return null;
        }
    };

    private void scrollToPosition(int position) {
        mScroller.scrollToPosition(position);
    }

    private class MessageThreadAdapter extends CursorAdapter {

        private final LayoutInflater mInflator;
        private final Context mContext;
        private Cursor cursor;

        public MessageThreadAdapter(Context context, Cursor c) {
            super(context, c, false);
            mInflator = getLayoutInflater();
            mContext = context;
            cursor = c;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            cursor.moveToPosition(position);
            return populateView(mContext, cursor);
        }

        private View populateView(Context context, Cursor c) {
            // Get message intelligence
            final Message message = mMessageStore.readFullMessageFromCursor(c);
            mRenderedMessages.add(message.key);
            boolean isSender = message.sender.equals(mMyEmail);
            boolean left = isSender;
            View view = mInflator
                .inflate(left ? R.layout.threaded_message_left : R.layout.threaded_message_right, null);
            view.setTag(message);

            // Populate screen
            final boolean isChat = SystemUtils.isFlagEnabled(message.flags, MessagingPlugin.FLAG_DYNAMIC_CHAT);
            final boolean allowChatButtons = SystemUtils.isFlagEnabled(message.flags,
                MessagingPlugin.FLAG_ALLOW_CHAT_BUTTONS);
            String senderName = setMessageInfo(context, view, isSender, message, isChat);
            setSenderAvatar(context, view, c, message);
            setMessage(message, context, view);
            setAttachments(message, context, view);
            if (!isChat || allowChatButtons) {
                boolean isLocked = SystemUtils.isFlagEnabled(message.flags, MessagingPlugin.FLAG_LOCKED);
                boolean canEdit = isLocked;
                if (!canEdit && allowChatButtons) {
                    canEdit = true;
                } else {
                    canEdit = !isLocked && iAmMember(message);
                }
                addButtons(c, message, view, left, canEdit, senderName, isChat);
            } else {
                mMessagingPlugin.ackChat(message.getThreadKey());
            }

            return view;
        }

        private void setAttachments(final Message message, final Context context, final View view) {
            if (message.attachments.length > 0) {
                final String threadKey = message.parent_key == null ? message.key : message.parent_key;
                final File attachmentsDir;
                try {
                    attachmentsDir = mMessagingPlugin.attachmentsDir(threadKey, message.key);
                } catch (IOException e) {
                    L.d("Unable to create attachment directory", e);
                    UIUtils.showAlertDialog(mService, "", R.string.unable_to_read_write_sd_card);
                    return;
                }

                final LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.message_attachments);

                for (int i = 0; i < message.attachments.length; i++) {
                    final AttachmentDownload attachment;
                    try {
                        attachment = new AttachmentDownload(message.attachments[i], threadKey, message.key);
                    } catch (IncompleteMessageException e) {
                        L.bug("Should never happen", e);
                        continue;
                    }

                    final String downloadUrlHash = mMessagingPlugin.attachmentDownloadUrlHash(attachment.download_url);
                    final File attachmentFile = new File(attachmentsDir, downloadUrlHash);

                    if (!attachmentFile.exists()
                        && mMessagingPlugin.getBrandingMgr().isAttachmentInBrandingQueue(attachment.threadKey,
                            attachment.messageKey, attachment.download_url)) {
                        L.i("show spinner");
                        // Show spinner if in queue and disable click
                        ProgressBar spinner = (ProgressBar) view.findViewById(R.id.spinner);
                        spinner.setVisibility(View.VISIBLE); // todo ruben

                    } else {
                        final ImageView attachmentImageView = (ImageView) View.inflate(mContext,
                            R.layout.threaded_message_attachment, null);
                        linearLayout.addView(attachmentImageView);

                        // Set 10dp margin bottom. Setting it in the xml did not work
                        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) attachmentImageView
                            .getLayoutParams();
                        final int dpi5 = UIUtils.convertDipToPixels(mContext, 5);
                        layoutParams.setMargins(0, dpi5, 0, dpi5);

                        final Bitmap thumbnail = setAttachmentThumbnail(attachment, attachmentFile, attachmentImageView);
                        final boolean generateThumbnail = thumbnail == null;

                        attachmentImageView.setOnClickListener(new SafeViewOnClickListener() {
                            @Override
                            public void safeOnClick(View v) {
                                Intent i = new Intent(FriendsThreadActivity.this, AttachmentViewerActivity.class);
                                i.putExtra("thread_key", attachment.threadKey);
                                i.putExtra("message", attachment.messageKey);
                                i.putExtra("content_type", attachment.content_type);
                                i.putExtra("download_url", attachment.download_url);
                                i.putExtra("name", attachment.name);
                                i.putExtra("download_url_hash", downloadUrlHash);
                                i.putExtra("generate_thumbnail", generateThumbnail);

                                startActivity(i);
                            }
                        });

                        linearLayout.setVisibility(View.VISIBLE);
                    }
                }
            }
        }

        private Bitmap setAttachmentThumbnail(final AttachmentDownload attachment, final File attachmentFile,
            final ImageView messageAttachmentView) {
            final boolean isImage = attachment.content_type.toLowerCase(Locale.US).startsWith("image/");
            final boolean isVideo = !isImage && attachment.content_type.toLowerCase(Locale.US).startsWith("video/");

            Bitmap thumbnail = null;
            if (isImage || isVideo) {
                final File thumbnailFile = new File(attachmentFile.getAbsolutePath() + ".thumb");
                if (thumbnailFile.exists()) {
                    thumbnail = ImageHelper.getBitmapFromFile(thumbnailFile.getAbsolutePath());
                }
            }

            if (thumbnail != null) {
                Drawable d = new BitmapDrawable(Resources.getSystem(),
                    ImageHelper.getRoundedCornerBitmap(thumbnail, 10));
                messageAttachmentView.setImageDrawable(d);
            } else {
                // No thumbnail available
                if (isImage) {
                    messageAttachmentView.setImageResource(R.drawable.attachment_img);
                } else if (isVideo) {
                    messageAttachmentView.setImageResource(R.drawable.attachment_video);
                } else if (AttachmentViewerActivity.CONTENT_TYPE_PDF.equals(attachment.content_type)) {
                    messageAttachmentView.setImageResource(R.drawable.attachment_pdf);
                } else {
                    L.d("attachment.content_type not known: " + attachment.content_type);
                    messageAttachmentView.setImageResource(R.drawable.attachment_unknown);
                }
            }

            return thumbnail;
        }

        private void addButtons(final Cursor cursor, final Message message, View view, boolean left, boolean canEdit,
            String senderName, boolean isChat) {
            LinearLayout buttons = (LinearLayout) view.findViewById(R.id.buttons);
            buttons.removeAllViews();
            for (ButtonTO button : message.buttons) {
                addButton(cursor, message, left, canEdit, senderName, buttons, button, isChat);
            }
            if (!isChat) {
                boolean addRogerthatButton = message.threadNeedsMyAnswer
                    && cursor.getPosition() == cursor.getCount() - 1;
                Button rogerThatButton = (Button) view.findViewById(R.id.rogerthat_button);
                rogerThatButton.setVisibility(addRogerthatButton ? View.VISIBLE : View.GONE);
                if (addRogerthatButton) {
                    rogerThatButton.getBackground()
                        .setColorFilter(Message.GREEN_BUTTON_COLOR, PorterDuff.Mode.MULTIPLY);
                    rogerThatButton.setOnClickListener(new SafeViewOnClickListener() {
                        @Override
                        public void safeOnClick(View v) {
                            mMessagingPlugin.ackThread(message.getThreadKey());
                        }
                    });
                }
            }
        }

        private void addButton(final Cursor cursor, final MessageTO message, boolean left, boolean canEdit,
            String senderName, final LinearLayout buttons, final ButtonTO button, final boolean isChat) {
            RelativeLayout buttonContainer = (RelativeLayout) mInflator.inflate(
                left ? R.layout.message_thread_member_detail_right : R.layout.message_thread_member_detail_left, null);
            buttons.addView(buttonContainer);
            Button buttonView = (Button) buttonContainer.findViewById(R.id.button);
            buttonView.setText(button.caption);
            if (button.id == null) {
                buttonView.getBackground().setColorFilter(Message.GREEN_BUTTON_COLOR, PorterDuff.Mode.MULTIPLY);
            } else {
                buttonView.getBackground().setColorFilter(Message.BLUE_BUTTON_COLOR, PorterDuff.Mode.MULTIPLY);
            }
            buttonView.setEnabled(canEdit);

            Map<String, String> actionInfo = mMessagingPlugin.getButtonActionInfo(button);
            final String buttonAction = actionInfo.get("androidAction");
            final String buttonUrl = actionInfo.get("androidUrl");

            buttonView.setOnClickListener(new SafeViewOnClickListener() {
                @Override
                public void safeOnClick(View v) {
                    T.UI();
                    if (buttonAction == Message.MC_CONFIRM_PREFIX) {
                        askConfirmation(message, button, buttonUrl);
                    } else {
                        if (buttonAction != null) {
                            final Intent intent = new Intent(buttonAction, Uri.parse(buttonUrl));
                            startActivity(intent);
                        }
                        ackMessage(message, button);
                    }
                }

                private void ackMessage(final MessageTO message, final ButtonTO button) {
                    String id = button.id;
                    boolean foundMemberStatus = false;
                    for (MemberStatusTO ms : message.members) {
                        if (ms.member.equals(mMyEmail)) {
                            foundMemberStatus = true;
                            if (id != null && id.equals(ms.button_id))
                                id = null;
                            break;
                        }
                    }
                    if (isChat && !foundMemberStatus) {
                        MemberStatusTO ms = new MemberStatusTO();
                        ms.acked_timestamp = 0;
                        ms.button_id = null;
                        ms.custom_reply = null;
                        ms.member = mMyEmail;
                        ms.received_timestamp = 0;
                        ms.status = 0;
                        mMessagingPlugin.getStore().insertMemberStatusBIZZ(message.parent_key, message.key, ms);
                    }

                    mMessagingPlugin.ackMessage(message, id, null, null, FriendsThreadActivity.this, buttons);
                }

                private void askConfirmation(final MessageTO message, final ButtonTO button, final String text) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(FriendsThreadActivity.this);
                    builder.setTitle(R.string.message_confirm);
                    builder.setMessage(text);
                    builder.setPositiveButton(R.string.yes, new SafeDialogInterfaceOnClickListener() {
                        @Override
                        public void safeOnClick(DialogInterface dialog, int which) {
                            T.UI();
                            dialog.dismiss();
                            ackMessage(message, button);
                        }
                    });
                    builder.setNegativeButton(R.string.no, new SafeDialogInterfaceOnClickListener() {
                        @Override
                        public void safeOnClick(DialogInterface dialog, int which) {
                            T.UI();
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                }
            });
            ViewGroup members = (ViewGroup) buttonContainer.findViewById(R.id.members);
            for (final MemberStatusTO member : message.members) {
                if ((member.status & MessagingPlugin.STATUS_ACKED) != MessagingPlugin.STATUS_ACKED)
                    continue;
                if ((button.id == null && member.button_id == null && !message.sender.equals(member.member))
                    || (button.id != null && button.id.equals(member.button_id))) {
                    ImageView avatar = new ImageView(mContext);
                    final SafeRunnable friendNotFoundRunnable;
                    if (isChat) {
                        friendNotFoundRunnable = new SafeRunnable() {
                            @Override
                            protected void safeRun() throws Exception {
                                mFriendsPlugin.requestUserInfo(member.member, true);
                            }
                        };
                    } else {
                        friendNotFoundRunnable = null;
                    }
                    avatar.setImageBitmap(mFriendsPlugin
                        .getAvatarBitmap(member.member, !isChat, friendNotFoundRunnable));
                    avatar.setBackgroundResource(R.drawable.avatar_background_black);
                    avatar.setPadding(_1_DP_IN_PX, _1_DP_IN_PX, _1_DP_IN_PX, _1_DP_IN_PX);
                    configureAvatarOnClickListener(member.member, avatar, isChat);
                    if (left) { // RTL
                        avatar.setLayoutParams(new FlowLayout.LayoutParams(_42_DP_IN_PX, _42_DP_IN_PX, _4_DP_IN_PX,
                            _4_DP_IN_PX));
                    } else {
                        avatar.setLayoutParams(new FlowLayoutRTL.LayoutParams(_42_DP_IN_PX, _42_DP_IN_PX, _4_DP_IN_PX,
                            _4_DP_IN_PX));
                    }
                    members.addView(avatar);
                }
            }
        }

        private void setMessage(MessageTO message, Context context, View view) {
            TextView messageView = (TextView) view.findViewById(R.id.message);
            if (message.message == null || "".equals(message.message)) {
                messageView.setVisibility(View.GONE);
            } else {
                messageView.setVisibility(View.VISIBLE);
                messageView.setText(message.message);
            }
        }

        private void setSenderAvatar(Context context, View view, Cursor c, final MessageTO message) {
            ImageView senderAvatar = (ImageView) view.findViewById(R.id.sender_avatar);
            ProgressBar spinner = (ProgressBar) view.findViewById(R.id.spinner); // todo ruben
            final boolean isChat = SystemUtils.isFlagEnabled(message.flags, MessagingPlugin.FLAG_DYNAMIC_CHAT);
            final SafeRunnable friendNotFoundRunnable;
            if (isChat) {
                friendNotFoundRunnable = new SafeRunnable() {
                    @Override
                    protected void safeRun() throws Exception {
                        mFriendsPlugin.requestUserInfo(message.sender, true);
                    }
                };
            } else {
                friendNotFoundRunnable = null;
            }
            senderAvatar
                .setImageBitmap(mFriendsPlugin.getAvatarBitmap(message.sender, !isChat, friendNotFoundRunnable));

            spinner.setVisibility(View.GONE); // todo ruben

            final boolean isSender = message.sender.equals(mMyEmail);
            if (isSender && !isChat && !SystemUtils.isFlagEnabled(message.flags, MessagingPlugin.FLAG_LOCKED)) {
                senderAvatar.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (!isSender)
                            return false;
                        final ProgressDialog dialog = ProgressDialog.show(FriendsThreadActivity.this, "",
                            getString(R.string.locking), true, false);
                        dialog.show();
                        mMessagingPlugin.lockMessage(message, new SafeRunnable() {
                            @Override
                            protected void safeRun() throws Exception {
                                mService.postOnUIHandler(new SafeRunnable() {
                                    @Override
                                    protected void safeRun() throws Exception {
                                        T.UI();
                                        dialog.dismiss();
                                    }
                                });
                            }
                        });
                        return true;
                    }
                });
                senderAvatar.setOnClickListener(null);
                senderAvatar.setTag(true);
            } else {
                senderAvatar.setOnLongClickListener(null);
                senderAvatar.setTag(false);
                configureAvatarOnClickListener(message.sender, senderAvatar, isChat);
            }
        }

        private String setMessageInfo(Context context, View view, boolean isSender, MessageTO message, boolean isChat) {
            if (message.priority == Message.PRIORITY_HIGH) {
                RelativeLayout textBubble = (RelativeLayout) view.findViewById(R.id.text_bubble);
                if (isSender) {
                    textBubble.setBackgroundResource(R.drawable.textballoon_right_blue);
                } else {
                    textBubble.setBackgroundResource(R.drawable.textballoon_left_blue);
                }
            } else if (message.priority == Message.PRIORITY_URGENT
                || message.priority == Message.PRIORITY_URGENT_WITH_ALARM) {
                RelativeLayout textBubble = (RelativeLayout) view.findViewById(R.id.text_bubble);
                if (isSender) {
                    textBubble.setBackgroundResource(R.drawable.textballoon_right_red);
                } else {
                    textBubble.setBackgroundResource(R.drawable.textballoon_left_red);
                }
            }

            // Set textual info ontop of message balloon
            TextView messageInfo = (TextView) view.findViewById(R.id.message_info);
            String senderName;
            if (isSender) {
                senderName = getString(R.string.__me_as_sender);
            } else {
                senderName = mFriendsPlugin.getName(message.sender);
                senderName = senderName.split(" ")[0];
            }
            String humanTime = TimeUtils.getHumanTime(mContext, message.timestamp * 1000, false);
            messageInfo.setText(String.format("%s, %s", senderName, humanTime));
            // Add locked icon if needed.
            ImageView locked = (ImageView) view.findViewById(R.id.status);
            boolean isLocked = (message.flags & MessagingPlugin.FLAG_LOCKED) == MessagingPlugin.FLAG_LOCKED;
            if (isChat) {
                if (message.priority == Message.PRIORITY_URGENT_WITH_ALARM) {
                    locked.setImageResource(R.drawable.status_ringing);
                    locked.setVisibility(View.VISIBLE);
                } else {
                    if (isLocked) {
                        locked.setImageResource(R.drawable.lock);
                        locked.setVisibility(View.VISIBLE);
                    } else {
                        locked.setVisibility(View.GONE);
                    }
                }
            } else {
                boolean isRinging = message.alert_flags >= AlertManager.ALERT_FLAG_RING_5
                    && !mMessagingPlugin.isMessageAckedByMe(message);
                if (isRinging || isLocked) {
                    if (isLocked) {
                        locked.setImageResource(R.drawable.lock);
                    } else {
                        locked.setImageResource(R.drawable.status_ringing);
                    }
                    locked.setVisibility(View.VISIBLE);
                } else {
                    locked.setVisibility(View.GONE);
                }
            }
            return senderName;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return null;
        }
    }

    private void scrollToBeAckedPosition(Cursor cursor) {
        ListView lv = getListView();
        int position = (int) mMessageStore.getToAckPosition(mParentMessageKey, mMyEmail);
        if (position == -1)
            lv.setSelection(cursor.getCount() - 1);
        else
            lv.setSelection(position);
    }

    private void setAvatarBackground(ImageView avatar, MemberStatusTO memberStatus) {
        if ((memberStatus.status & MessagingPlugin.STATUS_ACKED) == MessagingPlugin.STATUS_ACKED)
            avatar.setBackgroundResource(R.drawable.avatar_background_acknowledged);
        else if ((memberStatus.status & MessagingPlugin.STATUS_RECEIVED) == MessagingPlugin.STATUS_RECEIVED)
            avatar.setBackgroundResource(R.drawable.avatar_background_received);
        else
            avatar.setBackgroundResource(R.drawable.avatar_background_not_received);
    }

    private void configureAvatarOnClickListener(final String friendEmail, final ImageView avatar, final boolean isChat) {
        avatar.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                T.UI();
                if (mService.getIdentityStore().getIdentity().getEmail().equals(friendEmail)) {
                    Intent intent = new Intent(FriendsThreadActivity.this, ProfileActivity.class);
                    startActivity(intent);
                } else if (isChat) {
                    final int contactType = mFriendsPlugin.getContactType(friendEmail);
                    if ((contactType & FriendsPlugin.FRIEND) == FriendsPlugin.FRIEND && AppConstants.FRIENDS_ENABLED) {
                        mFriendsPlugin.launchDetailActivity(FriendsThreadActivity.this, friendEmail);
                    } else {
                        Intent intent = new Intent(FriendsThreadActivity.this, FriendDetailOrInviteActivity.class);
                        intent.putExtra(FriendDetailOrInviteActivity.EMAIL, friendEmail);
                        startActivity(intent);
                    }
                } else {
                    final int contactType = mFriendsPlugin.getContactType(friendEmail);
                    if ((contactType & FriendsPlugin.FRIEND) == FriendsPlugin.FRIEND) {
                        mFriendsPlugin.launchDetailActivity(FriendsThreadActivity.this, friendEmail);
                    } else {
                        if ((contactType & FriendsPlugin.NON_FRIEND) == FriendsPlugin.NON_FRIEND) {
                            new AlertDialog.Builder(FriendsThreadActivity.this)
                                .setMessage(getString(R.string.invite_as_friend, new Object[] { friendEmail }))
                                .setPositiveButton(R.string.yes, new SafeDialogInterfaceOnClickListener() {
                                    @Override
                                    public void safeOnClick(DialogInterface dialog, int which) {
                                        mFriendsPlugin.inviteFriend(friendEmail, null, null, true);
                                    }
                                }).setNegativeButton(R.string.no, null).create().show();
                        }
                    }
                }
            }
        });
    }

    private void displayMembers(MessageTO parentMessage) {
        final boolean isChat = SystemUtils.isFlagEnabled(mFlags, MessagingPlugin.FLAG_DYNAMIC_CHAT);
        if (isChat) {
            setTitle(R.string.group_chat);
            return;
        }
        List<String> members = new ArrayList<>();

        Collection<MemberStatusTO> leastMemberStatusses = mMessageStore.getLeastMemberStatusses(mParentMessageKey);
        MemberStatusTO senderStatus = null;
        for (final MemberStatusTO ms : leastMemberStatusses) {
            if (!ms.member.equals(mMyEmail)) {
                members.add(ms.member);
            }

            if (ms.member.equals(parentMessage.sender)) {
                senderStatus = ms;
            } else {
                ImageView avatar = new ImageView(this);
                avatar.setImageBitmap(mFriendsPlugin.getAvatarBitmap(ms.member, true));
                avatar.setPadding(_3_DP_IN_PX, _3_DP_IN_PX, _3_DP_IN_PX, _3_DP_IN_PX);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(_48_DP_IN_PX, _48_DP_IN_PX);
                layoutParams.setMargins(0, 0, _3_DP_IN_PX, 0);
                avatar.setLayoutParams(layoutParams);
                setAvatarBackground(avatar, ms);
                configureAvatarOnClickListener(ms.member, avatar, isChat);
            }
        }
        if (senderStatus == null) {
            L.bug("Sender status could not be determined!");
        }

        if (members.size() > 1) {
            setTitle(R.string.group_chat);
            final StringBuilder sb = new StringBuilder();
            boolean firstTime = true;
            for (String member : members) {
                if (firstTime) {
                    firstTime = false;
                } else {
                    sb.append(", ");
                }
                sb.append(mFriendsPlugin.getName(member));
            }

            getSupportActionBar().setSubtitle(sb.toString());
        } else {
            setTitle(mFriendsPlugin.getName(members.get(0)));
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        if (mGestureScanner != null) {
            mGestureScanner.onTouchEvent(e);
        }
        return super.dispatchTouchEvent(e);
    }

    @Override
    protected String[] getAllReceivingIntents() {
        return new String[] { MessagingPlugin.MESSAGE_MEMBER_STATUS_UPDATE_RECEIVED_INTENT,
            MessagingPlugin.MESSAGE_LOCKED_INTENT, MessagingPlugin.MESSAGE_PROCESSED_INTENT,
            MessagingPlugin.NEW_MESSAGE_RECEIVED_INTENT, FriendsPlugin.FRIEND_AVATAR_CHANGED_INTENT,
            FriendsPlugin.FRIEND_UPDATE_INTENT, IdentityStore.IDENTITY_CHANGED_INTENT,
            FriendsPlugin.FRIENDS_LIST_REFRESHED, MessagingPlugin.NEW_MESSAGE_QUEUED_TO_BACKLOG_INTENT };
    }

    private boolean iAmMember(final Message message) {
        boolean isMember = false;
        for (MemberStatusTO member : message.members) {
            if (mMyEmail.equals(member.member)) {
                isMember = true;
                break;
            }
        }
        return isMember;
    }

}
