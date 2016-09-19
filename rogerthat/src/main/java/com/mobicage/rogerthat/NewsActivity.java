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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.history.HistoryItem;
import com.mobicage.rogerthat.plugins.history.HistoryListAdapter;
import com.mobicage.rogerthat.plugins.history.HistoryPlugin;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.plugins.news.NewsPlugin;
import com.mobicage.rogerthat.plugins.news.NewsStore;
import com.mobicage.rogerthat.util.CachedDownloader;
import com.mobicage.rogerthat.util.DownloadImageTask;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.time.TimeUtils;
import com.mobicage.rogerthat.widget.Resizable16by6ImageView;
import com.mobicage.rogerthat.widget.Resizable16by9ImageView;
import com.mobicage.rpc.config.CloudConstants;
import com.mobicage.to.news.BaseNewsItemTO;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NewsActivity extends ServiceBoundActivity {

    private SwipeRefreshLayout swipeContainer;
    private NewsListAdapter mListAdapter;
    private ListView mListView;
    private NewsPlugin mNewsPlugin;
    private NewsStore mNewsStore;
    private FriendsPlugin mFriendsPlugin;
    private CachedDownloader mCachedDownloader;

    private Map<Long, Long> mDBItems = new HashMap<>();
    private List<Long> mOrder = new ArrayList<>();
    private List<Long> mLiveOrder = new ArrayList<>();
    private Map<Long, BaseNewsItemTO> mItems = new HashMap<>();
    private Map<String, ArrayList<Resizable16by6ImageView>> mImageViews = new HashMap<>();


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
            if (CachedDownloader.CACHED_DOWNLOAD_AVAILABLE_INTENT.equals(action)) {
                String url = intent.getStringExtra("url");

                File cachedFile = mCachedDownloader.getCachedFilePath(url);
                if (cachedFile != null) {
                    Bitmap bm = BitmapFactory.decodeFile(cachedFile.getAbsolutePath());

                    for (Resizable16by6ImageView image : mImageViews.get(url)) {
                        image.setImageBitmap(bm);
                        image.setVisibility(View.VISIBLE);
                    }
                }
            } else if (NewsPlugin.GET_NEWS_RECEIVED_INTENT.equals(action)) {
                boolean shouldUpdateLayout = false;

                if (swipeContainer.isRefreshing()) {
                    mOrder = new ArrayList<>();
                    mLiveOrder = new ArrayList<>();
                    mItems = new HashMap<>();
                    swipeContainer.setRefreshing(false);
                }

                Set<Long> idsToRequest = new LinkedHashSet<>();
                long[] ids = intent.getLongArrayExtra("ids");
                long[] versions = intent.getLongArrayExtra("versions");

                for (int i= 0 ; i < ids.length; i++) {
                    mLiveOrder.add(ids[i]);
                    if (!mDBItems.containsKey(ids[i])) {
                        idsToRequest.add(ids[i]);
                        L.i("GET_NEWS_RECEIVED_INTENT 1");
                    } else if (mDBItems.get(ids[i]) < versions[i]){
                        idsToRequest.add(ids[i]);
                        L.i("GET_NEWS_RECEIVED_INTENT 2");
                    } else if (!mOrder.contains(ids[i])) {
                        mItems.put(ids[i], mNewsStore.getNewsItem(ids[i]));
                        mOrder.add(ids[i]);
                        shouldUpdateLayout = true;
                        L.i("GET_NEWS_RECEIVED_INTENT 3");
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
                L.i("shouldUpdateLayout: "+ shouldUpdateLayout);

                if (shouldUpdateLayout) {
                    mListAdapter.notifyDataSetChanged();
                }

            } else if (NewsPlugin.GET_NEWS_ITEMS_RECEIVED_INTENT.equals(action)) {
                long[] ids = intent.getLongArrayExtra("ids");
                long[] versions = intent.getLongArrayExtra("versions");

                for (int i= 0 ; i < ids.length; i++) {
                    mDBItems.put(ids[i], versions[i]);
                    mOrder.add(ids[i]);
                    mItems.put(ids[i], mNewsStore.getNewsItem(ids[i]));
                }
                Collections.sort(mOrder, comparator);
                mListAdapter.notifyDataSetChanged();
            }
            return new String[] { action };
        }
    };

    @Override
    protected void onServiceBound() {
        mNewsPlugin = mService.getPlugin(NewsPlugin.class);
        mNewsStore = mNewsPlugin.getStore();
        mFriendsPlugin = mService.getPlugin(FriendsPlugin.class);
        mCachedDownloader = CachedDownloader.getInstance(getMainService());

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mNewsPlugin.getNews();
            }
        });
        swipeContainer.setColorSchemeResources(R.color.mc_primary_color, R.color.mc_secondary_color);

        mListView = (ListView) findViewById(R.id.news_list);
        mListAdapter = new NewsListAdapter(this);
        mListView.setAdapter(mListAdapter);
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

        mDBItems = mNewsStore.getNewsItemVersions();
        mNewsPlugin.getNews();

        final IntentFilter filter = new IntentFilter(CachedDownloader.CACHED_DOWNLOAD_AVAILABLE_INTENT);
        filter.addAction(NewsPlugin.GET_NEWS_RECEIVED_INTENT);
        filter.addAction(NewsPlugin.GET_NEWS_ITEMS_RECEIVED_INTENT);
        registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    protected void onServiceUnbound() {
    }

    public class NewsListAdapter extends BaseAdapter {

        private final LayoutInflater mLayoutInflater;
        private final Context mContext;

        public NewsListAdapter(Context context) {
            T.UI();
            mContext = context;
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

            Long newsId = mOrder.get(position);
            BaseNewsItemTO newsItem = mItems.get(newsId);

            Resizable16by6ImageView image = (Resizable16by6ImageView) view.findViewById(R.id.image);
            if (!TextUtils.isEmptyOrWhitespace(newsItem.image_url)) {
                if (mCachedDownloader.isStorageAvailable()) {
                    File cachedFile = mCachedDownloader.getCachedFilePath(newsItem.image_url);
                    if (cachedFile != null) {
                        Bitmap bm = BitmapFactory.decodeFile(cachedFile.getAbsolutePath());
                        image.setImageBitmap(bm);
                        image.setVisibility(View.VISIBLE);
                    } else {
                        if (!mImageViews.containsKey(newsItem.image_url)) {
                            mImageViews.put(newsItem.image_url, new ArrayList<Resizable16by6ImageView>());
                        }
                        mImageViews.get(newsItem.image_url).add(image);
                        // item started downloading intent when ready
                    }
                } else {
                    new DownloadImageTask(image).execute(newsItem.image_url);
                }
            }
            ImageView serviceAvatar = (ImageView) view.findViewById(R.id.service_avatar);
            // todo ruben we should check if friends else download
            new DownloadImageTask(serviceAvatar, true).execute(CloudConstants.CACHED_AVATAR_URL_PREFIX + newsItem.sender.avatar_id);

            TextView serviceName = (TextView) view.findViewById(R.id.service_name);
            serviceName.setText(newsItem.sender.name);

            TextView date = (TextView) view.findViewById(R.id.date);
            date.setText(TimeUtils.getDayTimeStr(NewsActivity.this, newsItem.timestamp * 1000));

            TextView title = (TextView) view.findViewById(R.id.title);
            title.setText(newsItem.title);
            TextView text = (TextView) view.findViewById(R.id.text);
            text.setText(newsItem.message);
            TextView reach = (TextView) view.findViewById(R.id.reach);
            reach.setText(newsItem.reach + "");

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

    private final Comparator<Long> comparator = new Comparator<Long>() {
        @Override
        public int compare(Long item1, Long item2) {
            int position1 = mLiveOrder.indexOf(item1);
            int position2 = mLiveOrder.indexOf(item2);
            return position1 > position2 ? 1 : -1;
        }
    };
}