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

import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.news.NewsPlugin;
import com.mobicage.rogerthat.util.CachedDownloader;

import java.util.HashSet;
import java.util.Set;

public class NewsPinnedActivity extends NewsActivity {


    @Override
    protected String[] getAllReceivingIntents() {
        Set<String> intents = new HashSet<>();
        intents.add(NewsPlugin.DISABLE_NEWS_ITEM_INTENT);
        intents.add(FriendsPlugin.FRIEND_UPDATE_INTENT);
        intents.add(FriendsPlugin.FRIEND_AVATAR_CHANGED_INTENT);
        intents.add(FriendsPlugin.FRIEND_REMOVED_INTENT);
        intents.add(FriendsPlugin.FRIEND_MARKED_FOR_REMOVAL_INTENT);
        intents.add(FriendsPlugin.FRIEND_ADDED_INTENT);
        intents.add(FriendsPlugin.FRIENDS_LIST_REFRESHED);
        intents.add(FriendsPlugin.SERVICE_DATA_UPDATED);
        return intents.toArray(new String[intents.size()]);
    }

    @Override
    protected void changeCursor() {
        if (mServiceIsBound) {
            updateView();
        }
    }

    private void updateView() {
        NewsListAdapter nla = ((NewsListAdapter) getAdapter());
        nla.refreshView();
    }

    @Override
    protected void onServiceBound() {
        setActivityName("news_pinned");
        setTitle(R.string.saved_items);

        findViewById(R.id.internet_status_container).setVisibility(View.GONE);

        newsPlugin = mService.getPlugin(NewsPlugin.class);
        newsStore = newsPlugin.getStore();
        friendsPlugin = mService.getPlugin(FriendsPlugin.class);

        swipeContainer.setRefreshing(false);
        swipeContainer.setEnabled(false);

        cachedDownloader = CachedDownloader.getInstance(mService);

        setRecyclerView((RecyclerView) findViewById(R.id.news_list));
        pinnedSearchQry = "";
        loadCursorAndSetAdaptar();

        final LinearLayout searchContainer = (LinearLayout) findViewById(R.id.search_container);
        final TextView searchTextField = (TextView) findViewById(R.id.search_text);
        searchTextField.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (searchContainer.getVisibility() == View.VISIBLE) {
                    pinnedSearchQry = searchTextField.getText().toString();
                    updateView();
                    setSelection(0);
                }
            }
        });
        searchContainer.setVisibility(View.VISIBLE);

        setupIntentFilter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }
}
