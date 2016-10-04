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

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.news.NewsPlugin;
import com.mobicage.rogerthat.plugins.news.NewsStore;
import com.mobicage.rogerthat.util.system.T;

class NewsListRecyclerAdapter extends RecyclerView.Adapter<NewsListRecyclerAdapter.ViewHolder> {


    NewsListAdapter mCursorAdapter;

    NewsActivity mActivity;

    public NewsListRecyclerAdapter(NewsActivity activity, MainService mainService, Cursor cursor, NewsPlugin newsPlugin, NewsStore store, FriendsPlugin friendsPlugin) {
        mActivity = activity;
        mCursorAdapter = new NewsListAdapter(mActivity, mainService, cursor, newsPlugin, store, friendsPlugin);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    @Override
    public int getItemCount() {
        return mCursorAdapter.getCount();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursorAdapter.getCursor().moveToPosition(position);
        mCursorAdapter.bindView(holder.itemView, mActivity, mCursorAdapter.getCursor());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mCursorAdapter.newView(mActivity, mCursorAdapter.getCursor(), parent);
        return new ViewHolder(v);
    }

    public void changeCursor(Cursor cursor) {
        mCursorAdapter.changeCursor(cursor);
    }

    public void updateView(long newsId) {
        int position = mCursorAdapter.getPositionForNewsId(newsId);
        if (position >= 0) {
            notifyItemChanged(position);
        }
    }

    public void handleIntent(Context context, Intent intent) {
        T.UI();
        mCursorAdapter.handleIntent(context, intent);
    }
}