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

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.config.AppConstants;
import com.mobicage.rpc.config.LookAndFeelConstants;

import java.util.HashSet;
import java.util.Set;


public class SendMessageContactActivity extends ServiceBoundActivity {

    private FriendsPlugin mFriendsPlugin;
    private HorizontalScrollView mRecipientsScroller;
    private View mRecipientsDivider;
    private LinearLayout mRecipients;
    private ListView mListView;
    private FloatingActionButton mFloatingActionButton;
    private Cursor mCursorFriends = null;

    private Set<String> mFriendRecipients;

    @Override
    protected void onServiceBound() {
        setContentView(R.layout.send_message_contact);
        setActivityName("send_message_contact");
        final int text;
        switch (AppConstants.FRIENDS_CAPTION) {
            case COLLEAGUES:
                text = R.string.select_colleague;
                break;
            case CONTACTS:
                text = R.string.select_contact;
                break;
            case FRIENDS:
            default:
                text = R.string.select_friend;
                break;
        }
        setTitle(text);
        mFriendsPlugin = mService.getPlugin(FriendsPlugin.class);

        mRecipientsScroller = (HorizontalScrollView) findViewById(R.id.recipients_scroller);
        mRecipientsDivider = findViewById(R.id.recipients_divider);
        mRecipients = (LinearLayout) findViewById(R.id.recipients);
        mFloatingActionButton = ((FloatingActionButton) findViewById(R.id.next_step));
        mFloatingActionButton.setImageDrawable(new IconicsDrawable(this, FontAwesome.Icon.faw_arrow_right).color(Color.WHITE).sizeDp(24));

        mFriendRecipients = new HashSet<>();
        initListView();

        mFloatingActionButton.hide();
        mFloatingActionButton.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                if (mFriendRecipients.size() == 0) {
                    return;
                }
                Intent intent = new Intent(SendMessageContactActivity.this, SendMessageMessageActivity.class);
                intent.putExtra(SendMessageMessageActivity.RECIPIENTS, mFriendRecipients.toArray(new String[] {}));
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onServiceUnbound() {

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCursorFriends != null) {
            stopManagingCursor(mCursorFriends);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getWasPaused() && mCursorFriends != null) {
            startManagingCursor(mCursorFriends);
        }
    }

    private void initListView() {
        T.UI();
        mListView = (ListView) findViewById(R.id.friend_list);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                T.UI();
                final Friend friend = (Friend) view.getTag();
                toggleRecipient(view, friend);
            }
        });

        mCursorFriends = mFriendsPlugin.getStore().getUserFriendListCursor();
        startManagingCursor(mCursorFriends);
        FriendListAdapter fla = new FriendListAdapter(this, mCursorFriends, mFriendsPlugin.getStore(), null, mFriendsPlugin, false, null);
        mListView.setAdapter(fla);
    }

    private void toggleRecipient(final View view, final Friend friend) {
        if (mFriendRecipients.contains(friend.email)) {
            removeRecipient(view, friend);
        } else {
            addRecipient(view, friend);
        }
    }

    private void addRecipient(final View view, final Friend friend) {
        mFriendRecipients.add(friend.email);

        getLayoutInflater().inflate(R.layout.new_message_recipient, mRecipients);
        final LinearLayout recipient = (LinearLayout) mRecipients.getChildAt(mRecipients.getChildCount() - 1);

        recipient.setTag(friend.email);
        recipient.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                toggleRecipient(view, friend);
            }
        });

        ImageView avatar = (ImageView) recipient.findViewById(R.id.friend_avatar);
        final Bitmap avatarBitmap = mFriendsPlugin.getAvatarBitmap(friend.email);
        if (avatarBitmap == null) {
            avatar.setImageBitmap(mFriendsPlugin.getMissingFriendAvatarBitmap());
        } else {
            avatar.setImageBitmap(avatarBitmap);
        }

        final ImageView friendRemove = ((ImageView) recipient.findViewById(R.id.friend_status));
        friendRemove.setImageDrawable(new IconicsDrawable(this, FontAwesome.Icon.faw_times).color(Color.WHITE).sizeDp(12));

        final ImageView friendAdded = ((ImageView) view.findViewById(R.id.friend_status));
        friendAdded.setImageDrawable(new IconicsDrawable(this, FontAwesome.Icon.faw_check).color(Color.WHITE).sizeDp(12));
        friendAdded.setVisibility(View.VISIBLE);

        // Colours
        UIUtils.setBackgroundColor(friendAdded, LookAndFeelConstants.getPrimaryColor(this));

        TextView name = (TextView) recipient.findViewById(R.id.friend_name);
        name.setText(friend.name);
        mRecipientsScroller.smoothScrollTo(mRecipients.getWidth(), 0);
        mRecipientsScroller.setVisibility(View.VISIBLE);
        mRecipientsDivider.setVisibility(View.VISIBLE);
        mFloatingActionButton.show();
    }

    private void removeRecipient(final View view, final Friend friend) {
        mFriendRecipients.remove(friend.email);

        View avatar = mRecipients.findViewWithTag(friend.email);
        mRecipients.removeView(avatar);

        final ImageView friendAdded = ((ImageView) view.findViewById(R.id.friend_status));
        friendAdded.setVisibility(View.GONE);

        mRecipientsScroller.smoothScrollTo(mRecipients.getWidth(), 0);
        if (mFriendRecipients.size() == 0) {
            mRecipientsScroller.setVisibility(View.GONE);
            mRecipientsDivider.setVisibility(View.GONE);
            mFloatingActionButton.hide();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        T.UI();
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.send_message_contact_menu, menu);

        switch (AppConstants.FRIENDS_CAPTION) {
            case COLLEAGUES:
                menu.getItem(0).setTitle(R.string.find_colleagues);
                break;
            case CONTACTS:
                menu.getItem(0).setTitle(R.string.find_contacts);
                break;
            case FRIENDS:
            default:
                menu.getItem(0).setTitle(R.string.invite_friends_short);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        T.UI();

        switch (item.getItemId()) {
            case R.id.find_friends:
                startActivity(new Intent(this, AddFriendsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
