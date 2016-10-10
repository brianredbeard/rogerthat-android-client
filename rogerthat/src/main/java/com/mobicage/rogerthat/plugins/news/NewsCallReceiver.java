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

import android.content.Intent;
import android.os.Bundle;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MainActivity;
import com.mobicage.rogerthat.MainService;
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

        NewsPlugin newsPlugin = mMainService.getPlugin(NewsPlugin.class);

        if (newsPlugin.getStore().getNewsItem(request.news_item.id) == null) {
            newsPlugin.getStore().insertNewsItem(request.news_item);
        }

        Intent intent = new Intent(NewsPlugin.NEW_NEWS_ITEM_INTENT);
        intent.putExtra("id", request.news_item.id);
        mMainService.sendBroadcast(intent);

        String message;
        if (!TextUtils.isEmptyOrWhitespace(request.news_item.title)) {
            message = request.news_item.title;
        } else if (request.news_item.type == NewsItem.TYPE_QR_CODE) {
            message = request.news_item.qr_code_caption;
        } else {
            message = request.news_item.message;
        }

        Bundle b = new Bundle();
        b.putLong("id", request.news_item.id);

        UIUtils.doNotification(mMainService, mMainService.getString(R.string.app_name),
                message, R.integer.news_item,
                MainActivity.ACTION_NOTIFICATION_NEW_NEWS, false, false, true, true,
                R.drawable.notification_icon, 0, b, null, System.currentTimeMillis());


        return response;
    }

    @Override
    public DisableNewsResponseTO disableNews(DisableNewsRequestTO request) throws Exception {
        T.BIZZ();
        DisableNewsResponseTO response = new DisableNewsResponseTO();

        NewsPlugin newsPlugin = mMainService.getPlugin(NewsPlugin.class);
        newsPlugin.getStore().setNewsItemDisabled(request.news_id);

        Intent intent = new Intent(NewsPlugin.DISABLE_NEWS_ITEM_INTENT);
        intent.putExtra("id", request.news_id);
        mMainService.sendBroadcast(intent);

        return response;
    }

}
