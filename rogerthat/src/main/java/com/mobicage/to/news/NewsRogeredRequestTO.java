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

package com.mobicage.to.news;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class NewsRogeredRequestTO implements com.mobicage.rpc.IJSONable {

    public long news_id;

    public NewsRogeredRequestTO() {
    }

    public NewsRogeredRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("news_id")) {
            Object val = json.get("news_id");
            if (val instanceof Integer) {
                this.news_id = ((Integer) val).longValue();
            } else {
                this.news_id = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.NewsRogeredRequestTO object is missing field 'news_id'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("news_id", this.news_id);
        return obj;
    }

}
