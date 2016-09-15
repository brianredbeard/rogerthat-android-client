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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.history.HistoryItem;
import com.mobicage.rogerthat.plugins.history.HistoryStore;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.plugins.scan.ProcessScanActivity;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.time.TimeUtils;

// XXX: red line should not move in case we rotate phone

public class NewsListAdapter extends CursorAdapter {

    private final LayoutInflater mLayoutInflater;
    private final Context mContext;
    private final MessagingPlugin mMessagingPlugin;
    private final FriendsPlugin mFriendsPlugin;

    public NewsListAdapter(Context context, Cursor cursor, MessagingPlugin messagingPlugin, FriendsPlugin friendsPlugin) {
        super(context, cursor, false);
        T.UI();
        mContext = context;
        mMessagingPlugin = messagingPlugin;
        mFriendsPlugin = friendsPlugin;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        T.UI();

        final View view;

        if (convertView == null) {
            view = mLayoutInflater.inflate(R.layout.news_list_item, parent, false);
        } else {
            view = convertView;
        }

        final Cursor cursor = getCursor();
        if (!cursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }

        return view;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        L.bug("Should not come here");
        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        L.bug("Should not come here");
    }
}
