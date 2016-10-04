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
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;

public abstract class ServiceBoundCursorRecyclerActivity extends ServiceBoundActivity implements ServiceBound {

    private BroadcastReceiver mBroadcastReceiver;
    private boolean mNeedsCursorRefresh = false;
    private boolean mNeedsViewRefresh = false;

    private boolean mIsVisible = false;
    private int mScrollPositionIndex = -1;
    private int mScrollPositionTop = -1;

    private LinearLayoutManager mLayoutManager;
    protected RecyclerView mRecyclerView;
    protected RecyclerView.Adapter mAdapter;

    protected RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    protected void setRecyclerView(RecyclerView recyclerView) {
        this.mRecyclerView = recyclerView;
        this.mLayoutManager = new LinearLayoutManager(this);
        this.mRecyclerView.setLayoutManager(mLayoutManager);
        if (mAdapter != null) {
            this.mRecyclerView.setAdapter(mAdapter);
        }
    }

    protected RecyclerView.Adapter getAdapter() {
        return mAdapter;
    }

    protected void setAdapter(RecyclerView.Adapter listAdapter) {
        this.mAdapter = listAdapter;
        if (mRecyclerView != null)
            mRecyclerView.setAdapter(listAdapter);
    }

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
            }

            ;
        };
    }

    protected int getFirstVisiblePosition() {
        super.onPause();
        if (mRecyclerView == null)
            return -1;
        return mLayoutManager.findFirstVisibleItemPosition();
    }

    protected void setSelection(int position) {
        mLayoutManager.scrollToPositionWithOffset(position, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mRecyclerView == null)
            return;
        mScrollPositionIndex = getFirstVisiblePosition();
        View v = mRecyclerView.getChildAt(0);
        mScrollPositionTop = (v == null) ? 0 : v.getTop();
        mIsVisible = false;
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
                mLayoutManager.scrollToPositionWithOffset(mScrollPositionIndex, mScrollPositionTop);
        } else if (mNeedsViewRefresh) {
            mNeedsViewRefresh = false;
            refreshView();
            if (mScrollPositionIndex != -1)
                mLayoutManager.scrollToPositionWithOffset(mScrollPositionIndex, mScrollPositionTop);
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
}
