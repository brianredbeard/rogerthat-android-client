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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;

public abstract class ServiceBoundCursorListActivity extends ServiceBoundActivity implements ServiceBound {

    private BroadcastReceiver mBroadcastReceiver;
    private boolean mNeedsCursorRefresh = false;
    private boolean mNeedsViewRefresh = false;

    private boolean mIsVisible = false;
    private int mScrollPositionIndex = -1;
    private int mScrollPositionTop = -1;

    protected ListView mListView;

    protected ListView getListView() {
        return mListView;
    }

    private Cursor mCursor = null;

    protected void setListView(ListView listView) {
        this.mListView = listView;
        this.mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
                onListItemClick((ListView) listView, view, position, id);
            }
        });
        this.mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> listView, View view, int position, long id) {
                return onListItemLongClick((ListView) listView, view, position, id);
            }
        });
        if (mListAdapter != null) {
            this.mListView.setAdapter(mListAdapter);
        }
    }

    protected ListAdapter mListAdapter;

    protected ListAdapter getListAdapter() {
        return mListAdapter;
    }

    protected void setListAdapter(ListAdapter listAdapter) {
        this.mListAdapter = listAdapter;
        if (mListView != null)
            mListView.setAdapter(listAdapter);
    }

    protected abstract void onListItemClick(ListView l, View v, int position, long id);

    protected abstract boolean onListItemLongClick(ListView l, View v, int position, long id);

    protected abstract String[] getAllReceivingIntents();

    protected abstract void changeCursor();

    protected void refreshCursor() {
        notifyContentChanged();
        if (mIsVisible)
            changeCursor();
        else
            mNeedsCursorRefresh = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBroadcastReceiver = new SafeBroadcastReceiver() {
            @Override
            public String[] onSafeReceive(Context context, Intent intent) {
                L.d("Content changed, updating screen! Intent: " + intent.getAction());
                refreshCursor();
                return getAllReceivingIntents();
            };
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mListView == null)
            return;
        mScrollPositionIndex = mListView.getFirstVisiblePosition();
        View v = mListView.getChildAt(0);
        mScrollPositionTop = (v == null) ? 0 : v.getTop();
        mIsVisible = false;

        if (mCursor != null) {
            stopManagingCursor(mCursor);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsVisible = true;
        if (mNeedsCursorRefresh) {
            mNeedsCursorRefresh = false;
            mNeedsViewRefresh = false;

            // XXX: doing cursor refresh here can make UI slow
            changeCursor();

            if (mScrollPositionIndex != -1)
                mListView.setSelectionFromTop(mScrollPositionIndex, mScrollPositionTop);
        } else if (mNeedsViewRefresh) {
            mNeedsViewRefresh = false;
            refreshView();
            if (mScrollPositionIndex != -1)
                mListView.setSelectionFromTop(mScrollPositionIndex, mScrollPositionTop);
        } else if (getWasPaused() && mCursor != null) {
            startManagingCursor(mCursor);
        }
    }

    protected BroadcastReceiver getDefaultBroadcastReceiver() {
        return mBroadcastReceiver;
    }

    protected void setNeedsViewRefresh() {
        mNeedsViewRefresh = true;
    }

    protected void notifyContentChanged() {
        // do nothing
    }

    protected void refreshView() {
        // to be overridden by code who has called setNeedsViewRefresh()
        // see example in HistoryListActivity
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public void setCursor(Cursor cursor) {
        if (mCursor != null) {
            stopManagingCursor(mCursor);
        }
        mCursor = cursor;
    }

}
