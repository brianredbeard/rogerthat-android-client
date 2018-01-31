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

package com.mobicage.to.news;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class NewNewsRequestTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.news.AppNewsItemTO news_item;

    public NewNewsRequestTO() {
    }

    @SuppressWarnings("unchecked")
    public NewNewsRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("news_item")) {
            Object val = json.get("news_item");
            this.news_item = val == null ? null : new com.mobicage.to.news.AppNewsItemTO((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.NewNewsRequestTO object is missing field 'news_item'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("news_item", this.news_item == null ? null : this.news_item.toJSONMap());
        return obj;
    }

}
