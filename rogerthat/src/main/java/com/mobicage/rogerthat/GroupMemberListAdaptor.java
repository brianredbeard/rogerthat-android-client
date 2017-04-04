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
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.Contact;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendStore;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.friends.PhoneContacts;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.T;

// Adapter lives on UI thread
// It can be temporarily out of date with respect to the
// underlying database content:
//   * we cache the retrieved item count, in order to avoid ANR
//   * if a non-existing entry is retrieved from the db (e.g. item 5 of 4), we return an empty (non-null) view
// When database is changed, code must call notifyDataSetChanged() to refresh the adapter

class GroupMemberListAdaptor extends CursorAdapter implements SectionIndexer {

    public interface ViewUpdater {
        void update(View view);
    }

    private final Context mContext;
    private final ViewUpdater mUpdater;
    private final FriendStore mStore;
    private final FriendsPlugin mFriendsPlugin;
    private final AlphabetIndexer mAlphaIndexer;
    private final PhoneContacts mPhoneContacts;
    private final Bitmap mNoAvatar;
    private final boolean mHasHeaderView;

    public GroupMemberListAdaptor(Context context, Cursor cursor, FriendStore store, ViewUpdater updater,
        FriendsPlugin friendsPlugin, boolean hasHeaderView) {
        super(context, cursor, false);
        T.UI();
        mContext = context;
        mStore = store;
        mUpdater = updater;
        mFriendsPlugin = friendsPlugin;
        mAlphaIndexer = new AlphabetIndexer(cursor, 2, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        mPhoneContacts = new PhoneContacts(mContext.getContentResolver());
        mNoAvatar = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.unknown_avatar)).getBitmap();
        mHasHeaderView = hasHeaderView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        T.UI();

        final View view;

        if (convertView == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.friend, parent, false);
        } else {
            view = convertView;
        }

        LinearLayout actions = (LinearLayout) view.findViewById(R.id.actions);
        actions.setVisibility(View.GONE);

        Cursor cursor = getCursor();
        if (!cursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }

        Friend friend = mStore.readGroupMemberFromCursor(cursor);

        if (friend.existenceStatus != Friend.ACTIVE && friend.existenceStatus != Friend.INVITE_PENDING) {
            L.d("Friend at index " + position + " with email " + friend.email + " has existence status "
                + friend.existenceStatus);
            ImageView image = (ImageView) view.findViewById(R.id.friend_avatar);
            image.setImageBitmap(mFriendsPlugin.getMissingFriendAvatarBitmap());
            TextView name = (TextView) view.findViewById(R.id.friend_name);
            name.setText("");
            return view;
        }

        setFriendOnView(view, friend);

        if (mUpdater != null)
            mUpdater.update(view);

        return view;
    }

    private void setFriendOnView(final View view, final Friend friend) {
        T.UI();
        final View spinner = view.findViewById(R.id.friend_spinner);
        spinner.setVisibility(friend.existenceStatus == Friend.INVITE_PENDING ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.friend_existence_layout).setVisibility(spinner.getVisibility());

        final ImageView image = (ImageView) view.findViewById(R.id.friend_avatar);
        final TextView name = (TextView) view.findViewById(R.id.friend_name);
        final TextView subtitle = (TextView) view.findViewById(R.id.friend_subtitle);

        Resources resources = view.getResources();
        if (friend instanceof Contact) {
            Contact contact = (Contact) friend;
            Bitmap avatar = mPhoneContacts.getAvatar(contact);
            if (avatar == null)
                avatar = mNoAvatar;
            image.setImageBitmap(avatar);
            name.setTextColor(resources.getColor(android.R.color.secondary_text_light));
            name.setText(contact.name);
            subtitle.setVisibility(View.GONE);
        } else {
            if (friend.category != null && friend.category.friendCount > 1) {
                image.setImageBitmap(mFriendsPlugin.toFriendBitmap(friend.category.avatar));
                name.setText(friend.category.name);
                subtitle.setText(friend.getDisplayName() + ", ...");
                subtitle.setVisibility(View.VISIBLE);
            } else {
                image.setImageBitmap(mFriendsPlugin.toFriendBitmap(friend.avatar));
                name.setText(friend.getDisplayName());
                subtitle.setVisibility(View.GONE);
            }
        }
        view.setTag(friend);
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
    public int getPositionForSection(int section) {
        if (mHasHeaderView && section == 0)
            return -1; // Show headerview when scrolling up
        else
            return mAlphaIndexer.getPositionForSection(section);
    }

    @Override
    public int getSectionForPosition(int position) {
        return mAlphaIndexer.getSectionForPosition(position);
    }

    @Override
    public Object[] getSections() {
        return mAlphaIndexer.getSections();
    }
}
