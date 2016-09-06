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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.history.HistoryListActivity;
import com.mobicage.rogerthat.plugins.scan.ProfileActivity;
import com.mobicage.rogerthat.plugins.scan.ScanTabActivity;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.config.AppConstants;

public class MoreActivity extends ServiceBoundActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View current = getCurrentFocus();
        if (current != null)
            current.clearFocus();
    }

    @Override
    protected void onServiceBound() {
        setContentView(R.layout.more);
        setActivityName("more");

        MoreListAdapter matchesAdapter = new MoreListAdapter();

        ListView list = (ListView) findViewById(R.id.more_options_list);
        list.setAdapter(matchesAdapter);

        setNavigationBarVisible(AppConstants.SHOW_NAV_HEADER);
        setNavigationBarTitle(R.string.more);
        findViewById(R.id.navigation_bar_home_button).setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                Intent i = new Intent(MoreActivity.this, HomeActivity.class);
                i.setFlags(MainActivity.FLAG_CLEAR_STACK);
                startActivity(i);
                finish();
            }
        });
    }

    @Override
    protected void onServiceUnbound() {
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private class MoreListAdapter extends BaseAdapter {

        public static final int FRIENDS_ACTIVITY = 0;
        public static final int PROFILE_ACTIVITY = 1;
        public static final int SCAN_ACTIVITY = 2;
        public static final int SETTINGS_ACTIVITY = 3;
        public static final int ABOUT_ACTIVITY = 4;
        public static final int STREAM_ACTIVITY = 5;

        public MoreListAdapter() {
        }

        @Override
        public int getCount() {
            T.UI();
            int mNumRows = 5;
            if (!AppConstants.SHOW_FRIENDS_IN_MORE)
                mNumRows -= 1;

            if (!AppConstants.SHOW_PROFILE_IN_MORE)
                mNumRows -= 1;

            if (!AppConstants.SHOW_SCAN_IN_MORE)
                mNumRows -= 1;

            return mNumRows;
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
            ViewInfoHolder holder = null;
            if (v == null || v.getTag() == null) {
                v = getLayoutInflater().inflate(R.layout.more_item, parent, false);
                holder = new ViewInfoHolder();
                holder.itemAvatar = (ImageView) v.findViewById(R.id.item_avatar);
                holder.itemName = (TextView) v.findViewById(R.id.item_name);
            } else {
                holder = (ViewInfoHolder) v.getTag();
            }

            holder.position = position;
            v.setTag(holder);

            setItemsOnView(v, holder);
            return v;
        }
    }

    private class ViewInfoHolder {
        int position;
        TextView itemName;
        ImageView itemAvatar;
    }

    private int rowForPosition(int position) {
        int row = position;
        if (position == MoreListAdapter.FRIENDS_ACTIVITY) {
            if (!AppConstants.SHOW_FRIENDS_IN_MORE) {
                row += 1;
                if (!AppConstants.SHOW_PROFILE_IN_MORE) {
                    row += 1;
                    if (!AppConstants.SHOW_SCAN_IN_MORE) {
                        row += 1;
                    }
                }
            }
        } else {
            if (!AppConstants.SHOW_FRIENDS_IN_MORE) {
                row += 1;
            }
            if (!AppConstants.SHOW_PROFILE_IN_MORE) {
                row += 1;
            }
            if (!AppConstants.SHOW_SCAN_IN_MORE) {
                row += 1;
            }
        }
        return row;
    }

    private void setItemsOnView(final View v, final ViewInfoHolder holder) {
        int position = rowForPosition(holder.position);

        if (position == MoreListAdapter.STREAM_ACTIVITY) {
            holder.itemAvatar.setImageResource(R.drawable.more_network_monitor);
            holder.itemName.setText(R.string.stream_title);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Intent launchIntent = new Intent(MoreActivity.this, HistoryListActivity.class);
                    startActivity(launchIntent);
                }
            });

        } else if (position == MoreListAdapter.SETTINGS_ACTIVITY) {
            holder.itemAvatar.setImageResource(R.drawable.more_gear);
            holder.itemName.setText(R.string.settings);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Intent launchIntent = new Intent(MoreActivity.this, SettingsActivity.class);
                    startActivity(launchIntent);
                }
            });

        } else if (position == MoreListAdapter.ABOUT_ACTIVITY) {
            holder.itemAvatar.setImageResource(R.drawable.more_info);
            holder.itemName.setText(R.string.about);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Intent launchIntent = new Intent(MoreActivity.this, AboutActivity.class);
                    startActivity(launchIntent);
                }
            });

        } else if (position == MoreListAdapter.FRIENDS_ACTIVITY) {
            final int text;
            switch (AppConstants.FRIENDS_CAPTION) {
            case COLLEAGUES:
                text = R.string.colleagues;
                break;
            case CONTACTS:
                text = R.string.contacts;
                break;
            case FRIENDS:
            default:
                text = R.string.tab_friends;
                break;
            }
            holder.itemName.setText(text);
            holder.itemAvatar.setImageResource(R.drawable.more_messenger);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Intent launchIntent = new Intent(MoreActivity.this, UserFriendsActivity.class);
                    startActivity(launchIntent);
                }
            });

        } else if (position == MoreListAdapter.PROFILE_ACTIVITY) {
            holder.itemAvatar.setImageResource(R.drawable.more_id);
            holder.itemName.setText(R.string.profile);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Intent launchIntent = new Intent(MoreActivity.this, ProfileActivity.class);
                    startActivity(launchIntent);
                }
            });

        } else if (position == MoreListAdapter.SCAN_ACTIVITY) {
            holder.itemAvatar.setImageResource(R.drawable.more_qrcode);
            holder.itemName.setText(R.string.scan);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Intent launchIntent = new Intent(MoreActivity.this, ScanTabActivity.class);
                    startActivity(launchIntent);
                }
            });

        } else {
            L.bug("Trying to show MoreActivity but has not activity for this position");
        }
    }
}
