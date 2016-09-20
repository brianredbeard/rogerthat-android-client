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

package com.mobicage.to.news;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class NewsReadRequestTO implements com.mobicage.rpc.IJSONable {

    public long[] news_ids;

    public NewsReadRequestTO() {
    }

    public NewsReadRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("news_ids")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("news_ids");
            if (val_arr == null) {
                this.news_ids = null;
            } else {
                this.news_ids = new long[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    this.news_ids[i] = ((Long) val_arr.get(i)).longValue();
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.NewsReadRequestTO object is missing field 'news_ids'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        if (this.news_ids == null) {
            obj.put("news_ids", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.news_ids.length; i++) {
                arr.add(this.news_ids[i]);
            }
            obj.put("news_ids", arr);
        }
        return obj;
    }

}
