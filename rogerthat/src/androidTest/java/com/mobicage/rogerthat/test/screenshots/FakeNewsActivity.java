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

package com.mobicage.rogerthat.test.screenshots;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.NewsActivity;
import com.mobicage.rogerthat.NewsListAdapter;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.news.NewsItem;
import com.mobicage.rogerthat.plugins.news.NewsPlugin;
import com.mobicage.rpc.config.AppConstants;
import com.mobicage.to.news.AppNewsItemTO;
import com.mobicage.to.news.NewsActionButtonTO;
import com.mobicage.to.news.NewsSenderTO;

import java.util.Random;

// Also used in util.ui.TestUtils to see if we are currently running test or not
public class FakeNewsActivity extends NewsActivity {

    private AppNewsItemTO createNewsItem(int id, String title, String message, String broadcast_type, String email,
                                         String sender, NewsActionButtonTO[] buttons, String[] users_that_rogered,
                                         long type, int timestamp, int sortTimestamp, int sortPriority,
                                         String qrCodeContent, String qrCodeCaption, int flags, String imageUrl) {
        AppNewsItemTO newsItem = new AppNewsItemTO();
        newsItem.id = id;
        newsItem.title = title;
        newsItem.message = message;
        newsItem.broadcast_type = broadcast_type;
        newsItem.users_that_rogered = users_that_rogered;
        newsItem.type = type;
        newsItem.timestamp = timestamp;
        newsItem.sort_timestamp = sortTimestamp;
        newsItem.sort_priority = sortPriority;
        newsItem.version = 1;
        NewsSenderTO newsSender = new NewsSenderTO();
        newsSender.avatar_id = 1L;
        newsSender.email = email;
        newsSender.name = sender;
        newsItem.sender = newsSender;
        newsItem.buttons = buttons;
        newsItem.flags = flags;
        newsItem.image_url = imageUrl;
        newsItem.qr_code_caption = qrCodeCaption;
        newsItem.qr_code_content = qrCodeContent;
        newsItem.reach = new Random().nextInt((5000 - 5) + 1) + 5;
        return newsItem;
    }

    @Override
    protected void onServiceBound() {
        //noinspection ConstantConditions
        if (AppConstants.HOME_ACTIVITY_LAYOUT != R.layout.news) {
            return;
        }
        newsPlugin = mService.getPlugin(NewsPlugin.class);
        friendsPlugin = mService.getPlugin(FriendsPlugin.class);
        // Insert some fake news items in the database
        int flags = NewsListAdapter.FLAG_ACTION_FOLLOW | NewsListAdapter.FLAG_ACTION_ROGERTHAT;
        int now = (int) System.currentTimeMillis();
        NewsActionButtonTO actionButton = new NewsActionButtonTO();
        actionButton.action = "https://google.com";
        actionButton.caption = "Website";
        actionButton.id = "action_button";
        actionButton.flow_params = null;
        NewsActionButtonTO[] actionButtons = new NewsActionButtonTO[]{actionButton};

        AppNewsItemTO fakeNews1 = createNewsItem(1,
                getString(R.string.sample_news_item_title_1),
                getString(R.string.sample_news_item_message_1),
                getString(R.string.news),
                "sample.service@rogerthat.net",
                getString(R.string.sample_news_item_sender_1),
                actionButtons,
                new String[]{},
                NewsItem.TYPE_NORMAL,
                now++,
                now,
                10,
                null,
                null,
                flags,
                null);
        AppNewsItemTO fakeNews2 = createNewsItem(2,
                getString(R.string.sample_news_item_title_2),
                getString(R.string.sample_news_item_message_2),
                getString(R.string.news),
                "sample.service@rogerthat.net",
                getString(R.string.sample_news_item_sender_2),
                new NewsActionButtonTO[]{},
                new String[]{},
                NewsItem.TYPE_NORMAL,
                now++,
                now,
                10,
                null,
                null,
                flags,
                null);
        newsPlugin.getStore().clearNewsItems();
        newsPlugin.getStore().insertNewsItem(fakeNews1);
        newsPlugin.getStore().insertNewsItem(fakeNews2);
        super.onServiceBound();
    }
}
