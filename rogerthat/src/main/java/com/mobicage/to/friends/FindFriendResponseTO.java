/*
 * Copyright 2017 Mobicage NV
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
 * @@license_version:1.2@@
 */

package com.mobicage.to.friends;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class FindFriendResponseTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.friends.FindFriendItemTO[] items;
    public String cursor;
    public String error_string;

    public FindFriendResponseTO() {
    }

    @SuppressWarnings("unchecked")
    public FindFriendResponseTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("items")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("items");
            if (val_arr == null) {
                this.items = null;
            } else {
                this.items = new com.mobicage.to.friends.FindFriendItemTO[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    Object item = val_arr.get(i);
                    if (item != null) {
                        this.items[i] = new com.mobicage.to.friends.FindFriendItemTO((Map<String, Object>) item);
                    }
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.FindFriendResponseTO object is missing field 'items'");
        }
        if (json.containsKey("cursor")) {
            Object val = json.get("cursor");
            this.cursor = (String) val;
        } else {
            this.cursor = null;
        }
        if (json.containsKey("error_string")) {
            Object val = json.get("error_string");
            this.error_string = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.FindFriendResponseTO object is missing field 'error_string'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        if (this.items == null) {
            obj.put("items", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.items.length; i++) {
                arr.add(this.items[i].toJSONMap());
            }
            obj.put("items", arr);
        }
        obj.put("cursor", this.cursor);
        obj.put("error_string", this.error_string);
        return obj;
    }

}
