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
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;

import com.mobicage.rogerthat.util.db.DatabaseManager;
import com.mobicage.rogerthat.util.db.TransactionHelper;
import com.mobicage.rogerthat.util.db.TransactionWithoutResult;
import com.mobicage.rogerthat.util.system.T;

import com.mobicage.to.news.NewsActionButtonTO;
import com.mobicage.to.news.BaseNewsItemTO;
import com.mobicage.to.news.NewsSenderTO;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mobicage.rogerthat.util.db.DBUtils.bindString;

public class NewsStore implements Closeable {

    private final SQLiteStatement mInsertNewsItem;
    private final SQLiteStatement mInsertNewsButton;
    private final SQLiteStatement mInsertNewsRogeredUser;

    private final SQLiteStatement mUpdateNewsDirty;
    private final SQLiteStatement mUpdateNewsPinned;
    private final SQLiteStatement mUpdateNewsRogered;
    private final SQLiteStatement mUpdateNewsDeleted;

    private final SQLiteDatabase mDb;
    private final MainService mMainService;

    public NewsStore(final DatabaseManager databaseManager, final MainService mainService) {
        T.UI();
        mDb = databaseManager.getDatabase();
        mMainService = mainService;

        mInsertNewsItem = mDb.compileStatement(mMainService.getString(R.string.sql_news_insert_item));
        mInsertNewsButton = mDb.compileStatement(mMainService.getString(R.string.sql_news_insert_button));
        mInsertNewsRogeredUser = mDb.compileStatement(mMainService.getString(R.string.sql_news_insert_rogered_user));

        mUpdateNewsDirty = mDb.compileStatement(mMainService.getString(R.string.sql_news_update_dirty));
        mUpdateNewsPinned = mDb.compileStatement(mMainService.getString(R.string.sql_news_update_pinned));
        mUpdateNewsRogered = mDb.compileStatement(mMainService.getString(R.string.sql_news_update_rogered));
        mUpdateNewsDeleted = mDb.compileStatement(mMainService.getString(R.string.sql_news_update_deleted));
    }

    @Override
    public void close() throws IOException {
        T.UI();
        mInsertNewsItem.close();
        mInsertNewsButton.close();
        mInsertNewsRogeredUser.close();

        mUpdateNewsDirty.close();
        mUpdateNewsPinned.close();
        mUpdateNewsRogered.close();
        mUpdateNewsDeleted.close();
    }

    public void saveNewsItem(final BaseNewsItemTO item) {
        T.BIZZ();
        TransactionHelper.runInTransaction(mDb, "saveNewsItem", new TransactionWithoutResult() {
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
                bindString(mInsertNewsItem, 9, item.label);
                mInsertNewsItem.bindLong(10, item.reach);
                bindString(mInsertNewsItem, 11, item.qr_code_content);
                bindString(mInsertNewsItem, 12, item.qr_code_caption);
                mInsertNewsItem.bindLong(13, item.version);
                mInsertNewsItem.bindLong(14, item.flags);
                mInsertNewsItem.bindLong(15, 0); // dirty
                mInsertNewsItem.bindLong(16, 0); // pinned
                mInsertNewsItem.bindLong(17, 0); // rogererd
                mInsertNewsItem.bindLong(18, 0); // deleted

                // todo ruben we should update instead of insert or replace to keep dirty & pinned
                mInsertNewsItem.execute();

                for (int i = 0; i < item.buttons.length; i++) {
                    NewsActionButtonTO button = item.buttons[i];
                    mInsertNewsButton.bindLong(1, item.id);
                    bindString(mInsertNewsButton, 2, button.id);
                    mInsertNewsButton.bindString(3, button.caption);
                    bindString(mInsertNewsButton, 4, button.action);
                    mInsertNewsButton.bindLong(5, i);
                    mInsertNewsButton.execute();
                }

                for (int i = 0; i < item.users_that_rogered.length; i++) {
                    mInsertNewsRogeredUser.bindLong(1, item.id);
                    mInsertNewsRogeredUser.bindString(2, item.users_that_rogered[i]);
                    mInsertNewsRogeredUser.execute();
                }
            }
        });
    }

    public void setNewsItemDirty(final long newsId) {
        TransactionHelper.runInTransaction(mDb, "setNewsItemDirty", new TransactionWithoutResult() {
            @Override
            protected void run() {
                mUpdateNewsDirty.bindLong(1, 1);
                mUpdateNewsDirty.bindLong(2, newsId);
                mUpdateNewsDirty.execute();
            }
        });
    }

    public void setNewsItemPinned(final long newsId, final boolean pinned) {
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
        TransactionHelper.runInTransaction(mDb, "setNewsItemRogered", new TransactionWithoutResult() {
            @Override
            protected void run() {
                mUpdateNewsRogered.bindLong(1, 1);
                mUpdateNewsRogered.bindLong(2, newsId);
                mUpdateNewsRogered.execute();
            }
        });
    }

    public void setNewsItemDeleted(final long newsId) {
        TransactionHelper.runInTransaction(mDb, "setNewsItemDeleted", new TransactionWithoutResult() {
            @Override
            protected void run() {
                mUpdateNewsDeleted.bindLong(1, 1);
                mUpdateNewsDeleted.bindLong(2, newsId);
                mUpdateNewsDeleted.execute();
            }
        });
    }

    public NewsItem getNewsItem(long newsId) {
        T.dontCare();
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
            newsItem.label = c.getString(7);
            newsItem.reach = c.getLong(8);
            newsItem.qr_code_content = c.getString(9);
            newsItem.qr_code_caption = c.getString(10);
            newsItem.version = c.getLong(11);
            newsItem.flags = c.getLong(12);
            newsItem.dirty = c.getLong(13) > 0;
            newsItem.pinned = c.getLong(14) > 0;
            newsItem.rogered = c.getLong(15) > 0;
            newsItem.deleted = c.getLong(16) > 0;

            addButtons(newsItem);
            addRogeredUsers(newsItem);
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

    public Map<Long, NewsItemDetails> getNewsItemVersions() {
        T.dontCare();
        Map<Long, NewsItemDetails> dbVersions = new HashMap<>();
        final Cursor c = mDb.rawQuery(mMainService.getString(R.string.sql_news_list_item_versions),
                new String[]{});

        try {
            if (!c.moveToFirst()) {
                return dbVersions;
            }

            NewsItemDetails d = readDetails(c);
            dbVersions.put(d.id, d);
            while (c.moveToNext()) {
                NewsItemDetails d2 = readDetails(c);
                dbVersions.put(d2.id, d2);
            }

            return dbVersions;
        } finally {
            c.close();
        }
    }

    private NewsItemDetails readDetails(Cursor c) {
        NewsItemDetails d = new NewsItemDetails();
        d.id = c.getLong(0);
        d.version = c.getLong(1);
        d.dirty = c.getLong(2) > 0;
        d.pinned = c.getLong(3) > 0;
        d.rogered = c.getLong(4) > 0;
        d.deleted = c.getLong(5) > 0;
        return d;
    }
}
