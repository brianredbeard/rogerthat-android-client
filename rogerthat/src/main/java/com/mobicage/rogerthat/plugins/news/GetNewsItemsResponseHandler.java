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

import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.pickle.PickleException;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.IResponse;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.news.AppNewsItemTO;
import com.mobicage.to.news.GetNewsItemsResponseTO;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

public class GetNewsItemsResponseHandler extends ResponseHandler<GetNewsItemsResponseTO> {

    private Set<Long> mUpdatedIds = new LinkedHashSet<>();

    public void setUpdatedIds(Set<Long> updatedIds) {
        this.mUpdatedIds = updatedIds;
    }

    @Override
    public void writePickle(DataOutput out) throws IOException {
        T.dontCare();
        super.writePickle(out);
        out.writeUTF(JSONValue.toJSONString(mUpdatedIds));
    }

    @Override
    public void readFromPickle(int version, DataInput in) throws IOException, PickleException {
        T.dontCare();
        super.readFromPickle(version, in);
        final JSONArray object = (JSONArray) JSONValue.parse(in.readUTF());
        for (Object v : object) {
            mUpdatedIds.add((Long) v);
        }
    }

    @Override
    public void handle(IResponse<GetNewsItemsResponseTO> response) {
        T.BIZZ();
        try {
            GetNewsItemsResponseTO resp = response.getResponse();
            NewsPlugin newsPlugin = mMainService.getPlugin(NewsPlugin.class);
            NewsStore newsStore = newsPlugin.getStore();

            long[] ids = new long[resp.items.length];
            long[] versions = new long[resp.items.length];
            for (int i= 0 ; i < resp.items.length; i++) {
                AppNewsItemTO newsItem = resp.items[i];
                newsStore.saveNewsItem(newsItem, mUpdatedIds.contains(newsItem.id));
                ids[i] = newsItem.id;
                versions[i] = newsItem.version;
            }

            Intent intent = new Intent(NewsPlugin.GET_NEWS_ITEMS_RECEIVED_INTENT);
            intent.putExtra("ids", ids);
            intent.putExtra("versions", versions);
            mMainService.sendBroadcast(intent);
        } catch (Exception e) {
            L.e("Server responded with an error.", e);
        }
    }
}
