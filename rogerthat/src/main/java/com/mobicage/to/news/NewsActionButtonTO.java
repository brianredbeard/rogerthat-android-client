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

public class NewsActionButtonTO implements com.mobicage.rpc.IJSONable {

    public String action;
    public String caption;
    public String flow_params;
    public String id;

    public NewsActionButtonTO() {
    }

    public NewsActionButtonTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("action")) {
            Object val = json.get("action");
            this.action = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.NewsActionButtonTO object is missing field 'action'");
        }
        if (json.containsKey("caption")) {
            Object val = json.get("caption");
            this.caption = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.NewsActionButtonTO object is missing field 'caption'");
        }
        if (json.containsKey("flow_params")) {
            Object val = json.get("flow_params");
            this.flow_params = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.NewsActionButtonTO object is missing field 'flow_params'");
        }
        if (json.containsKey("id")) {
            Object val = json.get("id");
            this.id = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.NewsActionButtonTO object is missing field 'id'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("action", this.action);
        obj.put("caption", this.caption);
        obj.put("flow_params", this.flow_params);
        obj.put("id", this.id);
        return obj;
    }

}
