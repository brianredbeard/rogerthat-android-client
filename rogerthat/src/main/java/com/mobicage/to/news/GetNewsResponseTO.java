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

public class GetNewsResponseTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.news.AppNewsInfoTO[] result;
    public String cursor;

    public GetNewsResponseTO() {
    }

    @SuppressWarnings("unchecked")
    public GetNewsResponseTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("result")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("result");
            if (val_arr == null) {
                this.result = null;
            } else {
                this.result = new com.mobicage.to.news.AppNewsInfoTO[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    Object item = val_arr.get(i);
                    if (item != null) {
                        this.result[i] = new com.mobicage.to.news.AppNewsInfoTO((Map<String, Object>) item);
                    }
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.GetNewsResponseTO object is missing field 'result'");
        }
        if (json.containsKey("cursor")) {
            Object val = json.get("cursor");
            this.cursor = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.GetNewsResponseTO object is missing field 'cursor'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        if (this.result == null) {
            obj.put("result", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.result.length; i++) {
                arr.add(this.result[i].toJSONMap());
            }
            obj.put("result", arr);
        }
        obj.put("cursor", this.cursor);
        return obj;
    }

}
