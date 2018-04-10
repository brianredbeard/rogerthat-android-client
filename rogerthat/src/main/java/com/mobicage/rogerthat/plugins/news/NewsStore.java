/*
 * Copyright 2018 GIG Technology NV
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
 * @@license_version:1.4@@
 */
package com.mobicage.rogerthat.plugins.news;


import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.util.DebugUtils;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.db.DatabaseManager;
import com.mobicage.rogerthat.util.db.Transaction;
import com.mobicage.rogerthat.util.db.TransactionHelper;
import com.mobicage.rogerthat.util.db.TransactionWithoutResult;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.to.news.AppNewsInfoTO;
import com.mobicage.to.news.AppNewsItemTO;
import com.mobicage.to.news.NewsActionButtonTO;
import com.mobicage.to.news.NewsSenderTO;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.mobicage.rogerthat.util.db.DBUtils.bindAsEmptyString;
import static com.mobicage.rogerthat.util.db.DBUtils.bindString;

public class NewsStore implements Closeable {

    private final SQLiteStatement mCountNewsItems;
    private final SQLiteStatement mCountFeedNewsItems;
    private final SQLiteStatement mCountAllNewsItems;
    private final SQLiteStatement mCountAllNewsItemsService;
    private final SQLiteStatement mCountNewsPinnedItems;
    private final SQLiteStatement mCountNewsPinnedItemsSearch;
    private final SQLiteStatement mCountNewsItemsWithId;

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
    private final SQLiteStatement mUpdateSortIndexes;
    private final SQLiteStatement mUpdateSortPriority;

    private final SQLiteStatement mDeleteNewsButtons;
    private final SQLiteStatement mDeleteNewsRogeredUsers;

    private final SQLiteStatement mClearNewsItems;

    private final SQLiteDatabase mDb;
    private final MainService mMainService;

    public NewsStore(final DatabaseManager databaseManager, final MainService mainService) {
        T.UI();
        mDb = databaseManager.getDatabase();
        mMainService = mainService;

        mCountNewsItems = mDb.compileStatement(mMainService.getString(R.string.sql_news_count_items));
        mCountFeedNewsItems = mDb.compileStatement(mainService.getString(R.string.sql_news_count_feed_items));
        mCountAllNewsItems = mDb.compileStatement(mMainService.getString(R.string.sql_news_count_all_items));
        mCountAllNewsItemsService = mDb.compileStatement(mMainService.getString(R.string.sql_news_count_all_items_service));
        mCountNewsPinnedItems = mDb.compileStatement(mMainService.getString(R.string.sql_news_count_pinned_items));
        mCountNewsPinnedItemsSearch = mDb.compileStatement(mMainService.getString(R.string.sql_news_count_pinned_items_search));
        mCountNewsItemsWithId = mDb.compileStatement(mMainService.getString(R.string.sql_news_get_item_existence));

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
        mUpdateSortIndexes = mDb.compileStatement(mMainService.getString(R.string.sql_news_reindex));
        mUpdateSortPriority = mDb.compileStatement(mMainService.getString(R.string.sql_news_update_sort_priority));

        mDeleteNewsButtons = mDb.compileStatement(mMainService.getString(R.string.sql_news_delete_buttons));
        mDeleteNewsRogeredUsers = mDb.compileStatement(mMainService.getString(R.string.sql_news_delete_rogerthat_users));

        mClearNewsItems = mDb.compileStatement(mMainService.getString(R.string.sql_news_clear_all));
    }

    @Override
    public void close() throws IOException {
        T.UI();

        mCountNewsItems.close();
        mCountFeedNewsItems.close();
        mCountAllNewsItems.close();
        mCountAllNewsItemsService.close();
        mCountNewsPinnedItems.close();
        mCountNewsPinnedItemsSearch.close();
        mCountNewsItemsWithId.close();

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
        mUpdateSortIndexes.close();
        mUpdateSortPriority.close();

        mDeleteNewsButtons.close();
        mDeleteNewsRogeredUsers.close();

        mClearNewsItems.close();
    }

    public Map<String, List<AppNewsInfoTO>> savePartialNewsItems(final AppNewsInfoTO[] partialNewsItems) {
        return TransactionHelper.runInTransaction(mDb, "savePartialNewsItems", new Transaction<Map<String, List<AppNewsInfoTO>>>() {
            @Override
            protected Map<String, List<AppNewsInfoTO>> run() {
                Map<String, List<AppNewsInfoTO>> result = new HashMap<>();
                List<AppNewsInfoTO> newItems = new ArrayList<>();
                List<AppNewsInfoTO> updatedItems = new ArrayList<>();

                long newsCount = countNewsItems();

                for (int i = 0; i < partialNewsItems.length; i++) {
                    AppNewsInfoTO partialNewsItem = partialNewsItems[i];
                    long sortPriority = newsCount > 0 ? NewsPlugin.SORT_PRIORITY_BOTTOM : partialNewsItem.sort_priority;

                    long extraSeconds = 0;
                    while (true) {
                        try {
                            mInsertPartialNewsItem.bindLong(1, partialNewsItem.id);
                            mInsertPartialNewsItem.bindLong(2, partialNewsItem.version);
                            mInsertPartialNewsItem.bindLong(3, partialNewsItem.sort_timestamp + extraSeconds);
                            mInsertPartialNewsItem.bindLong(4, partialNewsItem.sort_priority);
                            mInsertPartialNewsItem.bindLong(5, sortPriority);
                            mInsertPartialNewsItem.bindLong(6, partialNewsItem.sort_timestamp + extraSeconds);
                            mInsertPartialNewsItem.bindLong(7, newsCount > 0 ? 1 : 0);
                            mInsertPartialNewsItem.bindString(8, partialNewsItem.sender_email);
                            bindString(mInsertPartialNewsItem, 9, partialNewsItem.broadcast_type);
                            bindAsEmptyString(mInsertPartialNewsItem, 10, partialNewsItem.feed_name);
                            mInsertPartialNewsItem.execute();
                            newItems.add(partialNewsItem);
                            break;
                        } catch (SQLiteConstraintException e) {
                            try {
                                mUpdatePartialNewsItem.bindLong(1, partialNewsItem.version);
                                mUpdatePartialNewsItem.bindLong(2, partialNewsItem.sort_timestamp + extraSeconds);
                                mUpdatePartialNewsItem.bindLong(3, partialNewsItem.sort_priority);
                                mUpdatePartialNewsItem.bindLong(4, sortPriority);
                                mUpdatePartialNewsItem.bindLong(5, partialNewsItem.sort_timestamp + extraSeconds);
                                mUpdatePartialNewsItem.bindLong(6, newsCount > 0 ? 1 : 0);
                                mUpdatePartialNewsItem.bindString(7, partialNewsItem.sender_email);
                                bindString(mUpdatePartialNewsItem, 8, partialNewsItem.broadcast_type);
                                // WHERE
                                mUpdatePartialNewsItem.bindLong(9, partialNewsItem.id);
                                if (mUpdatePartialNewsItem.executeUpdateDelete() == 1) {
                                    updatedItems.add(partialNewsItem);
                                    break;
                                }
                            } catch (SQLiteConstraintException e2) {
                            }
                        }
                        extraSeconds++;
                    }
                }

                result.put("new", newItems);
                result.put("updated", updatedItems);
                return result;
            }
        });
    }

//    public void logNewsItems(String methodName) {
//        final Cursor c = mDb.rawQuery("select id, title, sort_key, sort_priority, sort_timestamp, reindex from news ORDER BY sort_key DESC LIMIT 5;",
//                new String[]{});
//
//        try {
//            if (!c.moveToFirst()) {
//                return;
//            }
//            do {
//                long id = c.getLong(0);
//                String title = c.getString(1);
//                long sortKey = c.getLong(2);
//                long sortPriority = c.getLong(3);
//                long sortTimestamp = c.getLong(4);
//                long reindex = c.getLong(5);
//
//                L.d("--- " + methodName + ": " + id + "|" + title + "|" + sortKey + "|" + sortPriority + "|" + sortTimestamp + "|" + reindex);
//
//            } while (c.moveToNext());
//
//        } finally {
//            c.close();
//        }
//    }

    public void clearNewsItems() {
        T.dontCare();
        TransactionHelper.runInTransaction(mDb, "clearNewsItems", new TransactionWithoutResult() {
            @Override
            protected void run() {
                mClearNewsItems.execute();
            }
        });
    }


    public boolean insertNewsItem(final AppNewsItemTO item) {
        T.BIZZ();

        return TransactionHelper.runInTransaction(mDb, "insertNewsItem", new Transaction<Boolean>() {
            @Override
            protected Boolean run() {
                long extraSeconds = 0;
                boolean didCheckIfAlreadyExists = false;
                while (true) {
                    try {
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
                        mInsertNewsItem.bindLong(16, 0); // read
                        mInsertNewsItem.bindLong(17, 0); // pinned
                        mInsertNewsItem.bindLong(18, 0); // rogererd
                        mInsertNewsItem.bindLong(19, 0); // disabled
                        mInsertNewsItem.bindLong(20, item.sort_timestamp + extraSeconds);
                        mInsertNewsItem.bindLong(21, item.sort_priority);
                        mInsertNewsItem.bindLong(22, NewsPlugin.SORT_PRIORITY_BOTTOM);
                        mInsertNewsItem.bindLong(23, item.sort_timestamp + extraSeconds);
                        bindAsEmptyString(mInsertNewsItem, 24, item.feed_name);
                        mInsertNewsItem.execute();
                        break;
                    } catch (SQLiteConstraintException e) {
                        if (!didCheckIfAlreadyExists) {
                            didCheckIfAlreadyExists = true;
                            if (newsItemExists(item.id)) {
                                return false;
                            }
                        }
                        extraSeconds++;
                    }
                }

                insertButtons(item);
                insertUsers(item);

                return true;
            }
        });
    }

    public void updateNewsItem(final AppNewsItemTO item) {
        T.BIZZ();

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
                mUpdateNewsItem.bindLong(14, item.type);
                // WHERE
                mUpdateNewsItem.bindLong(15, item.id);
                mUpdateNewsItem.execute();

                insertButtons(item);
                insertUsers(item);
            }
        });
    }

    private void insertButtons(final AppNewsItemTO item) {
        T.BIZZ();
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
        T.BIZZ();
        mDeleteNewsRogeredUsers.bindLong(1, item.id);
        mDeleteNewsRogeredUsers.execute();

        for (int i = 0; i < item.users_that_rogered.length; i++) {
            addUser(item.id, item.users_that_rogered[i]);
        }
    }

    public void addUser(final long newsId, final String email) {
        T.BIZZ();
        mInsertNewsRogeredUser.bindLong(1, newsId);
        mInsertNewsRogeredUser.bindString(2, email);
        mInsertNewsRogeredUser.execute();
    }

    public boolean setNewsItemRead(final long newsId) {
        T.BIZZ();
        return TransactionHelper.runInTransaction(mDb, "setNewsItemRead", new Transaction<Boolean>() {
            @Override
            protected Boolean run() {
                mUpdateNewsRead.bindLong(1, 1);
                mUpdateNewsRead.bindLong(2, newsId);
                if (mUpdateNewsRead.executeUpdateDelete() > 0) {
                    return true;
                }
                return false;
            }
        });
    }

    public void setNewsItemSortPriority(final long id, final long sortPriority) {
        T.dontCare();
        TransactionHelper.runInTransaction(mDb, "setNewsItemSortPriority", new TransactionWithoutResult() {
            @Override
            protected void run() {
                mUpdateSortPriority.bindLong(1, sortPriority);
                // WHERE
                mUpdateSortPriority.bindLong(2, id);
                mUpdateSortPriority.bindLong(3, sortPriority);
                int i = mUpdateSortPriority.executeUpdateDelete();
                L.d("setNewsItemSortPriority updated " + i + " rows");
            }
        });
    }

    public boolean setNewsItemPinned(final long newsId, final boolean pinned) {
        T.UI();
        return TransactionHelper.runInTransaction(mDb, "setNewsItemPinned", new Transaction<Boolean>() {
            @Override
            protected Boolean run() {
                mUpdateNewsPinned.bindLong(1, pinned ? 1: 0);
                mUpdateNewsPinned.bindLong(2, newsId);
                if (mUpdateNewsPinned.executeUpdateDelete() > 0) {
                    return true;
                }
                return false;
            }
        });
    }

    public void setNewsItemRogered(final long newsId) {
        T.BIZZ();
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
        T.BIZZ();
        TransactionHelper.runInTransaction(mDb, "setNewsItemDisabled", new TransactionWithoutResult() {
            @Override
            protected void run() {
                mUpdateNewsDisabled.bindLong(1, 1);
                mUpdateNewsDisabled.bindLong(2, newsId);
                mUpdateNewsDisabled.execute();
            }
        });
    }

    public boolean newsItemExists(long newsId) {
        mCountNewsItemsWithId.bindLong(1, newsId);
        return mCountNewsItemsWithId.simpleQueryForLong() > 0;
    }

    public NewsItem getNewsItem(long newsId) {
        T.dontCare(); // T.UI() or T.BIZZ();

        final Cursor c = mDb.rawQuery(mMainService.getString(R.string.sql_news_get_item),
                new String[] { "" + newsId });

        try {
            if (!c.moveToFirst()) {
                return null;
            }

            return readNewsItem(c);
        } finally {
            c.close();
        }
    }

    private void addButtons(NewsItem newsItem) {
        T.dontCare(); // T.UI() or T.BIZZ();
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
        T.dontCare(); // T.UI() or T.BIZZ();
        NewsActionButtonTO button = new NewsActionButtonTO();
        button.id = bcurs.getString(0);
        button.caption = bcurs.getString(1);
        button.action = bcurs.getString(2);
        button.flow_params = bcurs.getString(3);
        return button;
    }

    public void addRogeredUsers(NewsItem newsItem) {
        T.dontCare(); // T.UI() or T.BIZZ();
        final Cursor c = mDb.rawQuery(mMainService.getString(R.string.sql_news_get_rogered_users),
                new String[] { "" + newsItem.id });
        try {
            if (!c.moveToFirst()) {
                newsItem.users_that_rogered = new String[0];
                return;
            }
            List<String> rogeredUsers = new ArrayList<String>();

            do {
                rogeredUsers.add(c.getString(0));
            } while (c.moveToNext());

            newsItem.users_that_rogered = rogeredUsers.toArray(new String[rogeredUsers.size()]);
        } finally {
            c.close();
        }
    }

    public void addRogeredUsers(NewsItemIndex ni) {
        T.dontCare(); // T.UI() or T.BIZZ();
        final Cursor c = mDb.rawQuery(mMainService.getString(R.string.sql_news_get_rogered_users),
                new String[]{"" + ni.id});
        ni.usersThatRogered = new ArrayList<>();
        try {
            if (!c.moveToFirst()) {
                return;
            }
            do {
                ni.usersThatRogered.add(c.getString(0));
            } while (c.moveToNext());


        } finally {
            c.close();
        }
    }

    public void reindexSortKeys(final String feedName) {
        TransactionHelper.runInTransaction(mDb, "reindexSortKeys", new TransactionWithoutResult() {
            @Override
            protected void run() {
                bindAsEmptyString(mUpdateSortIndexes, 1, feedName);
                int i = mUpdateSortIndexes.executeUpdateDelete();
                L.d("reindexSortKeys updated " + i + " rows");
            }
        });

    }

    public long countNewsItems() {
        return mCountNewsItems.simpleQueryForLong();
    }

    public long countFeedNewsItems(String feedName) {
        bindAsEmptyString(mCountFeedNewsItems, 1, feedName);
        return mCountFeedNewsItems.simpleQueryForLong();
    }

    public long countNewsPinnedItems(String feedName) {
        bindAsEmptyString(mCountNewsPinnedItems, 1, feedName);
        return mCountNewsPinnedItems.simpleQueryForLong();
    }

    public long countNewsPinnedItemsSearch(String feedName, String qry) {
        String query = "%" + qry + "%";
        mCountNewsPinnedItemsSearch.bindString(1, query);
        mCountNewsPinnedItemsSearch.bindString(2, query);
        mCountNewsPinnedItemsSearch.bindString(3, query);
        mCountNewsPinnedItemsSearch.bindString(4, query);
        mCountNewsPinnedItemsSearch.bindString(5, query);
        bindAsEmptyString(mCountNewsPinnedItemsSearch, 6, feedName);
        return mCountNewsPinnedItemsSearch.simpleQueryForLong();
    }

    public String feedNameOrEmptyString(String name) {
        if (name == null) {
            return "";
        }
        return name;
    }

    public long countAllNewsItems(String service, String feedName) {
        if (TextUtils.isEmptyOrWhitespace(service)) {
            bindAsEmptyString(mCountAllNewsItems, 1, feedName);
            return mCountAllNewsItems.simpleQueryForLong();
        }
        mCountAllNewsItemsService.bindString(1, service);
        bindAsEmptyString(mCountAllNewsItemsService, 2, feedName);
        return mCountAllNewsItemsService.simpleQueryForLong();
    }

    public Map<String, Object> listNewsItems(final String service, String feedName, final String cursor, final long count) {
        T.dontCare();
        final String timstamp = cursor == null ? Long.toString(Long.MAX_VALUE) : cursor;
        final String limit = Long.toString(count);
        final String feed = feedNameOrEmptyString(feedName);

        Cursor c;
        if (TextUtils.isEmptyOrWhitespace(service)) {
            c = mDb.rawQuery(mMainService.getString(R.string.sql_news_list),
                    new String[]{timstamp, feed, limit});
        } else {
            c = mDb.rawQuery(mMainService.getString(R.string.sql_news_list_service),
                    new String[]{service, timstamp, feed, limit});
        }

        List<NewsItem> items = getNewsItemFromCursor(c);
        String newCursor = cursor;
        if (items.size() > 0) {
            long lastTimestamp = items.get(items.size() - 1).sort_timestamp;
            newCursor = Long.toString(lastTimestamp);
        }

        Map<String, Object> r = new HashMap<>();
        r.put("cursor", newCursor);
        r.put("items", items);
        return r;
    }

    private List<NewsItem> getNewsItemFromCursor(Cursor c) {
        T.dontCare();
        List<NewsItem> newsItems = new ArrayList<>();
        try {
            if (!c.moveToFirst()) {
                return newsItems;
            }
            do {
                newsItems.add(readNewsItem(c));
            } while (c.moveToNext());
        } finally {
            c.close();
        }
        return newsItems;
    }

    private List<NewsItemIndex> getNewsItemIndexesFromCursor(Cursor c) {
        T.dontCare();
        List<NewsItemIndex> newsItems = new ArrayList<>();
        try {
            if (!c.moveToFirst()) {
                return newsItems;
            }
            do {
                newsItems.add(readNewsItemIndex(c));
            } while (c.moveToNext());
        } finally {
            c.close();
        }
        return newsItems;
    }

    public List<NewsItemIndex> getNewsBefore(final String feedName, final long sortKey, final long count) {
        T.dontCare();
        final String feed = feedNameOrEmptyString(feedName);

        return DebugUtils.profile("NewsStore.getNewsBefore()", new Callable<List<NewsItemIndex>>() {
            @Override
            public List<NewsItemIndex> call() throws Exception {
                final Cursor c = mDb.rawQuery(mMainService.getString(R.string.sql_news_get_news_before),
                        new String[]{"" + sortKey, feed, "" + count});

                return getNewsItemIndexesFromCursor(c);
            }
        });
    }

    public List<NewsItemIndex> getNewsBefore(String feedName, long sortKey, long count, String qry) {
        T.dontCare();
        final String feed = feedNameOrEmptyString(feedName);

        String query = "%" + qry + "%";
        final Cursor c = mDb.rawQuery(mMainService.getString(R.string.sql_news_get_pinned_news_before),
                new String[]{"" + sortKey, query, query, query, query, query, feed, "" + count});

        return getNewsItemIndexesFromCursor(c);
    }

    public List<NewsItemIndex> getNewsAfter(final String feedName, final long sortKey, final long count) {
        T.dontCare();
        final String feed = feedNameOrEmptyString(feedName);

        return DebugUtils.profile("NewsStore.getNewsAfter()", new Callable<List<NewsItemIndex>>() {
            @Override
            public List<NewsItemIndex> call() throws Exception {
                final Cursor c = mDb.rawQuery(mMainService.getString(R.string.sql_news_get_news_after),
                        new String[]{"" + sortKey, feed, "" + count});

                return getNewsItemIndexesFromCursor(c);
            }
        });
    }


    public List<NewsItemIndex> getNewsAfter(String feedName, long sortKey, long count, String qry) {
        T.dontCare();
        final String feed = feedNameOrEmptyString(feedName);

        String query = "%" + qry + "%";
        final Cursor c = mDb.rawQuery(mMainService.getString(R.string.sql_news_get_pinned_news_after),
                new String[]{"" + sortKey, query, query, query, query, query, feed, "" + count});

        return getNewsItemIndexesFromCursor(c);
    }

    private NewsItem readNewsItem(Cursor c) {
        NewsItem newsItem = new NewsItem();
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
        newsItem.id = c.getLong(19);
        newsItem.sortKey = c.getLong(20);
        addButtons(newsItem);
        addRogeredUsers(newsItem);

        return newsItem;
    }

    private NewsItemIndex readNewsItemIndex(Cursor c) {
        T.dontCare();
        NewsItemIndex newsItemIndex = new NewsItemIndex();
        newsItemIndex.id = c.getLong(0);
        newsItemIndex.sortKey = c.getLong(1);
        newsItemIndex.isPartial = c.getLong(2) > 0;
        newsItemIndex.reach = -1;
        addRogeredUsers(newsItemIndex);
        return newsItemIndex;
    }
}
