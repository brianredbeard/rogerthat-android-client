/*
 * Copyright 2017 Mobicage NV
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
 * @@license_version:1.2@@
 */
package com.mobicage.rogerthat;

import android.app.AlertDialog;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.friends.Group;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;

public abstract class FriendsActivity extends ServiceBoundCursorListActivity {

    protected FriendsPlugin mFriendsPlugin;

    protected abstract int getLayout();

    protected abstract CharSequence getHeaderCellMainText();

    protected abstract CharSequence getHeaderCellSubText();

    protected boolean useAppBar() {
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        T.UI();
        super.onCreate(savedInstanceState);
        if (useAppBar()) {
            setContentView(getLayout());
        } else {
            setContentView(getLayout());
        }

        final ListView listView = (ListView) findViewById(R.id.friend_list);
        setListView(listView);

        final CharSequence headerMainText = getHeaderCellMainText();
        if (headerMainText != null) {
            View view = getLayoutInflater().inflate(R.layout.main_list_header, null);
            ((TextView) view.findViewById(R.id.mainheader)).setText(headerMainText);
            ((TextView) view.findViewById(R.id.subheader)).setText(getHeaderCellSubText());
            getListView().addHeaderView(view);
        } else {
            View headerView = getHeaderView();
            if (headerView != null) {
                getListView().addHeaderView(headerView);
            }
        }
    }

    protected View getHeaderView() {
        // if getHeaderCellMainText returns null, then this method can return a view which will be used as header
        return null;
    }

    protected abstract Class<? extends FriendDetailActivity> getDetailClass();

    @Override
    protected boolean onListItemLongClick(ListView l, View v, int position, long id) {
        Object tag = v.getTag();
        if (tag == null) {
            L.d("LongClicked on view which refers to deleted friend/group");
        } else if (tag instanceof Friend) {
            final Friend friend = (Friend) tag;
            if (friend.category != null && friend.category.friendCount > 1) {
                return false;
            }
            if (SystemUtils.isFlagEnabled(friend.flags, FriendsPlugin.FRIEND_NOT_REMOVABLE)) {
                return false;
            }
            mFriendsPlugin.removeFriendFromList(this, friend);
            return true;
        } else if (tag instanceof Group) {
            final Group group = (Group) tag;
            mFriendsPlugin.removeGroupFromList(this, group);
            return true;
        }
        return false;
    }

    @Override
    protected String[] getAllReceivingIntents() {
        return new String[] { FriendsPlugin.FRIEND_UPDATE_INTENT, FriendsPlugin.FRIEND_AVATAR_CHANGED_INTENT,
            FriendsPlugin.FRIEND_REMOVED_INTENT, FriendsPlugin.FRIEND_MARKED_FOR_REMOVAL_INTENT,
            FriendsPlugin.FRIEND_ADDED_INTENT, FriendsPlugin.FRIENDS_LIST_REFRESHED };
    }

    @Override
    protected void onServiceBound() {
        T.UI();
        mFriendsPlugin = mService.getPlugin(FriendsPlugin.class);

        IntentFilter filter = new IntentFilter();
        for (String action : getAllReceivingIntents())
            filter.addAction(action);
        registerReceiver(getDefaultBroadcastReceiver(), filter);

        loadCursorAndSetAdaptar();
    }

    protected void loadCursorAndSetAdaptar() {
        createCursor();
        startManagingCursor(getCursor());

        setListAdapter(new FriendListAdapter(this, getCursor(), mFriendsPlugin.getStore(), null, mFriendsPlugin, true, null));
    }

    protected abstract void createCursor();

    protected abstract String getHelpMessage();

    protected void showHelp() {
        T.UI();
        new AlertDialog.Builder(FriendsActivity.this).setTitle(R.string.friends_getting_started_title)
            .setMessage(getHelpMessage()).setPositiveButton(getString(R.string.ok), null).create().show();
    }

    @Override
    protected void onServiceUnbound() {
        unregisterReceiver(getDefaultBroadcastReceiver());
    }

}
