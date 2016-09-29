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

package com.mobicage.rogerthat.plugins.news;

import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.config.Configuration;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.plugins.MobicagePlugin;
import com.mobicage.rogerthat.util.db.DatabaseManager;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.CallReceiver;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.news.GetNewsItemsRequestTO;
import com.mobicage.to.news.GetNewsRequestTO;
import com.mobicage.to.news.NewsReadRequestTO;
import com.mobicage.to.news.NewsReadResponseTO;
import com.mobicage.to.news.NewsRogeredRequestTO;
import com.mobicage.to.news.NewsRogeredResponseTO;
import com.mobicage.to.system.SettingsTO;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class NewsPlugin implements MobicagePlugin {

    public static final String GET_NEWS_RECEIVED_INTENT = "com.mobicage.rogerthat.plugins.news.GET_NEWS_RECEIVED_INTENT";
    public static final String GET_NEWS_ITEMS_RECEIVED_INTENT = "com.mobicage.rogerthat.plugins.news.GET_NEWS_ITEMS_RECEIVED_INTENT";
    public static final String DELETE_NEWS_ITEM_INTENT = "com.mobicage.rogerthat.plugins.news.DELETE_NEWS_ITEM_INTENT";

    private static final String CONFIGKEY = "com.mobicage.rogerthat.plugins.news";
    private static final String NEWS_IDS = "news_ids";

    private final MainService mMainService;
    private final NewsStore mStore;
    private NewsCallReceiver mNewsCallReceiver;
    private final ConfigurationProvider mConfigProvider;

    private List<Long> mNewsIds = new ArrayList<>();

    public NewsPlugin(final MainService pMainService, ConfigurationProvider pConfigProvider,
                      final DatabaseManager pDatabaseManager) {
        T.UI();
        mMainService = pMainService;
        mConfigProvider = pConfigProvider;
        mStore = new NewsStore(pDatabaseManager, pMainService);

        Configuration cfg = mConfigProvider.getConfiguration(CONFIGKEY);
        final String newsIdsJSON = cfg.get(NEWS_IDS, null);
        if (newsIdsJSON != null) {
            JSONArray jsonNewsIds = (JSONArray) JSONValue.parse(newsIdsJSON);
            for (Object jsonNewsId : jsonNewsIds) {
                mNewsIds.add((Long) jsonNewsId);
            }
        }
    }

    @Override
    public void destroy() {
        T.UI();
        try {
            mStore.close();
        } catch (IOException e) {
            L.bug(e);
        }
        CallReceiver.comMobicageCapiNewsIClientRpc = null;
    }

    @Override
    public void initialize() {
        T.UI();
        reconfigure();

        mNewsCallReceiver = new NewsCallReceiver(mMainService, this);
        CallReceiver.comMobicageCapiNewsIClientRpc = mNewsCallReceiver;
    }

    @Override
    public void reconfigure() {
        T.UI();
    }

    @Override
    public void processSettings(SettingsTO settings) {
        // not used
    }

    public NewsStore getStore() {
        return mStore;
    }


    public List<Long> getNewsIdsFromDB() {
        return mNewsIds;
    }

    public void putNewsInDB(List<Long> newsIds) {
        boolean shouldUpdateList = false;
        if (mNewsIds.size() < newsIds.size()) {
            shouldUpdateList = true;
        } else {
            for (int i = 0; i < newsIds.size(); i++) {
                if (!mNewsIds.get(i).equals(newsIds.get(i))) {
                    shouldUpdateList = true;
                    break;
                }
            }
        }

        if (shouldUpdateList) {
            mNewsIds = newsIds;
            JSONArray jsonNewsIds = new JSONArray();
            for (Long newsId : mNewsIds) {
                jsonNewsIds.add(newsId);
            }
            Configuration cfg = mConfigProvider.getConfiguration(CONFIGKEY);
            cfg.put(NEWS_IDS, JSONValue.toJSONString(jsonNewsIds));
            mConfigProvider.updateConfigurationNow(CONFIGKEY, cfg);
        }
    }

    public void getNews(final String cursor, final String uuid) {
        SafeRunnable runnable = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                final GetNewsResponseHandler responseHandler = new GetNewsResponseHandler();
                responseHandler.setUUID(uuid);

                GetNewsRequestTO request = new GetNewsRequestTO();
                request.cursor = cursor;

                com.mobicage.api.news.Rpc.getNews(responseHandler, request);
            }
        };

        if (com.mobicage.rogerthat.util.system.T.getThreadType() == com.mobicage.rogerthat.util.system.T.BIZZ) {
            runnable.run();
        } else {
            mMainService.postAtFrontOfBIZZHandler(runnable);
        }
    }

    public void getNewsItems(final long[] ids, final Set<Long> updatedIds) {
        SafeRunnable runnable = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                final GetNewsItemsResponseHandler responseHandler = new GetNewsItemsResponseHandler();
                responseHandler.setUpdatedIds(updatedIds);

                GetNewsItemsRequestTO request = new GetNewsItemsRequestTO();
                request.ids = ids;

                com.mobicage.api.news.Rpc.getNewsItems(responseHandler, request);
            }
        };

        if (com.mobicage.rogerthat.util.system.T.getThreadType() == com.mobicage.rogerthat.util.system.T.BIZZ) {
            runnable.run();
        } else {
            mMainService.postAtFrontOfBIZZHandler(runnable);
        }
    }

    public void newsRead(final long[] ids) {
        SafeRunnable runnable = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                ResponseHandler<NewsReadResponseTO> responseHandler = new ResponseHandler<NewsReadResponseTO>();
                NewsReadRequestTO request = new NewsReadRequestTO();
                request.news_ids = ids;
                L.d("newsRead: " + request.news_ids);
                com.mobicage.api.news.Rpc.newsRead(responseHandler, request);
            }
        };

        if (com.mobicage.rogerthat.util.system.T.getThreadType() == com.mobicage.rogerthat.util.system.T.BIZZ) {
            runnable.run();
        } else {
            mMainService.postAtFrontOfBIZZHandler(runnable);
        }
    }

    public void newsRogered(final long id) {
        SafeRunnable runnable = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                ResponseHandler<NewsRogeredResponseTO> responseHandler = new ResponseHandler<NewsRogeredResponseTO>();
                NewsRogeredRequestTO request = new NewsRogeredRequestTO();
                request.news_id = id;
                L.d("newsRogered: " + request.news_id);
                com.mobicage.api.news.Rpc.newsRogered(responseHandler, request);
            }
        };

        if (com.mobicage.rogerthat.util.system.T.getThreadType() == com.mobicage.rogerthat.util.system.T.BIZZ) {
            runnable.run();
        } else {
            mMainService.postAtFrontOfBIZZHandler(runnable);
        }
    }
}
