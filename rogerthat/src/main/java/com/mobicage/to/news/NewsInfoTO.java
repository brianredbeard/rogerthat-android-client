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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class NewsInfoTO implements com.mobicage.rpc.IJSONable {

    public long reach;
    public String[] users_that_rogered;

    @SuppressWarnings("unchecked")
    public NewsInfoTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("reach")) {
            Object val = json.get("reach");
            if (val instanceof Integer) {
                this.reach = ((Integer) val).longValue();
            } else {
                this.reach = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.NewsInfoTO object is missing field 'reach'");
        }
        if (json.containsKey("users_that_rogered")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("users_that_rogered");
            if (val_arr == null) {
                this.users_that_rogered = null;
            } else {
                this.users_that_rogered = new String[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    this.users_that_rogered[i] = (String) val_arr.get(i);
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.NewsInfoTO object is missing field 'users_that_rogered'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<>();
        obj.put("reach", this.reach);
        if (this.users_that_rogered == null) {
            obj.put("users_that_rogered", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            Collections.addAll(arr, this.users_that_rogered);
            obj.put("users_that_rogered", arr);
        }
        return obj;
    }

}
