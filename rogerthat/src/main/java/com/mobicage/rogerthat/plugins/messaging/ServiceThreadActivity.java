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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.IdentityStore;
import com.mobicage.rogerthat.ServiceBoundCursorListActivity;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.messaging.widgets.Widget;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeDialogInterfaceOnClickListener;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.time.TimeUtils;
import com.mobicage.rogerthat.util.ui.Slider;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.to.messaging.ButtonTO;
import com.mobicage.to.messaging.MemberStatusTO;

import java.util.Arrays;
import java.util.Map;

public class ServiceThreadActivity extends ServiceBoundCursorListActivity {

    private final static String HINT_SWIPE = "com.mobicage.rogerthat.plugins.messaging.ServiceThreadActivity.HINT_SWIPE";

    public final static String SCROLL_TO_BOTTOM = "scroll_to_bottom";
    public static final String PARENT_MESSAGE_KEY = "parent_message_key";
    private String mParentMessageKey;
    private MessagingPlugin mMessagingPlugin;
    private FriendsPlugin mFriendsPlugin;
    private MessageStore mMessageStore;
    private String mMyEmail;
    private GestureDetector mGestureScanner;

    private Scroller mScroller;

    public static Intent createIntent(Context context, String parentMessageKey, String memberFilter) {
        return createIntent(context, parentMessageKey, memberFilter, false);
    }

    public static Intent createIntent(Context context, String parentMessageKey, String memberFilter,
        boolean scrollToBottom) {
        Intent intent = new Intent(context, ServiceThreadActivity.class);
        intent.putExtra(ServiceThreadActivity.PARENT_MESSAGE_KEY, parentMessageKey);
        intent.putExtra(ServiceThreadActivity.SCROLL_TO_BOTTOM, scrollToBottom);
        intent.putExtra(MessagingPlugin.MEMBER_FILTER, memberFilter);
        return intent;
    }

    private final BroadcastReceiver mReceiver = new SafeBroadcastReceiver() {
        @Override
        public String[] onSafeReceive(Context context, Intent intent) {
            if (MessagingPlugin.THREAD_DELETED_INTENT.equals(intent.getAction())) {
                if (mParentMessageKey.equals(intent.getStringExtra("key"))) {
                    finish();
                    return new String[] { intent.getAction() };
                }
            }
            return null;
        }
    };

    @Override
    protected String[] getAllReceivingIntents() {
        return new String[] { MessagingPlugin.MESSAGE_MEMBER_STATUS_UPDATE_RECEIVED_INTENT,
            MessagingPlugin.MESSAGE_LOCKED_INTENT, MessagingPlugin.MESSAGE_PROCESSED_INTENT,
            MessagingPlugin.NEW_MESSAGE_RECEIVED_INTENT, FriendsPlugin.FRIEND_AVATAR_CHANGED_INTENT,
            FriendsPlugin.FRIEND_UPDATE_INTENT, IdentityStore.IDENTITY_CHANGED_INTENT,
            FriendsPlugin.FRIENDS_LIST_REFRESHED };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentViewWithoutNavigationBar(R.layout.service_thread);
        mParentMessageKey = getIntent().getStringExtra(PARENT_MESSAGE_KEY);
        setListView((ListView) findViewById(R.id.thread_messages));

        mScroller = Scroller.getInstance();
        ListView listView = getListView();
        mScroller.setListView(listView);

        final IntentFilter filter = new IntentFilter();
        for (String action : getAllReceivingIntents())
            filter.addAction(action);
        registerReceiver(getDefaultBroadcastReceiver(), filter);
        registerReceiver(mReceiver, new IntentFilter(MessagingPlugin.THREAD_DELETED_INTENT));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(getDefaultBroadcastReceiver());
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        T.UI();
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.thread_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.delete_conversation:
            mMessagingPlugin.removeConversationFromList(this, mParentMessageKey);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final Object tag = v.getTag();
        if (tag != null) {
            final Message message = (Message) tag;
            mMessagingPlugin
                .showMessage(this, message, true, getIntent().getStringExtra(MessagingPlugin.MEMBER_FILTER));
        }
    }

    @Override
    protected boolean onListItemLongClick(ListView l, View v, int position, long id) {
        return false;
    }

    @Override
    protected void onServiceBound() {
        mMessagingPlugin = mService.getPlugin(MessagingPlugin.class);
        mFriendsPlugin = mService.getPlugin(FriendsPlugin.class);
        mMessageStore = mMessagingPlugin.getStore();
        mMyEmail = mService.getIdentityStore().getIdentity().getEmail();
        createCursor();
        startManagingCursor(getCursor());
        setListAdapter(new ServiceThreadAdapter(this, getCursor()));

        String memberFilter = getIntent().getStringExtra(MessagingPlugin.MEMBER_FILTER);
        mGestureScanner = new GestureDetector(new Slider(this, this, new LeftSwiper(this, mMessagingPlugin,
            mParentMessageKey, memberFilter), new RightSwiper(this, mMessagingPlugin, mParentMessageKey, memberFilter)));

        ListView listView = getListView();

        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra(SCROLL_TO_BOTTOM, false)) {
            listView.setSelection(getCursor().getCount() - 1);
        }

        UIUtils.showHint(this, mService, HINT_SWIPE, R.string.hint_swipe);
    }


    private void createCursor() {
        if (getCursor() != null) {
            stopManagingCursor(getCursor());
        }
        setCursor(mMessageStore.getFullServiceThreadCursor(mParentMessageKey));
    }

    @Override
    protected void changeCursor() {
        if (getCursor() == null)
            return;
        final int lastPosition = getListView().getLastVisiblePosition();
        final int oldCount = getCursor().getCount();

        if (mServiceIsBound) {
            createCursor();
            ((CursorAdapter) getListAdapter()).changeCursor(getCursor());
        }
        final int count = getCursor().getCount();
        if (lastPosition == oldCount - 1) {
            mService.postDelayedOnUIHandler(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    scrollToPosition(count - 1);
                }
            }, 500);
        }
    }

    @Override
    protected void onServiceUnbound() {
    }

    private void scrollToPosition(int position) {
        mScroller.scrollToPosition(position);
    }

    private class ServiceThreadAdapter extends CursorAdapter {

        private final LayoutInflater mInflator;

        public ServiceThreadAdapter(Context context, Cursor c) {
            super(context, c, false);
            mInflator = getLayoutInflater();
        }

        @Override
        public void bindView(View view, Context context, Cursor c) {
            populateView(context, c, view);
        }

        @Override
        public View newView(Context context, Cursor c, ViewGroup viewGroup) {
            View view = mInflator.inflate(R.layout.service_thread_message, null);
            populateView(context, c, view);
            return view;
        }

        private void populateView(Context context, Cursor cursor, View view) {
            // Get message intelligence
            final Message message = mMessageStore.readFullThreadMessageFromCursor(cursor);
            final int position = cursor.getPosition();

            final LinearLayout svcDetail = (LinearLayout) view.findViewById(R.id.svc_detail);
            final ImageView avatarView = (ImageView) svcDetail.findViewById(R.id.avatar);
            final TextView recipientsView = (TextView) svcDetail.findViewById(R.id.recipients);

            if (position == 0) {
                avatarView.setImageBitmap(mFriendsPlugin.getAvatarBitmap(message.sender));
                avatarView.setVisibility(View.VISIBLE);
                recipientsView.setText(message.recipients);
                recipientsView.setVisibility(View.VISIBLE);
                svcDetail.setOnClickListener(new SafeViewOnClickListener() {
                    @Override
                    public void safeOnClick(View v) {
                        final int contactType = mFriendsPlugin.getContactType(message.sender);
                        if ((contactType & FriendsPlugin.FRIEND) == FriendsPlugin.FRIEND) {
                            mFriendsPlugin.launchDetailActivity(ServiceThreadActivity.this, message.sender);
                        } else {
                            if ((contactType & FriendsPlugin.NON_FRIEND) == FriendsPlugin.NON_FRIEND) {
                                new AlertDialog.Builder(ServiceThreadActivity.this)
                                    .setMessage(getString(R.string.invite_as_friend, new Object[] { message.sender }))
                                    .setPositiveButton(R.string.yes, new SafeDialogInterfaceOnClickListener() {
                                        @Override
                                        public void safeOnClick(DialogInterface dialog, int which) {
                                            mFriendsPlugin.inviteFriend(message.sender, null, null, true);
                                        }
                                    }).setNegativeButton(R.string.no, null).create().show();
                            }
                        }
                    }
                });
            } else {
                svcDetail.setOnClickListener(null);
                svcDetail.setVisibility(View.GONE);
                avatarView.setVisibility(View.GONE);
                recipientsView.setVisibility(View.GONE);
            }

            final TextView timestamp = (TextView) view.findViewById(R.id.timestamp);
            timestamp.setText(TimeUtils.getHumanTime(ServiceThreadActivity.this, message.timestamp * 1000, false));

            final TextView messageView = (TextView) view.findViewById(R.id.message);

            final String lines[] = message.message.split("[\\r\\n]+");
            int keepNumLines = 3;
            if (lines.length < 3)
                keepNumLines = lines.length;
            final StringBuilder shortMessage = new StringBuilder();
            for (int i = 0; i < keepNumLines; i++) {
                shortMessage.append(lines[i]);
                if (i < keepNumLines - 1)
                    shortMessage.append('\n');
            }

            messageView.setText(shortMessage);

            final TextView myStatus = (TextView) view.findViewById(R.id.my_status);
            String status = null;
            for (MemberStatusTO ms : message.members) {
                if (mMyEmail.equals(ms.member)
                    && (ms.status & MessagingPlugin.STATUS_ACKED) == MessagingPlugin.STATUS_ACKED) {
                    if (ms.button_id == null)
                        status = "Roger that!";
                    else {
                        if (message.form != null && Message.POSITIVE.equals(ms.button_id)) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> widget = (Map<String, Object>) message.form.get("widget");
                            String formType = (String) message.form.get("type");
                            status = Widget.valueString(context, formType, widget);
                            if (status != null) {
                                final String[] splitted = status.split("\n");
                                if (splitted.length > 2) {
                                    status = TextUtils.join("\n", Arrays.copyOfRange(splitted, 0, 2)) + "...";
                                }
                            }
                        }
                        if (status == null || status.length() == 0) {
                            for (ButtonTO button : message.buttons) {
                                if (button.id.equals(ms.button_id)) {
                                    status = button.caption;
                                    break;
                                }
                            }
                        }
                    }
                    break;
                }
            }
            if (status == null) {
                myStatus.setVisibility(View.GONE);
            } else {
                myStatus.setVisibility(View.VISIBLE);
                myStatus.setText(status.trim());
            }

            view.setTag(message);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        if (mGestureScanner != null) {
            mGestureScanner.onTouchEvent(e);
        }
        return super.dispatchTouchEvent(e);
    }

}
