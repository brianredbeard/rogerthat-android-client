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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MyIdentity;
import com.mobicage.rogerthat.ServiceBoundActivity;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendSearchActivity;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.scan.ProfileActivity;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.ArrayList;
import java.util.List;

public class ChatInfoActivity extends ServiceBoundActivity {

    public static final String CHAT_KEY = "parent_message_key";
    private String mChatKey;

    public static Intent createIntent(Context context, String chatKey) {
        Intent intent = new Intent(context, ChatInfoActivity.class);
        intent.putExtra(CHAT_KEY, chatKey);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mChatKey = getIntent().getStringExtra(CHAT_KEY);
        setContentView(R.layout.chat_info);
        setTitle(R.string.about);
    }

    @Override
    protected void onServiceBound() {
        final MessagingPlugin messagingPlugin = mService.getPlugin(MessagingPlugin.class);
        final FriendsPlugin friendsPlugin = mService.getPlugin(FriendsPlugin.class);
        final Message chat = messagingPlugin.getStore().getFullMessageByKey(mChatKey);

        MyIdentity myIdentity = mService.getIdentityStore().getIdentity();
        if (myIdentity.getEmail().equals(chat.sender)) {
            ((ImageView) findViewById(R.id.avatar_image_view)).setImageBitmap(myIdentity.getAvatarBitmap());
            ((TextView) findViewById(R.id.chat_owner_name)).setText(myIdentity.getDisplayName());
        } else {
            Friend chatOwner = friendsPlugin.getStore().getFriend(chat.sender);
            if (chatOwner == null)
                findViewById(R.id.chat_owner).setVisibility(View.GONE);
            else {
                ((ImageView) findViewById(R.id.avatar_image_view)).setImageBitmap(chatOwner.getAvatarBitmap());
                ((TextView) findViewById(R.id.chat_owner_name)).setText(chatOwner.getDisplayName());
            }
        }

        JSONObject details = (JSONObject) JSONValue.parse(chat.message);
        List<ChatItem> items = new ArrayList<ChatItem>();
        if (details.containsKey("t")) {
            items.add(new ChatItem(getString(R.string.topic), (String) details.get("t")));
        }
        if (details.containsKey("d")) {
            items.add(new ChatItem(getString(R.string.description), (String) details.get("d")));
        }
        JSONArray chatInfo = (JSONArray) details.get("i");
        if (chatInfo != null) {
            items.add(null);
            for (Object item : chatInfo) {
                final JSONObject itemObject = (JSONObject) item;
                items.add(new ChatItem((String) itemObject.get("k"), (String) itemObject.get("v")));
            }
        }
        ((ListView) findViewById(R.id.chat_info_items)).setAdapter(new ListAdapter(this, items
            .toArray(new ChatItem[items.size()])));
    }

    @Override
    protected void onServiceUnbound() {
    }

    private class ChatItem {
        String title;
        String value;

        public ChatItem(String title, String values) {
            this.title = title;
            this.value = values;
        }
    }

    private class ListAdapter extends ArrayAdapter<ChatItem> {

        private ChatItem[] values;

        public ListAdapter(Context context, ChatItem[] values) {
            super(context, -1, values);
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ChatItem chatItem = this.values[position];
            if (chatItem == null) {
                return getLayoutInflater().inflate(R.layout.chat_info_separator, null);
            }
            final View itemView;
            if (convertView != null && convertView instanceof LinearLayout)
                itemView = convertView;
            else
                itemView = getLayoutInflater().inflate(R.layout.chat_info_item, null);
            ((TextView) itemView.findViewById(R.id.title)).setText(chatItem.title);
            ((TextView) itemView.findViewById(R.id.values)).setText(chatItem.value);
            return itemView;
        }

    }

}
