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
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendStore;
import com.mobicage.rogerthat.plugins.friends.ServiceActionMenuActivity;
import com.mobicage.rogerthat.plugins.friends.ServiceSearchActivity;
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
        TextView noServicesTextView = (TextView) findViewById(R.id.no_services_text);
        mOrganizationType = intent.getIntExtra(ORGANIZATION_TYPE, FriendStore.SERVICE_ORGANIZATION_TYPE_UNSPECIFIED);
        int noServicesStringId;
        if (mOrganizationType == FriendStore.SERVICE_ORGANIZATION_TYPE_NON_PROFIT) {
            mOrganizationTypeStringId = R.string.associations;
            noServicesStringId = R.string.no_associations_found;
            setActivityName("associations");
        } else if (mOrganizationType == FriendStore.SERVICE_ORGANIZATION_TYPE_PROFIT) {
            mOrganizationTypeStringId = R.string.merchants;
            noServicesStringId = R.string.no_merchants_found;
            setActivityName("merchants");
        } else if (mOrganizationType == FriendStore.SERVICE_ORGANIZATION_TYPE_CITY) {
            mOrganizationTypeStringId = R.string.community_service;
            noServicesStringId = R.string.no_community_services_found;
            setActivityName("community_services");
        } else if (mOrganizationType == FriendStore.SERVICE_ORGANIZATION_TYPE_EMERGENCY) {
            mOrganizationTypeStringId = R.string.care;
            noServicesStringId = R.string.no_care_institutions_found;
            setActivityName("care");
        } else {
            mOrganizationTypeStringId = R.string.tab_services;
            noServicesStringId = R.string.no_services_found;
            setActivityName("services");
        }
        noServicesTextView.setText(getString(noServicesStringId, getString(R.string.app_name)) + " " + getString(R
                .string.click_magnifying_glass_to_search_services));

        ImageButton magnifyingGlass = (ImageButton) findViewById(R.id.ic_magnifying_glass);
        magnifyingGlass.setImageDrawable(new IconicsDrawable(this, FontAwesome.Icon.faw_search).color(ContextCompat.getColor(this, R.color.mc_primary_icon)).sizeDp(200).paddingDp(20));

        magnifyingGlass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSearching();
            }
        });
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
            updateVisibleItems();
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
        setTitle(mOrganizationTypeStringId);
    }

    @Override
    protected void loadCursorAndSetAdaptar() {
        super.loadCursorAndSetAdaptar();
        updateVisibleItems();
    }

    private void updateVisibleItems() {
        if (mListAdapter.getCount() == 0) {
            boolean found = false;
            for (int i = 0; i < AppConstants.SEARCH_SERVICES_IF_NONE_CONNECTED.length; i++) {
                if (AppConstants.SEARCH_SERVICES_IF_NONE_CONNECTED[i] == mOrganizationType) {
                    found = true;
                    break;
                }
            }
            if (found) {
                findViewById(R.id.no_services).setVisibility(View.VISIBLE);
                findViewById(R.id.friend_list).setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onListItemClick(ListView listView, final View listItem, int position, long id) {
        T.UI();
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

    protected void startSearching() {
        final Intent serviceSearch = new Intent(this, ServiceSearchActivity.class);
        serviceSearch.putExtra(ServiceSearchActivity.ORGANIZATION_TYPE, mOrganizationType);
        startActivity(serviceSearch);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        T.UI();
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.services_menu, menu);
        menu.getItem(0).setIcon(new IconicsDrawable(this).icon(FontAwesome.Icon.faw_search).color(Color.DKGRAY).sizeDp(18));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        T.UI();

        switch (item.getItemId()) {
            case R.id.find_services:
                startSearching();
                return true;
            case R.id.help:
                showHelp();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
