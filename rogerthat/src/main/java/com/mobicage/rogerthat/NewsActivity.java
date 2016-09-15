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

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.history.HistoryItem;
import com.mobicage.rogerthat.plugins.history.HistoryListAdapter;
import com.mobicage.rogerthat.plugins.history.HistoryPlugin;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;

public class NewsActivity extends ServiceBoundCursorListActivity {

    private HistoryPlugin mHistoryPlugin;
    private MessagingPlugin mMessagingPlugin;
    private FriendsPlugin mFriendsPlugin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news);
        setListView((ListView) findViewById(R.id.news_list));
        setActivityName("news");
        setTitle(R.string.news);
    }

    @Override
    protected void onServiceBound() {
        mHistoryPlugin = mService.getPlugin(HistoryPlugin.class);
        mMessagingPlugin = mService.getPlugin(MessagingPlugin.class);
        mFriendsPlugin = mService.getPlugin(FriendsPlugin.class);
        setListAdapter();
    }

    @Override
    protected void onServiceUnbound() {
    }

    private void createCursor() {
        setCursor(mHistoryPlugin.getStore().getFullCursor());
    }

    private void setListAdapter() {
        if (getCursor() != null) {
            stopManagingCursor(getCursor());
            getCursor().close();
        }
        createCursor();
        startManagingCursor(getCursor());
        final NewsListAdapter adapter = new NewsListAdapter(this, getCursor(), mMessagingPlugin, mFriendsPlugin);
        setListAdapter(adapter);
    }

    @Override
    protected void changeCursor() {
        if (mServiceIsBound) {
            createCursor();
            ((NewsListAdapter) getListAdapter()).changeCursor(getCursor());
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        L.i("NewsActivity onListItemClick");
    }

    @Override
    protected boolean onListItemLongClick(ListView l, View v, int position, long id) {
        L.i("NewsActivity onListItemLongClick");
        return false;
    }

    @Override
    protected String[] getAllReceivingIntents() {
        return new String[] {};
    }
}