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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import android.view.ViewGroup;

import com.mobicage.api.messaging.Rpc;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.HomeActivity;
import com.mobicage.rogerthat.MainActivity;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.QuickReplyActivity;
import com.mobicage.rogerthat.ServiceBound;
import com.mobicage.rogerthat.config.Configuration;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.plugins.MobicagePlugin;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.messaging.mfr.EmptyStaticFlowException;
import com.mobicage.rogerthat.plugins.messaging.mfr.JsMfr;
import com.mobicage.rogerthat.plugins.messaging.mfr.MessageFlowRun;
import com.mobicage.rogerthat.plugins.messaging.widgets.Widget;
import com.mobicage.rogerthat.util.IOUtils;
import com.mobicage.rogerthat.util.Security;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.ZipUtils;
import com.mobicage.rogerthat.util.db.DatabaseManager;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.ImageHelper;
import com.mobicage.rogerthat.util.ui.SendMessageView;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.CallReceiver;
import com.mobicage.rpc.IJSONable;
import com.mobicage.rpc.IncompleteMessageException;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.rpc.http.HttpCommunicator;
import com.mobicage.to.messaging.AckMessageRequestTO;
import com.mobicage.to.messaging.AckMessageResponseTO;
import com.mobicage.to.messaging.AttachmentTO;
import com.mobicage.to.messaging.ButtonTO;
import com.mobicage.to.messaging.DeleteConversationRequestTO;
import com.mobicage.to.messaging.DeleteConversationResponseTO;
import com.mobicage.to.messaging.GetConversationRequestTO;
import com.mobicage.to.messaging.LockMessageRequestTO;
import com.mobicage.to.messaging.MarkMessagesAsReadRequestTO;
import com.mobicage.to.messaging.MarkMessagesAsReadResponseTO;
import com.mobicage.to.messaging.MemberStatusTO;
import com.mobicage.to.messaging.MemberStatusUpdateRequestTO;
import com.mobicage.to.messaging.MessageLockedRequestTO;
import com.mobicage.to.messaging.MessageTO;
import com.mobicage.to.messaging.SendMessageRequestTO;
import com.mobicage.to.messaging.StartFlowRequestTO;
import com.mobicage.to.messaging.UpdateMessageRequestTO;
import com.mobicage.to.messaging.UploadChunkRequestTO;
import com.mobicage.to.messaging.forms.AdvancedOrderWidgetResultTO;
import com.mobicage.to.messaging.forms.FloatListWidgetResultTO;
import com.mobicage.to.messaging.forms.FloatWidgetResultTO;
import com.mobicage.to.messaging.forms.LocationWidgetResultTO;
import com.mobicage.to.messaging.forms.LongWidgetResultTO;
import com.mobicage.to.messaging.forms.MyDigiPassWidgetResultTO;
import com.mobicage.to.messaging.forms.SubmitPhotoUploadFormRequestTO;
import com.mobicage.to.messaging.forms.SubmitPhotoUploadFormResponseTO;
import com.mobicage.to.messaging.forms.UnicodeListWidgetResultTO;
import com.mobicage.to.messaging.forms.UnicodeWidgetResultTO;
import com.mobicage.to.messaging.jsmfr.FlowStartedRequestTO;
import com.mobicage.to.news.NewsActionButtonTO;
import com.mobicage.to.system.SettingsTO;

import org.jivesoftware.smack.util.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import me.leolin.shortcutbadger.ShortcutBadger;

public class MessagingPlugin implements MobicagePlugin {

    public final static String ANDROID_TEL_PREFIX = "tel:";
    public final static String ANDROID_HTTP_PREFIX = "http://";
    public final static String ANDROID_HTTPS_PREFIX = "https://";
    public final static String ANDROID_GEO_PREFIX = "geo:";
    public final static String ANDROID_MAILTO_PREFIX = "mailto:";

    private final static String CONFIGKEY = "com.mobicage.rogerthat.plugins.notification";

    public final static String NEW_MESSAGE_RECEIVED_INTENT = "com.mobicage.rogerthat.plugins.messaging.NEW_MESSAGE_RECEIVED";
    public final static String MESSAGE_MEMBER_STATUS_UPDATE_RECEIVED_INTENT = "com.mobicage.rogerthat.plugins.messaging.MESSAGE_MEMBER_STATUS_UPDATE_RECEIVED";
    public final static String MESSAGE_FAILURE = "com.mobicage.rogerthat.plugins.messaging.MESSAGE_FAILURE";
    public final static String MESSAGE_DIRTY_CLEANED_INTENT = "com.mobicage.rogerthat.plugins.messaging.MESSAGE_DIRTY_CLEANED";
    public final static String MESSAGE_THREAD_VISIBILITY_CHANGED_INTENT = "com.mobicage.rogerthat.plugins.messaging.MESSAGE_THREAD_VISIBILITY_CHANGED_INTENT";
    public static final String MESSAGE_LOCKED_INTENT = "com.mobicage.rogerthat.plugins.messaging.MESSAGE_LOCKED";
    public static final String MESSAGE_PROCESSED_INTENT = "com.mobicage.rogerthat.plugins.messaging.MESSAGE_PROCESSED_INTENT";
    public static final String MESSAGE_FLOW_ENDED_INTENT = "com.mobicage.rogerthat.plugins.messaging.MESSAGE_FLOW_ENDED_INTENT";
    public static final String THREAD_DELETED_INTENT = "com.mobicage.rogerthat.plugins.messaging.THREAD_DELETED_INTENT";
    public static final String THREAD_RECOVERED_INTENT = "com.mobicage.rogerthat.plugins.messaging.THREAD_RECOVERED_INTENT";
    public final static String THREAD_MODIFIED_INTENT = "com.mobicage.rogerthat.plugins.messaging.THREAD_MODIFIED_INTENT";
    public static final String MESSAGE_JSMFR_ERROR = "com.mobicage.rogerthat.plugins.messaging.JSMFR_ERROR";
    public static final String MESSAGE_SUBMIT_PHOTO_UPLOAD = "com.mobicage.api.messaging.submitPhotoUploadForm";

    public final static long FLAG_ALLOW_DISMISS = 1;
    public final static long FLAG_ALLOW_CUSTOM_REPLY = 2;
    public final static long FLAG_ALLOW_REPLY = 4;
    public final static long FLAG_ALLOW_REPLY_ALL = 8;
    public final static long FLAG_SHARED_MEMBERS = 16;
    public final static long FLAG_LOCKED = 32;
    public final static long FLAG_AUTO_LOCK = 64;
    public final static long FLAG_SENT_BY_JSMFR = 256;
    public final static long FLAG_DYNAMIC_CHAT = 512;
    public final static long FLAG_NOT_REMOVABLE = 1024;
    public final static long FLAG_ALLOW_CHAT_BUTTONS = 2048;
    public final static long FLAG_CHAT_STICKY = 4096;
    public final static long FLAG_ALLOW_CHAT_PICTURE = 8192;
    public final static long FLAG_ALLOW_CHAT_VIDEO = 16384;
    public final static long FLAG_ALLOW_CHAT_PRIORITY = 32768;
    public final static long FLAG_ALLOW_CHAT_STICKY = 65536;

    public final static long STATUS_RECEIVED = 1;
    public final static long STATUS_ACKED = 2;
    public final static long STATUS_READ = 4;
    public final static long STATUS_DELETED = 8;

    public final static long DIRTY_BEHAVIOR_NORMAL = 1;
    public final static long DIRTY_BEHAVIOR_MAKE_DIRTY = 2;
    public final static long DIRTY_BEHAVIOR_CLEAR_DIRTY = 3;

    public final static long UI_FLAG_EXPECT_NEXT_WAIT_5 = 1;
    public final static long UI_FLAG_AUTHORIZE_LOCATION = 2;

    public static final int EXISTENCE_NOT_FOUND = -1;
    public static final int EXISTENCE_DELETED = 0;
    public static final int EXISTENCE_ACTIVE = 1;

    private final static long FLAG_UPDATE_NOTIFICATIONS = 1;
    private final static long FLAG_UPDATE_NOTIFICATIONS_NEW_INCOMMING = 2;
    private final static long FLAG_UPDATE_NOTIFICATIONS_UPDATES_FOR_ME = 4;

    private final static String CONFIG_SOUND_ENABLED_KEY = "soundEnabled";
    private final static boolean CONFIG_SOUND_ENABLED_DEFAULTVALUE = true;

    private final static String CONFIG_VIBRATION_ENABLED_KEY = "vibrationEnabled";
    private final static boolean CONFIG_VIBRATION_ENABLED_DEFAULTVALUE = true;

    public static final String NEW_MESSAGE_QUEUED_TO_BACKLOG_INTENT = "com.mobicage.rogerthat.plugins.messaging.NEW_MESSAGE_QUEUED_TO_BACKLOG_INTENT";

    public static final String MEMBER_FILTER = "member_filter";

    private static final int MAX_CHUNK_SIZE = 90 * 1024;

    private final ConfigurationProvider mConfigProvider;
    private final MainService mMainService;
    private final MessageStore mStore;
    private final BrandingMgr mBrandingMgr;
    private final MessageHistory mMessageHistory;
    private final AlertManager mAlertMgr;
    private MessagingCallReceiver mMessagingCallReceiver;

    private final SafeBroadcastReceiver mBroadcastReceiver;
    private List<String> mTransferQueue = null;
    private static final String TRANSFER_UPLOAD_CONFIGKEY = "TRANSFER_UPLOAD";
    private static final String TRANSFER_PHOTO_UPLOAD_CONFIGKEY = "TRANSFER_PHOTO_UPLOAD";

    private static final String TRANSFER_JSMFR_COMPLETED_CONFIGKEY = "TRANSFER_JSMFR_COMPLETED";
    private static final String TRANSFER_JSMFR_PHOTO_COMPLETED_CONFIGKEY = "TRANSFER_PHOTO_JSMFR_COMPLETED";

    private static final String TRANSFER_PHOTO_UPLOAD_SEND_MESSAGE_CONFIGKEY = "TRANSFER_PHOTO_UPLOAD_SEND_MESSAGE";

    // All members owned by UI thread
    @SuppressWarnings("unused")
    private boolean mWithSound = false;
    @SuppressWarnings("unused")
    private boolean mWithVibration = false;
    private int mMessageOffset;
    private final String mMyEmail;

    private volatile long mUpdateNotificationFlags = 0;

    public MessageTO storeMessage(final String me, final SendMessageRequestTO request, String selectedButtonId) {
        T.BIZZ();
        final MessageTO message = new MessageTO();
        message.key = request.key;
        message.sender = me;
        message.flags = request.flags;
        message.timeout = request.timeout;
        long currentTimeMillis = mMainService.currentTimeMillis();
        message.timestamp = currentTimeMillis / 1000;
        String parent_key = request.parent_key;
        message.parent_key = parent_key;
        message.message = request.message;
        message.buttons = request.buttons;
        String[] members;
        if (parent_key == null)
            members = request.members;
        else {
            MessageStore store = getStore();
            Set<String> memberList = store.getMessageMembers(parent_key);
            members = memberList.toArray(new String[memberList.size()]);
        }
        message.members = new MemberStatusTO[members.length];
        for (int i = 0; i < members.length; i++) {
            String member = members[i];
            MemberStatusTO ms = new MemberStatusTO();
            if (me.equals(member)) {
                ms.status = STATUS_ACKED | STATUS_RECEIVED;
                ms.received_timestamp = currentTimeMillis / 1000;
                ms.acked_timestamp = currentTimeMillis / 1000;
                ms.button_id = selectedButtonId;
            } else {
                ms.status = 0;
                ms.received_timestamp = 0;
                ms.acked_timestamp = 0;
                ms.button_id = null;
            }
            ms.custom_reply = null;
            ms.member = member;
            message.members[i] = ms;
        }
        message.branding = null;
        message.timestamp = currentTimeMillis / 1000;
        message.priority = request.priority;

        message.default_priority = Message.PRIORITY_NORMAL;
        message.default_sticky = false;

        newMessage(message, true, true);
        return message;
    }

    private interface IFormResultProcessor {
        public void processResult(final Message message);
    }

    public MessagingPlugin(final ConfigurationProvider pConfigProvider, final MainService pMainService,
        final DatabaseManager pDatabaseManager, final BrandingMgr brandingMgr) {
        T.UI();
        mConfigProvider = pConfigProvider;
        mMainService = pMainService;
        mStore = new MessageStore(pDatabaseManager, pMainService);
        mBrandingMgr = brandingMgr;
        mMessageHistory = new MessageHistory(mMainService, mStore);
        mAlertMgr = new AlertManager(pMainService, mStore);
        mMyEmail = mMainService.getIdentityStore().getIdentity().getEmail();

        mMainService.addHighPriorityIntent(MessagingPlugin.MESSAGE_PROCESSED_INTENT);
        mMainService.addHighPriorityIntent(MessagingPlugin.MESSAGE_LOCKED_INTENT);
        mMainService.addHighPriorityIntent(MessagingPlugin.NEW_MESSAGE_QUEUED_TO_BACKLOG_INTENT);
        mMainService.addHighPriorityIntent(MessagingPlugin.NEW_MESSAGE_RECEIVED_INTENT);
        mMainService.addHighPriorityIntent(MessagingPlugin.THREAD_DELETED_INTENT);
        mMainService.addHighPriorityIntent(MessagingPlugin.MESSAGE_JSMFR_ERROR);
        mMainService.addHighPriorityIntent(MessagingPlugin.MESSAGE_SUBMIT_PHOTO_UPLOAD);
        mMainService.addHighPriorityIntent(BrandingMgr.ATTACHMENT_AVAILABLE_INTENT);

        mMainService.postOnBIZZHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                transferQueueLoad();
            }
        });

        mBroadcastReceiver = new SafeBroadcastReceiver() {
            @Override
            public String[] onSafeReceive(Context context, Intent intent) {
                T.UI();

                final String action = intent.getAction();
                if (HttpCommunicator.INTENT_HTTP_START_OUTGOING_CALLS.equals(action)) {
                    boolean filterOnWifiOnly = !intent.getBooleanExtra(
                        HttpCommunicator.INTENT_HTTP_START_OUTGOING_CALLS_WIFI, false);
                    if (mTransferQueue.size() > 0) {
                        if (filterOnWifiOnly) {
                            showTransferPendingNotification(mMainService
                                .getString(R.string.transfer_uploading_notification));
                        } else {
                            SharedPreferences options = PreferenceManager.getDefaultSharedPreferences(mMainService);
                            boolean wifiOnlySettingEnabled = options.getBoolean(
                                MainService.PREFERENCE_UPLOAD_PHOTO_WIFI, false);
                            if (!mMainService.getNetworkConnectivityManager().isConnected()) {
                                showTransferPendingNotification(mMainService
                                    .getString(R.string.transfer_pending_notification_no_network));
                            } else if (wifiOnlySettingEnabled
                                && !mMainService.getNetworkConnectivityManager().isWifiConnected()) {
                                showTransferPendingNotification(mMainService
                                    .getString(R.string.transfer_pending_notification_no_wifi));
                            }
                        }

                    }

                } else {
                    L.d("Error - received unexpected intent in MessagingPlugin: action=" + action);
                }
                return null;
            }
        };

        final IntentFilter filter = new IntentFilter();
        filter.addAction(HttpCommunicator.INTENT_HTTP_START_OUTGOING_CALLS);
        mMainService.registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    public void initialize() {
        T.UI();
        reconfigure();

        mMessagingCallReceiver = new MessagingCallReceiver(mMainService, this);
        CallReceiver.comMobicageCapiMessagingIClientRpc = mMessagingCallReceiver;
        mConfigProvider.registerListener(CONFIGKEY, this);
    }

    @Override
    public void destroy() {
        T.UI();

        mConfigProvider.unregisterListener(CONFIGKEY, this);
        mMainService.unregisterReceiver(mBroadcastReceiver);
        try {
            mStore.close();
        } catch (IOException e) {
            L.bug(e);
        }
        mBrandingMgr.close();
        CallReceiver.comMobicageCapiMessagingIClientRpc = null;
        try {
            mAlertMgr.close();
        } catch (IOException e) {
            L.bug(e);
        }
    }

    @Override
    public void reconfigure() {
        T.UI();
        Configuration cfg = mConfigProvider.getConfiguration(CONFIGKEY);
        mWithSound = cfg.get(CONFIG_SOUND_ENABLED_KEY, CONFIG_SOUND_ENABLED_DEFAULTVALUE);
        mWithVibration = cfg.get(CONFIG_VIBRATION_ENABLED_KEY, CONFIG_VIBRATION_ENABLED_DEFAULTVALUE);
    }

    @Override
    public void processSettings(SettingsTO settings) {
        // not used
    }

    public void inboxOpened() {
        T.UI();
        mStore.setInboxOpenedAt(mMainService.currentTimeMillis() / 1000);
    }

    /**
     * I sent this msg or a human user sent this msg or a human started the thread, let's show the person thread view
     */
    private boolean isHumanThread(Message message, int friendType, FriendsPlugin friendsPlugin) {

        if (message.sender.equals(mMyEmail))
            return true;

        if (friendType == FriendsPlugin.FRIEND_TYPE_USER)
            return true;

        if (friendType == FriendsPlugin.FRIEND_TYPE_UNKNOWN && message.parent_key != null) {
            Message parent = mStore.getPartialMessageByKey(message.parent_key);
            return isHumanThread(parent, friendsPlugin.getStore().getFriendType(parent.sender), friendsPlugin);
        }

        return false;
    }

    public void showMessage(Context context, Message message, String memberFilter) {
        showMessage(context, message, false, memberFilter);
        String parentKey = message.parent_key != null ? message.parent_key : message.key;
        UIUtils.cancelNotification(context, parentKey);
    }

    public void showMessage(Context context, Message message, boolean detail, String memberFilter) {
        showMessage(context, message, detail, memberFilter, true);
    }

    public void showMessage(Context context, Message message, boolean detail, String memberFilter, boolean
            jumpToServiceHomeScreenWhenFinished) {
        FriendsPlugin friendsPlugin = mMainService.getPlugin(FriendsPlugin.class);
        int friendType = friendsPlugin.getStore().getFriendType(message.sender);
        String threadKey = message.parent_key != null ? message.parent_key : message.key;
        if (SystemUtils.isFlagEnabled(message.flags, FLAG_DYNAMIC_CHAT)
            || isHumanThread(message, friendType, friendsPlugin)) {

            Intent intent = FriendsThreadActivity.createIntent(context, threadKey, message.flags, memberFilter);
            context.startActivity(intent);

        } else if (FriendsPlugin.SYSTEM_FRIEND.equals(message.sender)
            || (friendType == FriendsPlugin.FRIEND_TYPE_SERVICE)
            || (friendType == FriendsPlugin.FRIEND_TYPE_UNKNOWN)) {
            // System sent this message, or Service sent this message, or
            // non-friend sent this message
            // Let's show the service thread view
            // For the case of non-friend we actually do not know whether it
            // was a svc or a user
            // Showing the service thread view is the safest
            if (detail || message.dirty || message.needsMyAnswer || message.replyCount == 1) {
                final Intent intent = new Intent(context, ServiceMessageDetailActivity.class);
                intent.putExtra("message", message.key);
                intent.putExtra(ServiceMessageDetailActivity.JUMP_TO_SERVICE_HOME_SCREEN,
                        jumpToServiceHomeScreenWhenFinished);
                intent.putExtra(MEMBER_FILTER, memberFilter);
                intent.putExtra(ServiceMessageDetailActivity.TITLE, friendsPlugin.getStore().getName(message.sender));
                context.startActivity(intent);

            } else {
                Intent intent = ServiceThreadActivity.createIntent(context, threadKey, memberFilter,
                    message.parent_key != null);
                context.startActivity(intent);
            }
        } else {
            L.bug("showMessage - unexpected friendType " + friendType + " for email " + message.sender);
        }
    }

    public void removeConversationFromList(final Context context, final String threadKey) {
        new AlertDialog.Builder(context).setTitle(R.string.message_delete_confirm)
            .setMessage(R.string.message_delete_question)
            .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    T.UI();
                    if (!deleteConversation(threadKey)) {
                        UIUtils.showLongToast(context, (context.getString(R.string.error_please_try_again)));
                        L.d("removeMessage failed");
                    } else {
                        L.d("removeMessage succeeded");
                    }
                    dialog.dismiss();
                }
            }).setNegativeButton(R.string.cancel, null).create().show();
    }

    public Intent getNextMessageThreadActivityIntent(Context context, String messageKey, String memberFilter) {
        T.UI();
        Cursor cursor = mStore.getThreadListCursor(memberFilter);
        try {
            if (cursor.moveToFirst()) {
                do {
                    String key = cursor.getString(0); // parent_key
                    if (key == null)
                        key = cursor.getString(1); // message key

                    if (key.equals(messageKey)) {
                        while (cursor.moveToNext()) {
                            String nextKey = cursor.getString(0); // parent_key
                            if (nextKey == null)
                                nextKey = cursor.getString(1); // message key

                            String nextSender = cursor.getString(2);
                            long nextFlags = cursor.getLong(3);
                            Intent intent = getThreadActivityIntent(context, nextKey, nextSender, nextFlags,
                                memberFilter);
                            if (intent != null)
                                return intent;
                        }
                        return null;
                    }
                } while (cursor.moveToNext());
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    public Intent getPreviousMessageThreadActivityIntent(Context context, String messageKey, String memberFilter) {
        T.UI();
        Cursor cursor = mStore.getThreadListCursor(memberFilter);
        try {
            if (cursor.moveToFirst()) {
                do {
                    String key = cursor.getString(0); // parent_key
                    if (key == null)
                        key = cursor.getString(1); // message key

                    if (key.equals(messageKey)) {
                        while (cursor.moveToPrevious()) {
                            String prevKey = cursor.getString(0); // parent_key
                            if (prevKey == null)
                                prevKey = cursor.getString(1); // message key

                            String prevSender = cursor.getString(2);
                            long prevFlags = cursor.getLong(3);
                            Intent intent = getThreadActivityIntent(context, prevKey, prevSender, prevFlags,
                                memberFilter);
                            if (intent != null)
                                return intent;
                        }
                        return null;
                    }
                } while (cursor.moveToNext());
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    private Intent getThreadActivityIntent(Context context, String nextKey, String nextSender, long nextFlags,
        String memberFilter) {
        if (SystemUtils.isFlagEnabled(nextFlags, FLAG_DYNAMIC_CHAT)) {
            return FriendsThreadActivity.createIntent(context, nextKey, nextFlags, memberFilter);
        }
        if (mMainService.getIdentityStore().getIdentity().getEmail().equals(nextSender)) {
            return FriendsThreadActivity.createIntent(context, nextKey, nextFlags, memberFilter);
        }
        if (FriendsPlugin.SYSTEM_FRIEND.equals(nextSender)) {
            return ServiceThreadActivity.createIntent(context, nextKey, memberFilter);
        }
        FriendsPlugin friendsPlugin = mMainService.getPlugin(FriendsPlugin.class);
        Friend friend = friendsPlugin.getStore().getExistingFriend(nextSender);
        if (friend == null) {
            return ServiceThreadActivity.createIntent(context, nextKey, memberFilter);
        }
        switch ((int) friend.type) {
        case FriendsPlugin.FRIEND_TYPE_USER:
            return FriendsThreadActivity.createIntent(context, nextKey, nextFlags, memberFilter);
        case FriendsPlugin.FRIEND_TYPE_SERVICE:
            return ServiceThreadActivity.createIntent(context, nextKey, memberFilter);
        }
        return null;
    }

    public Map<String, String> getButtonActionInfo(ButtonTO button) {
        return getButtonActionInfo(button.action);
    }

    public Map<String, String> getButtonActionInfo(NewsActionButtonTO button) {
        return getButtonActionInfo(button.action);
    }

    private Map<String, String> getButtonActionInfo(String action) {
        String buttonAction = null;
        String buttonUrl = null;

        if (action != null) {
            if (action.startsWith(Message.MC_TEL_PREFIX)) {
                buttonAction = Intent.ACTION_DIAL;
                buttonUrl = ANDROID_TEL_PREFIX + action.substring(Message.MC_TEL_PREFIX.length());

            } else if (action.startsWith(Message.MC_HTTP_PREFIX)) {
                buttonAction = Intent.ACTION_VIEW;
                buttonUrl = ANDROID_HTTP_PREFIX + action.substring(Message.MC_HTTP_PREFIX.length());

            } else if (action.startsWith(Message.MC_GEO_PREFIX)) {
                buttonAction = Intent.ACTION_VIEW;
                buttonUrl = ANDROID_GEO_PREFIX + action.substring(Message.MC_GEO_PREFIX.length());

            } else if (action.startsWith(Message.MC_HTTPS_PREFIX)) {
                buttonAction = Intent.ACTION_VIEW;
                buttonUrl = ANDROID_HTTPS_PREFIX + action.substring(Message.MC_HTTPS_PREFIX.length());

            } else if (action.startsWith(Message.MC_MAILTO_PREFIX)) {
                buttonAction = Intent.ACTION_VIEW;
                buttonUrl = ANDROID_MAILTO_PREFIX + action.substring(Message.MC_MAILTO_PREFIX.length());

            } else {
                for (final String prefix : new String[]{Message.MC_CONFIRM_PREFIX, Message.MC_SMI_PREFIX, Message
                        .MC_POKE_PREFIX}) {
                    if (action.startsWith(prefix)) {
                        buttonAction = prefix;
                        buttonUrl = action.substring(prefix.length());
                        break;
                    }
                }
            }
        }

        Map<String, String> info = new HashMap<String, String>();
        info.put("androidAction", buttonAction);
        info.put("androidUrl", buttonUrl);

        return info;
    }

    public void setMessageOffset(int pMessageOffset) {
        T.UI();
        mMessageOffset = pMessageOffset;
    }

    public int getMessageOffset() {
        T.UI();
        return mMessageOffset;
    }

    public MessageStore getStore() {
        T.dontCare();
        return mStore;
    }

    public BrandingMgr getBrandingMgr() {
        T.dontCare();
        return mBrandingMgr;
    }

    public void newMessage(MessageTO message, boolean brandingOk, boolean sendUpdateIntentImmediatly) {
        T.BIZZ();
        if (message.parent_key != null && !brandingOk && !mBrandingMgr.isMessageInBrandingQueue(message.parent_key)) {
            int existence = mStore.getExistence(message.parent_key);
            if (existence == EXISTENCE_NOT_FOUND) {
                boolean messageWasNotYetRequested = requestConversation(message.parent_key);
                if (messageWasNotYetRequested) {
                    // already start downloading branding
                    if (!TextUtils.isEmptyOrWhitespace(message.branding)) {
                        try {
                            if (!mBrandingMgr.isBrandingAvailable(message.branding)) {
                                mBrandingMgr.queueGenericBranding(message.branding);
                            }
                        } catch (Exception e) {
                            L.e(e);
                        }
                    }
                }
                return;

            } else if (existence == EXISTENCE_DELETED) {
                mStore.restoreConversation(message.parent_key);
                Intent intent = new Intent(THREAD_RECOVERED_INTENT);
                intent.putExtra("key", message.parent_key);
                mMainService.sendBroadcast(intent);
            }
        }

        if (message.attachments != null
            && message.attachments.length > 0
            && mMainService.getPlugin(FriendsPlugin.class).getStore().getFriendType(message.sender) != FriendsPlugin.FRIEND_TYPE_SERVICE) {

            for (AttachmentTO attachment : message.attachments) {
                AttachmentDownload ad;
                try {
                    ad = new AttachmentDownload(attachment, message.parent_key == null ? message.key
                        : message.parent_key, message.key);
                } catch (IncompleteMessageException e) {
                    L.bug("Should never happen", e);
                    continue;
                }
                mBrandingMgr.queue(ad);
            }
        }

        if (!brandingOk) {
            if (!TextUtils.isEmptyOrWhitespace(message.branding)
                || (message.parent_key != null && mBrandingMgr.isMessageInBrandingQueue(message.parent_key))) {
                mBrandingMgr.queue(message);
                return;
            }
        }

        boolean senderIsMobileOwner = mStore.storeNewMessage(message);

        // Clean up if this is a conversation which was requested
        if (message.parent_key == null) {
            mStore.deleteRequestedConversation(message.key);
        }

        mMessageHistory.putMessageInHistory(message);

        Intent broadcast = new Intent(MessagingPlugin.NEW_MESSAGE_RECEIVED_INTENT);
        broadcast.putExtra("message", message.key);
        broadcast.putExtra("parent", message.parent_key);
        broadcast.putExtra("context", message.context);
        broadcast.putExtra("flags", message.flags);
        mMainService.sendBroadcast(broadcast, sendUpdateIntentImmediatly, true);
        updateBadge();

        String parentKey = message.parent_key != null ? message.parent_key : message.key;
        if (senderIsMobileOwner) {
            removeNotificationForThread(parentKey);
        } else {
            int unreadInThreadCount = mStore.getUnreadMessageCountInThread(parentKey);
            ArrayList<UnreadMessage> unreadMessages = mStore.getFirstUnreadMessagesInThread(parentKey);
            updateNotificationForThread(parentKey, message.key, unreadMessages, unreadInThreadCount);
        }
    }

    public static long getNewMessageFlags(long parentMessageFlags) {
        long flags = MessagingPlugin.FLAG_ALLOW_DISMISS
                | MessagingPlugin.FLAG_ALLOW_CUSTOM_REPLY
                | MessagingPlugin.FLAG_ALLOW_REPLY
                | MessagingPlugin.FLAG_ALLOW_REPLY_ALL
                | MessagingPlugin.FLAG_SHARED_MEMBERS;
        flags |= (parentMessageFlags & MessagingPlugin.FLAG_DYNAMIC_CHAT)
                | (parentMessageFlags & MessagingPlugin.FLAG_NOT_REMOVABLE)
                | (parentMessageFlags & MessagingPlugin.FLAG_ALLOW_CHAT_BUTTONS)
                | (parentMessageFlags & MessagingPlugin.FLAG_ALLOW_CHAT_PICTURE)
                | (parentMessageFlags & MessagingPlugin.FLAG_ALLOW_CHAT_VIDEO)
                | (parentMessageFlags & MessagingPlugin.FLAG_ALLOW_CHAT_PRIORITY)
                | (parentMessageFlags & MessagingPlugin.FLAG_ALLOW_CHAT_STICKY);
        return flags;
    }

    /**
     * Updates a notification for a thread.
     * <p/>
     * thread between 2 people only:
     * title -> friend name
     * message -> message
     * long message -> message
     * icon -> friend avatar
     * <p/>
     * Multiple people:
     * title -> All the people who said something
     * message -> x new messages
     * long message ->
     * - only one friend: message1\n message2\n...
     * - multiple friends friend1: message1 \n friend2: message2
     * icon -> thread avatar
     */
    private void updateNotificationForThread(String threadKey, String messageKey, ArrayList<UnreadMessage> unreadMessages,
                                             int unreadInThreadCount) {
        T.BIZZ();
        FriendsPlugin friendsPlugin = mMainService.getPlugin(FriendsPlugin.class);
        Bitmap largeIcon;
        // Don't create notification when the currently opened message thread is the same as the thread from the notification
        Activity currentActivity = UIUtils.getTopActivity(mMainService);
        if (currentActivity instanceof FriendsThreadActivity) {
            FriendsThreadActivity friendsThreadActivity = (FriendsThreadActivity) currentActivity;
            if (threadKey.equals(friendsThreadActivity.getParentMessageKey())) {
                return;
            }
        }
        Message parentMessage = mMainService.getPlugin(MessagingPlugin.class).getStore().getFullMessageByKey(threadKey);
        Message message;
        if (threadKey.equals(messageKey)) {
            message = parentMessage;
        } else {
            message = mMainService.getPlugin(MessagingPlugin.class).getStore().getMessageByKey(messageKey);
        }
        if (SystemUtils.isFlagEnabled(message.alert_flags, AlertManager.ALERT_FLAG_SILENT)) {
            return;
        }

        boolean canAnswerToMessage = SystemUtils.isFlagEnabled(parentMessage.flags, MessagingPlugin.FLAG_ALLOW_REPLY);
        if (canAnswerToMessage && !SystemUtils.isFlagEnabled(parentMessage.flags, FLAG_ALLOW_REPLY)) {
            canAnswerToMessage = false;
        }

        int priority = Notification.PRIORITY_HIGH;  // Causes the notification to show up as a heads-up notification
        String notificationText;
        StringBuilder longNotificationText = new StringBuilder();

        if (parentMessage.dirty && !SystemUtils.isFlagEnabled(parentMessage.flags, FLAG_DYNAMIC_CHAT)) {
            unreadInThreadCount++;
            UnreadMessage unreadMessage = new UnreadMessage(parentMessage.key, parentMessage.message,
                    friendsPlugin.getName(parentMessage.sender),
                    parentMessage.sender);
            unreadMessages.add(0, unreadMessage);
        }

        if (unreadInThreadCount == 0 || unreadMessages.size() == 0) {
            return;
        }

        List<String> friendNames = new ArrayList<>();
        for (UnreadMessage msg : unreadMessages) {
            if (msg.friendEmail != null && !msg.friendEmail.equals(mMyEmail)) {
                String displayName = msg.friendName != null ? msg.friendName : msg.friendEmail;
                if (!friendNames.contains(displayName)) {
                    friendNames.add(displayName);
                }
            }
        }
        String notificationTitle = android.text.TextUtils.join(", ", friendNames);
        boolean isFromOneFriend = friendNames.size() == 1;
        String lastSender = null;
        if (unreadInThreadCount > 1) {
            notificationText = mMainService.getString(R.string.message_notification_n_new_messages, unreadInThreadCount);
            for (UnreadMessage msg : unreadMessages) {
                if (msg.friendEmail != null) {
                    lastSender = msg.friendEmail;
                }
                if (!isFromOneFriend) {
                    longNotificationText.append(msg.friendName);
                    longNotificationText.append(": ");
                }
                String text = msg.message;
                if (TextUtils.isEmptyOrWhitespace(text)) {
                    Message fullMessage = mMainService.getPlugin(MessagingPlugin.class).getStore().getFullMessageByKey(msg.key);
                    text = getMessageTextFromButtonsOrAttachments(fullMessage);
                }
                longNotificationText.append(text);
                longNotificationText.append("\n");
            }
        } else {
            UnreadMessage lastMessage = unreadMessages.get(0);
            notificationText = lastMessage.message;
            longNotificationText.append(notificationText);
            if (TextUtils.isEmptyOrWhitespace(notificationText)) {
                Message fullMessage = mMainService.getPlugin(MessagingPlugin.class).getStore().getFullMessageByKey(lastMessage.key);
                notificationText = getMessageTextFromButtonsOrAttachments(fullMessage);
            }
            lastSender = lastMessage.friendEmail;
        }

        List<NotificationCompat.Action> actionButtons = new ArrayList<>();

        if (canAnswerToMessage) {
            // add 'reply' button
            Intent intent = new Intent(mMainService, QuickReplyActivity.class);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.putExtra(QuickReplyActivity.TITLE, notificationTitle);
            intent.putExtra(QuickReplyActivity.MESSAGE, unreadInThreadCount > 1 ? longNotificationText.toString() : notificationText);
            intent.putExtra(QuickReplyActivity.SENDER, lastSender);
            intent.putExtra(QuickReplyActivity.MESSAGE_KEY, messageKey);
            PendingIntent pendingIntent = PendingIntent.getActivity(mMainService, (int) System.currentTimeMillis(), intent, 0);
            int replyIcon = R.drawable.fa_mail_reply;
            String replyString = mMainService.getString(R.string.reply);
            NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(replyIcon, replyString, pendingIntent)
                    .build();
            actionButtons.add(replyAction);
        }

        int notificationId = UIUtils.getNotificationId(threadKey, true);

        if (isFromOneFriend) {
            largeIcon = friendsPlugin.getAvatarBitmap(lastSender, false, getNotificationIconSize());
        } else {
            BitmapDrawable groupAvatar = (BitmapDrawable) mMainService.getResources().getDrawable(R.drawable.group_60);
            largeIcon = groupAvatar.getBitmap();
        }
        Bundle extras = new Bundle();
        extras.putString(HomeActivity.INTENT_KEY_LAUNCHINFO, HomeActivity.INTENT_VALUE_SHOW_NEW_MESSAGES);
        extras.putString(HomeActivity.INTENT_KEY_MESSAGE, threadKey);
        UIUtils.doNotification(mMainService, notificationTitle, notificationText, notificationId,
                MainActivity.ACTION_NOTIFICATION_MESSAGE_RECEIVED, true, true, true, true,
                R.drawable.notification_icon, unreadInThreadCount, extras, null, mMainService.currentTimeMillis(),
                priority, actionButtons, longNotificationText.toString(), largeIcon,
                NotificationCompat.CATEGORY_MESSAGE);
        updateUnreadCountBadge();
    }

    private int getNotificationIconSize() {
        float density = mMainService.getResources().getDisplayMetrics().density;
        if (density == 0.75f) {
            // LDPI
            return 48 - 9;
        } else if (density >= 1.0f && density < 1.5f) {
            // MDPI
            return 64 - 12;
        } else if (density == 1.5f) {
            // HDPI
            return 96 - 18;
        } else if (density > 1.5f && density <= 2.0f) {
            // XHDPI
            return 128 - 24;
        } else if (density > 2.0f && density <= 3.0f) {
            // XXHDPI
            return 192 - 36;
        } else {
            // XXXHDPI
            return 256 - 50;
        }
    }

    /**
     * Updates the badge shown on the app icon to reflect the total amount of messages that the user didn't see(not read) yet.
     */
    private void updateUnreadCountBadge() {
        final int lastInboxOpenedTimestamp = (int) mStore.getLastInboxOpenedTimestamp();
        final int totalUnreadCount = mStore.getTotalUnreadCount(lastInboxOpenedTimestamp);
        ShortcutBadger.applyCount(mMainService, totalUnreadCount);
    }

    public void updateMessage(UpdateMessageRequestTO request) throws MessageUpdateNotAllowedException {
        if (TextUtils.isEmptyOrWhitespace(request.message_key)
            && TextUtils.isEmptyOrWhitespace(request.parent_message_key)) {
            // Should never happen
            return;
        }

        final Long flags = request.has_flags ? Long.valueOf(request.flags) : null;
        final Long existence = request.has_existence ? Long.valueOf(request.existence) : null;

        if (flags == null && existence == null && request.message == null && request.thread_avatar_hash == null
            && request.thread_background_color == null && request.thread_text_color == null) {
            // Can only happen when updateMessage is extended with extra properties
            return;
        }

        boolean messageFound = mStore.updateMessage(request.message_key, request.parent_message_key, flags, existence,
            request.message, request.thread_avatar_hash, request.thread_background_color, request.thread_text_color);

        final String threadKey = TextUtils.isEmptyOrWhitespace(request.parent_message_key) ? request.message_key
            : request.parent_message_key;

        if (messageFound) {
            final Intent intent;
            if (request.message_key == null) {
                intent = new Intent(THREAD_MODIFIED_INTENT);
                intent.putExtra("thread_key", threadKey);
            } else {
                intent = new Intent(MESSAGE_PROCESSED_INTENT);
                intent.putExtra("message", request.message_key);
                updateBadge();
            }
            mMainService.sendBroadcast(intent);
        }

        // don't request a conversation when it's existence is changed to DELETED
        if (existence == null || existence == EXISTENCE_ACTIVE) {
            String offset = null;
            if (messageFound && !TextUtils.isEmptyOrWhitespace(request.last_child_message)) {
                // Check if the last child message on the server is equal to the last child message on the client.
                // If not, we should request the messages we don't have.

                List<String> children = mStore.listChildMessagesInThread(threadKey);

                if (children.size() == 0) {
                    offset = threadKey;
                } else {
                    String lastChildMessageKey = children.get(children.size() - 1);
                    if (!request.last_child_message.equals(lastChildMessageKey)) {
                        offset = lastChildMessageKey;
                    }
                }
            }

            if (!messageFound || offset != null) {
                requestConversation(threadKey, offset);
            }
        }
    }

    public void updateMemberStatus(final MemberStatusUpdateRequestTO request) {
        T.BIZZ();
        String threadKey = request.parent_message == null ? request.message : request.parent_message;
        if (mStore.getExistence(threadKey) == EXISTENCE_NOT_FOUND) {
            requestConversation(threadKey);
            return;
        }

        final String myEmail = myEmail();
        final boolean updateSenderIsMobileOwner = request.member.equals(myEmail);
        boolean shouldUpdateMessageMemberStatus = true;
        if (request.flags != -1 && SystemUtils.isFlagEnabled(request.flags, MessagingPlugin.FLAG_DYNAMIC_CHAT)
            && SystemUtils.isFlagEnabled(request.flags, MessagingPlugin.FLAG_ALLOW_CHAT_BUTTONS)) {
            MemberStatusTO ms = new MemberStatusTO();
            ms.acked_timestamp = request.acked_timestamp;
            ms.button_id = request.button_id;
            ms.custom_reply = request.custom_reply;
            ms.member = request.member;
            ms.received_timestamp = request.received_timestamp;
            ms.status = request.status;
            try {
                mStore.insertMemberStatusBIZZ(request.parent_message, request.message, ms);
                shouldUpdateMessageMemberStatus = false;
            } catch (SQLiteConstraintException e) {
            }
        }

        if (shouldUpdateMessageMemberStatus) {
            mStore.updateMessageMemberStatus(request);
        }

        mMessageHistory.putMessageUpdateInHistory(request);

        final Intent intent = new Intent(MessagingPlugin.MESSAGE_MEMBER_STATUS_UPDATE_RECEIVED_INTENT);
        intent.putExtra("message", request.message);
        mMainService.sendBroadcast(intent);
        updateBadge();

        boolean shouldUpdateNotification = false;
        if ((request.status & STATUS_ACKED) == STATUS_ACKED) {
            final String messageSender = mStore.getMessageSenderBIZZ(request.message);
            if (!updateSenderIsMobileOwner && messageSender.equals(myEmail)) {
                if (request.button_id != null) {
                    // Someone gave a quick reply to a question I posed
                    shouldUpdateNotification = true;
                } else {
                    // Someone dismissed a question I posed
                }
            } else if (!updateSenderIsMobileOwner && messageSender.equals(request.member)) {
                // Some time ago, user X sent a message to me. Now he updates
                // his own button choice
            } else if (!updateSenderIsMobileOwner) {
                // Some time ago, user X sent a message to me and user Y...
                // Now user Y updates his choice or dismisses
            } else if (updateSenderIsMobileOwner) {
                // I chose a button on a message I sent myself
                mStore.setMessageProcessed(request.message, request.button_id, request.custom_reply,
                    new SafeRunnable() {
                        @Override
                        protected void safeRun() throws Exception {
                            T.dontCare();
                            final Intent intent = new Intent(MessagingPlugin.MESSAGE_PROCESSED_INTENT);
                            intent.putExtra("message", request.message);
                            mMainService.sendBroadcast(intent);
                            updateBadge();
                        }
                    });
            }
            if (shouldUpdateNotification) {
                // xxx: we could show which button the user pressed in a notification
            }
        }
    }

    private String myEmail() {
        return mMainService.getIdentityStore().getIdentity().getEmail();
    }

    private List<MemberStatusTO> changedAnswersDuringLock(final MessageTO message, final MemberStatusTO[] newStatuses) {
        List<MemberStatusTO> changedAnswers = new ArrayList<MemberStatusTO>();

        if (newStatuses == null || newStatuses.length == 0)
            return changedAnswers;

        for (MemberStatusTO newStatus : newStatuses) {
            for (MemberStatusTO oldStatus : message.members) {
                if (newStatus.member.equals(oldStatus.member)) {
                    String oldAnswer = oldStatus.button_id;
                    String newAnswer = newStatus.button_id;

                    if (!((oldAnswer == null && newAnswer == null) || (oldAnswer != null && oldAnswer.equals(newAnswer))))
                        changedAnswers.add(newStatus);
                }
            }
        }
        return changedAnswers;
    }

    public void messageLocked(final MessageLockedRequestTO request) {
        String threadKey = request.parent_message_key == null ? request.message_key : request.parent_message_key;
        if (mStore.getExistence(threadKey) == EXISTENCE_NOT_FOUND) {
            requestConversation(threadKey);
            return;
        }

        messageLocked(request.message_key, request.members, request.dirty_behavior, true, null);
    }

    public void messageLocked(final String messageKey, final MemberStatusTO[] memberStatuses, long dirtyBehavior,
        final boolean createHistoryItem, final SafeRunnable lockDoneHandler) {
        T.dontCare();

        final MessageTO message = mStore.getFullMessageByKey(messageKey);

        final List<MemberStatusTO> changedAnswers = changedAnswersDuringLock(message, memberStatuses);
        if (dirtyBehavior == DIRTY_BEHAVIOR_NORMAL && changedAnswers.size() != 0)
            dirtyBehavior = DIRTY_BEHAVIOR_MAKE_DIRTY;

        mStore.lockMessage(messageKey, memberStatuses, dirtyBehavior, new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {

                Intent intent = new Intent(MESSAGE_LOCKED_INTENT);
                intent.putExtra("message", messageKey);
                mMainService.sendBroadcast(intent);
                if (lockDoneHandler != null)
                    lockDoneHandler.run();

                if (createHistoryItem) {
                    mMessageHistory.putMessageLockedInHistory(messageKey);

                    for (MemberStatusTO newStatus : changedAnswers) {
                        mMessageHistory.putQuickReplyUndoneInHistory(message, newStatus);
                    }
                }
            }
        });
    }

    public void cleanDirtyFlag(final String messageKey) {
        T.dontCare();
        mStore.setDirty(messageKey, false);
        Intent intent = new Intent(MESSAGE_DIRTY_CLEANED_INTENT);
        intent.putExtra("message", messageKey);
        mMainService.sendBroadcast(intent);
        updateBadge();
    }

    public void cleanThreadDirtyFlags(final String parentMessageKey) {
        T.UI();
        mStore.setMessageThreadRead(parentMessageKey);
        Intent intent = new Intent(MessagingPlugin.MESSAGE_DIRTY_CLEANED_INTENT);
        intent.putExtra("message", parentMessageKey);
        mMainService.sendBroadcast(intent);
        updateBadge();
    }

    public void ackMessage(final MessageTO message, final String button, final String custom_reply,
        final SafeRunnable updateDoneHandler, ServiceBound activity, ViewGroup parentView) {
        T.UI();
        if (SystemUtils.isFlagEnabled(message.flags, FLAG_DYNAMIC_CHAT)
            && !SystemUtils.isFlagEnabled(message.flags, FLAG_ALLOW_CHAT_BUTTONS)) {
            L.w("Trying to ack a chat message!");
            return;
        }

        L.d("Ack message " + message.key + " with button [" + (button == null ? "" : button) + "]");
        ResponseHandler<AckMessageResponseTO> responseHandler = new ResponseHandler<>();
        AckMessageRequestTO ack = new AckMessageRequestTO();
        ack.button_id = button;
        ack.message_key = message.key;
        ack.parent_message_key = message.parent_key;
        ack.custom_reply = custom_reply;
        ack.timestamp = mMainService.currentTimeMillis() / 1000;

        try {
            if ((message.flags & FLAG_SENT_BY_JSMFR) == FLAG_SENT_BY_JSMFR)
                answerJsMfrMessage(message, ack.toJSONMap(), "com.mobicage.api.messaging.ackMessage", activity,
                    parentView);
            else
                com.mobicage.api.messaging.Rpc.ackMessage(responseHandler, ack);
        } catch (Exception e) {
            L.bug(e);
            if (updateDoneHandler != null)
                updateDoneHandler.run();
            return;
        }

        mStore.setMessageProcessed(message.key, button, custom_reply, new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.dontCare();
                if (updateDoneHandler != null)
                    updateDoneHandler.run();
                Intent intent = new Intent(MessagingPlugin.MESSAGE_PROCESSED_INTENT);
                intent.putExtra("message", message.key);
                mMainService.sendBroadcast(intent);
                mMessageHistory.putMessageAckInHistory(message.key, button);
                updateBadge();
            }
        });
    }

    public void ackThread(final String threadKey) {
        ackThread(threadKey, null);
    }

    public void ackThread(final String threadKey, final Long untilTimestamp) {
        T.UI();
        mMainService.postOnBIZZHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                Cursor curs = mStore.getMessagesThatNeedAnswer(threadKey);
                try {
                    boolean proceed = curs.moveToFirst();
                    while (proceed) {
                        String key = curs.getString(0);
                        String parentKey = curs.getString(1);
                        long timestamp = curs.getLong(2);

                        if (untilTimestamp != null && timestamp > untilTimestamp) {
                            break;
                        }

                        ResponseHandler<AckMessageResponseTO> responseHandler = new ResponseHandler<>();
                        AckMessageRequestTO ack = new AckMessageRequestTO();
                        ack.button_id = null;
                        ack.message_key = key;
                        ack.parent_message_key = parentKey;
                        ack.custom_reply = null;
                        ack.timestamp = mMainService.currentTimeMillis() / 1000;
                        try {
                            com.mobicage.api.messaging.Rpc.ackMessage(responseHandler, ack);
                        } catch (Exception e) {
                            L.bug(e);
                        }
                        mStore.setMessageProcessedBizz(key, null, null, null);
                        mStore.setDirty(key, false);
                        proceed = curs.moveToNext();
                        Intent intent = new Intent(MessagingPlugin.MESSAGE_PROCESSED_INTENT);
                        intent.putExtra("message", key);
                        mMainService.sendBroadcast(intent);
                        updateBadge();
                    }
                } finally {
                    curs.close();
                }
            }
        });
    }

    public void ackChat(final String threadKey) {
        T.UI();
        mMainService.postOnBIZZHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                Cursor curs = mStore.getMessagesThatNeedAnswer(threadKey);
                try {
                    boolean proceed = curs.moveToFirst();
                    while (proceed) {
                        String key = curs.getString(0);

                        mStore.setChatMessageProcessedBizz(key);
                        proceed = curs.moveToNext();
                        Intent intent = new Intent(MessagingPlugin.MESSAGE_PROCESSED_INTENT);
                        intent.putExtra("message", key);
                        mMainService.sendBroadcast(intent);
                        updateBadge();
                    }
                } finally {
                    curs.close();
                }
            }
        });

    }

    public void markMessagesAsRead(String parentMessageKey, String[] messageKeys) {
        MarkMessagesAsReadRequestTO request = new MarkMessagesAsReadRequestTO();
        request.parent_message_key = parentMessageKey;
        request.message_keys = messageKeys;

        ResponseHandler<MarkMessagesAsReadResponseTO> rh = new ResponseHandler<MarkMessagesAsReadResponseTO>();

        L.d("markMessagesAsRead: " + request.parent_message_key + ", "
            + android.text.TextUtils.join(" | ", request.message_keys));
        try {
            com.mobicage.api.messaging.Rpc.markMessagesAsRead(rh, request);
        } catch (Exception e) {
            L.bug(e);
            return;
        }
    }

    public boolean isMessageAckedByMe(final MessageTO message) {
        String myEmail = myEmail();
        if (myEmail.equals(message.sender))
            return true;

        MemberStatusTO[] members = message.members;
        if (members == null || members.length == 0) {
            mStore.addMembers(message);
            members = message.members;
        }
        for (MemberStatusTO member : members)
            if (myEmail.equals(member.member))
                return (member.status & STATUS_ACKED) == STATUS_ACKED;

        L.e("I, " + myEmail + ", am not in the member list of message " + message + " with members " + members);
        return false;
    }

    // lock message (because of UI click on phone)
    public void lockMessage(final MessageTO message, final SafeRunnable lockDoneRunnable) {
        T.UI();
        final LockMessageRequestTO request = new LockMessageRequestTO();
        request.message_key = message.key;
        request.message_parent_key = message.parent_key;
        final LockMessageResponseHandler responseHandler = new LockMessageResponseHandler();
        responseHandler.setMessageKey(message.key);
        try {
            com.mobicage.api.messaging.Rpc.lockMessage(responseHandler, request);
        } catch (Exception e) {
            L.bug(e);
            if (lockDoneRunnable != null)
                lockDoneRunnable.run();
            return;
        }
        messageLocked(message.key, new MemberStatusTO[0], DIRTY_BEHAVIOR_NORMAL, false, lockDoneRunnable);
    }

    public boolean deleteConversation(final String threadKey) {
        T.UI();
        DeleteConversationRequestTO request = new DeleteConversationRequestTO();
        request.parent_message_key = threadKey;

        try {
            com.mobicage.api.messaging.Rpc.deleteConversation(new ResponseHandler<DeleteConversationResponseTO>(),
                request);
        } catch (Exception e) {
            L.bug(e);
            return false;
        }

        mStore.deleteConversation(threadKey);
        mMessageHistory.deleteMessageFromHistory(threadKey);

        Intent intent = new Intent(THREAD_DELETED_INTENT);
        intent.putExtra("key", threadKey);
        mMainService.sendBroadcast(intent);
        updateBadge();

        removeNotificationForThread(threadKey);
        try {
            deleteThreadAttachments(threadKey);
        } catch (IOException e) {
            L.d("Failed to remove attachments when thread was removed", e);
        }

        return true;
    }

    public void removeNotificationForThread(String threadKey) {
        UIUtils.cancelNotification(mMainService, threadKey);
        updateUnreadCountBadge();
    }

    public void conversationDeleted(final String threadKey) {
        T.BIZZ();
        mBrandingMgr.deleteConversation(threadKey);
        mStore.deleteConversation(threadKey);
        mMessageHistory.deleteMessageFromHistory(threadKey);

        Intent intent = new Intent(THREAD_DELETED_INTENT);
        intent.putExtra("key", threadKey);
        mMainService.sendBroadcast(intent);
        updateBadge();
        removeNotificationForThread(threadKey);
        try {
            deleteThreadAttachments(threadKey);
        } catch (IOException e) {
            L.d("Failed to remove attachments when conversation was deleted", e);
        }
    }

    public boolean requestConversation(final String threadKey) {
        return requestConversation(threadKey, null);
    }

    public boolean requestConversation(final String threadKey, final String offset) {
        T.BIZZ();
        if (mStore.isConversationAlreadyRequested(threadKey)) {
            L.d("Thread " + threadKey + " is already requested.");
            return false;
        }

        GetConversationRequestTO request = new GetConversationRequestTO();
        request.parent_message_key = threadKey;
        request.offset = offset;

        GetConversationResponseHandler responseHandler = new GetConversationResponseHandler();
        responseHandler.setThreadKey(threadKey);

        try {
            com.mobicage.api.messaging.Rpc.getConversation(responseHandler, request);
        } catch (Exception e) {
            L.bug(e);
            return false;
        }

        mStore.addRequestedConversation(threadKey);
        return true;
    }

    public void formSubmitted(final Message message, final String button) {
        mStore.updateForm(message);
        mStore.setMessageProcessed(message.key, button, null, new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.dontCare();
                Intent intent = new Intent(MessagingPlugin.MESSAGE_PROCESSED_INTENT);
                intent.putExtra("message", message.key);
                mMainService.sendBroadcast(intent);
                updateBadge();
            }
        });
    }

    public void updateForm(final String parentMessageKey, final String messageKey,
        final UnicodeWidgetResultTO formResult, final String buttonId, final long receivedTimestamp,
        final long ackedTimestamp) {

        IFormResultProcessor resultProcessor = new IFormResultProcessor() {
            @Override
            public void processResult(Message message) {
                if (formResult != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> widget = (Map<String, Object>) message.form.get("widget");
                    widget.put("value", formResult.value);
                }
            }
        };
        saveFormUpdate(parentMessageKey, messageKey, buttonId, receivedTimestamp, ackedTimestamp, resultProcessor);
    }

    public void updateForm(final String parentMessageKey, final String messageKey,
        final UnicodeListWidgetResultTO formResult, final String buttonId, final long receivedTimestamp,
        final long ackedTimestamp) {

        IFormResultProcessor resultProcessor = new IFormResultProcessor() {
            @Override
            public void processResult(Message message) {
                if (formResult != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> widget = (Map<String, Object>) message.form.get("widget");
                    widget.put("values", Arrays.asList(formResult.values));
                }
            }
        };
        saveFormUpdate(parentMessageKey, messageKey, buttonId, receivedTimestamp, ackedTimestamp, resultProcessor);
    }

    public void updateForm(final String parentMessageKey, final String messageKey,
        final FloatWidgetResultTO formResult, final String buttonId, final long receivedTimestamp,
        final long ackedTimestamp) {

        updateForm(parentMessageKey, messageKey, formResult, buttonId, receivedTimestamp, ackedTimestamp, "value");
    }

    public void updateForm(final String parentMessageKey, final String messageKey,
        final FloatWidgetResultTO formResult, final String buttonId, final long receivedTimestamp,
        final long ackedTimestamp, final String valueString) {

        IFormResultProcessor resultProcessor = new IFormResultProcessor() {
            @Override
            public void processResult(Message message) {
                if (formResult != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> widget = (Map<String, Object>) message.form.get("widget");
                    widget.put(valueString, formResult.value);
                }
            }
        };
        saveFormUpdate(parentMessageKey, messageKey, buttonId, receivedTimestamp, ackedTimestamp, resultProcessor);
    }

    public void updateForm(final String parentMessageKey, final String messageKey, final LongWidgetResultTO formResult,
        final String buttonId, final long receivedTimestamp, final long ackedTimestamp) {

        updateForm(parentMessageKey, messageKey, formResult, buttonId, receivedTimestamp, ackedTimestamp, "value");
    }

    public void updateForm(final String parentMessageKey, final String messageKey, final LongWidgetResultTO formResult,
        final String buttonId, final long receivedTimestamp, final long ackedTimestamp, final String valueString) {

        IFormResultProcessor resultProcessor = new IFormResultProcessor() {
            @Override
            public void processResult(Message message) {
                if (formResult != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> widget = (Map<String, Object>) message.form.get("widget");
                    widget.put(valueString, formResult.value);
                }
            }
        };
        saveFormUpdate(parentMessageKey, messageKey, buttonId, receivedTimestamp, ackedTimestamp, resultProcessor);
    }

    public void updateRangeSliderForm(final String parentMessageKey, final String messageKey,
        final FloatListWidgetResultTO formResult, final String buttonId, final long receivedTimestamp,
        final long ackedTimestamp) {

        IFormResultProcessor resultProcessor = new IFormResultProcessor() {
            @Override
            public void processResult(final Message message) {
                if (formResult != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> widget = (Map<String, Object>) message.form.get("widget");
                    widget.put("low_value", formResult.values[0]);
                    widget.put("high_value", formResult.values[1]);
                }
            }
        };
        saveFormUpdate(parentMessageKey, messageKey, buttonId, receivedTimestamp, ackedTimestamp, resultProcessor);
    }

    public void updateGPSLocationForm(final String parentMessageKey, final String messageKey,
        final LocationWidgetResultTO formResult, final String buttonId, final long receivedTimestamp,
        final long ackedTimestamp) {

        IFormResultProcessor resultProcessor = new IFormResultProcessor() {
            @Override
            public void processResult(final Message message) {
                if (formResult != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> widget = (Map<String, Object>) message.form.get("widget");
                    widget.put("value", formResult.toJSONMap());
                }
            }
        };
        saveFormUpdate(parentMessageKey, messageKey, buttonId, receivedTimestamp, ackedTimestamp, resultProcessor);
    }

    public void updateMyDigiPassForm(final String parentMessageKey, final String messageKey,
        final MyDigiPassWidgetResultTO formResult, final String buttonId, final long receivedTimestamp,
        final long ackedTimestamp) {

        IFormResultProcessor resultProcessor = new IFormResultProcessor() {
            @Override
            public void processResult(final Message message) {
                if (formResult != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> widget = (Map<String, Object>) message.form.get("widget");
                    widget.put("value", formResult.toJSONMap());
                }
            }
        };
        saveFormUpdate(parentMessageKey, messageKey, buttonId, receivedTimestamp, ackedTimestamp, resultProcessor);
    }

    public void updateAdvancedOrderForm(final String parentMessageKey, final String messageKey,
        final AdvancedOrderWidgetResultTO formResult, final String buttonId, final long receivedTimestamp,
        final long ackedTimestamp) {

        IFormResultProcessor resultProcessor = new IFormResultProcessor() {
            @Override
            public void processResult(final Message message) {
                if (formResult != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> widget = (Map<String, Object>) message.form.get("widget");
                    widget.put("value", formResult.toJSONMap());
                }
            }
        };
        saveFormUpdate(parentMessageKey, messageKey, buttonId, receivedTimestamp, ackedTimestamp, resultProcessor);
    }

    public void startFlow(final StartFlowRequestTO startFlow) {
        T.BIZZ();
        final StartFlowRequest flow;
        try {
            flow = new StartFlowRequest(startFlow.toJSONMap());
        } catch (IncompleteMessageException e) {
            L.bug("Should not happen", e);
            return;
        }

        flow.thread_key = startFlow.parent_message_key != null ? startFlow.parent_message_key : "_js_"
            + UUID.randomUUID().toString();

        final FriendsPlugin friendsPlugin = mMainService.getPlugin(FriendsPlugin.class);
        friendsPlugin.getStore().storeStaticFlow(flow.static_flow, flow.static_flow_hash);

        if (!mBrandingMgr.queue(flow)) {
            mMainService.postOnUIHandler(new SafeRunnable() {
                @Override
                public void safeRun() {
                    T.UI();
                    startLocalFlow(flow);
                }
            });
        }
    }

    public void startLocalFlow(final StartFlowRequest startFlow) {
        T.UI();
        FlowStartedRequestTO request = new FlowStartedRequestTO();
        request.thread_key = startFlow.thread_key;
        request.service = startFlow.service;
        request.static_flow_hash = startFlow.static_flow_hash;

        Map<String, Object> userInput = new HashMap<String, Object>();
        userInput.put("request", request.toJSONMap());
        userInput.put("func", "com.mobicage.api.messaging.jsmfr.flowStarted");

        Map<String, Object> tmpState = new HashMap<String, Object>();
        tmpState.put("message_flow_run_id", startFlow.message_flow_run_id);
        tmpState.put("flow_params", startFlow.flow_params);
        MessageFlowRun mfr = new MessageFlowRun();
        mfr.staticFlowHash = startFlow.static_flow_hash;
        mfr.state = JSONValue.toJSONString(tmpState);

        try {
            JsMfr.executeMfr(mfr, userInput, mMainService, false);
        } catch (EmptyStaticFlowException ex) {
            L.bug(ex);
        }
    }

    private void saveFormUpdate(String parentMessageKey, String messageKey, String buttonId, long receivedTimestamp,
        long ackedTimestamp, IFormResultProcessor resultProcessor) {

        Message message = mStore.getPartialMessageByKey(messageKey);
        if (message == null) {
            requestConversation(parentMessageKey == null ? messageKey : parentMessageKey);
            return;
        }

        resultProcessor.processResult(message);

        mStore.addMembers(message);
        mStore.saveFormUpdate(message, buttonId, receivedTimestamp, ackedTimestamp);

        Intent intent = new Intent(MessagingPlugin.MESSAGE_PROCESSED_INTENT);
        intent.putExtra("message", message.key);
        mMainService.sendBroadcast(intent);
        updateBadge();
    }

    public String validateFormResult(Message message, IJSONable formResult) {
        String javascriptValidation = (String) message.form.get("javascript_validation");
        if (!TextUtils.isEmptyOrWhitespace(javascriptValidation)) {
            return validateFormResult(message.sender, message.getThreadKey(), javascriptValidation, formResult);
        } else {
            return null;
        }
    }

    public String validateFormResult(String serviceEmail, String threadKey, String javascriptValidationCode,
        IJSONable formResult) {
        T.UI();
        return JsMfr.executeFormResultValidation(serviceEmail, threadKey, javascriptValidationCode,
            formResult.toJSONMap(), mMainService);
    }

    public void answerJsMfrMessage(MessageTO message, Map<String, Object> request, String function,
        ServiceBound activity, ViewGroup parentView) {
        String threadKey = message.parent_key == null ? message.key : message.parent_key;
        answerJsMfrMessage(threadKey, request, function, activity, parentView);
    }

    public void answerJsMfrMessage(String threadKey, Map<String, Object> request, String function,
        ServiceBound activity, ViewGroup parentView) {
        T.UI();

        MessageFlowRun mfr = mStore.getMessageFlowRun(threadKey);

        Map<String, Object> userInput = new HashMap<String, Object>();
        userInput.put("request", request);
        userInput.put("func", function);
        try {
            JsMfr.executeMfr(mfr, userInput, mMainService, false);
        } catch (EmptyStaticFlowException ex) {
            L.bug(ex);
        }
    }

    public void endMessageFlow(String parentMessageKey, boolean waitForFollowup) {
        mStore.deleteMessageFlowRun(parentMessageKey);
        Intent intent = new Intent(MESSAGE_FLOW_ENDED_INTENT);
        intent.putExtra("parent_message_key", parentMessageKey);
        intent.putExtra("wait_for_followup", waitForFollowup);
        mMainService.sendBroadcast(intent);
    }

    public void setMessageThreadVisibility(String threadKey, boolean visible) {
        mStore.setMessageThreadVisibility(threadKey, visible);

        Intent intent = new Intent(MessagingPlugin.MESSAGE_THREAD_VISIBILITY_CHANGED_INTENT);
        intent.putExtra("thread_key", threadKey);
        intent.putExtra("visible", visible);
        mMainService.sendBroadcast(intent);
    }

    public void putSendMessageRequest(final String tmpKey, final SendMessageRequestTO request) {
        T.UI();
        Configuration cfg = new Configuration();
        cfg.put(tmpKey, JSONValue.toJSONString(request.toJSONMap()));
        mConfigProvider.updateConfigurationNow(TRANSFER_PHOTO_UPLOAD_SEND_MESSAGE_CONFIGKEY, cfg);
    }

    public void setTransferCompleted(final String parentMessageKey, final String messageKey, String resultUrl) {
        T.BIZZ();
        Message message = mStore.getFullMessageByKey(messageKey);
        if (message == null) {
            return;
        }
        if (message.form == null) {
            Configuration cfg = mConfigProvider.getConfiguration(TRANSFER_PHOTO_UPLOAD_SEND_MESSAGE_CONFIGKEY);

            String serializedMessageRequest = cfg.get(messageKey, "");
            if ("".equals(serializedMessageRequest)) {
                L.bug("Could not find SendMessageRequestTO " + messageKey);
            } else {
                transferQueueDelete(messageKey);
                @SuppressWarnings("unchecked")
                Map<String, Object> jsonMap = (Map<String, Object>) JSONValue.parse(serializedMessageRequest);
                SendMessageRequestTO request;
                try {
                    request = new SendMessageRequestTO(jsonMap);
                } catch (IncompleteMessageException e1) {
                    L.bug(e1);
                    return;
                }

                request.attachments[0].download_url = resultUrl;
                try {
                    SendMessageView.sendMessage(request, mMainService);
                } catch (Exception e) {
                    L.bug("Failed to send message after transfer was complete", e);
                }
            }

        } else {
            if (Widget.TYPE_PHOTO_UPLOAD.equals(message.form.get("type"))) {
                final SubmitPhotoUploadFormRequestTO request = new SubmitPhotoUploadFormRequestTO();
                request.timestamp = message.members[0].acked_timestamp;
                request.button_id = message.members[0].button_id;
                request.message_key = messageKey;
                request.parent_message_key = parentMessageKey;
                if (Message.POSITIVE.equals(request.button_id)) {
                    request.result = new UnicodeWidgetResultTO();
                    request.result.value = resultUrl;
                }
                boolean isSentByJSMFR = (message.flags & FLAG_SENT_BY_JSMFR) == FLAG_SENT_BY_JSMFR;

                transferQueueDelete(messageKey);
                if (UIUtils.getTopActivity(mMainService) instanceof ServiceMessageDetailActivity) {
                    // Send an Intent to ServiceMessageDetailActivity so it can hide the processing dialog
                    final Intent iSubmitPhotoUploadForm = new Intent(ServiceMessageDetailActivity.class.getName());
                    iSubmitPhotoUploadForm.putExtra("threadKey", parentMessageKey == null ? messageKey
                        : parentMessageKey);
                    iSubmitPhotoUploadForm.putExtra("message_key", messageKey);
                    iSubmitPhotoUploadForm.setAction(MessagingPlugin.MESSAGE_SUBMIT_PHOTO_UPLOAD);
                    if (isSentByJSMFR) {
                        iSubmitPhotoUploadForm.putExtra("submitToJSMFR",
                            JSONValue.toJSONString(request.toJSONMap()));
                    }
                    mMainService.sendBroadcast(iSubmitPhotoUploadForm);
                    L.d("------------------------------------------------------------");
                    L.d("ServiceMessageDetailActivity on top");
                    L.d("------------------------------------------------------------");
                } else {
                    L.d("------------------------------------------------------------");
                    L.d("ServiceMessageDetailActivity NOT on top");
                    L.d("------------------------------------------------------------");

                    if (isSentByJSMFR) {
                        mMainService.postOnUIHandler(new SafeRunnable() {
                            @Override
                            protected void safeRun() throws Exception {
                                // TODO read from db
                                JSONObject json = jsmfrTransferCompletedLoad();
                                // TODO add current message
                                JSONObject transfer = new JSONObject();
                                try {
                                    transfer.put("threadKey", parentMessageKey == null ? messageKey
                                        : parentMessageKey);
                                    transfer.put("submitToJSMFR", JSONValue.toJSONString(request.toJSONMap()));
                                    json.put(messageKey, transfer);
                                } catch (JSONException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                // TODO save to db
                                jsmfrTransferCompletedSave(json);

                                String n_message = mMainService.getString(R.string.transfer_complete_notification);
                                String title = mMainService
                                    .getString(R.string.transfer_complete_notification_title);
                                int notificationId = R.integer.transfer_complete_continue;
                                boolean withSound = false;
                                boolean withVibration = true;
                                boolean withLight = false;
                                boolean autoCancel = false;
                                int icon = R.drawable.notification_icon;
                                int notificationNumber = 0;
                                Bundle b = new Bundle();
                                b.putString("threadKey", parentMessageKey == null ? messageKey : parentMessageKey);
                                b.putString("message_key", messageKey);
                                b.putBoolean("submitToJSMFR", true);
                                String tickerText = null;
                                long timestamp = mMainService.currentTimeMillis();

                                UIUtils.doNotification(mMainService, title, n_message, notificationId,
                                        MainActivity.ACTION_NOTIFICATION_PHOTO_UPLOAD_DONE, withSound, withVibration,
                                        withLight, autoCancel, icon, notificationNumber, b, tickerText, timestamp,
                                        Notification.PRIORITY_LOW, null, null, null, NotificationCompat.CATEGORY_EVENT);
                            }
                        });
                    }
                }

                if (!isSentByJSMFR) {
                    try {
                        Rpc.submitPhotoUploadForm(new ResponseHandler<SubmitPhotoUploadFormResponseTO>(), request);
                    } catch (Exception e) {
                        L.e("Sending the submitPhotoUploadForm failed.", e);
                    }
                }
            }
        }
    }

    private void hideTransferPendingNotification() {
        T.dontCare();
        UIUtils.cancelNotification(mMainService, R.integer.photo_upload_pending);
    }

    private void showTransferPendingNotification(String notificationMessage) {
        T.dontCare();
        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(mMainService)
                .setContentTitle(mMainService.getString(R.string.app_name))
                .setContentText(notificationMessage)
                .setSmallIcon(R.drawable.notification_icon)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationMessage))
                .setAutoCancel(false);

        final NotificationManager nm = (NotificationManager) mMainService.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = mBuilder.build();
        notification.flags |= NotificationCompat.FLAG_ONGOING_EVENT;
        notification.flags &= ~NotificationCompat.FLAG_AUTO_CANCEL;

        Intent i = new Intent(MainActivity.ACTION_NOTIFICATION_OPEN_APP, null, mMainService, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notification.contentIntent = PendingIntent.getActivity(mMainService, 1000, i, PendingIntent.FLAG_UPDATE_CURRENT);

        nm.notify(R.integer.photo_upload_pending, notification);
    }

    private long getExpectNext(long uiFlags) {
        T.dontCare();
        if ((uiFlags & MessagingPlugin.UI_FLAG_EXPECT_NEXT_WAIT_5) == MessagingPlugin.UI_FLAG_EXPECT_NEXT_WAIT_5)
            return 10;
        return 0;
    }

    public boolean startUploadingFile(final File f, final Message message, final String contentType) {
        return startUploadingFile(f, message.parent_key, message.key, message.sender,
            getExpectNext(message.getButton(Message.POSITIVE).ui_flags), true, contentType);
    }

    public boolean startUploadingFile(final File f, final String parentKey, final String messageKey,
        final String service, final long expectNext, final boolean deleteImageOnFinish, final String contentType) {
        T.UI();
        SharedPreferences options = PreferenceManager.getDefaultSharedPreferences(mMainService);
        boolean wifiOnlySettingEnabled = options.getBoolean(MainService.PREFERENCE_UPLOAD_PHOTO_WIFI, false);

        boolean willDirectlyStartTransferring = true;
        if (!mMainService.getNetworkConnectivityManager().isConnected()) {
            showTransferPendingNotification(mMainService.getString(R.string.transfer_pending_notification_no_network));

            if (expectNext == 0) {
                UIUtils.showLongToast(mMainService,
                    mMainService.getString(R.string.transfer_pending_notification_no_network));
            } else {
                UIUtils.showLongToast(mMainService,
                    mMainService.getString(R.string.transfer_pending_notification_no_network_followup));
            }

            willDirectlyStartTransferring = false;

        } else if (wifiOnlySettingEnabled && !mMainService.getNetworkConnectivityManager().isWifiConnected()) {
            showTransferPendingNotification(mMainService.getString(R.string.transfer_pending_notification_no_wifi));

            if (expectNext == 0) {
                UIUtils.showLongToast(mMainService,
                    mMainService.getString(R.string.transfer_pending_notification_no_wifi));
            } else {
                UIUtils.showLongToast(mMainService,
                    mMainService.getString(R.string.transfer_pending_notification_no_wifi_followup));
            }

            willDirectlyStartTransferring = false;
        }

        mMainService.postOnBIZZHandler(new SafeRunnable() {
            @SuppressWarnings("null")
            @Override
            protected void safeRun() throws Exception {
                FileInputStream in = new FileInputStream(f);
                try {
                    byte[] buffer = new byte[MAX_CHUNK_SIZE];
                    MessageDigest complete = MessageDigest.getInstance("SHA-256");
                    DigestInputStream dis = new DigestInputStream(in, complete);
                    try {
                        ZipUtils.DeflaterInputStream dos = new ZipUtils.DeflaterInputStream(dis);
                        try {
                            UploadChunkRequestTO chunkRequest = null;
                            int chunkNumber = 0;
                            while (true) {
                                int bufferPosition = 0;
                                while (true) {
                                    int numRead = dos.read(buffer, bufferPosition, MAX_CHUNK_SIZE - bufferPosition);
                                    if (numRead == -1)
                                        break;
                                    bufferPosition += numRead;
                                    if (bufferPosition == MAX_CHUNK_SIZE)
                                        break;
                                }
                                if (bufferPosition == 0) {
                                    chunkRequest.total_chunks = chunkRequest.number;
                                    chunkRequest.photo_hash = com.mobicage.rogerthat.util.TextUtils.toHex(complete
                                        .digest());
                                    UploadChunkResponseHandler responsehandler = new UploadChunkResponseHandler();
                                    responsehandler.setChunkRequest(chunkRequest);
                                    Rpc.uploadChunk(responsehandler, chunkRequest);
                                    break;
                                }
                                if (chunkNumber > 0) {
                                    chunkRequest.total_chunks = -1;
                                    chunkRequest.photo_hash = null;
                                    UploadChunkResponseHandler responsehandler = new UploadChunkResponseHandler();
                                    responsehandler.setChunkRequest(chunkRequest);
                                    Rpc.uploadChunk(responsehandler, chunkRequest);
                                }

                                String chunk = null;
                                if (bufferPosition == MAX_CHUNK_SIZE)
                                    chunk = Base64.encodeBytes(buffer);
                                else {
                                    byte[] tmp = new byte[bufferPosition];
                                    System.arraycopy(buffer, 0, tmp, 0, bufferPosition);
                                    chunk = Base64.encodeBytes(tmp);
                                }
                                chunkRequest = new UploadChunkRequestTO();
                                chunkRequest.parent_message_key = parentKey;
                                chunkRequest.message_key = messageKey;
                                chunkRequest.number = ++chunkNumber;
                                chunkRequest.chunk = chunk;
                                chunkRequest.service_identity_user = service;
                                chunkRequest.content_type = contentType;
                                if (bufferPosition < MAX_CHUNK_SIZE) {
                                    chunkRequest.total_chunks = chunkRequest.number;
                                    chunkRequest.photo_hash = com.mobicage.rogerthat.util.TextUtils.toHex(complete
                                        .digest());
                                    UploadChunkResponseHandler responsehandler = new UploadChunkResponseHandler();
                                    responsehandler.setChunkRequest(chunkRequest);
                                    Rpc.uploadChunk(responsehandler, chunkRequest);
                                    break;
                                }
                            }
                        } finally {
                            dos.close();
                        }

                    } finally {
                        dis.close();
                    }

                } finally {
                    in.close();
                }
                if (deleteImageOnFinish)
                    f.delete();
                transferQueueAdd(messageKey);
            }
        });
        return willDirectlyStartTransferring;
    }

    private void transferQueueAdd(String messageKey) {
        T.BIZZ();
        mTransferQueue.add(messageKey);
        transferQueueSave();
    }

    private void transferQueueDelete(String messageKey) {
        T.BIZZ();
        mTransferQueue.remove(messageKey);
        transferQueueSave();

        if (mTransferQueue.size() == 0) {
            hideTransferPendingNotification();
        }
    }

    private void transferQueueSave() {
        T.BIZZ();
        Configuration cfg = new Configuration();
        cfg.put(TRANSFER_PHOTO_UPLOAD_CONFIGKEY, android.text.TextUtils.join(";", mTransferQueue));
        mConfigProvider.updateConfigurationNow(TRANSFER_UPLOAD_CONFIGKEY, cfg);
    }

    private void transferQueueLoad() {
        T.BIZZ();
        mTransferQueue = new ArrayList<>();
        Configuration cfg = mConfigProvider.getConfiguration(TRANSFER_UPLOAD_CONFIGKEY);

        String serializedTransferQueue = cfg.get(TRANSFER_PHOTO_UPLOAD_CONFIGKEY, "");
        if (!"".equals(serializedTransferQueue)) {
            try {
                Collections.addAll(mTransferQueue, serializedTransferQueue.split(";"));
            } catch (Exception e) {
                L.bug(e);
            }
        }
    }

    private void jsmfrTransferCompletedSave(JSONObject jo) {
        T.UI();
        Configuration cfg = new Configuration();
        cfg.put(TRANSFER_JSMFR_PHOTO_COMPLETED_CONFIGKEY, jo.toString());
        mConfigProvider.updateConfigurationNow(TRANSFER_JSMFR_COMPLETED_CONFIGKEY, cfg);
    }

    private JSONObject jsmfrTransferCompletedLoad() {
        T.UI();
        Configuration cfg = mConfigProvider.getConfiguration(TRANSFER_JSMFR_COMPLETED_CONFIGKEY);
        String jo = cfg.get(TRANSFER_JSMFR_PHOTO_COMPLETED_CONFIGKEY, "");
        if ("".equals(jo))
            return new JSONObject();
        JSONObject outMap = new JSONObject();
        try {
            outMap = new JSONObject(jo);
        } catch (JSONException e) {
            L.e(e);
        }

        return outMap;
    }

    public JSONObject jsmfrTransferCompletedGetNext() {
        T.UI();
        JSONObject r = jsmfrTransferCompletedLoad();

        Iterator<String> iter = r.keys();
        while (iter.hasNext()) {
            String key = iter.next();
            try {
                JSONObject value = (JSONObject) r.get(key);
                r.remove(key);
                jsmfrTransferCompletedSave(r);
                return value;
            } catch (JSONException e) {
                L.e(e);
            }
        }
        return null;
    }

    private void deleteThreadAttachments(String threadKey) throws IOException {
        File attachmentsDir = attachmentTreadDir(threadKey);
        if (attachmentsDir.exists() && !SystemUtils.deleteDir(attachmentsDir)) {
            L.bug("Could not delete attachment thread. threadKey: " + threadKey);
        }
    }

    public File attachmentTreadDir(String threadKey) throws IOException {
        File file = IOUtils.getFilesDirectory(mMainService);
        file = new File(file, "attachments");
        file = new File(file, threadKey);
        createDirIfNotExists(file);
        return file;
    }

    public File attachmentsDir(String threadKey, String messageKey) throws IOException {
        File file = attachmentTreadDir(threadKey);
        if (messageKey != null)
            file = new File(file, messageKey);
        createDirIfNotExists(file);
        return file;
    }

    public String attachmentDownloadUrlHash(String downloadUrl) {
        return Security.sha256(downloadUrl);
    }

    public boolean attachmentExists(File dir, String downloadUrlHash) {
        File file = new File(dir, downloadUrlHash);
        return file.exists();
    }

    public File attachmentFile(Message message, AttachmentTO attachment) throws IOException {
        return new File(attachmentsDir(message.getThreadKey(), message.key), attachmentDownloadUrlHash(attachment
                .download_url));
    }

    private void createDirIfNotExists(File file) throws IOException {
        T.dontCare();
        if (!file.exists()) {
            if (!file.mkdirs())
                throw new IOException(mMainService.getString(R.string.failed_to_create_directory,
                    file.getAbsolutePath()));
        }
    }

    public String createAttachmentThumbnail(final AttachmentDownload attachment) throws Exception {
        boolean isImage = attachment.content_type.toLowerCase(Locale.US).startsWith("image/");
        boolean isVideo = !isImage && attachment.content_type.toLowerCase(Locale.US).startsWith("video/");

        if (isImage || isVideo) {
            final String attachmentPath = mBrandingMgr.getAttachmentFile(attachment).getAbsolutePath();
            return createAttachmentThumbnail(attachmentPath, isImage, isVideo);
        }

        return null;
    }

    public String createAttachmentThumbnail(final String attachmentPath, boolean isImage, boolean isVideo)
        throws IOException {
        int maxWidthOrHeight = UIUtils.convertDipToPixels(mMainService, 200);
        Bitmap thumbnail = null;

        if (isImage) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            Bitmap source = ImageHelper.getBitmapFromFile(attachmentPath, options);
            thumbnail = UIUtils.createThumbnail(source, maxWidthOrHeight);
        } else if (isVideo) {
            thumbnail = UIUtils.createVideoThumbnail(mMainService, attachmentPath, maxWidthOrHeight);
        }

        if (thumbnail != null) {
            final String dest = attachmentPath + ".thumb";
            final FileOutputStream stream = new FileOutputStream(dest);
            try {
                thumbnail.compress(Bitmap.CompressFormat.PNG, 85, stream);
            } finally {
                stream.close();
            }
            return dest;
        }

        return null;
    }

    public long getBadgeCount() {
        return mStore.getDirtyThreadsCount();
    }

    public void updateBadge() {
        Intent intent = new Intent(MainService.UPDATE_BADGE_INTENT);
        intent.putExtra("key", "messages");
        intent.putExtra("count", getBadgeCount());
        mMainService.sendBroadcast(intent);
    }

    public String getMessageTextFromButtonsOrAttachments(Message message) {
        String messageText = "";
        if (message.buttons != null && message.buttons.length > 0) {
            List<String> buttons = new ArrayList<>();
            for (ButtonTO bt : message.buttons) {
                buttons.add(bt.caption);
            }
            messageText = android.text.TextUtils.join(" / ", buttons);
        } else if (message.attachments != null && message.attachments.length > 0) {
            Set<String> attachments = new HashSet<>();
            for (AttachmentTO at : message.attachments) {
                if (!TextUtils.isEmptyOrWhitespace(at.name)) {
                    attachments.add(at.name);
                } else if (at.content_type.toLowerCase(Locale.US).startsWith("video/")) {
                    attachments.add(mMainService.getString(R.string.attachment_name_video));
                } else if (at.content_type.toLowerCase(Locale.US).startsWith("image/")) {
                    attachments.add(mMainService.getString(R.string.attachment_name_image));
                } else {
                    L.d("Not added attachment with type '" + at.content_type + "' because no translation found");
                }
            }
            if (attachments.size() > 0) {
                messageText = android.text.TextUtils.join(", ", attachments);
            }
        }
        return messageText;
    }
}
