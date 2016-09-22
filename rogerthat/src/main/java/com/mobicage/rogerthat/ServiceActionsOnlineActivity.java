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

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.friends.ServiceActionMenuActivity;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.ui.ImageHelper;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.to.service.FindServiceItemTO;
import com.mobicage.to.service.FindServiceResponseTO;

import org.jivesoftware.smack.util.Base64;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceActionsOnlineActivity extends ServiceBoundActivity {

    protected String mAction;

    private static final String[] UPDATE_VIEW_INTENTS = new String[]{FriendsPlugin.FRIENDS_LIST_REFRESHED,
            FriendsPlugin.FRIEND_ADDED_INTENT, FriendsPlugin.FRIEND_MARKED_FOR_REMOVAL_INTENT,
            FriendsPlugin.FRIEND_REMOVED_INTENT};

    private FriendsPlugin mFriendsPlugin;
    private BroadcastReceiver mBroadcastReceiver;
    private ProgressDialog mProgressDialog;
    private FindServiceResponseTO mResponseTO;
    private SearchInfo mSearchInfo;
    private AbsListView mListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mAction = intent.getStringExtra(ServiceActionsOfflineActivity.ACTION);

        setContentView(R.layout.service_actions);
        setNavigationBarBurgerVisible(false, true);
        setTitle(R.string.discover_services_short); // todo ruben title
    }

    @Override
    protected void onServiceBound() {
        mFriendsPlugin = mService.getPlugin(FriendsPlugin.class);
        mBroadcastReceiver = getBroadCastReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(FriendsPlugin.SERVICE_SEARCH_FAILED_INTENT);
        filter.addAction(FriendsPlugin.SERVICE_SEARCH_RESULT_INTENT);
        for (String action : UPDATE_VIEW_INTENTS)
            filter.addAction(action);
        registerReceiver(mBroadcastReceiver, filter);
    }


    @Override
    protected void onServiceUnbound() {
        unregisterReceiver(mBroadcastReceiver);
    }


    private AdapterView.OnItemClickListener mListItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            @SuppressWarnings("unchecked")
            Map<String, Object> tag = (Map<String, Object>) view.getTag();
            if (tag != null) {
                int existence = (Integer) tag.get("existence");
                FindServiceItemTO item = (FindServiceItemTO) tag.get("item");
                if (existence == Friend.ACTIVE) {
                    Intent intent = new Intent(ServiceActionsOnlineActivity.this, ServiceActionMenuActivity.class);
                    intent.putExtra(ServiceActionMenuActivity.SERVICE_EMAIL, item.email);
                    startActivity(intent);
                } else {
                    L.i("todo ruben show popup and connect");
                }
            }
        }
    };

    private BroadcastReceiver getBroadCastReceiver() {
        return new SafeBroadcastReceiver() {

            @Override
            public String[] onSafeReceive(Context context, Intent intent) {
                String action = intent.getAction();
                L.i("onSafeReceive: " + action);
                return null;
            }

        };
    }


    private void showSearchFailedDialog() {
        UIUtils.showAlertDialog(this, null, R.string.error_search);
    }


    private class ServiceSearchAdapter extends BaseAdapter {

        private final List<FindServiceItemTO> mItems = new ArrayList<FindServiceItemTO>();
        private final SearchInfo mSearchInfo;

        public ServiceSearchAdapter(FindServiceItemTO[] items, SearchInfo info) {
            mSearchInfo = info;
            addAll(items);
        }

        public void addAll(FindServiceItemTO[] items) {
            mItems.addAll(Arrays.asList(items));
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mItems.size() + (mSearchInfo.cursor == null ? 0 : 1);
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (position == mItems.size() && mSearchInfo.cursor != null) {
                return getLayoutInflater().inflate(R.layout.list_loading_more_indicator, null);
            }

            final FindServiceItemTO item = mItems.get(position);
            final int existence = mFriendsPlugin.getStore().getExistence(item.email);

            View v = convertView;
            if (v == null || v.getTag() == null) {
                v = getLayoutInflater().inflate(R.layout.search_friend, null);
            }

            Map<String, Object> tag = new HashMap<String, Object>();
            tag.put("item", item);
            tag.put("existence", existence);
            v.setTag(tag);

            // Set avatar
            byte[] img = Base64.decode(item.avatar);
            Bitmap avatar = ImageHelper.getRoundedCornerAvatar(BitmapFactory.decodeByteArray(img, 0, img.length));
            ImageView avatarView = (ImageView) v.findViewById(R.id.friend_avatar);
            avatarView.setImageBitmap(avatar);
            LayoutParams lp = avatarView.getLayoutParams();
            lp.width = lp.height = UIUtils.convertDipToPixels(ServiceActionsOnlineActivity.this, 40);

            // Set name
            ((TextView) v.findViewById(R.id.friend_name)).setText(item.name);
            final TextView detailTextView = (TextView) v.findViewById(R.id.friend_subtitle);
            detailTextView.setText(item.detail_text);

            // Set status icon
            v.findViewById(R.id.friend_existence_layout).setVisibility(View.VISIBLE);
            ProgressBar spinnerView = (ProgressBar) v.findViewById(R.id.friend_spinner);
            ImageView statusView = (ImageView) v.findViewById(R.id.friend_existence);
            int buttonColor = getColor(R.color.mc_default_text_inverse);

            switch (existence) {
                case Friend.ACTIVE:
                    spinnerView.setVisibility(View.GONE);
                    statusView.setVisibility(View.VISIBLE);
                    statusView.setImageDrawable(new IconicsDrawable(ServiceActionsOnlineActivity.this).icon(FontAwesome.Icon.faw_check).color(buttonColor).sizeDp(18));
                    statusView.setBackgroundColor(getColor(R.color.mc_default_button));

                    break;
                case Friend.DELETED:
                case Friend.DELETION_PENDING:
                case Friend.NOT_FOUND:
                    spinnerView.setVisibility(View.GONE);
                    statusView.setVisibility(View.VISIBLE);
                    statusView.setImageDrawable(new IconicsDrawable(ServiceActionsOnlineActivity.this).icon(FontAwesome.Icon.faw_plus).color(buttonColor).sizeDp(18));
                    statusView.setBackgroundColor(getColor(R.color.mc_primary_color));

                    break;
                case Friend.INVITE_PENDING:
                    spinnerView.setVisibility(View.VISIBLE);
                    statusView.setVisibility(View.GONE);
                    break;
                default:
                    spinnerView.setVisibility(View.GONE);
                    statusView.setVisibility(View.GONE);
                    break;
            }
            return v;

        }
    }

    private class SearchInfo {
        ServiceSearchAdapter adapter;
        String cursor;
        LinearLayout label;
        ListView results;
        boolean loading = false;
    }
}
