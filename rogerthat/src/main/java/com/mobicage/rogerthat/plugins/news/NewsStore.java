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


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.util.db.DatabaseManager;
import com.mobicage.rogerthat.util.db.TransactionHelper;
import com.mobicage.rogerthat.util.db.TransactionWithoutResult;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.to.news.AppNewsItemTO;
import com.mobicage.to.news.NewsActionButtonTO;
import com.mobicage.to.news.NewsSenderTO;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mobicage.rogerthat.util.db.DBUtils.bindString;

public class NewsStore implements Closeable {

    private final SQLiteStatement mInsertPartialNewsItem;
    private final SQLiteStatement mInsertNewsItem;
    private final SQLiteStatement mInsertNewsButton;
    private final SQLiteStatement mInsertNewsRogeredUser;

    private final SQLiteStatement mUpdatePartialNewsItem;
    private final SQLiteStatement mUpdateNewsItem;
    private final SQLiteStatement mUpdateNewsRead;
    private final SQLiteStatement mUpdateNewsPinned;
    private final SQLiteStatement mUpdateNewsRogered;
    private final SQLiteStatement mUpdateNewsDisabled;
    private final SQLiteStatement mUpdateNewsReach;

    private final SQLiteStatement mDeleteNewsButtons;
    private final SQLiteStatement mDeleteNewsRogeredUsers;

    private final SQLiteStatement mCountNewsPinned;

    private final SQLiteDatabase mDb;
    private final MainService mMainService;

    private Map<Long, NewsItemDetails> mNewsItemDetailsCache = new HashMap<>();
    private Map<Long, NewsItem> mNewsItemsCache = new HashMap<>();

    public NewsStore(final DatabaseManager databaseManager, final MainService mainService) {
        T.UI();
        mDb = databaseManager.getDatabase();
        mMainService = mainService;

        mInsertPartialNewsItem = mDb.compileStatement(mMainService.getString(R.string.sql_news_insert_partial_item));
        mInsertNewsItem = mDb.compileStatement(mMainService.getString(R.string.sql_news_insert_item));
        mInsertNewsButton = mDb.compileStatement(mMainService.getString(R.string.sql_news_insert_button));
        mInsertNewsRogeredUser = mDb.compileStatement(mMainService.getString(R.string.sql_news_insert_rogered_user));

        mUpdatePartialNewsItem = mDb.compileStatement(mMainService.getString(R.string.sql_news_update_partial_item));
        mUpdateNewsItem = mDb.compileStatement(mMainService.getString(R.string.sql_news_update_item));
        mUpdateNewsRead = mDb.compileStatement(mMainService.getString(R.string.sql_news_update_read));
        mUpdateNewsPinned = mDb.compileStatement(mMainService.getString(R.string.sql_news_update_pinned));
        mUpdateNewsRogered = mDb.compileStatement(mMainService.getString(R.string.sql_news_update_rogered));
        mUpdateNewsDisabled = mDb.compileStatement(mMainService.getString(R.string.sql_news_update_disabled));
        mUpdateNewsReach = mDb.compileStatement(mMainService.getString(R.string.sql_news_update_reach));

        mDeleteNewsButtons = mDb.compileStatement(mMainService.getString(R.string.sql_news_delete_buttons));
        mDeleteNewsRogeredUsers = mDb.compileStatement(mMainService.getString(R.string.sql_news_delete_rogerthat_users));

        mCountNewsPinned = mDb.compileStatement(mMainService.getString(R.string.sql_news_count_pinned));
    }

    @Override
    public void close() throws IOException {
        T.UI();
        mInsertPartialNewsItem.close();
        mInsertNewsItem.close();
        mInsertNewsButton.close();
        mInsertNewsRogeredUser.close();

        mUpdatePartialNewsItem.close();
        mUpdateNewsItem.close();
        mUpdateNewsRead.close();
        mUpdateNewsPinned.close();
        mUpdateNewsRogered.close();
        mUpdateNewsDisabled.close();
        mUpdateNewsReach.close();

        mDeleteNewsButtons.close();
        mDeleteNewsRogeredUsers.close();

        mCountNewsPinned.close();
    }

    public void savePartialNewsItem(final long id, final long version, final long sortTimestamp, final long sortPriority) {
        mNewsItemsCache.remove(id);
        if (mNewsItemDetailsCache.containsKey(id)) {
            updatePartialNewsItem(id, version, sortTimestamp, sortPriority);
        } else {
            insertPartialNewsItem(id, version, sortTimestamp, sortPriority);
        }
    }

    public void insertPartialNewsItem(final long id, final long version, final long sortTimestamp, final long sortPriority) {
        NewsItemDetails d = new NewsItemDetails();
        d.id = id;
        d.version = version;
        d.sortTimestamp = sortTimestamp;
        d.sortPriority = sortPriority;
        d.isPartial = true;
        d.read = false;
        mNewsItemDetailsCache.put(id, d);

        TransactionHelper.runInTransaction(mDb, "insertPartialNewsItem", new TransactionWithoutResult() {
            @Override
            protected void run() {
                mInsertPartialNewsItem.bindLong(1, id);
                mInsertPartialNewsItem.bindLong(2, version);
                mInsertPartialNewsItem.bindLong(3, sortTimestamp);
                mInsertPartialNewsItem.bindLong(4, sortPriority);
                mInsertPartialNewsItem.execute();
            }
        });
    }

    public void updatePartialNewsItem(final long id, final long version, final long sortTimestamp, final long sortPriority) {
        if (mNewsItemDetailsCache.containsKey(id)) {
            mNewsItemDetailsCache.get(id).version = version;
            mNewsItemDetailsCache.get(id).sortTimestamp = sortTimestamp;
            mNewsItemDetailsCache.get(id).sortPriority = sortPriority;
            mNewsItemDetailsCache.get(id).isPartial = true;
        }

        TransactionHelper.runInTransaction(mDb, "updatePartialNewsItem", new TransactionWithoutResult() {
            @Override
            protected void run() {
                mUpdatePartialNewsItem.bindLong(1, version);
                mUpdatePartialNewsItem.bindLong(2, sortTimestamp);
                mUpdatePartialNewsItem.bindLong(3, sortPriority);
                // WHERE
                mUpdatePartialNewsItem.bindLong(44, id);
                mUpdatePartialNewsItem.execute();
            }
        });
    }

    public boolean insertNewsItem(final AppNewsItemTO item) {
        if (mNewsItemDetailsCache.containsKey(item.id)) {
            L.bug("news channel send new item that i already have...");
            return false;
        }

        NewsItemDetails d = new NewsItemDetails();
        d.id = item.id;
        d.version = item.version;
        d.sortTimestamp = item.sort_timestamp;
        d.sortPriority = item.sort_priority;
        d.isPartial = true;
        d.read = false;
        mNewsItemDetailsCache.put(item.id, d);

        TransactionHelper.runInTransaction(mDb, "insertNewsItem", new TransactionWithoutResult() {
            @Override
            protected void run() {
                mInsertNewsItem.bindLong(1, item.id);
                mInsertNewsItem.bindLong(2, item.timestamp);
                mInsertNewsItem.bindString(3, item.sender.email);
                mInsertNewsItem.bindString(4, item.sender.name);
                mInsertNewsItem.bindLong(5, item.sender.avatar_id);
                bindString(mInsertNewsItem, 6, item.title);
                bindString(mInsertNewsItem, 7, item.message);
                bindString(mInsertNewsItem, 8, item.image_url);
                bindString(mInsertNewsItem, 9, item.broadcast_type);
                mInsertNewsItem.bindLong(10, item.reach);
                bindString(mInsertNewsItem, 11, item.qr_code_content);
                bindString(mInsertNewsItem, 12, item.qr_code_caption);
                mInsertNewsItem.bindLong(13, item.version);
                mInsertNewsItem.bindLong(14, item.flags);
                mInsertNewsItem.bindLong(15, item.type);
                mInsertNewsItem.bindLong(16, 0); // dirty
                mInsertNewsItem.bindLong(17, 0); // pinned
                mInsertNewsItem.bindLong(18, 0); // rogererd
                mInsertNewsItem.bindLong(19, 0); // deleted
                mInsertNewsItem.bindLong(20, item.sort_timestamp);
                mInsertNewsItem.bindLong(21, item.sort_priority);
                mInsertNewsItem.execute();

                insertButtons(item);
                insertUsers(item);
            }
        });

        return true;
    }

    public void saveNewsItem(final AppNewsItemTO item) {
        mNewsItemsCache.remove(item.id);
        if (mNewsItemDetailsCache.containsKey(item.id)) {
            mNewsItemDetailsCache.get(item.id).version = item.version;
            mNewsItemDetailsCache.get(item.id).sortTimestamp = item.sort_timestamp;
            mNewsItemDetailsCache.get(item.id).sortPriority = item.sort_priority;
            mNewsItemDetailsCache.get(item.id).isPartial = false;
        }

        TransactionHelper.runInTransaction(mDb, "updateNewsItem", new TransactionWithoutResult() {
            @Override
            protected void run() {
                mUpdateNewsItem.bindLong(1, item.timestamp);
                mUpdateNewsItem.bindString(2, item.sender.email);
                mUpdateNewsItem.bindString(3, item.sender.name);
                mUpdateNewsItem.bindLong(4, item.sender.avatar_id);
                bindString(mUpdateNewsItem, 5, item.title);
                bindString(mUpdateNewsItem, 6, item.message);
                bindString(mUpdateNewsItem, 7, item.image_url);
                bindString(mUpdateNewsItem, 8, item.broadcast_type);
                mUpdateNewsItem.bindLong(9, item.reach);
                bindString(mUpdateNewsItem, 10, item.qr_code_content);
                bindString(mUpdateNewsItem, 11, item.qr_code_caption);
                mUpdateNewsItem.bindLong(12, item.version);
                mUpdateNewsItem.bindLong(13, item.flags);
                // WHERE
                mUpdateNewsItem.bindLong(14, item.id);
                mUpdateNewsItem.execute();

                insertButtons(item);
                insertUsers(item);
            }
        });
    }

    private void insertButtons(final AppNewsItemTO item) {
        mDeleteNewsButtons.bindLong(1, item.id);
        mDeleteNewsButtons.execute();

        for (int i = 0; i < item.buttons.length; i++) {
            NewsActionButtonTO button = item.buttons[i];
            mInsertNewsButton.bindLong(1, item.id);
            bindString(mInsertNewsButton, 2, button.id);
            mInsertNewsButton.bindString(3, button.caption);
            bindString(mInsertNewsButton, 4, button.action);
            bindString(mInsertNewsButton, 5, button.flow_params);
            mInsertNewsButton.bindLong(6, i);
            mInsertNewsButton.execute();
        }
    }

    private void insertUsers(final AppNewsItemTO item) {
        mDeleteNewsRogeredUsers.bindLong(1, item.id);
        mDeleteNewsRogeredUsers.execute();

        for (int i = 0; i < item.users_that_rogered.length; i++) {
            addUser(item.id, item.users_that_rogered[i]);
        }
    }

    public void addUser(final long newsId, final String email) {
        mNewsItemsCache.remove(newsId);
        mInsertNewsRogeredUser.bindLong(1, newsId);
        mInsertNewsRogeredUser.bindString(2, email);
        mInsertNewsRogeredUser.execute();
    }

    public void setNewsItemRead(final long newsId) {
        if (mNewsItemsCache.containsKey(newsId)) {
            mNewsItemsCache.get(newsId).read = true;
        }
        if (mNewsItemDetailsCache.containsKey(newsId)) {
            mNewsItemDetailsCache.get(newsId).read = true;
        }

        TransactionHelper.runInTransaction(mDb, "setNewsItemRead", new TransactionWithoutResult() {
            @Override
            protected void run() {
                mUpdateNewsRead.bindLong(1, 1);
                mUpdateNewsRead.bindLong(2, newsId);
                mUpdateNewsRead.execute();
            }
        });
    }

    public void setNewsItemPinned(final long newsId, final boolean pinned) {
        if (mNewsItemsCache.containsKey(newsId)) {
            mNewsItemsCache.get(newsId).pinned = pinned;
        }
        TransactionHelper.runInTransaction(mDb, "setNewsItemPinned", new TransactionWithoutResult() {
            @Override
            protected void run() {
                mUpdateNewsPinned.bindLong(1, pinned ? 1: 0);
                mUpdateNewsPinned.bindLong(2, newsId);
                mUpdateNewsPinned.execute();
            }
        });
    }

    public void setNewsItemRogered(final long newsId) {
        if (mNewsItemsCache.containsKey(newsId)) {
            mNewsItemsCache.get(newsId).rogered = true;
        }
        TransactionHelper.runInTransaction(mDb, "setNewsItemRogered", new TransactionWithoutResult() {
            @Override
            protected void run() {
                mUpdateNewsRogered.bindLong(1, 1);
                mUpdateNewsRogered.bindLong(2, newsId);
                mUpdateNewsRogered.execute();
            }
        });
    }

    public void setNewsItemDisabled(final long newsId) {
        if (mNewsItemsCache.containsKey(newsId)) {
            mNewsItemsCache.get(newsId).disabled = true;
        }
        TransactionHelper.runInTransaction(mDb, "setNewsItemDisabled", new TransactionWithoutResult() {
            @Override
            protected void run() {
                mUpdateNewsDisabled.bindLong(1, 1);
                mUpdateNewsDisabled.bindLong(2, newsId);
                mUpdateNewsDisabled.execute();
            }
        });
    }

    public void setNewsItemReach(final long newsId, final long reach) {
        if (mNewsItemsCache.containsKey(newsId)) {
            mNewsItemsCache.get(newsId).reach = reach;
        }
        TransactionHelper.runInTransaction(mDb, "setNewsItemReach", new TransactionWithoutResult() {
            @Override
            protected void run() {
                mUpdateNewsDisabled.bindLong(1, reach);
                mUpdateNewsDisabled.bindLong(2, newsId);
                mUpdateNewsDisabled.execute();
            }
        });
    }

    public NewsItem getNewsItem(long newsId) {
        T.dontCare();
        if (mNewsItemsCache.containsKey(newsId)) {
            return mNewsItemsCache.get(newsId);
        }

        final Cursor c = mDb.rawQuery(mMainService.getString(R.string.sql_news_get_item),
                new String[] { "" + newsId });

        try {
            if (!c.moveToFirst()) {
                return null;
            }
            NewsItem newsItem = new NewsItem();
            newsItem.id = newsId;
            newsItem.timestamp = c.getLong(0);
            newsItem.sender = new NewsSenderTO();
            newsItem.sender.email = c.getString(1);
            newsItem.sender.name = c.getString(2);
            newsItem.sender.avatar_id = c.getLong(3);
            newsItem.title = c.getString(4);
            newsItem.message = c.getString(5);
            newsItem.image_url = c.getString(6);
            newsItem.broadcast_type = c.getString(7);
            newsItem.reach = c.getLong(8);
            newsItem.qr_code_content = c.getString(9);
            newsItem.qr_code_caption = c.getString(10);
            newsItem.version = c.getLong(11);
            newsItem.flags = c.getLong(12);
            newsItem.type = c.getLong(13);
            newsItem.read = c.getLong(14) > 0;
            newsItem.pinned = c.getLong(15) > 0;
            newsItem.rogered = c.getLong(16) > 0;
            newsItem.disabled = c.getLong(17) > 0;
            newsItem.isPartial = c.getLong(18) > 0;

            addButtons(newsItem);
            addRogeredUsers(newsItem);

            mNewsItemsCache.put(newsId, newsItem);
            return newsItem;
        } finally {
            c.close();
        }
    }

    private void addButtons(NewsItem newsItem) {
        final Cursor c = mDb.rawQuery(mMainService.getString(R.string.sql_news_get_buttons),
                new String[] { "" + newsItem.id });
        try {
            if (!c.moveToFirst()) {
                newsItem.buttons = new NewsActionButtonTO[0];
                return;
            }
            List<NewsActionButtonTO> buttons = new ArrayList<NewsActionButtonTO>();
            buttons.add(readButton(c));
            while (c.moveToNext())
                buttons.add(readButton(c));
            newsItem.buttons = buttons.toArray(new NewsActionButtonTO[buttons.size()]);
        } finally {
            c.close();
        }
    }

    private NewsActionButtonTO readButton(Cursor bcurs) {
        NewsActionButtonTO button = new NewsActionButtonTO();
        button.id = bcurs.getString(0);
        button.caption = bcurs.getString(1);
        button.action = bcurs.getString(2);
        button.flow_params = bcurs.getString(3);
        return button;
    }

    public void addRogeredUsers(NewsItem newsItem) {
        final Cursor c = mDb.rawQuery(mMainService.getString(R.string.sql_news_get_rogered_users),
                new String[] { "" + newsItem.id });
        try {
            if (!c.moveToFirst()) {
                newsItem.users_that_rogered = new String[0];
                return;
            }
            List<String> rogeredUsers = new ArrayList<String>();
            rogeredUsers.add(c.getString(0));
            while (c.moveToNext())
                rogeredUsers.add(c.getString(0));

            newsItem.users_that_rogered = rogeredUsers.toArray(new String[rogeredUsers.size()]);
        } finally {
            c.close();
        }
    }

    public void fillNewsItemDetailsCache() {
        T.dontCare();
        mNewsItemDetailsCache = new HashMap<>();
        final Cursor c = mDb.rawQuery(mMainService.getString(R.string.sql_news_list_item_versions),
                new String[]{});

        try {
            if (!c.moveToFirst()) {
                return;
            }

            NewsItemDetails d = readDetails(c);
            mNewsItemDetailsCache.put(d.id, d);
            while (c.moveToNext()) {
                NewsItemDetails d2 = readDetails(c);
                mNewsItemDetailsCache.put(d2.id, d2);
            }

            return;
        } finally {
            c.close();
        }
    }

    public Map<Long, NewsItemDetails> getNewsItemDetailsCache() {
        return mNewsItemDetailsCache;
    }

    public NewsItemDetails getNewsItemDetails(long newsId) {
        if (mNewsItemDetailsCache.containsKey(newsId)) {
            return mNewsItemDetailsCache.get(newsId);
        }
        return null;
    }

    public long countNewsItems() {
        return mNewsItemDetailsCache.size();
    }

    public long countPinnedItems() {
        return mCountNewsPinned.simpleQueryForLong();
    }

    public List<Long> searchPinnedNews(String qry) {
        T.dontCare();

        String query = "%" + qry + "%";

        List<Long> newsIds = new ArrayList<>();
        final Cursor c = mDb.rawQuery(mMainService.getString(R.string.sql_news_search_pinned),
                new String[]{query, query, query, query, query});

        try {
            if (!c.moveToFirst()) {
                return newsIds;
            }
            newsIds.add(c.getLong(0));
            while (c.moveToNext()) {
                newsIds.add(c.getLong(0));
            }
            return newsIds;
        } finally {
            c.close();
        }
    }

    private NewsItemDetails readDetails(Cursor c) {
        NewsItemDetails d = new NewsItemDetails();
        d.id = c.getLong(0);
        d.version = c.getLong(1);
        d.sortTimestamp = c.getLong(2);
        d.sortPriority = c.getLong(3);
        d.isPartial = c.getLong(4) > 0;
        d.read = c.getLong(5) > 0;
        return d;
    }

    public void clearCache() {
        mNewsItemDetailsCache.clear();
        mNewsItemsCache.clear();
    }

}
