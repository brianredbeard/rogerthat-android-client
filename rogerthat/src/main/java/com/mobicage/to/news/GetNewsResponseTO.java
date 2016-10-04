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

public class GetNewsResponseTO implements com.mobicage.rpc.IJSONable {

    public String cursor;
    public long[] ids;
    public long[] sort_priorities;
    public long[] sort_timestamps;
    public long[] versions;

    public GetNewsResponseTO() {
    }

    public GetNewsResponseTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("cursor")) {
            Object val = json.get("cursor");
            this.cursor = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.GetNewsResponseTO object is missing field 'cursor'");
        }
        if (json.containsKey("ids")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("ids");
            if (val_arr == null) {
                this.ids = null;
            } else {
                this.ids = new long[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    this.ids[i] = ((Long) val_arr.get(i)).longValue();
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.GetNewsResponseTO object is missing field 'ids'");
        }
        if (json.containsKey("sort_priorities")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("sort_priorities");
            if (val_arr == null) {
                this.sort_priorities = null;
            } else {
                this.sort_priorities = new long[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    this.sort_priorities[i] = ((Long) val_arr.get(i)).longValue();
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.GetNewsResponseTO object is missing field 'sort_priorities'");
        }
        if (json.containsKey("sort_timestamps")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("sort_timestamps");
            if (val_arr == null) {
                this.sort_timestamps = null;
            } else {
                this.sort_timestamps = new long[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    this.sort_timestamps[i] = ((Long) val_arr.get(i)).longValue();
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.GetNewsResponseTO object is missing field 'sort_timestamps'");
        }
        if (json.containsKey("versions")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("versions");
            if (val_arr == null) {
                this.versions = null;
            } else {
                this.versions = new long[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    this.versions[i] = ((Long) val_arr.get(i)).longValue();
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.GetNewsResponseTO object is missing field 'versions'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("cursor", this.cursor);
        if (this.ids == null) {
            obj.put("ids", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.ids.length; i++) {
                arr.add(this.ids[i]);
            }
            obj.put("ids", arr);
        }
        if (this.sort_priorities == null) {
            obj.put("sort_priorities", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.sort_priorities.length; i++) {
                arr.add(this.sort_priorities[i]);
            }
            obj.put("sort_priorities", arr);
        }
        if (this.sort_timestamps == null) {
            obj.put("sort_timestamps", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.sort_timestamps.length; i++) {
                arr.add(this.sort_timestamps[i]);
            }
            obj.put("sort_timestamps", arr);
        }
        if (this.versions == null) {
            obj.put("versions", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.versions.length; i++) {
                arr.add(this.versions[i]);
            }
            obj.put("versions", arr);
        }
        return obj;
    }

}
