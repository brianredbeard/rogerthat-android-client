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
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.news.NewsPlugin;
import com.mobicage.rogerthat.plugins.news.NewsStore;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.to.news.BaseNewsItemTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NewsActivity extends ServiceBoundActivity {

    protected NewsListAdapter mListAdapter;
    protected ListView mListView;
    private NewsPlugin mNewsPlugin;
    private NewsStore mNewsStore;
    private FriendsPlugin mFriendsPlugin;

    private Map<Long, Long> mDBItems = new HashMap<>();
    private List<Long> mOrder = new ArrayList<>();
    private Map<Long, BaseNewsItemTO> mItems = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news);
        setActivityName("news");
        setTitle(R.string.news);
    }

    private final BroadcastReceiver mBroadcastReceiver = new SafeBroadcastReceiver() {
        @Override
        public String[] onSafeReceive(Context context, Intent intent) {
            T.UI();
            String action = intent.getAction();
            if (NewsPlugin.GET_NEWS_RECEIVED_INTENT.equals(action)) {
                boolean shouldUpdateLayout = false;
                Set<Long> idsToRequest = new LinkedHashSet<>();
                long[] ids = intent.getLongArrayExtra("ids");
                long[] versions = intent.getLongArrayExtra("versions");

                for (int i= 0 ; i < ids.length; i++) {
                    if (!mDBItems.containsKey(ids[i])) {
                        idsToRequest.add(ids[i]);
                    } else if (mDBItems.get(ids[i]) < versions[i]){
                        idsToRequest.add(ids[i]);
                    } else if (!mOrder.contains(ids[i])) {
                        mItems.put(ids[i], mNewsStore.getNewsItem(ids[i]));
                        mOrder.add(ids[i]);
                        shouldUpdateLayout = true;
                    }
                }

                if (idsToRequest.size() > 0) {
                    long[] primitiveLongArray = new long[idsToRequest.size()];
                    Long[] longArray = idsToRequest.toArray(new Long[idsToRequest.size()]);
                    for (int i =0; i < longArray.length; i++) {
                        primitiveLongArray[i] = longArray[i].longValue();
                    }
                    mNewsPlugin.getNewsItems(primitiveLongArray);
                }
                if (shouldUpdateLayout) {
                    mListAdapter.notifyDataSetChanged();
                }

            } else if (NewsPlugin.GET_NEWS_ITEMS_RECEIVED_INTENT.equals(action)) {
                long[] ids = intent.getLongArrayExtra("ids");
                long[] versions = intent.getLongArrayExtra("versions");

                for (int i= 0 ; i < ids.length; i++) {
                    mDBItems.put(ids[i], versions[i]);
                    mItems.put(ids[i], mNewsStore.getNewsItem(ids[i]));
                    mOrder.add(ids[i]);
                }

                mListAdapter.notifyDataSetChanged();
            }
            return new String[] { action };
        }
    };

    protected void setListAdapater() {
        mListAdapter = new NewsListAdapter(this, mOrder, mItems);
        mListView.setAdapter(mListAdapter);
    }

    @Override
    protected void onServiceBound() {
        mNewsPlugin = mService.getPlugin(NewsPlugin.class);
        mNewsStore = mNewsPlugin.getStore();
        mFriendsPlugin = mService.getPlugin(FriendsPlugin.class);


        mListView = (ListView) findViewById(R.id.news_list);
        setListAdapater();
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                try {
                    final BaseNewsItemTO newsItem = (BaseNewsItemTO) view.getTag();
                    L.i("BaseNewsItemTO click: " + newsItem.id);
                } catch (Exception e) {
                    L.bug(e);
                }
            }
        });

        // todo ruben fill up mDBItems select id, version from News;
        mNewsPlugin.getNews();

        final IntentFilter filter = new IntentFilter();
        filter.addAction(NewsPlugin.GET_NEWS_RECEIVED_INTENT);
        filter.addAction(NewsPlugin.GET_NEWS_ITEMS_RECEIVED_INTENT);
        registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    protected void onServiceUnbound() {
    }

    public static class NewsListAdapter extends BaseAdapter {

        protected LayoutInflater mLayoutInflater;
        private final Context mContext;
        private List<Long> mOrder;
        private Map<Long, BaseNewsItemTO> mItems;

        public NewsListAdapter(Context context, List<Long> order, Map<Long, BaseNewsItemTO> items) {
            T.UI();
            mContext = context;
            mOrder = order;
            mItems = items;
            mLayoutInflater = LayoutInflater.from(mContext);
        }

        protected BaseNewsItemTO getNewsItem(int position) {
            Long newsId = mOrder.get(position);
            return mItems.get(newsId);
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

            BaseNewsItemTO newsItem = getNewsItem(position);

            TextView titleTextView = (TextView) view.findViewById(R.id.title);
            titleTextView.setText(newsItem.title);

            return view;
        }

        @Override
        public int getCount() {
            return mOrder.size();
        }

        @Override
        public Object getItem(int position) {
            return mOrder.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mOrder.get(position);
        }
    }
}