/*
 * Copyright 2017 GIG Technology NV
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
 * @@license_version:1.3@@
 */

package com.mobicage.rogerthat.plugins.news;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MainActivity;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.to.news.DisableNewsRequestTO;
import com.mobicage.to.news.DisableNewsResponseTO;
import com.mobicage.to.news.NewNewsRequestTO;
import com.mobicage.to.news.NewNewsResponseTO;

public class NewsCallReceiver implements com.mobicage.capi.news.IClientRpc {

    private final NewsPlugin mPlugin;
    private final MainService mMainService;

    public NewsCallReceiver(final MainService pMainService, final NewsPlugin pPlugin) {
        T.UI();
        mPlugin = pPlugin;
        mMainService = pMainService;
    }

    @Override
    public NewNewsResponseTO newNews(NewNewsRequestTO request) throws Exception {
        T.BIZZ();
        NewNewsResponseTO response = new NewNewsResponseTO();

        FriendsPlugin friendsPlugin = mMainService.getPlugin(FriendsPlugin.class);
        boolean showNewsItem = !friendsPlugin.isBroadcastTypeDisabled(request.news_item.sender.email, request.news_item.broadcast_type);

        if (showNewsItem && friendsPlugin.getStore().getExistence(request.news_item.sender.email) != Friend.ACTIVE) {
            showNewsItem = false;
        }

        if (mPlugin.getStore().getNewsItem(request.news_item.id) == null) {
            mPlugin.getStore().insertNewsItem(request.news_item);
            if (showNewsItem) {
                mPlugin.increaseBadgeCount();
            }
        } else {
            return response;
        }

        if (!showNewsItem) {
            return response;
        }

        Intent intent = new Intent(NewsPlugin.NEW_NEWS_ITEM_INTENT);
        intent.putExtra("id", request.news_item.id);
        mMainService.sendBroadcast(intent);
        mPlugin.createNewsNotification(request.news_item);
        return response;
    }

    @Override
    public DisableNewsResponseTO disableNews(DisableNewsRequestTO request) throws Exception {
        T.BIZZ();
        DisableNewsResponseTO response = new DisableNewsResponseTO();

        mPlugin.getStore().setNewsItemDisabled(request.news_id);

        Intent intent = new Intent(NewsPlugin.DISABLE_NEWS_ITEM_INTENT);
        intent.putExtra("id", request.news_id);
        mMainService.sendBroadcast(intent);

        return response;
    }

}
