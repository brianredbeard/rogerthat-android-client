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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.ServiceActionMenuActivity;
import com.mobicage.rogerthat.util.Security;
import com.mobicage.rogerthat.util.system.T;

public class ServiceActionsOfflineActivity extends FriendsActivity {

    public static final String ACTION = "action";
    protected String mAction;

    @Override
    protected boolean useAppBar() {
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mAction = intent.getStringExtra(ACTION);
    }

    @Override
    protected int getLayout() {
        return R.layout.servicefriends;
    }

    @Override
    protected void createCursor() {
        setCursor(mFriendsPlugin.getStore().getServiceActionsListCursor(Security.sha256Lower(mAction)));
    }

    @Override
    protected void changeCursor() {
        if (mServiceIsBound) {
            createCursor();
            ((FriendListAdapter) getListAdapter()).changeCursor(getCursor());
        }
    }

    @Override
    protected String getHelpMessage() {
        return getString(R.string.servicefriends_getting_started_msg, getString(R.string.discover_services_short));
    }

    @Override
    protected Class<? extends FriendDetailActivity> getDetailClass() {
        return ServiceDetailActivity.class;
    }

    @Override
    protected void onServiceBound() {
        super.onServiceBound();
        setActivityName("action|" + mAction);
        setTitle(mAction); // todo ruben name for the tag
    }

    @Override
    protected void loadCursorAndSetAdaptar() {
        super.loadCursorAndSetAdaptar();
    }

    @Override
    protected void onListItemClick(ListView listView, final View listItem, int position, long id) {
        T.UI();
        Friend friend = (Friend) listItem.getTag();
        Intent intent = new Intent(this, ServiceActionMenuActivity.class);
        intent.putExtra(ServiceActionMenuActivity.SERVICE_EMAIL, friend.email);
        intent.putExtra(ServiceActionMenuActivity.MENU_PAGE, 0);
        startActivity(intent);
    }

    @Override
    protected CharSequence getHeaderCellMainText() {
        return null;
    }

    @Override
    protected CharSequence getHeaderCellSubText() {
        return null;
    }
}
