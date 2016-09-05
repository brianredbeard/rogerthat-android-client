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

public class GroupTO implements com.mobicage.rpc.IJSONable {

    public String avatar_hash;
    public String guid;
    public String[] members;
    public String name;

    public GroupTO() {
    }

    public GroupTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("avatar_hash")) {
            Object val = json.get("avatar_hash");
            this.avatar_hash = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.GroupTO object is missing field 'avatar_hash'");
        }
        if (json.containsKey("guid")) {
            Object val = json.get("guid");
            this.guid = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.GroupTO object is missing field 'guid'");
        }
        if (json.containsKey("members")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("members");
            if (val_arr == null) {
                this.members = null;
            } else {
                this.members = new String[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    this.members[i] = (String) val_arr.get(i);
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.GroupTO object is missing field 'members'");
        }
        if (json.containsKey("name")) {
            Object val = json.get("name");
            this.name = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.GroupTO object is missing field 'name'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("avatar_hash", this.avatar_hash);
        obj.put("guid", this.guid);
        if (this.members == null) {
            obj.put("members", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.members.length; i++) {
                arr.add(this.members[i]);
            }
            obj.put("members", arr);
        }
        obj.put("name", this.name);
        return obj;
    }

}
