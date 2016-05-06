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
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendStore;
import com.mobicage.rogerthat.plugins.friends.ServiceActionMenuActivity;
import com.mobicage.rogerthat.plugins.friends.ServiceSearchActivity;
import com.mobicage.rogerthat.util.system.SafeDialogInterfaceOnClickListener;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.config.AppConstants;

public class ServiceFriendsActivity extends FriendsActivity {

    public static final String ORGANIZATION_TYPE = "organization_type";
    protected Integer mOrganizationType;
    protected Integer mOrganizationTypeStringId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mOrganizationType = intent.getIntExtra(ORGANIZATION_TYPE, FriendStore.SERVICE_ORGANIZATION_TYPE_UNSPECIFIED);
        if (mOrganizationType == FriendStore.SERVICE_ORGANIZATION_TYPE_NON_PROFIT) {
            mOrganizationTypeStringId = R.string.associations;
        } else if (mOrganizationType == FriendStore.SERVICE_ORGANIZATION_TYPE_PROFIT) {
            mOrganizationTypeStringId = R.string.merchants;
        } else if (mOrganizationType == FriendStore.SERVICE_ORGANIZATION_TYPE_CITY) {
            mOrganizationTypeStringId = R.string.community_service;
        } else if (mOrganizationType == FriendStore.SERVICE_ORGANIZATION_TYPE_EMERGENCY) {
            mOrganizationTypeStringId = R.string.care;
        } else {
            mOrganizationTypeStringId = R.string.tab_services;
        }
    }

    @Override
    protected int getLayout() {
        return R.layout.servicefriends;
    }

    @Override
    protected void createCursor() {
        if (mOrganizationType == null || mOrganizationType == FriendStore.SERVICE_ORGANIZATION_TYPE_UNSPECIFIED) {
            setCursor(mFriendsPlugin.getStore().getServiceFriendListCursor());
        } else {
            setCursor(mFriendsPlugin.getStore().getServiceFriendListCursor(mOrganizationType));
        }
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
        setNavigationBarVisible(AppConstants.SHOW_NAV_HEADER);
        setNavigationBarTitle(mOrganizationTypeStringId);

        findViewById(R.id.navigation_bar_home_button).setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                Intent i = new Intent(ServiceFriendsActivity.this, HomeActivity.class);
                i.setFlags(MainActivity.FLAG_CLEAR_STACK);
                startActivity(i);
                finish();
            }
        });
    }

    @Override
    protected void loadCursorAndSetAdaptar() {
        super.loadCursorAndSetAdaptar();
        if (((FriendListAdapter) mListAdapter).getCount() == 0) {
            boolean found = false;
            for (int i = 0; i < AppConstants.SEARCH_SERVICES_IF_NONE_CONNECTED.length; i++) {
                if (AppConstants.SEARCH_SERVICES_IF_NONE_CONNECTED[i] == mOrganizationType) {
                    found = true;
                    break;
                }
            }
            if (found) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ServiceFriendsActivity.this);
                builder.setCancelable(true);

                builder.setTitle(mOrganizationTypeStringId);
                builder.setMessage(getString(R.string.search_services_if_none_connected_message,
                    getString(mOrganizationTypeStringId)));
                builder.setNegativeButton(R.string.no, new SafeDialogInterfaceOnClickListener() {
                    @Override
                    public void safeOnClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setPositiveButton(R.string.yes, new SafeDialogInterfaceOnClickListener() {
                    @Override
                    public void safeOnClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        final Intent serviceSearch = new Intent(ServiceFriendsActivity.this,
                            ServiceSearchActivity.class);
                        serviceSearch.putExtra(ServiceSearchActivity.ORGANIZATION_TYPE, mOrganizationType);
                        startActivity(serviceSearch);
                    }
                });
                builder.create().show();
            }
        }
    }

    @Override
    protected void onListItemClick(ListView listView, final View listItem, int position, long id) {
        T.UI();

        if (position == 0) {
            // tapped header cell
            onHeaderTapped();
            return;
        }

        Friend friend = (Friend) listItem.getTag();
        if (friend.category != null && friend.category.friendCount > 1) {
            Intent intent = new Intent(this, FriendCategoryActivity.class);
            intent.putExtra(FriendCategoryActivity.FRIEND_CATEGORY_ID, friend.category.id);
            intent.putExtra(FriendCategoryActivity.FRIEND_CATEGORY_NAME, friend.category.name);
            startActivity(intent);
        } else if (friend.existenceStatus == Friend.ACTIVE) {
            Intent intent = new Intent(this, ServiceActionMenuActivity.class);
            intent.putExtra(ServiceActionMenuActivity.SERVICE_EMAIL, friend.email);
            intent.putExtra(ServiceActionMenuActivity.MENU_PAGE, 0);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, ServiceDetailActivity.class);
            intent.putExtra(ServiceDetailActivity.EXISTENCE, friend.existenceStatus);
            intent.putExtra(ServiceDetailActivity.EMAIL, friend.email);
            startActivity(intent);
        }
    }

    protected void onHeaderTapped() {
        final Intent serviceSearch = new Intent(this, ServiceSearchActivity.class);
        serviceSearch.putExtra(ServiceSearchActivity.ORGANIZATION_TYPE, mOrganizationType);
        startActivity(serviceSearch);
    }

    @Override
    protected boolean showFABMenu() {
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        T.UI();
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.services_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        T.UI();

        switch (item.getItemId()) {
        case R.id.help:
            showHelp();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected CharSequence getHeaderCellMainText() {
        return getString(R.string.discover_services_short, getString(R.string.app_name));
    }

    @Override
    protected CharSequence getHeaderCellSubText() {
        return getString(R.string.discover_services_long, getString(R.string.app_name));
    }

}