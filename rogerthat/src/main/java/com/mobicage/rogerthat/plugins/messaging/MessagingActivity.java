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

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.HomeActivity;
import com.mobicage.rogerthat.IdentityStore;
import com.mobicage.rogerthat.MainActivity;
import com.mobicage.rogerthat.SendMessageContactActivity;
import com.mobicage.rogerthat.ServiceBoundActivity;
import com.mobicage.rogerthat.ServiceBoundCursorListActivity;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.messaging.MessageStore.CursorSet;
import com.mobicage.rogerthat.plugins.scan.ProcessScanActivity;
import com.mobicage.rogerthat.util.ActivityUtils;
import com.mobicage.rogerthat.util.RegexPatterns;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.time.TimeUtils;
import com.mobicage.rpc.config.AppConstants;
import com.mobicage.to.messaging.AttachmentTO;
import com.mobicage.to.messaging.ButtonTO;
import com.mobicage.to.messaging.MemberStatusTO;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MessagingActivity extends ServiceBoundCursorListActivity {

    private static final long sGMTOffsetMillis = TimeUtils.getGMTOffsetMillis();

    // Owned by UI thread
    private FloatingActionButton mFloatingActionButton;
    private MessagingPlugin mMessagingPlugin;
    private FriendsPlugin mFriendsPlugin;
    private String mMemberFilter;
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

            ((CursorAdapter) getListAdapter()).notifyDataSetChanged();
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
        super.onCreate(savedInstanceState);
        mResources = getResources();
    }

    private void processMessageUpdatesIntent(final Intent intent) {
        final String url = intent.getDataString();
        if (intent.getBooleanExtra(HomeActivity.INTENT_PROCESSED, false))
            return;
        if (url != null) {
            ActivityUtils.goToMessagingActivity(this, false);
            processUrl(url);
        } else if (intent.hasExtra(HomeActivity.INTENT_KEY_LAUNCHINFO)) {
            String value = intent.getStringExtra(HomeActivity.INTENT_KEY_LAUNCHINFO);
            if (HomeActivity.INTENT_VALUE_SHOW_FRIENDS.equals(value)) {
                // goToUserFriendsActivity();

            } else if (HomeActivity.INTENT_VALUE_SHOW_MESSAGES.equals(value)) {
                ActivityUtils.goToMessagingActivity(this, false);

            } else if (HomeActivity.INTENT_VALUE_SHOW_NEW_MESSAGES.equals(value)) {
                if (intent.hasExtra(HomeActivity.INTENT_KEY_MESSAGE)) {
                    String messageKey = intent.getStringExtra(HomeActivity.INTENT_KEY_MESSAGE);
                    goToMessageDetail(messageKey);
                } else {
                    ActivityUtils.goToMessagingActivity(this, false);
                }

            } else if (HomeActivity.INTENT_VALUE_SHOW_UPDATED_MESSAGES.equals(value)) {
                if (intent.hasExtra(HomeActivity.INTENT_KEY_MESSAGE)) {
                    String messageKey = intent.getStringExtra(HomeActivity.INTENT_KEY_MESSAGE);
                    goToMessageDetail(messageKey);
                } else {
                    ActivityUtils.goToMessagingActivity(this, false);
                }

            } else if (HomeActivity.INTENT_VALUE_SHOW_SCANTAB.equals(value)) {
                ActivityUtils.goToScanActivity(this, false);
            } else {
                L.bug("Unexpected (key, value) for HomeActivity intent: (" + HomeActivity.INTENT_KEY_LAUNCHINFO + ", " + value + ")");
            }
        }
        intent.putExtra(HomeActivity.INTENT_PROCESSED, true);
    }

    private void processUrl(final String url) {
        T.UI();
        if (RegexPatterns.OPEN_HOME_URL.matcher(url).matches())
            return;

        if (RegexPatterns.FRIEND_INVITE_URL.matcher(url).matches()
                || RegexPatterns.SERVICE_INTERACT_URL.matcher(url).matches()) {
            final Intent launchIntent = new Intent(this, ProcessScanActivity.class);
            launchIntent.putExtra(ProcessScanActivity.URL, url);
            launchIntent.putExtra(ProcessScanActivity.SCAN_RESULT, false);
            startActivity(launchIntent);
        }
    }

    private void goToMessageDetail(final String messageKey) {
        Message message = mMessagingPlugin.getStore().getPartialMessageByKey(messageKey);
        mMessagingPlugin.showMessage(this, message, null);
    }

    @Override
    protected String[] getAllReceivingIntents() {
        return new String[]{MessagingPlugin.NEW_MESSAGE_RECEIVED_INTENT,
                MessagingPlugin.MESSAGE_MEMBER_STATUS_UPDATE_RECEIVED_INTENT, MessagingPlugin.MESSAGE_DIRTY_CLEANED_INTENT,
                MessagingPlugin.MESSAGE_LOCKED_INTENT,
                MessagingPlugin.MESSAGE_PROCESSED_INTENT, MessagingPlugin.MESSAGE_FAILURE,
                MessagingPlugin.MESSAGE_THREAD_VISIBILITY_CHANGED_INTENT, MessagingPlugin.THREAD_DELETED_INTENT,
                MessagingPlugin.THREAD_RECOVERED_INTENT, MessagingPlugin.THREAD_MODIFIED_INTENT,
                FriendsPlugin.FRIEND_AVATAR_CHANGED_INTENT, IdentityStore.IDENTITY_CHANGED_INTENT,
                FriendsPlugin.FRIENDS_LIST_REFRESHED};
    }

    @Override
    protected void onServiceBound() {
        T.UI();
        mMessagingPlugin = mService.getPlugin(MessagingPlugin.class);
        mFriendsPlugin = mService.getPlugin(FriendsPlugin.class);
        mMyEmail = mService.getIdentityStore().getIdentity().getEmail();

        Intent intent = getIntent();
        if (intent != null) {
            final String intentAction = intent.getAction();
            if (MainActivity.ACTION_NOTIFICATION_MESSAGE_UPDATES.equals(intentAction)) {
                processMessageUpdatesIntent(intent);
            } else {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    mMemberFilter = extras.getString(MessagingPlugin.MEMBER_FILTER);
                }
            }
        }

        if (mMemberFilter == null) {
            mMessagingPlugin.inboxOpened();
            setContentView(R.layout.messaging);
            setActivityName("messages");
        } else {
            setContentView(R.layout.messaging);
            setNavigationBarBurgerVisible(false, true);
            setActivityName("messages_filter");
        }

        setListView((ListView) findViewById(R.id.message_list));

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

        mFloatingActionButton = ((FloatingActionButton) findViewById(R.id.add));
        mFloatingActionButton.setImageDrawable(new IconicsDrawable(this, FontAwesome.Icon.faw_plus).color(Color.WHITE).sizeDp(24));

        mFloatingActionButton.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                showSendMessageActivity();
            }
        });

        if (!AppConstants.FRIENDS_ENABLED || mMemberFilter != null) {
            mFloatingActionButton.hide();
        }

        if (mMemberFilter == null) {
            setTitle(R.string.tab_messaging);
        } else {
            setTitle(R.string.message_history);
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


    }

    @Override
    protected void onServiceUnbound() {
        unregisterReceiver(getDefaultBroadcastReceiver());
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        T.UI();
        final Object tag = v.getTag();
        if (tag != null) {
            final ViewInfoHolder holder = (ViewInfoHolder) tag;
            final Message message = holder.message;
            mMessagingPlugin.showMessage(this, message, mMemberFilter);
        }
    }

    @Override
    protected boolean onListItemLongClick(ListView l, View v, int position, long id) {
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

    private void showSendMessageActivity() {
        long currentTime = System.currentTimeMillis();
        if (getLastTimeClicked() != 0
                && (currentTime < (getLastTimeClicked() + ServiceBoundActivity.DOUBLE_CLICK_TIMESPAN))) {
            L.d("ignoring click on send message");
            return;
        }

        setLastTimeClicked(currentTime);
        
        Intent intent = new Intent(this, SendMessageContactActivity.class);
        startActivity(intent);
    }

    private void setEditing(boolean editing) {
        mEditing = editing;
        if (AppConstants.FRIENDS_ENABLED) {
            if (editing) {
                mFloatingActionButton.hide();
            } else {
                mFloatingActionButton.show();
            }
        }

        invalidateOptionsMenu();
        mToBeDeleted = mEditing ? new HashSet<String>() : null;

        findViewById(R.id.delete_messages).setVisibility(mEditing ? View.VISIBLE : View.GONE);

        ((CursorAdapter) getListAdapter()).notifyDataSetChanged();
    }

    // Object put in view tag. Contains fast references to message and subviews
    private class ViewInfoHolder {
        Message message;
        TextView recipientsView;
        TextView timestampView;
        TextView messageView;
        TextView messageCountView;
        ImageView avatarView;
        CheckBox checkBox;
        ImageView statusView;
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
            int pos = 0;
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
                holder.messageCountView = (TextView) view.findViewById(R.id.message_count);
                holder.checkBox = (CheckBox) view.findViewById(R.id.message_checkbox);
                holder.statusView = (ImageView) view.findViewById(R.id.message_status);
                view.setTag(holder);
            }
            holder.message = message;

            final boolean dynamicChat = SystemUtils.isFlagEnabled(message.flags, MessagingPlugin.FLAG_DYNAMIC_CHAT);
            setMessageOnView(view, holder, dynamicChat);

            return view;
        }

        private void setMessageOnView(final View view, ViewInfoHolder holder, boolean dynamicChat) {

            final Message message = holder.message;

            final ImageView avatarView = holder.avatarView;

            boolean shouldShowStatusIcon = true;

            final Bitmap threadAvatar;
            if (message.thread_avatar_hash != null
                    && (threadAvatar = mMessagingPlugin.getStore().getThreadAvatar(message.thread_avatar_hash)) != null) {
                avatarView.setImageBitmap(threadAvatar);
            } else if (dynamicChat) {
                avatarView.setImageResource(R.drawable.group_60);
            } else if (FriendsPlugin.SYSTEM_FRIEND.equals(message.sender)
                    || mFriendsPlugin.getStore().getFriendType(message.sender) == FriendsPlugin.FRIEND_TYPE_SERVICE) {
                shouldShowStatusIcon = false;
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

            long messageUnreadCount = message.unreadCount;
            if (dynamicChat && replyCount < messageUnreadCount) {
                messageUnreadCount--;
            }
            if (messageUnreadCount >= 1) {
                messageCountView.setText(String.valueOf(messageUnreadCount));
                messageCountView.setVisibility(View.VISIBLE);
            } else {
                messageCountView.setVisibility(View.GONE);
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
            boolean showMessageStatus = messageUnreadCount == 0;
            if (showMessageStatus) {
                showMessageStatus = shouldShowStatusIcon && setStatusIcon(MessagingActivity.this, message, holder.statusView, dynamicChat);
            }
            holder.statusView.setVisibility(showMessageStatus ? View.VISIBLE : View.GONE);
        }

        private boolean setStatusIcon(Context context, Message message, ImageView statusView, boolean dynamicChat) {

            Resources resources = getResources();
            if (dynamicChat) {
                if (message.priority == Message.PRIORITY_URGENT_WITH_ALARM) {
                    int primaryColor = resources.getColor(R.color.mc_gray_11);
                    statusView.setImageDrawable(new IconicsDrawable(context, FontAwesome.Icon.faw_bell).color(primaryColor).sizeDp(15));
                    return true;
                } else {
                    return false;
                }
            } else {
                if (!mMyEmail.equals(message.sender))
                    return false;

                if (message.recipients_status == MessageMemberStatusSummaryEncoding.ERROR) {
                    int errorColor = resources.getColor(R.color.mc_error);
                    statusView.setImageDrawable(new IconicsDrawable(context, FontAwesome.Icon.faw_exclamation).color(errorColor).sizeDp(15));
                } else if (message.alert_flags >= AlertManager.ALERT_FLAG_RING_5
                        && !mMessagingPlugin.isMessageAckedByMe(message)) {
                    int rogerthatColor = resources.getColor(R.color.mc_green);
                    statusView.setImageDrawable(new IconicsDrawable(context, FontAwesome.Icon.faw_bell).color(rogerthatColor).sizeDp(15));
                } else if (message.numAcked() != 0) {
                    int rogerthatColor = resources.getColor(R.color.mc_green);
                    statusView.setImageDrawable(new IconicsDrawable(context, FontAwesome.Icon.faw_check).color(rogerthatColor).sizeDp(15));
                } else if (message.numReceived() != 0) {
                    int blueColor = resources.getColor(R.color.mc_timestamp_blue);
                    statusView.setImageDrawable(new IconicsDrawable(context, FontAwesome.Icon.faw_check).color(blueColor).sizeDp(15));
                } else {
                    int greyColor = resources.getColor(R.color.mc_gray_11);
                    statusView.setImageDrawable(new IconicsDrawable(context, FontAwesome.Icon.faw_paper_plane).color(greyColor).sizeDp(15));
                }
                return true;
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
