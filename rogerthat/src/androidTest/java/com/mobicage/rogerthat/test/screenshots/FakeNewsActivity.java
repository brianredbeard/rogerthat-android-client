/*
 * Copyright 2017 Mobicage NV
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
 * @@license_version:1.2@@
 */

package com.mobicage.rogerthat.test.screenshots;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.NewsActivity;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.news.NewsItem;
import com.mobicage.rogerthat.plugins.news.NewsPlugin;
import com.mobicage.rogerthat.util.CachedDownloader;
import com.mobicage.rogerthat.util.IOUtils;
import com.mobicage.rogerthat.util.Security;
import com.mobicage.rpc.config.AppConstants;
import com.mobicage.rpc.config.CloudConstants;
import com.mobicage.to.news.AppNewsItemTO;
import com.mobicage.to.news.NewsActionButtonTO;
import com.mobicage.to.news.NewsSenderTO;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

// Also used in util.ui.TestUtils to see if we are currently running test or not
public class FakeNewsActivity extends NewsActivity {

    private CachedDownloader mCachedDownloader;

    private static int randInt(int min, int max) {
        return new Random().nextInt((max - min) + 1) + min;
    }

    private AppNewsItemTO createNewsItem(int id, String title, String message, String broadcastType, String email,
                                         String sender, NewsActionButtonTO[] buttons, String[] users_that_rogered,
                                         long type, int timestamp, int sortTimestamp, int sortPriority,
                                         String qrCodeContent, String qrCodeCaption, long flags, String imageUrl,
                                         long avatar_id) {
        AppNewsItemTO newsItem = new AppNewsItemTO();
        newsItem.id = id;
        newsItem.title = title;
        newsItem.message = message;
        newsItem.broadcast_type = broadcastType;
        newsItem.users_that_rogered = users_that_rogered;
        newsItem.type = type;
        newsItem.timestamp = timestamp;
        newsItem.sort_timestamp = sortTimestamp;
        newsItem.sort_priority = sortPriority;
        newsItem.version = 1;
        NewsSenderTO newsSender = new NewsSenderTO();
        newsSender.avatar_id = avatar_id;
        newsSender.email = email;
        newsSender.name = sender;
        newsItem.sender = newsSender;
        newsItem.buttons = buttons;
        newsItem.flags = flags;
        newsItem.image_url = imageUrl;
        newsItem.qr_code_caption = qrCodeCaption;
        newsItem.qr_code_content = qrCodeContent;
        newsItem.reach = randInt(2000, 5000);
        return newsItem;
    }

    private File getCachedFile(String url) {
        final String urlHash = Security.sha256(url);
        File file = mCachedDownloader.getCachedDownloadDir();
        return new File(file, urlHash);
    }

    private void copyToCache(int source, String url) {
        File destination = getCachedFile(url);
        OutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(destination));
            try {
                InputStream in = getInstrumentation().getContext().getResources().openRawResource(source);
                try {
                    IOUtils.copy(in, out, 1024);
                } finally {
                    in.close();
                }
            } finally {
                out.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onServiceBound() {
        //noinspection ConstantConditions
        if (AppConstants.HOME_ACTIVITY_LAYOUT != R.layout.news) {
            return;
        }
        newsPlugin = mService.getPlugin(NewsPlugin.class);
        friendsPlugin = mService.getPlugin(FriendsPlugin.class);
        mCachedDownloader = CachedDownloader.getInstance(mService);

        // Insert some fake news items in the database
        long sampleItemAvatarId1 = 5782144819396608L;
        long sampleItemAvatarId2 = 4995569435017216L;

        int sampleItem1HeaderFile1 = com.mobicage.rogerth.at.debug.test.R.drawable.sample_news_header1;
        int sampleItem1HeaderFile2 = com.mobicage.rogerth.at.debug.test.R.drawable.sample_news_header2;
        int sampleItem1AvatarFile1 = com.mobicage.rogerth.at.debug.test.R.drawable.sample_news_avatar1;
        int sampleItem1AvatarFile2 = com.mobicage.rogerth.at.debug.test.R.drawable.sample_news_avatar2;

        String itemImageUrl1 = CloudConstants.HTTPS_BASE_URL + "/unauthenticated/news/image/41157003";
        String itemImageUrl2 = CloudConstants.HTTPS_BASE_URL + "/unauthenticated/news/image/44387003";

        copyToCache(sampleItem1HeaderFile1, itemImageUrl1);
        copyToCache(sampleItem1HeaderFile2, itemImageUrl2);

        copyToCache(sampleItem1AvatarFile1, CloudConstants.CACHED_AVATAR_URL_PREFIX + sampleItemAvatarId1);
        copyToCache(sampleItem1AvatarFile2, CloudConstants.CACHED_AVATAR_URL_PREFIX + sampleItemAvatarId2);

        long flags = NewsItem.FLAG_ACTION_FOLLOW | NewsItem.FLAG_ACTION_ROGERTHAT;
        int now = (int) System.currentTimeMillis();
        NewsActionButtonTO actionButton = new NewsActionButtonTO();
        actionButton.action = "http://www.aquariumneon.be/";
        actionButton.caption = getString(R.string.website);
        actionButton.id = "action_button";
        actionButton.flow_params = null;
        NewsActionButtonTO[] actionButtons = new NewsActionButtonTO[]{actionButton};

        AppNewsItemTO fakeNews2 = createNewsItem(2,
                getString(R.string.sample_news_title_2),
                getString(R.string.sample_news_message_2),
                getString(R.string.promo),
                "sample.service@rogerthat.net",
                "La Tapa Canaria",
                actionButtons,
                new String[]{},
                NewsItem.TYPE_NORMAL,
                now++,
                now,
                10,
                null,
                null,
                flags,
                itemImageUrl2,
                sampleItemAvatarId2);
        AppNewsItemTO fakeNews1 = createNewsItem(1,
                null,
                getString(R.string.sample_news_message_1),
                getString(R.string.news),
                "sample.service@rogerthat.net",
                "Aquarium Neon",
                new NewsActionButtonTO[]{},
                new String[]{},
                NewsItem.TYPE_QR_CODE,
                now++,
                now,
                10,
                "{'c':5629499534213120}",
                getString(R.string.sample_news_title_1),
                flags,
                itemImageUrl1,
                sampleItemAvatarId1);
        newsPlugin.getStore().clearNewsItems();
        newsPlugin.getStore().insertNewsItem(fakeNews1);
        newsPlugin.getStore().insertNewsItem(fakeNews2);
        super.onServiceBound();
    }
}
