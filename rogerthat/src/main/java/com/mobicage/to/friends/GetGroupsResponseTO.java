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

package com.mobicage.to.friends;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class GetGroupsResponseTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.friends.GroupTO[] groups;

    public GetGroupsResponseTO() {
    }

    @SuppressWarnings("unchecked")
    public GetGroupsResponseTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("groups")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("groups");
            if (val_arr == null) {
                this.groups = null;
            } else {
                this.groups = new com.mobicage.to.friends.GroupTO[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    Object item = val_arr.get(i);
                    if (item != null) {
                        this.groups[i] = new com.mobicage.to.friends.GroupTO((Map<String, Object>) item);
                    }
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.GetGroupsResponseTO object is missing field 'groups'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        if (this.groups == null) {
            obj.put("groups", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.groups.length; i++) {
                arr.add(this.groups[i].toJSONMap());
            }
            obj.put("groups", arr);
        }
        return obj;
    }

}
