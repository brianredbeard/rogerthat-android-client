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

package com.mobicage.rogerthat.plugins.messaging;

import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.FriendDetailOrInviteActivity;
import com.mobicage.rogerthat.ServiceBoundActivity;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.scan.ProfileActivity;
import com.mobicage.rogerthat.util.system.T;

import java.util.HashMap;

import java.util.Map;

public class FriendsThreadMembersActivity extends ServiceBoundActivity {

    public static final String ME = "me";
    public static final String MEMBERS = "members";

    private FriendsPlugin mFriendsPlugin;
    private String[] mMembers;
    private String mMyEmail;
    private Map<String, Bitmap> mMemberAvatars;
    private Map<String, String> mMemberNames;

    @Override
    protected void onServiceBound() {
        setContentViewWithoutNavigationBar(R.layout.message_thread_members);
        setTitle(R.string.members);

        Intent intent = getIntent();

        mMyEmail = intent.getStringExtra(ME);
        mMembers = intent.getStringArrayExtra(MEMBERS);
        mMemberAvatars = new HashMap<>();
        mMemberNames = new HashMap<>();

        mFriendsPlugin = mService.getPlugin(FriendsPlugin.class);

        displayMembers();
    }

    @Override
    protected void onServiceUnbound() {
    }

    private void displayMembers() {
        MembersListAdapter matchesAdapter = new MembersListAdapter(mMembers);
        ListView list = (ListView) findViewById(R.id.thread_members);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
                T.UI();
                if (mMyEmail.equals(mMembers[position])) {
                    Intent intent = new Intent(FriendsThreadMembersActivity.this, ProfileActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(FriendsThreadMembersActivity.this, FriendDetailOrInviteActivity.class);
                    intent.putExtra(FriendDetailOrInviteActivity.EMAIL, mMembers[position]);
                    startActivity(intent);
                }
            }
        });

        list.setAdapter(matchesAdapter);
    }

    private class MembersListAdapter extends BaseAdapter {

        private final String[] mMembers;

        public MembersListAdapter(String[] members) {
            mMembers = members;
        }

        @Override
        public int getCount() {
            T.UI();
            return mMembers.length;
        }

        @Override
        public Object getItem(int position) {
            T.UI();
            return null;
        }

        @Override
        public long getItemId(int position) {
            T.UI();
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null || v.getTag() == null) {
                v = getLayoutInflater().inflate(R.layout.friend, parent, false);
            }

            if (!mMemberAvatars.containsKey(mMembers[position])) {
                mMemberAvatars.put(mMembers[position], mFriendsPlugin.getAvatarBitmap(mMembers[position]));
                mMemberNames.put(mMembers[position], mFriendsPlugin.getName(mMembers[position]));
            }

            final ImageView image = (ImageView) v.findViewById(R.id.friend_avatar);
            image.setImageBitmap(mMemberAvatars.get(mMembers[position]));
            final TextView name = (TextView) v.findViewById(R.id.friend_name);
            name.setText(mMemberNames.get(mMembers[position]));
            final TextView subtitle = (TextView) v.findViewById(R.id.friend_subtitle);
            subtitle.setVisibility(View.GONE);
            final LinearLayout actions = (LinearLayout) v.findViewById(R.id.actions);
            actions.setVisibility(View.GONE);

            v.setTag(mMembers[position]);
            return v;
        }
    }
}