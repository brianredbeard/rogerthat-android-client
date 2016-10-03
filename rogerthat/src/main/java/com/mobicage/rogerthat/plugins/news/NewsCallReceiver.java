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

import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.to.news.DisableNewsRequestTO;
import com.mobicage.to.news.DisableNewsResponseTO;

public class NewsCallReceiver implements com.mobicage.capi.news.IClientRpc {

    private final NewsPlugin mPlugin;
    private final MainService mMainService;

    public NewsCallReceiver(final MainService pMainService, final NewsPlugin pPlugin) {
        T.UI();
        mPlugin = pPlugin;
        mMainService = pMainService;
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
