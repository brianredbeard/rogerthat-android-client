/*
 * Copyright 2017 GIG Technology NV
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
 * @@license_version:1.3@@
 */
package com.mobicage.rogerthat;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.FriendStore;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.friends.Group;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.T;

// Adapter lives on UI thread
// It can be temporarily out of date with respect to the
// underlying database content:
//   * we cache the retrieved item count, in order to avoid ANR
//   * if a non-existing entry is retrieved from the db (e.g. item 5 of 4), we return an empty (non-null) view
// When database is changed, code must call notifyDataSetChanged() to refresh the adapter

class GroupListAdapter extends CursorAdapter {

    public interface ViewUpdater {
        void update(View view);
    }

    private final Context mContext;
    private final ViewUpdater mUpdater;
    private final FriendStore mStore;
    private final FriendsPlugin mFriendsPlugin;
    private final boolean mHasHeaderView;
    private final View mHeaderView;

    public GroupListAdapter(Context context, Cursor cursor, FriendStore store, ViewUpdater updater,
        FriendsPlugin friendsPlugin, boolean hasHeaderView, View headerView) {
        super(context, cursor, false);
        T.UI();
        mContext = context;
        mStore = store;
        mUpdater = updater;
        mFriendsPlugin = friendsPlugin;
        mHasHeaderView = hasHeaderView;
        mHeaderView = headerView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        T.UI();
        if (mHasHeaderView && position == 0 && mHeaderView != null)
            return mHeaderView;

        final View view;

        if (convertView == null || convertView.findViewById(R.id.mainheader) != null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.group, parent, false);
        } else {
            view = convertView;
        }

        LinearLayout actions = (LinearLayout) view.findViewById(R.id.actions);
        if (actions != null)
            actions.setVisibility(View.GONE);

        if (mHasHeaderView && mHeaderView != null) {
            position -= 1;
        }
        Cursor cursor = getCursor();
        if (!cursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }

        Group group = mStore.readGroupFromCursor(cursor);
        setGroupOnView(view, group);

        if (mUpdater != null)
            mUpdater.update(view);

        return view;
    }

    private void setGroupOnView(final View view, final Group group) {
        T.UI();
        final ImageView image = (ImageView) view.findViewById(R.id.group_avatar);
        final TextView name = (TextView) view.findViewById(R.id.group_name);
        final TextView subtitle = (TextView) view.findViewById(R.id.group_subtitle);

        Resources resources = view.getResources();

        image.setImageBitmap(mFriendsPlugin.toGroupBitmap(group.avatar));
        name.setTextColor(resources.getColor(android.R.color.secondary_text_light));
        name.setText(group.name);
        subtitle.setVisibility(View.GONE);
        view.setTag(group);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        L.bug("Should not come here");
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        L.bug("Should not come here");
        return null;
    }

    @Override
    public int getCount() {
        if (mHasHeaderView && mHeaderView != null)
            return super.getCount() + 1;
        else
            return super.getCount();
    }
}
