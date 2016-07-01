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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.RecyclerListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.HomeActivity;
import com.mobicage.rogerthat.IdentityStore;
import com.mobicage.rogerthat.MainActivity;
import com.mobicage.rogerthat.SendMessageWizardActivity;
import com.mobicage.rogerthat.ServiceBoundActivity;
import com.mobicage.rogerthat.ServiceBoundCursorListActivity;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.messaging.MessageStore.CursorSet;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.time.TimeUtils;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.config.AppConstants;
import com.mobicage.to.messaging.AttachmentTO;
import com.mobicage.to.messaging.ButtonTO;
import com.mobicage.to.messaging.MemberStatusTO;

public class MessagingActivity extends ServiceBoundCursorListActivity {

    private static final long sGMTOffsetMillis = TimeUtils.getGMTOffsetMillis();

    // Owned by UI thread
    private MessagingPlugin mMessagingPlugin;
    private FriendsPlugin mFriendsPlugin;
    private String mMemberFilter;
    private boolean mFirstCellIsComposeMessage = false;
    private String mMyEmail;
    private Resources mResources;
    private boolean mEditing = false;
    private Set<String> mToBeDeleted;
    private CursorSet mCursorSet = null;
    private Map<View, SafeRunnable> mCellsToUpdate = new HashMap<View, SafeRunnable>();


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            switch (item.getItemId()) {
            case R.id.new_message:
                if (!mEditing)
                    item.setVisible(AppConstants.FRIENDS_ENABLED);
                else
                    item.setVisible(false);
                break;
            case R.id.select_all:
            case R.id.deselect_all:
                item.setVisible(mEditing);
                break;
            case R.id.delete_messages:
                item.setEnabled(((CursorAdapter) getListAdapter()).getCursor().getCount() > 0);
                //$FALL-THROUGH$
            default:
                item.setVisible(!mEditing);
                break;
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected boolean showFABMenu() {
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        T.UI();
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.messaging_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        T.UI();
        switch (item.getItemId()) {
        case R.id.new_message:
            showSendMessageWizardActivity();
            return true;
        case R.id.delete_messages:
            setEditing(true);
            return true;
        case R.id.select_all:
            mToBeDeleted.clear();
            Cursor c = ((CursorAdapter) getListAdapter()).getCursor();
            if (!c.moveToFirst()) {
                L.e("Could not move cursor to first position");
                return false;
            }
            do {
                String key = MessageStore.getKeyFromMessageCursor(c);
                String parentKey = MessageStore.getParentKeyFromMessageCursor(c);
                long flags = MessageStore.getFlagsFromMessageCursor(c);
                if (!SystemUtils.isFlagEnabled(flags, MessagingPlugin.FLAG_NOT_REMOVABLE))
                    mToBeDeleted.add(parentKey == null ? key : parentKey);
            } while (c.moveToNext());

            ((CursorAdapter) getListAdapter()).notifyDataSetChanged();
            return true;
        case R.id.deselect_all:
            mToBeDeleted.clear();
            ((CursorAdapter) getListAdapter()).notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        T.UI();
        if (mMessagingPlugin != null && mMemberFilter == null)
            mMessagingPlugin.inboxOpened();

        if (getWasPaused() && mCursorSet != null) {
            startManagingCursor(mCursorSet.cursor);
            startManagingCursor(mCursorSet.indexer);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCursorSet != null) {
            stopManagingCursor(mCursorSet.cursor);
            stopManagingCursor(mCursorSet.indexer);
        }
    }

    @Override
    protected void onDestroy() {
        if (mService != null) {
            for (SafeRunnable sr : mCellsToUpdate.values()) {
                mService.removeFromUIHandler(sr);
            }
        }
        mCellsToUpdate.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        T.UI();
        setContentView(R.layout.messaging);
        setListView((ListView) findViewById(R.id.message_list));
        super.onCreate(savedInstanceState);
        mResources = getResources();

        Intent intent = getIntent();
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                mMemberFilter = extras.getString(MessagingPlugin.MEMBER_FILTER);
            }
        }

        calculateFirstCellIsComposeMessage();

        findViewById(R.id.delete_done_button).setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                for (String threadKey : mToBeDeleted) {
                    mMessagingPlugin.deleteConversation(threadKey);
                }
                changeCursor();
                setEditing(false);
            }
        });
        findViewById(R.id.delete_cancel_button).setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                setEditing(false);
            }
        });
    }

    @Override
    protected String[] getAllReceivingIntents() {
        return new String[] { MessagingPlugin.NEW_MESSAGE_RECEIVED_INTENT,
            MessagingPlugin.MESSAGE_MEMBER_STATUS_UPDATE_RECEIVED_INTENT, MessagingPlugin.MESSAGE_DIRTY_CLEANED_INTENT,
            MessagingPlugin.MESSAGE_KEY_UPDATED_INTENT, MessagingPlugin.MESSAGE_LOCKED_INTENT,
            MessagingPlugin.MESSAGE_PROCESSED_INTENT, MessagingPlugin.MESSAGE_FAILURE,
            MessagingPlugin.MESSAGE_THREAD_VISIBILITY_CHANGED_INTENT, MessagingPlugin.THREAD_DELETED_INTENT,
            MessagingPlugin.THREAD_RECOVERED_INTENT, MessagingPlugin.THREAD_MODIFIED_INTENT,
            FriendsPlugin.FRIEND_AVATAR_CHANGED_INTENT, IdentityStore.IDENTITY_CHANGED_INTENT,
            FriendsPlugin.FRIENDS_LIST_REFRESHED };
    }

    @Override
    protected void onServiceBound() {
        T.UI();
        mMessagingPlugin = mService.getPlugin(MessagingPlugin.class);
        if (mMemberFilter == null)
            mMessagingPlugin.inboxOpened();
        mFriendsPlugin = mService.getPlugin(FriendsPlugin.class);
        mMyEmail = mService.getIdentityStore().getIdentity().getEmail();

        if (mMemberFilter == null) {
            setNavigationBarVisible(AppConstants.SHOW_NAV_HEADER);
            setNavigationBarTitle(R.string.tab_messaging);
        } else {
            setNavigationBarVisible(true);
            setNavigationBarTitle(getString(R.string.member_message_history, mFriendsPlugin.getName(mMemberFilter)));
        }

        createCursorSet();
        startManagingCursor(mCursorSet.cursor);
        startManagingCursor(mCursorSet.indexer);

        final MessageListAdapter messageListAdapter = new MessageListAdapter(this, mCursorSet, mMessagingPlugin.getStore(),
            mFriendsPlugin);
        setListAdapter(messageListAdapter);

        final IntentFilter filter = new IntentFilter();
        for (String action : getAllReceivingIntents())
            filter.addAction(action);
        registerReceiver(getDefaultBroadcastReceiver(), filter);

        findViewById(R.id.navigation_bar_home_button).setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                Intent i = new Intent(MessagingActivity.this, HomeActivity.class);
                i.setFlags(MainActivity.FLAG_CLEAR_STACK);
                startActivity(i);
                finish();
            }
        });
    }

    @Override
    protected void onServiceUnbound() {
        unregisterReceiver(getDefaultBroadcastReceiver());
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        T.UI();
        if (mFirstCellIsComposeMessage && (position == 0)) {
            showSendMessageWizardActivity();
        } else {
            final Object tag = v.getTag();
            if (tag != null) {
                final ViewInfoHolder holder = (ViewInfoHolder) tag;
                final Message message = holder.message;
                mMessagingPlugin.showMessage(this, message, mMemberFilter);
            }
        }
    }

    @Override
    protected boolean onListItemLongClick(ListView l, View v, int position, long id) {
        if (mFirstCellIsComposeMessage && (position == 0))
            return false;

        final Object tag = v.getTag();
        if (tag != null) {
            final ViewInfoHolder holder = (ViewInfoHolder) tag;
            if (SystemUtils.isFlagEnabled(holder.message.flags, MessagingPlugin.FLAG_NOT_REMOVABLE))
                return false;
            mMessagingPlugin.removeConversationFromList(this, holder.message.getThreadKey());
        }
        return true;
    }

    private void createCursorSet() {
        if (mCursorSet != null) {
            stopManagingCursor(mCursorSet.cursor);
            stopManagingCursor(mCursorSet.indexer);
        }
        mCursorSet = mMessagingPlugin.getStore().getMessagesCursor(mMemberFilter);
    }

    @Override
    protected void changeCursor() {
        if (mServiceIsBound) {
            createCursorSet();
            MessageListAdapter mla = (MessageListAdapter) getListAdapter();
            mla.mCursor = mCursorSet.cursor;
            mla.mIndexerCursor = mCursorSet.indexer;
            mla.mQuery = mCursorSet.query;
            mla.changeCursor(mCursorSet.cursor);
            mla.buildSectionIndex();
        }
    }

    private void showSendMessageWizardActivity() {
        long currentTime = System.currentTimeMillis();
        if (getLastTimeClicked() != 0
            && (currentTime < (getLastTimeClicked() + ServiceBoundActivity.DOUBLE_CLICK_TIMESPAN))) {
            L.d("ignoring click on send message");
            return;
        }

        setLastTimeClicked(currentTime);
        Intent intent = new Intent(this, SendMessageWizardActivity.class);
        startActivity(intent);
    }

    private void setEditing(boolean editing) {
        mEditing = editing;
        mToBeDeleted = mEditing ? new HashSet<String>() : null;

        findViewById(R.id.delete_messages).setVisibility(mEditing ? View.VISIBLE : View.GONE);

        calculateFirstCellIsComposeMessage();
        ((CursorAdapter) getListAdapter()).notifyDataSetChanged();
    }

    private void calculateFirstCellIsComposeMessage() {
        mFirstCellIsComposeMessage = AppConstants.FRIENDS_ENABLED ? (mMemberFilter == null && !mEditing) : false;
    }

    // Object put in view tag. Contains fast references to message and subviews
    private class ViewInfoHolder {
        Message message;
        TextView recipientsView;
        TextView timestampView;
        TextView messageView;
        TextView messageCountView;
        ImageView avatarView;
        ImageView statusView;
        CheckBox checkBox;
    }

    private class MessageListAdapter extends CursorAdapter implements SectionIndexer {

        private class Section {

            int day;
            int start;

            @Override
            public String toString() {
                // e.g. beginning of day 10 (start with day 0) is 10 * 86400 seconds in UTC
                // however, in Belgium summer time we need to subtract 7200 seconds
                return TimeUtils.getDayStrOrToday(MessagingActivity.this, day * 86400L * 1000L - sGMTOffsetMillis);
            }
        }

        private final MessageStore mStore;
        private final FriendsPlugin mFriendsPlugin;
        private final LayoutInflater mLayoutInflater;
        private Section[] mSections;

        private Cursor mCursor;
        private Cursor mIndexerCursor;
        private int mQuery;

        public MessageListAdapter(Context context, CursorSet cursorSet, MessageStore store, FriendsPlugin friendsPlugin) {
            super(context, cursorSet.cursor, false);
            mCursor = cursorSet.cursor;
            mStore = store;
            mFriendsPlugin = friendsPlugin;
            mLayoutInflater = getLayoutInflater();
            mIndexerCursor = cursorSet.indexer;
            mQuery = cursorSet.query;
            buildSectionIndex();
        }

        private void buildSectionIndex() {
            mSections = new Section[mIndexerCursor.getCount()];
            int pos = mFirstCellIsComposeMessage ? 1 : 0;
            int index = 0;
            if (mIndexerCursor.moveToFirst()) {
                while (true) {
                    Section section = new Section();
                    section.start = pos;
                    section.day = mIndexerCursor.getInt(0);
                    mSections[index++] = section;
                    pos += mIndexerCursor.getInt(1);
                    if (!mIndexerCursor.moveToNext()) {
                        break;
                    }
                }
            }

            getListView().setRecyclerListener(new RecyclerListener() {
                @Override
                public void onMovedToScrapHeap(View view) {
                    SafeRunnable sr = mCellsToUpdate.get(view);
                    if (sr != null) {
                        mService.removeFromUIHandler(sr);
                        mCellsToUpdate.remove(view);
                    }
                }
            });
        }

        @Override
        public int getCount() {
            int c = super.getCount();
            if (mFirstCellIsComposeMessage)
                c++;
            return c;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            T.UI();
            if (mFirstCellIsComposeMessage) {
                if (position == 0) {
                    View headerView = mLayoutInflater.inflate(R.layout.main_list_header, null);
                    ((TextView) headerView.findViewById(R.id.mainheader)).setText(R.string.new_message_short);
                    ((TextView) headerView.findViewById(R.id.subheader)).setText(R.string.new_message_long);
                    return headerView;
                }
                position--;
            }

            if (!mCursor.moveToPosition(position)) {
                throw new IllegalStateException("couldn't move cursor to position " + position);
            }

            View view;
            Message message = mStore.getCurrentMessage(mCursor, mQuery);

            if (!message.threadShowInList) {
                message.threadShowInList = true;
                mMessagingPlugin.setMessageThreadVisibility(message.getThreadKey(), true);
            }

            ViewInfoHolder holder = null;

            if (convertView == null || convertView.getTag() == null) {
                view = mLayoutInflater.inflate(R.layout.message, parent, false);
            } else {
                view = convertView;
                holder = (ViewInfoHolder) convertView.getTag();
            }

            if (holder == null) {
                holder = new ViewInfoHolder();
                holder.avatarView = (ImageView) view.findViewById(R.id.avatar);
                holder.recipientsView = (TextView) view.findViewById(R.id.recipients);
                holder.timestampView = (TextView) view.findViewById(R.id.timestamp);
                holder.messageView = (TextView) view.findViewById(R.id.message);
                holder.statusView = (ImageView) view.findViewById(R.id.status_icon);
                holder.messageCountView = (TextView) view.findViewById(R.id.message_count);
                holder.checkBox = (CheckBox) view.findViewById(R.id.message_checkbox);
                view.setTag(holder);
            }
            holder.message = message;

            final boolean dynamicChat = SystemUtils.isFlagEnabled(message.flags, MessagingPlugin.FLAG_DYNAMIC_CHAT);
            setStatusIcon(holder, dynamicChat);
            setMessageOnView(view, holder, dynamicChat);

            return view;
        }

        private void setMessageOnView(final View view, ViewInfoHolder holder, boolean dynamicChat) {

            final Message message = holder.message;

            final ImageView avatarView = holder.avatarView;

            final Bitmap threadAvatar;
            if (message.thread_avatar_hash != null
                && (threadAvatar = mMessagingPlugin.getStore().getThreadAvatar(message.thread_avatar_hash)) != null) {
                avatarView.setImageBitmap(threadAvatar);
            } else if (dynamicChat) {
                avatarView.setImageResource(R.drawable.group_60);
            } else if (FriendsPlugin.SYSTEM_FRIEND.equals(message.sender)
                || mFriendsPlugin.getStore().getFriendType(message.sender) == FriendsPlugin.FRIEND_TYPE_SERVICE) {
                avatarView.setImageBitmap(mFriendsPlugin.getAvatarBitmap(message.sender));

            } else if (message.members.length == 2) {
                String otherOne = null;
                for (MemberStatusTO member : message.members) {
                    if (!mMyEmail.equals(member.member)) {
                        otherOne = member.member;
                        break;
                    }
                }
                if (otherOne == null)
                    otherOne = message.sender;
                avatarView.setImageBitmap(mFriendsPlugin.getAvatarBitmap(otherOne));

            } else {
                avatarView.setImageResource(R.drawable.group_60);
            }

            String recipients;
            String messageText;
            if (message.parent_key == null && dynamicChat) {
                try {
                    JSONObject json = (JSONObject) JSONValue.parse(message.message);
                    recipients = (String) json.get("t");
                    messageText = (String) json.get("d");
                } catch (Throwable t) {
                    recipients = message.recipients;
                    messageText = message.message;
                }
            } else if (message.parent_key != null && dynamicChat) {
                try {
                    JSONObject json = (JSONObject) JSONValue.parse(mMessagingPlugin.getStore().getMessageMessage(
                        message.parent_key));
                    recipients = (String) json.get("t");
                    messageText = message.message;
                } catch (Throwable t) {
                    recipients = message.recipients;
                    messageText = message.message;
                }
            } else {
                recipients = message.recipients;
                messageText = message.message;
            }
            if (TextUtils.isEmptyOrWhitespace(messageText)) {
                if (message.buttons != null && message.buttons.length > 0) {
                    List<String> buttons = new ArrayList<String>();
                    for (ButtonTO bt : message.buttons) {
                        buttons.add(bt.caption);
                    }
                    messageText = android.text.TextUtils.join(" / ", buttons);
                } else if (message.attachments != null && message.attachments.length > 0) {
                    Set<String> attachments = new HashSet<String>();
                    for (AttachmentTO at : message.attachments) {
                        if (!TextUtils.isEmptyOrWhitespace(at.name)) {
                            attachments.add(at.name);
                        } else if (at.content_type.toLowerCase(Locale.US).startsWith("video/")) {
                            attachments.add(getString(R.string.attachment_name_video));
                        } else if (at.content_type.toLowerCase(Locale.US).startsWith("image/")) {
                            attachments.add(getString(R.string.attachment_name_image));
                        } else {
                            L.d("Not added attachment with type '" + at.content_type + "' because no translation found");
                        }
                    }
                    if (attachments.size() > 0) {
                        messageText = android.text.TextUtils.join(", ", attachments);
                    }
                }
            }

            int tmpThreadTextColor = Integer.MAX_VALUE;
            int tmpThreadBackgroundColor = Integer.MAX_VALUE;
            final long priority = message.priority;
            if (priority == Message.PRIORITY_HIGH) {
                tmpThreadTextColor = mResources.getColor(R.color.mc_priority_high_text);
                tmpThreadBackgroundColor = mResources.getColor(R.color.mc_priority_high_background);
            } else if (priority == Message.PRIORITY_URGENT || priority == Message.PRIORITY_URGENT_WITH_ALARM) {
                tmpThreadTextColor = mResources.getColor(R.color.mc_priority_urgent_text);
                tmpThreadBackgroundColor = mResources.getColor(R.color.mc_priority_urgent_background);
            }
            final int threadTextColor = message.thread_text_color != null ? Color.parseColor("#"
                + message.thread_text_color) : tmpThreadTextColor;
            final int threadBackgroundColor = message.thread_background_color != null ? Color.parseColor("#"
                + message.thread_background_color) : tmpThreadBackgroundColor;
            final TextView recipientsView = holder.recipientsView;
            recipientsView.setText(recipients);

            if (threadTextColor != Integer.MAX_VALUE) {
                recipientsView.setTextColor(threadTextColor);
            } else {
                recipientsView.setTextColor(mResources.getColorStateList(android.R.color.primary_text_light));
            }

            final TextView messageCountView = holder.messageCountView;
            long replyCount = message.replyCount;
            if (dynamicChat)
                replyCount--;

            long messageCountText = 0;
            if (AppConstants.MESSAGES_SHOW_REPLY_VS_UNREAD_COUNT) {
                if (replyCount > 1) {
                    messageCountText = replyCount;
                } else {
                    messageCountText = 0;
                }
            } else {
                messageCountText = message.unreadCount;
                if (dynamicChat && replyCount < messageCountText) {
                    messageCountText--;
                }
            }
            if (messageCountText >= 1) {
                messageCountView.setText("" + messageCountText);
                messageCountView.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;

                if (threadBackgroundColor != Integer.MAX_VALUE) {
                    final int bgColor;
                    final int color;
                    if (UIUtils.getLuminus(threadBackgroundColor) < 0.5f) {
                        // Dark background
                        // bg color white , alpha 0.75
                        // color black
                        bgColor = Color.argb(192, 255, 255, 255);
                        color = Color.BLACK;
                    } else {
                        // Light background
                        // bg color black , alpha 0.25
                        // color white
                        bgColor = Color.argb(63, 0, 0, 0);
                        color = Color.WHITE;
                    }
                    messageCountView.setTextColor(color);
                    GradientDrawable bgShape = (GradientDrawable) messageCountView.getBackground();
                    bgShape.setColor(bgColor);
                }
            } else {
                messageCountView.getLayoutParams().width = 0;
            }

            final TextView timestamp = holder.timestampView;
            if (timestamp != null) {
                long startRunnableIn = TimeUtils.startRunnableToUpdateTimeIn(MessagingActivity.this,
                    message.timestamp * 1000);
                if (startRunnableIn > 0) {
                    SafeRunnable sr = new SafeRunnable() {

                        @Override
                        protected void safeRun() throws Exception {
                            timestamp.setText(TimeUtils.getHumanTime(MessagingActivity.this, message.timestamp * 1000,
                                true));
                            long startRunnableIn = TimeUtils.startRunnableToUpdateTimeIn(MessagingActivity.this,
                                message.timestamp * 1000);
                            if (startRunnableIn > 0) {
                                mService.postDelayedOnUIHandler(this, startRunnableIn);
                            } else {
                                mCellsToUpdate.remove(view);
                            }
                        }
                    };
                    mCellsToUpdate.put(view, sr);
                    mService.postDelayedOnUIHandler(sr, startRunnableIn);
                }
                timestamp.setText(TimeUtils.getHumanTime(MessagingActivity.this, message.timestamp * 1000, true));
                if (threadTextColor != Integer.MAX_VALUE) {
                    timestamp.setTextColor(threadTextColor);
                } else {
                    timestamp.setTextColor(mResources.getColorStateList(android.R.color.secondary_text_light));
                }
            }

            final TextView messageView = holder.messageView;
            messageView.setText(messageText);
            if (threadTextColor != Integer.MAX_VALUE) {
                messageView.setTextColor(threadTextColor);
            }

            if (!message.threadDirty) {
                messageView.setTypeface(null, Typeface.NORMAL);
                if (threadBackgroundColor == Integer.MAX_VALUE)
                    view.setBackgroundDrawable(getResources().getDrawable(R.drawable.mc_message_background));
                if (threadTextColor == Integer.MAX_VALUE) {
                    messageView.setTextColor(mResources.getColorStateList(android.R.color.secondary_text_light));
                }
            } else {
                if (message.threadNeedsMyAnswer)
                    messageView.setTypeface(null, Typeface.BOLD);
                else
                    messageView.setTypeface(null, Typeface.ITALIC);
                if (threadBackgroundColor == Integer.MAX_VALUE)
                    view.setBackgroundDrawable(getResources().getDrawable(R.drawable.mc_message_highlighted_background));
                if (threadTextColor == Integer.MAX_VALUE) {
                    messageView.setTextColor(mResources.getColorStateList(android.R.color.primary_text_light));
                }
            }

            messageView.setTextSize(18);
            recipientsView.setTextSize(14);

            if (threadBackgroundColor != Integer.MAX_VALUE)
                view.setBackgroundColor(threadBackgroundColor);

            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (mToBeDeleted != null) {
                        if (isChecked) {
                            mToBeDeleted.add(message.getThreadKey());
                        } else {
                            mToBeDeleted.remove(message.getThreadKey());
                        }
                    }
                }
            });
            holder.checkBox.setChecked(mToBeDeleted != null && mToBeDeleted.contains(message.getThreadKey()));
            holder.checkBox.setVisibility(mEditing
                && !SystemUtils.isFlagEnabled(message.flags, MessagingPlugin.FLAG_NOT_REMOVABLE) ? View.VISIBLE
                : View.GONE);
        }

        private void setStatusIcon(ViewInfoHolder holder, boolean dynamicChat) {
            final Message message = holder.message;
            if (dynamicChat) {
                if (message.priority == Message.PRIORITY_URGENT_WITH_ALARM) {
                    holder.statusView.setImageResource(R.drawable.status_ringing);
                    holder.statusView.setVisibility(View.VISIBLE);
                } else {
                    holder.statusView.setVisibility(View.GONE);
                }
            } else {
                if ((message.flags & MessagingPlugin.FLAG_LOCKED) == MessagingPlugin.FLAG_LOCKED) {
                    holder.statusView.setImageResource(R.drawable.status_locked);
                } else if (message.hasTempKey) {
                    holder.statusView.setImageDrawable(null);
                } else if (message.recipients_status == MessageMemberStatusSummaryEncoding.ERROR) {
                    holder.statusView.setImageResource(R.drawable.status_red);
                } else if (message.alert_flags >= AlertManager.ALERT_FLAG_RING_5
                    && !mMessagingPlugin.isMessageAckedByMe(message)) {
                    holder.statusView.setImageResource(R.drawable.status_ringing);
                } else if (message.numAcked() != 0) {
                    holder.statusView.setImageResource(R.drawable.status_blue);
                } else if (message.numRecipients() == message.numReceived()) {
                    holder.statusView.setImageResource(R.drawable.status_green);
                } else {
                    // message is on server
                    holder.statusView.setImageResource(R.drawable.status_yellow);
                }
                holder.statusView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            L.bug("unused?");
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            L.bug("unused?");
            return null;
        }

        @Override
        public int getPositionForSection(int sectionIndex) {
            return mSections[sectionIndex].start;
        }

        @Override
        public int getSectionForPosition(int position) {
            for (int i = 0; i < mSections.length; i++) {
                if (mSections[i].start > position) {
                    if (i > 0)
                        return i - 1;
                    return 0;
                }
            }
            return 0;
        }

        @Override
        public Object[] getSections() {
            return mSections;
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mEditing) {
            setEditing(false);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
