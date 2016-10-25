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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.messaging.mfr.MessageFlowRun;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.db.DatabaseManager;
import com.mobicage.rogerthat.util.db.MultiThreadedSQLStatement;
import com.mobicage.rogerthat.util.db.Transaction;
import com.mobicage.rogerthat.util.db.TransactionHelper;
import com.mobicage.rogerthat.util.db.TransactionWithoutResult;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.time.TimeUtils;
import com.mobicage.rogerthat.util.ui.ImageHelper;
import com.mobicage.rpc.config.CloudConstants;
import com.mobicage.to.messaging.AttachmentTO;
import com.mobicage.to.messaging.ButtonTO;
import com.mobicage.to.messaging.GetConversationAvatarRequestTO;
import com.mobicage.to.messaging.MemberStatusTO;
import com.mobicage.to.messaging.MemberStatusUpdateRequestTO;
import com.mobicage.to.messaging.MessageTO;

import org.jivesoftware.smack.util.Base64;
import org.json.simple.JSONValue;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.mobicage.rogerthat.util.db.DBUtils.bindString;

public class MessageStore implements Closeable {

    public class CursorSet {
        public Cursor cursor;
        public int query;
        public Cursor indexer;
    }

    private static final long UNKNOWN_SORT_ID = -1;

    private final DatabaseManager mDatabaseManager;
    private final SQLiteDatabase mDb;
    private final MainService mMainService;
    private final FriendsPlugin mFriendsPlugin;

    private final MultiThreadedSQLStatement mSetMessageDirty;

    private final SQLiteStatement mAddMemberStatusBIZZ;
    private final SQLiteStatement mGetCountUI;
    private final SQLiteStatement mSetMessageProcessedBIZZ;
    private final SQLiteStatement mInsertMessageBIZZ;
    private final SQLiteStatement mAddButtonBIZZ;
    private final SQLiteStatement mUpdateMemberStatusBIZZ;
    private final SQLiteStatement mGetParentSortIDBIZZ;
    private final SQLiteStatement mGetMaxTimestampBySortIDBIZZ;
    private final SQLiteStatement mGetMessageSenderBIZZ;
    private final SQLiteStatement mUpdateMessageKeyAndTimestampBIZZ;
    private final SQLiteStatement mUpdateMessageButtonKeyBIZZ;
    private final SQLiteStatement mUpdateMessageAttachmentKeyBIZZ;
    private final SQLiteStatement mUpdateMessageMemberKeyBIZZ;
    private final SQLiteStatement mGetMessageButtonCountBIZZ;
    private final SQLiteStatement mUpdateFormBIZZ;
    private final SQLiteStatement mUpdateMyMemberStatusBIZZ;
    private final SQLiteStatement mGetMessageNeedsAnswerUI;
    private final SQLiteStatement mGetMessageNeedsAnswerBIZZ;
    private final SQLiteStatement mUpdateFlagsBIZZ;
    private final SQLiteStatement mGetMessageFlagsUI;
    private final SQLiteStatement mGetMessageFlagsBIZZ;
    private final SQLiteStatement mUpdateSortidForThreadBySortidBIZZ;
    private final SQLiteStatement mGetHighestSortidBIZZ;
    private final SQLiteStatement mUpdateMessageLastThreadMessageBIZZ;

    private final SQLiteStatement mSetMessageThreadReadUI;
    private final SQLiteStatement mSetMessageThreadVisibilityUI;
    private final SQLiteStatement mGetThreadShowInListBIZZ;

    private final SQLiteStatement mGetLastInboxOpenTime;
    private final SQLiteStatement mSetLastInboxOpenTime;

    private final SQLiteStatement mGetMessageExistenceUI;
    private final MultiThreadedSQLStatement mSetMessageExistence;

    private final SQLiteStatement mInsertRequestedConversationBIZZ;
    private final SQLiteStatement mDeleteRequestedConversationBIZZ;
    private final SQLiteStatement mCountRequestedConversationBIZZ;

    private final SQLiteStatement mSaveMessageFlowRunHTTP;
    private final SQLiteStatement mDeleteMessageFlowRunHTTP;

    private final SQLiteStatement mRecalculateMessagesShowInList;

    private final SQLiteStatement mAddMessageAttachments;

    private final SQLiteStatement mGetDirtyThreadsCount;

    private final SQLiteStatement mThreadAvatarCountByHash;
    private final SQLiteStatement mStoreThreadAvatar;

    private final SQLiteStatement mGetMessageMessage;

    private final SQLiteStatement mGetUnreadMessageCountInThread;
    private final SQLiteStatement mGetTotalDirtyMessagecount;

    public MessageStore(final DatabaseManager databaseManager, final MainService mainService) {
        T.UI();
        mDatabaseManager = databaseManager;
        mDb = mDatabaseManager.getDatabase();
        mMainService = mainService;
        mFriendsPlugin = mMainService.getPlugin(FriendsPlugin.class);

        mGetCountUI = mDb.compileStatement(mMainService.getString(R.string.sql_message_get_count));

        mSetMessageProcessedBIZZ = mDb.compileStatement(mMainService
            .getString(R.string.sql_message_set_message_processed));
        mInsertMessageBIZZ = mDb.compileStatement(mMainService.getString(R.string.sql_message_insert));
        mGetParentSortIDBIZZ = mDb.compileStatement(mMainService.getString(R.string.sql_message_get_parent_sortid));
        mGetMaxTimestampBySortIDBIZZ = mDb.compileStatement(mMainService
            .getString(R.string.sql_message_max_timestamp_by_sortid));
        mAddButtonBIZZ = mDb.compileStatement(mMainService.getString(R.string.sql_message_insert_button));
        mAddMemberStatusBIZZ = mDb.compileStatement(mMainService.getString(R.string.sql_message_insert_member_status));
        mUpdateMemberStatusBIZZ = mDb.compileStatement(mMainService
            .getString(R.string.sql_message_update_member_status));
        mGetMessageSenderBIZZ = mDb.compileStatement(mMainService.getString(R.string.sql_message_get_message_sender));
        mUpdateMessageKeyAndTimestampBIZZ = mDb.compileStatement(mMainService
            .getString(R.string.sql_message_replace_tmp_key_message));
        mUpdateMessageLastThreadMessageBIZZ = mDb.compileStatement(mMainService
            .getString(R.string.sql_message_replace_tmp_key_last_thread_message));
        mUpdateMessageButtonKeyBIZZ = mDb.compileStatement(mMainService
            .getString(R.string.sql_message_replace_tmp_key_button));
        mUpdateMessageAttachmentKeyBIZZ = mDb.compileStatement(mMainService
            .getString(R.string.sql_message_replace_tmp_key_attachment));
        mUpdateMessageMemberKeyBIZZ = mDb.compileStatement(mMainService
            .getString(R.string.sql_message_replace_tmp_key_member));
        mSetMessageDirty = new MultiThreadedSQLStatement(mDb, new int[] { T.UI, T.BIZZ },
            mMainService.getString(R.string.sql_message_set_message_dirty));
        mSetMessageThreadReadUI = mDb.compileStatement(mMainService.getString(R.string.sql_message_set_thread_as_read));
        mSetMessageThreadVisibilityUI = mDb.compileStatement(mMainService
            .getString(R.string.sql_message_update_thread_show_in_list));
        mGetThreadShowInListBIZZ = mDb.compileStatement(mMainService
            .getString(R.string.sql_message_get_thread_show_in_list));
        mGetMessageButtonCountBIZZ = mDb.compileStatement(mMainService
            .getString(R.string.sql_message_get_message_button_count));
        mUpdateFormBIZZ = mDb.compileStatement(mMainService.getString(R.string.sql_message_update_form));
        mUpdateMyMemberStatusBIZZ = mDb.compileStatement(mMainService
            .getString(R.string.sql_message_update_my_member_status));
        mGetMessageNeedsAnswerUI = mDb.compileStatement(mMainService.getString(R.string.sql_message_get_needs_answer));
        mGetMessageNeedsAnswerBIZZ = mDb
            .compileStatement(mMainService.getString(R.string.sql_message_get_needs_answer));
        mUpdateFlagsBIZZ = mDb.compileStatement(mMainService.getString(R.string.sql_message_update_flags));
        mGetMessageFlagsUI = mDb.compileStatement(mMainService.getString(R.string.sql_message_get_flags));
        mGetMessageFlagsBIZZ = mDb.compileStatement(mMainService.getString(R.string.sql_message_get_flags));
        mUpdateSortidForThreadBySortidBIZZ = mDb.compileStatement(mMainService
            .getString(R.string.sql_message_update_sortid_for_thread));
        mGetHighestSortidBIZZ = mDb.compileStatement(mMainService.getString(R.string.sql_message_get_highest_sortid));
        mGetLastInboxOpenTime = mDb.compileStatement(mMainService
            .getString(R.string.sql_message_get_last_inbox_open_time));
        mSetLastInboxOpenTime = mDb.compileStatement(mMainService
            .getString(R.string.sql_message_set_last_inbox_open_time));
        mGetMessageExistenceUI = mDb.compileStatement(mMainService.getString(R.string.sql_message_get_existence));
        mSetMessageExistence = new MultiThreadedSQLStatement(mDb, new int[] { T.UI, T.BIZZ },
            mMainService.getString(R.string.sql_message_update_thread_existence));

        mInsertRequestedConversationBIZZ = mDb.compileStatement(mMainService
            .getString(R.string.sql_message_requested_conversation_insert));
        mDeleteRequestedConversationBIZZ = mDb.compileStatement(mMainService
            .getString(R.string.sql_message_requested_conversation_delete));
        mCountRequestedConversationBIZZ = mDb.compileStatement(mMainService
            .getString(R.string.sql_message_requested_conversation_count));

        mSaveMessageFlowRunHTTP = mDb.compileStatement(mMainService.getString(R.string.sql_mf_run_save));
        mDeleteMessageFlowRunHTTP = mDb.compileStatement(mMainService.getString(R.string.sql_mf_run_delete));

        mRecalculateMessagesShowInList = mDb.compileStatement(mMainService
            .getString(R.string.sql_message_recalculate_show_in_list));

        mAddMessageAttachments = mDb.compileStatement(mMainService.getString(R.string.sql_message_insert_attachment));

        mGetDirtyThreadsCount = mDb.compileStatement(mMainService
            .getString(R.string.sql_message_get_thread_dirty_count));

        mThreadAvatarCountByHash = mDb.compileStatement(mMainService.getString(R.string.sql_thread_avatar_count));
        mStoreThreadAvatar = mDb.compileStatement(mMainService.getString(R.string.sql_thread_avatar_insert));
        mGetMessageMessage = mDb.compileStatement(mMainService.getString(R.string.sql_message_message_get));
        mGetUnreadMessageCountInThread = mDb.compileStatement(mMainService.getString(R.string.sql_get_unread_count_in_thread));
        mGetTotalDirtyMessagecount = mDb.compileStatement(mMainService.getString(R.string.sql_get_total_unread_count));
    }

    public CursorSet getMessagesCursor(String memberFilter) {
        T.UI();
        final CursorSet set = new CursorSet();

        if (memberFilter == null) {
            set.cursor = mDb.rawQuery(mMainService.getString(R.string.sql_message_cursor_query), null);
            set.query = R.string.sql_message_cursor_query;
            set.indexer = mDb.rawQuery(mMainService.getString(R.string.sql_message_cursor_query_indexor), null);
        } else {
            set.cursor = mDb.rawQuery(mMainService.getString(R.string.sql_message_cursor_query_filter_member),
                new String[] { memberFilter, memberFilter, memberFilter });
            set.query = R.string.sql_message_cursor_query_filter_member;
            set.indexer = mDb.rawQuery(mMainService.getString(R.string.sql_message_cursor_query_filter_member_indexor),
                new String[] { memberFilter, memberFilter, memberFilter });
        }
        return set;
    }

    public Cursor getFullThreadCursor(String parentKey) {
        return mDb.rawQuery(mMainService.getString(R.string.sql_message_cursor_full_thread), new String[] { parentKey,
            parentKey });
    }

    public Cursor getFullServiceThreadCursor(String parentKey) {
        return mDb.rawQuery(mMainService.getString(R.string.sql_message_cursor_full_service_thread), new String[] {
            parentKey, parentKey });
    }

    public MessageBreadCrumbs getMessageBreadCrumbs(String key) {
        T.dontCare();
        MessageBreadCrumbs mbc = new MessageBreadCrumbs();
        final Cursor bc = mDb.rawQuery(mMainService.getString(R.string.sql_message_get_bread_crumbs),
            new String[] { key });
        try {
            if (!bc.moveToFirst())
                return mbc;
            boolean before = true;
            while (true) {
                if (key.equals(bc.getString(0))) {
                    before = false;
                } else {
                    MessageBreadCrumb mb = new MessageBreadCrumb();
                    mb.key = bc.getString(0);
                    mb.parentKey = bc.getString(1);
                    mb.sender = bc.getString(2);
                    mb.message = bc.getString(3);
                    mb.timestamp = bc.getLong(4);
                    if (before)
                        mbc.previous.add(mb);
                    else
                        mbc.next.add(mb);
                }
                if (!bc.moveToNext())
                    break;
            }
        } finally {
            bc.close();
        }
        return mbc;
    }

    public int[] getAlertFlags(Long since) {
        T.dontCare();
        final Cursor bc = mDb.rawQuery(mMainService.getString(R.string.sql_message_get_alert_flags_of_open_messages),
            new String[] { since.toString() });
        try {
            if (!bc.moveToFirst())
                return new int[0];
            int[] result = new int[bc.getCount()];
            for (int i = 0; i < result.length; i++) {
                result[i] = bc.getInt(0);
                if (!bc.moveToNext())
                    break;
            }
            return result;
        } finally {
            bc.close();
        }
    }

    @SuppressWarnings("unchecked")
    public Message getCurrentMessage(Cursor cursor, final int query) {
        T.dontCare();
        final Message message = new Message();
        // Don't forget to update MessagingActivity.onOptionsItemSelected if the order of key and parent_key is changed
        message.key = getKeyFromMessageCursor(cursor);
        message.parent_key = getParentKeyFromMessageCursor(cursor);
        message.sender = cursor.getString(2);
        message.message = cursor.getString(3);
        message.timestamp = cursor.getLong(4);
        message.dirty = cursor.getLong(5) == 1;
        message.recipients = cursor.getString(6);
        message.flags = getFlagsFromMessageCursor(cursor);
        message.needsMyAnswer = cursor.getLong(8) != 0;
        message.recipients_status = cursor.getLong(9);
        message.alert_flags = cursor.getLong(10);
        message.threadDirty = cursor.getLong(14) != 0;
        message.lastThreadMessage = cursor.getString(15);
        message.replyCount = cursor.getLong(16);
        switch (query) {
        case R.string.sql_message_get_thread_message_by_key:
            break;
        case R.string.sql_message_cursor_full_service_thread:
            String formString = cursor.getString(17);
            if (formString != null)
                message.form = (Map<String, Object>) JSONValue.parse(formString);
            message.dismiss_button_ui_flags = cursor.getLong(18);
            message.threadNeedsMyAnswer = cursor.getLong(19) != 0;
            message.priority = cursor.getLong(20);
            message.default_priority = cursor.getLong(21);
            message.default_sticky = cursor.getLong(22) != 0;
            break;
        case R.string.sql_message_cursor_query:
        case R.string.sql_message_cursor_query_filter_member:
            message.threadNeedsMyAnswer = cursor.getLong(17) != 0;
            message.threadShowInList = cursor.getLong(18) != 0;
            message.thread_avatar_hash = cursor.getString(19);
            message.thread_background_color = cursor.getString(20);
            message.thread_text_color = cursor.getString(21);
            message.priority = cursor.getLong(22);
            message.default_priority = cursor.getLong(23);
            message.default_sticky = cursor.getLong(24) != 0;
            message.unreadCount = cursor.getLong(25);
            break;
        }
        addMembers(message);
        if (TextUtils.isEmptyOrWhitespace(message.message)) {
            addButtonsToMessageObject(message);
            message.attachments = getAttachmentsFromMessage(message.key);
        }
        return message;
    }

    public boolean storeNewMessage(final MessageTO message) {
        T.BIZZ();
        final long tzdiff = TimeUtils.getGMTOffsetMillis() / 1000L;

        final String me = myEmail();
        final boolean senderIsMobileOwner = message.sender.equals(me);

        boolean messageLocked = (message.flags & MessagingPlugin.FLAG_LOCKED) == MessagingPlugin.FLAG_LOCKED;
        long myStatus = 0;
        for (MemberStatusTO ms : message.members) {
            if (ms.member.equals(me)) {
                myStatus = ms.status;
                break;
            }
        }
        final boolean needsMyAnswer = !senderIsMobileOwner && !messageLocked
            && (myStatus & MessagingPlugin.STATUS_ACKED) == 0;
        final boolean dirty = !senderIsMobileOwner && (myStatus & MessagingPlugin.STATUS_READ) == 0
            && (myStatus & MessagingPlugin.STATUS_ACKED) == 0;

        final boolean threadForceVisible = message.parent_key == null ? TextUtils.isEmptyOrWhitespace(message.context)
            : mustShowThreadInList(message.parent_key);

        TransactionHelper.runInTransaction(mDb, "storeNewMessage", new TransactionWithoutResult() {
            @Override
            protected void run() {
                L.d("Storing new message with key " + message.key + " and parent key " + message.parent_key);
                final boolean fetchThreadAvatar;

                long sortid;

                mInsertMessageBIZZ.bindString(1, message.key);
                if (message.parent_key != null) {
                    // child message
                    mInsertMessageBIZZ.bindString(2, message.parent_key);
                    sortid = getSortId(message.parent_key);

                    if (senderIsMobileOwner) {
                        mGetMaxTimestampBySortIDBIZZ.bindLong(1, sortid);
                        if (mGetMaxTimestampBySortIDBIZZ.simpleQueryForLong() >= message.timestamp)
                            message.timestamp++;
                    }
                } else {
                    // new message
                    mInsertMessageBIZZ.bindNull(2);
                    sortid = UNKNOWN_SORT_ID;
                }
                mInsertMessageBIZZ.bindLong(10, sortid);
                mInsertMessageBIZZ.bindString(3, message.sender);
                mInsertMessageBIZZ.bindString(4, (message.message == null ? "" : message.message));
                mInsertMessageBIZZ.bindLong(5, message.timeout);
                mInsertMessageBIZZ.bindLong(6, message.timestamp);
                mInsertMessageBIZZ.bindLong(7, message.flags);
                mInsertMessageBIZZ.bindLong(8, needsMyAnswer ? 1 : 0);
                if (!TextUtils.isEmptyOrWhitespace(message.branding))
                    mInsertMessageBIZZ.bindString(9, message.branding);
                else
                    mInsertMessageBIZZ.bindNull(9);
                mInsertMessageBIZZ.bindLong(11, dirty ? 1 : 0);

                StringBuilder sb = new StringBuilder();
                boolean needscomma = false;
                if (FriendsPlugin.SYSTEM_FRIEND.equals(message.sender) ||
                        mFriendsPlugin.getStore().getFriendType(message.sender) == FriendsPlugin.FRIEND_TYPE_SERVICE) {
                    needscomma = true;
                    sb.append(mFriendsPlugin.getName(message.sender));
                }

                for (MemberStatusTO member : message.members) {
                    MemberStatusTO memberStatus = member;
                    if (memberStatus.member.equals(me)) {
                        memberStatus.status |= MessagingPlugin.STATUS_RECEIVED;
                    }
                    if (memberStatus.member.equals(me))
                        continue;
                    if (needscomma)
                        sb.append(", ");
                    else
                        needscomma = true;
                    sb.append(mFriendsPlugin.getName(memberStatus.member));
                }
                mInsertMessageBIZZ.bindString(12, sb.toString());

                mInsertMessageBIZZ.bindLong(13, calculateMemberStatusSummary(message));
                mInsertMessageBIZZ.bindLong(14, message.alert_flags);
                mInsertMessageBIZZ.bindLong(15, (message.timestamp + tzdiff) / 86400);

                Message msg = (message instanceof Message) ? (Message) message : null;
                if (msg != null && msg.form != null) {
                    String formJsonString = JSONValue.toJSONString(msg.form);
                    mInsertMessageBIZZ.bindString(16, formJsonString);
                } else
                    mInsertMessageBIZZ.bindNull(16);

                mInsertMessageBIZZ.bindLong(17, message.dismiss_button_ui_flags);
                mInsertMessageBIZZ.bindString(18, message.key);
                mInsertMessageBIZZ.bindLong(19, threadForceVisible ? 1 : 0);
                bindString(mInsertMessageBIZZ, 20, message.broadcast_type);
                bindString(mInsertMessageBIZZ, 21, message.thread_avatar_hash);
                fetchThreadAvatar = message.parent_key == null && message.thread_avatar_hash != null
                    && !threadAvatarExists(message.thread_avatar_hash);
                bindString(mInsertMessageBIZZ, 22, message.thread_background_color);
                bindString(mInsertMessageBIZZ, 23, message.thread_text_color);
                mInsertMessageBIZZ.bindLong(24, message.priority);
                mInsertMessageBIZZ.bindLong(25, message.default_priority);
                mInsertMessageBIZZ.bindLong(26, message.default_sticky ? 1 : 0);

                mInsertMessageBIZZ.execute();

                long highestSortid;
                try {
                    highestSortid = mGetHighestSortidBIZZ.simpleQueryForLong() + 1;
                } catch (SQLiteDoneException e) {
                    highestSortid = 1;
                }

                updateSortId(sortid, highestSortid);

                // Add buttons
                for (int i = 0; i < message.buttons.length; i++) {
                    ButtonTO button = message.buttons[i];
                    mAddButtonBIZZ.bindString(1, message.key);
                    mAddButtonBIZZ.bindString(2, button.id);
                    mAddButtonBIZZ.bindString(3, button.caption);
                    if (button.action == null)
                        mAddButtonBIZZ.bindNull(4);
                    else
                        mAddButtonBIZZ.bindString(4, button.action);
                    mAddButtonBIZZ.bindLong(5, i);
                    mAddButtonBIZZ.bindLong(6, button.ui_flags);
                    mAddButtonBIZZ.execute();
                }
                // Add member statuses
                for (MemberStatusTO memberStatus : message.members) {
                    mAddMemberStatusBIZZ.bindString(1, message.key);
                    mAddMemberStatusBIZZ.bindString(2, memberStatus.member);
                    long ackedTimeStamp = memberStatus.acked_timestamp;
                    if (me.equals(memberStatus.member)) {
                        mAddMemberStatusBIZZ.bindLong(3, mMainService.currentTimeMillis() / 1000);
                        mAddMemberStatusBIZZ.bindLong(6, memberStatus.status | MessagingPlugin.STATUS_RECEIVED);
                    } else {
                        mAddMemberStatusBIZZ.bindLong(3, memberStatus.received_timestamp);
                        long status = memberStatus.status;
                        if (message.sender.equals(memberStatus.member)) {
                            status |= MessagingPlugin.STATUS_ACKED | MessagingPlugin.STATUS_READ | MessagingPlugin.STATUS_RECEIVED;
                            ackedTimeStamp = message.timestamp;
                        }
                        mAddMemberStatusBIZZ.bindLong(6, status);
                    }
                    mAddMemberStatusBIZZ.bindLong(4, ackedTimeStamp);
                    if (memberStatus.button_id != null)
                        mAddMemberStatusBIZZ.bindString(5, memberStatus.button_id);
                    else
                        mAddMemberStatusBIZZ.bindNull(5);
                    mAddMemberStatusBIZZ.execute();
                }
                if (message.attachments != null) {
                    insertAttachments(message.attachments, message.key);
                }

                if (fetchThreadAvatar) {
                    TransactionHelper.onTransactionCommitted(new SafeRunnable() {
                        @Override
                        protected void safeRun() throws Exception {
                            downloadThreadAvatar(message.thread_avatar_hash, message.key);
                        }
                    });
                }
            }
        });

        return senderIsMobileOwner;
    }

    public void insertMemberStatusBIZZ(final String parentKey, final String messageKey,
        final MemberStatusTO memberStatus) {
        final String me = myEmail();

        TransactionHelper.runInTransaction(mDb, "insertMemberStatus", new TransactionWithoutResult() {
            @Override
            protected void run() {
                mAddMemberStatusBIZZ.bindString(1, messageKey);
                mAddMemberStatusBIZZ.bindString(2, memberStatus.member);
                if (me.equals(memberStatus.member)) {
                    mAddMemberStatusBIZZ.bindLong(3, mMainService.currentTimeMillis() / 1000);
                    mAddMemberStatusBIZZ.bindLong(6, memberStatus.status | MessagingPlugin.STATUS_RECEIVED);
                } else {
                    mAddMemberStatusBIZZ.bindLong(3, memberStatus.received_timestamp);
                    mAddMemberStatusBIZZ.bindLong(6, memberStatus.status);
                }
                mAddMemberStatusBIZZ.bindLong(4, memberStatus.acked_timestamp);
                if (memberStatus.button_id != null)
                    mAddMemberStatusBIZZ.bindString(5, memberStatus.button_id);
                else
                    mAddMemberStatusBIZZ.bindNull(5);
                mAddMemberStatusBIZZ.execute();
            }
        });
    }

    private void downloadThreadAvatar(String avatarHash, String threadKey) {
        GetConversationAvatarRequestTO request = new GetConversationAvatarRequestTO();
        request.avatar_hash = avatarHash;
        request.thread_key = threadKey;
        GetConversationAvatarResponseHandler responseHandler = new GetConversationAvatarResponseHandler();
        responseHandler.setAvatarHash(avatarHash);
        try {
            com.mobicage.api.messaging.Rpc.getConversationAvatar(responseHandler, request);
        } catch (Exception e) {
            L.bug("Could not request conversation avatar");
        }
    }

    public void storeThreadAvatar(String avatarHash, String avatar) {
        T.BIZZ();
        mStoreThreadAvatar.bindString(1, avatarHash);
        mStoreThreadAvatar.bindBlob(2, Base64.decode(avatar));
        mStoreThreadAvatar.execute();
    }

    public Bitmap getThreadAvatar(String avatarHash) {
        T.UI();
        if (avatarHash == null)
            return null;
        final Cursor curs = mDb.rawQuery(mMainService.getString(R.string.sql_thread_avatar_get),
            new String[] { avatarHash });
        try {
            if (!curs.moveToFirst()) {
                return null;
            }
            byte[] avatar = curs.getBlob(0);
            return ImageHelper.getRoundedCornerAvatar(BitmapFactory.decodeByteArray(avatar, 0, avatar.length));
        } finally {
            curs.close();
        }
    }

    private boolean threadAvatarExists(String avatarHash) {
        T.BIZZ();
        mThreadAvatarCountByHash.bindString(1, avatarHash);
        return mThreadAvatarCountByHash.simpleQueryForLong() > 0;
    }

    public String getMessageMessage(String messageKey) {
        T.UI();
        mGetMessageMessage.bindString(1, messageKey);
        return mGetMessageMessage.simpleQueryForString();
    }

    public Cursor getThreadListCursor(String memberFilter) {
        if (memberFilter == null) {
            return mDb.rawQuery(mMainService.getString(R.string.sql_message_thread_list), null);
        } else {
            return mDb.rawQuery(mMainService.getString(R.string.sql_message_thread_list_filter_member), new String[] {
                memberFilter, memberFilter });
        }
    }

    public static String getKeyFromMessageCursor(Cursor cursor) {
        return cursor.getString(0);
    }

    public static String getParentKeyFromMessageCursor(Cursor cursor) {
        return cursor.getString(1);
    }

    public static long getFlagsFromMessageCursor(Cursor cursor) {
        return cursor.getLong(7);
    }

    public List<String> listChildMessagesInThread(String parentMessageKey) {
        final Cursor c = mDb.rawQuery(mMainService.getString(R.string.sql_message_select_children),
            new String[] { parentMessageKey });

        final List<String> results = new ArrayList<String>();
        try {
            if (c.moveToFirst()) {
                do {
                    results.add(c.getString(0));
                } while (c.moveToNext());
            }

            return results;
        } finally {
            c.close();
        }
    }

    private long getSortId(String messageKey) {
        mGetParentSortIDBIZZ.bindString(1, messageKey);
        return mGetParentSortIDBIZZ.simpleQueryForLong();
    }

    private void updateSortId(long oldSortId, long newSortId) {
        mUpdateSortidForThreadBySortidBIZZ.bindLong(1, newSortId);
        mUpdateSortidForThreadBySortidBIZZ.bindLong(2, oldSortId);
        mUpdateSortidForThreadBySortidBIZZ.bindLong(3, oldSortId);
        mUpdateSortidForThreadBySortidBIZZ.execute();
    }

    public boolean updateMessage(final String messageKey, final String parentMessageKey, final Long flags,
        final Long existence, final String message, final String threadAvatarHash, final String threadBackgroundColor,
        final String threadTextolor) throws MessageUpdateNotAllowedException {

        final boolean updateWholeThread = messageKey == null;

        if (updateWholeThread && message != null) {
            throw new MessageUpdateNotAllowedException("Updating the message of every thread message is not allowed!");
        }

        return TransactionHelper.runInTransaction(mDb, "updateMessage", new Transaction<Boolean>() {

            @Override
            protected Boolean run() {
                ContentValues values = new ContentValues();
                if (flags != null) {
                    values.put("flags", flags);
                }
                if (existence != null) {
                    values.put("existence", existence);
                }
                if (message != null) {
                    values.put("message", message);
                }
                boolean fetchThreadAvatar = false;
                if (threadAvatarHash != null) {
                    String value = threadAvatarHash.length() == 0 ? null : threadAvatarHash;
                    values.put("thread_avatar_hash", value);
                    if (value != null) {
                        fetchThreadAvatar = !threadAvatarExists(threadAvatarHash);
                    }
                }
                if (threadBackgroundColor != null) {
                    values.put("thread_background_color", threadBackgroundColor.length() == 0 ? null
                        : threadBackgroundColor);
                }
                if (threadTextolor != null) {
                    values.put("thread_text_color", threadTextolor.length() == 0 ? null : threadTextolor);
                }

                final String whereClause;
                final String[] whereArgs;
                if (updateWholeThread) {
                    whereClause = "key = ? OR parent_key = ?";
                    whereArgs = new String[] { parentMessageKey, parentMessageKey };
                } else {
                    whereClause = "key = ?";
                    whereArgs = new String[] { messageKey };
                }

                if (CloudConstants.DEBUG_LOGGING) {
                    L.d("UPDATE message SET " + values + " WHERE "
                        + String.format(whereClause.replace("?", "'%s'"), (Object[]) whereArgs));
                }
                int rowsUpdated = mDb.update("message", values, whereClause, whereArgs);

                if (fetchThreadAvatar) {
                    downloadThreadAvatar(threadAvatarHash, parentMessageKey);
                }
                if (messageKey != null && existence != null && existence == 0) {
                    long sortId = getSortId(messageKey);
                    updateSortId(sortId, sortId);
                }

                return rowsUpdated > 0;
            }
        });
    }

    public boolean updateMessageMemberStatus(final MemberStatusUpdateRequestTO request) {
        T.BIZZ();
        final String myEmail = myEmail();
        final boolean senderIsMobileOwner = request.member.equals(myEmail);

        TransactionHelper.runInTransaction(mDb, "updateMessageMemberStatus", new TransactionWithoutResult() {

            @Override
            protected void run() {
                updateMessageMemberStatusInDbNotInTransaction(request);

                if (!senderIsMobileOwner && request.button_id != null) {
                    // someone else pressed magic button
                    setDirty(request.message, true);
                } else if (senderIsMobileOwner
                    && (request.status & MessagingPlugin.STATUS_READ) == MessagingPlugin.STATUS_READ) {
                    // message read by me
                    setDirty(request.message, false);
                }

                updateMessageMemberSummary(request.message);
            }
        });
        return senderIsMobileOwner;
    }

    public Collection<MessageMemberStatus> getLeastMemberStatusses(String parentMessageKey) {
        T.UI();
        Map<String, MessageMemberStatus> result = new HashMap<String, MessageMemberStatus>();
        final Cursor c = mDb.rawQuery(mMainService.getString(R.string.sql_message_get_least_member_statusses),
            new String[] { parentMessageKey, parentMessageKey });
        try {
            if (!c.moveToFirst())
                return result.values();
            do {
                String messageKey = c.getString(0);
                long receivedTimeStamp = c.getLong(1);
                long ackedTimestamp = c.getLong(2);
                String sender = c.getString(3);
                String member = c.getString(4);
                long status = c.getLong(5);
                long flags = c.getLong(6);
                if (sender.equals(member) || (flags & MessagingPlugin.FLAG_LOCKED) == MessagingPlugin.FLAG_LOCKED)
                    status |= MessagingPlugin.STATUS_ACKED;

                MessageMemberStatus mst = result.get(member);
                if (mst == null) {
                    mst = new MessageMemberStatus();
                    mst.acked_timestamp = ackedTimestamp;
                    mst.received_timestamp = receivedTimeStamp;
                    mst.member = member;
                    mst.status = status;
                    mst.messageKey = messageKey;
                    result.put(member, mst);
                } else {
                    if (status < mst.status)
                        mst.status = status;
                }
            } while (c.moveToNext());
            return result.values();
        } finally {
            c.close();
        }
    }

    public long getToAckPosition(String parentMessageKey, String member) {
        long result = -1;
        final Cursor c = mDb.rawQuery(mMainService.getString(R.string.sql_message_cursor_full_thread_my_member_status),
            new String[] { parentMessageKey, member, parentMessageKey, member });
        try {
            if (!c.moveToFirst())
                return result;
            do {
                String csender = c.getString(0);
                String cmember = c.isNull(1) ? null : c.getString(1);
                long status = c.getLong(2);
                if (csender.equals(member))
                    continue;
                if (cmember == null)
                    continue;
                if ((status & MessagingPlugin.STATUS_ACKED) != MessagingPlugin.STATUS_ACKED)
                    return c.getPosition();
            } while (c.moveToNext());
            return result;
        } finally {
            c.close();
        }
    }

    public String getSingleToBeAckedThreadMessage(String parentMessageKey, String member) {
        final Cursor c = mDb.rawQuery(
            mMainService.getString(R.string.sql_message_cursor_full_thread_to_be_acked_messages), new String[] {
                parentMessageKey, member, parentMessageKey, member });
        try {
            if (!c.moveToFirst())
                return null;
            if (c.getCount() == 1)
                return c.getString(0);
            return null;
        } finally {
            c.close();
        }
    }

    public long getCount() {
        T.UI();
        return mGetCountUI.simpleQueryForLong();
    }

    public void setDirty(final String key, final boolean dirty) {
        T.dontCare();
        final long timestamp = mMainService.currentTimeMillis() / 1000;
        L.d("Setting message " + key + " dirty=" + dirty + " on " + timestamp);

        TransactionHelper.runInTransaction(mDb, "setDirty", new TransactionWithoutResult() {

            @Override
            protected void run() {
                final SQLiteStatement stmt = mSetMessageDirty.getStatementForThisThread();
                stmt.bindLong(1, dirty ? 1 : 0);
                stmt.bindLong(2, timestamp);
                stmt.bindString(3, key);
                stmt.execute();
            }
        });
    }

    public void setMessageThreadRead(final String key) {
        T.UI();
        TransactionHelper.runInTransaction(mDb, "setMessageThreadRead", new TransactionWithoutResult() {

            @Override
            protected void run() {
                mSetMessageThreadReadUI.bindString(1, key);
                mSetMessageThreadReadUI.bindString(2, key);
                mSetMessageThreadReadUI.execute();
            }
        });
    }

    public void setMessageThreadVisibility(final String threadKey, final boolean visible) {
        T.UI();
        mSetMessageThreadVisibilityUI.bindLong(1, visible ? 1 : 0);
        mSetMessageThreadVisibilityUI.bindString(2, threadKey);
        mSetMessageThreadVisibilityUI.bindString(3, threadKey);
        mSetMessageThreadVisibilityUI.execute();
    }

    public boolean mustShowThreadInList(final String key) {
        T.BIZZ();
        mGetThreadShowInListBIZZ.bindString(1, key);
        return mGetThreadShowInListBIZZ.simpleQueryForLong() == 1;
    }

    public List<String> getDirtyMessageKeys(long timestamp) {
        T.UI();
        L.d("Getting dirty count after timestamp " + timestamp);
        final Cursor c = mDb.rawQuery(mMainService.getString(R.string.sql_message_get_dirty_keys), new String[] { ""
            + timestamp });

        final List<String> results = new ArrayList<String>();
        try {
            if (c.moveToFirst()) {
                do {
                    results.add(c.getString(0));
                } while (c.moveToNext());
            }

            return results;
        } finally {
            c.close();
        }
    }

    public void setInboxOpenedAt(long timestamp) {
        T.UI();
        L.d("Setting last inbox opened timestamp to " + timestamp);
        mSetLastInboxOpenTime.bindLong(1, timestamp);
        mSetLastInboxOpenTime.execute();
    }

    public long getLastInboxOpenedTimestamp() {
        T.dontCare();
        return mGetLastInboxOpenTime.simpleQueryForLong();
    }

    public Message getFullMessageByUnprocessedMessageIndex(long index) {
        T.dontCare();
        final Cursor curs = mDb.rawQuery(
            mMainService.getString(R.string.sql_message_get_message_by_unprocessed_message_index),
            new String[] { Long.toString(index) });
        try {
            if (!curs.moveToFirst()) {
                return null;
            }
            Message fullMessage = toFullMessage(curs, R.string.sql_message_get_message_by_unprocessed_message_index);
            addMembers(fullMessage);
            return fullMessage;
        } finally {
            curs.close();
        }
    }

    // Get message with buttons, but without member statuses
    public Message getPartialMessageByKey(String key) {
        T.dontCare();
        final Cursor curs = mDb.rawQuery(mMainService.getString(R.string.sql_message_get_message_by_key),
            new String[] { key });
        try {
            if (!curs.moveToFirst()) {
                L.d("Cannot find message with key " + key);
                return null;
            }
            final Message message = toFullMessage(curs, R.string.sql_message_get_message_by_key);
            return message;
        } finally {
            curs.close();
        }
    }

    // Get message with buttons and member statuses
    public Message getFullMessageByKey(String key) {
        T.dontCare();
        final Cursor curs = mDb.rawQuery(mMainService.getString(R.string.sql_message_get_message_by_key),
            new String[] { key });
        try {
            if (!curs.moveToFirst()) {
                L.bug("Cannot find message with key " + key);
                return null;
            }
            Message fullMessage = toFullMessage(curs, R.string.sql_message_get_message_by_key);
            addMembers(fullMessage);
            return fullMessage;
        } finally {
            curs.close();
        }
    }

    public Message getMessageByKey(String key) {
        return getMessageByKey(key, false);
    }


    public Message getMessageByKey(String key, boolean ignoreNotFound) {
        T.dontCare();
        final Cursor curs = mDb.rawQuery(mMainService.getString(R.string.sql_message_get_thread_message_by_key),
            new String[] { key });
        try {
            if (!curs.moveToFirst()) {
                if (!ignoreNotFound) {
                    L.bug("Cannot find message with key " + key);
                }
                return null;
            }
            return getCurrentMessage(curs, R.string.sql_message_get_thread_message_by_key);
        } finally {
            curs.close();
        }
    }

    public Message readFullMessageFromCursor(Cursor cursor) {
        T.UI();
        Message fullMessage = toFullMessage(cursor, R.string.sql_message_cursor_full_thread);
        addMembers(fullMessage);
        return fullMessage;
    }

    public Cursor getMessagesThatNeedAnswer(String threadKey) {
        return mDb.rawQuery(mMainService.getString(R.string.sql_message_cursor_need_my_answer_message_from_thread),
            new String[] { threadKey, threadKey });
    }

    public Message readFullThreadMessageFromCursor(Cursor cursor) {
        Message message = getCurrentMessage(cursor, R.string.sql_message_cursor_full_service_thread);
        addMembers(message);
        addButtonsToMessageObject(message);
        return message;
    }

    public String getMessageSenderBIZZ(String key) {
        T.BIZZ();
        mGetMessageSenderBIZZ.bindString(1, key);
        return mGetMessageSenderBIZZ.simpleQueryForString();
    }

    public long getMessageButtonCount(String key) {
        T.BIZZ();
        mGetMessageButtonCountBIZZ.bindString(1, key);
        return mGetMessageButtonCountBIZZ.simpleQueryForLong();
    }

    public Set<String> getMessageMembers(String key) {
        T.BIZZ();
        final Cursor bcurs = mDb.rawQuery(mMainService.getString(R.string.sql_message_get_message_members),
            new String[] { key });
        try {
            Set<String> members = new HashSet<String>();
            members.add(getMessageSenderBIZZ(key));
            if (!bcurs.moveToFirst()) {
                return members;
            }
            members.add(bcurs.getString(0));
            while (bcurs.moveToNext())
                members.add(bcurs.getString(0));
            return members;
        } finally {
            bcurs.close();
        }
    }

    public List<String> getUnprocessedMessageKeysAfterTimestamp(long timestamp) {
        T.UI();
        L.d("Getting unprocessed message count after " + timestamp);
        final Cursor c = mDb.rawQuery(
            mMainService.getString(R.string.sql_message_get_unprocessed_message_after_timestamp_keys),
            new String[] { "" + timestamp });

        final List<String> results = new ArrayList<String>();
        try {
            if (c.moveToFirst()) {
                do {
                    results.add(c.getString(0));
                } while (c.moveToNext());
            }

            return results;
        } finally {
            c.close();
        }
    }

    public void setMessageProcessed(final String message, final String buttonId, final String customReply,
        final SafeRunnable updateDoneHandler) {
        T.dontCare();
        mMainService.postOnBIZZHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                setMessageProcessedBizz(message, buttonId, customReply, updateDoneHandler);
            }
        });
    }

    // XXX: use MultiThreadedSQLStatement
    public boolean messageNeedsAnswerUI(final String messageKey) {
        T.UI();
        mGetMessageNeedsAnswerUI.bindString(1, messageKey);
        return mGetMessageNeedsAnswerUI.simpleQueryForLong() != 0;
    }

    // XXX: use MultiThreadedSQLStatement
    public boolean messageNeedsAnswerBIZZ(final String messageKey) {
        T.BIZZ();
        mGetMessageNeedsAnswerBIZZ.bindString(1, messageKey);
        return mGetMessageNeedsAnswerBIZZ.simpleQueryForLong() != 0;
    }

    public void lockMessage(final String messageKey, final MemberStatusTO[] memberStatuses, final long dirtyBehavior,
        final SafeRunnable lockDoneRunnable) {
        T.dontCare();
        mMainService.postOnBIZZHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.BIZZ();
                TransactionHelper.runInTransaction(mDb, "lockMessage", new TransactionWithoutResult() {

                    @Override
                    protected void run() {
                        final boolean messageNeedsAnswerBeforeLocking = messageNeedsAnswerBIZZ(messageKey);

                        mUpdateFlagsBIZZ.bindLong(1, MessagingPlugin.FLAG_LOCKED);
                        mUpdateFlagsBIZZ.bindString(2, messageKey);
                        mUpdateFlagsBIZZ.execute();
                        setMessageProcessed(messageKey);

                        final Message message = getFullMessageByKey(messageKey);

                        if (dirtyBehavior == MessagingPlugin.DIRTY_BEHAVIOR_MAKE_DIRTY) {
                            setDirty(messageKey, true);
                        } else if (dirtyBehavior == MessagingPlugin.DIRTY_BEHAVIOR_CLEAR_DIRTY) {
                            setDirty(messageKey, false);
                        } else if (message.sender.equals(myEmail())) {
                            // I lock a message hence set dirty = false
                            setDirty(messageKey, false);
                        } else {

                            if ((message.flags & MessagingPlugin.FLAG_AUTO_LOCK) == MessagingPlugin.FLAG_AUTO_LOCK)
                                setDirty(messageKey, false);
                            else {
                                if (messageNeedsAnswerBeforeLocking)
                                    setDirty(messageKey, true);
                                else
                                    setDirty(messageKey, false);
                            }
                        }

                        for (MemberStatusTO ms : memberStatuses) {
                            MemberStatusUpdateRequestTO msu = new MemberStatusUpdateRequestTO();
                            msu.acked_timestamp = ms.acked_timestamp;
                            msu.button_id = ms.button_id;
                            msu.custom_reply = ms.custom_reply;
                            msu.member = ms.member;
                            msu.message = messageKey;
                            msu.received_timestamp = ms.received_timestamp;
                            msu.status = ms.status;
                            msu.flags = message.flags;
                            updateMessageMemberStatusInDbNotInTransaction(msu);
                        }

                        if (lockDoneRunnable != null)
                            TransactionHelper.onTransactionCommitted(lockDoneRunnable);
                    }
                });
            }
        });
    }

    @Override
    public void close() throws IOException {
        T.UI();
        mSetMessageDirty.close();

        mAddMemberStatusBIZZ.close();
        mGetCountUI.close();
        mSetMessageProcessedBIZZ.close();
        mInsertMessageBIZZ.close();
        mAddButtonBIZZ.close();
        mUpdateMemberStatusBIZZ.close();
        mGetParentSortIDBIZZ.close();
        mGetMaxTimestampBySortIDBIZZ.close();
        mGetMessageSenderBIZZ.close();
        mUpdateMessageKeyAndTimestampBIZZ.close();
        mUpdateMessageButtonKeyBIZZ.close();
        mUpdateMessageAttachmentKeyBIZZ.close();
        mUpdateMessageMemberKeyBIZZ.close();
        mGetMessageButtonCountBIZZ.close();
        mUpdateFormBIZZ.close();
        mUpdateMyMemberStatusBIZZ.close();
        mGetMessageNeedsAnswerUI.close();
        mGetMessageNeedsAnswerBIZZ.close();
        mUpdateFlagsBIZZ.close();
        mGetMessageFlagsUI.close();
        mGetMessageFlagsBIZZ.close();
        mUpdateSortidForThreadBySortidBIZZ.close();
        mGetHighestSortidBIZZ.close();
        mUpdateMessageLastThreadMessageBIZZ.close();

        mSetMessageThreadReadUI.close();
        mSetMessageThreadVisibilityUI.close();
        mGetThreadShowInListBIZZ.close();

        mGetLastInboxOpenTime.close();
        mSetLastInboxOpenTime.close();

        mGetMessageExistenceUI.close();
        mSetMessageExistence.close();

        mInsertRequestedConversationBIZZ.close();
        mDeleteRequestedConversationBIZZ.close();
        mCountRequestedConversationBIZZ.close();

        mSaveMessageFlowRunHTTP.close();
        mDeleteMessageFlowRunHTTP.close();

        mRecalculateMessagesShowInList.close();

        mAddMessageAttachments.close();

        mGetDirtyThreadsCount.close();

        mThreadAvatarCountByHash.close();
        mStoreThreadAvatar.close();
        mGetMessageMessage.close();
    }

    private void updateMessageMemberStatusInDbNotInTransaction(MemberStatusUpdateRequestTO request) {
        T.BIZZ();
        updateMessageMemberStatusInDbNotInTransaction(request.message, request.member, request.status,
            request.button_id, request.custom_reply, request.received_timestamp, request.acked_timestamp);
    }

    private void updateMessageMemberStatusInDbNotInTransaction(String message, String member, long status,
        String buttonId, String customReply, long receivedTimestamp, long ackedTimestamp) {
        mUpdateMemberStatusBIZZ.bindLong(1, receivedTimestamp);
        mUpdateMemberStatusBIZZ.bindLong(2, receivedTimestamp);
        mUpdateMemberStatusBIZZ.bindLong(3, receivedTimestamp);
        mUpdateMemberStatusBIZZ.bindLong(4, receivedTimestamp);
        mUpdateMemberStatusBIZZ.bindLong(5, ackedTimestamp);
        if (buttonId != null)
            mUpdateMemberStatusBIZZ.bindString(6, buttonId);
        else
            mUpdateMemberStatusBIZZ.bindNull(6);

        if (customReply != null)
            mUpdateMemberStatusBIZZ.bindString(7, customReply);
        else
            mUpdateMemberStatusBIZZ.bindNull(7);
        mUpdateMemberStatusBIZZ.bindLong(8, status);
        mUpdateMemberStatusBIZZ.bindString(9, message);
        mUpdateMemberStatusBIZZ.bindString(10, member);
        mUpdateMemberStatusBIZZ.execute();
    }

    // Must be run inside transaction !
    private void updateMessageMemberSummary(final String messageKey) {
        final Message message = getFullMessageByKey(messageKey);
        if (message != null) {
            final long summaryStatus = calculateMemberStatusSummary(message);
            mDb.execSQL(mMainService.getString(R.string.sql_message_set_member_summary),
                new String[] { String.valueOf(summaryStatus), message.key });
        }
    }

    protected void setMessageSummaryFailed(final String messageKey) {
        T.dontCare();
        mDb.execSQL(mMainService.getString(R.string.sql_message_set_member_summary),
            new String[] { String.valueOf(MessageMemberStatusSummaryEncoding.ERROR), messageKey });
    }

    private long calculateMemberStatusSummary(final MessageTO message) {
        int numNonSenderMembers = 0;
        int numNonSenderMembersReceived = 0;
        int numNonSenderMembersQuickReplied = 0;
        int numNonSenderMembersDismissed = 0;

        for (MemberStatusTO memberStatus : message.members) {
            if (!memberStatus.member.equals(message.sender)) {
                numNonSenderMembers++;
                if ((memberStatus.status & MessagingPlugin.STATUS_RECEIVED) == MessagingPlugin.STATUS_RECEIVED) {
                    numNonSenderMembersReceived++;
                    if ((memberStatus.status & MessagingPlugin.STATUS_ACKED) == MessagingPlugin.STATUS_ACKED) {
                        if (memberStatus.button_id == null)
                            numNonSenderMembersDismissed++;
                        else
                            numNonSenderMembersQuickReplied++;
                    }
                }
            }
        }

        final long summaryStatus = MessageMemberStatusSummaryEncoding.encodeMessageMemberSummary(numNonSenderMembers,
            numNonSenderMembersReceived, numNonSenderMembersQuickReplied, numNonSenderMembersDismissed);

        return summaryStatus;
    }

    public String myEmail() {
        T.dontCare();
        return mMainService.getIdentityStore().getIdentity().getEmail();
    }

    private ButtonTO readButton(Cursor bcurs) {
        ButtonTO button = new ButtonTO();
        button.id = bcurs.getString(0);
        button.caption = bcurs.getString(1);
        button.action = bcurs.getString(2);
        button.ui_flags = bcurs.getLong(3);
        return button;
    }

    @SuppressWarnings("unchecked")
    private Message toFullMessage(Cursor curs, int query) {
        // 8 columns --> sql_message_get_message_by_unprocessed_message_index
        // 15 columns -> sql_message_cursor_full_thread
        // 21 columns -> sql_message_get_message_by_key
        Message message = new Message();
        String formString = null;
        switch (query) {
        case R.string.sql_message_get_message_by_key:
            message.key = curs.getString(0);
            message.parent_key = curs.getString(1);
            message.sender = curs.getString(2);
            message.message = curs.getString(3);
            message.timestamp = curs.getLong(4);
            message.flags = curs.getLong(5);
            message.branding = curs.getString(6);
            message.alert_flags = curs.getLong(7);
            message.needsMyAnswer = curs.getLong(8) != 0;
            message.replyCount = curs.getLong(9);
            message.dirty = curs.getLong(10) != 0;
            message.recipients_status = curs.getLong(11);
            message.recipients = curs.getString(12);
            formString = curs.getString(13);
            if (formString != null)
                message.form = (Map<String, Object>) JSONValue.parse(formString);
            message.threadDirty = curs.getLong(14) != 0;
            message.threadNeedsMyAnswer = curs.getLong(15) != 0;
            message.dismiss_button_ui_flags = curs.getLong(16);
            message.lastThreadMessage = curs.getString(17);
            message.threadShowInList = curs.getInt(18) != 0;
            message.broadcast_type = curs.getString(19);
            message.thread_avatar_hash = curs.getString(20);
            message.thread_background_color = curs.getString(21);
            message.thread_text_color = curs.getString(22);
            message.priority = curs.getLong(23);
            message.default_priority = curs.getLong(24);
            message.default_sticky = curs.getLong(25) != 0;
            break;
        case R.string.sql_message_cursor_full_thread:
            message.needsMyAnswer = curs.getLong(11) != 0;
            message.replyCount = curs.getLong(12);
            message.dirty = curs.getLong(13) != 0;
            message.recipients_status = curs.getLong(14);
            message.recipients = curs.getString(15);
            formString = curs.getString(16);
            if (formString != null)
                message.form = (Map<String, Object>) JSONValue.parse(formString);
            // column 15: rowid
            message.threadNeedsMyAnswer = curs.getLong(18) != 0;
            //$FALL-THROUGH$
        case R.string.sql_message_get_message_by_unprocessed_message_index:
            message.key = curs.getString(0);
            message.parent_key = curs.getString(1);
            message.sender = curs.getString(2);
            message.message = curs.getString(3);
            message.timestamp = curs.getLong(4);
            message.flags = curs.getLong(5);
            message.branding = curs.getString(6);
            message.alert_flags = curs.getLong(7);
            message.priority = curs.getLong(8);
            message.default_priority = curs.getLong(9);
            message.default_sticky = curs.getLong(10) != 0;
        }
        addButtonsToMessageObject(message);
        message.attachments = getAttachmentsFromMessage(message.key);
        return message;
    }

    private Message addButtonsToMessageObject(Message message) {
        Cursor bcurs = mDb.rawQuery(mMainService.getString(R.string.sql_message_get_message_buttons),
            new String[] { message.key });
        try {
            if (!bcurs.moveToFirst()) {
                message.buttons = new ButtonTO[0];
                return message;
            }
            List<ButtonTO> buttons = new ArrayList<ButtonTO>();
            buttons.add(readButton(bcurs));
            while (bcurs.moveToNext())
                buttons.add(readButton(bcurs));
            message.buttons = buttons.toArray(new ButtonTO[buttons.size()]);
            return message;
        } finally {
            bcurs.close();
        }
    }

    public void addMembers(MessageTO message) {
        Cursor bcurs = mDb.rawQuery(mMainService.getString(R.string.sql_message_get_message_members_statusses),
            new String[] { message.key });
        try {
            if (!bcurs.moveToFirst()) {
                message.members = new MemberStatusTO[0];
                return;
            }
            List<MemberStatusTO> members = new ArrayList<MemberStatusTO>();
            members.add(readMember(bcurs));
            while (bcurs.moveToNext())
                members.add(readMember(bcurs));
            message.members = members.toArray(new MemberStatusTO[members.size()]);
        } finally {
            bcurs.close();
        }
    }

    private MemberStatusTO readMember(Cursor curs) {
        MemberStatusTO member = new MemberStatusTO();
        member.member = curs.getString(0);
        member.received_timestamp = curs.getLong(1);
        member.acked_timestamp = curs.getLong(2);
        member.button_id = curs.getString(3);
        member.custom_reply = curs.getString(4);
        member.status = curs.getLong(5);
        return member;
    }

    private void setMessageProcessed(final String messageKey) {
        T.BIZZ();
        mSetMessageProcessedBIZZ.bindString(1, messageKey);
        mSetMessageProcessedBIZZ.execute();
    }

    private void setMessageProcessed(final String messageKey, final String buttonId, final String customReply) {
        T.BIZZ();
        setMessageProcessed(messageKey);

        mUpdateMyMemberStatusBIZZ.bindLong(1, mMainService.currentTimeMillis() / 1000);
        if (buttonId == null)
            mUpdateMyMemberStatusBIZZ.bindNull(2);
        else
            mUpdateMyMemberStatusBIZZ.bindString(2, buttonId);
        if (customReply == null)
            mUpdateMyMemberStatusBIZZ.bindNull(3);
        else
            mUpdateMyMemberStatusBIZZ.bindString(3, customReply);
        mUpdateMyMemberStatusBIZZ.bindLong(4, MessagingPlugin.STATUS_ACKED);
        mUpdateMyMemberStatusBIZZ.bindString(5, messageKey);
        mUpdateMyMemberStatusBIZZ.bindString(6, myEmail());
        mUpdateMyMemberStatusBIZZ.execute();

        if ((getMessageFlagsBIZZ(messageKey) & MessagingPlugin.FLAG_AUTO_LOCK) == MessagingPlugin.FLAG_AUTO_LOCK) {
            mUpdateFlagsBIZZ.bindLong(1, MessagingPlugin.FLAG_LOCKED);
            mUpdateFlagsBIZZ.bindString(2, messageKey);
            mUpdateFlagsBIZZ.execute();
        }

        updateMessageMemberSummary(messageKey);
    }

    public long getMessageFlagsUI(final String messageKey) {
        T.UI();
        mGetMessageFlagsUI.bindString(1, messageKey);
        return mGetMessageFlagsUI.simpleQueryForLong();
    }

    public long getMessageFlagsBIZZ(final String messageKey) {
        T.BIZZ();
        mGetMessageFlagsBIZZ.bindString(1, messageKey);
        return mGetMessageFlagsBIZZ.simpleQueryForLong();
    }

    public void updateForm(final Message message) {
        T.dontCare();
        if (T.BIZZ != T.getThreadType()) {
            mMainService.postOnBIZZHandler(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    updateForm(message);
                }
            });
            return;
        }

        T.BIZZ();
        if (message.form == null) {
            mUpdateFormBIZZ.bindNull(1);
        } else {
            mUpdateFormBIZZ.bindString(1, JSONValue.toJSONString(message.form));
        }
        mUpdateFormBIZZ.bindString(2, message.key);
        mUpdateFormBIZZ.execute();
    }

    public void saveFormUpdate(final Message message, final String buttonId, final long receivedTimestamp,
        final long ackedTimestamp) {
        T.BIZZ();
        TransactionHelper.runInTransaction(mDb, "saveFormUpdate", new TransactionWithoutResult() {
            @Override
            protected void run() {
                updateForm(message);
                long status = MessagingPlugin.STATUS_ACKED | MessagingPlugin.STATUS_RECEIVED;
                updateMessageMemberStatusInDbNotInTransaction(message.key, myEmail(), status, buttonId, null,
                    receivedTimestamp, ackedTimestamp);
                setMessageProcessed(message.key);
                setDirty(message.key, false);
                updateMessageMemberSummary(message.key);
            }
        });
    }

    public void setMessageProcessedBizz(final String message, final String buttonId, final String customReply,
        final SafeRunnable updateDoneHandler) {
        T.BIZZ();
        TransactionHelper.runInTransaction(mDb, "saveFormUpdate", new TransactionWithoutResult() {
            @Override
            protected void run() {
                setMessageProcessed(message, buttonId, customReply);
                if (updateDoneHandler != null) {
                    TransactionHelper.onTransactionCommitted(updateDoneHandler);
                }
            }
        });
    }

    public void setChatMessageProcessedBizz(final String messageKey) {
        T.BIZZ();
        TransactionHelper.runInTransaction(mDb, "saveFormUpdate", new TransactionWithoutResult() {
            @Override
            protected void run() {
                setMessageProcessed(messageKey);
            }
        });
    }

    public int getExistence(final String threadKey) {
        T.BIZZ();
        mGetMessageExistenceUI.bindString(1, threadKey);
        try {
            return (int) mGetMessageExistenceUI.simpleQueryForLong();
        } catch (SQLiteDoneException e) {
            return MessagingPlugin.EXISTENCE_NOT_FOUND;
        }
    }

    public void deleteConversation(final String threadKey) {
        T.dontCare();
        setExistence(MessagingPlugin.EXISTENCE_DELETED, threadKey);
    }

    public void restoreConversation(final String threadKey) {
        T.BIZZ();
        setExistence(MessagingPlugin.EXISTENCE_ACTIVE, threadKey);
    }

    private void setExistence(final int existence, final String threadKey) {
        T.dontCare();
        final SQLiteStatement stmt = mSetMessageExistence.getStatementForThisThread();
        stmt.bindLong(1, existence);
        stmt.bindString(2, threadKey);
        stmt.bindString(3, threadKey);
        stmt.execute();
    }

    public void addRequestedConversation(final String threadKey) {
        T.BIZZ();
        mInsertRequestedConversationBIZZ.bindString(1, threadKey);
        mInsertRequestedConversationBIZZ.execute();
    }

    public boolean isConversationAlreadyRequested(final String threadKey) {
        T.BIZZ();
        mCountRequestedConversationBIZZ.bindString(1, threadKey);
        return mCountRequestedConversationBIZZ.simpleQueryForLong() > 0;
    }

    public void deleteRequestedConversation(final String threadKey) {
        T.BIZZ();
        mDeleteRequestedConversationBIZZ.bindString(1, threadKey);
        mDeleteRequestedConversationBIZZ.execute();
    }

    public void saveMessageFlowRun(MessageFlowRun mfr) {
        T.BIZZ();
        mSaveMessageFlowRunHTTP.bindString(1, mfr.parentKey);
        mSaveMessageFlowRunHTTP.bindString(2, mfr.state);
        mSaveMessageFlowRunHTTP.bindString(3, mfr.staticFlowHash);
        mSaveMessageFlowRunHTTP.execute();
    }

    public void deleteMessageFlowRun(String parentMessageKey) {
        T.BIZZ();
        mDeleteMessageFlowRunHTTP.bindString(1, parentMessageKey);
        mDeleteMessageFlowRunHTTP.execute();
    }

    public MessageFlowRun getMessageFlowRun(String parentMessageKey) {
        Cursor curs = mDb.rawQuery(mMainService.getString(R.string.sql_mf_run_get), new String[] { parentMessageKey });
        try {
            if (!curs.moveToFirst())
                return null;

            MessageFlowRun mfr = new MessageFlowRun();
            mfr.parentKey = parentMessageKey;
            mfr.state = curs.getString(0);
            mfr.staticFlowHash = curs.getString(1);
            return mfr;
        } finally {
            curs.close();
        }
    }

    public void recalculateShowInList() {
        mRecalculateMessagesShowInList.execute();
    }

    public void insertAttachments(final AttachmentTO[] attachments, final String messageKey) {
        T.dontCare();
        for (AttachmentTO attachment : attachments) {
            mAddMessageAttachments.bindString(1, messageKey);
            mAddMessageAttachments.bindString(2, attachment.content_type);
            mAddMessageAttachments.bindString(3, attachment.download_url);
            mAddMessageAttachments.bindLong(4, attachment.size);
            mAddMessageAttachments.bindString(5, attachment.name);
            mAddMessageAttachments.execute();
        }
    }

    private AttachmentTO[] getAttachmentsFromMessage(String messageKey) {
        T.dontCare();
        List<AttachmentTO> attachments = new ArrayList<AttachmentTO>();

        final Cursor bcurs = mDb.rawQuery(mMainService.getString(R.string.sql_message_get_attachments),
            new String[] { messageKey });
        try {
            if (!bcurs.moveToFirst()) {
                return new AttachmentTO[0];
            }
            do {
                attachments.add(readAttachment(bcurs));
            } while (bcurs.moveToNext());
        } finally {
            bcurs.close();
        }

        return attachments.toArray(new AttachmentTO[attachments.size()]);
    }

    private AttachmentTO readAttachment(Cursor curs) {
        AttachmentTO attachment = new AttachmentTO();
        attachment.content_type = curs.getString(0);
        attachment.download_url = curs.getString(1);
        attachment.size = curs.getLong(2);
        attachment.name = curs.getString(3);
        return attachment;
    }

    public long getDirtyThreadsCount() {
        return mGetDirtyThreadsCount.simpleQueryForLong();
    }

    public ArrayList<UnreadMessage> getFirstUnreadMessagesInThread(String parentKey) {
        ArrayList<UnreadMessage> messages = new ArrayList<>();

        final Cursor cursor = mDb.rawQuery(mMainService.getString(R.string.sql_get_first_unread_messages_in_thread),
                new String[]{parentKey});
        try {
            if (!cursor.moveToFirst()) {
                return new ArrayList<>();
            }
            do {
                messages.add(readUnreadMessage(cursor));
            } while (cursor.moveToNext());
        } finally {
            cursor.close();
        }
        return messages;
    }

    public UnreadMessage readUnreadMessage(Cursor cursor) {
        return new UnreadMessage(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
    }

    public int getUnreadMessageCountInThread(String parentKey) {
        mGetUnreadMessageCountInThread.bindString(1, parentKey);
        return (int) mGetUnreadMessageCountInThread.simpleQueryForLong();
    }


    public int getTotalUnreadCount(long timestamp) {
        T.BIZZ();
        mGetTotalDirtyMessagecount.bindLong(1, timestamp);
        return (int) mGetTotalDirtyMessagecount.simpleQueryForLong();
    }

}
