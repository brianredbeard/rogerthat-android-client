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

package com.mobicage.rogerthat.plugins.history;

import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ListView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.ServiceBoundCursorListActivity;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.T;

public class HistoryListActivity extends ServiceBoundCursorListActivity implements OnSharedPreferenceChangeListener {

    // All owned by UI thread
    private HistoryPlugin mHistoryPlugin;
    private MessagingPlugin mMessagingPlugin;
    private FriendsPlugin mFriendsPlugin;
    private boolean mHasSubActivity = false;
    private boolean mIsShowingNewContent = true;

    private boolean mShowImportantOnly = true;

    // XXX: could improve performance by calculating mIsShowingNewContent in onServiceBound

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        T.UI();
        setContentView(R.layout.activity);
        setListView((ListView) findViewById(R.id.activity_list));
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onServiceBound() {
        mHistoryPlugin = mService.getPlugin(HistoryPlugin.class);
        mMessagingPlugin = mService.getPlugin(MessagingPlugin.class);
        mFriendsPlugin = mService.getPlugin(FriendsPlugin.class);

        IntentFilter filter = new IntentFilter();
        for (String action : getAllReceivingIntents()) {
            filter.addAction(action);
        }
        registerReceiver(getDefaultBroadcastReceiver(), filter);

        final SharedPreferences options = PreferenceManager.getDefaultSharedPreferences(mService);
        options.registerOnSharedPreferenceChangeListener(this);
        mShowImportantOnly = mustShowImportantOnly(options);
        setListAdapter();

        setTitle(R.string.stream_title);
    }

    private void createCursor() {
        if (mShowImportantOnly)
            setCursor(mHistoryPlugin.getStore().getImportantOnlyCursor());
        else
            setCursor(mHistoryPlugin.getStore().getFullCursor());
    }

    private void setListAdapter() {
        if (getCursor() != null) {
            stopManagingCursor(getCursor());
            getCursor().close();
        }
        createCursor();
        startManagingCursor(getCursor());
        final HistoryListAdapter adapter = new HistoryListAdapter(this, getCursor(), mHistoryPlugin.getStore(),
            mMessagingPlugin, mFriendsPlugin, mHistoryPlugin.getStore().getLastReadItemID());
        setListAdapter(adapter);
    }

    @Override
    protected void onServiceUnbound() {
        unregisterReceiver(getDefaultBroadcastReceiver());
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        HistoryItem item = (HistoryItem) v.getTag();
        ((HistoryListAdapter) getListAdapter()).startItemDetailActivity(this, item);
        mHasSubActivity = true;
    }

    @Override
    protected boolean onListItemLongClick(ListView l, View v, int position, long id) {
        return false;
    }

    @Override
    protected void changeCursor() {
        if (mServiceIsBound) {
            createCursor();
            ((HistoryListAdapter) getListAdapter()).changeCursor(getCursor());
        }
    }

    @Override
    protected void onPause() {
        T.UI();
        super.onPause();
        try {
            if (mServiceIsBound && !mHasSubActivity && mIsShowingNewContent) {
                // No subactivity but doing onPause -> I probably switch tabs
                // Could also be that a foreign app jumps in front of us
                mHistoryPlugin.getStore().updateLastReadItemID();
                ((HistoryListAdapter) getListAdapter()).setLastReadItem(mHistoryPlugin.getStore().getLastReadItemID());
                mIsShowingNewContent = false;
                setNeedsViewRefresh();
            }
        } catch (Exception e) {
            L.bug(e);
        }
    }

    @Override
    protected void onResume() {
        T.UI();
        mHasSubActivity = false;
        super.onResume();
    }

    @Override
    protected void notifyContentChanged() {
        T.UI();
        mIsShowingNewContent = true;
    }

    @Override
    protected void refreshView() {
        T.UI();

        final HistoryListAdapter adapter = (HistoryListAdapter) getListAdapter();
        final long lastReadItem = adapter.getLastReadItem();

        final ListView lv = getListView();
        final int first = lv.getFirstVisiblePosition();
        final int last = lv.getLastVisiblePosition();

        L.d("last read item = " + lastReadItem);
        for (int position = first; position <= last; position++) {
            try {
                final View v = lv.getChildAt(position);
                adapter.updateDividerLine(v, (HistoryItem) v.getTag());
            } catch (Exception e) {
                L.bug("Error in HistoryListActivity - position is " + position, e);
                break;
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (MainService.PREFERENCE_STREAM_ONLY_IMPORTANT.equals(key)) {
            mShowImportantOnly = mustShowImportantOnly(sharedPreferences);
            setListAdapter();
        }
    }

    private boolean mustShowImportantOnly(SharedPreferences sharedPreferences) {
        return sharedPreferences.getBoolean(MainService.PREFERENCE_STREAM_ONLY_IMPORTANT, true);
    }

    @Override
    protected String[] getAllReceivingIntents() {
        return new String[] { HistoryPlugin.INTENT_HISTORY_ITEM_ADDED,
            HistoryPlugin.INTENT_HISTORY_ITEM_REFERENCE_MODIFIED, HistoryPlugin.INTENT_HISTORY_ITEM_DELETED };
    }
}
